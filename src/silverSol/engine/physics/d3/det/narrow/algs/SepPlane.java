package silverSol.engine.physics.d3.det.narrow.algs;

import org.lwjgl.util.vector.Vector3f;

/**
 * Represents a separating plane for use by the Separating Axis Theorem.
 * @author Julian
 *
 */
public class SepPlane extends Separator{
		
	/**
	 * Creates a separating plane object
	 * @param direction The normal of the separating plane
	 */
	public SepPlane(Vector3f direction) {
		super(direction);
	}

}
