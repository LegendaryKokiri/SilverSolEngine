package silverSol.game.gameState;

import java.util.ArrayList;
import java.util.List;

import silverSol.engine.Engine;
import silverSol.engine.audio.sound.Sound;
import silverSol.engine.physics.d3.PhysicsEngine3d;
import silverSol.engine.render.RenderEngine;

/**
 * GameState objects are the backbone of Game objects.
 * @author Julian
 *
 */
public abstract class GameState {
	
	private List<Sound> playingSounds;
	protected RenderEngine renderEngine;
	protected PhysicsEngine3d physicsEngine3d;
	
	protected boolean shouldClose;
	protected GameState queuedGameState;
	
	/**
	 * Creates a new GameState.
	 * @param engine The Engine that should run this GameState
	 */
	public GameState(Engine engine) {
		playingSounds = new ArrayList<>();
		this.renderEngine = engine.getRenderEngine();
		this.physicsEngine3d = engine.getPhysicsEngine3d();
		
		shouldClose = false;
	}
	
	public boolean shouldClose() {
		return shouldClose;
	}
	
	public GameState getQueuedGameState() {
		return queuedGameState;
	}

	public abstract void init();
	public abstract void updatePrePhysics();
	public abstract void updatePostPhysics();
	public abstract void updatePreRender();
	public abstract void updatePostRender();
	public abstract void close();
	
	protected void playSound(Sound sound) {
		playingSounds.add(sound);
	}
	
	public List<Sound> getPlayingSounds() {
		return playingSounds;
	}
}
