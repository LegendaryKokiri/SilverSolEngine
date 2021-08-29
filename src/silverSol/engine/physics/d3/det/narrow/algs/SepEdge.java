package silverSol.engine.physics.d3.det.narrow.algs;

import org.lwjgl.util.vector.Vector3f;

public class SepEdge extends Separator {
	
	private Vector3f end1;
	private Vector3f end2;

	public SepEdge(Vector3f direction, Vector3f end1, Vector3f end2) {
		super(direction);
		this.end1 = new Vector3f(end1);
		this.end2 = new Vector3f(end2);
	}

	public Vector3f getEnd1() {
		return end1;
	}

	public Vector3f getEnd2() {
		return end2;
	}
}
