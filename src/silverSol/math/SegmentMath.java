package silverSol.math;

import org.lwjgl.util.vector.Vector3f;

public class SegmentMath {
	
	private static final float EPSILON = 1E-6f;

	public static Vector3f closestPointTo(Vector3f point, Vector3f s1, Vector3f s2) {
		Vector3f segment = Vector3f.sub(s2, s1, null);
		float segmentSq = segment.lengthSquared();
		if(segmentSq < EPSILON) return new Vector3f(s1);
		
		Vector3f direction = segment.normalise(null);
		
		Vector3f displacement = Vector3f.sub(point, s1, null);
		float t = Vector3f.dot(displacement, direction);
		
		if(t * t > segmentSq) return Vector3f.add(s1, segment, null);
		return Vector3f.add(s1, VectorMath.mul(direction, NumberMath.clamp(t, 0f, segment.length()), null), null);
	}
	
	public static Vector3f rayIntersection(Vector3f origin, Vector3f direction, Vector3f s1, Vector3f s2) {
		Vector3f segment = Vector3f.sub(s2, s1, null);
		Vector3f perp = Vector3f.cross(direction, segment, null);
		float perpLengthSq = perp.lengthSquared();
		if(perpLengthSq < EPSILON) return null;
		
		Vector3f dispCross = Vector3f.cross(Vector3f.sub(s1, origin, null), segment, null);
		float dispCrossLengthSq = dispCross.lengthSquared();
		
		float dot = Vector3f.dot(perp, dispCross);
		if(dot * dot - dispCrossLengthSq * perpLengthSq < -EPSILON) return null;
		
		float t = (float) (Math.sqrt(dispCrossLengthSq) / Math.sqrt(perpLengthSq));
		
		return Vector3f.add(origin, VectorMath.mul(direction, t, null), null);
	}
	
}
