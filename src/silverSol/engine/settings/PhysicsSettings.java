package silverSol.engine.settings;

public class PhysicsSettings {
	
	private int fps;
	
	public PhysicsSettings() {
		this.fps = 60;
	}
	
	public PhysicsSettings(int fps) {
		this.fps = fps;
	}

	public int getFPS() {
		return fps;
	}

	public void setFPS(int fps) {
		this.fps = fps;
	}

}
