package silverSol.engine.physics.d3.det.narrow.algs2;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Vector3f;

import silverSol.math.TriangleMath;
import silverSol.math.VectorMath;

public class Polytype {
	
	private static final Vector3f ORIGIN = new Vector3f();
	private static final float EPSILON = 1E-3f;
	
	private List<Edge> edges;
	private List<Face> faces;
	
	public Polytype(Simplex simplex) {
		edges = new ArrayList<>();
		faces = new ArrayList<>();
		
		Support[] cso = simplex.getCso();
		
		addFace(cso[0], cso[1], cso[2]);
		addFace(cso[0], cso[2], cso[3]);
		addFace(cso[0], cso[3], cso[1]);
		addFace(cso[3], cso[2], cso[1]);
	}
	
	private class Edge {
		
		private Support e1;
		private Support e2;
		
		public Edge(Support e1, Support e2) {
			this.e1 = e1;
			this.e2 = e2;
		}
		
		@Override
		public String toString() {
			return "Edge " + e1.getS() + " - " + e2.getS();
		}
	}
	
	public class Face {
		
		private Support t1;
		private Support t2;
		private Support t3;
		
		private Vector3f normal;
		private float distance;
		
		public Face(Support t1, Support t2, Support t3) {
			this.t1 = new Support(t1);
			this.t2 = new Support(t2);
			this.t3 = new Support(t3);
			
			Vector3f edge12 = Vector3f.sub(this.t2.getS(), this.t1.getS(), null);
			Vector3f edge13 = Vector3f.sub(this.t3.getS(), this.t1.getS(), null);
			this.normal = Vector3f.cross(edge12, edge13, null);
						
			//Handle degenerate face with an algorithm that can handle degenerate triangles
			if(this.normal.lengthSquared() < EPSILON) {
				this.normal.set(TriangleMath.closestPointTo(ORIGIN, this.t1.getS(), this.t2.getS(), this.t3.getS()));
			}
			
			this.normal.normalise(this.normal);
			
			//Ensure outward pointing
			this.distance = Vector3f.dot(this.t1.getS(), this.normal);
			if(this.distance < 0f) flipDirection();
		}
		
		private void flipDirection() {
			Support temp = this.t1;
			this.t1 = t2;
			this.t2 = temp;
			this.normal.negate(this.normal);
			this.distance *= -1f;
		}
		
		public Support getT1() {
			return t1;
		}
		
		public Support getT2() {
			return t2;
		}
		
		public Support getT3() {
			return t3;
		}
		
		public Vector3f getNormal() {
			return normal;
		}
		
		public float getDistance() {
			return distance;
		}
		
		@Override
		public String toString() {
			return "Face " + t1.getS() + " - " + t2.getS() + " - " + t3.getS() + " (" + normal + ")";
		}
		
	}
	
	private void addEdge(Edge e) {
		for(int i = 0; i < edges.size(); i++) {
			Edge ep = edges.get(i);
			
			if(VectorMath.getEqual(e.e1.getS(), ep.e2.getS()) && VectorMath.getEqual(e.e2.getS(), ep.e1.getS())) {
				edges.remove(i);
				return;
			}
		}
		
		edges.add(e);
	}
	
	private void addFace(Support t1, Support t2, Support t3) {
		Face face = new Face(t1, t2, t3);
		
		for(int i = 0; i <= faces.size(); i++) {
			if(i == faces.size() || face.distance < faces.get(i).distance) {
				faces.add(i, face);
				break;
			}
		}
	}
	
	public void patch(Support s) {
		for(Edge edge : edges) {
			addFace(s, edge.e1, edge.e2);
		}
		
		edges.clear();
	}
	
	public Face getClosestFace() {
		if(faces.size() == 0) return null;		
		return faces.get(0);
	}
	
	public void removeClosestFace() {	
		if(faces.size() == 0) return;
		removeFace(0);
	}
	
	public void removeVisible(Support support) {
		Vector3f s = support.getS();
		
		for(int i = 0; i < faces.size(); i++) {
			Face face = faces.get(i);
			
			if(Vector3f.dot(Vector3f.sub(s, face.t1.getS(), null), face.normal) >= 0f) {
				removeFace(i);
				i--;
			}
		}
	}
	
	private void removeFace(int index) {
		Face face = faces.get(index);
		faces.remove(index);
		
		addEdge(new Edge(face.t1, face.t2));
		addEdge(new Edge(face.t2, face.t3));
		addEdge(new Edge(face.t3, face.t1));
	}
	
}
