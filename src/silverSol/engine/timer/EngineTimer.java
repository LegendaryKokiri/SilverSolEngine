package silverSol.engine.timer;

import org.lwjgl.Sys;

public class EngineTimer {

	private long lastFrameTime;
	private long dtMillis;
	private float dt;
	
	private float accumulator;
	private int maxIterations;
	
	public EngineTimer() {
		reset();
		maxIterations = 5;
	}
	
	public void update() {
		long currentTime = calculateCurrentTime();
		dtMillis = currentTime - lastFrameTime;
		dt = (float) dtMillis / 1000f;
		lastFrameTime = currentTime;
		accumulator += dt;
	}
	
	public void reset() {
		lastFrameTime = calculateCurrentTime();
		dtMillis = 0;
		dt = 0;
		accumulator = 0;
	}
	
	private long calculateCurrentTime() {
		return Sys.getTime() * 1000 / Sys.getTimerResolution();
	}
	
	public float getDT() {
		return dt;
	}
	
	public float getDTMillis() {
		return dtMillis;
	}
	
	public boolean isTimeAccumulated() {
		return accumulator > 0;
	}
	
	public void stepAccumulatorDown(float dt) {
		accumulator -= dt;
	}
	
	public int getMaxIterations() {
		return maxIterations;
	}

	public void setMaxIterations(int maxIterations) {
		this.maxIterations = maxIterations;
	}

	@Override
	public String toString() {
		return "EngineTimer: " + dt + " second(s)";
	}
	
}
