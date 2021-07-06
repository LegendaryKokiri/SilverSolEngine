package silverSol.engine.physics.d3.collision;

import org.lwjgl.util.vector.Vector3f;

import silverSol.engine.physics.d3.collider.Collider;
import silverSol.math.VectorMath;

public class Collision {
	
	//ACTIVATION
	private boolean active;
	
	//BODIES
	private Collider colliderA;
	private Collider colliderB;
	private Vector3f localContactA;
	private Vector3f localContactB;
	private Vector3f globalContactA;
	private Vector3f globalContactB;
	
	//PENETRATION
	private float penetrationDepth;
	private Vector3f separatingAxis;
	private Vector3f tangent1;
	private Vector3f tangent2;
	
	//PROCESSING
	private boolean constraintsGenerated;
	
	//PERSISTENCE
	private boolean persistent;
	
	public Collision() {
		active = true;
		
		localContactA = new Vector3f();
		localContactB = new Vector3f();
		globalContactA = new Vector3f();
		globalContactB = new Vector3f();
		
		penetrationDepth = 0;
		separatingAxis = new Vector3f();
		tangent1 = new Vector3f();
		tangent2 = new Vector3f();
	}
	
	public boolean isActive() {
		return active;
	}
	
	public void setActive(boolean active) {
		this.active = active;
	}
		
	public Collider getColliderA() {
		return colliderA;
	}

	public void setColliderA(Collider colliderA) {
		this.colliderA = colliderA;
	}

	public Collider getColliderB() {
		return colliderB;
	}

	public void setColliderB(Collider colliderB) {
		this.colliderB = colliderB;
	}
	
	public boolean involves(Collider collider) {
		return colliderA == collider || colliderB == collider;
	}
	
	public Collider getOtherCollider(Collider collider) {
		if(colliderA == collider) return colliderB;
		if(colliderB == collider) return colliderA;
		return null;
	}
	
	public Vector3f getLocalContact(Collider collider) {
		if(colliderA == collider) return localContactA;
		if(colliderB == collider) return localContactB;
		return null;
	}
	
	public Vector3f getGlobalContact(Collider collider) {
		if(colliderA == collider) return globalContactA;
		if(colliderB == collider) return globalContactB;
		return null;
	}
	
	public Vector3f getLocalContactA() {
		return localContactA;
	}
	
	public Vector3f getGlobalContactA() {
		return globalContactA;
	}

	public void setContactA(Vector3f local, Vector3f global) {
		this.localContactA.set(local);
		this.globalContactA.set(global);
	}
	
	public Vector3f getLocalContactB() {
		return localContactB;
	}

	public Vector3f getGlobalContactB() {
		return globalContactB;
	}

	public void setContactB(Vector3f local, Vector3f global) {
		this.localContactB.set(local);
		this.globalContactB.set(global);
	}

	public float getPenetrationDepth() {
		return penetrationDepth;
	}

	public void setPenetrationDepth(float penetrationDepth) {
		this.penetrationDepth = penetrationDepth;
	}
	
	public Vector3f getSeparatingAxis() {
		return separatingAxis;
	}

	/**
	 * Returns the separating axis, oriented such that it points towards the inputted collider.
	 * @param collider The collider into which the separating axis is to be pointing.
	 * @return The separating axis oriented such that it points towards the inputted collider.
	 */
	public Vector3f getSeparatingAxis(Collider collider) {
		if(colliderA == collider) return separatingAxis.negate(null);
		return new Vector3f(separatingAxis);
	}
	
	public void setSeparatingAxis(Vector3f separatingAxis) {
		this.separatingAxis.set(separatingAxis);
		calculateTangents();
	}
	
	private void calculateTangents() {
		VectorMath.generateBasis(separatingAxis, tangent1, tangent2);
	}
	
	public Vector3f getTangent1() {
		return tangent1;
	}

	public Vector3f getTangent2() {
		return tangent2;
	}
	
	public boolean isPersistent() {
		return persistent;
	}
	
	public boolean getConstraintsGenerated() {
		return constraintsGenerated;
	}

	public void setConstraintsGenerated(boolean constraintsGenerated) {
		this.constraintsGenerated = constraintsGenerated;
	}

	@Override
	public String toString() {
		return "Collision between " + colliderA + " and " + colliderB;
	}
}

