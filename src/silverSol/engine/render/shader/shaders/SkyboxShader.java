package silverSol.engine.render.shader.shaders;

import java.util.List;

import org.lwjgl.util.vector.Matrix4f;

import silverSol.engine.render.camera.Camera;
import silverSol.engine.render.opengl.attribute.AttributeVector3f;
import silverSol.engine.render.opengl.uniform.UniformMatrix4f;
import silverSol.engine.render.opengl.uniform.UniformSamplerCube;
import silverSol.engine.render.shader.ShaderProgram;
import silverSol.engine.render.skybox.Skybox;
import silverSol.math.MatrixMath;

public class SkyboxShader<T extends Skybox> extends ShaderProgram<T> {
	
	private UniformMatrix4f projectionMatrix;
	private UniformMatrix4f viewMatrix;
	
	private UniformSamplerCube cubeMap;
	
	public SkyboxShader() {
		super("/silverSol/engine/render/shader/shaders/skyboxVertexShader.txt",
				"/silverSol/engine/render/shader/shaders/skyboxFragmentShader.txt",
				new AttributeVector3f("position", false));	
		
		this.start();
				
		viewMatrix = new UniformMatrix4f(programID, "viewMatrix");
		projectionMatrix = new UniformMatrix4f(programID, "projectionMatrix");
		
		cubeMap = new UniformSamplerCube(programID, "cubeMap");
			cubeMap.connectTextureUnit(0);
		
		this.stop();
	}

	@Override
	public void preRender(Camera camera, List<T> entities) {
		projectionMatrix.loadMatrix(camera.getProjectionMatrix());
		
		Matrix4f view = MatrixMath.clone(camera.getViewMatrix());
		view.m30 = view.m31 = view.m32 = 0;
		viewMatrix.loadMatrix(view);
	}

	@Override
	public void preInstance(Camera camera, T entity, int index) {

	}

	@Override
	public void postInstance(Camera camera, T entity, int index) {
		
	}

	@Override
	public void postRender(Camera camera, List<T> entities) {
		
	}
	
}
