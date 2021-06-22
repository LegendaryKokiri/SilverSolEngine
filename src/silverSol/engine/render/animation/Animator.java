package silverSol.engine.render.animation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import silverSol.engine.render.animation.model.Keyframe;
import silverSol.engine.render.animation.model.ModelAnimation;
import silverSol.engine.render.animation.texture.TextureAnimation;
import silverSol.engine.render.armature.Bone;
import silverSol.math.QuaternionMath;
import silverSol.math.VectorMath;

public class Animator {

	private static final Matrix4f IDENTITY_MATRIX = new Matrix4f();
	
	//Model Animation
	private boolean animatingModel;
	private List<ModelAnimation> modelAnimations;
	private int currentModelAnimationIndex;
	private ModelAnimation currentModelAnimation;
	private int transitionModelAnimationIndex;
	
	//Model Animation Armature
	private Bone armature;
	
	//Model Animation Progression
	private float modelAnimationTime;
	private float modelFrameProgression;
	private int modelAnimationPlayCount;
	private Map<Integer, Matrix4f> localTransforms;
	private Map<Integer, Matrix4f> modelTransforms;
	private Map<Integer, Matrix4f> parentTransforms;
	
	//External Animation Controls
	private Map<Integer, Matrix4f> localControls;
	
	//Texture Animation
	private boolean animatingTexture;
	private List<TextureAnimation> textureAnimations;
	private int currentTextureAnimationIndex;
	
	//Texture Animation Progression
	private float textureAnimationTime;
	private float textureFrameProgression;
	private int textureAnimationPlayCount;
	private int textureCurrentFrame;
	private int textureNextFrame;
	
	public Animator() {
		this.animatingModel = false;
		this.modelAnimations = new ArrayList<>();
		this.currentModelAnimationIndex = 0;
		
		this.modelAnimationTime = 0;
		this.modelFrameProgression = 0;
		this.modelAnimationPlayCount = 0;
		this.localTransforms = new HashMap<>();
		this.modelTransforms = new HashMap<>();
		this.localControls = new HashMap<>();
		this.parentTransforms = new HashMap<>();
		
		this.animatingTexture = false;
		this.textureAnimations = new ArrayList<>();
		this.currentTextureAnimationIndex = 0;
		
		this.textureAnimationTime = 0;
		this.textureFrameProgression = 0;
		this.textureAnimationPlayCount = 0;
		this.textureCurrentFrame = 0;
		this.textureNextFrame = 0;
	}

	public void progressAnimation(float dt) {
		progressModelAnimation(dt);
		progressTextureAnimation(dt);
	}
	
	public void progressModelAnimation(float dt) {
		if(animatingModel && armature != null) {			
			progressModelAnimationTime(currentModelAnimation, dt);
			interpolateArmatureFromAnimation(armature, currentModelAnimation);
			positionArmature(armature, IDENTITY_MATRIX);
		}
	}
	
	private void progressModelAnimationTime(ModelAnimation animation, float dt) {
		modelAnimationTime += dt;
		if(modelAnimationTime > animation.getAnimationTimeLength()) {
			modelAnimationPlayCount++;
			if(animation.oughtLoop()) modelAnimationTime %= animation.getAnimationTimeLength();
			if(animation.isTransition()) setCurrentModelAnimation(transitionModelAnimationIndex);
		}
	}
	
	private void interpolateArmatureFromAnimation(Bone armature, ModelAnimation animation) {
		interpolateBoneFromAnimation(armature, animation);
		for(Bone child : armature.getChildren()) {
			interpolateArmatureFromAnimation(child, animation);
		}
	}
	
	private void interpolateBoneFromAnimation(Bone bone, ModelAnimation animation) {
		int boneIndex = bone.getIndex();
		
		Keyframe[] activeKeyframes = animation.getActiveKeyframes(boneIndex, modelAnimationTime);
		Keyframe currentPose = activeKeyframes[0];
		Keyframe nextPose = activeKeyframes[1];
		
		float proximityToCurrent = Keyframe.getProximityToNextFrame(currentPose, nextPose, modelAnimationTime, animation.getAnimationTimeLength());
			
		//Scale, rotate, and translate the matrix
		Vector3f translation = VectorMath.interpolate(currentPose.getPosition(), nextPose.getPosition(), proximityToCurrent);
		Quaternion rotation = QuaternionMath.interpolate(currentPose.getQuaternion(), nextPose.getQuaternion(), proximityToCurrent, null);
		Vector3f scale = VectorMath.interpolate(currentPose.getScale(), nextPose.getScale(), proximityToCurrent);
		
		Matrix4f boneTransformation = localTransforms.get(boneIndex);
		boneTransformation.setIdentity();
		boneTransformation.scale(scale);
		boneTransformation.translate(translation);
		Matrix4f.mul(boneTransformation, QuaternionMath.getMatrix4f(rotation), boneTransformation);
	}
	
	private void positionArmature(Bone bone, Matrix4f parentTransformation) {
		//Calculate model-space transformation of each bone
		Matrix4f localTransformation = localTransforms.get(bone.getIndex());
		Matrix4f controlTransformation = localControls.get(bone.getIndex());
		Matrix4f modelTransformation = modelTransforms.get(bone.getIndex());
		parentTransforms.put(bone.getIndex(), parentTransformation);
		
		Matrix4f controlLocal = Matrix4f.mul(localTransformation, controlTransformation, null);
		Matrix4f.mul(parentTransformation, controlLocal, modelTransformation);
		
		for(Bone child : bone.getChildren()) {
			positionArmature(child, modelTransformation);
		}
		
		//Calculate the transformation applied by the animation in model space
		Matrix4f.mul(modelTransformation, bone.getInverseBindTransformation(), modelTransformation);
	}
	
	public void progressTextureAnimation(float dt) {
		if(animatingTexture) {
			TextureAnimation animation = textureAnimations.get(currentTextureAnimationIndex);
			textureAnimationTime += dt;
			if(textureAnimationTime > animation.getAnimationTimeLength()) textureAnimationPlayCount++;
			textureAnimationTime %= animation.getAnimationTimeLength();
			
			float frameProgression = textureAnimationTime / animation.getSecondsPerFrame();
			
			textureFrameProgression = frameProgression % 1;
			
			textureCurrentFrame = (int) (frameProgression % animation.getNumberOfFrames());
			textureNextFrame = (textureCurrentFrame + 1) % animation.getNumberOfFrames();	
		}
	}
	
	public void controlBone(int boneIndex, Matrix4f localTransform) {
		localControls.get(boneIndex).load(localTransform);
	}
	
	public void resetAnimation() {
		resetModelAnimation();
		resetTextureAnimation();
	}
	
	public void resetModelAnimation() {
		modelAnimationTime = modelFrameProgression = modelAnimationPlayCount = 0;
	}
	
	public void resetTextureAnimation() {
		textureAnimationTime = textureFrameProgression = textureAnimationPlayCount = 0;
		setTextureCurrentFrame(0);
	}
	
	public boolean isAnimatingModel() {
		return animatingModel;
	}

	public void setAnimatingModel(boolean animating) {
		this.animatingModel = animating;
	}

	public boolean isAnimatingTexture() {
		return animatingTexture;
	}

	public void setAnimatingTexture(boolean animatingTexture) {
		this.animatingTexture = animatingTexture;
	}

	public List<ModelAnimation> getModelAnimations() {
		return modelAnimations;
	}

	public void setModelAnimations(Bone armature, List<ModelAnimation> modelAnimations) {
		this.armature = armature;
		this.modelAnimations = new ArrayList<>();
			this.modelAnimations.addAll(modelAnimations);
		this.animatingModel = armature != null && this.modelAnimations.size() > 0;		
		
		if(animatingModel) {
			for(ModelAnimation animation : modelAnimations) {
				Set<Integer> boneIndices = animation.getKeyframes().keySet();
				for(int boneIndex : boneIndices) {
					if(!localTransforms.containsKey(boneIndex)) localTransforms.put(boneIndex, new Matrix4f());
					if(!modelTransforms.containsKey(boneIndex)) modelTransforms.put(boneIndex, new Matrix4f());
					if(!localControls.containsKey(boneIndex)) localControls.put(boneIndex, new Matrix4f());
					if(!parentTransforms.containsKey(boneIndex)) parentTransforms.put(boneIndex, new Matrix4f());
				}
			}
			
			this.currentModelAnimation = this.modelAnimations.get(0);
		}
	}
	
	public Map<Integer, Matrix4f> getLocalTransforms() {
		return localTransforms;
	}
	
	public Matrix4f getLocalTransform(int boneIndex) {
		return localTransforms.get(boneIndex);
	}
	
	public Map<Integer, Matrix4f> getModelTransforms() {
		return modelTransforms;
	}
	
	public Matrix4f getModelTransform(int boneIndex) {
		return modelTransforms.get(boneIndex);
	}
	
	public Map<Integer, Matrix4f> getParentTransforms() {
		return parentTransforms;
	}
	
	public Matrix4f getParentTransform(int boneIndex) {
		return parentTransforms.get(boneIndex);
	}

	public List<TextureAnimation> getTextureAnimations() {
		return textureAnimations;
	}

	public void setTextureAnimations(List<TextureAnimation> textureAnimations) {
		this.textureAnimations = textureAnimations;
		if(this.textureAnimations != null && this.textureAnimations.size() > 0) this.animatingTexture = true;
	}
	
	public ModelAnimation getCurrentModelAnimation() {
		return currentModelAnimation;
	}
	
	public TextureAnimation getCurrentTextureAnimation() {
		return textureAnimations.get(currentTextureAnimationIndex);
	}

	public int getCurrentModelAnimationIndex() {
		return currentModelAnimationIndex;
	}

	public void setCurrentModelAnimation(int animationIndex) {
		if(this.currentModelAnimationIndex == animationIndex) return;
		this.currentModelAnimationIndex = animationIndex;
		this.currentModelAnimation = this.modelAnimations.get(animationIndex);
		resetModelAnimation();
	}
	
	public void transitionToAnimation(int animationIndex, int framesToTransition, float fps) {				
		ModelAnimation transition = new ModelAnimation();
		transition.setTransition(true);
		transition.setAnimationTimeLength(((float) framesToTransition) / fps);
		transition.setOughtLoop(false);
		
		addTransitionKeyframes(transition, modelAnimations.get(animationIndex), armature, framesToTransition, fps);
		
		this.currentModelAnimation = transition;
		this.currentModelAnimationIndex = -1;
		
		resetModelAnimation();
		
		this.transitionModelAnimationIndex = animationIndex;
	}
	
	private void addTransitionKeyframes(ModelAnimation transition, ModelAnimation target, Bone armature, int framesToTransition, float fps) {
		List<Keyframe> keyframes = new ArrayList<>();
		int boneIndex = armature.getIndex();
		
		keyframes.add(getTransitionStart(armature, fps));
		keyframes.add(getTransitionEnd(target, armature, framesToTransition, fps));
		
		transition.addKeyframes(boneIndex, keyframes);
		
		for(Bone child : armature.getChildren()) {
			addTransitionKeyframes(transition, target, child, framesToTransition, fps);
		}
	}
	
	private Keyframe getTransitionStart(Bone bone, float fps) {
		int boneIndex = bone.getIndex();
		
		Keyframe[] activeKeyframes = currentModelAnimation.getActiveKeyframes(boneIndex, modelAnimationTime);
		Keyframe currentPose = activeKeyframes[0];
		Keyframe nextPose = activeKeyframes[1];
		
		float proximityToCurrent = Keyframe.getProximityToNextFrame(currentPose, nextPose, modelAnimationTime, currentModelAnimation.getAnimationTimeLength());
			
		//Scale, rotate, and translate the matrix
		Vector3f translation = VectorMath.interpolate(currentPose.getPosition(), nextPose.getPosition(), proximityToCurrent);
		Quaternion rotation = QuaternionMath.interpolate(currentPose.getQuaternion(), nextPose.getQuaternion(), proximityToCurrent, null);
		Vector3f scale = VectorMath.interpolate(currentPose.getScale(), nextPose.getScale(), proximityToCurrent);
		
		return new Keyframe(0, fps, translation, rotation, scale);
	}
	
	private Keyframe getTransitionEnd(ModelAnimation target, Bone bone, int frame, float fps) {
		Keyframe end = target.getKeyframes(bone.getIndex()).get(0);
		return new Keyframe(frame, fps, end.getPosition(), end.getQuaternion(), end.getScale());
	}

	public boolean modelIsTransitioning() {
		return currentModelAnimation.isTransition();
	}
	
	public int getCurrentTextureAnimationIndex() {
		return currentTextureAnimationIndex;
	}

	public void setCurrentTextureAnimation(int currentTextureAnimationIndex) {
		this.currentTextureAnimationIndex = currentTextureAnimationIndex;
		this.textureAnimationPlayCount = 0;
	}
	
	//TODO: Add a setModelCurrentFrame() function
	
	public int getTextureCurrentFrame() {
		return textureCurrentFrame;
	}

	public int getTextureNextFrame() {
		return textureNextFrame;
	}
	
	public void setTextureCurrentFrame(int currentFrame) {
		this.textureCurrentFrame = currentFrame;
		this.textureNextFrame = (currentFrame + 1) % textureAnimations.get(currentTextureAnimationIndex).getNumberOfFrames();
	}

	public Vector2f getTextureCurrentFrameOffset() {
		return textureAnimations.get(currentTextureAnimationIndex).getFrameOffset(textureCurrentFrame);
	}
	
	public Vector2f getTextureNextFrameOffset() {
		return textureAnimations.get(currentTextureAnimationIndex).getFrameOffset(textureNextFrame);
	}
	
	public float getModelAnimationTime() {
		return modelAnimationTime;
	}

	public void setModelAnimationTime(float modelAnimationTime) {
		this.modelAnimationTime = modelAnimationTime;
	}

	public float getTextureAnimationTime() {
		return textureAnimationTime;
	}

	public void setTextureAnimationTime(float textureAnimationTime) {
		this.textureAnimationTime = textureAnimationTime;
	}

	public float getModelFrameProgression() {
		return modelFrameProgression;
	}
	
	public float getTextureFrameProgression() {
		return textureFrameProgression;
	}
	
	public boolean modelAnimationHasPlayed() {
		return modelAnimationPlayCount > 0;
	}
	
	public boolean textureAnimationHasPlayed() {
		return textureAnimationPlayCount > 0;
	}
	
	public int getModelAnimationPlayCount() {
		return modelAnimationPlayCount;
	}
	
	public int getTextureAnimationPlayCount() {
		return textureAnimationPlayCount;
	}
	
}
