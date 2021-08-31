package silverSol.engine.render.particle;

import silverSol.engine.entity.Entity;
import silverSol.engine.physics.d3.body.Body;
import silverSol.engine.render.model.Model;

public class Particle extends Entity {
	
	//Particle Lifetime
	private float lifetime;
	private float elapsedTime;

	public Particle(Model model, Body body) {
		super(model, body);
				
		this.lifetime = 1f;
		this.elapsedTime = 0f;
	}
	
	public void update(float dt) {
		elapsedTime += dt;
	}
	
	public boolean shouldRemove() {
		return elapsedTime > lifetime;
	}

	public float getLifetime() {
		return lifetime;
	}

	public void setLifetime(float lifetime) {
		this.lifetime = lifetime;
	}
	
}
