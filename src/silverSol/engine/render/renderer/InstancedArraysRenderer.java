package silverSol.engine.render.renderer;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.lwjgl.opengl.GL31;

import silverSol.engine.entity.Entity;
import silverSol.engine.render.camera.Camera;
import silverSol.engine.render.model.Model;

public class InstancedArraysRenderer<T extends Entity> extends InstancedRenderer<T> {
	
	public InstancedArraysRenderer(Camera camera, int maximumInstances, int instancedDataLength, int drawType) {
		super(camera, maximumInstances, instancedDataLength, drawType);
	}

	@Override
	public void render() {
		preRender();
		
		List<T> entities = shaderEntities.get(activeShaderProgramIndex);
		Set<Model> instanceKeys = modelInstances.keySet();
		
		for(Model model : instanceKeys) {
			List<T> keyInstances = modelInstances.get(model);
			
			if(keyInstances.size() > 0) {
				Entity referenceEntity = keyInstances.get(0);
				referenceEntity.getModel().getVao().bind();
				activeShaderProgram.enableAttributes();
				if(referenceEntity.getModel().hasTexture()) {
					activeShaderProgram.bindTextures(referenceEntity.getModel().getTextures());
				}
			
				for(int i = 0; i < entities.size(); i++) {
					T entity = entities.get(i);
					preInstance(entity, i);
				}
				
				instanceVbo.overwriteAttribute(instanceData);
				GL31.glDrawArraysInstanced(primitive, 0, referenceEntity.getModel().getVao().getRenderedVertexCount(), keyInstances.size());
				
				for(int i = 0; i < entities.size(); i++) {
					T entity = entities.get(i);
					postInstance(entity, i);
				}
			}
		}
		
		postRender();
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
}
