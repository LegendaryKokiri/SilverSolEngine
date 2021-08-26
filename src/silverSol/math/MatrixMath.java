package silverSol.math;

import org.lwjgl.util.vector.Matrix2f;
import org.lwjgl.util.vector.Matrix3f;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Vector3f;

public class MatrixMath {
	
	/**
	 * Returns a copy of the source matrix.
	 * @param src The source matrix
	 * @return A new instance of Matrix2f whose elements are identical to those in the source matrix
	 */
	public static Matrix2f clone(Matrix2f src) {
		Matrix2f matrix = new Matrix2f();
		Matrix2f.setZero(matrix);
		Matrix2f.add(src, matrix, matrix);
		return matrix;
	}
	
	/**
	 * Returns a copy of the source matrix.
	 * @param src The source matrix
	 * @return A new instance of Matrix2f whose elements are identical to those in the source matrix
	 */
	public static Matrix3f clone(Matrix3f src) {
		Matrix3f matrix = new Matrix3f();
		Matrix3f.setZero(matrix);
		Matrix3f.add(src, matrix, matrix);
		return matrix;
	}
	
	/**
	 * Returns a copy of the source matrix.
	 * @param src The source matrix
	 * @return A new instance of Matrix2f whose elements are identical to those in the source matrix
	 */
	public static Matrix4f clone(Matrix4f src) {
		Matrix4f matrix = new Matrix4f();
		Matrix4f.setZero(matrix);
		Matrix4f.add(src, matrix, matrix);
		return matrix;
	}
	
	public static Matrix2f mul(Matrix2f src, float multiplicand, Matrix2f dest) {
		Matrix2f m = new Matrix2f();
		m.load(src);
		
		m.m00 *= multiplicand;
		m.m01 *= multiplicand;
		m.m10 *= multiplicand;
		m.m11 *= multiplicand;
		
		if(dest != null) dest.load(m);
		return m;
	}
	
	public static Matrix3f mul(Matrix3f src, float multiplicand, Matrix3f dest) {
		Matrix3f m = new Matrix3f();
		m.load(src);
		
		m.m00 *= multiplicand;
		m.m01 *= multiplicand;
		m.m02 *= multiplicand;
		m.m10 *= multiplicand;
		m.m11 *= multiplicand;
		m.m12 *= multiplicand;
		m.m20 *= multiplicand;
		m.m21 *= multiplicand;
		m.m22 *= multiplicand;
		
		if(dest != null) dest.load(m);
		return m;
	}
	
	public static Matrix4f mul(Matrix4f src, float multiplicand, Matrix4f dest) {
		Matrix4f m = new Matrix4f();
		m.load(src);
		
		m.m00 *= multiplicand;
		m.m01 *= multiplicand;
		m.m02 *= multiplicand;
		m.m03 *= multiplicand;
		m.m10 *= multiplicand;
		m.m11 *= multiplicand;
		m.m12 *= multiplicand;
		m.m13 *= multiplicand;
		m.m20 *= multiplicand;
		m.m21 *= multiplicand;
		m.m22 *= multiplicand;
		m.m23 *= multiplicand;
		m.m30 *= multiplicand;
		m.m31 *= multiplicand;
		m.m32 *= multiplicand;
		m.m33 *= multiplicand;
		
		if(dest != null) dest.load(m);
		return m;
	}
	
	/**
	 * Creates a 2-by-2 matrix whose elements match those in the given array.
	 * @param data The desired elements of the matrix
	 * @return The 2-by-2 matrix whose elements match those in the given array
	 */
	public static Matrix2f createMatrix2f(float[] data) {
		Matrix2f matrix = new Matrix2f();
		
		if(data.length < 4) return matrix;
		
		matrix.m00 = data[0]; matrix.m01 = data[1];
		matrix.m10 = data[2]; matrix.m11 = data[3];
		
		return matrix;
	}
	
	/**
	 * Creates a 3-by-3 matrix whose elements match those in the given array.
	 * @param data The desired elements of the matrix
	 * @return The 3-by-3 matrix whose elements match those in the given array
	 */
	public static Matrix3f createMatrix3f(float[] data) {
		Matrix3f matrix = new Matrix3f();
		
		if(data.length < 9) return matrix;
		
		matrix.m00 = data[0]; matrix.m01 = data[1]; matrix.m02 = data[2];
		matrix.m10 = data[3]; matrix.m11 = data[4]; matrix.m12 = data[5];
		matrix.m20 = data[6]; matrix.m21 = data[7]; matrix.m22 = data[8];
		
		return matrix;
	}
	
	/**
	 * Creates a 4-by-4 matrix whose elements match those in the given array.
	 * @param data The desired elements of the matrix
	 * @return The 4-by-4 matrix whose elements match those in the given array
	 */
	public static Matrix4f createMatrix4f(float[] data) {
		Matrix4f matrix = new Matrix4f();
		
		if(data.length < 16) return matrix;
		
		matrix.m00 = data[0]; matrix.m01 = data[1]; matrix.m02 = data[2]; matrix.m03 = data[3];
		matrix.m10 = data[4]; matrix.m11 = data[5]; matrix.m12 = data[6]; matrix.m13 = data[7];
		matrix.m20 = data[8]; matrix.m21 = data[9]; matrix.m22 = data[10]; matrix.m23 = data[11];
		matrix.m30 = data[12]; matrix.m31 = data[13]; matrix.m32 = data[14]; matrix.m33 = data[15];
		
		return matrix;
	}
	
	/**
	 * Creates a 3-by-3 rotation matrix from the given Euler rotations
	 * @param rotation The magnitude of the Euler rotations along each axis (in radians)
	 * @return The 3-by-3 rotation matrix constructed from the given Euler rotations
	 */
	public static Matrix3f createRotation(Vector3f rotation) {		
		Matrix3f xMatrix = new Matrix3f();
		Matrix3f yMatrix = new Matrix3f();
		Matrix3f zMatrix = new Matrix3f();
		Matrix3f composite = new Matrix3f();
		
		double rotX = rotation.x;
		double rotY = rotation.y;
		double rotZ = rotation.z;
		
		xMatrix.m00 = 1; xMatrix.m01 = 0; xMatrix.m02 = 0;
		xMatrix.m10 = 0; xMatrix.m11 = (float) Math.cos(rotX); xMatrix.m12 = (float) Math.sin(rotX);
		xMatrix.m20 = 0; xMatrix.m21 = (float) -Math.sin(rotX); xMatrix.m22 = (float) Math.cos(rotX);

		yMatrix.m00 = (float) Math.cos(rotY); yMatrix.m01 = 0; yMatrix.m02 = (float) -Math.sin(rotY);
		yMatrix.m10 = 0; yMatrix.m11 = 1; yMatrix.m12 = 0;
		yMatrix.m20 = (float) Math.sin(rotY); yMatrix.m21 = 0; yMatrix.m22 = (float) Math.cos(rotY);
		
		zMatrix.m00 = (float) Math.cos(rotZ); zMatrix.m01 = (float) Math.sin(rotZ); zMatrix.m02 = 0;
		zMatrix.m10 = (float) -Math.sin(rotZ); zMatrix.m11 = (float) Math.cos(rotZ); zMatrix.m12 = 0;
		zMatrix.m20 = 0; zMatrix.m21 = 0; zMatrix.m22 = 1;
		
		Matrix3f.mul(xMatrix, yMatrix, composite);
		Matrix3f.mul(composite, zMatrix, composite);
		
		return composite;
	}
	
	/**
	 * Creates a 3-by-3 rotation matrix about the given axis by the given rotation
	 * @param rotation The magnitude of the Euler rotations along each axis (in radians)
	 * @return The 3-by-3 rotation matrix constructed from the given rotation
	 */
	public static Matrix3f createRotation(Vector3f axis, float angle) {
		Matrix4f matrixFour = new Matrix4f();
		Matrix4f.rotate(angle, axis, matrixFour, matrixFour);
		
		Matrix3f matrix = new Matrix3f();
		matrix.m00 = matrixFour.m00;
		matrix.m01 = matrixFour.m01;
		matrix.m02 = matrixFour.m02;
		matrix.m10 = matrixFour.m10;
		matrix.m11 = matrixFour.m11;
		matrix.m12 = matrixFour.m12;
		matrix.m20 = matrixFour.m20;
		matrix.m21 = matrixFour.m21;
		matrix.m22 = matrixFour.m22;
		
		return matrix;
	}
	
	public static Matrix3f extractRotation(Matrix4f transformation) {
		Matrix3f matrix = new Matrix3f();
		matrix.m00 = transformation.m00;
		matrix.m01 = transformation.m01;
		matrix.m02 = transformation.m02;
		matrix.m10 = transformation.m10;
		matrix.m11 = transformation.m11;
		matrix.m12 = transformation.m12;
		matrix.m20 = transformation.m20;
		matrix.m21 = transformation.m21;
		matrix.m22 = transformation.m22;
		
		return matrix;
	}
	
	public static Matrix4f createTransformation(Matrix3f rotation) {
		Matrix4f matrix = new Matrix4f();
		matrix.m00 = rotation.m00;
		matrix.m01 = rotation.m01;
		matrix.m02 = rotation.m02;
		matrix.m03 = 0;
		matrix.m10 = rotation.m10;
		matrix.m11 = rotation.m11;
		matrix.m12 = rotation.m12;
		matrix.m13 = 0;
		matrix.m20 = rotation.m20;
		matrix.m21 = rotation.m21;
		matrix.m22 = rotation.m22;
		matrix.m23 = 0;
		matrix.m30 = 0;
		matrix.m31 = 0;
		matrix.m32 = 0;
		matrix.m33 = 1;
		
		return matrix;
	}
		
	/**
	 * Creates a 4-by-4 transformation matrix from the given transformations
	 * @param translation The translation of the entity
	 * @param rotation The magnitude of the Euler rotations along each axis (in degrees)
	 * @return The 4-by-4 transformation matrix constructed from the given transformations
	 */
	public static Matrix4f createTransformation(Vector3f translation, Vector3f rotation) {
		Matrix4f matrix = new Matrix4f();
		
		Matrix4f.rotate(rotation.z, new Vector3f(0, 0, 1), matrix, matrix);
		Matrix4f.rotate(rotation.y, new Vector3f(0, 1, 0), matrix, matrix);
		Matrix4f.rotate(rotation.x, new Vector3f(1, 0, 0), matrix, matrix);
		Matrix4f.translate(translation, matrix, matrix);
		
		return matrix;
	}
	
	/**
	 * Creates a 4-by-4 transformation matrix from the given transformations
	 * @param translation The translation of the entity
	 * @param rotation The magnitude of the Euler rotations along each axis (in degrees)
	 * @param scale The scale of the entity
	 * @return The 4-by-4 transformation matrix constructed from the given transformations
	 */
	public static Matrix4f createTransformation(Vector3f translation, Vector3f rotation, Vector3f scale) {
		Matrix4f matrix = new Matrix4f();
		
		Matrix4f.translate(translation, matrix, matrix);
		Matrix4f.rotate(rotation.z, new Vector3f(0, 0, 1), matrix, matrix);
		Matrix4f.rotate(rotation.y, new Vector3f(0, 1, 0), matrix, matrix);
		Matrix4f.rotate(rotation.x, new Vector3f(1, 0, 0), matrix, matrix);
		Matrix4f.scale(scale, matrix, matrix);
		
		return matrix;
	}
	
	/**
	 * Creates a 4-by-4 transformation matrix from the given transformations
	 * @param translation The translation of the entity
	 * @param scale The scale of the entity
	 * @return The 4-by-4 transformation matrix constructed from the given transformations
	 */
	public static Matrix4f createTransformation(Vector3f translation, Quaternion rotation) {
		Matrix4f matrix = new Matrix4f();
		
		Matrix4f.translate(translation, matrix, matrix);
		Matrix4f.mul(QuaternionMath.getMatrix4f(rotation), matrix, matrix);
		
		return matrix;
	}
	
	/**
	 * Creates a 4-by-4 transformation matrix from the given transformations
	 * @param translation The translation of the entity
	 * @param rotation The rotation of the entity
	 * @param scale The scale of the entity
	 * @return The 4-by-4 transformation matrix constructed from the given transformations
	 */
	public static Matrix4f createTransformation(Vector3f translation, Quaternion rotation, Vector3f scale) {
		Matrix4f matrix = new Matrix4f();
		Vector3f euler = QuaternionMath.getEuler(rotation);
		
		Matrix4f.translate(translation, matrix, matrix);
		Matrix4f.rotate(euler.z, new Vector3f(0, 0, 1), matrix, matrix);
		Matrix4f.rotate(euler.y, new Vector3f(0, 1, 0), matrix, matrix);
		Matrix4f.rotate(euler.x, new Vector3f(1, 0, 0), matrix, matrix);
		Matrix4f.scale(scale, matrix, matrix);
		
		return matrix;
	}
	
	/**
	 * Creates a 4-by-4 transformation matrix from the given transformations
	 * @param translation The translation of the entity
	 * @param rotation The rotation matrix of the entity
	 * @return The 4-by-4 transformation matrix constructed from the given transformations
	 */
	public static Matrix4f createTransformation(Vector3f translation, Matrix4f rotation) {
		Matrix4f matrix = new Matrix4f();
		
		Matrix4f.translate(translation, matrix, matrix);
		Matrix4f.mul(rotation, matrix, matrix);
		
		return matrix;
	}
	
	/**
	 * Creates a 4-by-4 transformation matrix from the given transformations
	 * @param translation The translation of the entity
	 * @param rotation The rotation matrix of the entity
	 * @param scale The scale of the entity
	 * @return The 4-by-4 transformation matrix constructed from the given transformations
	 */
	public static Matrix4f createTransformation(Vector3f translation, Matrix4f rotation, Vector3f scale) {
		Matrix4f matrix = new Matrix4f();
		
		Matrix4f.translate(translation, matrix, matrix);
		Matrix4f.scale(scale, matrix, matrix);
		Matrix4f.mul(rotation, matrix, matrix);
		
		return matrix;
	}
	
	/**
	 * Creates a 4-by-4 transformation-view matrix from the given transformations, constrained to face the camera
	 * @param translation The translation of the entity
	 * @param rotation The magnitude of the entity's rotation (in degrees)
	 * @param scale The scale of the entity
	 * @return The 4-by-4 transformation matrix constructed from the given transformations
	 */
	public static Matrix4f createFrontFacingTransformationView(Vector3f position, float rotation, Vector3f scale, Matrix4f viewMatrix) {
		Matrix4f transformationMatrix = new Matrix4f();
		Matrix4f.translate(position, transformationMatrix, transformationMatrix);
		
		transformationMatrix.m00 = viewMatrix.m00;
		transformationMatrix.m01 = viewMatrix.m10;
		transformationMatrix.m02 = viewMatrix.m20;
		transformationMatrix.m10 = viewMatrix.m01;
		transformationMatrix.m11 = viewMatrix.m11;
		transformationMatrix.m12 = viewMatrix.m21;
		transformationMatrix.m20 = viewMatrix.m02;
		transformationMatrix.m21 = viewMatrix.m12;
		transformationMatrix.m22 = viewMatrix.m22;
		
		Matrix4f.rotate(rotation, new Vector3f(0, 0, 1), transformationMatrix, transformationMatrix);
		Matrix4f.scale(scale, transformationMatrix, transformationMatrix);
		Matrix4f transformationViewMatrix = Matrix4f.mul(viewMatrix, transformationMatrix, null);
		
		return transformationViewMatrix;
	}
	
	/**
	 * Returns the translation represented by the given 4-by-4 transformation matrix
	 * @param transformationMatrix The transformation matrix
	 * @return The translation represented by the passed transformation matrix
	 */
	public static Vector3f getTranslation(Matrix4f transformationMatrix) {
		return new Vector3f(transformationMatrix.m30, transformationMatrix.m31, transformationMatrix.m32);
	}
	
	/**
	 * Returns the unit vector parallel to the transformation matrix's local z-axis
	 * @param transformationMatrix The transformation matrix
	 * @return The unit vector parallel to the transformation matrix's local z-axis
	 */
	public static Vector3f getForward(Matrix4f transformationMatrix) {
		return new Vector3f(transformationMatrix.m20, transformationMatrix.m21, transformationMatrix.m22);
	}
	
	/**
	 * Returns the unit vector parallel to the negation of the transformation matrix's local z-axis
	 * @param transformationMatrix The transformation matrix
	 * @return The unit vector parallel to the negation of the transformation matrix's local z-axis
	 */
	public static Vector3f getBackward(Matrix4f transformationMatrix) {
		return new Vector3f(-transformationMatrix.m20, -transformationMatrix.m21, -transformationMatrix.m22);
	}
	
	/**
	 * Returns the unit vector parallel to the transformation matrix's local y-axis
	 * @param transformationMatrix The transformation matrix
	 * @return The unit vector parallel to the transformation matrix's local y-axis
	 */
	public static Vector3f getUp(Matrix4f transformationMatrix) {
		return new Vector3f(transformationMatrix.m10, transformationMatrix.m11, transformationMatrix.m12);
	}
	
	/**
	 * Returns the unit vector parallel to the negation of the transformation matrix's local y-axis
	 * @param transformationMatrix The transformation matrix
	 * @return The unit vector parallel to the negation of the transformation matrix's local y-axis
	 */
	public static Vector3f getDown(Matrix4f transformationMatrix) {
		return new Vector3f(-transformationMatrix.m10, -transformationMatrix.m11, -transformationMatrix.m12);
	}
	
	/**
	 * Returns the unit vector parallel to the negation of the transformation matrix's local x-axis
	 * @param transformationMatrix The transformation matrix
	 * @return The unit vector parallel to the negation of the transformation matrix's local x-axis
	 */
	public static Vector3f getRight(Matrix4f transformationMatrix) {
		return new Vector3f(-transformationMatrix.m00, -transformationMatrix.m01, -transformationMatrix.m02);
	}
	
	/**
	 * Returns the unit vector parallel to the transformation matrix's local x-axis
	 * @param transformationMatrix The transformation matrix
	 * @return The unit vector parallel to the transformation matrix's local x-axis
	 */
	public static Vector3f getLeft(Matrix4f transformationMatrix) {
		return new Vector3f(transformationMatrix.m00, transformationMatrix.m01, transformationMatrix.m02);
	}
	
	public static void main(String[] args) {
		Quaternion q = QuaternionMath.create(0f, 1.57f, 0f);
		System.out.println(createTransformation(new Vector3f(10f, 5f, 3f), q, new Vector3f(1f, 1f, 1f)));
		System.out.println();
		System.out.println(createTransformation(new Vector3f(10f, 5f, 3f), QuaternionMath.getEuler(q),
				new Vector3f(1f, 1f, 1f)));
		System.out.println(getLeft(createTransformation(new Vector3f(10f, 5f, 3f), q, new Vector3f(1f, 1f, 1f))));
	}
	
}
