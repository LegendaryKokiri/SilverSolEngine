package silverSol.engine.physics.d3.collider.volume;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.util.vector.Vector3f;

import silverSol.engine.physics.d3.collision.Collision;
import silverSol.engine.physics.d3.det.narrow.algs.SeparatingAxis;
import silverSol.math.NumberMath;
import silverSol.math.TriangleMath;

public class Mesh extends Volume {
	
	private class Edge {
		private int e1;
		private int e2;
		
		public Edge(int e1, int e2) {
			this.e1 = e1;
			this.e2 = e2;
		}
		
		public Edge getReversed() {
			return new Edge(this.e2, this.e1);
		}
		
		@Override
		public boolean equals(Object obj) {
			if(!(obj instanceof Edge)) return false;
			Edge e = (Edge) obj;
			return this.e1 == e.e1 && this.e2 == e.e2;
		}
		
		@Override
		public int hashCode() {
			return e1 * 31 + e2;
		}
		
		@Override
		public String toString() {
			return "Edge " + this.e1 + " - " + this.e2;
		}
	}
	
	private class Face {
		private int f1;
		private int f2;
		private int f3;
		
		public Face(int f1, int f2, int f3) {
			this.f1 = f1;
			this.f2 = f2;
			this.f3 = f3;
		}
		
		@Override
		public boolean equals(Object obj) {
			if(!(obj instanceof Face)) return false;
			Face f = (Face) obj;
			return this.f1 == f.f1 && this.f2 == f.f2 && this.f3 == f.f3;
		}
	}
	
	protected Vector3f[] vertices;
	protected int[] indices;
	protected float radius;
	protected Hull[] hulls;
	
	public Mesh(Type collisionType, Object colliderData) {
		super(collisionType, colliderData);
		this.vertices = new Vector3f[0];
		this.indices = new int[0];
		this.hulls = new Hull[0];
	}
	
	//TODO: This does not consider rotating the mesh
	public Mesh(Vector3f[] vertices, int[] indices, Type collisionType, Object colliderData) {
		super(collisionType, colliderData);
		init(vertices, indices, collisionType, colliderData);
	}
	
	protected void init(Vector3f[] vertices, int[] indices, Type collisionType, Object colliderData) {
		this.vertices = new Vector3f[vertices.length];
		for(int i = 0; i < vertices.length; i++) this.vertices[i] = new Vector3f(vertices[i]);
		this.indices = indices.clone();
		this.hulls = calculateConvexity(collisionType, colliderData);
	}
	
	//TODO: We are currently making the foolish assumption that each edge can only occur once. For now, we embrace said foolishness, though it may not hold up for long.
	private Hull[] calculateConvexity(Type collisionType, Object colliderData) {
		Map<Edge, Face> faces = new HashMap<>();
		Map<Face, List<Face>> hulls = new HashMap<>();
		
		for(int i = 0; i + 2 < indices.length; i += 3) {
			Face face = new Face(indices[i], indices[i+1], indices[i+2]);
			for(int j = 0; j < 3; j++) {
				faces.put(new Edge(indices[i+(j%3)], indices[i+((j+1)%3)]), face);
			}		
		}
				
		for(int i = 0; i + 2 < indices.length; i += 3) {
			Face face = faces.get(new Edge(indices[i], indices[i+1]));
			if(!hulls.containsKey(face)) hulls.put(face, new ArrayList<Face>());
			
			List<Face> faceHull = hulls.get(face);
			faceHull.add(face);  
			
			for(int j = 0; j < 3; j++) {
				Edge edge = new Edge(indices[i+(j%3)], indices[i+((j+1)%3)]);
				Edge reversed = edge.getReversed();
				
				if(faces.containsKey(reversed)) {
					Face reverseFace = faces.get(reversed);
					if(convex(reverseFace, indices[i+((j+2)%3)])) {
						if(!hulls.containsKey(reverseFace)) {
							List<Face> newHull = new ArrayList<>();
							newHull.add(reverseFace);
							hulls.put(reverseFace, newHull);
						}
						
						List<Face> reverseHull = hulls.get(reverseFace);
						if(!reverseHull.contains(face)) reverseHull.add(face);
						hulls.put(face, reverseHull);
					}
				}
			}
		}
		
		List<Hull> hullList = new ArrayList<>();
		for(Face face : hulls.keySet()) {
			List<Face> hullFaces = hulls.get(face);
			List<Vector3f> hullVertices = new ArrayList<>();
			
			for(Face hullFace : hullFaces) {
				if(!hullVertices.contains(vertices[indices[hullFace.f1]])) hullVertices.add(vertices[indices[hullFace.f1]]);
				if(!hullVertices.contains(vertices[indices[hullFace.f2]])) hullVertices.add(vertices[indices[hullFace.f2]]);
				if(!hullVertices.contains(vertices[indices[hullFace.f3]])) hullVertices.add(vertices[indices[hullFace.f3]]);
			}
			
			hullList.add(new Hull(hullVertices.toArray(new Vector3f[0]), new int[0], collisionType, colliderData));
		}
		
		return hullList.toArray(new Hull[0]);
	}
	
	private boolean convex(Face face, int uncommonPoint) {
		Vector3f t1 = Vector3f.sub(vertices[face.f2], vertices[face.f1], null);
		Vector3f t2 = Vector3f.sub(vertices[face.f3], vertices[face.f2], null);
		Vector3f toPoint = Vector3f.sub(vertices[uncommonPoint], vertices[face.f1], null);
		return Vector3f.dot(Vector3f.cross(t1, t2, null), toPoint) <= 0f;
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
	//TODO: Find something more efficient.
	public Vector3f[] raycast(Vector3f globalOrigin, Vector3f globalDirection,
			float maxLength, boolean global) {
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
		List<Collision> collisions = new ArrayList<>();
		
		for(Hull hull : hulls) {
			Collision[] hullCollisions = hull.testForCollisions(volume);
			if(hullCollisions != null) {
				for(Collision hullCollision : hullCollisions) collisions.add(hullCollision);
			}
		}
		
		return collisions.toArray(new Collision[0]);
	}

	@Override
	public Collision[] testForResolutions(Volume volume) {
		List<Collision> collisions = new ArrayList<>();
		
		for(Hull hull : hulls) {
			Collision[] hullCollisions = hull.testForResolutions(volume);
			if(hullCollisions != null) {
				for(Collision c : hullCollisions) {
					c.setContactA(this.toLocalPosition(c.getGlobalContactA()), c.getGlobalContactA());
					c.setContactB(this.toLocalPosition(c.getGlobalContactB()), c.getGlobalContactB());
					collisions.add(c);
				}
			}
		}
		
		return collisions.toArray(new Collision[0]);
	}
	
	@Override
	public SeparatingAxis[] getSeparatingAxes(Volume other) {
		return new SeparatingAxis[0];
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
	
}
