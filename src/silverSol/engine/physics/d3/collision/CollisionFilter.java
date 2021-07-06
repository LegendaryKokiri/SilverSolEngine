package silverSol.engine.physics.d3.collision;

import silverSol.engine.physics.d3.collider.Collider;

public interface CollisionFilter {
	
	/**
	 * Returns whether or not a generated Collision ought to be evaluated.
	 * @param collision The Collision to evaluate
	 * @param collider The Collider involved in the collision
	 * @return true if the Collision ought be resolved, and false if the Collision ought be ignored
	 */
	public boolean filter(Collision collision, Collider collider);

}
