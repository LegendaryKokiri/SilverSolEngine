package silverSol.engine.physics.d3.det.narrow.algs;

import org.lwjgl.util.vector.Vector3f;

/**
 * Represents a separating plane for use by the Separating Axis Theorem.
 * @author Julian
 *
 */
public class SepPlane {
	
	private Vector3f normal;
	
	/**
	 * Creates a separating plane object
	 * @param normal The normal of the separating plane
	 * @param resolution In which directions should the OTHER body resolve along this axis?
	 */
	public SepPlane(Vector3f normal) {
		this.normal = new Vector3f(normal);
	}
	
	public Vector3f getNormal() {
		return normal;
	}

}
