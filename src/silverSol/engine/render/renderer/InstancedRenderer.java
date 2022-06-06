package silverSol.engine.render.renderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import silverSol.engine.entity.Entity;
import silverSol.engine.render.camera.Camera;
import silverSol.engine.render.model.Model;
import silverSol.engine.render.opengl.object.Vao;
import silverSol.engine.render.opengl.object.Vbo;
import silverSol.math.OpenGLMath;

public abstract class InstancedRenderer<T extends Entity> extends Renderer<T> {
	
	protected HashMap<Model, List<T>> modelInstances;
	
	protected Vbo instanceVbo;
	private int maximumInstances;
	private int instancedDataLength;
	
	protected float[] instanceData;
	protected List<Float> dataToStore;
	
	public InstancedRenderer(Camera camera, int maximumInstances, int instancedDataLength, int drawType) {
		super(camera);
		
		this.maximumInstances = maximumInstances;
		this.instancedDataLength = instancedDataLength;
		
		int floatCount = instancedDataLength * maximumInstances;
		
		this.instanceVbo = new Vbo();
		this.instanceVbo.setDrawType(drawType);
		this.instanceVbo.allocateData(OpenGLMath.getFloatByteSize(floatCount));
		this.instanceData = new float[floatCount];
		dataToStore = new ArrayList<>();
		
		modelInstances = new HashMap<>();
	}
	
	@Override
	public void addEntity(T entity) {
		super.addEntity(entity);
		
		Model model = entity.getModel();
		if(modelInstances.get(model) == null)
			modelInstances.put(model, new ArrayList<T>());
			
			modelInstances.get(model).add(entity);
	}
	
	@Override
	public void removeEntity(int index) {
		removeEntity(shaderEntities.get(activeShaderProgramIndex).get(index));
	}
	
	@Override
	public void removeEntity(T entity) {
		shaderEntities.get(activeShaderProgramIndex).remove(entity);
		modelInstances.get(entity.getModel()).remove(entity);
	}

	@Override
	protected void preInstance(T entity, int index) {
		activeShaderProgram.preInstance(camera, entity, index);
	}
	
	@Override
	protected void postInstance(T entity, int index) {
		activeShaderProgram.postInstance(camera, entity, index);
	}
	
	public int getMaximumInstances() {
		return maximumInstances;
	}

	public void setMaximumInstances(int maximumInstances) {
		this.maximumInstances = maximumInstances;
	}

	public int getInstancedDataLength() {
		return instancedDataLength;
	}

	public void setInstancedDataLength(int instancedDataLength) {
		this.instancedDataLength = instancedDataLength;
	}

	public Vbo getInstanceVbo() {
		return instanceVbo;
	}

	public void setInstanceVbo(Vbo instanceVbo) {
		this.instanceVbo = instanceVbo;
	}
	
}
