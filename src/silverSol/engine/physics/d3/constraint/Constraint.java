package silverSol.engine.physics.d3.constraint;

public abstract class Constraint {
	
	protected float unitsPerMeter;
	protected boolean resolved;
	
	public Constraint() {
		
	}
	
	public abstract void resolve(float dt);
	
	/**
	 * Sets the constraints' units per meter.
	 * Note that Game code should not use this function, as the physics engine will overwrite this value each time it receives a constraint.
	 * Constraint resolution should be adjusted by altering constraint parameters instead.
	 * @param unitsPerMeter
	 */
	public void setUnitsPerMeter(float unitsPerMeter) {
		this.unitsPerMeter = unitsPerMeter;
	}
	
	/**
	 * Returns whether or not this constraint has been resolved.
	 * A resolved constraint will be automatically removed from the physics engine when it updates.
	 * @return Whether or not this constraint has been resolved.
	 */
	public boolean isResolved() {
		return resolved;
	}
	
	/**
	 * Sets whether or not this constraint has been resolved.
	 * A resolved constraint will be automatically removed from the physics engine when it updates.
	 * @param resolved
	 */
	public void setResolved(boolean resolved) {
		this.resolved = resolved;
	}
	
}
