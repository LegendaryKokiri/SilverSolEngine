package silverSol.engine.physics.d3.collider.volume;

import org.lwjgl.util.vector.Vector3f;

import silverSol.engine.physics.d3.collider.Collider;
import silverSol.engine.physics.d3.collision.Collision;
import silverSol.engine.physics.d3.det.narrow.algs.EPA;
import silverSol.engine.physics.d3.det.narrow.algs.GJK;
import silverSol.engine.physics.d3.det.narrow.algs.SepEdge;
import silverSol.engine.physics.d3.det.narrow.algs.SepPlane;
import silverSol.math.NumberMath;
import silverSol.math.TriangleMath;
import silverSol.math.VectorMath;

public class Hull extends Volume {
		
	private Vector3f[] vertices;
	private int[] indices;
	private float radius;
	
	private Vector3f[] normals;
	
	public Hull(Type collisionType, Object colliderData) {
		super(collisionType, colliderData);
		this.vertices = new Vector3f[0];
		this.indices = new int[0];
		this.normals = new Vector3f[0];
	}
	
	public Hull(Vector3f[] vertices, int[] indices, Type collisionType, Object colliderData) {
		super(collisionType, colliderData);
		this.vertices = vertices.clone();	
		this.indices = indices.clone();
		this.radius = VectorMath.longest(vertices).length();
		
		this.normals = new Vector3f[indices.length / 3];
		for(int i = 0; i + 2 < indices.length; i += 3) {
			Vector3f edge1 = Vector3f.sub(vertices[indices[i+1]], vertices[indices[i]], null);
			Vector3f edge2 = Vector3f.sub(vertices[indices[i+2]], vertices[indices[i+1]], null);
			Vector3f.cross(edge1, edge2, null).normalise(this.normals[i/3]);
		}
	}
	
	public Collider clone() {
		return new Hull(vertices, indices, type, colliderData);
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
	public SepPlane[] getSeparatingPlanes(Planar planar) {
		SepPlane[] planes = new SepPlane[normals.length];
		
		for(int i = 0; i < normals.length; i++) {
			planes[i] = new SepPlane(normals[i]);
		}
		
		return planes;
	}
	
	@Override
	public SepEdge[] getSeparatingEdges(Planar planar) {
		SepEdge[] edges = new SepEdge[indices.length];
		
		for(int i = 0; i < edges.length; i++) {
			Vector3f end1 = toGlobalPosition(vertices[indices[i]]);
			Vector3f end2 = toGlobalPosition(vertices[indices[(i+1)%indices.length]]);
			edges[i] = new SepEdge(Vector3f.sub(end2, end1, null), end1, end2);
		}
		
		return edges;
	}
	
}
