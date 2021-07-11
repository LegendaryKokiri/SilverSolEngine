package silverSol.engine.physics.d3.det.narrow;

import java.util.ArrayList;
import java.util.List;

import silverSol.engine.physics.d3.collider.Collider;

public class PairManager {
	
	private List<Collider[]> pairs;
	
	public PairManager() {
		pairs = new ArrayList<>();
	}
	
	public void addPair(Collider c1, Collider c2) {
		pairs.add(new Collider[]{c1, c2});
	}
	
	public List<Collider[]> getPairs() {
		return pairs;
	}
	
	public void clearPairs() {
		pairs.clear();
	}
}
