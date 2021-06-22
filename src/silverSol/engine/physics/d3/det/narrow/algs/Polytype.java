package silverSol.engine.physics.d3.det.narrow.algs;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Vector3f;

import silverSol.engine.physics.d3.collider.volume.Volume;
import silverSol.engine.physics.d3.collision.Collision;
import silverSol.math.TriangleMath;
import silverSol.math.VectorMath;
import silverSol.math.geometry.Triangle;

public class Polytype {

	private static final Vector3f ORIGIN = new Vector3f();
	private static final float EXIT_THRESHOLD = 1E-2f;
	
	private class SupportTriangle extends Triangle {
		
		private Vector3f s0A;
		private Vector3f s1A;
		private Vector3f s2A;
		private Vector3f s0B;
		private Vector3f s1B;
		private Vector3f s2B;
		
		public SupportTriangle(Vector3f t0, Vector3f t1, Vector3f t2, 
				Vector3f s0A, Vector3f s1A, Vector3f s2A,
				Vector3f s0B, Vector3f s1B, Vector3f s2B,
				boolean unitNormal) {
			super(t0, t1, t2, unitNormal);
			this.s0A = s0A;
			this.s1A = s1A;
			this.s2A = s2A;
			this.s0B = s0B;
			this.s1B = s1B;
			this.s2B = s2B;
		}
	}
		
	private class Edge {
		
		private Vector3f e1;
		private Vector3f e2;
		private Vector3f s1A;
		private Vector3f s2A;
		private Vector3f s1B;
		private Vector3f s2B;
		
		public Edge(Vector3f e1, Vector3f e2, Vector3f s1A, Vector3f s2A, Vector3f s1B, Vector3f s2B) {
			this.e1 = e1;
			this.e2 = e2;
			this.s1A = s1A;
			this.s2A = s2A;
			this.s1B = s1B;
			this.s2B = s2B;
		}
		
	}
	
	private List<Edge> edges;
	private List<SupportTriangle> faces;
	private List<Float> distances;
	
	private Vector3f support;
	private Vector3f supportA;
	private Vector3f supportB;
		
	public Polytype(Simplex simplex) {
		edges = new ArrayList<>();
		faces = new ArrayList<>();
		distances = new ArrayList<>();
		
		support = new Vector3f();
		supportA = new Vector3f();
		supportB = new Vector3f();
		
		addSimplexFace(3, 0, 1, simplex.getPoints(), simplex.getPointsV1(), simplex.getPointsV2());
		addSimplexFace(3, 1, 2, simplex.getPoints(), simplex.getPointsV1(), simplex.getPointsV2());
		addSimplexFace(3, 2, 0, simplex.getPoints(), simplex.getPointsV1(), simplex.getPointsV2());
		addSimplexFace(0, 2, 1, simplex.getPoints(), simplex.getPointsV1(), simplex.getPointsV2());		
	}
	
	public boolean expand(Simplex simplex, Volume v1, Volume v2) {
		if(faces.size() == 0) return false;
		
		Triangle closestFace = faces.get(0);
		float closestDistance = distances.get(0);
		Vector3f direction = closestFace.getNormal();
		
		simplex.findSupport(direction, v1, v2, supportA, supportB, support);
		
//		if(Vector3f.dot(direction, support) - distances.get(0) < EXIT_THRESHOLD) return false;
		
		for(int i = 0; i < faces.size(); i++) {
			Triangle face = faces.get(i);
			if(facingSupport(face, support)) {
				removeFace(i);
				i--;
			}
		}
		
		
		patchPolytype();
		if(faces.size() == 0) return false;
		
		//Terminate EPA if the new closest face isn't much closer than the last one.
		//This avoids a degenerate case in which the closest point on the CSO is on multiple faces.
		if(Math.abs(closestDistance - distances.get(0)) < EXIT_THRESHOLD) return false;
		
		return true;
	}
	
	//TODO: Determine if the origin being on the edge is a true problem. If so, fix it.
	private boolean facingSupport(Triangle face, Vector3f support) {
		float dot = Vector3f.dot(face.getNormal(), Vector3f.sub(support, face.getT0(), null));
		return dot >= 0;
	}
	
	private void patchPolytype() {
		for(Edge edge : edges) {
			addFace(new SupportTriangle(support, edge.e1, edge.e2,
					supportA, edge.s1A, edge.s2A,
					supportB, edge.s1B, edge.s2B, true));
		}
		
		edges.clear();
	}
	
	public Collision generateCollision(Volume v1, Volume v2) {
		if(faces.size() == 0) return null;
		
		Collision collision = new Collision();
		collision.setColliderA(v1);
		collision.setColliderB(v2);
		
		SupportTriangle closestFace = faces.get(0);
		
		//TODO: Replace these contact calculations with a single call to TriangleMath.getParametricWeights() and doing the math.
		Vector3f closestPoint = TriangleMath.closestPointTo(ORIGIN, closestFace);
		Vector3f contactA = TriangleMath.closestPointTo(ORIGIN, closestFace.s0A, closestFace.s1A, closestFace.s2A);
		Vector3f contactB = TriangleMath.closestPointTo(ORIGIN, closestFace.s0B, closestFace.s1B, closestFace.s2B);
		Vector3f normal = new Vector3f(closestFace.getNormal());
		
		collision.setSeparatingAxis(normal);
		collision.setPenetrationDepth(closestPoint.length());
		collision.setContactA(v1.toLocalPosition(contactA), contactA);
		collision.setContactB(v2.toLocalPosition(contactB), contactB);
		
		return collision;
	}
	
	public void addEdge(Edge edge) {
		for(int i = 0; i < edges.size(); i++) {
			Edge e = edges.get(i);
			if(VectorMath.getEqual(e.e1, edge.e2) && VectorMath.getEqual(e.e2, edge.e1)) {
				edges.remove(i);
				return;
			}
		}
		
		edges.add(edge);
	}
	
	public void addSimplexFace(int v0, int v1, int v2, Vector3f[] points, Vector3f[] pointsV1, Vector3f[] pointsV2) {
		addFace(new SupportTriangle(points[v0], points[v1], points[v2], 
				pointsV1[v0], pointsV1[v1], pointsV1[v2],
				pointsV2[v0], pointsV2[v1], pointsV2[v2],
				true));
	}
	
	public void addFace(SupportTriangle face) {		
		float distance = Vector3f.dot(face.getNormal(), face.getT0());
		for(int i = 0; i <= faces.size(); i++) {
			if(i == faces.size() || distance < distances.get(i)) {
				faces.add(i, face);
				distances.add(i, distance);
				return;
			}
		}
	}
	
	public void removeFace(int faceIndex) {
		SupportTriangle face = faces.get(faceIndex);
		addEdge(new Edge(face.getT0(), face.getT1(), face.s0A, face.s1A, face.s0B, face.s1B));
		addEdge(new Edge(face.getT1(), face.getT2(), face.s1A, face.s2A, face.s1B, face.s2B));
		addEdge(new Edge(face.getT2(), face.getT0(), face.s2A, face.s0A, face.s2B, face.s0B));
		
		faces.remove(faceIndex);
		distances.remove(faceIndex);
	}
	
}
