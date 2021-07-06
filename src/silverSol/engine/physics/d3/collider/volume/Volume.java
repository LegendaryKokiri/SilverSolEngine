package silverSol.engine.physics.d3.collider.volume;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Vector3f;

import silverSol.engine.physics.d3.body.Body;
import silverSol.engine.physics.d3.collider.Collider;
import silverSol.engine.physics.d3.collider.ray.Ray;
import silverSol.engine.physics.d3.collision.Collision;
import silverSol.engine.physics.d3.det.narrow.algs.SeparatingAxis;
import silverSol.math.MatrixMath;
import silverSol.parsers.model.ModelParser;

/**
 * Colliders are the volumes that collide with one another in the physics engine.
 * References to Colliders are stored in Body objects.
 * @author Julian
 *
 */
public abstract class Volume extends Collider {
	
	private Type type;
	public enum Type {
		ETHEREAL, SENSOR, SENSOR_PLUS, SOLID
	}
	
	//TODO: For applications such as animation with scale, we need to be able to track scale here, too.
	//Transformation
	protected Vector3f position;
	protected Quaternion rotation;
	protected Matrix4f transformation;
		
	//Resolution Data
	protected float restitution;
	protected boolean immovable;
	
	public Volume(Type collisionType) {
		super();
		init(collisionType);
	}
	
	public Volume(Type collisionType, Object colliderData) {
		super(colliderData);
		init(collisionType);
	}
	
	private void init(Type collisionType) {
		this.position = new Vector3f();
		this.rotation = new Quaternion();
		this.transformation = new Matrix4f();
		
		this.type = collisionType;
		this.immovable = true;
		this.restitution = 1f;
	}
	
	@Override
	public void setBody(Body body) {
		super.setBody(body);
		
		if(hasBody) {
			updateTransformation();
			this.immovable = body.isImmovable();
		}
	}
	
	@Override
	public boolean canCollideWith(Collider collidable) {
		return super.canCollideWith(collidable) && type != Type.ETHEREAL;
	}
	
	@Override
	public boolean oughtOmitData() {
		return type == Type.SENSOR;
	}
	
	@Override
	public boolean oughtResolve() {
		return type == Type.SOLID;
	}

	public Type getType() {
		return type;
	}
	
	public void setType(Type type) {
		this.type = type;
	}

	public float getRestitution() {
		return restitution;
	}

	public void setRestitution(float restitution) {
		this.restitution = restitution;
	}
	
	public Vector3f getPosition() {
		return position;
	}

	public void setPosition(float x, float y, float z) {
		this.position.set(x, y, z);
	}
	
	public void setPosition(Vector3f position) {
		this.position.set(position);
	}
	
	public Quaternion getRotation() {
		return rotation;
	}

	public Matrix4f getTransformation() {
		return transformation;
	}

	public void setTransformation(Matrix4f transformation) {
		this.transformation.load(transformation);
		this.setPosition(MatrixMath.getTranslation(transformation));
		Quaternion.setFromMatrix(transformation, this.rotation);
	}
	
	public void setImmovable(boolean immovable) {
		this.immovable = immovable;
	}
	
	public boolean getImmovable() {
		return immovable;
	}
	
	@Override
	public Collision[] testForCollisions(Collider collider) {
		if(collider instanceof Volume) return testForCollisions((Volume) collider);
		
		if(collider instanceof Ray) {
			Ray ray = (Ray) collider;
			return ray.testForCollisions(this);
		}
			
		return null;
	}
	
	public Collision[] testForResolutions(Collider collider) {
		if(collider instanceof Volume) return testForResolutions((Volume) collider);
		
		if(collider instanceof Ray) {
			Ray ray = (Ray) collider;
			return ray.testForResolutions(this);
		}
		
		return null;
	}
	
	/**
	 * Given a direction, return any of the farthest points in that direction
	 * @param globalDirection The direction in which to search
	 * @param global True if the returned point should be in global space and false if it should be in local space
	 * @return Vector3f containing the farthest point in the passed direction
	 */
	public abstract Vector3f supportMap(Vector3f globalDirection, boolean global);
	
	/**
	 * Given a ray, find the point of intersection and the surface normal of the intersected surface if one exists
	 * @param globalOrigin The global-space origin of the ray
	 * @param globalDirection The global-space direction of the ray
	 * @param maxLength The maximum distance from the ray origin at which an intersection will be acknowledged
	 * @param global True if the returned vectors should be returned in global space and false if they should be returned in local space
	 * @return An array containing the point of intersection (index 0) and the surface normal at the intersection point (index 1)
	 */
	public abstract Vector3f[] raycast(Vector3f globalOrigin, Vector3f globalDirection, float maxLength, boolean global);
	
	public abstract Collision[] testForCollisions(Volume volume);
	public abstract Collision[] testForResolutions(Volume volume);
	
	public abstract SeparatingAxis[] getSeparatingAxes(Volume other);
	
	public void updateTransformation() {
		transformation.load(Matrix4f.mul(body.getTransformation(), bodyOffset, null));
		this.position.set(MatrixMath.getTranslation(transformation));
		Quaternion.setFromMatrix(transformation, this.rotation);
		calculateEndpoints();
	}
	
	public static void main(String[] args) {
		Capsule capsule = new Capsule(1f, 1f, Type.SOLID, null);
			capsule.setID(1);
		
		Hull hull = ModelParser.parseHull("/models/Test Cube.ssm", 1f, Type.SOLID, null);
			hull.setID(2);
		
		Body body1 = new Body();
			body1.setPosition(0f, 3.01f, 0f);
			body1.addVolume(capsule);
			body1.updateTransformation();
		Body body3 = new Body();
			body3.setPosition(0f, 0f, 0f);
			body3.addVolume(hull);
			body3.updateTransformation();
		
		System.out.println(capsule.getPosition() + " -- " + hull.getPosition());
	}
}
