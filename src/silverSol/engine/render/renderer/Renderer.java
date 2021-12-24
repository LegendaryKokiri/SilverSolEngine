package silverSol.engine.render.renderer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.lwjgl.opengl.GL11;

import silverSol.engine.entity.Entity;
import silverSol.engine.render.camera.Camera;
import silverSol.engine.render.opengl.object.Vao;
import silverSol.engine.render.shader.ShaderProgram;

public abstract class Renderer<T extends Entity> {
	
	protected boolean enabled;
	
	protected int primitive;
	
	protected boolean hasCamera;
		protected Camera camera;
	
	protected List<ShaderProgram<T>> shaderPrograms;
		protected ShaderProgram<T> activeShaderProgram;
		protected int activeShaderProgramIndex;
	
	protected List<Vao> vaos;
	protected List<T> entities;
	
	public Renderer(Camera camera) {
		this.enabled = true;
		
		this.primitive = GL11.GL_TRIANGLES;
		
		this.camera = camera;
		this.hasCamera = this.camera != null;
		
		this.shaderPrograms = new ArrayList<>();
		this.activeShaderProgramIndex = 0;
		
		this.vaos = new ArrayList<>();
		this.entities = new ArrayList<>();
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	public int getPrimitive() {
		return primitive;
	}

	public void setPrimitive(int primitive) {
		this.primitive = primitive;
	}

	public void addCamera(Camera camera) {
		this.camera = camera;
		this.hasCamera = this.camera != null;
	}
	
	public Camera getCamera() {
		return camera;
	}
	
	public void addShaderProgram(ShaderProgram<T> shaderProgram) {
		this.shaderPrograms.add(shaderProgram);
		
		if(this.shaderPrograms.size() == 1) {
			activeShaderProgramIndex = 0;
			activeShaderProgram = this.shaderPrograms.get(0);
		}
	}
	
	public void activateShaderProgram(int shaderIndex) {
		if(shaderIndex < 0 || shaderIndex >= this.shaderPrograms.size()) return;
		
		this.activeShaderProgramIndex = shaderIndex;
		this.activeShaderProgram = this.shaderPrograms.get(shaderIndex);
	}
	
	protected void startActiveShader() {
		activeShaderProgram.start();
	}
	
	public ShaderProgram<T> getActiveShader() {
		return activeShaderProgram;
	}
	
	protected void stopActiveShader() {
		activeShaderProgram.stop();
	}
	
	public ShaderProgram<T> getShaderProgram(int index) {
		return shaderPrograms.get(index);
	}
	
	public void progressTime(float dt, float interpolationFactor) {
		for(T entity : entities) {
			if(entity.animates()) entity.getAnimator().update(dt);
		}
	}
	
	public List<T> getEntities() {
		return entities;
	}
	
	public void addEntity(T entity) {
		entities.add(entity);		
		vaos.add(entity.getModel().getVao());
	}
	
	public void addEntities(T[] entities) {
		for(T entity : entities) {
			addEntity(entity);
		}
	}
	
	public void addEntities(List<T> entities) {
		for(T entity : entities) {
			addEntity(entity);
		}
	}
	
	public void setEntities(List<T> entities) {
		this.entities = entities;
	}
	
	public List<Vao> getVaos() {
		return vaos;
	}

	protected abstract void preRender();
	protected abstract void preInstance(T entity, int index);
	public abstract void render();
	protected abstract void postInstance(T entity, int index);
	protected abstract void postRender();
	
	public abstract void removeEntity(int index);
	public abstract void removeEntity(T entity);
	@SuppressWarnings("unchecked")
	public abstract void removeEntities(T... entities);
	public abstract void removeEntities(Collection<T> entities);
	
	public void clearEntities() {
		entities.clear();
	}
	
	public void cleanUp() {
		for(Vao vao : vaos) {
			vao.delete();
		}
		
		for(ShaderProgram<T> shaderProgram : shaderPrograms) {
			shaderProgram.cleanUp();
		}
	}
	
}
