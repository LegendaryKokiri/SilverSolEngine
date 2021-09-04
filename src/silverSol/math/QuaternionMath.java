package silverSol.math;

import org.lwjgl.util.vector.Matrix3f;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

public class QuaternionMath {
	
	private static final float EPSILON = 1e-3f;
	private static final float HALF_PI = 0.5f * (float) Math.PI;
	
	public static Quaternion create(float eulerX, float eulerY, float eulerZ) {
		float cosX = (float) Math.cos(eulerX * 0.5f);
		float sinX = (float) Math.sin(eulerX * 0.5f);
		float cosY = (float) Math.cos(eulerY * 0.5f);
		float sinY = (float) Math.sin(eulerY * 0.5f);
		float cosZ = (float) Math.cos(eulerZ * 0.5f);
		float sinZ = (float) Math.sin(eulerZ * 0.5f);
		
		float w = cosX * cosY * cosZ + sinX * sinY * sinZ;
		float x = sinX * cosY * cosZ - cosX * sinY * sinZ;
		float y = cosX * sinY * cosZ + sinX * cosY * sinZ;
		float z = cosX * cosY * sinZ - sinX * sinY * cosZ;
		
		return new Quaternion(x, y, z, w);
	}
	
	public static Quaternion create(Vector3f eulerRotation) {
		return create(eulerRotation.x, eulerRotation.y, eulerRotation.z);
	}
	
	public static Vector3f getEuler(Quaternion q) {
		float sinxcosy = 2f * (q.w * q.x + q.y * q.z);
		float cosxcosy = 1f - 2f * (q.x * q.x + q.y * q.y);
		float siny = 2f * (q.w * q.y - q.x * q.z);
		float sinzcosy = 2f * (q.w * q.z + q.x * q.y);
		float coszcosy = 1f - 2f * (q.y * q.y + q.z * q.z);
		
		float xEuler = (float) Math.atan2(sinxcosy, cosxcosy);
		float yEuler = (float) (Math.abs(siny) >= 1f ? HALF_PI * Math.signum(siny) : Math.asin(siny));
		float zEuler = (float) Math.atan2(sinzcosy, coszcosy);
		
		return new Vector3f(xEuler, yEuler, zEuler);
	}
	
	public static Matrix3f getMatrix(Quaternion q) {
		Matrix3f matrix = new Matrix3f();
		
		float xy = q.x * q.y;
		float xz = q.x * q.z;
		float yz = q.y * q.z;
		float wx = q.w * q.x;
		float wy = q.w * q.y;
		float wz = q.w * q.z;
		float xSquared = q.x * q.x;
		float ySquared = q.y * q.y;
		float zSquared = q.z * q.z;
		
		matrix.m00 = 1 - 2 * (ySquared + zSquared);
		matrix.m01 = 2 * (xy - wz);
		matrix.m02 = 2 * (xz + wy);
		matrix.m10 = 2 * (xy + wz);
		matrix.m11 = 1 - 2 * (xSquared + zSquared);
		matrix.m12 = 2 * (yz - wx);
		matrix.m20 = 2 * (xz - wy);
		matrix.m21 = 2 * (yz + wx);
		matrix.m22 = 1 - 2 * (xSquared + ySquared);
		
		return matrix;
	}
	
	public static Matrix4f getMatrix4f(Quaternion q) {
		Matrix4f matrix = new Matrix4f();
		
		float xy = q.x * q.y;
		float xz = q.x * q.z;
		float yz = q.y * q.z;
		float wx = q.w * q.x;
		float wy = q.w * q.y;
		float wz = q.w * q.z;
		float xSquared = q.x * q.x;
		float ySquared = q.y * q.y;
		float zSquared = q.z * q.z;
		
		matrix.m00 = 1 - 2 * (ySquared + zSquared);
		matrix.m01 = 2 * (xy - wz);
		matrix.m02 = 2 * (xz + wy);
		matrix.m10 = 2 * (xy + wz);
		matrix.m11 = 1 - 2 * (xSquared + zSquared);
		matrix.m12 = 2 * (yz - wx);
		matrix.m20 = 2 * (xz - wy);
		matrix.m21 = 2 * (yz + wx);
		matrix.m22 = 1 - 2 * (xSquared + ySquared);
		
		matrix.m03 = matrix.m13 = matrix.m23 = 0f;
		matrix.m30 = matrix.m31 = matrix.m32 = 0f;
		matrix.m33 = 1f;
		
		return matrix;
	}
	
	/**
	 * Computes the quaternion representing the rotation between two vectors
	 * @param u The starting vector
	 * @param v The ending vector
	 * @return The quaternion representing the rotation from u to v
	 */
	public static Quaternion rotationBetween(Vector3f u, Vector3f v) {
		Vector3f nu = u.normalise(null);
		Vector3f nv = v.normalise(null);
		Vector3f sum = Vector3f.add(nu, nv, null);
		
		if(sum.lengthSquared() < EPSILON) {
			Vector3f ortho = VectorMath.generateOrthogonal(nu, null);
			return new Quaternion(ortho.x, ortho.y, ortho.z, 0f);
		}
		
		Vector3f half = sum.normalise(null);
		Vector3f cross = Vector3f.cross(half, nu, null);
		return new Quaternion(cross.x, cross.y, cross.z, Vector3f.dot(nu, half));
	}
	
	/**
	 * Computes the Quaternion representing the opposite rotation of q
	 * @param q The original quaternion
	 * @param dest The quaternion in which to store the result, or null
	 * @return The inverted quaternion
	 */
	public static Quaternion invert(Quaternion q, Quaternion dest) {
		Quaternion inverted = new Quaternion(-q.x, -q.y, -q.z, q.w);
		if(dest != null) dest.set(inverted.x, inverted.y, inverted.z, inverted.w);
		return inverted;
	}
	
	public static Quaternion interpolate(Quaternion left, Quaternion right, float t, Quaternion dest) {
		float w = 1, x = 0, y = 0, z = 0;
		float dotProduct = left.w * right.w + left.x * right.x + left.y * right.y + left.z * right.z;
		float blendInverse = 1f - t;
		if(dotProduct < 0) {
			w = blendInverse * left.w + t * -right.w;
			x = blendInverse * left.x + t * -right.x;
			y = blendInverse * left.y + t * -right.y;
			z = blendInverse * left.z + t * -right.z;
		} else {
			w = blendInverse * left.w + t * right.w;
			x = blendInverse * left.x + t * right.x;
			y = blendInverse * left.y + t * right.y;
			z = blendInverse * left.z + t * right.z;
		}
		
		Quaternion quaternion = new Quaternion(x, y, z, w);
		
		if(dest != null) dest.set(x, y, z, w);
		
		return quaternion;
	}
	
	/**
	 * Performs a SLERP between the two quaternions
	 * Implementation from Wikipedia, which is in turn adapted from the Eigen C++ library
	 * @param left The first quaternion
	 * @param right The second quaternion
	 * @param t The interpolation parameter (between 0 and 1)
	 * @return
	 */
	public static Quaternion slerp(Quaternion left, Quaternion right, float t, Quaternion dest) {
		float d = Quaternion.dot(left, right);
		float absD = Math.abs(d);
		
		float scaleLeft = 0f;
		float scaleRight = 0f;
		
		if(absD >= 1f) {
			scaleLeft = 1f - t;
			scaleRight = t;
		} else {
			float theta = (float) Math.acos(absD);
			float sinTheta = (float) Math.sin(theta);
			
			scaleLeft = (float) Math.sin((1f-t) * theta) / sinTheta;
			scaleRight = (float) Math.sin((t * theta)) / sinTheta;
		}
		
		if(d < 0f) scaleLeft *= -1f;
		
		float x = left.x * scaleLeft + right.x * scaleRight;
		float y = left.y * scaleLeft + right.y * scaleRight;
		float z = left.z * scaleLeft + right.z * scaleRight;
		float w = left.w * scaleLeft + right.w * scaleRight;
		
		if(dest != null) dest.set(x, y, z, w);
			
		return new Quaternion(x, y, z, w);
	}
	
	public static void main(String[] args) {
		Vector3f rotation = new Vector3f(3.14f, 1.57f, 0f);
		System.out.println(create(rotation)); //0.0, 0.9999997, 0.0, 0.0007963
		System.out.println();
		System.out.println(getEuler(create(rotation)));
		System.out.println();
		System.out.println(MatrixMath.createRotation(rotation));
		System.out.println();
		
//		Quaternion q = rotationBetween(new Vector3f(1f, 0f, 0f), new Vector3f(0.707f, 0.707f, 0f));
		Quaternion q = rotationBetween(new Vector3f(1f, 0f, 0f), new Vector3f(-0.707f, 0.707f, 0f));
		
		Matrix4f m = MatrixMath.createTransformation(new Vector3f(), q);
		Vector4f v = VectorMath.mulMatrix(m, new Vector4f(1f, 0f, 0f, 0f), null);
		System.out.println(v);
		Matrix4f.transform(m, new Vector4f(1f, 0f, 0f, 0f), v);
		System.out.println(v);
	}
	
}
