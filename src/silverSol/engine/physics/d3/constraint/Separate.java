package silverSol.engine.physics.d3.constraint;

import org.lwjgl.util.vector.Vector3f;

import silverSol.engine.physics.d3.body.Body;
import silverSol.engine.physics.d3.collider.volume.Volume;
import silverSol.engine.physics.d3.collision.Collision;
import silverSol.math.NumberMath;
import silverSol.math.VectorMath;

public class Separate extends Constraint {

	//SOURCE COLLISION
	private Collision collision;
	
	//RESOLUTION PARAMETERS
	protected float beta;
	protected float slopPenetration;	
	protected float restitutionCoeff;
	protected float slopRestitution;
	
	//PERSISTANCE
	private float normalImpulseSum;
	private float lagrangian;
	private float lagrangianFraction;
	
	public Separate(Collision collision) {
		super();
		this.collision = collision;
		
		this.beta = 0.3f;
		this.slopPenetration = 0.001f;
		this.restitutionCoeff = 0.01f;
		this.slopRestitution = 0.001f;
		this.lagrangianFraction = 1f;
	}
	
	@Override
	//TODO: Resolution needs to be generalized such that it does not inherently include volume-specific data, as resolution with ray-generated collisions needs to be allowed.
	public void resolve(float dt) {
		if(!(collision.getColliderA() instanceof Volume)) return;
		if(!(collision.getColliderB() instanceof Volume)) return;
		
		Volume volumeA = (Volume) collision.getColliderA();
		Volume volumeB = (Volume) collision.getColliderB();
		Vector3f separatingAxis = collision.getSeparatingAxis();
		
		Body bodyA = volumeA.getBody();
		Body bodyB = volumeB.getBody();
		
		Vector3f vA = bodyA.getLinearVelocity();
		Vector3f vB = bodyB.getLinearVelocity();
		Vector3f vAB = Vector3f.sub(vB, vA, null);
		
//		Vector3f wA = bodyA.getAngularVelocity();
//		Vector3f wB = bodyB.getAngularVelocity();
		
		float mA = bodyA.getMass();
		float mB = bodyB.getMass();
		
		float massComponent = bodyA.getInverseMass() + bodyB.getInverseMass();
		
		//TODO: Implement inertia tensor.
		/*
		Vector3f rA = Vector3f.sub(contactA, colliderA.getPosition(), null);
		Vector3f rB = Vector3f.sub(contactB, colliderB.getPosition(), null);
		Vector3f dRotA = Vector3f.cross(rA.negate(null), separatingAxis, null);
		Vector3f dRotB = Vector3f.cross(rB, separatingAxis, null);
		float inertiaComponent = Vector3f.dot(dRotA, colliderA.getInertiaTensor()) + Vector3f.dot(dRotB, colliderB.getInertiaTensor());
		*/
		
		float restitution = -(1f + volumeA.getRestitution() * volumeB.getRestitution());
		float velocityProjection = Vector3f.dot(vAB, separatingAxis);
		float bias = beta / dt * Math.max(collision.getPenetrationDepth() - slopPenetration, 0f);
		
		//TODO: Add inertia component to mass component in the denominator.
		float impulse = (restitution * Math.max(velocityProjection - slopRestitution, 0f) + bias) / massComponent;
		
		if(!collision.isPersistent()) {
			float previousNormalSum = normalImpulseSum;
			normalImpulseSum = NumberMath.clamp(normalImpulseSum + impulse, 0f, Float.POSITIVE_INFINITY);
			lagrangian = normalImpulseSum - previousNormalSum;
		} else {
			lagrangian = lagrangian * lagrangianFraction;
		}
		
		if(VectorMath.hasNaN(vA) || VectorMath.hasNaN(vB)) {
			System.err.println("Resolving NaN velocity: vA = " + vA + "; vB = " + vB);
		}
		
		correctVelocity(separatingAxis, vA, vB, mA, mB);
	}

	private void correctVelocity(Vector3f separatingAxis, Vector3f vA, Vector3f vB, float mA, float mB) {
		Vector3f.sub(vA, VectorMath.mul(separatingAxis, lagrangian / mA, null), vA);
		Vector3f.add(vB, VectorMath.mul(separatingAxis, lagrangian / mB, null), vB);
		/*
		 * Vector3f.add/sub(wA, Vector3f.dot(dRotA, inverseInertiaA), wA);
		 * Vector3f.add/sub(wB, Vector3f.dot(dRotB, inverseInertiaB), wB);
		 */
	}
	
	public float getBeta() {
		return beta;
	}

	public void setBeta(float beta) {
		this.beta = beta;
	}

	public float getSlopPenetration() {
		return slopPenetration;
	}

	public void setSlopPenetration(float slopPenetration) {
		this.slopPenetration = slopPenetration;
	}

	public float getSlopRestitution() {
		return slopRestitution;
	}

	public void setSlopRestitution(float slopRestitution) {
		this.slopRestitution = slopRestitution;
	}

	public float getLagrangianFraction() {
		return lagrangianFraction;
	}

	public void setLagrangianFraction(float lagrangianFraction) {
		this.lagrangianFraction = lagrangianFraction;
	}
	
}
