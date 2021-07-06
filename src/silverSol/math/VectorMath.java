package silverSol.math;

import java.util.List;

import org.lwjgl.util.vector.Matrix3f;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

public class VectorMath {
		
	public static boolean getEqual(Vector2f v1, Vector2f v2) {
		return v1.x == v2.x && v1.y == v2.y;
	}
	
	public static boolean getEqual(Vector3f v1, Vector3f v2) {
		return v1.x == v2.x && v1.y == v2.y && v1.z == v2.z;
	}
	
	public static boolean getEqual(Vector4f v1, Vector4f v2) {
		return v1.x == v2.x && v1.y == v2.y && v1.z == v2.z && v1.w == v2.w;
	}
	
	public static boolean hasNaN(Vector2f v) {
		return Float.isNaN(v.x) || Float.isNaN(v.y);
	}
	
	public static boolean hasNaN(Vector3f v) {
		return Float.isNaN(v.x) || Float.isNaN(v.y) || Float.isNaN(v.z);
	}
	
	public static boolean hasNaN(Vector4f v) {
		return Float.isNaN(v.x) || Float.isNaN(v.y) || Float.isNaN(v.z) || Float.isNaN(v.w);
	}
	
	public static Vector2f abs(Vector2f v, Vector2f dest) {
		Vector2f abs = new Vector2f(Math.abs(v.x), Math.abs(v.y));
		if(dest != null) dest.set(abs);
		return abs;
	}
	
	public static Vector3f abs(Vector3f v, Vector3f dest) {
		Vector3f abs = new Vector3f(Math.abs(v.x), Math.abs(v.y), Math.abs(v.z));
		if(dest != null) dest.set(abs);
		return abs;
	}
	
	public static Vector4f abs(Vector4f v, Vector4f dest) {
		Vector4f abs = new Vector4f(Math.abs(v.x), Math.abs(v.y), Math.abs(v.z), Math.abs(v.w));
		if(dest != null) dest.set(abs);
		return abs;
	}
	
	public static Vector2f min(Vector2f v1, Vector2f v2, Vector2f dest) {
		Vector2f v = new Vector2f(Math.min(v1.x, v2.x), Math.min(v1.y, v2.y));
		if(dest != null) dest.set(v);
		return v;
	}
	
	public static Vector3f min(Vector3f v1, Vector3f v2, Vector3f dest) {
		Vector3f v = new Vector3f(Math.min(v1.x, v2.x), Math.min(v1.y, v2.y), Math.min(v1.z, v2.z));
		if(dest != null) dest.set(v);
		return v;
	}
	
	public static Vector4f min(Vector4f v1, Vector4f v2, Vector4f dest) {
		Vector4f v = new Vector4f(Math.min(v1.x, v2.x), Math.min(v1.y, v2.y), Math.min(v1.z, v2.z), Math.min(v1.w, v2.w));
		if(dest != null) dest.set(v);
		return v;
	}
	
	public static Vector2f max(Vector2f v1, Vector2f v2, Vector2f dest) {
		Vector2f v = new Vector2f(Math.max(v1.x, v2.x), Math.max(v1.y, v2.y));
		if(dest != null) dest.set(v);
		return v;
	}
	
	public static Vector3f max(Vector3f v1, Vector3f v2, Vector3f dest) {
		Vector3f v = new Vector3f(Math.max(v1.x, v2.x), Math.max(v1.y, v2.y), Math.max(v1.z, v2.z));
		if(dest != null) dest.set(v);
		return v;
	}
	
	public static Vector4f max(Vector4f v1, Vector4f v2, Vector4f dest) {
		Vector4f v = new Vector4f(Math.max(v1.x, v2.x), Math.max(v1.y, v2.y), Math.max(v1.z, v2.z), Math.max(v1.w, v2.w));
		if(dest != null) dest.set(v);
		return v;
	}
	
	public static Vector3f shortest(Vector3f... vectors) {
		Vector3f shortestVector = null;
		float shortestDistance = Float.MAX_VALUE;
		
		for(Vector3f vector : vectors) {
			float lengthSquared = vector.lengthSquared();
			if(lengthSquared < shortestDistance) {
				shortestVector = vector;
				shortestDistance = lengthSquared;
			}
		}
		
		return shortestVector;
	}
	
	public static Vector3f shortest(List<Vector3f> vectors) {
		Vector3f shortestVector = null;
		float shortestDistance = Float.MAX_VALUE;
		
		for(Vector3f vector : vectors) {
			float lengthSquared = vector.lengthSquared();
			if(lengthSquared < shortestDistance) {
				shortestVector = vector;
				shortestDistance = lengthSquared;
			}
		}
		
		return shortestVector;
	}
	
	public static Vector3f longest(Vector3f... vectors) {
		Vector3f longestVector = null;
		float longestDistance = Float.MIN_VALUE;
		
		for(Vector3f vector : vectors) {
			float lengthSquared = vector.lengthSquared();
			if(lengthSquared > longestDistance) {
				longestVector = vector;
				longestDistance = lengthSquared;
			}
		}
		
		return longestVector;
	}
	
	public static Vector3f longest(List<Vector3f> vectors) {
		Vector3f longestVector = null;
		float longestDistance = Float.MIN_VALUE;
		
		for(Vector3f vector : vectors) {
			float lengthSquared = vector.lengthSquared();
			if(lengthSquared > longestDistance) {
				longestVector = vector;
				longestDistance = lengthSquared;
			}
		}
		
		return longestVector;
	}
	
	public static Vector3f mean(Vector3f... vectors) {
		Vector3f mean = new Vector3f();
		for(Vector3f vector : vectors) {
			Vector3f.add(mean, vector, mean);
		}
		
		return VectorMath.div(mean, vectors.length, null);
	}
	
	public static Vector3f mean(List<Vector3f> vectors) {
		Vector3f mean = new Vector3f();
		for(Vector3f vector : vectors) {
			Vector3f.add(mean, vector, mean);
		}
		
		return VectorMath.div(mean, vectors.size(), null);
	}
	
	public static Vector2f midpoint(Vector2f v1, Vector2f v2, Vector2f dest) {
		Vector2f v = new Vector2f((v1.x + v2.x) * 0.5f, (v1.y + v2.y) * 0.5f);
		if(dest != null) dest.set(v);
		return v;
	}
	
	public static Vector3f midpoint(Vector3f v1, Vector3f v2, Vector3f dest) {
		Vector3f v = new Vector3f((v1.x + v2.x) * 0.5f, (v1.y + v2.y) * 0.5f, (v1.z + v2.z) * 0.5f);
		if(dest != null) dest.set(v);
		return v;
	}
	
	public static Vector4f midpoint(Vector4f v1, Vector4f v2, Vector4f dest) {
		Vector4f v = new Vector4f((v1.x + v2.x) * 0.5f, (v1.y + v2.y) * 0.5f, (v1.z + v2.z) * 0.5f, (v1.w + v2.w) * 0.5f);
		if(dest != null) dest.set(v);
		return v;
	}
	
	public static Vector2f leastSignificantAxis(Vector2f v) {
		return (v.x < v.y) ? new Vector2f(1f, 0f) : new Vector2f(0f, 1f);
	}
	
	public static Vector3f leastSignificantAxis(Vector3f v) {
		Vector3f vAbs = abs(v, null);
		
		if(vAbs.x < vAbs.y) {
			return (vAbs.x < vAbs.z) ? new Vector3f(1f, 0f, 0f) : new Vector3f(0f, 0f, 1f);
		} else if(vAbs.y < vAbs.z) {
			return new Vector3f(0f, 1f, 0f);
		} else {
			return new Vector3f(0f, 0f, 1f);
		}
	}
	
	public static Vector2f mostSignificantAxis(Vector2f v) {
		return (v.x > v.y) ? new Vector2f(1f, 0f) : new Vector2f(0f, 1f);
	}
	
	public static Vector3f mostSignificantAxis(Vector3f v) {
		Vector3f vAbs = abs(v, null);
		
		if(vAbs.x > vAbs.y) {
			return (vAbs.x > vAbs.z) ? new Vector3f(1f, 0f, 0f) : new Vector3f(0f, 0f, 1f);
		} else if(vAbs.y > vAbs.z) {
			return new Vector3f(0f, 1f, 0f);
		} else {
			return new Vector3f(0f, 0f, 1f);
		}
	}
	
	public static Vector2f mul(Vector2f v, float multiplicand, Vector2f dest) {
		Vector2f product = new Vector2f(v.x * multiplicand, v.y * multiplicand);
		if(dest != null) dest.set(product);
		return product;
	}
	
	public static Vector3f mul(Vector3f v, float multiplicand, Vector3f dest) {
		Vector3f product = new Vector3f(v.x * multiplicand, v.y * multiplicand, v.z * multiplicand);
		if(dest != null) dest.set(product);
		return product;
	}
	
	public static Vector4f mul(Vector4f v, float multiplicand, Vector4f dest) {
		Vector4f product = new Vector4f(v.x * multiplicand, v.y * multiplicand, v.z * multiplicand, v.w * multiplicand);
		if(dest != null) dest.set(product);
		return product;
	}
	
	public static Vector2f mul(Vector2f v1, Vector2f v2, Vector2f dest) {
		Vector2f product = new Vector2f(v1.x * v2.x, v1.y * v2.y);
		if(dest != null) dest.set(product);
		return product;
	}
	
	public static Vector3f mul(Vector3f v1, Vector3f v2, Vector3f dest) {
		Vector3f product = new Vector3f(v1.x * v2.x, v1.y * v2.y, v1.z * v2.z);
		if(dest != null) dest.set(product);
		return product;
	}
	
	public static Vector4f mul(Vector4f v1, Vector4f v2, Vector4f dest) {
		Vector4f product = new Vector4f(v1.x * v2.x, v1.y * v2.y, v1.z * v2.z, v1.w * v2.w);
		if(dest != null) dest.set(product);
		return product;
	}
	
	public static Vector2f div(Vector2f v1, float divisor, Vector2f dest) {
		Vector2f product = new Vector2f(v1.x / divisor, v1.y / divisor);
		if(dest != null) dest.set(product);
		return product;
	}
	
	public static Vector3f div(Vector3f v1, float divisor, Vector3f dest) {
		Vector3f product = new Vector3f(v1.x / divisor, v1.y / divisor, v1.z / divisor);
		if(dest != null) dest.set(product);
		return product;
	}
	
	public static Vector4f div(Vector4f v1, float divisor, Vector4f dest) {
		Vector4f product = new Vector4f(v1.x / divisor, v1.y / divisor, v1.z / divisor, v1.w / divisor);
		if(dest != null) dest.set(product);
		return product;
	}
	
	public static Vector2f div(Vector2f v1, Vector2f v2, Vector2f dest) {
		Vector2f product = new Vector2f(v1.x / v2.x, v1.y / v2.y);
		if(dest != null) dest.set(product);
		return product;
	}
	
	public static Vector3f div(Vector3f v1, Vector3f v2, Vector3f dest) {
		Vector3f product = new Vector3f(v1.x / v2.x, v1.y / v2.y, v1.z / v2.z);
		if(dest != null) dest.set(product);
		return product;
	}
	
	public static Vector4f div(Vector4f v1, Vector4f v2, Vector4f dest) {
		Vector4f product = new Vector4f(v1.x / v2.x, v1.y / v2.y, v1.z / v2.z, v1.w / v2.w);
		if(dest != null) dest.set(product);
		return product;
	}
	
	public static Vector2f mod(Vector2f v1, float divisor, Vector2f dest) {
		Vector2f remainder = new Vector2f(v1.x % divisor, v1.y % divisor);
		if(dest != null) dest.set(remainder);
		return remainder;
	}
	
	public static Vector3f mod(Vector3f v1, float divisor, Vector3f dest) {
		Vector3f remainder = new Vector3f(v1.x % divisor, v1.y % divisor, v1.z % divisor);
		if(dest != null) dest.set(remainder);
		return remainder;
	}
	
	public static Vector4f mod(Vector4f v1, float divisor, Vector4f dest) {
		Vector4f remainder = new Vector4f(v1.x % divisor, v1.y % divisor, v1.z % divisor, v1.w % divisor);
		if(dest != null) dest.set(remainder);
		return remainder;
	}
	
	public static Vector2f mod(Vector2f v1, Vector2f v2, Vector2f dest) {
		Vector2f remainder = new Vector2f(v1.x % v2.x, v1.y % v2.y);
		if(dest != null) dest.set(remainder);
		return remainder;
	}
	
	public static Vector3f mod(Vector3f v1, Vector3f v2, Vector3f dest) {
		Vector3f remainder = new Vector3f(v1.x % v2.x, v1.y % v2.y, v1.z % v2.z);
		if(dest != null) dest.set(remainder);
		return remainder;
	}
	
	public static Vector4f mod(Vector4f v1, Vector4f v2, Vector4f dest) {
		Vector4f remainder = new Vector4f(v1.x % v2.x, v1.y % v2.y, v1.z % v2.z, v1.w % v2.w);
		if(dest != null) dest.set(remainder);
		return remainder;
	}
	
	public static Vector3f mulMatrix(Matrix3f mat, Vector3f v, Vector3f dest) {
		Vector3f vector = new Vector3f(
				mat.m00 * v.x + mat.m01 * v.y + mat.m02 * v.z,
				mat.m10 * v.x + mat.m11 * v.y + mat.m12 * v.z,
				mat.m20 * v.x + mat.m21 * v.y + mat.m22 * v.z);
		if(dest != null) dest.set(vector);
		return vector;
	}
	
	public static Vector4f mulMatrix(Matrix4f mat, Vector4f v, Vector4f dest) {
		Vector4f vector = new Vector4f(
				mat.m00 * v.x + mat.m10 * v.y + mat.m20 * v.z + mat.m30 * v.w,
				mat.m01 * v.x + mat.m11 * v.y + mat.m21 * v.z + mat.m31 * v.w,
				mat.m02 * v.x + mat.m12 * v.y + mat.m22 * v.z + mat.m32 * v.w,
				mat.m03 * v.x + mat.m13 * v.y + mat.m23 * v.z + mat.m33 * v.w);
		if(dest != null) dest.set(vector);
		return vector;
	}
	
	public static Vector2f setLength(Vector2f src, float length, Vector2f dest) {
		float currentLength = src.length();
		
		if(currentLength <= 1E-9) return new Vector2f();
		
		float resize = length / currentLength;
		
		Vector2f toReturn = new Vector2f();
			toReturn.set(src.x * resize, src.y * resize);
		if(dest != null) dest.set(toReturn);
		return toReturn;
	}
	
	public static Vector3f setLength(Vector3f src, float length, Vector3f dest) {
		float currentLength = src.length();
		
//		System.out.println("\tCurrent Length = " + currentLength);
		if(currentLength <= 1E-9) return new Vector3f();
		
		float resize = length / currentLength;
		
		Vector3f toReturn = new Vector3f();
			toReturn.set(src.x * resize, src.y * resize, src.z * resize);
		if(dest != null) dest.set(toReturn);
		return toReturn;
	}
	
	public static Vector4f setLength(Vector4f src, float length, Vector3f dest) {
		float currentLength = src.length();
		
		if(currentLength <= 1E-9) return new Vector4f();
		
		float resize = length / currentLength;
		
		Vector4f toReturn = new Vector4f();
			toReturn.set(src.x * resize, src.y * resize, src.z * resize, src.w * resize);
		if(dest != null) dest.set(toReturn);
		return toReturn;
	}
	
	public static float getDistance(Vector2f v1, Vector2f v2) {
		return Vector2f.sub(v1, v2, null).length();
	}
	
	public static float getDistance(Vector3f v1, Vector3f v2) {
		return Vector3f.sub(v1, v2, null).length();
	}
	
	public static float getDistance(Vector4f v1, Vector4f v2) {
		return Vector4f.sub(v1, v2, null).length();
	}
	
	public static float getDistanceSquared(Vector2f v1, Vector2f v2) {
		return Vector2f.sub(v1, v2, null).lengthSquared();
	}
	
	public static float getDistanceSquared(Vector3f v1, Vector3f v2) {
		return Vector3f.sub(v1, v2, null).lengthSquared();
	}
	
	public static float getDistanceSquared(Vector4f v1, Vector4f v2) {
		return Vector4f.sub(v1, v2, null).lengthSquared();
	}
	
	public static Vector2f interpolate(Vector2f startVector, Vector2f finalVector, float interpolationFactor) {
		Vector2f interpolatedVector = Vector2f.add(startVector, mul(Vector2f.sub(finalVector, startVector, null), interpolationFactor, null), null);
		return interpolatedVector;
	}
	
	public static Vector3f interpolate(Vector3f startVector, Vector3f endVector, float interpolationFactor) {
		Vector3f interpolatedVector = Vector3f.add(startVector, mul(Vector3f.sub(endVector, startVector, null), interpolationFactor, null), null);
		return interpolatedVector;
	}
	
	public static Vector4f interpolate(Vector4f startVector, Vector4f finalVector, float interpolationFactor) {
		Vector4f interpolatedVector = Vector4f.add(startVector, mul(Vector4f.sub(finalVector, startVector, null), interpolationFactor, null), null);
		return interpolatedVector;
	}
	
	public static Vector2f interpolate(Vector2f startVector, Vector2f finalVector, Vector2f interpolationFactor) {
		Vector2f interpolatedVector = Vector2f.add(startVector, mul(Vector2f.sub(finalVector, startVector, null), interpolationFactor, null), null);
		return interpolatedVector;
	}
	
	public static Vector3f interpolate(Vector3f startVector, Vector3f endVector, Vector3f interpolationFactor) {
		Vector3f interpolatedVector = Vector3f.add(startVector, mul(Vector3f.sub(endVector, startVector, null), interpolationFactor, null), null);
		return interpolatedVector;
	}
	
	public static Vector4f interpolate(Vector4f startVector, Vector4f finalVector, Vector4f interpolationFactor) {
		Vector4f interpolatedVector = Vector4f.add(startVector, mul(Vector4f.sub(finalVector, startVector, null), interpolationFactor, null), null);
		return interpolatedVector;
	}
	
	public static Vector3f rotate(Vector3f src, Vector3f axis, float angle, Vector3f dest) {
		Vector3f parallel = setLength(axis, Vector3f.dot(src, axis) / axis.lengthSquared(), null);
		Vector3f orthogonal = Vector3f.sub(src, parallel, null);
		Vector3f w = Vector3f.cross(axis, orthogonal, null);
		
		float orthogonalLength = orthogonal.length();
		
		float x_1 = (float) Math.cos(angle) / orthogonalLength;
		float x_2 = (float) Math.sin(angle) / w.length();
		
		Vector3f unitRot = Vector3f.add(mul(orthogonal, x_1, null), mul(w, x_2, null), null);
		return mul(unitRot, orthogonalLength, dest);
	}
	
	/**
	 * Generates two additional vectors that form an orthogonal basis when joined with v.
	 * @param v Normalized Vector3f
	 * @return An array of two vectors that form an orthogonal basis together with v
	 */
	public static Vector3f[] generateBasis(Vector3f v, Vector3f dest1, Vector3f dest2) {
		Vector3f tangent1 = new Vector3f();
		Vector3f tangent2 = new Vector3f();
		
		if(v.x >= 0.57735f) tangent1.set(v.y, -v.x, 0f);
		else tangent1.set(0f, v.z, -v.y);
		tangent1.normalise(tangent1);
		
		tangent2.set(Vector3f.cross(v, tangent1, null));
		
		if(dest1 != null) dest1.set(tangent1);
		if(dest2 != null) dest2.set(tangent2);
		return new Vector3f[]{tangent1, tangent2};
	}
	
	public static Vector3f gramSchmidt(Vector3f u, Vector3f v) {
		Vector3f projection = VectorMath.mul(u, Vector3f.dot(v, u) / u.lengthSquared(), null);
		return Vector3f.sub(v, projection, null);
	}
	
	public static void main(String[] args) {
		Vector4f v = new Vector4f(0, 1, 2, 3);
		System.out.println(v.w);
		System.out.println(v.x);
		System.out.println(v.y);
		System.out.println(v.z);
	}
}
