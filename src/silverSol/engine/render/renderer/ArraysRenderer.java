package silverSol.engine.render.renderer;

import org.lwjgl.opengl.GL11;

import silverSol.engine.entity.Entity;
import silverSol.engine.render.camera.Camera;
import silverSol.engine.render.opengl.object.Vao;

public class ArraysRenderer<T extends Entity> extends Renderer<T> {
	
	public ArraysRenderer(Camera camera) {
		super(camera);
	}
	
	@Override
	protected void preRender() {
		activeShaderProgram.start();
		activeShaderProgram.preRender(camera, entities);
	}

	@Override
	protected void preInstance(T entity, int index) {
		entity.getModel().getVao().bind();
		activeShaderProgram.preInstance(camera, entity, index);
		activeShaderProgram.enableAttribues();
		activeShaderProgram.bindTextures(entity.getModel().getTextures());
	}

	@Override
	public void render() {
		preRender();
		
		for(int i = 0; i < entities.size(); i++) {
			T entity = entities.get(i);
			preInstance(entity, i);
			GL11.glDrawArrays(primitive, 0, entity.getModel().getVao().getRenderedVertexCount());
			postInstance(entity, i);
		}
		
		postRender();
	}

	@Override
	protected void postInstance(T entity, int index) {
		activeShaderProgram.postInstance(camera, entity, index);
	}
	
	@Override
	protected void postRender() {
		activeShaderProgram.postRender(camera, entities);
		Vao.unbindVao();
		activeShaderProgram.stop();
	}
	
	@Override
	public void removeEntity(int index) {
		entities.remove(index);
	}
	
	@Override
	public void removeEntity(T entity) {
		entities.remove(entity);
	}
}
