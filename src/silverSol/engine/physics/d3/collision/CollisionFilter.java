package silverSol.engine.physics.d3.collision;

import silverSol.engine.physics.d3.collider.Collider;

public interface CollisionFilter {
	
	public boolean filter(Collision collision, Collider collider);

}
