package silverSol.math;

import org.lwjgl.util.vector.Vector3f;

public class TetrahedronMath {

	public static boolean contains(Vector3f point, Vector3f t0, Vector3f t1, Vector3f t2, Vector3f t3) {
		return sameSide(point, t0, t1, t2, t3) &&
				sameSide(point, t1, t2, t3, t0) &&
				sameSide(point, t2, t3, t0, t1) &&
				sameSide(point, t3, t0, t1, t2);
	}
	
	private static boolean sameSide(Vector3f point, Vector3f t0, Vector3f t1, Vector3f t2, Vector3f t3) {
		Vector3f normal = Vector3f.cross(Vector3f.sub(t1, t0, null), Vector3f.sub(t2, t0, null), null);
		float dot3 = Vector3f.dot(normal, Vector3f.sub(t3, t0, null));
		float dotPoint = Vector3f.dot(normal, Vector3f.sub(point, t0, null));
		return Math.signum(dot3) == Math.signum(dotPoint);
	}
	
	public static int closestFaceTo(Vector3f point, Vector3f t0, Vector3f t1, Vector3f t2, Vector3f t3) {
		Vector3f t012 = TriangleMath.closestPointTo(point, t0, t1, t2);
		Vector3f t123 = TriangleMath.closestPointTo(point, t1, t2, t3);
		Vector3f t230 = TriangleMath.closestPointTo(point, t2, t3, t0);
		Vector3f t301 = TriangleMath.closestPointTo(point, t3, t0, t1);
		return NumberMath.minIndex(t012.lengthSquared(), t123.lengthSquared(), t230.lengthSquared(), t301.lengthSquared());
	}
	
	public static Vector3f closestPointTo(Vector3f point, Vector3f t0, Vector3f t1, Vector3f t2, Vector3f t3) {
		Vector3f t012 = TriangleMath.closestPointTo(point, t0, t1, t2);
		Vector3f t123 = TriangleMath.closestPointTo(point, t1, t2, t3);
		Vector3f t230 = TriangleMath.closestPointTo(point, t2, t3, t0);
		Vector3f t301 = TriangleMath.closestPointTo(point, t3, t0, t1);
		return VectorMath.shortest(t012, t123, t230, t301);
	}
	
}
