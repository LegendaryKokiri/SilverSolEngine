package silverSol.engine.render.opengl.uniform;

import org.lwjgl.opengl.GL20;
import org.lwjgl.util.vector.Vector2f;

public class UniformVector2f extends Uniform {

	public UniformVector2f(int programID, String uniformName) {
		super(programID, uniformName);
	}
	
	public UniformVector2f(int programID, String uniformName, int arrayLength) {
		super(programID, uniformName, arrayLength);
	}

	public void loadVector(Vector2f vector) {
		GL20.glUniform2f(location[0], vector.x, vector.y);
	}
	
	public void loadVector(Vector2f vector, int arrayIndex) {
		GL20.glUniform2f(location[arrayIndex], vector.x, vector.y);
	}
	
	public void loadVectors(Vector2f[] values) {
		for(int i = 0; i < values.length; i++) {
			Vector2f vector = values[i];
			GL20.glUniform2f(location[i], vector.x, vector.y);
		}
	}

}
