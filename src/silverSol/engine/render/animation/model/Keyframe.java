package silverSol.engine.render.animation.model;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Vector3f;

public class Keyframe {
		
	//The frame and time at which the keyframe is perfectly realized
	private int frame;
	private float time;
	
	//The transformation of the bone at the specified frame in bone space
	private Vector3f position;
	private Quaternion quaternion;
	private Vector3f scale;
	
	public Keyframe(int frame, float fps, Vector3f position, Matrix4f rotation, Vector3f scale) {		
		this.frame = frame;
		this.time = (float) frame / fps;
		
		this.position = position;
		this.quaternion = Quaternion.setFromMatrix(rotation, null);
		this.scale = scale;	
	}
	
	public Keyframe(int frame, float fps, Vector3f position, Quaternion quaternion, Vector3f scale) {		
		this.frame = frame;
		this.time = (float) frame / fps;
		
		this.position = position;
		this.quaternion = quaternion;
		this.scale = scale;
	}

	public int getFrame() {
		return frame;
	}
	
	public float getTime() {
		return time;
	}
	
	public Vector3f getPosition() {
		return position;
	}
	
	public Quaternion getQuaternion() {
		return quaternion;
	}
	
	public Vector3f getScale() {
		return scale;
	}
	
	public static float getProximityToNextFrame(Keyframe currentFrame, Keyframe nextFrame, float currentTime, float animationTimeLength) {
		//For this to be possible, we have to be approaching the time at which the animation loops.
		if(currentFrame == nextFrame) return 1f;
		
		float progressionFactor = (currentFrame.time > nextFrame.time)
			? (float) -Math.cos(Math.PI * (currentTime - currentFrame.time) / (animationTimeLength - currentFrame.time))
			: (float) -Math.cos(Math.PI * (currentTime - currentFrame.time) / (nextFrame.time - currentFrame.time));

		return (progressionFactor + 1f) / 2f;
	}
	
	@Override
	public String toString() {
		return "Keyframe at frame " + frame + ": " + position + " " + quaternion;
	}
}
