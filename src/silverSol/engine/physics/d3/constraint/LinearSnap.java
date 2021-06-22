package silverSol.engine.physics.d3.constraint;

import org.lwjgl.util.vector.Vector3f;

import silverSol.engine.physics.d3.body.Body;
import silverSol.engine.physics.d3.collider.volume.Volume;
import silverSol.engine.physics.d3.collision.Collision;
import silverSol.math.NumberMath;
import silverSol.math.VectorMath;

public class LinearSnap extends Constraint {

	//SOURCE COLLISION
	private Collision collision;
	
	//RESOLUTION PARAMETERS
	private float beta;
	private float slopPenetration;
	private float lagrangianFraction;
	
	private float normalImpulseSum;
	private float lagrangian;
	
	public LinearSnap(Collision collision) {
		super();
		
		this.collision = collision;
		
		beta = 0.3f;
		slopPenetration = 0.001f;
		lagrangianFraction = 1f;
	}
	
	public LinearSnap(Collision collision, 
			float beta, float slopPenetration, float slopRestitution, float lagrangianFraction) {
		super();
		
		this.collision = collision;
		
		this.beta = beta;
		this.slopPenetration = slopPenetration;
		this.lagrangianFraction = lagrangianFraction;
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
		
		float massComponent = separatingAxis.lengthSquared() * ((1f / mA) + (1f / mB));
		
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
		float impulse = (restitution * velocityProjection + bias) / massComponent;
		
		if(!collision.isPersistent()) {
			float previousNormalSum = normalImpulseSum;
			normalImpulseSum = NumberMath.clamp(normalImpulseSum + impulse, 0f, Float.POSITIVE_INFINITY);
			lagrangian = normalImpulseSum - previousNormalSum;
		} else {
//			Display.setTitle("Persistent");
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
	
}
