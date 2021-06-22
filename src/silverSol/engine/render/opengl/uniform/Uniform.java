package silverSol.engine.render.opengl.uniform;

import org.lwjgl.opengl.GL20;

public abstract class Uniform {

	protected int programID;
	private String name;
	
	protected int[] location;
	
	public Uniform(int programID, String uniformName) {
		this.programID = programID;
		this.name = uniformName;
		
		this.location = new int[1];
		this.location[0] = GL20.glGetUniformLocation(programID, uniformName);
		if(location[0] == -1) {
			System.err.println("Uniform variable " + uniformName + " was not found in the given shader program. It is possible that it was initialized or defined but not used in a shader calculation.");
		}
	}
	
	public Uniform(int programID, String uniformName, int arrayLength) {
		this.name = uniformName;
		this.location = new int[arrayLength];
		for(int i = 0; i < arrayLength; i++) {
			this.location[i] = GL20.glGetUniformLocation(programID, uniformName + "[" + i + "]");
			if(location[i] == -1 && !(this instanceof UniformSampler2D)) System.err.println("Uniform variable " + uniformName + "[" + i + "] was not found in the given shader program. It is possible that it was initialized or defined but not used in a shader calculation.");
		}
	}
	
	public String getName() {
		return name;
	}
	
	public int getLocation() {
		return location[0];
	}
	
	public int getLocation(int arrayIndex) {
		return location[arrayIndex];
	}
	
	@Override
	public String toString() {
		return "Uniform " + name + " at location " + getLocation();
	}
	
}
