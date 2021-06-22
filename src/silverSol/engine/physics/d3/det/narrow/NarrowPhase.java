package silverSol.engine.physics.d3.det.narrow;

import java.util.ArrayList;
import java.util.List;

import silverSol.engine.physics.d3.collision.Collision;

public class NarrowPhase {
	
	private PairManager pm;
	private List<Collision> collisions;
	
	public NarrowPhase() {
		pm = new PairManager();
		collisions = new ArrayList<>();
	}
	
	public void run() {
		pm.updatePairs(collisions);
	}
	
	public PairManager getPairManager() {
		return pm;
	}
	
	public List<Collision> getCollisions() {
		return collisions;
	}
	
	public void clearData() {
		pm.clearPairs();
		collisions.clear();
	}
}
