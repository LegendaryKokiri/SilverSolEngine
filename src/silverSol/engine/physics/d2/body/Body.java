package silverSol.engine.physics.d2.body;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import silverSol.engine.physics.d2.collider.Collider;
import silverSol.engine.physics.d3.collision.Collision;
import silverSol.math.VectorMath;

public class Body {
	
	//Physics Engine Identification Data
	private int physicsEngineIndex;
	
	//Hierarchy Data
	private boolean hasParent;
		private Body parent;
	private List<Body> children;
	
	//Transformation Data
	private Vector2f position, radius, scale;
	private float rotation;
	private Vector2f positionOffset;
	private float rotationOffset; //Offset from parent
		
	//Bounding Volumes
	private boolean hasCollider;
		private Collider collider;
	
	//Collision Data
	protected boolean immovable;
	protected boolean sensor;
	
	//Linear Motion
	private Vector2f linearVelocity;
	private Vector2f linearDisplacement; //The actual change in position the entity will undergo this frame
	private Vector2f linearAcceleration;
	private Vector2f projectedPosition; //The position the object will have at the end of the frame assuming no collisions
	
	private float maximumLinearVelocityLength;
	private float maximumLinearVelocityLengthSquared;
	
	//Angular Motion
	private float angularVelocity;
	private float angularDisplacement;
	private float angularAcceleration;
	private float projectedRotation;  //The rotation the object will have at the end of the frame assuming no collisions
	
	//Mass
	private float mass; //in kilograms
	private float inverseMass;
	
	//Moment of Inertia
	private float momentOfInertia;
	
	private List<Collision> collisions;
	
	public Body() {		
		this.physicsEngineIndex = -1;
		
		this.hasParent = false;
		this.children = new ArrayList<>();
		
		this.position = new Vector2f(0, 0);
		this.rotation = 0;
		this.radius = new Vector2f(1, 1);
		this.scale = new Vector2f(1, 1);
		
		this.positionOffset = new Vector2f(0, 0);
		this.rotationOffset = 0;
		
		this.hasCollider = false;
		
		this.immovable = true;
		
		this.linearDisplacement = new Vector2f(0, 0);
		this.linearVelocity = new Vector2f(0, 0);
		this.linearAcceleration = new Vector2f(0, 0);
		this.projectedPosition = new Vector2f(0, 0);
		
		this.maximumLinearVelocityLength = Float.POSITIVE_INFINITY;
		this.maximumLinearVelocityLengthSquared = Float.POSITIVE_INFINITY;
		
		this.angularDisplacement = 0;
		this.angularVelocity = 0;
		this.angularAcceleration = 0;
		this.projectedRotation = 0;
		
		this.mass = this.inverseMass = 1f;
		
		this.momentOfInertia = 1;
		
		this.collisions = new ArrayList<>();
	}
	
	public void preResolution(float dt) {
		Vector2f.add(linearVelocity, VectorMath.mul(linearAcceleration, dt, null), linearVelocity);
		
		if(linearVelocity.lengthSquared() > maximumLinearVelocityLengthSquared) {
			VectorMath.setLength(linearVelocity, maximumLinearVelocityLength, linearVelocity);
		}
		
		VectorMath.mul(linearVelocity, dt, linearDisplacement);
		Vector2f.add(position, linearDisplacement, projectedPosition);
		
		angularVelocity += angularAcceleration * dt;
		angularDisplacement = angularVelocity * dt;
		projectedRotation = rotation + angularDisplacement;		
	}
	
	public void collisionReaction(Body body) {
		
	}
	
	public void postResolution(float dt) {
		VectorMath.mul(linearVelocity, dt, linearDisplacement);
		angularDisplacement = angularVelocity * dt;
		
		setPosition(Vector2f.add(position, linearDisplacement, null));
		rotate(angularDisplacement);				
	}
	
	public int getPhysicsEngineIndex() {
		return physicsEngineIndex;
	}

	public void setPhysicsEngineIndex(int physicsEngineIndex) {
		this.physicsEngineIndex = physicsEngineIndex;
	}

	public Body getParent() {
		return parent;
	}

	public void setParent(Body parent) {
		if(parent == null) return;
		this.hasParent = true;
		this.parent = parent;
	}

	public boolean hasParent() {
		return hasParent;
	}

	public boolean hasChildren() {
		return children.size() > 0;
	}
	
	public List<Body> getChildren() {
		return children;
	}
	
	public void addChild(Body childBody) {
		if(childBody == null) return;
		childBody.setParent(this);
		children.add(childBody);
	}

	public void setChildren(List<Body> children) {
		this.children = children;
	}

	public void setTransformation(Vector2f position, float rotation, Vector2f scale) {
		setPosition(position);
		setRotation(rotation);
		setScale(scale);
	}
	
	public Vector2f getPosition() {
		return position;
	}
	
	public void setPosition(float x, float y) {
		this.position.set(x, y);
		if(hasCollider) collider.setPosition(position);
	}
	
	public void setPosition(Vector2f position) {
		this.position.set(position);
		if(hasCollider) collider.setPosition(position);
	}
	
	public void translate(Vector2f translation) {
		Vector2f.add(position, translation, position);
		if(hasCollider) collider.setPosition(position);
	}
	
	public Vector2f getPositionOffset() {
		return positionOffset;
	}

	public void setPositionOffset(Vector2f positionOffset) {
		this.positionOffset = positionOffset;
	}
	
	public Vector2f getLinearAcceleration() {
		return linearAcceleration;
	}

	public void setLinearAcceleration(Vector2f linearAcceleration) {
		this.linearAcceleration = linearAcceleration;
	}

	public Vector2f getLinearVelocity() {
		return linearVelocity;
	}

	public void setLinearVelocity(Vector2f linearVelocity) {
		this.linearVelocity.set(linearVelocity);
	}

	public Vector2f getLinearDisplacement() {
		return linearDisplacement;
	}

	public float getMaximumLinearVelocityLength() {
		return maximumLinearVelocityLength;
	}

	public void setMaximumLinearVelocityLength(float maximumLinearVelocityLength) {
		this.maximumLinearVelocityLength = maximumLinearVelocityLength;
		this.maximumLinearVelocityLengthSquared = maximumLinearVelocityLength * maximumLinearVelocityLength;
	}

	public void setLinearDisplacement(Vector2f linearDisplacement) {
		this.linearDisplacement = linearDisplacement;
	}
	
	public Vector2f getProjectedPosition() {
		return projectedPosition;
	}

	public float getRotation() {
		return rotation;
	}

	public void setRotation(float rotation) {
		this.rotation = rotation;
	}
	
	public void rotate(float rotation) {
		this.rotationOffset += rotation;
	}

	public float getRotationOffset() {
		return rotationOffset;
	}

	public void setRotationOffset(float rotationOffset) {
		this.rotationOffset = rotationOffset;
	}

	public float getAngularVelocity() {
		return angularVelocity;
	}

	public void setAngularVelocity(float angularVelocity) {
		this.angularVelocity = angularVelocity;
	}

	public float getAngularDisplacement() {
		return angularDisplacement;
	}

	public void setAngularDisplacement(float angularDisplacement) {
		this.angularDisplacement = angularDisplacement;
	}

	public float getAngularAcceleration() {
		return angularAcceleration;
	}

	public void setAngularAcceleration(float angularAcceleration) {
		this.angularAcceleration = angularAcceleration;
	}
	
	public float getProjectedRotation() {
		return projectedRotation;
	}

	public Vector2f getRadius() {
		return radius;
	}

	public void setRadius(Vector2f radius) {
		this.radius = radius;
	}
	
	public Vector2f getScale() {
		return scale;
	}
	
	//TODO: Switch this so that Vector2f calls this one.
	public void setScale(float x, float y) {
		setScale(new Vector2f(x, y));
	}
	
	public void setScale(Vector2f scale) {
		Vector2f scaleFactor = VectorMath.div(scale, this.scale, null);
		VectorMath.mul(radius, scaleFactor, radius);
		this.scale = scale;
	}

	public float getMass() {
		return mass;
	}

	public void setMass(float mass) {
		this.mass = mass;
		this.inverseMass = 1f / mass;
	}

	public float getInverseMass() {
		return inverseMass;
	}

	public float getMomentOfInertia() {
		return momentOfInertia;
	}
	
	public boolean isImmovable() {
		return immovable;
	}

	public void setImmovable(boolean immovable) {
		this.immovable = immovable;
	}

	public boolean hasCollider() {
		return hasCollider;
	}

	public Collider getCollider() {
		return collider;
	}

	public void setCollider(Collider collider) {
		this.collider = collider;
		this.hasCollider = this.collider != null;
		if(this.hasCollider) this.collider.setBody(this);
	}
	
	public List<Collision> getCollisions() {
		return collisions;
	}
	
	public void clearCollisions() {
		collisions.clear();
	}
	
	@Override
	public String toString() {
		return "Body: Position = " + position + ", Rotation = " + rotation + ", Scale = " + scale;
	}
}
