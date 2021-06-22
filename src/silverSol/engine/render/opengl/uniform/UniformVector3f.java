package silverSol.engine.render.opengl.uniform;

import org.lwjgl.opengl.GL20;
import org.lwjgl.util.vector.Vector3f;

public class UniformVector3f extends Uniform {

	public UniformVector3f(int programID, String uniformName) {
		super(programID, uniformName);
	}
	
	public UniformVector3f(int programID, String uniformName, int arrayLength) {
		super(programID, uniformName, arrayLength);
	}

	public void loadVector(Vector3f vector) {
		GL20.glUniform3f(location[0], vector.x, vector.y, vector.z);
	}
	
	public void loadVector(Vector3f vector, int arrayIndex) {
		GL20.glUniform3f(location[arrayIndex], vector.x, vector.y, vector.z);
	}
	
	public void loadVectors(Vector3f[] values) {
		for(int i = 0; i < values.length; i++) {
			Vector3f vector = values[i];
			GL20.glUniform3f(location[i], vector.x, vector.y, vector.z);
		}
	}

}
