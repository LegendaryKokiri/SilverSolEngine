package silverSol.parsers.area;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.lwjgl.util.vector.Vector3f;

import silverSol.engine.entity.terrain.HeightMapTerrain;
import silverSol.parsers.model.ModelParser;

public class TerrainParser {

	private static final int NO_ALPHA = (0xFF << 16) + (0xFF << 8) + 0xFF;
	private static final float MAX_PIXEL_COLOR = (float) NO_ALPHA;
	
	public static HeightMapTerrain parseHeightMapTerrain(String heightMapPath, float maxHeight) {
		InputStream in = TerrainParser.class.getResourceAsStream(heightMapPath);
		return parseHeightMapTerrain(in, maxHeight);
	}
	
	public static HeightMapTerrain parseHeightMapTerrain(File heightMap, float maxHeight) {
		try {
			FileInputStream fileInput = new FileInputStream(heightMap);
			return parseHeightMapTerrain(fileInput, maxHeight);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static HeightMapTerrain parseHeightMapTerrain(InputStream in, float maxHeight) {
		HeightMapTerrain terrain = null;
		
		BufferedImage image = null;
		try {
			image = ImageIO.read(in);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		float width = image.getWidth();
		float length = image.getHeight();
		
		int VERTEX_COUNT = image.getHeight();
		float[][] heights = new float[VERTEX_COUNT][VERTEX_COUNT];
		
		int count = VERTEX_COUNT * VERTEX_COUNT;
		float[] vertices = new float[count * 3];
		float[] normals = new float[count * 3];
		float[] textureCoords = new float[count * 2];
		int[] indices = new int[6 * (VERTEX_COUNT - 1) * (VERTEX_COUNT - 1)];
		int vertexPointer = 0;
		
		for(int i = 0; i < VERTEX_COUNT; i++){
			for(int j = 0; j < VERTEX_COUNT; j++) {
				float height = getHeight(j, i, maxHeight, image);
				heights[j][i] = height;
				
				vertices[vertexPointer * 3] = ((float) j / ((float) VERTEX_COUNT - 1)) * width - (width / 2);
				vertices[vertexPointer * 3 + 1] = height;
				vertices[vertexPointer * 3 + 2] = ((float) i / ((float) VERTEX_COUNT - 1)) * length - (length / 2);
				
				Vector3f normal = calculateNormal(j, i, maxHeight, image);
				
				normals[vertexPointer*3] = normal.x;
				normals[vertexPointer*3+1] = normal.y;
				normals[vertexPointer*3+2] = normal.z;
				
				textureCoords[vertexPointer*2] = (float)j/((float)VERTEX_COUNT - 1);
				textureCoords[vertexPointer*2+1] = (float)i/((float)VERTEX_COUNT - 1);
				vertexPointer++;
			}
		}
		
		int pointer = 0;
		for(int gridZ = 0; gridZ < VERTEX_COUNT - 1; gridZ++){
			for(int gridX = 0; gridX < VERTEX_COUNT - 1; gridX++){
				int topLeft = (gridZ * VERTEX_COUNT) + gridX;
				int topRight = topLeft + 1;
				int bottomLeft = ((gridZ + 1) * VERTEX_COUNT) + gridX;
				int bottomRight = bottomLeft + 1;
				indices[pointer++] = topLeft;
				indices[pointer++] = bottomLeft;
				indices[pointer++] = topRight;
				indices[pointer++] = topRight;
				indices[pointer++] = bottomLeft;
				indices[pointer++] = bottomRight;
			}
		}
		
		//TODO: Why on earth is maxHeight multiplied by 2? What was I trying to accomplish? Does it actually yield a terrain that ranges from heights 0 to maxHeight?
		//Since I'm deleting this on December 21, 2021, note that the original line was: terrain = new HeightMapTerrain(width, maxHeight * 2, length, heights);
		//I presume that the original rationale was to size the AABB properly for broadphase detection and resolution, stretching it both up AND down rather than changing its offset
		terrain = new HeightMapTerrain(heights);
		terrain.setModel(ModelParser.create3dModel(indices.length, null, vertices, textureCoords, normals, indices));
		
		return terrain;
	}
	
	private static float getHeight(int x, int y, float maxHeight, BufferedImage image) {
		if(x < 0 || x >= image.getHeight() || y < 0 || y >= image.getHeight()) return 0;
		return (image.getRGB(x, y) & NO_ALPHA) / MAX_PIXEL_COLOR * maxHeight;
	}
	
	private static Vector3f calculateNormal(int x, int y, float maxHeight, BufferedImage image) {
		float heightL = getHeight(x - 1, y, maxHeight, image);
		float heightR = getHeight(x + 1, y, maxHeight, image);
		float heightD = getHeight(x, y - 1, maxHeight, image);
		float heightU = getHeight(x, y + 1, maxHeight, image);
		Vector3f normal = new Vector3f(heightL - heightR, 2f, heightD - heightU).normalise(null);
		return normal;
	}
	
	public static void main(String[] args) {
		InputStream in = TerrainParser.class.getResourceAsStream("/textures/Pixel Test.png");
		BufferedImage image = null;
		try {
			image = ImageIO.read(in);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		int black = image.getRGB(0, 0);
		int white = image.getRGB(1, 0);
		
		int r = 255;
		int g = 255;
		int b = 255;
		int a = 255;
		float height = ((a << 24) + (b << 16) + (g << 8) + r) & NO_ALPHA;
		
		System.out.println("Black = " + black + " = " + Integer.toBinaryString(black));
		System.out.println("White = " + white + " = " + Integer.toBinaryString(white));
		
		height /= MAX_PIXEL_COLOR;
		System.out.println("Divided = " + height + " = " + height);
	}
	
}
