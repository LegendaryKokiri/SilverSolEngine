package silverSol.engine.physics.d3.collider.ray;

import org.lwjgl.util.vector.Vector3f;

import silverSol.engine.physics.d3.collider.Collider;
import silverSol.engine.physics.d3.collider.volume.Volume;
import silverSol.engine.physics.d3.collision.Collision;
import silverSol.engine.physics.d3.det.broad.Endpoint;
import silverSol.math.VectorMath;

public class Ray extends Collider {
	
	private Vector3f origin;
	private Vector3f direction;
	private float length;
	
	private Vector3f globalOrigin;
	private Vector3f globalDirection;
	
	private Collision firstCollision;
	private float firstDistance;
		
	public Ray(Vector3f origin, Vector3f direction, float length) {
		super();
		this.origin = new Vector3f(origin);
		this.direction = new Vector3f(direction.normalise(null));
		this.length = length;
		
		this.globalOrigin = new Vector3f(this.origin);
		this.globalDirection = new Vector3f(this.direction);
	}
	
	public Collider clone() {
		return new Ray(origin, direction, length);
	}
	
	@Override
	public void calculateEndpoints() {
		setEndpointsAxis(globalOrigin.x, globalDirection.x * length, endpoints[0], endpoints[1]);
		setEndpointsAxis(globalOrigin.y, globalDirection.y * length, endpoints[2], endpoints[3]);
		setEndpointsAxis(globalOrigin.z, globalDirection.z * length, endpoints[4], endpoints[5]);
	}
	
	private void setEndpointsAxis(float origin, float length, Endpoint min, Endpoint max) {
		if(length < 0f) {
			min.value = origin + length;
			max.value = origin - length;
		} else {
			min.value = origin - length;
			max.value = origin + length;
		}
	}
	
	public Vector3f getPointAtDistance(float length) {
		return Vector3f.add(origin, VectorMath.mul(direction, length, null), null);
	}
	
	@Override
	public Endpoint[] getEndpoints() {
		return endpoints;
	}

	@Override
	public void setEndpoints(Endpoint[] endpoints) {
		this.endpoints = endpoints.clone();
	}

	public Vector3f getOrigin() {
		return origin;
	}

	public Vector3f getGlobalOrigin() {
		return globalOrigin;
	}
	
	public void setOrigin(Vector3f origin) {
		this.origin.set(origin);
		this.globalOrigin.set(hasBody ? this.toGlobalPosition(this.origin) : origin);
	}

	public Vector3f getDirection() {
		return direction;
	}
	
	public Vector3f getGlobalDirection() {
		return globalDirection;
	}

	public void setDirection(Vector3f direction) {
		this.direction.set(direction.normalise(null));
		this.globalDirection.set(hasBody ? this.toGlobalDirection(this.direction) : direction);
	}
	
	//TODO: Figure out how you want to attach rays to bodies in terms of global and local fields.
	//As of right now, you've separated the local and global fields, as can clearly be seen here.
	public void updateTransformation() {
		globalOrigin.set(hasBody ? this.toGlobalPosition(origin) : origin);
		globalDirection.set(hasBody ? this.toGlobalDirection(direction) : direction);
	}

	public float getLength() {
		return length;
	}

	public void setLength(float length) {
		this.length = length;
	}

	@Override
	public boolean oughtOmitData() {
		return false;
	}

	@Override
	public boolean oughtResolve() {
		return false;
	}

	@Override
	public Collision[] testForCollisions(Collider c) {
		if(c instanceof Volume) return testForCollisions((Volume) c);
		else if(c instanceof Ray) return null; //TODO: Allow for rays to intersect.
		return null;
	}
	
	private Collision[] testForCollisions(Volume v) {
		Vector3f[] intersection = v.raycast(globalOrigin, globalDirection, length, true);
		
		if(intersection == null) return null;
		
		Collision collision = new Collision();
		collision.setColliderA(v);
		collision.setColliderB(this);
		return new Collision[]{collision};
	}
	
	@Override
	public Collision[] testForResolutions(Collider c) {
		if(c instanceof Volume) return testForResolutions((Volume) c);
		return null;
	}

	public Collision[] testForResolutions(Volume v) {
		Vector3f[] intersection = v.raycast(globalOrigin, globalDirection, length, true);
		
		if(intersection == null) return null;
		
		//The ray is made into collider B in order to have the separating axis represent the surface normal
		Collision collision = new Collision();
		collision.setColliderA(v);
		collision.setColliderB(this);
		
		/*In order to permit rays to be added to the collision routine apart from a body,
		we calculate the local intersection parametrically rather than with this.toLocalPosition().*/
		float t = Vector3f.sub(intersection[0], globalOrigin, null).length() / length;
		collision.setContactA(v.toLocalPosition(intersection[0]), intersection[0]);
		collision.setContactB(VectorMath.mul(direction, t, null), intersection[0]);
		
		collision.setSeparatingAxis(intersection[1]);
		
		collision.setPenetrationDepth(0f);
		
		return new Collision[]{collision};
	}
	
	public Collision getFirstCollision() {
		return firstCollision;
	}
	
	@Override
	public void addCollision(Collision collision) {
		super.addCollision(collision);
		
		float distance = Vector3f.dot(globalDirection, Vector3f.sub(collision.getGlobalContact(this), globalOrigin, null));
		if(firstCollision == null || distance < firstDistance) {
			firstDistance = distance;
			firstCollision = collision;
		}
	}
	
	@Override
	public void clearCollisions() {
		super.clearCollisions();
		firstCollision = null;
	}
	
}
