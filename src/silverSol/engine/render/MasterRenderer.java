package silverSol.engine.render;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Vector4f;

import silverSol.engine.entity.Entity;
import silverSol.engine.render.command.RenderCommand;
import silverSol.engine.render.opengl.object.Fbo;
import silverSol.engine.render.renderer.Renderer;
import silverSol.utils.structures.Queue;

public class MasterRenderer {
	
	private Map<Integer, Fbo> fbos;
	private Vector4f clearColor;
	
	public MasterRenderer(float fieldOfView, float nearPlaneDistance, float farPlaneDistance) {
		enableDepthTest();
		enableSmoothShading();
		
		fbos = new HashMap<>();
			fbos.put(0, new Fbo());
		
		clearColor = new Vector4f(0, 0, 0, 0);
	}
	
	public void renderScene(Queue<RenderCommand> renderCommands) {
		clearRenderTargets(clearColor);
		
		for(RenderCommand renderCommand : renderCommands) {
			if(!renderCommand.isActive()) continue;
			Renderer<? extends Entity> renderer = renderCommand.getRenderer();
			renderCommand.getTargetFbo().bind();
			renderer.activateShaderProgram(renderCommand.getShaderProgramIndex());
			renderer.getActiveShader().setRenderSettings(renderCommand.getRenderSettings());
			renderer.render();
		}
	}
	
	public Fbo getDefaultFbo() {
		return fbos.get(0);
	}
	
	public void addFbo(Fbo fbo) {
		fbos.put(fbo.getFboID(), fbo);
	}
	
	public boolean contains(Fbo fbo) {
		return fbos.containsValue(fbo);
	}
	
	public void clearFbos() {
		fbos.clear();
		fbos.put(0, new Fbo());
	}
	
	public static void enableCulling() {
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glCullFace(GL11.GL_BACK);
	}
	
	public static void enableSmoothShading() {
		GL11.glShadeModel(GL11.GL_SMOOTH);
	}
	
	public static void enableDepthTest() {
		GL11.glEnable(GL11.GL_DEPTH_TEST);
	}
	
	public static void enableClipDistanceZero() {
		GL11.glEnable(GL30.GL_CLIP_DISTANCE0);
	}
	
	public void setClearColor(float r, float g, float b, float a) {
		setClearColor(new Vector4f(r, g, b, a));
	}
	
	public void setClearColor(Vector4f clearColor) {
		this.clearColor = clearColor;
	}
	
	private void clearRenderTargets(Vector4f clearColor) {
		Set<Integer> fboIDs = fbos.keySet();
		for(int fboID : fboIDs) {
			Fbo fbo = fbos.get(fboID);
			
			fbo.bind();
			GL11.glClearColor(clearColor.x, clearColor.y, clearColor.z, clearColor.w);
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
			fbo.unbind();
		}
	}
	
	public void progressTime(List<Renderer<? extends Entity>> renderers, float dt, float frameInterpolationFactor) {
		for(Renderer<? extends Entity> renderer : renderers) {
			if(renderer.isEnabled()) renderer.progressTime(dt, frameInterpolationFactor);
		}
	}
}
