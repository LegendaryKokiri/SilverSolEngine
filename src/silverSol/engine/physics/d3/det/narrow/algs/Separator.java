package silverSol.engine.physics.d3.det.narrow.algs;

import org.lwjgl.util.vector.Vector3f;

public class Separator {
	
	private Vector3f direction;
	
	public Separator(Vector3f direction) {
		this.direction = new Vector3f(direction);
	}
	
	public Vector3f getDirection() {
		return direction;
	}

}
