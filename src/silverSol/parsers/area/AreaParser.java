package silverSol.parsers.area;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Vector3f;

import silverSol.engine.entity.Entity;
import silverSol.engine.entity.terrain.HeightMapTerrain;
import silverSol.engine.entity.terrain.Terrain;
import silverSol.engine.physics.d3.body.Body;
import silverSol.engine.render.model.Model;
import silverSol.engine.render.texture.Texture;
import silverSol.game.area.Area;
import silverSol.math.QuaternionMath;
import silverSol.parsers.model.ModelParser;
import silverSol.parsers.xml.XmlNode;
import silverSol.parsers.xml.XmlParser;

public class AreaParser {
		
	public static Area parseAreaFile(String filePath) throws IOException {		
		XmlNode xmlNode = XmlParser.parseXmlFile(filePath);
		return parseArea(xmlNode);
	}
	
	public static Area parseAreaFile(File file) throws IOException {		
		XmlNode xmlNode = XmlParser.parseXmlFile(file);
		return parseArea(xmlNode);
	}
	
	private static Area parseArea(XmlNode areaNode) throws IOException {
		Area area = new Area();
		
		List<XmlNode> terrains = areaNode.getChildren("Terrain");
		for(XmlNode terrainNode : terrains) {
			area.addTerrains(parseTerrains(terrainNode));
		}
		
		List<XmlNode> entities = areaNode.getChildren("Entity");
		for(XmlNode entityNode : entities) {
			area.addEntities(parseEntities(entityNode));
		}
		
		return area;
	}
	
	private static List<Terrain> parseTerrains(XmlNode terrainNode) {
		List<Terrain> terrains = new ArrayList<>();
		
		String terrainPath = terrainNode.getAttribute("path");
		for(XmlNode terrainInstance : terrainNode.getChildren("TInstance")) {
			terrains.add(parseTerrain(terrainInstance, terrainPath));
		}
		
		return terrains;
	}
	
	private static Terrain parseTerrain(XmlNode terrainInstance, String terrainPath) {
		HeightMapTerrain terrain = TerrainParser.parseHeightMapTerrain(terrainPath, 40);
		
		String blendMap = "", black = "", red = "", green = "", blue = "";
		
		blendMap = terrainInstance.getChild("BlendMap").getContent();
		
		List<XmlNode> textures = terrainInstance.getChildren("TTexture");
		for(XmlNode texture : textures) {
			String color = texture.getAttribute("color");
			if(color.equals("black")) black = texture.getContent();
			else if(color.equals("red")) red = texture.getContent();
			else if(color.equals("green")) green = texture.getContent();
			else if(color.equals("blue")) blue = texture.getContent();
		}
		
		terrain.getModel().addTexture(new Texture(blendMap));
		terrain.getModel().addTexture(new Texture(black));
		terrain.getModel().addTexture(new Texture(red));
		terrain.getModel().addTexture(new Texture(green));
		terrain.getModel().addTexture(new Texture(blue));
		
		XmlNode transformation = terrainInstance.getChild("Transformation");
		parseTransformation(terrain.getBody3d(), transformation);
		
		return terrain;
	}
	
	private static List<Entity> parseEntities(XmlNode entityNode) throws IOException {
		List<Entity> entities = new ArrayList<>();
		
		String entityPath = (entityNode.getAttribute("path"));
		for(XmlNode entityInstance : entityNode.getChildren("EInstance")) {
			entities.add(parseEntity(entityInstance, entityPath));
		}
		
		return entities;
	}
	
	private static Entity parseEntity(XmlNode entityInstance, String entityPath) throws IOException {
		Model entityModel = ModelParser.parseModel(entityPath, 1f);
		
		Body body = new Body();
		body.setImmovable(true);
		XmlNode transformation = entityInstance.getChild("Transformation");
		parseTransformation(body, transformation);
	
		return new Entity(entityModel, body);
	}
	
	private static void parseTransformation(Body body, XmlNode transformationNode) {
		String[] transformation = transformationNode.getContent().split("\\s+");
		
		body.setPosition(Float.parseFloat(transformation[0]), Float.parseFloat(transformation[1]), Float.parseFloat(transformation[2]));
		body.setScale(Float.parseFloat(transformation[6]), Float.parseFloat(transformation[7]), Float.parseFloat(transformation[8]));
		
		Vector3f degrees = new Vector3f(
				Float.parseFloat(transformation[3]), Float.parseFloat(transformation[4]), Float.parseFloat(transformation[5]));
		Vector3f eulerRotation = new Vector3f(
				(float) Math.toRadians(degrees.x), (float) Math.toRadians(degrees.y), (float) Math.toRadians(degrees.z));
		body.setRotation(QuaternionMath.create(eulerRotation));
	}
	
}
