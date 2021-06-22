package silverSol.engine.entity;

import silverSol.engine.render.animation.Animator;
import silverSol.engine.render.model.Model;

/**
 * The Entity class connects game entities to the render engine and the physics engine.
 * @author Julian
 *
 */
public class Entity {
	
	protected boolean hasModel;
		protected Model model;
	
	protected boolean animates;
		protected Animator animator;
	
	protected boolean hasBody2d;
		protected silverSol.engine.physics.d2.body.Body body2d;
		
	protected boolean hasBody3d;
		protected silverSol.engine.physics.d3.body.Body body3d;
		
	public Entity() {
		this.hasModel = this.hasBody2d = this.hasBody3d = false;
	}
	
	public Entity(Model model) {
		this.model = model;
		this.hasModel = model != null;
		
		prepareAnimator();
	}
	
	public Entity(silverSol.engine.physics.d2.body.Body body) {
		this.body2d = body;
		this.hasBody2d = body != null;
	}
	
	public Entity(silverSol.engine.physics.d3.body.Body body) {
		this.body3d = body;
		this.hasBody3d = body != null;
	}
	
	public Entity(Model model, silverSol.engine.physics.d2.body.Body body) {
		this.model = model;
		this.hasModel = model != null;

		prepareAnimator();
		
		this.body2d = body;
		this.hasBody2d = body != null;
	}
	
	public Entity(Model model, silverSol.engine.physics.d3.body.Body body) {
		this.model = model;
		this.hasModel = model != null;

		prepareAnimator();
		
		this.body3d = body;
		this.hasBody3d = body != null;
	}

	public boolean hasModel() {
		return hasModel;
	}
	
	public Model getModel() {
		return model;
	}

	public void setModel(Model model) {
		this.model = model;
		this.hasModel = model != null;
		prepareAnimator();
	}
	
	public boolean animates() {
		return animates;
	}
	
	public void animate(float dt) {
		if(!animates) return;
		animator.progressAnimation(dt);
	}

	public void setAnimates(boolean animates) {
		this.animates = animates;
		if(animates) prepareAnimator();
	}

	public Animator getAnimator() {
		return animator;
	}

	public void prepareAnimator() {
		if(hasModel)  {
			if(model.hasArmature() && model.getArmature().hasAnimation()) {
				if(animator == null) animator = new Animator();
				this.animates = true;
				animator.setModelAnimations(model.getArmature(), model.getArmature().getAnimations());
			}
			
			if(model.hasTexture()) {
				if(animator == null) animator = new Animator();
				this.animates = true;
				animator.setTextureAnimations(model.getTextures().get(0).getAnimations());
			}
		}			
	}
	
	public void setAnimator(Animator animator) {
		this.animator = animator;
	}

	public boolean hasBody2d() {
		return hasBody2d;
	}

	public silverSol.engine.physics.d2.body.Body getBody2d() {
		return body2d;
	}

	public void setBody(silverSol.engine.physics.d2.body.Body body) {
		this.body2d = body;
		this.hasBody2d = body != null;
	}

	public boolean hasBody3d() {
		return hasBody3d;
	}

	public silverSol.engine.physics.d3.body.Body getBody3d() {
		return body3d;
	}

	public void setBody(silverSol.engine.physics.d3.body.Body body) {
		this.body3d = body;
		this.hasBody3d = body != null;
	}
}
