package silverSol.engine.physics.d3.res;

import java.util.ArrayList;
import java.util.List;

import silverSol.engine.physics.d3.body.Body;
import silverSol.engine.physics.d3.collider.Collider;
import silverSol.engine.physics.d3.collider.volume.Volume;
import silverSol.engine.physics.d3.collision.Collision;
import silverSol.engine.physics.d3.constraint.Constraint;

public class CollisionResolution {
	
	private List<Constraint> constraints;
	
	public CollisionResolution() {
		constraints = new ArrayList<>();
	}
	
	public List<Constraint> run(List<Collision> collisions) {
		constraints.clear();
		
		for(Collision collision : collisions) {
			Collider colliderA = collision.getColliderA();
			Collider colliderB = collision.getColliderB();
			
			if(!(colliderA instanceof Volume)) continue;
			if(!(colliderB instanceof Volume)) continue;
			
			Volume volumeA = (Volume) colliderA;
			Volume volumeB = (Volume) colliderB;
			
			Body bodyA = volumeA.getBody();
			Body bodyB = volumeB.getBody();
			
			volumeA.addCollision(collision);
			volumeB.addCollision(collision);
			
			if(volumeA.oughtResolve() && volumeB.oughtResolve()) {
				//Volume A gets priority in the case of a tie.
				if(volumeB.getModPriority() > volumeA.getModPriority()) volumeB.modifyCollision(collision);
				else volumeA.modifyCollision(collision);
				
				storeConstraints(bodyA, collision);
				storeConstraints(bodyB, collision);
			}
		}
		
		return constraints;
	}
	
	private void storeConstraints(Body body, Collision collision) {
		Constraint[] constraints = body.generateConstraints(collision);
		if(constraints == null) return;
		
		for(Constraint constraint : constraints) {
			constraint.setResolved(true); //Contact constraints are used once and then removed.
			this.constraints.add(constraint);
		}
	}
}
