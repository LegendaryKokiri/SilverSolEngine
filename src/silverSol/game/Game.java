package silverSol.game;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;

import silverSol.game.gameState.GameState;

/**
 * The Game object is the main object upon which Engine objects operate.
 * @author Julian
 *
 */
public class Game {

	protected String name;
	private GameState activeGameState;
	
	/**
	 * Creates a new instance of a game.
	 * @param name The name of this game
	 * @param activeGameState The GameState that this game should initialize and run first
	 */
	public Game(String name, GameState activeGameState) {
		this.name = name;
		this.activeGameState = activeGameState;
	}
	
	/**
	 * Starts this game
	 * @throws LWJGLException
	 */
	public void startGame() throws LWJGLException {
		initializeActiveGameState();
	}
	
	/**
	 * Closes the current GameState and initializes the GameState that the current GameState has queued.
	 * @return The GameState that the current GameState has queued
	 * @throws LWJGLException
	 */
	public GameState proceedToNextGameState() throws LWJGLException {
		activeGameState.close();
		activeGameState = activeGameState.getQueuedGameState();
		initializeActiveGameState();
		return activeGameState;
	}
	
	private void initializeActiveGameState() {
		if(activeGameState != null) {
			activeGameState.init();
		}
	}
	
	public String getName() {
		return name;
	}

	/**
	 * Sets this Game's name and updates the window's title to match.
	 * @param name The name of this Game
	 */
	public void setName(String name) {
		this.name = name;
		Display.setTitle(name);
	}
	
	public GameState getActiveGameState() {
		return activeGameState;
	}

	public boolean shouldClose() {
		return activeGameState == null || Display.isCloseRequested();
	}
	
	@Override
	public String toString() {
		return "Game " + name;
	}
	
}
