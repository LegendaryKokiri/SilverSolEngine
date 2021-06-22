package silverSol.engine.render.opengl.uniform;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.vector.Matrix4f;

public class UniformMatrix4f extends Uniform {

	public UniformMatrix4f(int programID, String uniformName) {
		super(programID, uniformName);
	}
	
	public UniformMatrix4f(int programID, String uniformName, int arrayLength) {
		super(programID, uniformName, arrayLength);
	}

	public void loadMatrix(Matrix4f matrix) {
		FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);
		matrix.store(matrixBuffer);
		matrixBuffer.flip();
		GL20.glUniformMatrix4(location[0], false, matrixBuffer);
	}
	
	public void loadMatrix(Matrix4f matrix, int arrayIndex) {
		FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);
		matrix.store(matrixBuffer);
		matrixBuffer.flip();
		GL20.glUniformMatrix4(location[arrayIndex], false, matrixBuffer);
	}
	
	public void loadMatrices(Matrix4f[] values) {
		FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);
		for(int i = 0; i < values.length; i++) {
			values[i].store(matrixBuffer);
			matrixBuffer.flip();
			GL20.glUniformMatrix4(location[i], false, matrixBuffer);
			matrixBuffer.flip();
		}
	}

}
