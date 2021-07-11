package silverSol.engine.physics.d3.det.narrow;

import java.util.ArrayList;
import java.util.List;

import silverSol.engine.physics.d3.collider.Collider;
import silverSol.engine.physics.d3.collision.Collision;

public class NarrowPhase {
	
	private PairManager pairManager;
	private List<Collision> collisions;
	
	public NarrowPhase() {
		pairManager = new PairManager();
		collisions = new ArrayList<>();
	}
	
	public void run() {
		collisions.clear();
		
		List<Collider[]> pairs = pairManager.getPairs();
		for(Collider[] pair : pairs) {
			Collider collider1 = pair[0];
			Collider collider2 = pair[1];
						
			Collision[] pairCollisions = null;
			if(collider1.oughtOmitData() && collider2.oughtOmitData()) pairCollisions = collider1.testForCollisions(collider2);
			else pairCollisions = collider1.testForResolutions(collider2);
			
			if(pairCollisions != null) {
				for(Collision collision : pairCollisions) {
					if(collision != null) {
						if(!collider1.filterCollision(collision)) continue;
						if(!collider2.filterCollision(collision)) continue;
						
						collider1.addCollision(collision);
						collider2.addCollision(collision);
						collisions.add(collision);
					}
				}
			}
		}
	}
	
	public PairManager getPairManager() {
		return pairManager;
	}
	
	public List<Collision> getCollisions() {
		return collisions;
	}
	
	public void clearData() {
		pairManager.clearPairs();
		collisions.clear();
	}
}
