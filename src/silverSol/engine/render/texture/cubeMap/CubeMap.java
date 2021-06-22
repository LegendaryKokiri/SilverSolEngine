package silverSol.engine.render.texture.cubeMap;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;

import silverSol.engine.render.texture.Texture;

public class CubeMap extends Texture {
	
	public CubeMap(String rightTexturePath, String leftTexturePath, String topTexturePath, String bottomTexturePath, String backTexturePath, String frontTexturePath) {
		this.textureID = GL11.glGenTextures();
		
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		
		bind();
		
		addFace(new CubeMapFace(rightTexturePath), GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X);
		addFace(new CubeMapFace(leftTexturePath), GL13.GL_TEXTURE_CUBE_MAP_NEGATIVE_X);
		addFace(new CubeMapFace(topTexturePath), GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_Y);
		addFace(new CubeMapFace(bottomTexturePath), GL13.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y);
		addFace(new CubeMapFace(backTexturePath), GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_Z);
		addFace(new CubeMapFace(frontTexturePath), GL13.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z);
		
		GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
		
		unbind();
	}
	
	public void bind() {
		GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, textureID);
	}
	
	private void addFace(CubeMapFace texture, int whichFace) {
		GL11.glTexImage2D(whichFace, 0, GL11.GL_RGBA,
				texture.getWidth(), texture.getHeight(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, texture.getBuffer());
	}
	
	public void unbind() {
		GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, 0);
	}
	
}
