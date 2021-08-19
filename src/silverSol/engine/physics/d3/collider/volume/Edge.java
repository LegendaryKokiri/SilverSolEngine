package silverSol.engine.physics.d3.collider.volume;

import org.lwjgl.util.vector.Vector3f;

import silverSol.math.VectorMath;

public class Edge {
	
	public Vector3f v1;
	public Vector3f v2;
	
	public Edge(Vector3f v1, Vector3f v2) {
		this.v1 = new Vector3f(v1);
		this.v2 = new Vector3f(v2);
	}
	
	@Override
	public boolean equals(Object o) {
		if(this == o) return true;
		if(o == null) return false;
		if(this.getClass() != o.getClass()) return false;
		return VectorMath.getEqual(v1, this.v1) && VectorMath.getEqual(v2, this.v2);
	}
	
	public boolean isReverseOf(Edge e) {
		return this.v1.equals(e.v2) && this.v2.equals(e.v1);
	}
	
	public Edge getReverse() {
		return new Edge(v2, v1);
	}
	
	@Override
	public int hashCode() {
		int x1 = Float.floatToIntBits(v1.x);
		int y1 = Float.floatToIntBits(v1.y);
		int z1 = Float.floatToIntBits(v1.z);
		int x2 = Float.floatToIntBits(v2.x);
		int y2 = Float.floatToIntBits(v2.y);
		int z2 = Float.floatToIntBits(v2.z);
		return x1 ^ (y1 >> 1) ^ (z1 >> 2) ^ (x2 >> 4) ^ (y2 >> 8) ^ (z2 >> 16);
	}
	
}
