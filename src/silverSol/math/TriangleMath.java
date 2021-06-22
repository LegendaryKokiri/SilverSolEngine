package silverSol.math;

import org.lwjgl.util.vector.Vector3f;

import silverSol.math.geometry.Triangle;

public class TriangleMath {
	
	private static final float EPSILON = 1E-3f;
	
	public static boolean contains(Vector3f point, Triangle tri) {
		return contains(point, tri.getT0(), tri.getT1(), tri.getT2());
	}
	
	public static boolean contains(Vector3f p, Vector3f tri0, Vector3f tri1, Vector3f tri2) {
		if(!PlaneMath.contains(p, tri0, tri1, tri2)) return false;
		
		Vector3f edge01 = Vector3f.sub(tri1, tri0, null);
		Vector3f edge02 = Vector3f.sub(tri2, tri0, null);
		Vector3f normal = Vector3f.cross(edge01, edge02, null);
		Vector3f displacement = Vector3f.sub(p, tri0, null);
		
		float nSquared = normal.lengthSquared();
		
		float gamma = Vector3f.dot(Vector3f.cross(edge01, displacement, null), normal) / nSquared;
		float beta = Vector3f.dot(Vector3f.cross(displacement, edge02, null), normal) / nSquared;
		float alpha = 1 - beta - gamma;
		
		return 0 <= alpha && alpha <= 1 && 0 <= beta && beta <= 1 && 0 <= gamma && gamma <= 1;
	}
	
	public static Vector3f rayIntersection(Vector3f origin, Vector3f direction, Vector3f tri0, Vector3f tri1, Vector3f tri2) {
		return rayIntersection(origin, direction, new Triangle(tri0, tri1, tri2, false));
	}
	
	public static Vector3f rayIntersection(Vector3f origin, Vector3f direction, Triangle tri) {
		Vector3f normal = tri.getNormal();
		
		float denominator = Vector3f.dot(normal, direction);
		if(Math.abs(denominator) > EPSILON) {
			float t = Vector3f.dot(Vector3f.sub(tri.getT0(), origin, null), normal) / denominator;
			if(t >= 0) {
				Vector3f planeIntersection = (Vector3f.add(origin, VectorMath.mul(direction, t, null), null));
				return contains(planeIntersection, tri) ? planeIntersection : null;
			}
		}
		
		return null;
	}
	
	public static Vector3f closestPointTo(Vector3f p, Triangle tri) {
		return closestPointTo(p, tri.getT0(), tri.getT1(), tri.getT2());
	}
	
	public static Vector3f closestPointTo(Vector3f p, Vector3f tri0, Vector3f tri1, Vector3f tri2) {
		Vector3f edge01 = Vector3f.sub(tri1, tri0, null);
		if(edge01.lengthSquared() < EPSILON) return SegmentMath.closestPointTo(p, tri0, tri2);
		
		Vector3f edge02 = Vector3f.sub(tri2, tri0, null);
		if(edge02.lengthSquared() < EPSILON) return SegmentMath.closestPointTo(p, tri0, tri1);
		
		Vector3f edge12 = Vector3f.sub(tri2, tri1, null);
		if(edge12.lengthSquared() < EPSILON) return SegmentMath.closestPointTo(p, tri0, tri1);
		
		Vector3f displacement = Vector3f.sub(tri0, p, null);
		if(displacement.lengthSquared() < EPSILON) return new Vector3f(tri0);
		
		float[] weights = getParametricWeights(edge01, edge02, edge12, displacement);
		
		return Vector3f.add(tri0, Vector3f.add(VectorMath.mul(edge01, weights[0], null),
				VectorMath.mul(edge02, weights[1], null), null), null);
	}
	
	public static float[] getParametricWeights(Vector3f edge01, Vector3f edge02, Vector3f edge12, Vector3f displacement) {
		float a = Vector3f.dot(edge01, edge01);
		float b = Vector3f.dot(edge01, edge02);
		float c = Vector3f.dot(edge02, edge02);
		float d = Vector3f.dot(displacement, edge01);
		float e = Vector3f.dot(displacement, edge02);
		
		float delta = a * c - b * b;
		float s = b * e - c * d;
		float t = b * d - a * e;
		
		if(s + t < delta) {
			if(s < 0f) {
				if(t < 0f) {
					if(d < 0f) {
						s = -d / a;
						t = 0f;
					} else {
						s = 0f;
						t = -e / c;
					}
				} else {
					s = 0f;
					t = -e / c;
				}
			} else if(t < 0f) {
				s = -d / a;
				t = 0f;
			} else {
				float inverseDeterminant = 1f / delta;
				s *= inverseDeterminant;
				t *= inverseDeterminant;
			}
		} else {			
			if(s < 0f) {
				float temp0 = b + d;
				float temp1 = c + e;
				if(temp1 > temp0) {
					float numerator = temp1 - temp0;
					float denominator = a - (2f * b) + c;
					s = numerator / denominator;
					t = 1f - s;
				} else {
					s = 0f;
					t = -e / c;
				}
			} else if(t < 0f) {
				if(a + d > b + e) {
					float numerator = c + e - b - d;
					float denominator = a - (2f * b) + c;
					s = numerator / denominator;
					t = 1f - s;
				} else {
					s = -e / c;
					t = 0f;
				}
			} else {
				float numerator = c + e - b - d;
				float denominator = a - (2f * b) + c;
				s = numerator / denominator;
				t = 1f - s;
			}
		}
		
		return new float[]{s, t};
	}
	
	public static void main(String[] args) {
		Vector3f origin = new Vector3f();
		Triangle degenerate = new Triangle(new Vector3f(0f, 0f, 1f), new Vector3f(0f, 0f, 6f), new Vector3f(0f, 0f, -2.5f), false);
		System.out.println("Degenerate Triangle? " + degenerate.isDegenerate());
		System.out.println("Closest Point = " + closestPointTo(origin, degenerate));
		
		Triangle test2 = new Triangle(new Vector3f(0f, 0f, 0f), new Vector3f(2f, 0f, 0f), new Vector3f(2f, 3f, 0f), false);
		System.out.println(contains(new Vector3f(0f, 0f, 0f), test2));
	}
	
}
