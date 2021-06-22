package silverSol.parsers.font;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector2f;

import silverSol.engine.render.font.Character;
import silverSol.engine.render.font.Font;

public class FontParser {
	
	private static final float LINE_HEIGHT = 0.03f;
	private static final int SPACE_ASCII_CODE = 32;
	
	private static int[] padding;
	private static int paddingWidth, paddingHeight;
	private static int TARGET_PADDING = 3;
	private static final int PAD_TOP_INDEX = 0;
	private static final int PAD_LEFT_INDEX = 1;
	private static final int PAD_BOTTOM_INDEX = 2;
	private static final int PAD_RIGHT_INDEX = 3;
	
	private static float aspectRatio;
	private static int imageSideLength;
	private static float pixelWidth, pixelHeight;
	
	public static Font parseFontFile(String fontFile, int targetPadding) {
		aspectRatio = (float) Display.getWidth() / (float) Display.getHeight();
		TARGET_PADDING = targetPadding;
		
		InputStream in = FontParser.class.getResourceAsStream(fontFile);
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		
		Font font = new Font();
		
		try {
			String line = reader.readLine();
			Map<String, String> dataStrings = new HashMap<>();
			
			while(line != null) {
				String[] data = line.split("\\s+");
				
				for(int i = 1; i < data.length; i++) {
					String[] stringData = data[i].split("=");
					dataStrings.put(stringData[0], stringData[1]);
				}
				
				 if(data.length > 0 && data[0].equals("char")) {
					 Character newCharacter = parseChar(font, dataStrings);
					 if(newCharacter != null) font.loadCharacter(newCharacter);
				} else if(data.length > 0 && data[0].startsWith("common")) {
					parseCommon(dataStrings);
				} else if(data.length > 0 && data[0].startsWith("info")) {
					parseInfo(dataStrings);
				}
				 
				 line = reader.readLine();
			}
			
			reader.close();
		} catch(IOException e) {
			
		}
		
		return font;
	}
	
	private static int getValueOfVariable(String variableName, Map<String, String> dataStrings) {
		return Integer.parseInt(dataStrings.get(variableName));
	}
	
	private static void parseInfo(Map<String, String> dataStrings) {
		padding = parseStringData(dataStrings.get("padding").split(","));
		paddingWidth = padding[PAD_LEFT_INDEX] + padding[PAD_RIGHT_INDEX];
		paddingHeight = padding[PAD_TOP_INDEX] + padding[PAD_BOTTOM_INDEX];
	}
	
	private static void parseCommon(Map<String, String> dataStrings) {
		int pixelLineHeight = getValueOfVariable("lineHeight", dataStrings) - paddingHeight;
		pixelHeight = LINE_HEIGHT / (float) pixelLineHeight;
		pixelWidth = pixelHeight / aspectRatio;
		imageSideLength = getValueOfVariable("scaleW", dataStrings);
	}
	
	private static Character parseChar(Font font, Map<String, String> dataStrings) {
		int ID = getValueOfVariable("id", dataStrings);
		if(ID == SPACE_ASCII_CODE) {
			font.setSpaceWidth((getValueOfVariable("xadvance", dataStrings) - paddingWidth) * pixelWidth);
			return null;
		}
		
		float xTextureCoordinate = ((float) getValueOfVariable("x", dataStrings) + (padding[PAD_LEFT_INDEX] - TARGET_PADDING)) / imageSideLength;
		float yTextureCoordinate = ((float) getValueOfVariable("y", dataStrings) + (padding[PAD_TOP_INDEX] - TARGET_PADDING)) / imageSideLength;
		int width = getValueOfVariable("width", dataStrings) - (paddingWidth - (2 * TARGET_PADDING));
		int height = getValueOfVariable("height", dataStrings) - (paddingHeight - (2 * TARGET_PADDING));
		float quadWidth = width * pixelWidth;
		float quadHeight = height * pixelHeight;
		float xTextureAtlasSize = (float) width / imageSideLength;
		float yTextureAtlasSize = (float) height / imageSideLength;
		float xOffset = (getValueOfVariable("xoffset", dataStrings) + padding[PAD_LEFT_INDEX] - TARGET_PADDING) * pixelWidth;
		float yOffset = (getValueOfVariable("yoffset", dataStrings) + padding[PAD_TOP_INDEX] - TARGET_PADDING) * pixelHeight;
		float xAdvance = (getValueOfVariable("xadvance", dataStrings) - paddingWidth) * pixelWidth;
		
		return new Character(ID, new Vector2f(xTextureCoordinate, yTextureCoordinate),
				new Vector2f(xTextureAtlasSize, yTextureAtlasSize), new Vector2f(xOffset, yOffset), new Vector2f(quadWidth, quadHeight), xAdvance);
	}
	
	private static int[] parseStringData(String[] strings) {
		int[] data = new int[strings.length];
		for(int i = 0; i < strings.length; i++) {
			data[i] = Integer.parseInt(strings[i]);
		}
		
		return data;
	}
	
}
