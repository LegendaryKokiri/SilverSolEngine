package silverSol.engine.render;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.LWJGLException;

import silverSol.engine.entity.Entity;
import silverSol.engine.render.command.RenderCommand;
import silverSol.engine.render.renderer.Renderer;
import silverSol.engine.settings.RenderSettings;
import silverSol.utils.structures.Queue;


public class RenderEngine {
	protected DisplayManager displayManager;
	protected MasterRenderer masterRenderer;
	
	private List<Renderer<? extends Entity>> renderers;
	private Queue<RenderCommand> renderCommands;
	
	private int fps;
	private float targetDT;
		
	public RenderEngine(RenderSettings rs) {
		this.fps = rs.getFPS();
		this.targetDT = 1f / (float) fps;
		
		displayManager = new DisplayManager();
			displayManager.createDisplay(rs.getOpenGLMajorVersion(), rs.getOpenGLMinorVersion(),
					rs.getScreenWidth(), rs.getScreenHeight(), rs.getScreenX(), rs.getScreenY());
			displayManager.setFPS(fps);
		
		//TODO: Make these parameters configurable
		masterRenderer = new MasterRenderer(70f, 0.1f, 2000f);
		
		renderers = new ArrayList<>();
		renderCommands = new Queue<>();
	}
	
	public void init() {
		
	}
	
	public void addRenderCommand(RenderCommand renderCommand) {
		Renderer<? extends Entity> renderer = renderCommand.getRenderer();
		if(!renderers.contains(renderer)) renderers.add(renderer);
		if(!masterRenderer.contains(renderCommand.getTargetFbo())) masterRenderer.addFbo(renderCommand.getTargetFbo());
		
		renderCommands.enqueue(renderCommand);
	}
	
	public void clearRenderCommands() {
		renderCommands.clear();
	}
	
	public void render() throws LWJGLException {
		masterRenderer.renderScene(renderCommands);
		displayManager.updateDisplay();
	}
	
	public MasterRenderer getMasterRenderer() {
		return masterRenderer;
	}
	
	public DisplayManager getDisplayManager() {
		return displayManager;
	}
	
	public void progressTime(List<Renderer<? extends Entity>> renderers, float dt, float frameInterpolationFactor) {
		masterRenderer.progressTime(renderers, dt, frameInterpolationFactor);
	}
	
	public void setFPS(int fps) {
		this.fps = fps;
		this.targetDT = 1f / (float) fps;
		
		displayManager.setFPS(fps);
	}

	public int getFPS() {
		return fps;
	}
	
	public float getTargetDT() {
		return targetDT;
	}
	
	public void cleanUp() {
		for(Renderer<? extends Entity> renderer : renderers) {
			renderer.cleanUp();
		}
		
		DisplayManager.closeDisplay();
	}
}
