package silverSol.engine.physics.d2.state;

import org.lwjgl.util.vector.Vector2f;

public class PhysicalState {
	
	private Vector2f position, scale;
	private float rotation;
	
	public PhysicalState() {
		this.position = new Vector2f(0, 0);
		this.rotation = 0;
		this.scale = new Vector2f(1, 1);
	}
	
	public PhysicalState(Vector2f position, float rotation, Vector2f scale) {
		this.position = position;
		this.rotation = rotation;
		this.scale = scale;
	}

	public Vector2f getPosition() {
		return position;
	}

	public void setPosition(Vector2f position) {
		this.position = position;
	}

	public float getRotation() {
		return rotation;
	}

	public void setRotation(float rotation) {
		this.rotation = rotation;
	}

	public Vector2f getScale() {
		return scale;
	}

	public void setScale(Vector2f scale) {
		this.scale = scale;
	}
	
}
