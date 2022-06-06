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
	protected List<List<T>> shaderEntities;
	
	public Renderer(Camera camera) {
		this.enabled = true;
		
		this.primitive = GL11.GL_TRIANGLES;
		
		this.camera = camera;
		this.hasCamera = this.camera != null;
		
		this.shaderPrograms = new ArrayList<>();
		this.activeShaderProgramIndex = 0;
		
		this.vaos = new ArrayList<>();
		this.shaderEntities = new ArrayList<>();
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
		
		this.shaderEntities.add(new ArrayList<>());
	}
	
	public void activateShaderProgram(int shaderIndex) {
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
		for(int i = 0; i < shaderPrograms.size(); i++) {
			List<T> entities = shaderEntities.get(i);
			for(T entity : entities) {
				if(entity.animates()) entity.getAnimator().update(dt);
			}
		}
	}
	
	public List<T> getEntities() {
		return getEntities(activeShaderProgramIndex);
	}
	
	public List<T> getEntities(int shaderProgramIndex) {
		return shaderEntities.get(shaderProgramIndex);
	}
	
	public void addEntity(T entity) {
		addEntity(entity, activeShaderProgramIndex);
	}
	
	public void addEntity(T entity, int shaderProgramIndex) {
		shaderEntities.get(shaderProgramIndex).add(entity);		
		vaos.add(entity.getModel().getVao());
	}
	
	public void addEntities(T[] entities) {
		addEntities(entities, activeShaderProgramIndex);
	}
	
	public void addEntities(T[] entities, int shaderProgramIndex) {
		for(T entity : entities) {
			addEntity(entity, shaderProgramIndex);
		}
	}
	
	public void addEntities(List<T> entities) {
		addEntities(entities, activeShaderProgramIndex);
	}
	
	public void addEntities(List<T> entities, int shaderProgramIndex) {
		for(T entity : entities) {
			addEntity(entity, shaderProgramIndex);
		}
	}
	
	public void setEntities(T[] entities) {
		setEntities(entities, activeShaderProgramIndex);
	}
	
	public void setEntities(T[] entities, int shaderProgramIndex) {
		this.shaderEntities.get(shaderProgramIndex).clear();
		for(T entity : entities) {
			addEntity(entity, shaderProgramIndex);
		}
	}
	
	public void setEntities(List<T> entities) {
		setEntities(entities, activeShaderProgramIndex);
	}
	
	public void setEntities(List<T> entities, int shaderProgramIndex) {
		this.shaderEntities.get(shaderProgramIndex).clear();
		for(T entity : entities) {
			addEntity(entity, shaderProgramIndex);
		}
	}
	
	public List<Vao> getVaos() {
		return vaos;
	}

	protected void preRender() {
		activeShaderProgram.start();
		activeShaderProgram.preRender(camera, shaderEntities.get(activeShaderProgramIndex));
	}

	protected void preInstance(T entity, int index) {
		entity.getModel().getVao().bind();
		activeShaderProgram.preInstance(camera, entity, index);
		activeShaderProgram.enableAttributes();
		activeShaderProgram.bindTextures(entity.getModel().getTextures());
	}
	
	public abstract void render();
	
	protected void postInstance(T entity, int index) {
		activeShaderProgram.postInstance(camera, entity, index);
	}
	
	protected void postRender() {
		activeShaderProgram.postRender(camera, shaderEntities.get(activeShaderProgramIndex));
		
		Vao.unbindVao();
		activeShaderProgram.stop();
	}
	
	public void removeEntity(int index) {
		shaderEntities.get(activeShaderProgramIndex).remove(index);
	}
	
	public void removeEntity(T entity) {
		shaderEntities.get(activeShaderProgramIndex).remove(entity);
	}

	public void removeEntities(T[] entities) {
		removeEntities(entities, activeShaderProgramIndex);
	}
	
	public void removeEntities(T[] entities, int shaderProgramIndex) {
		for(T entity : entities) this.shaderEntities.get(shaderProgramIndex).remove(entity);
	}
	
	public void removeEntities(Collection<T> entities) {
		removeEntities(entities, activeShaderProgramIndex);
	}

	public void removeEntities(Collection<T> entities, int shaderProgramIndex) {
		this.shaderEntities.get(shaderProgramIndex).removeAll(entities);
	}
	
	public void clearEntities() {
		clearEntities(activeShaderProgramIndex);
	}
	
	public void clearEntities(int shaderProgramIndex) {
		shaderEntities.get(shaderProgramIndex).clear();
	}
	
	public void clearAllEntities() {
		for(List<T> entities : shaderEntities) {
			entities.clear();
		}
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
