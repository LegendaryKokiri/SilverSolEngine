package silverSol.engine.physics.d3.det.narrow.algs2;

import org.lwjgl.util.vector.Vector3f;

public class Support {

	//Global Point
	private Vector3f s;
	
	//Local Points
	private Vector3f s1;
	private Vector3f s2;
	
	public Support() {
		this.s = new Vector3f();
		this.s1 = new Vector3f();
		this.s2 = new Vector3f();
	}
	
	public Support(Vector3f s, Vector3f s1, Vector3f s2) {
		this.s = new Vector3f(s);
		this.s1 = new Vector3f(s1);
		this.s2 = new Vector3f(s2);
	}

	public Support(Support s) {
		this.s = new Vector3f(s.s);
		this.s1 = new Vector3f(s.s1);
		this.s2 = new Vector3f(s.s2);
	}
	
	public Vector3f getS() {
		return s;
	}

	public Vector3f getS1() {
		return s1;
	}

	public Vector3f getS2() {
		return s2;
	}
	
	public void set(Vector3f s, Vector3f s1, Vector3f s2) {
		this.s.set(s);
		this.s1.set(s1);
		this.s2.set(s2);
	}
	
	@Override
	public String toString() {
		return "Support " + s + " from " + s1 + " - " + s2;
	}
	
}
