package silverSol.engine.render.opengl.uniform;

import org.lwjgl.opengl.GL20;

public class UniformSamplerCube extends Uniform {

	public UniformSamplerCube(int programID, String uniformName) {
		super(programID, uniformName);
	}
	
	public UniformSamplerCube(int programID, String uniformName, int arrayLength) {
		super(programID, uniformName, arrayLength);
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
