  package silverSol.engine.physics.d3.constraint;

import org.lwjgl.util.vector.Vector3f;

import silverSol.engine.physics.d3.collider.volume.Volume;
import silverSol.math.VectorMath;

public class LinearAlign extends Constraint {

	private static final float DEFAULT_K = 1f;
	private static final float DEFAULT_RESTITUTION = 0f;
	
	private Volume volumeA;
	private Volume volumeB;
	private Vector3f tetherA;
	private Vector3f tetherB;
	private Vector3f planeNormal;
	
	private float tolerance; //TODO: Actually use this
	private float k;
	private float restitution;
	private float restitutionSlop;
	
	public LinearAlign(Volume volumeA, Volume volumeB, Vector3f tetherA, Vector3f tetherB, Vector3f planeNormal) {
		super();
		this.volumeA = volumeA;
		this.volumeB = volumeB;
		this.tetherA = new Vector3f(tetherA);
		this.tetherB = new Vector3f(tetherB);
		this.planeNormal = new Vector3f(planeNormal);
		this.k = DEFAULT_K;
		this.restitution = DEFAULT_RESTITUTION;
	}
	
	@Override
	//TODO: Resolution needs to be generalized such that it does not inherently include volume-specific data, as resolution with ray-generated collisions needs to be allowed.
	public void resolve(float dt) {
		Vector3f vA = volumeA.getBody().getLinearVelocity();
		Vector3f vB = volumeB.getBody().getLinearVelocity();
		Vector3f vAB = Vector3f.sub(vB, vA, null);
		
		Vector3f tA = volumeA.toGlobalPosition(tetherA);
		Vector3f tB = volumeB.toGlobalPosition(tetherB);
		Vector3f displacement = Vector3f.sub(tB, tA, null);
		float displacementProjection = Vector3f.dot(planeNormal, displacement);
		
		//Resolution occurs as an intersection between a rope and a spring.
		//The rope component cancels any velocity that brings the objects farther from each other.
		//The spring component pulls the objects back in towards one another.
		float massComponent = volumeA.getBody().getInverseMass() + volumeB.getBody().getInverseMass();
		float restitutionCompoment = 1f + restitution;
		float velocityProjection = Vector3f.dot(vAB, planeNormal);
		float effectiveVelocity = (
				(velocityProjection > 0 && displacementProjection > 0) ||
				velocityProjection < 0 && displacementProjection < 0) ? velocityProjection : 0f;
		
		float ropeImpulse = restitutionCompoment * effectiveVelocity / massComponent;
		float springImpulse = k * displacementProjection * dt;
		float impulse = ropeImpulse + springImpulse;
		
		Vector3f.add(vA, VectorMath.mul(planeNormal, impulse / volumeA.getBody().getMass(), null), vA);
		Vector3f.sub(vB, VectorMath.mul(planeNormal, impulse / volumeB.getBody().getMass(), null), vB);
	}

	public Vector3f getPlaneNormal() {
		return planeNormal;
	}
	
	public void setPlaneNormal(float x, float y, float z) {
		this.planeNormal.set(x, y, z);
	}
	
	public void setPlaneNormal(Vector3f planeNormal) {
		this.planeNormal.set(planeNormal);
	}
	
	public float getTolerance() {
		return tolerance;
	}

	public void setTolerance(float tolerance) {
		this.tolerance = tolerance;
	}

	public float getSpringConstant() {
		return k;
	}

	public void setSpringConstant(float k) {
		this.k = k;
	}

	public float getRestitution() {
		return restitution;
	}

	public void setRestitution(float restitution) {
		this.restitution = restitution;
	}

	public float getRestitutionSlop() {
		return restitutionSlop;
	}

	public void setRestitutionSlop(float restitutionSlop) {
		this.restitutionSlop = restitutionSlop;
	}
	
}
