package silverSol.engine.render.font;

import org.lwjgl.util.vector.Vector2f;

public class Character {
	
	private int asciiID;
	
	private Vector2f minTextureCoordinates, maxTextureCoordinates;
	private Vector2f textureAtlasSize, screenspaceSize;
	private Vector2f offset;
	private float xAdvancement;
	
	public Character(int asciiID, Vector2f minTextureCoordinates,
			Vector2f textureAtlasSize, Vector2f offset, Vector2f screenspaceSize, float xAdvancement) {
		super();
		this.asciiID = asciiID;
		this.minTextureCoordinates = minTextureCoordinates;
		this.screenspaceSize = screenspaceSize;
		this.maxTextureCoordinates = Vector2f.add(minTextureCoordinates, textureAtlasSize, null);
		this.textureAtlasSize = textureAtlasSize;
		this.offset = offset;
		this.xAdvancement = xAdvancement;
	}

	public int getAsciiID() {
		return asciiID;
	}

	public Vector2f getMinTextureCoordinates() {
		return minTextureCoordinates;
	}

	public Vector2f getMaxTextureCoordinates() {
		return maxTextureCoordinates;
	}
	
	public Vector2f getTextureAtlasSize() {
		return textureAtlasSize;
	}

	public Vector2f getScreenspaceSize() {
		return screenspaceSize;
	}

	public Vector2f getOffset() {
		return offset;
	}

	public float getXAdvancement() {
		return xAdvancement;
	}

	
}
