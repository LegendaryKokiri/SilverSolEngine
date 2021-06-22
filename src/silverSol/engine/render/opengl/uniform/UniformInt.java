package silverSol.engine.render.opengl.uniform;

import org.lwjgl.opengl.GL20;

public class UniformInt extends Uniform {

	public UniformInt(int programID, String uniformName) {
		super(programID, uniformName);
	}

	public UniformInt(int programID, String uniformName, int arrayLength) {
		super(programID, uniformName, arrayLength);
	}

	public void loadInt(int value) {
		GL20.glUniform1i(location[0], value);
	}
	
	public void loadInt(int value, int arrayIndex) {
		GL20.glUniform1i(location[arrayIndex], value);
	}
	
	public void loadInts(int[] values) {
		for(int i = 0; i < values.length; i++) {
			GL20.glUniform1i(location[i], values[i]);
		}
	}
	
}
