package silverSol.engine.render.animation.model;

import java.util.List;

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
	private Matrix4f localTransformationMatrix;
	
	public Keyframe(int frame, float fps, Vector3f position, Matrix4f rotation, Vector3f scale) {		
		this.frame = frame;
		this.time = (float) frame / fps;
		
		this.position = position;
		this.quaternion = Quaternion.setFromMatrix(rotation, null);
		this.scale = scale;
		
		this.localTransformationMatrix = new Matrix4f();
			localTransformationMatrix.translate(position);
			Matrix4f.mul(localTransformationMatrix, rotation, localTransformationMatrix);
			Matrix4f.scale(scale, localTransformationMatrix, localTransformationMatrix);
			
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
	
	public static int getCurrentKeyframeIndex(List<Keyframe> keyframes, float currentTime) {	
		for(int i = keyframes.size() - 1; i >= 0; i--) {
			if(currentTime > keyframes.get(i).getTime()) return i;
		}
		
		return 0;
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
