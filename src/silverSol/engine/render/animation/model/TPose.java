package silverSol.engine.render.animation.model;

import java.util.HashMap;

import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Vector3f;

public class TPose extends ModelAnimation {
	
	private static final Keyframe DEFAULT_KEYFRAME = new Keyframe(0, 1f, new Vector3f(), new Quaternion(), new Vector3f(1f, 1f, 1f));
	
	public TPose() {
		super("T-Pose", 1, 1f, new HashMap<>());
	}
	
	@Override
	public Keyframe getKeyframe(int boneIndex, int frame) {
		return DEFAULT_KEYFRAME;
	}
	
	@Override
	public Keyframe getKeyframe(int boneIndex, float time) {
		return DEFAULT_KEYFRAME;
	}
	
	@Override
	public Keyframe getNextKeyframe(int boneIndex, Keyframe keyframe) {
		return DEFAULT_KEYFRAME;
	}
	
}
