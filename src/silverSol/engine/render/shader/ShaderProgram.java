package silverSol.engine.render.shader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL32;

import silverSol.engine.entity.Entity;
import silverSol.engine.render.camera.Camera;
import silverSol.engine.render.opengl.attribute.Attribute;
import silverSol.engine.render.texture.Texture;
import silverSol.engine.render.texture.cubeMap.CubeMap;

public abstract class ShaderProgram<T extends Entity> {		
	protected int programID;
	
	private int vertexShaderID;
	private int fragmentShaderID;
	
	private boolean hasGeometryShader;
		private int geometryShaderID;
	
	private int numberOfAttributeBindLocations;
	
	protected Object renderSettings;
	
	public ShaderProgram(String vertexFile, String fragmentFile, Attribute... attributes) {
		vertexShaderID = loadShader(vertexFile, GL20.GL_VERTEX_SHADER);
		fragmentShaderID = loadShader(fragmentFile, GL20.GL_FRAGMENT_SHADER);
		programID = GL20.glCreateProgram();
		
		hasGeometryShader = false;
		
		numberOfAttributeBindLocations = 0;
		for(Attribute attribute : attributes) {
			attribute.setProgramID(programID);
			attribute.setBindLocation(numberOfAttributeBindLocations);
			numberOfAttributeBindLocations += attribute.getAttributeBindLength();
		}
		
		GL20.glAttachShader(programID, vertexShaderID);
		GL20.glAttachShader(programID, fragmentShaderID);
		bindAttributes(attributes);
		GL20.glLinkProgram(programID);
		GL20.glValidateProgram(programID);
	}
	
	public ShaderProgram(String vertexFile, String geometryFile, String fragmentFile, Attribute... attributes) {
		vertexShaderID = loadShader(vertexFile, GL20.GL_VERTEX_SHADER);
		geometryShaderID = loadShader(geometryFile, GL32.GL_GEOMETRY_SHADER);
		fragmentShaderID = loadShader(fragmentFile, GL20.GL_FRAGMENT_SHADER);
		programID = GL20.glCreateProgram();
		
		hasGeometryShader = true;
		
		numberOfAttributeBindLocations = 0;
		for(Attribute attribute : attributes) {
			attribute.setProgramID(programID);
			numberOfAttributeBindLocations += attribute.getAttributeBindLength();
		}
		
		GL20.glAttachShader(programID, vertexShaderID);
		GL20.glAttachShader(programID, geometryShaderID);
		GL20.glAttachShader(programID, fragmentShaderID);
		bindAttributes(attributes);
		GL20.glLinkProgram(programID);
		GL20.glValidateProgram(programID);
	}
	
	public int getProgramID() {
		return programID;
	}
	
	private void bindAttributes(Attribute... attributes) {
		for(Attribute attribute : attributes) {
			GL20.glBindAttribLocation(programID, attribute.getBindLocation(), attribute.getName());
		}
	}
	
	public void bindTextures(List<Texture> textures) {
		for(int i = 0; i < textures.size(); i++) {
			Texture texture = textures.get(i);
			GL13.glActiveTexture(GL13.GL_TEXTURE0 + i);
									
			if(texture instanceof CubeMap) GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, texture.getTextureID());
			else GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getTextureID());
		}
	}
	
	public Object getRenderSettings() {
		return renderSettings;
	}

	public void setRenderSettings(Object renderSettings) {
		this.renderSettings = renderSettings;
	}

	public void start() {
		GL20.glUseProgram(programID);
	}
	
	public void enableAttribues() {
		for(int i = 0; i < numberOfAttributeBindLocations; i++) {
			GL20.glEnableVertexAttribArray(i);
		}
	}
		
	public void disableAttribues(T entity) {
		for(int i = 0; i < numberOfAttributeBindLocations; i++) {
			GL20.glDisableVertexAttribArray(i);
		}
	}
	
	public abstract void preRender(Camera camera, List<T> entities);
	public abstract void preInstance(Camera camera, T entity, int index);
	public abstract void postInstance(Camera camera, T entity, int index);
	public abstract void postRender(Camera camera, List<T> entities);
	
	public void stop() {
		GL20.glUseProgram(0);
	} 
	
	public void cleanUp() {
		stop();
		GL20.glDetachShader(programID, vertexShaderID);
		if(hasGeometryShader) GL20.glDetachShader(programID, geometryShaderID);
		GL20.glDetachShader(programID, fragmentShaderID);
		GL20.glDeleteShader(vertexShaderID);
		if(hasGeometryShader) GL20.glDeleteShader(geometryShaderID);
		GL20.glDeleteShader(fragmentShaderID);
		GL20.glDeleteProgram(programID);
	}
	
	private static int loadShader(String file, int type) {
		StringBuilder shaderSource = new StringBuilder();
		try {
			InputStream in = ShaderProgram.class.getResourceAsStream(file);
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			String line;
			while((line = reader.readLine()) != null) {
				shaderSource.append(line).append("\n");
			}
			reader.close();
		} catch(IOException e) {
			System.err.println("Could not read the shader program file.");
			e.printStackTrace();
			System.exit(-1);
		}
		
		int shaderID = GL20.glCreateShader(type);
		GL20.glShaderSource(shaderID, shaderSource);
		GL20.glCompileShader(shaderID);
		if(GL20.glGetShaderi(shaderID, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
			System.out.println(GL20.glGetShaderInfoLog(shaderID, 500));
			System.err.println("Could not compile the shader program.");
			System.exit(-1);
		}
		
		return shaderID;
	}
	
	@Override
	public String toString() {
		return "ShaderProgram ID " + programID;
	}
}
