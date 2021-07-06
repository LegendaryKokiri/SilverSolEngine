package silverSol.engine.physics.d3.det;

import java.util.ArrayList;
import java.util.List;

import silverSol.engine.physics.d3.body.Body;
import silverSol.engine.physics.d3.collider.Collider;
import silverSol.engine.physics.d3.collider.ray.Ray;
import silverSol.engine.physics.d3.collider.volume.Volume;
import silverSol.engine.physics.d3.collision.Collision;
import silverSol.engine.physics.d3.det.broad.BroadPhase;
import silverSol.engine.physics.d3.det.narrow.NarrowPhase;

public class CollisionDetection {
	
	private BroadPhase broadPhase;
	private NarrowPhase narrowPhase;
	private List<Collision> collisions;
	
	public CollisionDetection() {
		broadPhase = new BroadPhase();
		narrowPhase = new NarrowPhase();
		collisions = new ArrayList<>();
	}
	
	public void addCollider(Collider collider) {
		broadPhase.addCollider(collider);
	}
	
	public void removeCollider(Collider collider) {
		broadPhase.removeCollider(collider);
	}
	
	public List<Collision> run(List<Body> bodies) {		
		broadPhase.run(bodies, narrowPhase.getPairManager());
		narrowPhase.run();
		storeCollisions(bodies);
		narrowPhase.getPairManager().clearPairs();
		
		return collisions;
	}
	
	private void storeCollisions(List<Body> bodies) {
		collisions.clear();
		for(Body body : bodies) {
			for(Volume volume : body.getVolumes()) collisions.addAll(volume.getCollisions());
			for(Ray ray : body.getRays()) collisions.addAll(ray.getCollisions());
		}
	}
	
	public void clearData() {
		broadPhase.clearData();
		narrowPhase.clearData();
	}
}
