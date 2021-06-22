package silverSol.parsers.xml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import silverSol.math.NumberMath;

public class XmlParser {
	
	public static XmlNode parseXmlFile(File xmlFile) throws IOException {
		FileReader reader = new FileReader(xmlFile);
		return parseXmlFile(new BufferedReader(reader));
	}
	
	public static XmlNode parseXmlFile(String ssmFilePath) throws IOException {
		InputStream in = XmlParser.class.getResourceAsStream(ssmFilePath);
		return parseXmlFile(new BufferedReader(new InputStreamReader(in)));
	}
	
	public static XmlNode parseXmlFile(BufferedReader reader) throws IOException {
		XmlNode rootNode = new XmlNode();
		
		buildNodeInfrastructure(rootNode, reader, reader.readLine(), false);
		reader.close();
		
		return rootNode;
	}
	
	private static void buildNodeInfrastructure(XmlNode activeNode, BufferedReader reader, String line, boolean parsingContent) throws IOException {
		if(line == null) {
			return;
		} else if(line.isEmpty()) {
			buildNodeInfrastructure(activeNode, reader, reader.readLine(), parsingContent);
			return;
		}
		
		int dataStart = 0, dataEnd = 0;
			
		line = trimWhitespace(line);
		
		int length = line.length();
		for(int i = 0; i < length; i++) {
			if(line.charAt(i) == '<') {
				dataEnd = i;
				
				if(parsingContent) {
					//If we have found an end tag, we will handle it once we reach the end of the tag.
					if(dataStart == dataEnd && !isEndTag(line.substring(NumberMath.min(i + 1, length - 1), length))) {
						//We found a new node.
						buildNodeInfrastructure(activeNode.generateChildNode(), reader, line.substring(dataStart, length), false);
						return;
					} else {
						//We are parsing data between nodes.
						handleContent(activeNode, line, dataStart, dataEnd);
					}
				}
				
				dataStart = i + 1;
				parsingContent = false;
			} else if(line.charAt(i) == '>') {
				dataEnd = i;
				
				handleTag(activeNode, line, dataStart, dataEnd);
				
				if(isEndTag(line.substring(dataStart, dataEnd))) {
					dataStart = i + 1;
					buildNodeInfrastructure(activeNode.getParentNode(), reader, line.substring(dataStart, length), true);
					return;
				}
				
				dataStart = i + 1;
				
				parsingContent = line.charAt(i - 1) != '/';
				if(!parsingContent) {
					//We reached the end of the current node and need to go the parent.
					buildNodeInfrastructure(activeNode.getParentNode(), reader, line.substring(dataStart, length), true);
					return;
				} //else we are going to parse content between the start and end tags.
			}
		}
		
		if((line = reader.readLine()) != null) buildNodeInfrastructure(activeNode, reader, line, parsingContent);
	}
	
	private static String trimWhitespace(String line) {
		line.trim();
		
		for(int i = 0; i < line.length(); i++) {
			if(line.charAt(i) == '\t') {
				continue;
			} else {
				line = line.substring(i, line.length());
				break;
			}
		}
		
		for(int i = line.length() - 1; i >= 0; i--) {
			if(line.charAt(i) == '\t') {
				continue;
			} else {
				line = line.substring(0, i + 1);
				break;
			}
		}
		
		return line;
	}
	
	private static boolean isEndTag(String tag) {
		return (tag.charAt(0) == '/');
	}
	
	private static void handleTag(XmlNode activeNode, String line, int startCharacter, int endCharacter) {
		String tag = line.substring(startCharacter, endCharacter);
				
		boolean tagIsNamed = !activeNode.getName().isEmpty();
		boolean parsedTagName = false;
		
		boolean inQuotes = false;
		
		int length = tag.length();
		
		int dataStart = isEndTag(tag) ? 1 : 0, dataEnd = dataStart;
		for(int i = dataStart; i < length; i++) {
			char c = tag.charAt(i);
			
			if(c == '\"') {
				inQuotes = !inQuotes;
			}
			
			if(Character.isWhitespace(c) || i == length - 1) {
				if(inQuotes) continue;
				
				dataEnd = (i == length - 1) ? length : i;
				if(!parsedTagName) {
					if(!tagIsNamed) assignName(activeNode, tag, dataStart, dataEnd);
					else checkName(activeNode, tag, dataStart, dataEnd);
					
					parsedTagName = true;
				} else {
					parseAttribute(activeNode, tag, dataStart, dataEnd);	
				}
				
				dataStart = i + 1;
			}
		}
	}
	
	private static void assignName(XmlNode activeNode, String tag, int startCharacter, int endCharacter) {
		activeNode.setName(tag.substring(startCharacter, endCharacter));
	}
	
	private static void checkName(XmlNode activeNode, String tag, int startCharacter, int endCharacter) {
		String name = tag.substring(startCharacter, endCharacter);
		if(!activeNode.getName().equals(name)) System.err.println("Node " + name + " does not match up with node " + activeNode.getName() + ". Results may be unexpected.");
	}
	
	private static void handleContent(XmlNode activeNode, String tag, int startCharacter, int endCharacter) {
		String content = tag.substring(startCharacter, endCharacter);
		activeNode.setContent(content.trim());
	}
	
	private static void parseAttribute(XmlNode activeNode, String tag, int startCharacter, int endCharacter) {
		String attribute = tag.substring(startCharacter, endCharacter);
		int splitLocation = attribute.indexOf("=");
		if(splitLocation == -1) return;
		
		String[] attributeData = attribute.split("=");
		attributeData[1] = attributeData[1].replaceAll("\"", "");
		activeNode.addAttribute(new XmlAttribute(attributeData[0], attributeData[1]));
	}
	
}
