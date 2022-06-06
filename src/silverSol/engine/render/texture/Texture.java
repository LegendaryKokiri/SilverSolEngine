package silverSol.engine.render.texture;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;
import org.newdawn.slick.opengl.TextureLoader;

import silverSol.engine.render.animation.texture.TextureAnimation;

public class Texture {

	protected int textureID;
	
	protected int width, height;
	
	protected int numberOfRows;
		
	protected boolean hasAnimations;
		protected List<TextureAnimation> animations;
	
	public static enum FrameLayout {
		SINGLE, ROWS, COLUMNS
	}
	
	private float shineDamper;
	private float reflectivity;
		
	public Texture() {
		
	}
	
	public Texture(int textureID) {
		this.textureID = textureID;
	}
	
	public Texture(File textureFile) {
		try {
			FileInputStream in = new FileInputStream(textureFile);
			loadTexture(in);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public Texture(String texturePath) {
		loadTexture(Texture.class.getResourceAsStream(texturePath));		
	}
	
	
	private void loadTexture(InputStream in) {
		org.newdawn.slick.opengl.Texture texture = null;
		try {
			texture = TextureLoader.getTexture("PNG", in);
			//TODO: There are many parameters that are being hard-coded here. The Engine needs to grant user control over all of these.
			GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
			GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL14.GL_TEXTURE_LOD_BIAS, -0.4f);
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		this.textureID = texture.getTextureID();
		this.width = texture.getImageWidth();
		this.height = texture.getImageHeight();
		
		this.numberOfRows = 1;
		
		this.hasAnimations = false;
			this.animations = new ArrayList<>();
		
		this.shineDamper = 1.0f;
		this.reflectivity = 0.0f;
	}
	
	public void bind() {
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
	}
	
	public int getTextureID() {
		return textureID;
	}

	public void setTextureID(int textureID) {
		this.textureID = textureID;
	}
	
	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}
	public int getNumberOfRows() {
		return numberOfRows;
	}

	public void setNumberOfRows(int numberOfRows) {
		this.numberOfRows = numberOfRows;
	}
	
	public boolean hasAnimations() {
		return hasAnimations;
	}
	
	public List<TextureAnimation> getAnimations() {
		return animations;
	}
	
	public void addAnimation(TextureAnimation animation) {
		if(animation != null && animations != null) {
			animations.add(animation);
			this.hasAnimations = true;
		}
	}

	public void setAnimations(List<TextureAnimation> animations) {
		this.animations = animations;
		this.hasAnimations = animations != null && animations.size() > 0;
	}

	public void generateAnimations(FrameLayout frameLayout, Float... fps) {
		if(frameLayout == FrameLayout.SINGLE) generateSingleAnimation(fps);
		else if(frameLayout == FrameLayout.ROWS) generateRowAnimations(fps);
		else if(frameLayout == FrameLayout.COLUMNS) generateColumnAnimations(fps);
	}
	
	private void generateSingleAnimation(Float... fps) {
		List<TextureAnimation> animations = new ArrayList<>();
		List<float[]> frameBounds = new ArrayList<>();
		
		float rowCount = (float) numberOfRows;
		
		for(int i = 0; i < numberOfRows; i++) {
			for(int j = 0; j < numberOfRows; j++) {
				frameBounds.add(new float[] {(float) j / rowCount, (float) (i+1) / rowCount, (float) (j+1) / rowCount, (float) i / rowCount });
			}
		}
		
		animations.add(new TextureAnimation(frameBounds, fps[0]));
		
		setAnimations(animations);
	}
	
	private void generateRowAnimations(Float... fps) {
		List<TextureAnimation> animations = new ArrayList<>();
		
		float rowCount = (float) numberOfRows;
		
		for(int i = 0; i < numberOfRows; i++) {
			List<float[]> frameBounds = new ArrayList<>();
			
			for(int j = 0; j < numberOfRows; j++) {
				frameBounds.add(new float[] {(float) j / rowCount, (float) (i+1) / rowCount, (float) (j+1) / rowCount, (float) i / rowCount });
			}
			
			animations.add(new TextureAnimation(frameBounds, fps[i]));
		}
		
		setAnimations(animations);
	}
	
	private void generateColumnAnimations(Float... fps) {
		List<TextureAnimation> animations = new ArrayList<>();
		
		float rowCount = (float) numberOfRows;
		
		for(int i = 0; i < numberOfRows; i++) {
			List<float[]> frameBounds = new ArrayList<>();
			
			for(int j = 0; j < numberOfRows; j++) {
				frameBounds.add(new float[] {(float) j / rowCount, (float) (i+1) / rowCount, (float) (j+1) / rowCount, (float) i / rowCount });
			}
			
			animations.add(new TextureAnimation(frameBounds, fps[i]));
		}
		
		setAnimations(animations);
	}

	public float getShineDamper() {
		return shineDamper;
	}

	public void setShineDamper(float shineDamper) {
		this.shineDamper = shineDamper;
	}

	public float getReflectivity() {
		return reflectivity;
	}

	public void setReflectivity(float reflectivity) {
		this.reflectivity = reflectivity;
	}

	public void unbind() {
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
	}
	
	public void delete() {
		GL11.glDeleteTextures(textureID);
	}
	
	@Override
	public String toString() {
		return "Texture ID " + textureID;
	}
}
