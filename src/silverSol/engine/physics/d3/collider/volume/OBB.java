package silverSol.engine.physics.d3.collider.volume;

import org.lwjgl.util.vector.Vector3f;

import silverSol.engine.physics.d3.body.Body;
import silverSol.engine.physics.d3.collider.Collider;
import silverSol.engine.physics.d3.collision.Collision;
import silverSol.engine.physics.d3.det.narrow.algs.EPA;
import silverSol.engine.physics.d3.det.narrow.algs.GJK;
import silverSol.engine.physics.d3.det.narrow.algs.SepEdge;
import silverSol.engine.physics.d3.det.narrow.algs.SepPlane;
import silverSol.math.NumberMath;
import silverSol.math.VectorMath;

public class OBB extends Volume {
	
	public static final float EPSILON = 1E-3f;
	
	//Axes and halfspaces
	public Vector3f[] u;
	public float e[];
	
	public OBB(float halfX, float halfY, float halfZ, Type collisionType, Object colliderData) {
		super(collisionType, colliderData);
		
		//Local axes 0 = x; 1 = y; 2 = z
		u = new Vector3f[]{new Vector3f(1, 0, 0), new Vector3f(0, 1, 0), new Vector3f(0, 0, 1)};
		e = new float[]{halfX, halfY, halfZ};
	}
	
	public OBB(Vector3f radius, Type collisionType, Object colliderData) {
		this(radius.x, radius.y, radius.z, collisionType, colliderData);
	}
	
	public Collider clone() {
		return new OBB(e[0], e[1], e[2], type, colliderData);
	}
	
	@Override
	public void calculateEndpoints() {
		float[] values = new float[]{position.x - e[0], position.x + e[0], position.y - e[1],
				position.y + e[1], position.z - e[2], position.z + e[2]};
		
		//The larger value corresponds to the maximum endpoint.
		for(int i = 0; i < endpoints.length; i++) {
			endpoints[i].value = values[i];	
		}
	}
	
	@Override
	public Vector3f supportMap(Vector3f globalDirection, boolean global) {
		Vector3f direction = toLocalDirection(globalDirection);
		Vector3f support = new Vector3f(Math.signum(direction.x) * e[0],
				Math.signum(direction.y) * e[1],
				Math.signum(direction.z) * e[2]);
		return global ? this.toGlobalPosition(support) : support;
	}
	
	@Override
	public Vector3f[] raycast(Vector3f globalOrigin, Vector3f globalDirection, float maxLength, boolean global) {
		Vector3f origin = toLocalPosition(globalOrigin);
		Vector3f direction = toLocalDirection(globalDirection);
				
		if(Math.abs(direction.x) < EPSILON && (origin.x < -e[0] || origin.x > e[0])) return null;
		if(Math.abs(direction.y) < EPSILON && (origin.y < -e[1] || origin.y > e[1])) return null;
		if(Math.abs(direction.z) < EPSILON && (origin.z < -e[2] || origin.z > e[2])) return null;
		
		Vector3f toOrigin = unitsToOrigin(origin, direction);
		Vector3f toFaces = unitsToFaces(direction);
		
		Vector3f t1 = Vector3f.sub(toOrigin.negate(null), toFaces, null);
		Vector3f t2 = Vector3f.add(toOrigin.negate(null), toFaces, null);
		
		float tNear = 0f;
		Vector3f normal = new Vector3f();
		
		if(t1.x > t1.y) {
			if(t1.x > t1.z) {
				tNear = t1.x;
				normal.set(1f, 0f, 0f);
			} else {
				tNear = t1.z;
				normal.set(0f, 0f, 1f);
			}
		} else if(t1.y > t1.z) {
			tNear = t1.y;
			normal.set(0f, 1f, 0f);
		} else {
			tNear = t1.z;
			normal.set(0f, 0f, 1f);
		}
		
		float tFar = NumberMath.min(t2.x, t2.y, t2.z);
		
		if(tNear > tFar || tFar < 0) return null;
		
		if(tNear > maxLength) return null;
		
		Vector3f intersection = Vector3f.add(origin, VectorMath.mul(direction, tNear, null), null);
		if(Vector3f.dot(normal, direction) > 0) normal.negate(normal);
		
		if(global) return new Vector3f[]{toGlobalPosition(intersection), toGlobalDirection(normal)};
		return new Vector3f[]{intersection, normal};
	}
	
	private Vector3f unitsToOrigin(Vector3f origin, Vector3f direction) {
		float x = unitsToOrigin(origin.x, direction.x);
		float y = unitsToOrigin(origin.y, direction.y);
		float z = unitsToOrigin(origin.z, direction.z);
		return new Vector3f(x, y, z);
	}
	
	private float unitsToOrigin(float origin, float direction) {
		if(Math.abs(direction) < EPSILON) return (direction < 0) ? Float.NEGATIVE_INFINITY : Float.POSITIVE_INFINITY;
		return origin / direction;
	}
	
	private Vector3f unitsToFaces(Vector3f direction) {
		float x = unitsToFaces(direction.x, 0);
		float y = unitsToFaces(direction.y, 1);
		float z = unitsToFaces(direction.z, 2);
		return new Vector3f(x, y, z);
	}
	
	private float unitsToFaces(float direction, int axisIndex) {
		if(Math.abs(direction) < EPSILON) return Float.POSITIVE_INFINITY;
		return Math.abs(e[axisIndex] / direction);
	}
	
	@Override
	public Collision[] testForCollisions(Volume volume) {
		return new Collision[]{GJK.detect(this, volume)};
	}
	
	@Override
	public Collision[] testForResolutions(Volume volume) {
		if(volume instanceof Landscape) return ((Landscape) volume).testForResolutions(this);
		else return new Collision[]{EPA.run(GJK.run(this, volume), this, volume)};
	}
	
	@Override
	public SepPlane[] getSeparatingPlanes(Planar planar) {
		Vector3f x = toGlobalDirection(new Vector3f(1f, 0f, 0f));
		Vector3f y = toGlobalDirection(new Vector3f(0f, 1f, 0f));
		Vector3f z = toGlobalDirection(new Vector3f(0f, 0f, 1f));
		
		return new SepPlane[] {new SepPlane(x), new SepPlane(y), new SepPlane(z)};
	}
	
	@Override
	public SepEdge[] getSeparatingEdges(Planar planar) {
		Vector3f x = toGlobalDirection(new Vector3f(1f, 0f, 0f));
		Vector3f y = toGlobalDirection(new Vector3f(0f, 1f, 0f));
		Vector3f z = toGlobalDirection(new Vector3f(0f, 0f, 1f));
		
		return new SepEdge[] {new SepEdge(x), new SepEdge(y), new SepEdge(z)};
	}
	
	public Vector3f closestPointTo(Vector3f globalPoint, boolean global) {
		Vector3f localPoint = toLocalPosition(globalPoint);
		
		float halfX = e[0];
		float halfY = e[1];
		float halfZ = e[2];
		Vector3f vLocal = new Vector3f(
				NumberMath.clamp(localPoint.x, -halfX, halfX),
				NumberMath.clamp(localPoint.y, -halfY, halfY),
				NumberMath.clamp(localPoint.z, -halfZ, halfZ));
		
		boolean xBetween = -halfX < vLocal.x && vLocal.x < halfX;
		boolean yBetween = -halfX < vLocal.x && vLocal.x < halfX;
		boolean zBetween = -halfX < vLocal.x && vLocal.x < halfX;
		
		//If the point is not on the extremem on any axis, then localPoint was inside the shape.
		//We therefore must find the closest point on the surface.
		if(xBetween && yBetween && zBetween) {
			float xDist = xBetween ? halfX - Math.abs(vLocal.x) : Float.POSITIVE_INFINITY;
			float yDist = yBetween ? halfY - Math.abs(vLocal.y) : Float.POSITIVE_INFINITY;
			float zDist = zBetween ? halfZ - Math.abs(vLocal.z) : Float.POSITIVE_INFINITY;
			
			float min = NumberMath.min(xDist, yDist, zDist);
			
			if(min == xDist) vLocal.x = Math.signum(vLocal.x) * halfX;
			else if(min == yDist) vLocal.y = Math.signum(vLocal.y) * halfY;
			else vLocal.z = Math.signum(vLocal.z) * halfZ;
		}
		
		return global ? toGlobalPosition(vLocal) : vLocal;
	}
	
	public static void main(String[] args) {
		OBB obb = new OBB(1f, 1f, 1f, Type.SOLID, null);
		Body body = new Body();
			body.setPosition(7f, 11f, 15f);
			body.updateTransformation();
			body.addVolume(obb);
			
		Vector3f[] results = obb.raycast(new Vector3f(0f, 11.5f, 15f), new Vector3f(1f, 0f, 0f), 10f, false);
		System.out.println("Intersection Point = " + results[0]);
		System.out.println("Surface Normal = " + results[1]);
		
		System.out.println("PROXIMITY TESTS");
		Vector3f[] proximityTests = new Vector3f[]{new Vector3f(5f, 10.5f, 14.9f), new Vector3f(6f, 10.5f, 14.9f)};
		
		for(int i = 0; i < proximityTests.length; i += 2) {
			Vector3f closest = obb.closestPointTo(proximityTests[i], true);
			if(!VectorMath.getEqual(closest, proximityTests[i+1])) System.out.println("FAILED proximity test " + (i/2+1) + "! Closest = " + closest + " instead of " + proximityTests[i+1]);
			else System.out.println("Passed proximity test " + (i/2+1));
		}
	}
}
