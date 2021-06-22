package silverSol.engine.physics.d2.collider;

import org.lwjgl.util.vector.Vector2f;

import silverSol.engine.physics.d2.body.Body;
import silverSol.engine.physics.d3.det.broad.Endpoint;

//BV stands for Bounding Volume
public abstract class Collider {
	
	protected boolean hasBody;
		protected Body body;
	
	protected int collisionType;
	public static final int TYPE_ETHEREAL = 30001;
	public static final int TYPE_SENSOR = 30002;
	public static final int TYPE_COLLIDER = 30003;
	
	//Transformation
	protected Vector2f position;
	protected float rotation;
	
	//Size
	protected Vector2f radius;
	protected float radiusLength;
	protected float radiusLengthSquared;
	protected Endpoint[] endpoints;
		
	//Collision Data
	protected int ID; //TODO: Set ID!
	protected boolean immovable;
	
	//Hierarchy Data
	protected boolean hasParent;
		protected Collider parent;
	protected boolean hasChild;
		protected Collider child;

	//Game Data
	private boolean isHitbox;
	private boolean isHurtbox;
	
	public Collider() {
		this.position = new Vector2f(0, 0);
		this.rotation = 0f;
		this.radius = new Vector2f(1, 1);
		this.radiusLength = 0;
		this.radiusLengthSquared = 3;
		
		this.hasBody = false;
		
		this.collisionType = TYPE_COLLIDER;
		
		endpoints = new Endpoint[6];
		
		for(int i = 0; i < endpoints.length; i++) {
			endpoints[i] = new Endpoint();
			endpoints[i].value = 0;
			endpoints[i].minimum = (i % 2 == 0);			
		}
		
		ID = -1;
		this.immovable = true;
				
		this.hasParent = this.hasChild = false;
		
		this.isHitbox = this.isHurtbox = false;
	}
	
	protected void calculateExtrema() {
		//Indices 0-1 are for x, 2-3 are for y, 4-5 are for z.
		float[] values = new float[]{position.x - radiusLength, position.x + radiusLength, position.y - radiusLength,
				position.y + radiusLength};
		
		//The larger value corresponds to the maximum endpoint.
		for(int i = 0; i < endpoints.length; i++) {
			endpoints[i].value = values[i];	
		}		
	}
	
	public Body getBody() {
		return body;
	}
	
	public void setBody(Body body) {
		this.body = body;
		this.hasBody = this.body != null;
		if(this.hasBody) {
			this.position = new Vector2f().set(body.getPosition());
			this.rotation = body.getRotation();
			this.immovable = body.isImmovable();
			
			calculateExtrema();
		}
	}
	
	public int getID() {
		return ID;
	}

	public void setID(int id) {
		ID = id;
		for(Endpoint e : endpoints) {
			e.id = id;
		}
	}
	
	public boolean hasParent() {
		return hasParent;
	}

	public void setParent(Collider parent) {
		this.parent = parent;
		this.hasParent = this.parent != null;
	}
	
	public Collider getParent() {
		return parent;
	}
	
	public boolean hasChild() {
		return hasChild;
	}

	public Collider getChild() {
		return child;
	}

	public void setChild(Collider child) {
		this.child = child;
		this.hasChild = this.child != null;
	}
	
	public int getCollisionType() {
		return collisionType;
	}
	
	public void setCollisionType(int collisionType) {
		this.collisionType = collisionType;
	}
	
	public void setPosition(Vector2f position) {
		this.position = position;
		calculateExtrema();
	}
	
	public void setRotation(float rotation) {
		this.rotation = rotation;
	}
	
	public Vector2f getRadius() {
		return radius;
	}
	
	public void setRadius(Vector2f radius) {
		this.radius = radius;
		radiusLength = radius.length();
		radiusLengthSquared = radiusLength * radiusLength;
	}
	
	public void scale(float scale) {
		radius.scale(scale);
		radiusLength = radius.length();
		radiusLengthSquared = radiusLength * radiusLength;
	}
	
	public void scale(Vector2f scale) {
		radius = new Vector2f(radius.x * scale.x, radius.y * scale.y);
		radiusLength = radius.length();
		radiusLengthSquared = radiusLength * radiusLength;
	}
	
	public void setIDOfVolumeAndEndpoints(int id) {
		ID = id;
		for(int i = 0; i < endpoints.length; i++) {
			endpoints[i].id = id;
		}
	}
	
	public void setEndpoints(Endpoint[] endpoints) {
		this.endpoints = endpoints;
	}
	
	public void setImmovable(boolean immovable) {
		this.immovable = immovable;
	}
	
	public boolean getImmovable() {
		return immovable;
	}

	public Endpoint[] getEndpoints() {
		return endpoints;
	}
	
	public void updateTransformation() {
		this.position.set(body.getPosition());
	}

	public boolean isHitbox() {
		return isHitbox;
	}

	public void setHitbox(boolean isHitbox) {
		this.isHitbox = isHitbox;
	}

	public boolean isHurtbox() {
		return isHurtbox;
	}

	public void setHurtbox(boolean isHurtbox) {
		this.isHurtbox = isHurtbox;
	}

	@Override
	public String toString() {
		return "BV: Position = " + position + ", Rotation = " + rotation + ", Radius = " + radius;
	}
}
