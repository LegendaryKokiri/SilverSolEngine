package silverSol.engine.physics.d3.det.broad;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import silverSol.engine.physics.d3.collider.Collider;

public class BroadPhase {
	//Greatest Entity ID given thus far
	private int greatestID;
	private Queue<Integer> emptiedIDs;

	private Map<Integer, Collider> loadedColliders; //Give an ID to get a collider
	
	private List<Endpoint> xEndpoints;
	private List<Endpoint> yEndpoints;
	private List<Endpoint> zEndpoints;
		
	public BroadPhase() {
		greatestID = -1;
		emptiedIDs = new LinkedList<>();
		
		loadedColliders = new HashMap<>();
		
		xEndpoints = new ArrayList<>();
		yEndpoints = new ArrayList<>();
		zEndpoints = new ArrayList<>();
	}
	
	public List<Collider[]> run() {
		List<Collider[]> pairs = new ArrayList<>();
		
		recalculateEndpoints();
		reorderEndpoints();
		addOverlappingPairs(pairs);
		
		return pairs;
	}
	
	private void recalculateEndpoints() {
		for(int colliderKey : loadedColliders.keySet()) {
			loadedColliders.get(colliderKey).calculateEndpoints();
		}
	}
	
	private void reorderEndpoints() {
		Collections.sort(xEndpoints);
		Collections.sort(yEndpoints);
		Collections.sort(zEndpoints);
		
		for(int i = 0; i < xEndpoints.size(); i++) {
			xEndpoints.get(i).index = i;
			yEndpoints.get(i).index = i;
			zEndpoints.get(i).index = i;
		}
	}
	
	private void addOverlappingPairs(List<Collider[]> pairs) {
		int length = greatestID + 1;
		boolean[][][] overlapTracker = new boolean[length][length][3];
		
		for(int colliderKey : loadedColliders.keySet()) {
			Collider collider = loadedColliders.get(colliderKey);
			
			Endpoint[] endpoints = collider.getEndpoints();
			int id = collider.getID();
			recordOverlaps(overlapTracker, xEndpoints, endpoints[0].index, endpoints[1].index, id, 0);
			recordOverlaps(overlapTracker, yEndpoints, endpoints[2].index, endpoints[3].index, id, 1);
			recordOverlaps(overlapTracker, zEndpoints, endpoints[4].index, endpoints[5].index, id, 2);
		}
		
		for(int i: loadedColliders.keySet()) {
			for(int j : loadedColliders.keySet()) {
				Collider c1 = loadedColliders.get(i);
				Collider c2 = loadedColliders.get(j);
				if(!c1.canCollideWith(c2) || !c2.canCollideWith(c1)) continue;
				if(overlap(overlapTracker, i, j)) {
					pairs.add(new Collider[]{c1, c2});
				}
			}
		}
	}
	
	private void recordOverlaps(boolean[][][] overlapTracker, List<Endpoint> endpoints, int minIndex, int maxIndex, int id, int axis) {
		for(int i = minIndex + 1; i < maxIndex; i++) {
			overlapTracker[id][endpoints.get(i).id][axis] = true;
			overlapTracker[endpoints.get(i).id][id][axis] = true;
		}
	}
	
	private boolean overlap(boolean[][][] overlapTracker, int id1, int id2) {
		boolean[] axisOverlaps = overlapTracker[id1][id2];
		return axisOverlaps[0] && axisOverlaps[1] && axisOverlaps[2];
	}
	
	public void addCollider(Collider collider) {
		setVolumeID(collider);
				
		loadedColliders.put(collider.getID(), collider);
		
		Endpoint[] endpoints = collider.getEndpoints();	
		
		addEndpoints(endpoints[0], endpoints[1], xEndpoints);
		addEndpoints(endpoints[2], endpoints[3], yEndpoints);
		addEndpoints(endpoints[4], endpoints[5], zEndpoints);
	}
	
	private void setVolumeID(Collider collider) {
		if(emptiedIDs.isEmpty()) collider.setID(++greatestID);
		else collider.setID(emptiedIDs.poll());
	}
	
	private void addEndpoints(Endpoint minEndpoint, Endpoint maxEndpoint, List<Endpoint> endpoints) {
		//Minimum is added before maximum on purpose to ensure that the minimum value never ends up after the maximum value in an endpoint list
		endpoints.add(minEndpoint);
		minEndpoint.index = endpoints.size() - 1;
		endpoints.add(maxEndpoint);
		maxEndpoint.index = endpoints.size() - 1;
	}
	
	public void removeCollider(Collider collider) {
		int ID = collider.getID();
		emptiedIDs.add(ID);
				
		Endpoint[] endpoints = collider.getEndpoints();
		
		removeEndpoints(endpoints[0], endpoints[1], xEndpoints);
		removeEndpoints(endpoints[2], endpoints[3], yEndpoints);
		removeEndpoints(endpoints[4], endpoints[5], zEndpoints);
		
		loadedColliders.remove(ID);
	}
	
	private void removeEndpoints(Endpoint minEndpoint, Endpoint maxEndpoint, List<Endpoint> endpoints) {
		//Maximum is removed first in order to avoid altering the index of the minimum endpoint before removing
		endpoints.remove(maxEndpoint);
		endpoints.remove(minEndpoint);
	}
	
	public void clearData() {
		greatestID = -1;
		emptiedIDs.clear();
		loadedColliders.clear();
		xEndpoints.clear();
		yEndpoints.clear();
		zEndpoints.clear();
	}
}
