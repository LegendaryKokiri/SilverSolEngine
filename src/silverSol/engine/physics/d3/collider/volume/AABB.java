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

public class AABB extends Volume {
	
	private static final Vector3f X = new Vector3f(1f, 0f, 0f);
	private static final Vector3f Y = new Vector3f(0f, 1f, 0f);
	private static final Vector3f Z = new Vector3f(0f, 0f, 1f);
	public static final float EPSILON = 1E-3f;
	
	//DIMENSIONS
	public float halfLengths[];
	
	public AABB(float halfX, float halfY, float halfZ, Type collisionType, Object colliderData) {
		super(collisionType, colliderData);
		halfLengths = new float[]{halfX, halfY, halfZ};
	}
	
	public AABB(Vector3f halfDimensions, Type collisionType, Object colliderData) {
		super(collisionType, colliderData);
		halfLengths = new float[]{halfDimensions.x, halfDimensions.y, halfDimensions.z};
	}
	
	public Collider clone() {
		return new AABB(halfLengths[0], halfLengths[1], halfLengths[2], type, colliderData);
	}
	
	@Override
	public void calculateEndpoints() {
		float[] values = new float[]{position.x - halfLengths[0], position.x + halfLengths[0], position.y - halfLengths[1],
				position.y + halfLengths[1], position.z - halfLengths[2], position.z + halfLengths[2]};
		
		//The larger value corresponds to the maximum endpoint.
		for(int i = 0; i < endpoints.length; i++) {
			endpoints[i].value = values[i];	
		}
	}
	
	@Override
	public Vector3f supportMap(Vector3f globalDirection, boolean global) {
		Vector3f support = new Vector3f(Math.signum(globalDirection.x) * halfLengths[0],
				Math.signum(globalDirection.y) * halfLengths[1],
				Math.signum(globalDirection.z) * halfLengths[2]);
		return global ? this.toGlobalPosition(support) : support;
	}
	
	@Override
	public Vector3f[] raycast(Vector3f globalOrigin, Vector3f globalDirection, float maxLength, boolean global) {
		//TODO: AABB Raycast
		return new Vector3f[]{new Vector3f(), new Vector3f()};
	}
	
	@Override
	public Collision[] testForCollisions(Volume volume) {
		return new Collision[]{GJK.detect(this, volume)};
	}
	
	@Override
	public Collision[] testForResolutions(Volume volume) {
		if(volume instanceof Landscape) return testLandscapeCollision((Landscape) volume);
		else return new Collision[]{EPA.run(GJK.run(this, volume), this, volume)};
	}
	
	public Collision[] testLandscapeCollision(Landscape landscape) {
		return landscape.testForResolutions(this);
	}
	
	@Override
	public SepPlane[] getSeparatingPlanes(Planar planar) {
		return new SepPlane[] {new SepPlane(X), new SepPlane(Y), new SepPlane(Z)};
	}
	
	@Override
	public SepEdge[] getSeparatingEdges(Planar planar) {
		Vector3f dP = toLocalPosition(planar.position);
		
		float posX = position.x + halfLengths[0];
		float negX = position.x - halfLengths[0];
		float posY = position.y + halfLengths[1];
		float negY = position.y - halfLengths[1];
		float posZ = position.z + halfLengths[2];
		float negZ = position.z - halfLengths[2];
		
		float closeX = dP.x > 0f ? posX : negX;
		float closeY = dP.y > 0f ? posY : negY;
		float closeZ = dP.z > 0f ? posZ : negZ;
		
		return new SepEdge[] {
				new SepEdge(X, new Vector3f(negX, closeY, closeZ), new Vector3f(posX, closeY, closeZ)),
				new SepEdge(Y, new Vector3f(closeX, negY, closeZ), new Vector3f(closeX, posY, closeZ)),
				new SepEdge(Z, new Vector3f(closeX, closeY, negZ), new Vector3f(closeX, closeY, posZ))};
	}
	
	public Vector3f closestPointTo(Vector3f globalPoint, boolean global) {
		Vector3f localPoint = toLocalPosition(globalPoint);
		
		float halfX = halfLengths[0];
		float halfY = halfLengths[1];
		float halfZ = halfLengths[2];
		Vector3f vLocal = new Vector3f(
				NumberMath.clamp(localPoint.x, -halfX, halfX),
				NumberMath.clamp(localPoint.y, -halfY, halfY),
				NumberMath.clamp(localPoint.z, -halfZ, halfZ));
		
		boolean xBetween = -halfX < vLocal.x && vLocal.x < halfX;
		boolean yBetween = -halfX < vLocal.x && vLocal.x < halfX;
		boolean zBetween = -halfX < vLocal.x && vLocal.x < halfX;
		
		//If the point is not on the extreme on any axis, then localPoint was inside the shape.
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
		AABB obb = new AABB(1f, 1f, 1f, Type.SOLID, null);
		Body body = new Body();
			body.setPosition(7f, 11f, 15f);
			body.updateTransformation();
			body.addVolume(obb);
			
		Vector3f[] results = obb.raycast(new Vector3f(0f, 11.5f, 15f), new Vector3f(1f, 0f, 0f), 10f, false);
		System.out.println("Intersection Point = " + results[0]);
		System.out.println("Surface Normal = " + results[1]);
	}
}
