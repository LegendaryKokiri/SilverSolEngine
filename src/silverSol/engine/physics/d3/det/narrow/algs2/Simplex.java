package silverSol.engine.physics.d3.det.narrow.algs2;

import org.lwjgl.util.vector.Vector3f;

public class Simplex {
	
	private Support[] cso;
	private int numVertices;
	private boolean containsOrigin;
	
	public Simplex() {
		cso = new Support[4];
		
		for(int i = 0; i < 4; i++) {
			cso[i] = new Support();
		}
		
		numVertices = 0;
	}
	
	public void expand(Support support) {
		if(numVertices >= 4) return;
		
		cso[numVertices].set(support.getS(), support.getS1(), support.getS2());
		numVertices++;
	}
	
	public void expand(Vector3f csoSupport, Vector3f support1, Vector3f support2) {
		if(numVertices >= 4) return;
		
		cso[numVertices].set(csoSupport, support1, support2);
		numVertices++;
	}
	
	//Reorder vertices such that 0-1-2 is counter-clockwise
	public void orientFaces() {
		Vector3f a = cso[0].getS();
		Vector3f b = cso[1].getS();
		Vector3f c = cso[2].getS();
		Vector3f d = cso[3].getS();
		
		Vector3f ab = Vector3f.sub(b, a, null);
		Vector3f ac = Vector3f.sub(c, a, null);
		Vector3f ad = Vector3f.sub(d, a, null);
		float det = Vector3f.dot(ad, Vector3f.cross(ab, ac, null));
		
		if(det > 0f) {
			Support temp = cso[0];
			cso[0] = cso[1];
			cso[1] = temp;
		}
	}
	
	public void removeVertex(int index) {
		if(numVertices == 0) return;
		if(index < 0 || index >= numVertices) return;
		
		for(int i = index; i < 3; i++) cso[i] = cso[i+1];
		
		numVertices--;
	}
	
	public Support[] getCso() {
		return cso;
	}
	
	public int getNumVertices() {
		return numVertices;
	}
	
	public boolean containsOrigin() {
		return containsOrigin;
	}
	
	@Override
	public String toString() {
		String s = "Simplex";
		for(int i = 0; i < numVertices; i++) s += (i == 0 ? ": " : " - ") + cso[i].getS();
		return s;
	}
	
}
