package silverSol.engine.render.opengl.uniform;

import org.lwjgl.opengl.GL20;
import org.lwjgl.util.vector.Vector4f;

public class UniformVector4f extends Uniform {

	public UniformVector4f(int programID, String uniformName) {
		super(programID, uniformName);
	}
	
	public UniformVector4f(int programID, String uniformName, int arrayLength) {
		super(programID, uniformName, arrayLength);
	}
	
	public void loadVector(Vector4f vector) {
		GL20.glUniform4f(location[0], vector.x, vector.y, vector.z, vector.w);
	}
	
	public void loadVector(Vector4f vector, int arrayIndex) {
		GL20.glUniform4f(location[arrayIndex], vector.x, vector.y, vector.z, vector.w);
	}
	
	public void loadVectors(Vector4f[] values) {
		for(int i = 0; i < values.length; i++) {
			Vector4f vector = values[i];
			GL20.glUniform4f(location[i], vector.x, vector.y, vector.z, vector.w);
		}
	}
}
