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
	private int frameCount;
	private float secondsPerFrame;
	private float timeLength;

	//Keyframes
	private Map<Integer, List<Keyframe>> keyframes; // Given a bone index, return keyframes associated with that bone
	private Map<Integer, Map<Keyframe, Keyframe>> nextKeyframes; // Given a bone index, get a map from each keyframe to the next chronological keyframe
	
	public ModelAnimation(String name, int frameCount, float secondsPerFrame, Map<Integer, List<Keyframe>> keyframes) {
		this.name = name;
		this.transition = false;
		this.index = 0;
		
		this.oughtLoop = false;
		
		this.frameCount = frameCount;
		this.secondsPerFrame = secondsPerFrame;
		this.timeLength = (float)(frameCount) * secondsPerFrame;
		
		this.keyframes = keyframes;
		
		this.nextKeyframes = new HashMap<>();
		for(Integer boneIndex : this.keyframes.keySet()) {
			List<Keyframe> boneFrames = this.keyframes.get(boneIndex);
			
			Map<Keyframe, Keyframe> next = new HashMap<>();
			for(int i = 0; i < boneFrames.size(); i++) {
				next.put(boneFrames.get(i), boneFrames.get((i+1) % boneFrames.size()));
			}
			
			this.nextKeyframes.put(boneIndex, next);
		}
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
	
	public int getFrameCount() {
		return frameCount;
	}

	public float getSecondsPerFrame() {
		return secondsPerFrame;
	}
	
	public float getTimeLength() {
		return timeLength;
	}
	
	public Keyframe getKeyframe(int boneIndex, int keyframeIndex) {
		return keyframes.get(boneIndex).get(keyframeIndex);
	}
	
	public Keyframe getKeyframe(int boneIndex, float time) {
		List<Keyframe> boneKeyframes = keyframes.get(boneIndex);
		int currentIndex = this.getCurrentKeyframeIndex(boneKeyframes, time);
		return boneKeyframes.get(currentIndex);
	}
	
	public Keyframe getNextKeyframe(int boneIndex, Keyframe keyframe) {
		if(this.nextKeyframes.containsKey(boneIndex)) return this.nextKeyframes.get(boneIndex).get(keyframe);
		return null;
	}
	
	/**
	 * Gets the keyframe in the given list corresponding to the given time
	 * Times less than zero will clamp to keyframe zero, and times greater than
	 * the animation's total time will clamp to the final keyframe
	 * @param keyframes The list of keyframes to evaluate
	 * @param currentTime The time of the current model animation
	 * @return The keyframe in the list that is active at the given time
	 */
	private int getCurrentKeyframeIndex(List<Keyframe> keyframes, float currentTime) {	
		for(int i = keyframes.size() - 1; i >= 0; i--) {
			if(currentTime > keyframes.get(i).getTime()) return i;
		}
		
		return 0;
	}
	
	@Override
	public String toString() {
		return "Model Animation \"" + name + "\"";
	}
}
