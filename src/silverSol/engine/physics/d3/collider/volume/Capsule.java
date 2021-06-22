package silverSol.engine.physics.d3.collider.volume;

import org.lwjgl.util.vector.Vector3f;

import silverSol.engine.physics.d3.body.Body;
import silverSol.engine.physics.d3.collision.Collision;
import silverSol.engine.physics.d3.det.narrow.algs.EPA;
import silverSol.engine.physics.d3.det.narrow.algs.GJK;
import silverSol.engine.physics.d3.det.narrow.algs.SeparatingAxis;
import silverSol.math.VectorMath;

public class Capsule extends Volume {
	
	private static final Vector3f UP = new Vector3f(0f, 1f, 0f);
	
	private float halfCyl;
	private float radius;
	
	public Capsule(float halfCyl, float radius, Type collisionType, Object colliderData) {
		super(collisionType, colliderData);
		this.halfCyl = halfCyl;
		this.radius = radius;
	}

	@Override
	public void calculateEndpoints() {
		endpoints[0].value = position.x - halfCyl - radius;
		endpoints[1].value = position.x + halfCyl + radius;
		endpoints[2].value = position.y - halfCyl - radius;
		endpoints[3].value = position.y + halfCyl + radius;
		endpoints[4].value = position.z - halfCyl - radius;
		endpoints[5].value = position.z + halfCyl + radius;
	}

	@Override
	public Vector3f supportMap(Vector3f globalDirection, boolean global) {
		Vector3f direction = toLocalDirection(globalDirection);
		Vector3f halfSphere = new Vector3f(0f, halfCyl * Math.signum(direction.y), 0f);
		Vector3f support = Vector3f.add(halfSphere, VectorMath.mul(direction, radius, null), null);
		return global ? this.toGlobalPosition(support) : support;
	}
	
	@Override
	public Vector3f[] raycast(Vector3f globalOrigin, Vector3f globalDirection, float maxLength, boolean global) {
		Vector3f origin = toLocalPosition(globalOrigin);
		Vector3f direction = toLocalDirection(globalDirection);
		
		Vector3f sphereA = new Vector3f(0f, -halfCyl, 0f);
		Vector3f sphereB = new Vector3f(0f, halfCyl, 0f);
		Vector3f sphereDisplacement = new Vector3f(0f, halfCyl * 2f, 0f);
		Vector3f toOrigin = Vector3f.sub(origin, sphereA, null);
				
		float baba = sphereDisplacement.lengthSquared();
		float bard = Vector3f.dot(sphereDisplacement, direction);
		float baoa = Vector3f.dot(sphereDisplacement, toOrigin);
		float rdoa = Vector3f.dot(direction, toOrigin);
		float oaoa = toOrigin.lengthSquared();
		
		float a = baba - bard * bard;
		float b = baba * rdoa - baoa*bard;
		float c = baba * oaoa - baoa * baoa - radius * radius * baba;
		float h = b * b - a * c;
		
		if(h < 0f) return null;
		
		float t = (-b - (float) Math.sqrt(h)) / a;
		float y = baoa + t * bard;
		
		float distance = 0f;
		
		Vector3f intersection = new Vector3f();
		Vector3f normal = new Vector3f();
		
		if(y > 0f && y < baba) {
			distance = t;
			Vector3f.add(origin, VectorMath.mul(direction, distance, null), intersection);
			normal.set(VectorMath.gramSchmidt(UP, intersection));
			normal.normalise(normal);
		} else {
			Vector3f oc = (y <= 0f) ? new Vector3f(toOrigin) : Vector3f.sub(origin, sphereB, null);
			b = Vector3f.dot(direction, oc);
			c = oc.lengthSquared() - radius * radius;
			h = b * b - c;
			
			if(h <= 0f) return null;
			
			distance = -b - (float) Math.sqrt(h);
			Vector3f.add(origin, VectorMath.mul(direction, distance, null), intersection);
			Vector3f.sub(intersection, VectorMath.mul(UP, halfCyl, null), normal);
			normal.normalise(normal);
		}
		
		if(distance > maxLength) return null;
		
		if(global) return new Vector3f[]{toGlobalPosition(intersection), toGlobalDirection(normal)};
		return new Vector3f[]{intersection, normal};
	}
	
	@Override
	public Collision[] testForCollisions(Volume volume) {
		if(volume instanceof Landscape) return volume.testForCollisions(this);
		return new Collision[]{GJK.detect(this, volume)};
	}
	
	@Override
	public Collision[] testForResolutions(Volume volume) {
		if(volume instanceof Landscape) return volume.testForResolutions(this);
		Collision[] collisions = new Collision[]{EPA.run(GJK.run(this, volume), this, volume)};
		return collisions;
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

	public float getHalfCyl() {
		return halfCyl;
	}

	public void setHalfCyl(float halfHeight) {
		this.halfCyl = halfHeight;
	}
	
	public static void main(String[] args) {
		Capsule capsule = new Capsule(2f, 1f, Type.SOLID, null);
		Body body = new Body();
			body.setPosition(7f, 11f, 15f);
			body.updateTransformation();
			body.addVolume(capsule);
		
//		Vector3f[] results = capsule.raycast(new Vector3f(9f, 15f, 17f), new Vector3f(-1f, -1f, -1f), 10f, true);
		Vector3f[] results = capsule.raycast(new Vector3f(10f, 11f, 18f), new Vector3f(-0.707f, 0f, -0.707f), 10f, true);
		System.out.println("Intersection = " + results[0]);
		System.out.println("Normal = " + results[1]);
	}

}
