package silverSol.engine.render.font;

import java.util.ArrayList;
import java.util.List;

public class Line {

	private List<Word> words;
	
	private float currentScreenspaceWidth;
	private float screenspaceSpaceWidth;
	private float maxScreenspaceWidth;
	
	public Line(float spaceWidth, float fontSize, float maxScreenspaceWidth) {
		this.words = new ArrayList<>();
		
		this.currentScreenspaceWidth = 0;
		this.screenspaceSpaceWidth = spaceWidth * fontSize;
		this.maxScreenspaceWidth = maxScreenspaceWidth;
	}
	
	public boolean attemptToAddWord(Word word) {
		float widthToAdd = word.getScreenspaceWidth();
		widthToAdd += !words.isEmpty() ? screenspaceSpaceWidth : 0;
		if(currentScreenspaceWidth + widthToAdd <= maxScreenspaceWidth) {
			words.add(word);
			currentScreenspaceWidth += widthToAdd;
			return true;
		}
		
		return false;
	}
	
	public void forciblyAddWord(Word word) {
		float widthToAdd = word.getScreenspaceWidth();
		widthToAdd += !words.isEmpty() ? screenspaceSpaceWidth : 0;
		words.add(word);
		currentScreenspaceWidth += widthToAdd;
	}
	
	public float getMaxScreenspaceWidth() {
		return maxScreenspaceWidth;
	}

	public float getCurrentScreenspaceWidth() {
		return currentScreenspaceWidth;
	}

	public List<Word> getWords() {
		return words;
	}
	
}
