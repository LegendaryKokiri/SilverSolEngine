package silverSol.engine.render.animation.model;

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
	
	//Takes in the bone's index and returns the keyframes associated with that bone.
	private Map<Integer, List<Keyframe>> keyframes;
	
	public ModelAnimation(String name, int frameCount, float secondsPerFrame, Map<Integer, List<Keyframe>> keyframes) {
		this.name = name;
		this.transition = false;
		this.index = 0;
		
		this.oughtLoop = false;
		
		this.frameCount = frameCount;
		this.secondsPerFrame = secondsPerFrame;
		this.timeLength = (float)(frameCount) * secondsPerFrame;
		
		this.keyframes = keyframes;
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

	public Map<Integer, List<Keyframe>> getKeyframes() {
		return keyframes;
	}
	
	public List<Keyframe> getKeyframes(int boneIndex) {
		return keyframes.get(boneIndex);
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
