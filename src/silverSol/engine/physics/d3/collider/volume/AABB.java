package silverSol.engine.physics.d3.collider.volume;

import org.lwjgl.util.vector.Vector3f;

import silverSol.engine.physics.d3.body.Body;
import silverSol.engine.physics.d3.collision.Collision;
import silverSol.engine.physics.d3.det.narrow.algs.EPA;
import silverSol.engine.physics.d3.det.narrow.algs.GJK;
import silverSol.engine.physics.d3.det.narrow.algs.SeparatingAxis;

public class AABB extends Volume {
	
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
		Vector3f origin = toLocalPosition(globalOrigin);
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
	public SeparatingAxis[] getSeparatingAxes(Volume other) {
		return new SeparatingAxis[0];
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
