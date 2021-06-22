package silverSol.engine.render.command;

import silverSol.engine.entity.Entity;
import silverSol.engine.render.opengl.object.Fbo;
import silverSol.engine.render.renderer.Renderer;

public class RenderCommand {

	private boolean active;
	private Renderer<? extends Entity> renderer;
	private Fbo targetFbo;
	private int shaderProgramIndex;
	private Object renderSettings;
	
	public RenderCommand(Renderer<? extends Entity> renderer, Fbo targetFbo, int shaderProgramIndex, Object renderSettings) {
		super();
		this.active = true;
		this.renderer = renderer;
		this.targetFbo = targetFbo;
		this.shaderProgramIndex = shaderProgramIndex;
		this.renderSettings = renderSettings;
	}
	
	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public Renderer<? extends Entity> getRenderer() {
		return renderer;
	}
	
	public void setRenderer(Renderer<? extends Entity> renderer) {
		this.renderer = renderer;
	}

	public Fbo getTargetFbo() {
		return targetFbo;
	}
	
	public void setTargetFbo(Fbo targetFbo) {
		this.targetFbo = targetFbo;
	}

	public int getShaderProgramIndex() {
		return shaderProgramIndex;
	}

	public void setShaderProgramIndex(int shaderProgramIndex) {
		this.shaderProgramIndex = shaderProgramIndex;
	}

	public Object getRenderSettings() {
		return renderSettings;
	}

	public void setRenderSettings(Object renderSettings) {
		this.renderSettings = renderSettings;
	}
	
}
