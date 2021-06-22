  package silverSol.engine.physics.d3.constraint;

import org.lwjgl.util.vector.Vector3f;

import silverSol.engine.physics.d3.collider.volume.Volume;
import silverSol.math.VectorMath;

public class LinearDistance extends Constraint {

	private static final float DEFAULT_K = 1f;
	private static final float DEFAULT_RESTITUTION = 0f;
	private float EPSILON = 1E-3f;
	
	private Volume volumeA;
	private Volume volumeB;
	private Vector3f tetherA;
	private Vector3f tetherB;
	
	private float distance;
	private float k;
	private float restitution;
	
	public LinearDistance(Volume volumeA, Volume volumeB, Vector3f tetherA, Vector3f tetherB) {
		super();
		this.volumeA = volumeA;
		this.volumeB = volumeB;
		this.tetherA = new Vector3f(tetherA);
		this.tetherB = new Vector3f(tetherB);
		this.distance = Vector3f.sub(volumeA.toGlobalPosition(tetherA), volumeB.toGlobalPosition(tetherB), null).length();
		this.k = DEFAULT_K;
		this.restitution = DEFAULT_RESTITUTION;
	}
	
	public LinearDistance(Volume volumeA, Volume volumeB, Vector3f tetherA, Vector3f tetherB, float distance) {
		super();
		this.volumeA = volumeA;
		this.volumeB = volumeB;
		this.tetherA = new Vector3f(tetherA);
		this.tetherB = new Vector3f(tetherB);
		this.distance = distance;
		this.k = DEFAULT_K;
		this.restitution = DEFAULT_RESTITUTION;
	}
	
	public LinearDistance(Volume volumeA, Volume volumeB, Vector3f tetherA, Vector3f tetherB, float distance, float k) {
		super();
		this.volumeA = volumeA;
		this.volumeB = volumeB;
		this.tetherA = new Vector3f(tetherA);
		this.tetherB = new Vector3f(tetherB);
		this.distance = distance;
		this.k = k;
		this.restitution = DEFAULT_RESTITUTION;
	}
	
	public LinearDistance(Volume volumeA, Volume volumeB, Vector3f tetherA, Vector3f tetherB, float distance, float k, float restitution) {
		super();
		this.volumeA = volumeA;
		this.volumeB = volumeB;
		this.tetherA = new Vector3f(tetherA);
		this.tetherB = new Vector3f(tetherB);
		this.distance = distance;
		this.k = k;
		this.restitution = restitution;
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
		
		Vector3f tAB = new Vector3f();
		if(displacement.lengthSquared() < EPSILON) tAB.set(0f, 1f, 0f);
		else displacement.normalise(tAB);
		
		float distance = Vector3f.dot(tAB, displacement);
		
		//Resolution occurs as an intersection between a rope and a spring.
		//The rope component cancels any velocity that brings the objects farther from each other.
		//The spring component pulls the objects back in towards one another.
		float massComponent = volumeA.getBody().getInverseMass() + volumeB.getBody().getInverseMass();
		float restitutionCompoment = 1f + restitution;
		float velocityProjection = Vector3f.dot(vAB, tAB);
		float effectiveVelocity = (
				(velocityProjection > 0 && distance > this.distance) ||
				velocityProjection < 0 && distance < this.distance) ? velocityProjection : 0f;
		
		float ropeImpulse = restitutionCompoment * effectiveVelocity / massComponent;
		float springImpulse = k * (this.distance - distance) * dt;
		float impulse = ropeImpulse + springImpulse;
		
		Vector3f.add(vA, VectorMath.mul(tAB, impulse / volumeA.getBody().getMass(), null), vA);
		Vector3f.sub(vB, VectorMath.mul(tAB, impulse / volumeB.getBody().getMass(), null), vB);
		
		/*
		float deltaV = Vector3f.dot(vAB, tAB);
		float springForce = Math.max(k * (distance - tetherLength), 0f);
		float dP = springForce * dt;
		
		System.out.println("Distance = " + distance + " / " + tetherLength);
		System.out.println("dP = " + dP);
		System.out.print("vA = " + vA + " --> ");
		
		Vector3f.add(vA, VectorMath.mul(tAB, dP / volumeA.getBody().getMass(), null), vA);
		Vector3f.sub(vB, VectorMath.mul(tAB, dP / volumeB.getBody().getMass(), null), vB);
		System.out.println(vA);
		System.out.println();
		*/
		
		/*
		float constraint = Vector3f.dot(vAB, tAB);
		
		float baumgarte = Math.min(-beta * (distance - tetherLength) / dt, 0f);
		float restitution = -restitutionCoeff * constraint; 
		float bias = baumgarte + restitution;
		
		float effectiveMass = iMA + iMB;
		
		float lagrangian = -(constraint + bias) / effectiveMass;
		System.out.println("Resolving LinearTether");
		System.out.println("\tdt = " + dt);
		System.out.println("\tdistance = " + distance);
		System.out.println("\tvAB = " + vAB);
		System.out.println("\tEffective Mass = " + effectiveMass);
		System.out.println("\tlagrangian = " + lagrangian);
		System.out.println("\tvA correction = " + VectorMath.mul(tAB, lagrangian * iMA, null));
		System.out.println("\tvB correction = " + VectorMath.mul(tAB, lagrangian * iMB, null));
		System.out.println();
		
		Vector3f.sub(vA, VectorMath.mul(tAB, lagrangian, null), vA);
		Vector3f.add(vB, VectorMath.mul(tAB, lagrangian, null), vB);
		*/
	}

	public float getDistance() {
		return distance;
	}

	public void setDistance(float distance) {
		this.distance = distance;
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
	
}
