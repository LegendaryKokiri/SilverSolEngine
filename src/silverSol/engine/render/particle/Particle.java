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
				
		this.lifetime = 1;
		this.elapsedTime = 0;
	}
	
	//TODO: What in the what now? Why does it work like this?
	public void preResolution(float dt) {
		elapsedTime += dt;
		body3d.setShouldRemove(elapsedTime > lifetime); //TODO: Does this belong in the body class?
	}
	
	public void postResolution(float dt) {
		body3d.postResolution(dt);
	}

	public float getLifetime() {
		return lifetime;
	}

	public void setLifetime(float lifetime) {
		this.lifetime = lifetime;
	}
	
}
