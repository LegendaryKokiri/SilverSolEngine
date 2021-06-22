package silverSol.math;

import org.lwjgl.util.vector.Vector3f;

public class PlaneMath {
	
	private static final float EPSILON = 1E-6f;

	public static boolean contains(Vector3f testPoint, Vector3f p0, Vector3f p1, Vector3f p2) {
		return contains(testPoint, p0, Vector3f.cross(Vector3f.sub(p1, p0, null), Vector3f.sub(p2, p0, null), null).normalise(null));
	}
	
	public static boolean contains(Vector3f testPoint, Vector3f p0, Vector3f normal) {
		return Math.abs(Vector3f.dot(normal, Vector3f.sub(testPoint, p0, null))) < EPSILON;
	}
	
	public static Vector3f rayIntersection(Vector3f origin, Vector3f direction, Vector3f p0, Vector3f p1, Vector3f p2) {
		Vector3f edge01 = Vector3f.sub(p1, p0, null);
		Vector3f edge12 = Vector3f.sub(p2, p1, null);
		Vector3f normal = Vector3f.cross(edge12, edge01, null);
		return rayIntersection(origin, direction, p0, normal);
	}
	
	public static Vector3f rayIntersection(Vector3f origin, Vector3f direction, Vector3f p0, Vector3f normal) {
		float denominator = Vector3f.dot(normal, direction);
		if(Math.abs(denominator) > EPSILON) {
			float t = Vector3f.dot(Vector3f.sub(p0, origin, null), normal) / denominator;
			if(t >= 0) return Vector3f.add(origin, VectorMath.mul(direction, t, null), null);
		}
		
		return null;
	}
	
}
