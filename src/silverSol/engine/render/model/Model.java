package silverSol.engine.render.model;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Vector3f;

import silverSol.engine.render.armature.Bone;
import silverSol.engine.render.opengl.object.Vao;
import silverSol.engine.render.texture.Texture;

public class Model {
	
	//FILE DATA: The bare bones of the model
	protected Vao vao;
	
	//SIZE DATA: Size the model
	protected Vector3f center;
	protected Vector3f radius;
	protected Vector3f size;
	
	//TEXTURE COMPONENT
	protected boolean hasTexture;
		protected List<Texture> textures;
	
	//ARMATURE COMPONENT
	protected boolean hasArmature;
		protected Bone armature;
	
	public static enum VertexType {
		ARRAYS, ELEMENTS
	}
	
	public Model() {
		center = new Vector3f(0, 0, 0);
		radius = new Vector3f(0, 0, 0);
		size = new Vector3f(0, 0, 0);
		
		hasTexture = hasArmature = false;
		textures = new ArrayList<>();
	}
		
	public Model(Vao vao) {
		this.vao = vao;
		
		center = new Vector3f(0, 0, 0);
		radius = new Vector3f(0, 0, 0);
		size = new Vector3f(0, 0, 0);
		
		hasTexture = hasArmature = false;
		textures = new ArrayList<>();
	}
	
	public void calculateAttributes(float[] vertices) {
		float minX = Float.MAX_VALUE, maxX = Float.MIN_VALUE;
		float minY = Float.MAX_VALUE, maxY = Float.MIN_VALUE;
		float minZ = Float.MAX_VALUE, maxZ = Float.MIN_VALUE;
		
		for(int i = 0; i + 2 < vertices.length; i += 3) {
			float vertexX = vertices[i];
			float vertexY = vertices[i + 1];
			float vertexZ = vertices[i + 2];
			
			minX = Math.min(minX, vertexX); maxX = Math.max(maxX, vertexX);
			minY = Math.min(minY, vertexY); maxY = Math.max(maxY, vertexY);
			minZ = Math.min(minZ, vertexZ); maxZ = Math.max(maxZ, vertexZ);
		}
		
		center.set((minX + maxX) / 2f, (minY + maxY) / 2f, (minZ + maxZ) / 2f);
		size.set(Math.abs(maxX - minX), Math.abs(maxY - minY), Math.abs(maxZ - minZ));
		radius.set(size.x / 2f, size.y / 2f, size.z / 2f);
	}
	
	public Vao getVao() {
		return vao;
	}
	
	public void setVao(Vao vao) {
		deleteVao();
		this.vao = vao;
	}
	
	public Vector3f getCenter() {
		return center;
	}
	
	public Vector3f getRadius() {
		return radius;
	}
	
	public Vector3f getSize() {
		return size;
	}
	
	public boolean hasTexture() {
		return hasTexture;
	}
	
	public void addTexture(Texture texture) {
		if(texture == null) return;
		textures.add(texture);
		hasTexture = true;
	}
	
	public void addTextures(Texture... textures) {
		for(Texture texture : textures) {
			addTexture(texture);
		}
	}
	
	public List<Texture> getTextures() {
		return textures;
	}
	
	public boolean hasArmature() {
		return hasArmature;
	}
	
	public void addArmature(Bone armature) {		
		if(armature == null) return;
		this.hasArmature = true;
		this.armature = armature;
	}
	
	public Bone getArmature() {
		return armature;
	}
	
	public void delete() {
		deleteTextures();
		deleteVao();
	}
	
	public void deleteVao() {
		if(vao != null) vao.delete();
		vao = null;
	}
	
	public void deleteTextures() {
		for(Texture texture : textures) {
			texture.delete();
		}
		
		textures.clear();
	}
}
