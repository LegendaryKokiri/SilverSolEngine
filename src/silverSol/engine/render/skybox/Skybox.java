package silverSol.engine.render.skybox;

import org.lwjgl.opengl.GL15;

import silverSol.engine.entity.Entity;
import silverSol.engine.render.model.models.SkyboxCube;
import silverSol.engine.render.texture.cubeMap.CubeMap;

public class Skybox extends Entity {
	public Skybox(CubeMap cubeMap, float sideLength) {
		super(new SkyboxCube(sideLength, GL15.GL_STATIC_DRAW));
		this.model.addTexture(cubeMap);
	}
}
