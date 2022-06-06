package silverSol.engine.render.renderer;

import java.util.List;

import org.lwjgl.opengl.GL11;

import silverSol.engine.entity.Entity;
import silverSol.engine.render.camera.Camera;

public class ArraysRenderer<T extends Entity> extends Renderer<T> {
	
	public ArraysRenderer(Camera camera) {
		super(camera);
	}

	@Override
	public void render() {
		preRender();
		
		List<T> entities = shaderEntities.get(activeShaderProgramIndex);
		for(int i = 0; i < entities.size(); i++) {
			T entity = entities.get(i);
			preInstance(entity, i);
			GL11.glDrawArrays(primitive, 0, entity.getModel().getVao().getRenderedVertexCount());
			postInstance(entity, i);
		}
		
		postRender();
	}
	
	
}
