package silverSol.engine.physics.d3.det.narrow.algs;

import org.lwjgl.util.vector.Vector3f;

public class SepEdge {
	
	private Vector3f direction;

	public SepEdge(Vector3f direction) {
		this.direction = new Vector3f(direction);
	}
	
	public Vector3f getDirection() {
		return direction;
	}
}
