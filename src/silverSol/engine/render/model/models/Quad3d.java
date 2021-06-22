package silverSol.engine.render.model.models;

import java.util.ArrayList;

import org.lwjgl.util.vector.Vector3f;

import silverSol.engine.render.model.Model;
import silverSol.engine.render.opengl.object.Vao;
import silverSol.engine.render.opengl.object.Vbo;

public class Quad3d extends Model {
	
	public Quad3d(VertexType type, boolean addTextureCoordinates, boolean addNormals, float width, float height, int glDrawType) {
		switch(type) {
			case ARRAYS:
				initArrays(addTextureCoordinates, addNormals, width, height, glDrawType);
			case ELEMENTS:
				initElements(addTextureCoordinates, addNormals, width, height, glDrawType);
		}
	}
	
	private void initArrays(boolean addTextureCoordinates, boolean addNormals, float width, float height, int glDrawType) {
		this.vao = new Vao(6);
		vao.bind();
		
		float x = width / 2f, y = height / 2f;
		
		int attributeNumber = 0;
		
		Vbo vertexVbo = new Vbo();
		vertexVbo.setDrawType(glDrawType);
		vertexVbo.storeAttribute(new float[]{x, y, 0f, -x, y, 0f, -x, -y, 0f, -x, -y, 0f, x, -y, 0f, x, y, 0f},
				attributeNumber++, 3);
		vao.addAttribute(vertexVbo, false, 0, 0);
		
		if(addTextureCoordinates) {
			Vbo textureCoordinateVbo = new Vbo();
			textureCoordinateVbo.setDrawType(glDrawType);
			textureCoordinateVbo.storeAttribute(new float[]{1f, 1f, 0f, 1f, 0f, 0f, 0f, 0f, 1f, 0f, 1f, 1f},
					attributeNumber++, 2);
			vao.addAttribute(textureCoordinateVbo, false, 0, 0);
		}
		
		if(addNormals) {
			Vbo normalVbo = new Vbo();
			normalVbo.setDrawType(glDrawType);
			normalVbo.storeAttribute(new float[]{0f, 0f, 1f, 0f, 0f, 1f, 0f, 0f, 1f, 0f, 0f, 1f, 0f, 0f, 1f, 0f, 0f, 1f},
					attributeNumber++, 3);
			vao.addAttribute(normalVbo, false, 0, 0);
		}
				
		vao.unbind();
		
		center = new Vector3f(0f, 0f, 0f);
		size = new Vector3f(2f * x, 0, 2f * y);
		radius = new Vector3f(x, 0, y);

		hasTexture = hasArmature = false;
		textures = new ArrayList<>();
	}

	private void initElements(boolean addTextureCoordinates, boolean addNormals, float width, float height, int glDrawType) {
		this.vao = new Vao(6);
		vao.bind();
		
		float x = width / 2f, y = height / 2f;
		
		Vbo indexVbo = new Vbo();
		indexVbo.setDrawType(glDrawType);
		indexVbo.storeIndexData(new int[]{0, 1, 2, 2, 3, 0});	
		vao.addIndexVbo(indexVbo);
		
		int attributeNumber = 0;
		
		Vbo vertexVbo = new Vbo();
		vertexVbo.setDrawType(glDrawType);
		vertexVbo.storeAttribute(new float[]{x, y, 0f, -x, y, 0f, -x, -y, 0f, x, -y, 0f}, attributeNumber++, 3);
		vao.addAttribute(vertexVbo, false, 0, 0);
		
		if(addTextureCoordinates) {
			Vbo textureCoordinateVbo = new Vbo();
			textureCoordinateVbo.setDrawType(glDrawType);
			textureCoordinateVbo.storeAttribute(new float[]{1f, 1f, 0f, 1f, 0f, 0f, 1f, 0f}, attributeNumber++, 2);
			vao.addAttribute(textureCoordinateVbo, false, 0, 0);
		}
		
		if(addNormals) {
			Vbo normalVbo = new Vbo();
			normalVbo.setDrawType(glDrawType);
			normalVbo.storeAttribute(new float[]{0f, 0f, 1f, 0f, 0f, 1f, 0f, 0f, 1f, 0f, 0f, 1f}, attributeNumber++, 3);
			vao.addAttribute(normalVbo, false, 0, 0);
		}
				
		vao.unbind();
		
		center = new Vector3f(0f, 0f, 0f);
		size = new Vector3f(2f * x, 0, 2f * y);
		radius = new Vector3f(x, 0, y);

		hasTexture = hasArmature = false;
		textures = new ArrayList<>();
	}
	
}
