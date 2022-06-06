package silverSol.engine.render.command;

public abstract class RendererCommand {

	protected boolean active;
	
	public RendererCommand() {
		this.active = true;
	}
	
	public abstract void execute();
	
	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
	
}
