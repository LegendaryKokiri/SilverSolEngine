package silverSol.engine.render.renderer;

import java.util.List;
import java.util.Set;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL31;

import silverSol.engine.entity.Entity;
import silverSol.engine.render.camera.Camera;
import silverSol.engine.render.model.Model;

public class InstancedElementsRenderer<T extends Entity> extends InstancedRenderer<T> {

	private int indexOffset;
	
	public InstancedElementsRenderer(Camera camera, int maximumInstances, int instancedDataLength, int drawType) {
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
				GL31.glDrawElementsInstanced(primitive, model.getVao().getRenderedVertexCount(), GL11.GL_UNSIGNED_INT, indexOffset, keyInstances.size());
				
				for(int i = 0; i < entities.size(); i++) {
					T entity = entities.get(i);
					postInstance(entity, i);
				}
			}
		}
		
		postRender();
	}
	
	public int getIndexOffset() {
		return indexOffset;
	}

	public void setIndexOffset(int indexOffset) {
		this.indexOffset = indexOffset;
	}
}
