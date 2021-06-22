package silverSol.engine.render.font;

import java.util.HashMap;
import java.util.Map;

public class Font {

	private int textureAtlas;
	private Map<Integer, Character> characters;
	private float spaceWidth;
	
	public Font() {
		characters = new HashMap<>();
	}
	
	public void setTextureAtlas(int textureAtlas) {
		this.textureAtlas = textureAtlas;
	}
	
	public int getTextureAtlas() {
		return textureAtlas;
	}
	
	public Map<Integer, Character> getCharacters() {
		return characters;
	}
	
	public void loadCharacter(Character character) {
		characters.put(character.getAsciiID(), character);
	}

	public Character getCharacter(int asciiCode) {
		return characters.get(asciiCode);
	}
	
	public float getSpaceWidth() {
		return spaceWidth;
	}

	public void setSpaceWidth(float spaceWidth) {
		this.spaceWidth = spaceWidth;
	}
	
}
