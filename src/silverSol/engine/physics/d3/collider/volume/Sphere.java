package silverSol.engine.physics.d3.collider.volume;

import org.lwjgl.util.vector.Vector3f;

import silverSol.engine.physics.d3.body.Body;
import silverSol.engine.physics.d3.collision.Collision;
import silverSol.engine.physics.d3.det.narrow.algs.EPA;
import silverSol.engine.physics.d3.det.narrow.algs.GJK;
import silverSol.engine.physics.d3.det.narrow.algs.SeparatingAxis;
import silverSol.math.VectorMath;

public class Sphere extends Volume {

	private float radius;
	
	public Sphere(float radius, Type collisionType, Object colliderData) {
		super(collisionType, colliderData);
		setRadius(radius);
	}
	
	@Override
	public void calculateEndpoints() {
		float[] values = new float[]{position.x - radius, position.x + radius, position.y - radius,
				position.y + radius, position.z - radius, position.z + radius};
		
		//The larger value corresponds to the maximum endpoint.
		for(int i = 0; i < endpoints.length; i++) {
			endpoints[i].value = values[i];	
		}	
	}
	
	@Override
	public Vector3f supportMap(Vector3f globalDirection, boolean global) {
		if(global) return Vector3f.add(position, VectorMath.mul(globalDirection, radius, null), null);
		return VectorMath.mul(toLocalDirection(globalDirection), radius, null);
	}
	
	@Override
	public Vector3f[] raycast(Vector3f globalOrigin, Vector3f globalDirection, float maxLength, boolean global) {
		Vector3f toOrigin = Vector3f.sub(globalOrigin, position, null);
		float b = Vector3f.dot(toOrigin, globalDirection);
		float c = toOrigin.lengthSquared() - (radius * radius);
		float h = b * b - c;
		
		if(h < 0f) return null;
		
		h = (float) Math.sqrt(h);
		float distance = -b - h;
		
		if(distance > maxLength) return null;
		
		Vector3f intersection = Vector3f.add(globalOrigin, VectorMath.mul(globalDirection, distance, null), null);
		Vector3f direction = Vector3f.sub(intersection, position, null).normalise(null);
		
		if(global) return new Vector3f[]{this.toGlobalPosition(intersection), this.toGlobalDirection(direction)};
		return new Vector3f[]{intersection, direction};
	}
	
	@Override
	public Collision[] testForCollisions(Volume volume) {
		if(volume instanceof Sphere) return new Collision[]{sphereCollision((Sphere) volume)};
		else if(volume instanceof Landscape) return volume.testForCollisions(this);
		else return new Collision[]{GJK.detect(this, volume)};
	}
	
	@Override
	public Collision[] testForResolutions(Volume volume) {
		if(volume instanceof Sphere) return new Collision[]{sphereResolution((Sphere) volume)};
		else if(volume instanceof Landscape) return volume.testForResolutions(this);
		else return new Collision[]{EPA.run(GJK.run(this, volume), this, volume)};
	}
	
	public Collision sphereCollision(Sphere sphere) {
		float displacementSq = Vector3f.sub(sphere.position, position, null).lengthSquared();
		if(displacementSq > radius * radius + sphere.radius * sphere.radius) return null;
		
		Collision collision = new Collision();
		collision.setColliderA(this);
		collision.setColliderB(sphere);
		return collision;
	}
	
	public Collision sphereResolution(Sphere sphere) {
		Vector3f displacement = Vector3f.sub(sphere.position, position, null);
		Vector3f displacementNorm = displacement.normalise(null);
		float displacementSq = displacement.lengthSquared();
		if(displacementSq > radius * radius + sphere.radius * sphere.radius) return null;
		float displacementLength = (float) Math.sqrt(displacementSq);
		
		Collision collision = new Collision();
		collision.setColliderA(this);
		collision.setColliderB(sphere);
		
		Vector3f localA = this.supportMap(displacementNorm, false);
		Vector3f localB = sphere.supportMap(displacementNorm.negate(null), false);
		collision.setContactA(localA, this.toGlobalPosition(localA));
		collision.setContactB(localB, sphere.toGlobalPosition(localB));
		collision.setSeparatingAxis(displacementNorm);
		collision.setPenetrationDepth(radius + sphere.radius - displacementLength);
		return collision;
	}
	
	@Override
	public SeparatingAxis[] getSeparatingAxes(Volume other) {
		return new SeparatingAxis[0];
	}
	
	public float getRadius() {
		return radius;
	}

	public void setRadius(float radius) {
		this.radius = radius;
	}
	
	public static void main(String[] args) {
		Sphere sphere = new Sphere(2f, Type.SOLID, null);
		Body body = new Body();
			body.setPosition(7f, 11f, 15f);
			body.updateTransformation();
			body.addVolume(sphere);
			
		System.out.println(sphere.raycast(new Vector3f(-5.001f, 11f, 15f), new Vector3f(1f, 0f, 0f), 10f, true));
	}
	
}
