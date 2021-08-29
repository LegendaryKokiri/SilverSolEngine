package silverSol.engine.physics.d3.collider.volume;

import org.lwjgl.util.vector.Vector3f;

import silverSol.engine.physics.d3.body.Body;
import silverSol.engine.physics.d3.collider.Collider;
import silverSol.engine.physics.d3.collision.Collision;
import silverSol.engine.physics.d3.det.narrow.algs.EPA;
import silverSol.engine.physics.d3.det.narrow.algs.GJK;
import silverSol.engine.physics.d3.det.narrow.algs.SepEdge;
import silverSol.engine.physics.d3.det.narrow.algs.SepPlane;
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
	
	public Collider clone() {
		return new Capsule(halfCyl, radius, type, colliderData);
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
			VectorMath.gramSchmidt(UP, intersection, normal);
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
	public SepPlane[] getSeparatingPlanes(Planar planar) {		
		Vector3f topSphere = toGlobalPosition(new Vector3f(0f, halfCyl, 0f));
		Vector3f bottomSphere = toGlobalPosition(new Vector3f(0f, -halfCyl, 0f));
		
		Vector3f topClosest = planar.closestPointTo(topSphere, true);
		Vector3f bottomClosest = planar.closestPointTo(bottomSphere, true);
		
		Vector3f toTop = Vector3f.sub(topClosest, topSphere, null);
		Vector3f toBottom = Vector3f.sub(bottomClosest, bottomSphere, null);
				
		toTop.normalise(toTop);
		toBottom.normalise(toBottom);
		
		return new SepPlane[] {new SepPlane(toTop), new SepPlane(toBottom)};
	}
	
	@Override
	public SepEdge[] getSeparatingEdges(Planar planar) {
		Vector3f dP = toLocalPosition(planar.position);
		Vector3f toEdge = VectorMath.mul(VectorMath.gramSchmidt(UP, dP, null), radius, null);
		
		Vector3f toCylBound = VectorMath.mul(UP, halfCyl, null);
		Vector3f end1 = toGlobalPosition(Vector3f.add(toEdge, toCylBound, null));
		Vector3f end2 = toGlobalPosition(Vector3f.sub(toEdge, toCylBound, null));
		
		return new SepEdge[] {new SepEdge(toGlobalDirection(UP), end1, end2)};
	}
	
	public Vector3f closestPointTo(Vector3f globalPoint, boolean global) {
		Vector3f local = toLocalPosition(globalPoint);
		
		Vector3f vLocal = new Vector3f();
		if(local.y > halfCyl) {
			Vector3f sphereCenter = new Vector3f(0f, halfCyl, 0f);
			Vector3f toLocal = Vector3f.sub(local, sphereCenter, null);
			VectorMath.setLength(toLocal, radius, toLocal);
			Vector3f.add(sphereCenter, toLocal, vLocal);
		} else if(local.y < -halfCyl) {
			Vector3f sphereCenter = new Vector3f(0f, -halfCyl, 0f);
			Vector3f toLocal = Vector3f.sub(local, sphereCenter, null);
			VectorMath.setLength(toLocal, radius, toLocal);
			Vector3f.add(sphereCenter, toLocal, vLocal);
		} else {
			Vector3f toCyl = new Vector3f(local.x, 0f, local.z);
			VectorMath.setLength(toCyl, radius, toCyl);
			vLocal.set(toCyl.x, local.y, toCyl.z);
		}
		
		return global ? toGlobalPosition(vLocal) : vLocal;
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
		
		System.out.println("PROXIMITY TESTS");
		Vector3f[] proximityTests = new Vector3f[]{new Vector3f(10f, 16f, 15f), new Vector3f(7.707f, 13.707f, 15f),
				new Vector3f(4f, 6f, 15f), new Vector3f(6.293f, 8.293f, 15f),
				new Vector3f(4f, 12f, 8f), new Vector3f(6.606f, 12f, 14.081f)};
		
		
		for(int i = 0; i < proximityTests.length; i += 2) {
			Vector3f closest = capsule.closestPointTo(proximityTests[i], true);
			if(!VectorMath.getEqual(closest, proximityTests[i+1])) System.out.println("FAILED proximity test " + (i/2+1) + "! Closest = " + closest + " instead of " + proximityTests[i+1]);
			else System.out.println("Passed proximity test " + (i/2+1));
		}
	}

}
