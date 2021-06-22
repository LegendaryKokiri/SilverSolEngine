package silverSol.engine.physics.d3.det.narrow.algs;

import org.lwjgl.util.vector.Vector3f;

/**
 * Represents a separating axis for use by the Separating Axis Theorem.
 * @author Julian
 *
 */
public class SeparatingAxis {
	
	private Vector3f axis;
	
	public enum Resolution {
		NONE, FORWARD, BACKWARD, BOTH
	}
	private Resolution resolution;
	
	/**
	 * Creates a separating axis object
	 * @param axis The separating axis
	 * @param resolution In which directions should the OTHER body resolve along this axis?
	 */
	public SeparatingAxis(Vector3f axis, Resolution resolution) {
		this.axis = new Vector3f(axis);
		this.resolution = resolution;
	}
	
	public Vector3f getAxis() {
		return axis;
	}
	
	public Resolution getResolution() {
		return resolution;
	}

}
