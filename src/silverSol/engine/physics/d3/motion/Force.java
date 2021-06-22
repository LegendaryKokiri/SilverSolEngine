package silverSol.engine.physics.d3.motion;

import org.lwjgl.util.vector.Vector3f;

public class Force {
	
	private Vector3f force;
	private Vector3f appPoint;
	
	public Force(Vector3f force) {
		this.force = new Vector3f(force);
		this.appPoint = new Vector3f();
	}
	
	public Force(Vector3f force, Vector3f applicationPoint) {
		this.force = new Vector3f(force);
		this.appPoint = new Vector3f(applicationPoint);
	}
	
	public Vector3f getForce() {
		return force;
	}
	
	public Vector3f getAppPoint() {
		return appPoint;
	}

}
