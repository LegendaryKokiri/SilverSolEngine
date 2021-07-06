package silverSol.engine.physics.d3.collider.volume;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import silverSol.engine.physics.d3.collision.Collision;
import silverSol.engine.physics.d3.det.narrow.algs.EPA;
import silverSol.engine.physics.d3.det.narrow.algs.GJK;
import silverSol.engine.physics.d3.det.narrow.algs.SeparatingAxis;
import silverSol.engine.physics.d3.det.narrow.algs.SeparatingAxis.Resolution;
import silverSol.math.PlaneMath;
import silverSol.math.SegmentMath;
import silverSol.math.VectorMath;

public class Planar extends Volume {
	
	private static final float EPSILON = 1E-3f;
	
	private Vector3f[] vertices;
	private Vector3f normal;
	
	//PROJECTIONS (FOR RAYCASTING)
	private Vector2f[] projectedVertices; 
	private Vector3f basis1;
	private Vector3f basis2;
	
	public Planar(Vector3f[] vertices, Type collisionType, Object colliderData) {
		super(collisionType, colliderData);
		
		if(vertices.length < 3) System.err.println("It's impossible to define a plane with fewer than three points.");
		
		this.vertices = vertices.clone();
		normal = Vector3f.cross(Vector3f.sub(vertices[1], vertices[0], null), Vector3f.sub(vertices[2], vertices[0], null), null);
		normal.normalise(normal);
		
		this.basis1 = new Vector3f();
		this.basis2 = new Vector3f();
		VectorMath.generateBasis(this.normal, this.basis1, this.basis2);
		
		this.projectedVertices = new Vector2f[vertices.length];
		for(int i = 0; i < vertices.length; i++) {
			this.projectedVertices[i] = project(vertices[i]);
		}
	}

	@Override
	public void calculateEndpoints() {
		Vector3f min = new Vector3f(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
		Vector3f max = new Vector3f(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
		
		for(Vector3f vertex : vertices) {
			Vector3f v = toGlobalPosition(vertex);
			VectorMath.min(min, v, min);
			VectorMath.max(max, v, max);
		}
		
		endpoints[0].value = min.x;
		endpoints[1].value = max.x;
		endpoints[2].value = min.y;
		endpoints[3].value = max.y;
		endpoints[4].value = min.z;
		endpoints[5].value = max.z;
	}

	@Override
	public Vector3f supportMap(Vector3f globalDirection, boolean global) {
		if(vertices.length == 0) return null;
		
		Vector3f direction = toLocalDirection(globalDirection);
		Vector3f support = new Vector3f();
		
		float maxDot = Float.NEGATIVE_INFINITY;
		for(Vector3f vertex : vertices) {
			float dot = Vector3f.dot(vertex, direction);
			if(dot > maxDot) {
				support.set(vertex);
				maxDot = dot;
			}
		}
		
		return global ? this.toGlobalPosition(support) : support;
	}
	
	@Override
	public Vector3f[] raycast(Vector3f globalOrigin, Vector3f globalDirection, float maxLength, boolean global) {
		Vector3f origin = toLocalPosition(globalOrigin);
		Vector3f direction = toLocalDirection(globalDirection);
				
		Vector3f intersection = PlaneMath.rayIntersection(origin, direction, vertices[0], normal);
		if(intersection == null) return null;
		
		Vector3f toPlane = Vector3f.sub(intersection, origin, null);
		if(toPlane.lengthSquared() > maxLength * maxLength) return null;
		
		Vector2f p = project(intersection);
		boolean inShape = false;
		
		//Cast the ray (1,0) from the point and see how many edges it crosses.
		//To avoid counting repeat edges on vertices, the intersection must be above the bottom point and left of the rightmost point
		for(int i = 1; i <= projectedVertices.length; i++) {
			Vector2f p1 = projectedVertices[i-1];
			Vector2f p2 = projectedVertices[i % projectedVertices.length];
			
			//Is intersection point between the y-boundaries of the edge?
			if(!(p1.y < p.y && p.y <= p2.y) && !(p2.y <= p.y && p.y < p1.y)) {
				continue;
			}
			
			//Is the edge vertical? If so, we can't do traditional slope calculations.
			//TODO: We probably want to check within EPSILON rather than exact equality
			if(p1.x == p2.x) {
				if(p.x <= p1.x) inShape = !inShape;
				continue;
			}
			
			//Is the intersection point of the ray with the edge on the right of the intersection?
			float slope = (p2.y - p1.y) / (p2.x - p1.x); //TODO: We could precompute slopes for a speedup.
			float edgeX = (p.y - p1.y) / slope + p1.x;
			if(edgeX <= p.x) continue;
			
			inShape = !inShape;
		}
		
		if(!inShape) return null;
		
		if(global) return new Vector3f[]{toGlobalPosition(intersection), toGlobalDirection(normal)};
		return new Vector3f[]{intersection, new Vector3f(normal)};
	}

	private Vector2f project(Vector3f v) {
		return new Vector2f(Vector3f.dot(this.basis1, v), Vector3f.dot(this.basis2, v));
	}
	
	@Override
	public Collision[] testForCollisions(Volume volume) {
		if(volume instanceof Landscape) return volume.testForCollisions(this);
		return new Collision[]{GJK.detect(this, volume)};
	}
	
	@Override
	public Collision[] testForResolutions(Volume volume) {
		if(volume instanceof Landscape) return volume.testForResolutions(this);
		return new Collision[]{EPA.run(GJK.run(this, volume), this, volume)};
	}
	
	@Override
	public SeparatingAxis[] getSeparatingAxes(Volume other) {
		SeparatingAxis axes[] = new SeparatingAxis[vertices.length + 1];
		axes[0] = new SeparatingAxis(this.toGlobalDirection(normal), Resolution.FORWARD);
		
		int index = 1;
		for(int i = 1; i <= vertices.length; i++) {
			//TODO: Having a set of normalised edges would speed this up by saving us from having to normalise.
			//TODO: Better yet, we can just precompute the separating axes in local space and globalize them
			Vector3f local = Vector3f.cross(normal, Vector3f.sub(vertices[i % vertices.length], vertices[i-1], null), null).normalise(null);
			axes[index++] = new SeparatingAxis(this.toGlobalDirection(local), Resolution.NONE);
		}
		
		//TODO: Note that we are not returning all axes.
		return axes;
	}
	
	public Vector3f[] getVertices() {
		return vertices;
	}

	public Vector3f getNormal() {
		return normal;
	}
	
	/**
	 * Returns whether the two planes form a common edge and are convex with one another
	 * @param p The Planar to check this one against
	 * @return true if the Planars share at least one common edge but specified in opposite order from one another, false otherwise
	 */
	public boolean convexTo(Planar p) {
		Vector3f reference = vertices[0];
		
		for(Vector3f v : p.getVertices()) {
			if(Vector3f.dot(Vector3f.sub(v, reference, null), normal) > EPSILON) return false;
		}
		
		return true;
	}
	
	public static void main(String[] args) {
		
	}

}
