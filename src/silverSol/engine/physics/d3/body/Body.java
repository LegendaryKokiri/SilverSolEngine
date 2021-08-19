package silverSol.engine.physics.d3.body;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Matrix3f;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import silverSol.engine.physics.d3.collider.Collider;
import silverSol.engine.physics.d3.collider.ray.Ray;
import silverSol.engine.physics.d3.collider.volume.Sphere;
import silverSol.engine.physics.d3.collider.volume.Volume;
import silverSol.engine.physics.d3.collider.volume.Volume.Type;
import silverSol.engine.physics.d3.collision.Collision;
import silverSol.engine.physics.d3.collision.CollisionMod;
import silverSol.engine.physics.d3.constraint.Constraint;
import silverSol.engine.physics.d3.constraint.LinearSeparate;
import silverSol.engine.physics.d3.motion.Force;
import silverSol.math.MatrixMath;
import silverSol.math.QuaternionMath;
import silverSol.math.VectorMath;

/**
 * The Body class is the main structure upon which the physics engine operates.
 * @author Julian
 *
 */
public class Body {
	
	//Physics Engine Data
	private int physicsEngineIndex;
	protected boolean shouldRemove;
	
	//Transformation Data
	private Vector3f position;
	private Quaternion rotation;
	private Vector3f scale;
	private Matrix4f transformation;
	
	//Colliders
	private List<Volume> volumes;
	private List<Ray> rays;
	
	//Collision Data
	private boolean immovable;
	
	//Gravity
	private boolean gravitySet;
	private Vector3f gravity;
	private boolean gravityImmune;
	
	//Motion
	private List<Force> forces;
	private Vector3f linearVelocity;
	private Vector3f linearAcceleration;
	private Vector3f angularVelocity;
	private Vector3f angularAcceleration;
	
	//Mass
	private float mass; //in kilograms
	private float inverseMass;
	
	//Moment of Inertia
	private Matrix3f inertiaTensor;
	
	//Collisions
	private List<CollisionMod> modifiers;
	
	public Body() {		
		this.physicsEngineIndex = -1;
		
		this.position = new Vector3f();
		this.rotation = new Quaternion();
		this.scale = new Vector3f(1f, 1f, 1f);
		this.transformation = new Matrix4f();
				
		this.volumes = new ArrayList<>();
		this.rays = new ArrayList<>();
		
		this.gravitySet = false;
		this.gravity = new Vector3f();
		this.gravityImmune = false;
		
		this.linearVelocity = new Vector3f();
		this.linearAcceleration = new Vector3f();
		this.angularVelocity = new Vector3f();
		this.angularAcceleration = new Vector3f();
		
		this.mass = this.inverseMass = 1f;
		
		this.inertiaTensor = new Matrix3f();
		this.inertiaTensor.m00 = Float.POSITIVE_INFINITY;
		this.inertiaTensor.m11 = Float.POSITIVE_INFINITY;
		this.inertiaTensor.m22 = Float.POSITIVE_INFINITY;
		
		this.modifiers = new ArrayList<>();
	}
	
	public void preResolution(float dt) {
		calculateLinearMotion(dt);
		calculateAngularMotion(dt);		
	}
	
	private void calculateLinearMotion(float dt) {
//		for(Force force : forces) Vector3f.add(linearAcceleration, VectorMath.div(force.getForce(), mass, null), linearAcceleration);
		Vector3f.add(linearVelocity, VectorMath.mul(linearAcceleration, dt, null), linearVelocity);
	}
	
	private void calculateAngularMotion(float dt) {
		Vector3f.add(angularVelocity, VectorMath.mul(angularAcceleration, dt, null), angularVelocity);
	}
	
	public void postResolution(float dt) {
		translate(VectorMath.mul(linearVelocity, dt, null));
		rotate(VectorMath.mul(angularVelocity, dt, null));
		
		linearAcceleration.set(0, 0, 0);
		angularAcceleration.set(0, 0, 0);
	}
	
	public int getPhysicsEngineIndex() {
		return physicsEngineIndex;
	}

	public void setPhysicsEngineIndex(int physicsEngineIndex) {
		this.physicsEngineIndex = physicsEngineIndex;
	}

	public boolean shouldRemove() {
		return shouldRemove;
	}

	public void setShouldRemove(boolean shouldRemove) {
		this.shouldRemove = shouldRemove;
	}
	
	public Matrix4f getTransformation() {
		return transformation;
	}
	
	public void updateTransformation() {
		transformation.load(MatrixMath.createTransformation(position, rotation, scale));
	}

	public void setTransformation(Vector3f position, Quaternion rotation, Vector3f scale) {
		this.position.set(MatrixMath.getTranslation(transformation));
		this.rotation.set(rotation);
		this.scale.set(scale);
		updateTransformation();
		
		for(Volume volume : volumes) {
			volume.updateTransformation();
		}
		
		for(Ray ray : rays) {
			ray.updateTransformation();
		}
	}
	
	public Vector3f getPosition() {
		return position;
	}
	
	//TODO: Update ray transformations when body transformations are updated
	public void setPosition(float x, float y, float z) {
		this.position.set(x, y, z);
		updateTransformation();
		
		for(Volume volume : volumes) {
			volume.updateTransformation();
		}
		
		for(Ray ray : rays) {
			ray.updateTransformation();
		}
	}

	public void setPosition(Vector3f position) {
		setPosition(position.x, position.y, position.z);
	}
	
	public void translate(float x, float y, float z) {
		position.translate(x, y, z);
		updateTransformation();
		
		for(Volume volume : volumes) {
			volume.updateTransformation();
		}
		
		for(Ray ray : rays) {
			ray.updateTransformation();
		}
	}
	
	public void translate(Vector3f translation) {
		translate(translation.x, translation.y, translation.z);
	}
	
	public Vector3f getLinearAcceleration() {
		return linearAcceleration;
	}
	
	public void addLinearAcceleration(float x, float y, float z) {
		linearAcceleration.translate(x, y, z);
	}
	
	public void addLinearAcceleration(Vector3f linearAcceleration) {
		this.linearAcceleration.translate(linearAcceleration.x, linearAcceleration.y, linearAcceleration.z);
	}
	
	public void setLinearAcceleration(float x, float y, float z) {
		this.linearAcceleration.set(x, y, z);
	}

	public void setLinearAcceleration(Vector3f linearAcceleration) {
		this.linearAcceleration.set(linearAcceleration);
	}

	public Vector3f getLinearVelocity() {
		return linearVelocity;
	}

	public void setLinearVelocity(float x, float y, float z) {
		this.linearVelocity.set(x, y, z);
	}
	
	public void setLinearVelocity(Vector3f linearVelocity) {
		this.linearVelocity.set(linearVelocity);
	}
	
	public Quaternion getRotation() {
		return rotation;
	}
	
	public void setRotation(float eulerX, float eulerY, float eulerZ) {
		setRotation(QuaternionMath.create(eulerX, eulerY, eulerZ));
	}
	
	public void setRotation(Vector3f eulerRotation) {
		setRotation(QuaternionMath.create(eulerRotation));
	}

	public void setRotation(Quaternion rotation) {
		this.rotation.set(rotation.x, rotation.y, rotation.z, rotation.w);

		for(Volume volume : volumes) {
			volume.updateTransformation();
		}
		
		for(Ray ray : rays) {
			ray.updateTransformation();
		}
	}
	
	public void rotate(float eulerX, float eulerY, float eulerZ) {
		rotate(QuaternionMath.create(eulerX, eulerY, eulerZ));
	}
	
	public void rotate(Vector3f rotation) {
		rotate(QuaternionMath.create(rotation));
	}
	
	public void rotate(Quaternion rotation) {
		Quaternion.mul(rotation, this.rotation, this.rotation);
		
		updateTransformation();
		
		for(Volume volume : volumes) {
			volume.updateTransformation();
		}
		
		for(Ray ray : rays) {
			ray.updateTransformation();
		}
	}
	
	public Vector3f getAngularVelocity() {
		return angularVelocity;
	}
	
	public void setAngularVelocity(float x, float y, float z) {
		this.angularVelocity.set(x, y, z);
	}

	public void setAngularVelocity(Vector3f angularVelocity) {
		this.angularVelocity.set(angularVelocity);
	}

	public Vector3f getAngularAcceleration() {
		return angularAcceleration;
	}
	
	public void setAngularAcceleration(float x, float y, float z) {
		this.angularAcceleration.set(x, y, z);
	}

	public void setAngularAcceleration(Vector3f angularAcceleration) {
		this.angularAcceleration.set(angularAcceleration);
	}

	public Vector3f getScale() {
		return scale;
	}
	
	//TODO: Make sure that this scaling function is actually accurate.
	//TODO: Also, update the collider transformations in the same way you update them in setPosition() and setRotation().
	public void setScale(float x, float y, float z) {
		this.scale.set(x, y, z);
		updateTransformation();
		
		for(Volume volume : volumes) {
			volume.updateTransformation();
		}
		
		for(Ray ray : rays) {
			ray.updateTransformation();
		}
	}
	
	public void setScale(Vector3f scale) {
		setScale(scale.x, scale.y, scale.z);
	}
	
	public void setTransformation(Matrix4f transformation) {
		this.transformation.load(transformation);
		this.position.set(MatrixMath.getTranslation(transformation));
		Quaternion.setFromMatrix(transformation, rotation);
		this.scale.set(new Vector3f(transformation.m00, transformation.m01, transformation.m02).length(),
				new Vector3f(transformation.m10, transformation.m11, transformation.m12).length(),
				new Vector3f(transformation.m20, transformation.m21, transformation.m22).length());
		
		for(Volume volume : volumes) {
			volume.updateTransformation();
		}
		
		for(Ray ray : rays) {
			ray.updateTransformation();
		}
	}
	
	public float getMass() {
		return mass;
	}

	public void setMass(float mass) {
		this.mass = mass;
		this.inverseMass = 1f / mass;
	}

	public float getInverseMass() {
		return inverseMass;
	}

	public Matrix3f getInertiaTensor() {
		return inertiaTensor;
	}

	public void setInertiaTensor(Matrix3f inertiaTensor) {
		this.inertiaTensor = inertiaTensor;
	}
	
	//TODO: Implement the parallel axis theorem
	private void adjustMomentOfInertia() {
		if(volumes.size() == 0) return;
		inertiaTensor.setZero();
		
		for(Volume volume : volumes) {
			if(volume instanceof Sphere) {
				Sphere sphere = (Sphere) volume;
				float radius = sphere.getRadius();
				float inertiaOnAxis = (2f / 5f) * (mass / volumes.size()) * radius * radius;
				inertiaTensor.m00 += inertiaOnAxis;
				inertiaTensor.m11 += inertiaOnAxis;
				inertiaTensor.m22 += inertiaOnAxis;
			}
		}
	}

	public boolean isImmovable() {
		return immovable;
	}

	public void setImmovable(boolean immovable) {
		this.immovable = immovable;
	}
	
	public boolean isGravitySet() {
		return gravitySet;
	}
	
	public Vector3f getGravity() {
		return gravity;
	}
	
	public void setGravity(Vector3f gravity) {
		this.gravitySet = true;
		this.gravity.set(gravity);
	}
	
	public boolean isGravityImmune() {
		return gravityImmune;
	}

	public void setGravityImmune(boolean gravityImmune) {
		this.gravityImmune = gravityImmune;
	}

	public List<Volume> getVolumes() {
		return volumes;
	}

	public void addVolume(Volume volume) {
		if(volume != null) {
			volume.setBody(this);
			volumes.add(volume);
			if(volume.getType() == Type.SOLID) {
				adjustMomentOfInertia();
			}
		}
	}
	
	public List<Ray> getRays() {
		return rays;
	}

	public void addRay(Ray ray) {
		if(ray != null) {
			ray.setBody(this);
			rays.add(ray);
		}
	}
	
	public boolean ownsCollider(Collider collider) {
		return collider.getBody() == this;
	}
	
	public void setMasks(int identityBitMask, int collisionBitMask) {
		for(Volume volume : volumes) {
			volume.setIdentityBitMask(identityBitMask);
			volume.setCollisionBitMask(collisionBitMask);
		}
		
		for(Ray ray : rays) {
			ray.setIdentityBitMask(identityBitMask);
			ray.setCollisionBitMask(collisionBitMask);
		}
	}
	
	public List<CollisionMod> getModifiers() {
		return modifiers;
	}
	
	public void addModifier(CollisionMod modifier) {
		this.modifiers.add(modifier);
	}
	
	public void setModifiers(List<CollisionMod> modifiers) {
		this.modifiers = modifiers;
	}
	
	public void modifyCollision(Collision collision) {
		for(CollisionMod modifier : modifiers) {
			modifier.modify(collision);
		}
	}
	
	public Constraint[] generateConstraints(Collision collision) {
		if(collision.getConstraintsGenerated()) return null;
		collision.setConstraintsGenerated(true);
		return new Constraint[]{new LinearSeparate(collision)};
	}
	
	//TODO: Restore persistent contacts
	/*
	public void clearInvalidCollisions() {
		for(int i = 0; i < collisions.size(); i++) {
			Collision c = collisions.get(i);
			if(c.getColliderA().getType() != Type.SOLID || c.getColliderB().getType() != Type.SOLID) {
				collisions.remove(i);
				i--;
				continue;
			}
			
			c.validate();
			if(!c.isPersistent()) {
				collisions.remove(i);
				i--;
				continue;
			}
		}
	}*/
	
	public void clearCollisions() {
		for(Volume volume : volumes) volume.clearCollisions();
		for(Ray ray : rays) ray.clearCollisions();
	}
	
	public Vector3f toLocalDirection(Vector3f globalDirection) {
		Matrix3f transformRotation = MatrixMath.extractRotation(transformation);
		return VectorMath.mulMatrix(transformRotation, globalDirection, null);
	}
	
	public Vector3f toGlobalDirection(Vector3f localDirection) {
		Matrix3f transformRotation = MatrixMath.extractRotation(transformation);
		return VectorMath.mulMatrix(Matrix3f.invert(transformRotation, null), localDirection, null);
	}
	
	public Vector3f toLocalPosition(Vector3f globalPosition) {
		Vector4f local4 = VectorMath.mulMatrix(Matrix4f.invert(transformation, null),
				new Vector4f(globalPosition.x, globalPosition.y, globalPosition.z, 1f), null);
		return new Vector3f(local4.x, local4.y, local4.z);
	}

	public Vector3f toGlobalPosition(Vector3f localPosition) {
		Vector4f local4 = new Vector4f(localPosition.x, localPosition.y, localPosition.z, 1f);
		Vector4f global4 = VectorMath.mulMatrix(transformation, local4, null);
		return new Vector3f(global4.x, global4.y, global4.z);
	}
	
	@Override
	public String toString() {
		return "Body at position " + position;
	}
	
	public static void main(String[] args) {
		Body body = new Body();
		body.setPosition(7f, 11f, 15f);
		body.updateTransformation();
		
		System.out.println(body.toGlobalPosition(new Vector3f(0f, 1f, 0f)));
		System.out.println(body.toLocalPosition(new Vector3f(6f, 9f, 12f)));
		System.out.println(body.toGlobalPosition(new Vector3f(0f, 1f, 0f)));
	}
}
