package silverSol.engine.render.opengl.uniform;

import org.lwjgl.opengl.GL20;

public class UniformSampler2D extends Uniform {

	public UniformSampler2D(int programID, String uniformName) {
		super(programID, uniformName);
	}
	
	public UniformSampler2D(int programID, String uniformName, int arrayLength) {
		super(programID, uniformName, arrayLength);
		for(int i = 0; i < arrayLength; i++) {
			this.location[i] = GL20.glGetUniformLocation(programID, uniformName + i);
			if(location[i] == -1) System.err.println("Uniform variable " + uniformName + i + " was not found in the given shader program. It is possible that it was initialized or defined but not used in a shader calculation.");
		}
	}

	public void connectTextureUnit(int unit) {
		GL20.glUniform1i(location[0], unit);
	}
	
	public void connectTextureUnit(int unit, int arrayIndex) {
		GL20.glUniform1i(location[arrayIndex], unit);
	}
	
	public void connectTextureUnits(int[] units) {
		for(int i = 0; i < units.length; i++) {
			GL20.glUniform1i(location[i], units[i]);
		}
	}

}
