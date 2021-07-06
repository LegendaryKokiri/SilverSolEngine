package silverSol.engine.physics.d3;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Vector3f;

import silverSol.engine.entity.Entity;
import silverSol.engine.physics.d3.body.Body;
import silverSol.engine.physics.d3.collider.Collider;
import silverSol.engine.physics.d3.collider.ray.Ray;
import silverSol.engine.physics.d3.collider.volume.Volume;
import silverSol.engine.physics.d3.collision.Collision;
import silverSol.engine.physics.d3.constraint.Constraint;
import silverSol.engine.physics.d3.det.CollisionDetection;
import silverSol.engine.physics.d3.res.CollisionResolution;
import silverSol.engine.settings.PhysicsSettings;

/**
 * PhysicsEngine3d handles movement and collision of 3d bodies in a game.
 * @author Julian
 *
 */
public class PhysicsEngine3d {
	
	//Time Steps
	private int fps;
	private float targetDT;
	private int numIterations;
	
	//Distance
	private float unitsPerMeter;
	
	//Gravity
	private Vector3f defaultGravity;
	
	//Bodies
	private List<Body> bodies;
	private List<Collider> colliders;
	
	//Collision
	private CollisionDetection collisionDetection;
	private CollisionResolution collisionResolution;
	
	//Constraints
	private List<Constraint> constraints;
		
	public PhysicsEngine3d(PhysicsSettings physicsSettings) {
		this.fps = physicsSettings.getFPS();
		targetDT = 1f / (float) fps;
		numIterations = 1;
				
		unitsPerMeter = 1f;
		
		defaultGravity = new Vector3f(0f, 9.8f, 0f);
		
		bodies = new ArrayList<>();
		colliders = new ArrayList<>();
		
		collisionDetection = new CollisionDetection();
		collisionResolution = new CollisionResolution();
		constraints = new ArrayList<>();
	}
	
	/**
	 * Updates the motion and collisions of all bodies stored in the physics engine.
	 * @param dt Time step
	 */
	public void update() {
		clean();
		preSolve();
		generateConstraints();
		solveConstraints();
		postSolve();
	}
	
	public void addEntities(Entity... entities) {
		for(Entity entity : entities) {
			addEntity(entity);
		}
	}
	
	public void addEntities(List<? extends Entity> entities) {
		for(Entity entity : entities) {
			addEntity(entity);
		}
	}
	
	public void addEntity(Entity entity) {
		if(entity.hasBody3d()) {
			Body body = entity.getBody3d();
			bodies.add(body);
			
			if(!body.isGravitySet()) body.setGravity(defaultGravity);
			
			for(Volume volume : body.getVolumes()) {
				processCollider(volume);
			}
			
			for(Ray ray : body.getRays()) {
				processCollider(ray);
			}
		}
	}
	
	public void removeEntity(Entity entity) {
		if(entity.hasBody3d()) {
			removeBody(entity.getBody3d());
		}
	}
	
	public void addCollider(Collider collider) {
		processCollider(collider);
		colliders.add(collider);
	}
	
	private void processCollider(Collider collider) {
		if(collider == null) return;
		if(collider.getID() == Volume.DEFAULT_ID) collisionDetection.addCollider(collider);
	}
	
	private void removeBody(Body body) {
		for(Volume volume : body.getVolumes()) {
			removeCollider(volume);
		}
		
		for(Ray ray : body.getRays()) {
			removeCollider(ray);
		}
		
		bodies.remove(body);
	}
	
	public void removeCollider(Collider collider) {
		collisionDetection.removeCollider(collider);
		colliders.remove(collider);
	}
	
	private void clean() {
		for(int i = 0; i < bodies.size(); i++) {
			if(bodies.get(i).shouldRemove()) {
				removeBody(bodies.get(i));
				i--;
			}
		}
		
		for(int i = 0; i < constraints.size(); i++) {
			if(constraints.get(i).isResolved()) {
				constraints.remove(i);
				i--;
			}
		}
	}
	
	private void preSolve() {
		for(Body body : bodies) {
			if(!body.isImmovable() && !body.isGravityImmune()) body.addLinearAcceleration(body.getGravity());
			body.preResolution(targetDT);
		}
		
		for(Body body : bodies) {
			body.clearCollisions();
		}
		
		for(Collider collider : colliders) {
			collider.clearCollisions();
		}
	}
	
	private void generateConstraints() {
		List<Collision> collisions = collisionDetection.run(bodies);
		List<Constraint> constraints = collisionResolution.run(collisions);
		this.constraints.addAll(constraints);
	}

	private void solveConstraints() {
		for(int i = 0; i < numIterations; i++) {
			for(Constraint c : constraints) {
				c.resolve(targetDT);
			}
		}
	}
	
	private void postSolve() {
		for(Body body : bodies) {
			body.postResolution(targetDT);
		}
	}
	
	public void addConstraint(Constraint constraint) {
		this.constraints.add(constraint);
	}
	
	public void addConstraints(List<Constraint> constraints) {
		this.constraints.addAll(constraints);
	}
	
	//TODO: Rather than having to clear constraints as a consequence of clearing bodies, I'd rather the engine independently detect the bodies involved in the constraints are missing and remove them.
	public void clearBodies() {
		collisionDetection.clearData();
		bodies.clear();
		colliders.clear();
		constraints.clear();
	}
	
	public float getUnitsPerMeter() {
		return unitsPerMeter;
	}
	
	public void setUnitsPerMeter(float unitsPerMeter) {
		this.unitsPerMeter = unitsPerMeter;
	}
	
	/**
	 * Get the default gravitational acceleration in the universe.
	 * @return The default gravitational acceleration of the universe.
	 */
	public Vector3f getDefaultGravity() {
		return defaultGravity;
	}

	/**
	 * Set the default gravitational acceleration of the universe.
	 * @param gravity The default gravitational acceleration of the universe.
	 */
	public void setGravity(Vector3f defaultGravity) {
		this.defaultGravity.set(defaultGravity);
	}
	
	public float getFPS() {
		return fps;
	}
	
	public void setFPS(int fps) {
		this.fps = fps;
		this.targetDT = 1f / (float) fps;
	}
	
	public float getTargetDT() {
		return targetDT;
	}
	
	public int getNumIterations() {
		return numIterations;
	}
	
	public void setNumIterations(int numIterations) {
		this.numIterations = numIterations;
	}
}
