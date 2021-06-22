package silverSol.math.geometry;

import org.lwjgl.util.vector.Vector3f;

import silverSol.math.VectorMath;

public class Triangle {

	private static final float EPSILON = 2E-2f;
	private static final float EPSILON_SQ = EPSILON * EPSILON;
	
	protected Vector3f t0, t1, t2;
	protected Vector3f normal;
	protected boolean degenerate;
	
	public Triangle(Vector3f t0, Vector3f t1, Vector3f t2, boolean unitNormal) {
		this.t0 = new Vector3f(t0);
		this.t1 = new Vector3f(t1);
		this.t2 = new Vector3f(t2);
		
		this.normal = new Vector3f(Vector3f.cross(Vector3f.sub(t1, t0, null), Vector3f.sub(t2, t0, null), null));
		
		float normalLength = normal.lengthSquared();
		this.degenerate = normalLength < EPSILON_SQ;
		
		if(degenerate) normal.set(VectorMath.mean(t0, t1, t2));

		if(degenerate && normal.lengthSquared() < EPSILON_SQ) normal.set(0f, 0f, 0f);
		else if(unitNormal) normal.normalise(normal);
	}
	
	public void orderCCW() {		
		if(Vector3f.dot(normal, t0) < 0) {
			Vector3f temp = new Vector3f(t2);
			t2.set(t1);
			t1.set(temp);
			normal.negate(normal);
		}
	}
	
	public void orderCCW(Vector3f reference) {		
		if(Vector3f.dot(normal, Vector3f.sub(t0, reference, null)) < 0) {
			Vector3f temp = new Vector3f(t2);
			t2.set(t1);
			t1.set(temp);
			normal.negate(normal);
		}
	}

	public Vector3f getT0() {
		return t0;
	}

	public Vector3f getT1() {
		return t1;
	}

	public Vector3f getT2() {
		return t2;
	}

	public Vector3f getNormal() {
		return normal;
	}

	public boolean isDegenerate() {
		return degenerate;
	}

	@Override
	public String toString() {
		return "Triangle " + t0 + " - " + t1 + " - " + t2;
	}
	
}
