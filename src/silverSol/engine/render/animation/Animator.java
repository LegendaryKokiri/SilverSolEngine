package silverSol.engine.render.animation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Quaternion;
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
	private int modelIndex;
	private ModelAnimation modelAnimation;
	private int modelTransitionIndex;
	
	//Model Animation Progression
	private float modelTime;
	private int modelFrame;
	private int modelLoops;
	private Map<Integer, Matrix4f> localTransforms;
	private Map<Integer, Matrix4f> modelTransforms;
	private Map<Integer, Matrix4f> parentTransforms;
	
	//Armature
	private Bone armature;
	
	//External Animation Controls
	private Map<Integer, Matrix4f> localControls;
	
	//Texture Animation
	private boolean animatingTexture;
	private List<TextureAnimation> textureAnimations;
	private int textureIndex;
	private TextureAnimation textureAnimation;
	
	//Texture Animation Progression
	private float textureTime;
	private int textureFrame;
	private int textureLoops;
	
	public Animator() {
		this.animatingModel = false;
		this.modelAnimations = new ArrayList<>();
		
		this.localTransforms = new HashMap<>();
		this.modelTransforms = new HashMap<>();
		this.parentTransforms = new HashMap<>();
		
		this.localControls = new HashMap<>();
		
		this.animatingTexture = false;
		this.textureAnimations = new ArrayList<>();
	}

	public void update(float dt) {
		animateModel(dt);
		animateTexture(dt);
	}
	
	public void animateModel(float dt) {
		if(animatingModel && armature != null) {			
			progressModelTime(modelAnimation, dt);
			interpolateArmature(armature, modelAnimation);
			positionArmature(armature, IDENTITY_MATRIX);
		}
	}
	
	private void progressModelTime(ModelAnimation animation, float dt) {
		modelTime += dt;
		
		if(modelTime > animation.getTimeLength()) {
			modelLoops++;
			if(animation.oughtLoop()) modelTime %= animation.getTimeLength();
			if(animation.isTransition()) setModelAnimation(modelTransitionIndex);
		}
		
		modelFrame = (int) (modelTime / animation.getSecondsPerFrame());
	}
	
	private void interpolateArmature(Bone armature, ModelAnimation animation) {
		interpolateBone(armature, animation);
		for(Bone child : armature.getChildren()) {
			interpolateArmature(child, animation);
		}
	}
	
	private void interpolateBone(Bone bone, ModelAnimation animation) {
		int boneIndex = bone.getIndex();
		
		Keyframe[] activeKeyframes = animation.getActiveKeyframes(boneIndex, modelTime);
		Keyframe currentPose = activeKeyframes[0];
		Keyframe nextPose = activeKeyframes[1];
		
		float proximity = Keyframe.getProximityToNextFrame(currentPose, nextPose, modelTime, animation.getTimeLength());
			
		//Scale, rotate, and translate the matrix
		Vector3f translation = VectorMath.interpolate(currentPose.getPosition(), nextPose.getPosition(), proximity);
		Quaternion rotation = QuaternionMath.interpolate(currentPose.getQuaternion(), nextPose.getQuaternion(), proximity, null);
		Vector3f scale = VectorMath.interpolate(currentPose.getScale(), nextPose.getScale(), proximity);
		
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
	
	public void controlBone(int boneIndex, Matrix4f localTransform) {
		localControls.get(boneIndex).load(localTransform);
	}
	
	public void transitionModelAnimation(int animationIndex, int framesToTransition) {
		ModelAnimation target = modelAnimations.get(animationIndex);
		Map<Integer, List<Keyframe>> keyframes = calculateTransition(target, armature, framesToTransition, 1f / target.getSecondsPerFrame());
		
		ModelAnimation transition = new ModelAnimation("", framesToTransition, target.getSecondsPerFrame(), keyframes);
		transition.setTransition(true);
		transition.setOughtLoop(false);
				
		this.modelAnimation = transition;
		this.modelIndex = -1;
		
		resetModelAnimation();
		
		this.modelTransitionIndex = animationIndex;
	}
	
	private Map<Integer, List<Keyframe>> calculateTransition(ModelAnimation target, Bone armature, int framesToTransition, float fps) {
		Map<Integer, List<Keyframe>> keyframes = new HashMap<>();
		calculateTransition(keyframes, target, armature, framesToTransition, fps);
		return keyframes;
	}
	
	private void calculateTransition(Map<Integer, List<Keyframe>> keyframes, ModelAnimation target, Bone armature, int framesToTransition, float fps) {
		int boneIndex = armature.getIndex();
		
		List<Keyframe> boneFrames = new ArrayList<>();
		Keyframe start = getTransitionStart(armature, fps);
		Keyframe end = getTransitionEnd(target, armature, framesToTransition, fps);
		
		boneFrames.add(start);
		boneFrames.add(end);
		
		keyframes.put(boneIndex, boneFrames);
		
		for(Bone child : armature.getChildren()) {
			calculateTransition(keyframes, target, child, framesToTransition, fps);
		}
	}
	
	private Keyframe getTransitionStart(Bone bone, float fps) {
		int boneIndex = bone.getIndex();
		
		Keyframe[] activeKeyframes = modelAnimation.getActiveKeyframes(boneIndex, modelTime);
		Keyframe currentPose = activeKeyframes[0];
		Keyframe nextPose = activeKeyframes[1];
		
		float proximityToCurrent = Keyframe.getProximityToNextFrame(currentPose, nextPose, modelTime, modelAnimation.getTimeLength());
			
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
	
	public void animateTexture(float dt) {
		if(animatingTexture) {
			textureTime += dt;
			if(textureTime > textureAnimation.getAnimationTimeLength()) textureLoops++;
			textureTime %= textureAnimation.getAnimationTimeLength();
			
			float frameProgression = textureTime / textureAnimation.getSecondsPerFrame();			
			textureFrame = (int) (frameProgression % textureAnimation.getNumberOfFrames());
		}
	}
	
	public boolean isAnimatingModel() {
		return animatingModel;
	}

	public void setAnimatingModel(boolean animating) {
		this.animatingModel = animating;
	}
	
	public ModelAnimation getModelAnimation() {
		return modelAnimation;
	}
	
	public int getModelAnimationIndex() {
		return modelIndex;
	}

	public void setModelAnimation(int animationIndex) {
		if(this.modelIndex == animationIndex) return;
		this.modelIndex = animationIndex;
		this.modelAnimation = this.modelAnimations.get(animationIndex);
		resetModelAnimation();
	}
	
	public List<ModelAnimation> getModelAnimations() {
		return modelAnimations;
	}
	
	public void setModelAnimations(Bone armature, List<ModelAnimation> modelAnimations) {
		this.armature = armature;
		this.modelAnimations.clear();
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
			
			this.modelAnimation = this.modelAnimations.get(0);
			resetModelAnimation();
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
	
	public int getModelFrame() {
		return modelFrame;
	}
	
	public void setModelFrame(int modelFrame) {
		this.modelFrame = Math.max(modelFrame, 0) % modelAnimation.getFrameCount();
		this.modelTime = this.modelFrame * modelAnimation.getSecondsPerFrame();
		animateModel(0f);
	}
	
	public boolean modelAnimated() {
		return modelLoops > 0;
	}
	
	public int getModelLoops() {
		return modelLoops;
	}
	
	public boolean modelIsTransitioning() {
		return modelAnimation.isTransition();
	}
	
	public boolean isAnimatingTexture() {
		return animatingTexture;
	}

	public void setAnimatingTexture(boolean animatingTexture) {
		this.animatingTexture = animatingTexture;
	}
	
	public TextureAnimation getTextureAnimation() {
		return textureAnimation;
	}
	
	public int getTextureAnimationIndex() {
		return textureIndex;
	}
	
	public void setTextureAnimation(int animationIndex) {
		this.textureIndex = animationIndex;
		this.textureAnimation = textureAnimations.get(textureIndex);
		this.textureLoops = 0;
	}
	
	public List<TextureAnimation> getTextureAnimations() {
		return textureAnimations;
	}

	public void setTextureAnimations(List<TextureAnimation> textureAnimations) {
		this.textureAnimations = textureAnimations;		
		this.animatingTexture = this.textureAnimations != null && this.textureAnimations.size() > 0;
		
		if(this.animatingTexture) {
			this.textureAnimation = this.textureAnimations.get(0);
			resetTextureAnimation();
		}
	}
	
	public int getTextureFrame() {
		return textureFrame;
	}
	
	public void setTextureFrame(int textureFrame) {
		this.textureFrame = Math.max(textureFrame, 0) % textureAnimation.getNumberOfFrames();
		this.textureTime = ((float) this.textureFrame) * textureAnimation.getSecondsPerFrame();
		animateTexture(0f);
	}

	public float[] getTextureFrameBounds() {
		return textureAnimation.getFrameBounds(textureFrame);
	}
	
	public boolean textureAnimated() {
		return textureLoops > 0;
	}
	
	public int getTextureLoops() {
		return textureLoops;
	}
	
	public void resetAnimation() {
		resetModelAnimation();
		resetTextureAnimation();
	}
	
	public void resetModelAnimation() {
		setModelFrame(0);
		modelLoops = 0;
	}
	
	public void resetTextureAnimation() {
		setTextureFrame(0);
		textureLoops = 0;
	}
	
}
