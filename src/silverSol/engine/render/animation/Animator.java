package silverSol.engine.render.animation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Vector3f;

import silverSol.engine.render.animation.model.Keyframe;
import silverSol.engine.render.animation.model.ModelAnimation;
import silverSol.engine.render.animation.model.TPose;
import silverSol.engine.render.animation.texture.TextureAnimation;
import silverSol.engine.render.armature.Bone;
import silverSol.math.QuaternionMath;
import silverSol.math.VectorMath;

public class Animator {

	private static final Matrix4f IDENTITY_MATRIX = new Matrix4f();
	private static final ModelAnimation T_POSE = new TPose();
	
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

	/**
	 * Progresses this model's armature and texture animations
	 * @param dt The time step in seconds
	 */
	public void update(float dt) {
		animateModel(dt);
		animateTexture(dt);
	}
	
	/**
	 * Progresses this model's armature animation
	 * @param dt The time step in seconds
	 */
	public void animateModel(float dt) {
		if(animatingModel && armature != null) {			
			progressModelTime(modelAnimation, dt);
			interpolateArmature(armature, modelAnimation);
			positionArmature(armature);
		}
	}
	
	/**
	 * Progresses the time and frame information for the active information
	 * If the animation completes, appropriately loops the animation or activates the transition target animation
	 * @param animation The animation to progress through
	 * @param dt The time step in seconds
	 */
	private void progressModelTime(ModelAnimation animation, float dt) {
		modelTime += dt;
		
		if(modelTime > animation.getTimeLength()) {
			modelLoops++;
			modelTime = animation.oughtLoop() ? modelTime % animation.getTimeLength() : animation.getTimeLength();
			if(animation.isTransition()) setModelAnimation(modelTransitionIndex);
		}
		
		modelFrame = (int) (modelTime / animation.getSecondsPerFrame());
	}
	
	/**
	 * Interpolates the armature's bone transformations according to the active animation
	 * @param armature The armature to transform
	 * @param animation The animation to conform to
	 */
	private void interpolateArmature(Bone armature, ModelAnimation animation) {
		interpolateBone(armature, animation);
		for(Bone child : armature.getChildren()) {
			interpolateArmature(child, animation);
		}
	}
	
	/**
	 * Calculates the given bone's local transformations according to the active animation
	 * Stores the results in this.localTransforms
	 * @param bone The bone to transform
	 * @param animation The animation to conform to
	 */
	private void interpolateBone(Bone bone, ModelAnimation animation) {
		int boneIndex = bone.getIndex();
		
		Keyframe currentPose = modelAnimation.getKeyframe(boneIndex, modelTime);
		Keyframe nextPose = modelAnimation.getNextKeyframe(boneIndex, currentPose);
		
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
	
	/**
	 * Applies the transformations in this.localTransforms and this.localControls to the given armature
	 * @param armature The armature to transform
	 */
	private void positionArmature(Bone armature) {
		this.positionArmature(armature, IDENTITY_MATRIX);
	}
	
	/**
	 * Applies the transformations in this.localTransforms and this.localControls to the given bone
	 * @param armature The bone to transform
	 * @param parentTransformation The transformation of the parent bone
	 */
	private void positionArmature(Bone bone, Matrix4f parentTransformation) {
		//Calculate model-space transformation of each bone
		Matrix4f localTransformation = localTransforms.get(bone.getIndex());
		Matrix4f controlTransformation = localControls.get(bone.getIndex());
		Matrix4f modelTransformation = modelTransforms.get(bone.getIndex());
		parentTransforms.put(bone.getIndex(), parentTransformation);
		
		Matrix4f controlLocal = Matrix4f.mul(localTransformation, controlTransformation, null);
		Matrix4f.mul(parentTransformation, controlLocal, modelTransformation);
		
		for(Bone child : bone.getChildren()) {
			this.positionArmature(child, modelTransformation);
		}
		
		//Calculate the transformation applied by the animation in model space
		Matrix4f.mul(modelTransformation, bone.getInverseBindTransformation(), modelTransformation);
	}
	
	/**
	 * Sets the local-space transformation to be applied to the given bone
	 * @param boneIndex The bone to apply the transformation to
	 * @param localTransform The transformation to apply
	 */
	public void controlBone(int boneIndex, Matrix4f localTransform) {
		localControls.get(boneIndex).load(localTransform);
	}
	
	/**
	 * Transitions the armature from its current position to the first frame of the given animation
	 * The transition occurs over the specified number of frames at an FPS equal to the target animation's FPS
	 * @param animationIndex The index of the animation to transition to
	 * @param framesToTransition The number of frames over which the transition should take place
	 */
	public void transitionModelAnimation(int animationIndex, int framesToTransition) {
		ModelAnimation target = this.getModelAnimation(animationIndex);
		Map<Integer, List<Keyframe>> keyframes = calculateTransition(target, armature, framesToTransition, 1f / target.getSecondsPerFrame());
		
		ModelAnimation transition = new ModelAnimation("Transition", framesToTransition, target.getSecondsPerFrame(), keyframes);
		transition.setTransition(true);
		transition.setOughtLoop(false);
		
		this.modelAnimation = transition;
		this.modelTransitionIndex = animationIndex;
		this.modelIndex = -1;
		
		resetModelAnimation();
	}
	
	/**
	 * Calculates the keyframes to transition to the target animation
	 * @param target The animation to transition to
	 * @param armature The armature to apply the animation to
	 * @param framesToTransition The number of frames over which the transition should take place
	 * @param fps The FPS rate of the transition animation
	 * @return A mapping of bone indices to the keyframes in the transition animation
	 */
	private Map<Integer, List<Keyframe>> calculateTransition(ModelAnimation target, Bone armature, int framesToTransition, float fps) {
		Map<Integer, List<Keyframe>> keyframes = new HashMap<>();
		calculateTransition(keyframes, target, armature, framesToTransition, fps);
		return keyframes;
	}
	
	/**
	 * Calculates a mapping of bone indices to keyframes to transition to the target animation
	 * The results are stored in the given Map
	 * @param keyframes The Map in which to store the results
	 * @param target The animation to transition to
	 * @param armature The armature to apply the animation to
	 * @param framesToTransition The number of frames over which the transition should take place
	 * @param fps The FPS rate of the transition animation
	 */
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
	
	/**
	 * Calculate the Keyframe at which a transition should begin
	 * @param bone The bone to calculate the Keyframe for
	 * @param fps The FPS rate of the transition animation
	 * @return The Keyframe at which a transition should begin
	 */
	private Keyframe getTransitionStart(Bone bone, float fps) {
		int boneIndex = bone.getIndex();
		
		Keyframe currentPose = modelAnimation.getKeyframe(boneIndex, modelTime);
		Keyframe nextPose = modelAnimation.getNextKeyframe(boneIndex, currentPose);
		
		float proximityToCurrent = Keyframe.getProximityToNextFrame(currentPose, nextPose, modelTime, modelAnimation.getTimeLength());
			
		//Scale, rotate, and translate the matrix
		Vector3f translation = VectorMath.interpolate(currentPose.getPosition(), nextPose.getPosition(), proximityToCurrent);
		Quaternion rotation = QuaternionMath.interpolate(currentPose.getQuaternion(), nextPose.getQuaternion(), proximityToCurrent, null);
		Vector3f scale = VectorMath.interpolate(currentPose.getScale(), nextPose.getScale(), proximityToCurrent);
		
		return new Keyframe(0, fps, translation, rotation, scale);
	}
	
	/**
	 * Calculate the Keyframe at which a transition to the given animation should end
	 * @param target The animation to transition to
	 * @param bone The bone to calculate the Keyframe for
	 * @param framesToTransition The number of frames over which the transition should take place
	 * @param fps The FPS rate of the transition animation
	 * @return
	 */
	private Keyframe getTransitionEnd(ModelAnimation target, Bone bone, int framesToTransition, float fps) {
		Keyframe end = target.getKeyframe(bone.getIndex(), 0);
		return new Keyframe(framesToTransition, fps, end.getPosition(), end.getQuaternion(), end.getScale());
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
	
	public ModelAnimation getModelAnimation(int animationIndex) {
		if(animationIndex >= 0 && animationIndex < this.modelAnimations.size()) return this.modelAnimations.get(animationIndex);
		System.err.println("ERROR: Animator.getModelAnimation(): Index " + animationIndex + " does not correspond to any stored animation.");
		return T_POSE;
	}

	public void setModelAnimation(int animationIndex) {
		if(this.modelIndex == animationIndex) return;
		this.modelIndex = animationIndex;
		this.modelAnimation = this.getModelAnimation(animationIndex);
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
			for(int boneIndex : armature.getBoneIndices()) {
				if(!localTransforms.containsKey(boneIndex)) localTransforms.put(boneIndex, new Matrix4f());
				if(!modelTransforms.containsKey(boneIndex)) modelTransforms.put(boneIndex, new Matrix4f());
				if(!localControls.containsKey(boneIndex)) localControls.put(boneIndex, new Matrix4f());
				if(!parentTransforms.containsKey(boneIndex)) parentTransforms.put(boneIndex, new Matrix4f());
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
