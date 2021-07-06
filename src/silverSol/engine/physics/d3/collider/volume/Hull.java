package silverSol.engine.physics.d3.collider.volume;

import org.lwjgl.util.vector.Vector3f;

import silverSol.engine.physics.d3.collision.Collision;
import silverSol.engine.physics.d3.det.narrow.algs.EPA;
import silverSol.engine.physics.d3.det.narrow.algs.GJK;
import silverSol.engine.physics.d3.det.narrow.algs.SeparatingAxis;
import silverSol.math.NumberMath;
import silverSol.math.TriangleMath;
import silverSol.math.VectorMath;

public class Hull extends Volume {
		
	private Vector3f[] vertices;
	private int[] indices;
	private float radius;
	
	public Hull(Type collisionType, Object colliderData) {
		super(collisionType, colliderData);
		vertices = new Vector3f[0];
	}
	
	public Hull(Vector3f[] vertices, int[] indices, Type collisionType, Object colliderData) {
		super(collisionType, colliderData);
		this.vertices = vertices.clone();	
		this.indices = indices.clone();
		radius = VectorMath.longest(vertices).length();
	}

	@Override
	public void calculateEndpoints() {
		endpoints[0].value = position.x - radius;
		endpoints[1].value = position.x + radius;
		endpoints[2].value = position.y - radius;
		endpoints[3].value = position.y + radius;
		endpoints[4].value = position.z - radius;
		endpoints[5].value = position.z + radius;
	}

	@Override
	public Vector3f supportMap(Vector3f globalDirection, boolean global) {
		if(vertices.length == 0) return null;
		Vector3f direction = toLocalDirection(globalDirection);
		
		float[] distances = new float[vertices.length];
		for(int i = 0; i < distances.length; i++) distances[i] = Vector3f.dot(vertices[i], direction);
		
		Vector3f support = new Vector3f(vertices[NumberMath.maxIndex(distances)]);
		
		return global ? this.toGlobalPosition(support) : support;
	}

	@Override
	public Vector3f[] raycast(Vector3f globalOrigin, Vector3f globalDirection, float maxLength, boolean global) {	
		System.out.println("Hull.raycast(): Casting");
		
		Vector3f origin = toLocalPosition(globalOrigin);
		Vector3f direction = toLocalDirection(globalDirection);
		float maxLengthSq = maxLength * maxLength;
		
		Vector3f intersection = null;
		Vector3f normal = null;
		float closest = Float.POSITIVE_INFINITY;
		for(int i = 0; i + 2 < indices.length; i += 3) {
			Vector3f t1 = vertices[indices[i]];
			Vector3f t2 = vertices[indices[i+1]];
			Vector3f t3 = vertices[indices[i+2]];
			Vector3f p = TriangleMath.rayIntersection(origin, direction, t1, t2, t3);
			
			if(p == null) continue;
			float distanceSq = Vector3f.sub(p, origin, null).lengthSquared();
			
			if(distanceSq > closest) continue;
			if(distanceSq > maxLengthSq) continue;
			
			closest = distanceSq;
			intersection = new Vector3f(p);
			normal = Vector3f.cross(Vector3f.sub(t2, t1, null), Vector3f.sub(t3, t2, null), null);
		}
		
		if(intersection == null) return null;
		normal.normalise(normal);
		
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
		return new Collision[]{EPA.run(GJK.run(this, volume), this, volume)};
	}
	
	@Override
	public SeparatingAxis[] getSeparatingAxes(Volume other) {
		return new SeparatingAxis[0];
	}
}
