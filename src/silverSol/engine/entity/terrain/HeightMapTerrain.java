package silverSol.engine.entity.terrain;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Vector3f;

import silverSol.engine.entity.terrain.hmap.PropertyMap;
import silverSol.engine.physics.d3.collider.volume.Landscape;
import silverSol.engine.physics.d3.collider.volume.Volume.Type;
import silverSol.math.NumberMath;

public class HeightMapTerrain extends Terrain {
	
	private float[][] heights;
	private List<PropertyMap> propertyMaps;
	
	private Vector3f dimensions;
	
	public HeightMapTerrain(float[][] heights) {
		super();
		this.heights = heights;
		propertyMaps = new ArrayList<>();
		
		float width = (float) heights.length;;
		float depth = width > 0 ? (float) heights[0].length : 0f;
		
		float min = Float.POSITIVE_INFINITY;
		float max = Float.NEGATIVE_INFINITY;
		for(float[] points : heights) {
			min = Math.min(min, NumberMath.min(points));
			max = Math.max(max, NumberMath.max(points));
		}
		
		float height = depth > 0 ? max - min : 0f;
		
		dimensions = new Vector3f(width, height, depth);
	}
	
	public Landscape generateLandscape(Type collisionType, Object colliderData) {
		return new Landscape(dimensions.x, dimensions.y, dimensions.z, heights, collisionType, colliderData);
	}
	
	public float getWidth() {
		return dimensions.x;
	}
	
	public float getHeight() {
		return dimensions.y;
	}
	
	public float getDepth() {
		return dimensions.z;
	}
	
}
