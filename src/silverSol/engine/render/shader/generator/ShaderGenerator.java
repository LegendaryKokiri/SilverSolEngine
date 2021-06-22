package silverSol.engine.render.shader.generator;

public class ShaderGenerator {

	private static final boolean[][] featureDependencies = new boolean[][]{
		{true}//Per-Pixel Lighting
	};
	
	private static final int NUM_FEATURES = 1;
	public static final int FEATURE_PER_PIXEL_LIGHTING = 0;
	
	private static final int NUM_DEPENDENCIES = 1;
//	private static final int DEPENDENCY_WORLD_POSITION = 0;
	
	private boolean[] features;
	private int[] dependencies; //TODO: If, at the time of generation, the int corresponding to a dependency is greater than 0, add that dependency.
	
	public ShaderGenerator() {
		features = new boolean[NUM_FEATURES];
		dependencies = new int[NUM_DEPENDENCIES];
	}
	
	public void addFeature(int feature) {
		if(features[feature]) return;
		features[feature] = true;
		addDependencies(feature);
	}
	
	public void removeFeature(int feature) {
		if(!features[feature]) return;
		features[feature] = false;
		removeDependencies(feature);
	}
	
	private void addDependencies(int feature) {
		boolean[] dependenciesToAdd = featureDependencies[feature];
		for(int i = 0; i < NUM_DEPENDENCIES; i++) {
			if(dependenciesToAdd[i]) dependencies[i]++; 
		}
	}
	
	private void removeDependencies(int feature) {
		boolean[] dependenciesToRemove = featureDependencies[feature];
		for(int i = 0; i < NUM_DEPENDENCIES; i++) {
			if(dependenciesToRemove[i]) dependencies[i]--; 
		}
	}

	
}
