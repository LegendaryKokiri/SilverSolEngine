package silverSol.engine.render.animation.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModelAnimation {
	
	//Metadata
	private String name;
	private boolean transition;
	private int index;
	
	//Looping
	private boolean oughtLoop;
	
	//Animation Length
	private float animationTimeLength;
	
	//Takes in the bone's index and returns the keyframes associated with that bone.
	private Map<Integer, List<Keyframe>> keyframes;
	
	public ModelAnimation() {
		this.name = "";
		this.transition = false;
		this.index = 0;
		
		this.oughtLoop = false;
		this.animationTimeLength = 0;
		this.keyframes = new HashMap<>();
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public boolean isTransition() {
		return transition;
	}

	public void setTransition(boolean transition) {
		this.transition = transition;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public boolean oughtLoop() {
		return oughtLoop;
	}
	
	public void setOughtLoop(boolean oughtLoop) {
		this.oughtLoop = oughtLoop;
	}

	public float getAnimationTimeLength() {
		return animationTimeLength;
	}
	
	public void setAnimationTimeLength(float animationTimeLength) {
		this.animationTimeLength = animationTimeLength;
	}

	public Map<Integer, List<Keyframe>> getKeyframes() {
		return keyframes;
	}
	
	public List<Keyframe> getKeyframes(int boneIndex) {
		return keyframes.get(boneIndex);
	}
	
	public void addKeyframes(int boneIndex, List<Keyframe> keyframes) {
		this.keyframes.put(boneIndex, keyframes);
	}
	
	public void setKeyframes(Map<Integer, List<Keyframe>> keyframes) {
		this.keyframes = keyframes;
	}
	
	public Keyframe[] getActiveKeyframes(int boneIndex, float currentFrame) {
		List<Keyframe> boneKeyframes = keyframes.get(boneIndex);
		
		int currentIndex = Keyframe.getCurrentKeyframeIndex(boneKeyframes, currentFrame);
		int nextIndex = (currentIndex == boneKeyframes.size() - 1) ? ((oughtLoop) ? 0 : currentIndex) : currentIndex + 1; 
		
		return new Keyframe[]{boneKeyframes.get(currentIndex), boneKeyframes.get(nextIndex)};
	}
	
	@Override
	public String toString() {
		return "Model Animation \"" + name + "\"";
	}
}
