package silverSol.engine.physics.d3.collider;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Matrix3f;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import silverSol.engine.physics.d3.body.Body;
import silverSol.engine.physics.d3.collider.ray.Ray;
import silverSol.engine.physics.d3.collider.volume.Volume;
import silverSol.engine.physics.d3.collision.Collision;
import silverSol.engine.physics.d3.collision.CollisionFilter;
import silverSol.engine.physics.d3.collision.CollisionMod;
import silverSol.engine.physics.d3.det.broad.Endpoint;
import silverSol.math.MatrixMath;
import silverSol.math.VectorMath;

public abstract class Collider {

	//Body
	protected boolean hasBody;
		protected Body body;
	
	//Identification
	private short identityBitMask;
	private short collisionBitMask;
	public static final int DEFAULT_ID = -1;
	protected int ID;
	
	//Transformation
	protected Matrix4f bodyOffset;
	
	//Size
	protected Endpoint[] endpoints;
	
	//Collision Data
	private List<Collision> collisions;
	private List<Collider> ignoredColliders;
	private List<CollisionMod> modifiers;
	private List<CollisionFilter> filters;
	private int modPriority;
	
	//User-Defined Collider Data
	private Object colliderData;
	
	public Collider() {
		this.identityBitMask = 1;
		this.collisionBitMask = 1;
		this.ID = DEFAULT_ID;
		
		this.bodyOffset = new Matrix4f();
		
		this.endpoints = new Endpoint[6];
		for(int i = 0; i < endpoints.length; i++) {
			this.endpoints[i] = new Endpoint();
			this.endpoints[i].value = 0;
			this.endpoints[i].minimum = (i % 2 == 0);			
		}
		
		this.collisions = new ArrayList<>();
		this.ignoredColliders = new ArrayList<>();
		this.modifiers = new ArrayList<>();
		this.filters = new ArrayList<>();
		this.modPriority = 0;
	}
	
	public Collider(Object colliderData) {
		this();
		this.colliderData = colliderData;
	}
	
	public abstract boolean oughtOmitData();
	public abstract boolean oughtResolve();
	
	public abstract Collision[] testForCollisions(Collider c);
	public abstract Collision[] testForResolutions(Collider c);
	
	public Body getBody() {
		return body;
	}
	
	public void setBody(Body body) {
		this.body = body;
		this.hasBody = this.body != null;
	}
	
	public int getID() {
		return ID;
	}

	public void setID(int id) {
		ID = id;
		for(Endpoint e : endpoints) {
			e.id = id;
		}
	}
	
	public short getIdentityBitMask() {
		return identityBitMask;
	}

	public void setIdentityBitMask(short identityBitMask) {
		this.identityBitMask = identityBitMask;
	}

	public short getCollisionBitMask() {
		return collisionBitMask;
	}

	public void setCollisionBitMask(short collisionBitMask) {
		this.collisionBitMask = collisionBitMask;
	}
	
	public boolean canCollideWith(Collider collidable) {
		for(Collider ignored : ignoredColliders) {
			if(collidable == ignored) return false;
		}
		
		return (collisionBitMask & collidable.identityBitMask) != 0;
	}
	
	public Matrix4f getBodyOffset() {
		return bodyOffset;
	}

	public void setBodyOffset(Matrix4f bodyOffset) {
		Matrix4f.load(bodyOffset, this.bodyOffset);
	}
	
	public Vector3f toLocalDirection(Vector3f globalDirection) {
		Matrix3f offsetRotation = MatrixMath.extractRotation(bodyOffset);
		Matrix3f transformRotation = MatrixMath.extractRotation(body.getTransformation());
		
		Vector3f offsetLocal = VectorMath.mulMatrix(transformRotation, globalDirection, null);
		return VectorMath.mulMatrix(offsetRotation, offsetLocal, null);
	}
	
	public Vector3f toGlobalDirection(Vector3f localDirection) {
		Matrix3f offsetRotation = MatrixMath.extractRotation(bodyOffset);
		Matrix3f transformRotation = MatrixMath.extractRotation(body.getTransformation());
		
		Vector3f offsetLocal = VectorMath.mulMatrix(Matrix3f.invert(offsetRotation, null), localDirection, null);
		return VectorMath.mulMatrix(Matrix3f.invert(transformRotation, null), offsetLocal, null);
	}
	
	public Vector3f toLocalPosition(Vector3f globalPosition) {
		Vector4f offsetLocal = VectorMath.mulMatrix(Matrix4f.invert(body.getTransformation(), null),
				new Vector4f(globalPosition.x, globalPosition.y, globalPosition.z, 1f), null);
		Vector4f local4 = VectorMath.mulMatrix(Matrix4f.invert(bodyOffset, null), offsetLocal, null);
		
		return new Vector3f(local4.x, local4.y, local4.z);
	}

	public Vector3f toGlobalPosition(Vector3f localPosition) {
		Vector4f local4 = VectorMath.mulMatrix(bodyOffset, new Vector4f(localPosition.x, localPosition.y, localPosition.z, 1f), null);
		Vector4f global4 = VectorMath.mulMatrix(body.getTransformation(), local4, null);
		
		return new Vector3f(global4.x, global4.y, global4.z);
	}
	
	public abstract void calculateEndpoints();
	
	public Endpoint[] getEndpoints() {
		return endpoints;
	}
	
	public void setEndpoints(Endpoint[] endpoints) {
		this.endpoints = endpoints.clone();
	}
	
	public List<Collision> getCollisions() {
		return collisions;
	}
	
	public void addCollision(Collision collision) {
		collisions.add(collision);
	}
	
	public void clearCollisions() {
		collisions.clear();
	}
	
	public void ignoreCollidersOf(Body body) {
		for(Volume volume : body.getVolumes()) {
			ignoreCollider(volume);
		}
		
		for(Ray ray : body.getRays()) {
			ignoreCollider(ray);
		}
	}
	
	public void ignoreCollider(Collider collider) {
		if(!ignoredColliders.contains(collider)) ignoredColliders.add(collider);
	}
	
	public void acknowledgeCollider(Collider collider) {
		if(ignoredColliders.contains(collider)) ignoredColliders.remove(collider);
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
	
	public void clearModifiers() {
		this.modifiers.clear();
	}
	
	public void modifyCollision(Collision collision) {
		for(CollisionMod modifier : modifiers) {
			modifier.modify(collision);
		}
	}
	
	public void addFilter(CollisionFilter filter) {
		this.filters.add(filter);
	}
	
	public void setFilters(List<CollisionFilter> filters) {
		this.filters = filters;
	}
	
	public void clearFilters() {
		this.filters.clear();
	}
	
	public boolean filterCollision(Collision collision) {
		for(CollisionFilter filter : filters) {
			if(!filter.filter(collision, this)) return false;
		}
		
		return true;
	}
	
	public int getModPriority() {
		return modPriority;
	}

	public void setModPriority(int modPriority) {
		this.modPriority = modPriority;
	}
	
	public Object getColliderData() {
		return colliderData;
	}

	public void setColliderData(Object colliderData) {
		this.colliderData = colliderData;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + " (ID = " + ID + ")";
	}
	
}
