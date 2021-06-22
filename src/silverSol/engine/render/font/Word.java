package silverSol.engine.render.font;

import java.util.ArrayList;
import java.util.List;

public class Word {

	private List<Character> characters;
	private float screenspaceWidth;
	private float fontSize;
	
	public Word(float fontSize) {
		this.characters = new ArrayList<>();
		this.screenspaceWidth = 0;
		this.fontSize = fontSize;
	}
	
	public void addCharacter(Character character){
		characters.add(character);
		screenspaceWidth += character.getXAdvancement() * fontSize;
	}
	
	public List<Character> getCharacters() {
		return characters;
	}
	
	public float getScreenspaceWidth() {
		return screenspaceWidth;
	}
}
