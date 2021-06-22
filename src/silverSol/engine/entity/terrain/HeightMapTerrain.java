package silverSol.engine.entity.terrain;

import silverSol.engine.physics.d3.body.Body;
import silverSol.engine.physics.d3.collider.volume.Landscape;
import silverSol.engine.physics.d3.collider.volume.Volume.Type;

public class HeightMapTerrain extends Terrain {
	
	public HeightMapTerrain(float width, float height, float depth, float[][] heights) {
		super();
		
		hasBody3d = true;
		body3d = new Body();
		body3d.setImmovable(true);
		body3d.setMass(Float.POSITIVE_INFINITY);
		body3d.addVolume(new Landscape(Type.SOLID, width, height, depth, heights, null));
	}
	
}
