package silverSol.engine.render.renderer;

import java.util.List;

import org.lwjgl.opengl.GL11;

import silverSol.engine.entity.Entity;
import silverSol.engine.render.camera.Camera;

public class ElementsRenderer<T extends Entity> extends Renderer<T> {

	private int indexOffset;
	
	public ElementsRenderer(Camera camera) {
		super(camera);
	}

	@Override
	public void render() {
		preRender();
		
		List<T> entities = shaderEntities.get(activeShaderProgramIndex);
		for(int i = 0; i < entities.size(); i++) {
			T entity = entities.get(i);
			preInstance(entity, i);
			GL11.glDrawElements(primitive, entity.getModel().getVao().getRenderedVertexCount(), GL11.GL_UNSIGNED_INT, indexOffset);
			postInstance(entity, i);
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
