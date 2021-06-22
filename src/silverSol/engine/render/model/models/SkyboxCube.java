package silverSol.engine.render.model.models;

import java.util.ArrayList;

import org.lwjgl.util.vector.Vector3f;

import silverSol.engine.render.model.Model;
import silverSol.engine.render.opengl.object.Vao;
import silverSol.engine.render.opengl.object.Vbo;

public class SkyboxCube extends Model {

	public SkyboxCube(float sideLength, int glDrawType) {
		this.vao = new Vao(108);
		vao.bind();
		
		Vbo indexVbo = new Vbo();
		indexVbo.setDrawType(glDrawType);
		indexVbo.storeIndexData(new int[]{
				0, 2, 6, 6, 4, 0,
				3, 2, 0, 0, 1, 3,
				6, 7, 5, 5, 4, 6,
				3, 1, 5, 5, 7, 3,
				0, 4, 5, 5, 1, 0,
				2, 3, 6, 6, 3, 7});	
		vao.addIndexVbo(indexVbo);
		
		Vbo vertexVbo = new Vbo();
		vertexVbo.setDrawType(glDrawType);
		vertexVbo.storeAttribute(new float[]{
				-sideLength, sideLength, -sideLength,
				-sideLength, sideLength, sideLength,
				-sideLength, -sideLength, -sideLength,
				-sideLength, -sideLength, sideLength,
				sideLength, sideLength, -sideLength,
				sideLength, sideLength, sideLength,
				sideLength, -sideLength, -sideLength,
				sideLength, -sideLength, sideLength}, 0, 3);
		vao.addAttribute(vertexVbo, false, 0, 0);
				
		vao.unbind();
		
		center = new Vector3f(0f, 0f, 0f);
		size = new Vector3f(sideLength, sideLength, sideLength);
		radius = new Vector3f(sideLength / 2, sideLength / 2, sideLength / 2);

		hasTexture = hasArmature = false;
		textures = new ArrayList<>();
	}
	
}
