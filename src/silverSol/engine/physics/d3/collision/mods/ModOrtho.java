package silverSol.engine.physics.d3.collision.mods;

import org.lwjgl.util.vector.Vector3f;

import silverSol.engine.physics.d3.collision.Collision;
import silverSol.engine.physics.d3.collision.CollisionMod;
import silverSol.math.VectorMath;

public class ModOrtho implements CollisionMod {
	
	private static final float PI = (float) Math.PI;
	
	protected Vector3f axis;
	protected float alignAngle;
	
	/**
	 * Modifies collisions such that their separating axes are either parallel or
	 * orthogonal to the passed axis.
	 * @param axis The global axis with which to form the orthogonal basis
	 * @param alignAngle Separating axes that form an angle of at most alignAngle (in radians) with axis
	 * will be made parallel to axis. The rest will be made orthogonal to axis.
	 */
	public ModOrtho(Vector3f axis, float alignAngle) {
		this.axis = new Vector3f(axis.normalise(null));
		this.alignAngle = alignAngle;
	}

	@Override
	public void modify(Collision collision) {
		Vector3f separatingAxis = new Vector3f(collision.getSeparatingAxis());
		float angle = Vector3f.angle(axis, separatingAxis);
		
		Vector3f newSeparation = new Vector3f();
		if(angle <= alignAngle) newSeparation.set(axis);
		else if(PI - angle <= alignAngle) newSeparation.set(axis.negate(null));
		else VectorMath.gramSchmidt(axis, separatingAxis, null).normalise(newSeparation);
		
		float projection = Vector3f.dot(separatingAxis, newSeparation);
		collision.setSeparatingAxis(newSeparation);
		collision.setPenetrationDepth(collision.getPenetrationDepth() / projection);
	}

}
