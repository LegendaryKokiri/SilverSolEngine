package silverSol.engine.render.opengl.uniform;

import org.lwjgl.opengl.GL20;

public class UniformBoolean extends Uniform {

	public UniformBoolean(int programID, String uniformName) {
		super(programID, uniformName);
	}
	
	public UniformBoolean(int programID, String uniformName, int arrayLength) {
		super(programID, uniformName, arrayLength);
	}

	public void loadBoolean(boolean value) {
		GL20.glUniform1i(location[0], value ? 1 : 0);
	}
	
	public void loadBoolean(boolean value, int arrayIndex) {
		GL20.glUniform1i(location[arrayIndex], value ? 1 : 0);
	}
	
	public void loadBooleans(boolean[] values) {
		for(int i = 0; i < values.length; i++) {
			GL20.glUniform1i(location[i], values[i] ? 1 : 0);
		}
	}

}
