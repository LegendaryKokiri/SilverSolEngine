package silverSol.engine.render.animation.texture;

import java.util.List;

import org.lwjgl.util.vector.Vector2f;

public class TextureAnimation {

	//Looping
	private boolean oughtLoop;
	
	//Frames
	private List<Vector2f> frameOffsets;
	
	//Frame Progression
	private int numberOfFrames;
	
	//Time Progression
	private float secondsPerFrame;
	private float animationTimeLength;
	
	public TextureAnimation(List<Vector2f> frameOffsets, float fps) {
		this.oughtLoop = false;
		this.frameOffsets = frameOffsets;
		this.numberOfFrames = frameOffsets.size();
		this.secondsPerFrame = 1f / fps;
		this.animationTimeLength = secondsPerFrame * numberOfFrames;
	}
	
	public boolean getOughtLoop() {
		return oughtLoop;
	}
	
	public void setOughtLoop(boolean oughtLoop) {
		this.oughtLoop = oughtLoop;
	}

	public List<Vector2f> getFrames() {
		return frameOffsets;
	}

	public void setFrames(List<Vector2f> frames) {
		this.frameOffsets = frames;
	}
	
	public Vector2f getFrameOffset(int frame) {
		return frameOffsets.get(frame);
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
