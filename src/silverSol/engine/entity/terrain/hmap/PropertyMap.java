package silverSol.engine.entity.terrain.hmap;

import org.lwjgl.util.vector.Vector4f;

public class PropertyMap<T extends Object> {
	
	private T[][][] map;
	
	public PropertyMap(T[][][] map) {
		this.map = map;
	}
	
	public T getPropertyAt(int x, int z, boolean upperLeft) {
		return this.map[x][z][upperLeft ? 0 : 1];
	}
	
	public void setPropertyAt(int x, int z, boolean upperLeft, T property) {
		this.map[x][z][upperLeft ? 0 : 1] = property;
	}
	
	//TODO: Implement this
	public static PropertyMap<Integer> buildBinaryMap() {
		return null;
	}
	
	//TODO: Implement this
	public static PropertyMap<Vector4f> buildBlendMap() {
		return null;
	}

}
