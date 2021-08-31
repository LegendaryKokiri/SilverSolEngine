package silverSol.engine.render.animation.texture;

import java.util.List;

public class TextureAnimation {

	//Looping
	private boolean oughtLoop;
	
	//Frames
	private List<float[]> frameBounds;
	
	//Frame Progression
	private int numberOfFrames;
	
	//Time Progression
	private float secondsPerFrame;
	private float animationTimeLength;
	
	public TextureAnimation(List<float[]> frameBounds, float fps) {
		this.oughtLoop = false;
		this.frameBounds = frameBounds;
		this.numberOfFrames = frameBounds.size();
		this.secondsPerFrame = 1f / fps;
		this.animationTimeLength = secondsPerFrame * numberOfFrames;
	}
	
	public boolean getOughtLoop() {
		return oughtLoop;
	}
	
	public void setOughtLoop(boolean oughtLoop) {
		this.oughtLoop = oughtLoop;
	}
	
	public float[] getFrameBounds(int frame) {
		return frameBounds.get(frame);
	}
	
	public int getNumberOfFrames() {
		return numberOfFrames;
	}
	
	public float getSecondsPerFrame() {
		return secondsPerFrame;
	}

	public void setSecondsPerFrame(float secondsPerFrame) {
		this.secondsPerFrame = secondsPerFrame;
	}

	public float getAnimationTimeLength() {
		return animationTimeLength;
	}

	public void setAnimationTimeLength(float animationTimeLength) {
		this.animationTimeLength = animationTimeLength;
	}
	
}
