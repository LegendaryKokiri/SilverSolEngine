package silverSol.engine.physics.d3.det.narrow;

import java.util.ArrayList;
import java.util.List;

import silverSol.engine.physics.d3.collider.Collider;
import silverSol.engine.physics.d3.collision.Collision;

public class PairManager {
	
	private List<Collider[]> pairs;
	
	public PairManager() {
		pairs = new ArrayList<>();
	}
	
	public void addPair(Collider c1, Collider c2) {
		pairs.add(new Collider[]{c1, c2});
	}
	
	public void updatePairs(List<Collision> collisions) {
		collisions.clear();
		
		for(int i = 0; i < pairs.size(); i++) {
			Collider collider1 = pairs.get(i)[0];
			Collider collider2 = pairs.get(i)[1];
						
			Collision[] pairCollisions = null;
			if(collider1.oughtOmitData() && collider2.oughtOmitData()) pairCollisions = collider1.testForCollisions(collider2);
			else pairCollisions = collider1.testForResolutions(collider2);
			
			if(pairCollisions != null) {
				for(Collision collision : pairCollisions) {
					if(collision != null) {
						if(!collider1.filterCollision(collision)) continue;
						if(!collider2.filterCollision(collision)) continue;
						
						collider1.getBody().addCollision(collision);
						collider2.getBody().addCollision(collision);
						collisions.add(collision);
					}
				}
			}
		}
	}
	
	public List<Collider[]> getPairs() {
		return pairs;
	}
	
	public void clearPairs() {
		pairs.clear();
	}
}
