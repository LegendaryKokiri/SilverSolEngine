package silverSol.engine.render.font;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import silverSol.engine.render.gui.guis.TextGui;
import silverSol.engine.render.model.Model;
import silverSol.parsers.model.ModelParser;

public class TextModelBuilder {
	
	private static final float LINE_HEIGHT = 0.03f;
	private static final int SPACE_ASCII = 32;
	
	public static Model createTextModel(TextGui textGui) {
		List<Line> lines = buildLines(textGui);
		return createQuad(textGui, lines);
	}
	
	private static List<Line> buildLines(TextGui textGui) {
		Font font = textGui.getFont();
		
		char[] chars = textGui.getText().toCharArray();
		List<Line> lines = new ArrayList<>();
		Line currentLine = new Line(font.getSpaceWidth(), textGui.getFontSize(), textGui.getMaxLineSize());
		Word currentWord = new Word(textGui.getFontSize());
				
		for(char c : chars) {
			int ascii = (int) c;
			if(ascii == SPACE_ASCII) {
				currentLine = addWordToLine(currentWord, lines, currentLine, textGui, false);
				currentWord = new Word(textGui.getFontSize());
			} else {
				currentWord.addCharacter(font.getCharacter(ascii));
			}
		}
		
		addWordToLine(currentWord, lines, currentLine, textGui, true);
		return lines;
	}
	
	private static Line addWordToLine(Word wordToAdd, List<Line> lines, Line currentLine, TextGui textGui, boolean finalWord) {
		boolean wordAdded = currentLine.attemptToAddWord(wordToAdd);
		Line lineToReturn = currentLine;
		
		if(!wordAdded) {
			lines.add(currentLine);
			lineToReturn = startNewLine(textGui.getFont(), textGui, wordToAdd);
		}
		
		if(finalWord) {
			lines.add(lineToReturn);
		}
		
		return lineToReturn;
	}
	
	private static Line startNewLine(Font font, TextGui textGui, Word firstWord) {
		Line newLine = new Line(font.getSpaceWidth(), textGui.getFontSize(), textGui.getMaxLineSize());
		newLine.forciblyAddWord(firstWord);
		return newLine;
	}
	
	private static Model createQuad(TextGui textGui, List<Line> lines) {
		textGui.setNumberOfLines(lines.size());
		
		float cursorX = 0f, cursorY = 0f;
		List<Float> vertices = new ArrayList<>(), textureCoordinates = new ArrayList<>();
		
		float fontSize = textGui.getFontSize();
		for(Line line : lines) {
			if(textGui.isCentered()) {
				cursorX = (line.getMaxScreenspaceWidth() - line.getCurrentScreenspaceWidth()) / 2f;
			}
			
			for(Word word : line.getWords()) {
				for(Character character : word.getCharacters()) {
					addCharacterVertices(vertices, character, cursorX, cursorY, fontSize);
					addData(textureCoordinates, character.getMinTextureCoordinates(), character.getMaxTextureCoordinates());
					cursorX += character.getXAdvancement() * fontSize;						
				}
				
				cursorX += textGui.getFont().getSpaceWidth() * fontSize;
			}
			
			cursorX = 0;
			cursorY += LINE_HEIGHT * fontSize;
		}
		
		float[] vertexArray = new float[vertices.size()];
		float[] textureCoordinateArray = new float[textureCoordinates.size()];
		
		for(int i = 0; i < vertices.size(); i++) {
			vertexArray[i] = vertices.get(i);
		}
		
		for(int i = 0; i < textureCoordinates.size(); i++) {
			textureCoordinateArray[i] = textureCoordinates.get(i);
		}
		
		return ModelParser.create2dModel(vertexArray.length / 2, null, vertexArray, textureCoordinateArray, null);
	}
	
	private static void addCharacterVertices(List<Float> currentVertexList, Character character, float cursorX, float cursorY, float fontSize) {
		float x = cursorX + character.getOffset().x * fontSize;
		float y = cursorY + character.getOffset().y * fontSize;
		float maxX = x + character.getScreenspaceSize().x * fontSize;
		float maxY = y + character.getScreenspaceSize().y * fontSize;
		
		x = x * 2f - 1f;
		y = y * -2f + 1f;
		maxX = maxX * 2f - 1f;
		maxY = maxY * -2f + 1f;
		
		addData(currentVertexList, x, y, maxX, maxY);
	}
	
	private static void addData(List<Float> currentVboData, Vector2f min, Vector2f max) {
		addData(currentVboData, min.x, min.y, max.x, max.y);
	}
	
	private static void addData(List<Float> currentVboData, float x, float y, float maxX, float maxY) {
		currentVboData.add(x);
		currentVboData.add(y);
		currentVboData.add(x);
		currentVboData.add(maxY);
		currentVboData.add(maxX);
		currentVboData.add(maxY);
		currentVboData.add(maxX);
		currentVboData.add(maxY);
		currentVboData.add(maxX);
		currentVboData.add(y);
		currentVboData.add(x);
		currentVboData.add(y);
	}
	
}
