package silverSol.engine.render.camera;

import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import silverSol.engine.entity.Entity;
import silverSol.engine.physics.d3.body.Body;

/**
 * The Camera is an Entity that tracks given positions in the world and maintains a view matrix
 * @author Julian
 *
 */
public class Camera extends Entity {
		
	private static final float PI = (float) Math.PI;
	private static final float TWO_PI = (float) Math.PI * 2f;
	
	//TRANSFORMATION
	protected float pitch;
	protected float yaw;
	protected float roll;
	protected Matrix4f viewMatrix;
	
	protected float screenWidth;
	protected float screenHeight;
	protected float fieldOfView;
	protected float nearPlane;
	protected float farPlane;
	protected Matrix4f projectionMatrix;
	
	protected float aspectRatio;
	
	public Camera() {
		super(null, new Body());
		this.body3d.setGravityImmune(true);
		
		this.pitch = 0;
		this.yaw = 0;
		this.roll = 0;
		
		this.fieldOfView = (float) Math.PI * 0.3889f;
		this.nearPlane = 0.1f;
		this.farPlane = 2000f;
		
		this.screenWidth = Display.getWidth();
		this.screenHeight = Display.getHeight();
		this.aspectRatio = screenWidth / screenHeight;
			
		this.viewMatrix = new Matrix4f();
		calculateViewMatrix();
		
		this.projectionMatrix = new Matrix4f();
		calculateProjectionMatrix();
	}
	
	public void translate(float x, float y, float z) {
		body3d.translate(x, y, z);
	}
	
	public void translate(Vector3f translation) {
		body3d.translate(translation);
	}
	
	public void setPosition(float x, float y, float z) {
		body3d.setPosition(x, y, z);
	}
	
	public void setPosition(Vector3f position) {
		body3d.setPosition(position);
	}
	
	public void rotate(float x, float y, float z) {
		pitch += x;
		yaw += y;
		roll += z;
		body3d.setRotation(pitch, yaw, roll);
	}
	
	public void rotate(Vector3f rotation) {
		pitch += rotation.x;
		yaw += rotation.y;
		roll += rotation.z;
		body3d.setRotation(pitch, yaw, roll);
	}
	
	public void setRotation(float x, float y, float z) {
		pitch = x;
		yaw = y;
		roll = z;
		body3d.setRotation(x, y, z);
	}

	public void setRotation(Vector3f rotation) {
		pitch = rotation.x;
		yaw = rotation.y;
		roll = rotation.z;
		body3d.setRotation(rotation);
	}
	
	public void clampRotation() {
		while(pitch < -PI) pitch += TWO_PI;
		while(pitch >= PI) pitch -= TWO_PI;
		while(yaw < -PI) yaw += TWO_PI;
		while(yaw >= PI) yaw -= TWO_PI;
		while(roll < -PI) roll += TWO_PI;
		while(roll >= PI) roll -= TWO_PI;
	}
	
	public float getPitch() {
		return pitch;
	}

	public void setPitch(float pitch) {
		this.pitch = pitch;
		body3d.setRotation(pitch, yaw, roll);
	}

	public float getYaw() {
		return yaw;
	}

	public void setYaw(float yaw) {
		this.yaw = yaw;
		body3d.setRotation(pitch, yaw, roll);
	}

	public float getRoll() {
		return roll;
	}

	public void setRoll(float roll) {
		this.roll = roll;
		body3d.setRotation(pitch, yaw, roll);
	}
	
	public float getScreenWidth() {
		return screenWidth;
	}

	public void setScreenWidth(float screenWidth) {
		this.screenWidth = screenWidth;
		this.aspectRatio = screenWidth / screenHeight;
	}

	public float getScreenHeight() {
		return screenHeight;
	}

	public void setScreenHeight(float screenHeight) {
		this.screenHeight = screenHeight;
		this.aspectRatio = screenWidth / screenHeight;
	}

	public float getFieldOfView() {
		return fieldOfView;
	}

	public void setFieldOfView(float fieldOfView) {
		this.fieldOfView = fieldOfView;
	}

	public float getNearPlane() {
		return nearPlane;
	}

	public void setNearPlane(float nearPlane) {
		this.nearPlane = nearPlane;
	}

	public float getFarPlane() {
		return farPlane;
	}

	public void setFarPlane(float farPlane) {
		this.farPlane = farPlane;
	}

	/**
	 * Forces the view matrix to be recalculated.
	 */
	public void calculateViewMatrix() {
		viewMatrix.setIdentity();
		Matrix4f.rotate(pitch, new Vector3f(1, 0, 0), viewMatrix, viewMatrix);
		Matrix4f.rotate(yaw, new Vector3f(0, 1, 0), viewMatrix, viewMatrix);
		Matrix4f.rotate(roll, new Vector3f(0, 0, 1), viewMatrix, viewMatrix);
		Vector3f negativeCameraPosition = body3d.getPosition().negate(null);
		Matrix4f.translate(negativeCameraPosition, viewMatrix, viewMatrix);
	}
	
	public Matrix4f getViewMatrix() {
		return viewMatrix;
	}
	
	/**
	 * Forces the projection matrix to be recalculated.
	 */
	public void calculateProjectionMatrix() {
		float yScale = (float) ((1f / Math.tan(fieldOfView * 0.5f)) * aspectRatio);
		float xScale = yScale / aspectRatio;
		float frustumLength = farPlane - nearPlane;
		
		projectionMatrix = new Matrix4f();
		projectionMatrix.m00 = xScale;
		projectionMatrix.m11 = yScale;
		projectionMatrix.m22 = -((farPlane + nearPlane) / frustumLength);
		projectionMatrix.m23 = -1;
		projectionMatrix.m32 = -((2 * nearPlane * farPlane) / frustumLength);
		projectionMatrix.m33 = 0;
	}
	
	public Matrix4f getProjectionMatrix() {
		return projectionMatrix;
	}
	
	/**
	 * Forces the view and projection matrices to be recalculated
	 */
	public void calculateMatrices() {
		calculateViewMatrix();
		calculateProjectionMatrix();
	}
	
	/**
	 * Get the normalized device coordinates of a position on the screen
	 * @param screenX The x-coordinate of the screen position
	 * @param screenY The y-coordinate of the screen position
	 * @return The normalized device coordinates corresponding to the screen position
	 */
	public Vector2f getNDC(float screenX, float screenY) {
		float x = 2f * screenX / screenWidth - 1f;
		float y = 2f * screenY / screenHeight - 1f;
		return new Vector2f(x, y);
	}
	
	/**
	 * Get the world-space direction of a point on the screen
	 * @return The world-space direction from the camera to that point on the screen
	 */
	public Vector3f getDirection(float screenX, float screenY) {
		Vector2f ndc = getNDC(screenX, screenY);
		Vector4f clipCoords = new Vector4f(ndc.x, ndc.y, -1f, 1f);
		Matrix4f invertedProjection = Matrix4f.invert(projectionMatrix, null);
		Vector4f eyeCoords = Matrix4f.transform(invertedProjection, clipCoords, null);
		eyeCoords.z = -1f;
		eyeCoords.w = 0f;
		Matrix4f invertedView = Matrix4f.invert(viewMatrix, null);
		Vector4f rayWorld = Matrix4f.transform(invertedView, eyeCoords, null);
		Vector3f forward = new Vector3f(rayWorld.x, rayWorld.y, rayWorld.z);
		forward.normalise();
		return forward;
	}
	
	/**
	 * Get the world-space direction to the camera's right
	 * @return The world-space direction to the camera's right
	 */
	public Vector3f getRight() {
		Vector4f eyeCoords = new Vector4f(1f, 0f, 0f, 0f);
		Matrix4f invertedView = Matrix4f.invert(viewMatrix, null);
		Vector4f rayWorld = Matrix4f.transform(invertedView, eyeCoords, null);
		Vector3f right = new Vector3f(rayWorld.x, rayWorld.y, rayWorld.z);
		right.normalise();
		return right;
	}
	
	/**
	 * Get the world-space direction in which the camera is facing
	 * @return The world-space direction in which the camera is facing
	 */
	public Vector3f getForward() {
		Vector4f clipCoords = new Vector4f(0f, 0f, -1f, 1f);
		Matrix4f invertedProjection = Matrix4f.invert(projectionMatrix, null);
		Vector4f eyeCoords = Matrix4f.transform(invertedProjection, clipCoords, null);
		eyeCoords.z = -1f;
		eyeCoords.w = 0f;
		Matrix4f invertedView = Matrix4f.invert(viewMatrix, null);
		Vector4f rayWorld = Matrix4f.transform(invertedView, eyeCoords, null);
		Vector3f forward = new Vector3f(rayWorld.x, rayWorld.y, rayWorld.z);
		forward.normalise();
		return forward;
	}
	
	/**
	 * Get the world-space direction to the camera's top
	 * @return The world-space direction to the camera's top
	 */
	public Vector3f getUp() {
		Vector4f eyeCoords = new Vector4f(0f, 1f, 0f, 0f);
		Matrix4f invertedView = Matrix4f.invert(viewMatrix, null);
		Vector4f rayWorld = Matrix4f.transform(invertedView, eyeCoords, null);
		Vector3f up = new Vector3f(rayWorld.x, rayWorld.y, rayWorld.z);
		up.normalise();
		return up;
	}
	
}
