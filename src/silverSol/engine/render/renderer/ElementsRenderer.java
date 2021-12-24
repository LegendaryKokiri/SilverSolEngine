package silverSol.engine.render.renderer;

import java.util.Collection;

import org.lwjgl.opengl.GL11;

import silverSol.engine.entity.Entity;
import silverSol.engine.render.camera.Camera;
import silverSol.engine.render.opengl.object.Vao;

public class ElementsRenderer<T extends Entity> extends Renderer<T> {

	private int indexOffset;
	
	public ElementsRenderer(Camera camera) {
		super(camera);
		this.indexOffset = 0;
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
			GL11.glDrawElements(primitive, entity.getModel().getVao().getRenderedVertexCount(), GL11.GL_UNSIGNED_INT, indexOffset);
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
	
	@Override
	@SuppressWarnings("unchecked")
	public void removeEntities(T... entities) {
		for(T entity : entities) this.entities.remove(entity);
	}

	@Override
	public void removeEntities(Collection<T> entities) {
		this.entities.removeAll(entities);
	}

	public int getIndexOffset() {
		return indexOffset;
	}

	public void setIndexOffset(int indexOffset) {
		this.indexOffset = indexOffset;
	}
}
