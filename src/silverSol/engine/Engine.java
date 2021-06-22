package silverSol.engine;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.xml.sax.SAXException;

import silverSol.engine.audio.MasterAudioPlayer;
import silverSol.engine.physics.d3.PhysicsEngine3d;
import silverSol.engine.render.RenderEngine;
import silverSol.engine.settings.PhysicsSettings;
import silverSol.engine.settings.RenderSettings;
import silverSol.engine.timer.EngineTimer;
import silverSol.game.Game;
import silverSol.game.gameState.GameState;

public class Engine {
	
	private MasterAudioPlayer masterAudioPlayer;
	private RenderEngine renderEngine;
	private PhysicsEngine3d physicsEngine3d;
	
	private EngineTimer timer;
	
	/**
	 * Creates a new instance of SilverSol Engine.
	 * @param openGLMajorVersion The major version of OpenGL in use
	 * @param openGLMinorVersion The minor version of OpenGL in use
	 * @param screenWidth The width of the game window in pixels
	 * @param screenHeight The height of the game window in pixels
	 * @param screenX The x-coordinate of the upper-left corner of the game window in pixels
	 * @param screenY The y-coordinate of the upper-left corner of the game window in pixels
	 */
	public Engine() {
		this(new PhysicsSettings(), new RenderSettings());
	}
	
	public Engine(PhysicsSettings physicsSettings, RenderSettings renderSettings) {
		masterAudioPlayer = new MasterAudioPlayer();
		renderEngine = new RenderEngine(renderSettings);
		physicsEngine3d = new PhysicsEngine3d(physicsSettings);
		timer = new EngineTimer();
	}
	
	/**
	 * Runs the game passed as a parameter.
	 * @param game The game to be run by this engine.
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws LWJGLException
	 */
	public void runGame(Game game) throws ParserConfigurationException, SAXException, IOException, InterruptedException, LWJGLException {
		startGame(game);
		GameState activeGameState = game.getActiveGameState();
		
		while(!game.shouldClose()) {
			if(activeGameState.shouldClose()) {
				activeGameState = proceedToNextGameState(game);
				if(activeGameState != null) renderEngine.init();
				timer.reset();
				continue;
			}
			
			masterAudioPlayer.update(activeGameState.getPlayingSounds());
			
			int iterationCount = 0;
			while(timer.isTimeAccumulated() && iterationCount < timer.getMaxIterations()) {
				activeGameState.updatePrePhysics();
				physicsEngine3d.update();
				activeGameState.updatePostPhysics();
				timer.stepAccumulatorDown(physicsEngine3d.getTargetDT());
				iterationCount++;
			}
			
			activeGameState.updatePreRender();
			renderEngine.render();
			activeGameState.updatePostRender();
			timer.update();
		}
				
		cleanUp();
	}
	
	private void startGame(Game game) throws LWJGLException {
		game.startGame();
		renderEngine.init();
		Display.setTitle(game.getName());
		timer.reset();
	}
	
	private GameState proceedToNextGameState(Game game) throws LWJGLException {
		GameState nextGameState = game.proceedToNextGameState();
		physicsEngine3d.clearBodies();		
		return nextGameState;
	}
	
	public MasterAudioPlayer getMasterAudioPlayer() {
		return masterAudioPlayer;
	}
	
	public RenderEngine getRenderEngine() {
		return renderEngine;
	}
	
	public PhysicsEngine3d getPhysicsEngine3d() {
		return physicsEngine3d;
	}
	
	private void cleanUp() {
		masterAudioPlayer.cleanUp();
		renderEngine.cleanUp();
	}
}