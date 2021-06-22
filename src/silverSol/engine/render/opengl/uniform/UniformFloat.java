package silverSol.engine.render.opengl.uniform;

import org.lwjgl.opengl.GL20;

public class UniformFloat extends Uniform {

	public UniformFloat(int programID, String uniformName) {
		super(programID, uniformName);
	}
	
	public UniformFloat(int programID, String uniformName, int arrayLength) {
		super(programID, uniformName, arrayLength);
	}

	public void loadFloat(float value) {
		GL20.glUniform1f(location[0], value);
	}
	
	public void loadFloat(float value, int arrayIndex) {
		GL20.glUniform1f(location[arrayIndex], value);
	}
	
	public void loadFloats(float[] values) {
		for(int i = 0; i < values.length; i++) {
			GL20.glUniform1f(location[i], values[i]);
		}
	}

}
