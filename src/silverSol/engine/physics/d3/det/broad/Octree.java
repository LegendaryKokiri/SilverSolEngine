package silverSol.engine.physics.d3.det.broad;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Vector3f;

import silverSol.engine.physics.d3.collider.Collider;
import silverSol.engine.physics.d3.collider.volume.AABB;
import silverSol.engine.physics.d3.collider.volume.Volume;
import silverSol.engine.physics.d3.collider.volume.Volume.Type;
import silverSol.engine.physics.d3.det.narrow.algs.GJK;
import silverSol.math.VectorMath;

public class Octree {
	
	//TREE
	private Octree parent;
	private Octree[] children;
	private boolean hasChildren;
	private int capacity;
	private int maxDepth;
	
	//AABB
	private Vector3f min;
	private Vector3f max;
	private AABB aabb;
	
	//COLLIDERS
	private List<Collider> colliders;
	
	public Octree(Vector3f min, Vector3f max, int capacity, int maxDepth) {
		this.parent = null;
		this.children = new Octree[8];
		this.hasChildren = false;
		this.capacity = capacity;
		
		this.min = new Vector3f(min);
		this.max = new Vector3f(max);
		Vector3f center = VectorMath.interpolate(min, max, 0.5f);
		this.aabb = new AABB(Vector3f.sub(center, min, null), Type.ETHEREAL, null);
		
		this.colliders = new ArrayList<>();
	}
	
	public void partition() {
		Vector3f half = VectorMath.interpolate(min, max, 0.5f);
		int depth = maxDepth - 1;
		
		children[0] = new Octree(new Vector3f(min.x, min.y, min.z), new Vector3f(half.x, half.y, half.z), capacity, depth);
		children[1] = new Octree(new Vector3f(half.x, min.y, min.z), new Vector3f(max.x, half.y, half.z), capacity, depth);
		children[2] = new Octree(new Vector3f(min.x, half.y, min.z), new Vector3f(half.x, max.y, half.z), capacity, depth);
		children[3] = new Octree(new Vector3f(min.x, min.y, half.z), new Vector3f(half.x, half.y, max.z), capacity, depth);
		children[4] = new Octree(new Vector3f(half.x, half.y, min.z), new Vector3f(max.x, max.y, half.z), capacity, depth);
		children[5] = new Octree(new Vector3f(half.x, min.y, half.z), new Vector3f(max.x, half.y, max.z), capacity, depth);
		children[6] = new Octree(new Vector3f(min.x, half.y, half.z), new Vector3f(half.x, max.y, max.z), capacity, depth);
		children[7] = new Octree(new Vector3f(half.x, half.y, half.z), new Vector3f(max.x, max.y, max.z), capacity, depth);
		this.hasChildren = true;
	}
	
	public void store(Collider collider) {
		if(!this.contains(collider)) return;
		
		if(colliders.size() >= capacity && maxDepth > 0) {
			partition();
			
			for(int i = 0; i < colliders.size(); ) {
				store(colliders.get(0));
				colliders.remove(0);
			}
		}
		
		if(hasChildren) {
			for(int i = 0; i < 8; i++) {
				if(children[i].contains(collider)) children[i].store(collider);
			}
		} else {
			this.colliders.add(collider);
		}
	}
	
	public void store(Collider[] colliders) {
		for(Collider collider : colliders) store(collider);
	}
	
	private boolean contains(Collider collider) {
		if(collider instanceof Volume) return GJK.detect(aabb, (Volume) collider) != null;
		return false; //TODO: Check for a ray intersection as well
	}
	
	public void setParent(Octree parent) {
		this.parent = parent;
	}

}
