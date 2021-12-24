package silverSol.engine.render.renderer;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL31;

import silverSol.engine.entity.Entity;
import silverSol.engine.render.camera.Camera;
import silverSol.engine.render.model.Model;
import silverSol.engine.render.opengl.object.Vao;

public class InstancedElementsRenderer<T extends Entity> extends InstancedRenderer<T> {

	private int indexOffset;
	
	public InstancedElementsRenderer(Camera camera, int maximumInstances, int instancedDataLength, int drawType) {
		super(camera, maximumInstances, instancedDataLength, drawType);
		this.indexOffset = 0;
	}
	
	@Override
	protected void preRender() {
		activeShaderProgram.start();
		activeShaderProgram.preRender(camera, entities);
	}

	@Override
	protected void preInstance(T entity, int index) {
		activeShaderProgram.preInstance(camera, entity, index);
	}

	@Override
	public void render() {
		preRender();
		
		Set<Model> instanceKeys = modelInstances.keySet();
		for(Model model : instanceKeys) {
			List<T> keyInstances = modelInstances.get(model);
			
			if(keyInstances.size() > 0) {
				Entity referenceEntity = keyInstances.get(0);
				referenceEntity.getModel().getVao().bind();
				activeShaderProgram.enableAttribues();
				if(referenceEntity.getModel().hasTexture()) {
					activeShaderProgram.bindTextures(referenceEntity.getModel().getTextures());
				}
			
				for(int i = 0; i < entities.size(); i++) {
					T entity = entities.get(i);
					preInstance(entity, i);
				}
				
				instanceVbo.overwriteAttribute(instanceData);
				GL31.glDrawElementsInstanced(primitive, model.getVao().getRenderedVertexCount(), GL11.GL_UNSIGNED_INT, 0, keyInstances.size());
				
				for(int i = 0; i < entities.size(); i++) {
					T entity = entities.get(i);
					postInstance(entity, i);
				}
			}
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
		removeEntity(entities.get(index));
	}
	
	@Override
	public void removeEntity(T entity) {
		entities.remove(entity);
		modelInstances.get(entity.getModel()).remove(entity);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void removeEntities(T... entities) {
		for(T entity : entities) removeEntity(entity);
	}
	
	@Override
	public void removeEntities(Collection<T> entities) {
		for(T entity : entities) removeEntity(entity);
	}
	
	public int getIndexOffset() {
		return indexOffset;
	}

	public void setIndexOffset(int indexOffset) {
		this.indexOffset = indexOffset;
	}
}
