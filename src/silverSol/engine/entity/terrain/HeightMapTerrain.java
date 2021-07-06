package silverSol.engine.entity.terrain;

import silverSol.engine.physics.d3.body.Body;
import silverSol.engine.physics.d3.collider.volume.Landscape;
import silverSol.engine.physics.d3.collider.volume.Volume.Type;

public class HeightMapTerrain extends Terrain {
	
	public HeightMapTerrain(float width, float height, float depth, float[][] heights) {
		super();
		
		Body body = new Body();
		body.setImmovable(true);
		body.setMass(Float.POSITIVE_INFINITY);
		body.addVolume(new Landscape(Type.SOLID, width, height, depth, heights, null));
		setBody(body);
	}
	
}
