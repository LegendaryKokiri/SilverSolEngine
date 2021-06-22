package silverSol.parsers.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Vector3f;

import silverSol.engine.physics.d3.collider.volume.Hull;
import silverSol.engine.physics.d3.collider.volume.Planar;
import silverSol.engine.physics.d3.collider.volume.Volume.Type;
import silverSol.engine.render.armature.Bone;
import silverSol.engine.render.model.Model;
import silverSol.engine.render.opengl.object.Vao;
import silverSol.engine.render.opengl.object.Vbo;
import silverSol.engine.render.texture.Texture;
import silverSol.math.MatrixMath;
import silverSol.math.VectorMath;

public class ModelParser {
	
	public static final int NUM_ATTRIBUTES = 8;
	public static final int INDICES = 0;
	public static final int OBJECT_INDICES = 1;
	public static final int TEXTURE_INDICES = 2;
	public static final int VERTICES = 3;
	public static final int TEXTURE_COORDINATES = 4;
	public static final int NORMALS = 5;
	public static final int BONE_INDICES = 6;
	public static final int BONE_WEIGHTS = 7;
	
	//We use "inactiveAttributes" instead of "activeAttributes" because booleans are false by default
	private static final boolean[] inactiveAttributes = new boolean[NUM_ATTRIBUTES];
	
	//TODO: The only reason this exists is to interface with the Area Editor.
	//As it turns out, it's in turn only used in the Area Editor to get textures, not models.
	//Remove this entirely. It belongs in the Area Editor, not the engine.
	private static String rootDirectory = "";
	
	public static String getRootDirectory() {
		return rootDirectory;
	}
	
	public static void setRootDirectory(String directory) {
		rootDirectory = directory;
	}
	
	public static Model parseModel(String ssmFilePath, float scale) {
		try {
			InputStream in = ModelParser.class.getResourceAsStream(ssmFilePath);
			return parseModel(new BufferedReader(new InputStreamReader(in)), scale);
		} catch(IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static Model parseModel(File ssmFile, float scale) {
		try {
			FileReader reader = new FileReader(ssmFile);
			return parseModel(new BufferedReader(reader), scale);
		} catch(IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static Model parseModel(String ssmFilePath, float scale, String... textureFilePaths) throws IOException {
		try {
			InputStream in = ModelParser.class.getResourceAsStream(ssmFilePath);
			Model model = parseModel(new BufferedReader(new InputStreamReader(in)), scale);
			addTextures(model, textureFilePaths);
			return model;
		} catch(IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private static Model parseModel(BufferedReader reader, float scale) throws IOException {
		boolean hasObjectIndices = false;
			float[] objectIndices = null;
		
		boolean hasTextureIndices = false;
			float[] textureIndices = null;
		
		boolean hasVertices = false;
			float[] vertices = null;
		
		boolean hasTextureCoordinates = false;
			float[] textureCoordinates = null;
		
		boolean hasNormals = false;
			float[] normals = null;
		
		boolean hasIndices = false;
			int[] indices = null;
			
		int textureAtlasDimensions = 1;
		List<Texture> textures = new ArrayList<>();
		
		boolean hasArmature = false;
			Bone armature = null;
		
		boolean hasBoneIndices = false;
			int[] boneIndices = null;
		
		boolean hasBoneWeights = false;
			float[] boneWeights = null;
		
		String line;
		String boneNameLine = null, boneIndicesLine = null;
		while((line = reader.readLine()) != null) {
			if(line.startsWith("ObjectIndices: ") && !inactiveAttributes[OBJECT_INDICES]) {
				hasObjectIndices = true;
				objectIndices = fillFloatArray(line, 1);
			} else if(line.startsWith("TextureIndices: ") && !inactiveAttributes[TEXTURE_INDICES]) {
				hasTextureIndices = true;
				textureIndices = fillFloatArray(line, 1);
			} else if(line.startsWith("Vertices: ") && !inactiveAttributes[VERTICES]) {
				hasVertices = true;
				vertices = fillFloatArray(line, 1, scale);
			} else if(line.startsWith("TextureCoordinates: ") && !inactiveAttributes[TEXTURE_COORDINATES]) {
				hasTextureCoordinates = true;
				textureCoordinates = fillFloatArray(line, 1);	
			} else if(line.startsWith("Normals: ") && !inactiveAttributes[NORMALS]) {
				hasNormals = true;
				normals = fillFloatArray(line, 1);		
			} else if(line.startsWith("Indices: ") && !inactiveAttributes[INDICES]) {
				hasIndices = true;
				indices = fillIntArray(line, 1);
			} else if(line.startsWith("AtlasDimension: ")) {
				textureAtlasDimensions = Integer.parseInt(line.split("\\s+")[1]);
			} else if(line.startsWith("Texture: ")){
				Texture texture = null;
				
				//If the root directory is empty, then we're searching in the classpath.
				//Otherwise, we need to denote that we are searching for a specific file at a specific path.
				if(rootDirectory.isEmpty()) texture = new Texture(line.split("\\s+", 2)[1]);
				else texture = new Texture(new File(rootDirectory + line.split("\\s+", 2)[1]));
				
				texture.setNumberOfRows(textureAtlasDimensions);
				textures.add(texture);
			} else if(line.startsWith("Bone Names: ")) {
				boneNameLine = line;
				if(boneNameLine != null && boneIndicesLine != null && armature == null) {
					hasArmature = true;
					armature = ArmatureParser.parseArmature(boneNameLine, boneIndicesLine);
				}
			} else if(line.startsWith("Bone Indices: ")) {
				boneIndicesLine = line;
				if(boneNameLine != null && boneIndicesLine != null && armature == null) {
					hasArmature = true;
					armature = ArmatureParser.parseArmature(boneNameLine, boneIndicesLine);
				}
			} else if(line.startsWith("Bone VBO Indices: ") && !inactiveAttributes[BONE_INDICES]) {
				hasBoneIndices = true;
				boneIndices = fillIntArray(line, 3);
			} else if(line.startsWith("Bone Weights: ") && !inactiveAttributes[BONE_WEIGHTS]) {
				hasBoneWeights = true;
				boneWeights = fillFloatArray(line, 2);
			} else if(line.equals("BIND MODEL SPACE POSITIONS")) {
				AnimationParser.parseAnimations(reader, armature, line);
			}
		}
		
		Vao vao = new Vao(0);
		
		if(hasIndices) vao.setVertexCount(indices.length);
		else vao.setVertexCount(vertices.length / 3);
		
		vao.bind();
		
		if(hasIndices) addIndicesTo(vao, indices);
				
		int attributeNumber = 0;
				
		if(hasObjectIndices) addVboTo(vao, objectIndices, attributeNumber++, 1, false, 0, 0);
		if(hasTextureIndices) addVboTo(vao, textureIndices, attributeNumber++, 1, false, 0, 0);
		if(hasVertices) addVboTo(vao, vertices, attributeNumber++, 3, false, 0, 0);
		if(hasTextureCoordinates) addVboTo(vao, textureCoordinates, attributeNumber++, 2, false, 0, 0);
		if(hasNormals) addVboTo(vao, normals, attributeNumber++, 3, false, 0, 0);
		if(hasBoneIndices) addVboTo(vao, boneIndices, attributeNumber++, 3, false, 0, 0);
		if(hasBoneWeights) addVboTo(vao, boneWeights, attributeNumber++, 3, false, 0, 0);
				
		vao.unbind();
		
		Model model = new Model(vao);
		model.calculateAttributes(vertices);
		if(hasArmature) model.addArmature(armature);
		
		for(Texture texture : textures) {
			model.addTexture(texture);
		}
		
		return model;
	}
	
	public static Hull parseHull(String ssmFilePath, float scale, Type collisionType, Object colliderData) {
		try {
			InputStream in = ModelParser.class.getResourceAsStream(ssmFilePath);
			return parseHull(new BufferedReader(new InputStreamReader(in)), scale, collisionType, colliderData);
		} catch(IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static Hull parseHull(File ssmFile, float scale, Type collisionType, Object colliderData) {
		try {
			FileReader reader = new FileReader(ssmFile);
			return parseHull(new BufferedReader(reader), scale, collisionType, colliderData);
		} catch(IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static Planar[] parseMesh(String ssmFilePath, float scale, Type collisionType, Object colliderData) {
		try {
			InputStream in = ModelParser.class.getResourceAsStream(ssmFilePath);
			return parseMesh(new BufferedReader(new InputStreamReader(in)), scale, collisionType, colliderData);
		} catch(IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static Planar[] parseMesh(File ssmFile, float scale, Type collisionType, Object colliderData) {
		try {
			FileReader reader = new FileReader(ssmFile);
			return parseMesh(new BufferedReader(reader), scale, collisionType, colliderData);
		} catch(IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private static Hull parseHull(BufferedReader reader, float scale, Type collisionType, Object colliderData) throws IOException {
		Object[] modelData = parseCollider(reader, scale);
		if(modelData[1] == null) {
			System.err.println("ModelParser could not construct a Collider. The passed model must have indexed vertices.");
			return null;
		}
		
		return new Hull((Vector3f[]) modelData[0], (int[]) modelData[1], collisionType, colliderData);
	}
	
	private static Planar[] parseMesh(BufferedReader reader, float scale, Type collisionType, Object colliderData) throws IOException {
		Object[] modelData = parseCollider(reader, scale);
		Vector3f[] vertices = (Vector3f[]) modelData[0];
		int[] indices = (int[]) modelData[1];
		
		return indices != null ? parseIndexedMesh(vertices, indices, collisionType, colliderData)
				: parseUnindexedMesh(vertices, collisionType, colliderData);
	}
	
	private static Planar[] parseUnindexedMesh(Vector3f[] vertices, Type collisionType, Object colliderData) {
		Planar[] faces = new Planar[vertices.length / 3];
		
		for(int i = 0; i + 2 < vertices.length; i += 3) {
			Vector3f[] face = new Vector3f[]{vertices[i], vertices[i + 1], vertices[i + 2]};
			Vector3f centroid = VectorMath.mean(face);
			
			//face[j] is deliberately set to the output rather than passed in as dest to avoid altering mesh vertices
			for(int j = 0; j < 3; j++) face[j] = Vector3f.sub(face[j], centroid, null);
			
			Planar p = new Planar(face, collisionType, colliderData);
			p.setBodyOffset(MatrixMath.createTransformation(centroid, new Quaternion(0f, 0f, 0f, 1f)));
			faces[i / 3] = p;
		}
		
		return faces;
	}
	
	private static Planar[] parseIndexedMesh(Vector3f[] vertices, int[] indices, Type collisionType, Object colliderData) {
		Planar[] faces = new Planar[indices.length / 3];
		
		for(int i = 0; i + 2 < indices.length; i += 3) {
			Vector3f[] face = new Vector3f[]{vertices[indices[i]], vertices[indices[i + 1]], vertices[indices[i + 2]]};
			Vector3f centroid = VectorMath.mean(face);
			
			//face[j] is deliberately set to the output rather than passed in as dest to avoid altering mesh vertices
			for(int j = 0; j < 3; j++) face[j] = Vector3f.sub(face[j], centroid, null);
			
			Planar p = new Planar(face, collisionType, colliderData);
			p.setBodyOffset(MatrixMath.createTransformation(centroid, new Quaternion(0f, 0f, 0f, 1f)));
			faces[i / 3] = p;
		}
		
		return faces;
	}
	
	private static Object[] parseCollider(BufferedReader reader, float scale) throws IOException {
		Object[] arrays = new Object[2];
		
		boolean hasVertices = false;
			float[] vertices = null;
		
		int[] indices = null;
		
		String line;
		while((line = reader.readLine()) != null) {
			if(line.startsWith("Vertices: ") && !inactiveAttributes[VERTICES]) {
				hasVertices = true;
				vertices = fillFloatArray(line, 1, scale);
			} else if(line.startsWith("Indices: ") && !inactiveAttributes[INDICES]) {
				indices = fillIntArray(line, 1);
			}
		}
		
		if(!hasVertices) {
			System.err.println("ModelParser could not construct a Collider. The passed model must have a vertices attribute.");
			return null;
		}
				
		Vector3f[] vertices3f = new Vector3f[vertices.length / 3];
		for(int i = 0; i + 2 < vertices.length; i += 3) {
			vertices3f[i / 3] = new Vector3f(vertices[i], vertices[i + 1], vertices[i + 2]);
		}
		
		arrays[0] = vertices3f;
		arrays[1] = indices;
		return arrays;
	}
	
	public static Model create2dModel(int vertexCount, int[] objectIndices, float[] vertices, float[] textureCoordinates,
			int[] indices) {
		Vao vao = new Vao(vertexCount);		
		int attributeNumber = 0;
		
		vao.bind();
		
		if(indices != null && indices.length > 0) {
			addIndicesTo(vao, indices);
		}
		
		//TODO: Replace all of these calls with addVboTo() calls
		if(objectIndices != null && objectIndices.length > 0) {
			Vbo objectIndicesVbo = new Vbo();
			objectIndicesVbo.storeAttribute(objectIndices, attributeNumber++, 1);
			vao.addAttribute(objectIndicesVbo, false, 0, 0);
		}
		
		if(vertices != null && vertices.length > 0) {
			Vbo vertexVbo = new Vbo();
			vertexVbo.storeAttribute(vertices, attributeNumber++, 2);
			vao.addAttribute(vertexVbo, false, 0, 0);
		}
		
		if(textureCoordinates != null && textureCoordinates.length > 0) {
			Vbo textureCoordinateVbo = new Vbo();
			textureCoordinateVbo.storeAttribute(textureCoordinates, attributeNumber++, 2);
			vao.addAttribute(textureCoordinateVbo, false, 0, 0);
		}
		
		vao.unbind();
		
		Model model = new Model(vao);
		
		if(vertices != null) model.calculateAttributes(vertices);
		
		return model;
	}
	
	//TODO: This class needs to be redefined in a far more general sense.
	/*TODO: Replace all of the attribute parameters with generalized ones (a new class will likely be required)
	 * that stores the attribute data, whether or not it is float data or integer data, and the coordinate size.*/
	//TODO: Once that is done, you can likely delete create2dModel() above.
	public static Model create3dModel(int vertexCount, int[] objectIndices, float[] vertices, float[] textureCoordinates, float[] normals, int[] indices) {
		Vao vao = new Vao(vertexCount);		
		int attributeNumber = 0;
		
		vao.bind();
		
		if(indices != null && indices.length > 0) {
			Vbo indexVbo = new Vbo();
			indexVbo.storeIndexData(indices);	
			vao.addIndexVbo(indexVbo);
		}
		
		if(objectIndices != null && objectIndices.length > 0) {
			Vbo objectIndicesVbo = new Vbo();
			objectIndicesVbo.storeAttribute(objectIndices, attributeNumber++, 1);
			vao.addAttribute(objectIndicesVbo, false, 0, 0);
		}
		
		if(vertices != null && vertices.length > 0) {
			Vbo vertexVbo = new Vbo();
			vertexVbo.storeAttribute(vertices, attributeNumber++, 3);
			vao.addAttribute(vertexVbo, false, 0, 0);
		}
		
		if(textureCoordinates != null && textureCoordinates.length > 0) {
			Vbo textureCoordinateVbo = new Vbo();
			textureCoordinateVbo.storeAttribute(textureCoordinates, attributeNumber++, 2);
			vao.addAttribute(textureCoordinateVbo, false, 0, 0);
		}
		
		if(normals != null && normals.length > 0) {
			Vbo normalVbo = new Vbo();
			normalVbo.storeAttribute(normals, attributeNumber++, 3);
			vao.addAttribute(normalVbo, false, 0, 0);
		}
		
		vao.unbind();
		
		Model model = new Model(vao);
		
		if(vertices != null) model.calculateAttributes(vertices);
		
		return model;
	}
	
	private static void addVboTo(Vao vao, float[] data, int attributeNumber, int dimensions,
			boolean normalizedData, int stride, int offset) {
		Vbo vbo = new Vbo();
		vbo.storeAttribute(data, attributeNumber, dimensions);
		vao.addAttribute(vbo, normalizedData, stride, offset);
	}
	
	private static void addIndicesTo(Vao vao, int[] indices){
		Vbo indexVbo = new Vbo();
		indexVbo.storeIndexData(indices);	
		vao.addIndexVbo(indexVbo);
	}
	
	private static void addVboTo(Vao vao, int[] data, int attributeNumber, int dimensions,
			boolean normalizedData, int stride, int offset) {
		Vbo vbo = new Vbo();
		vbo.storeAttribute(data, attributeNumber, dimensions);
		vao.addAttribute(vbo, normalizedData, stride, offset);
	}
	
	private static int[] fillIntArray(String line, int startingIndex) {
		String[] stringArray = line.split("\\s+");
		int[] array = new int[stringArray.length - startingIndex];
		
		for(int i = startingIndex; i < stringArray.length; i++) {
			array[i - startingIndex] = Integer.parseInt(stringArray[i]);
		}
		
		return array;
	}
	
	private static float[] fillFloatArray(String line, int startingIndex) {
		String[] stringArray = line.split("\\s+");
		float[] array = new float[stringArray.length - startingIndex];
		
		for(int i = startingIndex; i < stringArray.length; i++) {
			array[i - startingIndex] = Float.parseFloat(stringArray[i]);
		}
		
		return array;
	}
	
	private static float[] fillFloatArray(String line, int startingIndex, float scale) {
		String[] stringArray = line.split("\\s+");
		float[] array = new float[stringArray.length - startingIndex];
		
		for(int i = startingIndex; i < stringArray.length; i++) {
			array[i - startingIndex] = Float.parseFloat(stringArray[i]) * scale;
		}
		
		return array;
	}
	
	private static void addTextures(Model model, String... textureFilePaths) {
		Texture[] textures = new Texture[textureFilePaths.length];
		for(int i = 0; i < textureFilePaths.length; i++) {
			textures[i] = new Texture(textureFilePaths[i]);
		}
		model.addTextures(textures);
	}
	
	public static void activateAttributes(int... attributesToActivate) {
		for(int i = 0; i < inactiveAttributes.length; i++) {
			inactiveAttributes[i] = true;
		}
		
		for(int attributeToActivate : attributesToActivate) {
			inactiveAttributes[attributeToActivate] = false;
		}
	}
}
