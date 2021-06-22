package silverSol.engine.physics.d3.det.narrow.algs;

import org.lwjgl.util.vector.Matrix3f;
import org.lwjgl.util.vector.Vector3f;

import silverSol.engine.physics.d3.collider.volume.Volume;
import silverSol.math.MatrixMath;
import silverSol.math.VectorMath;

public class Simplex {
	
	private static final Vector3f[] AXES = new Vector3f[]{
		new Vector3f(1f, 0f, 0f),
		new Vector3f(0f, 1f, 0f),
		new Vector3f(0f, 0f, 1f),
		new Vector3f(-1f, 0f, 0f),
		new Vector3f(0f, -1f, 0f),
		new Vector3f(0f, 0f, -1f)
	};
	
	private static final float REDIRECT_THRESHOLD = 1E-3f;
	private static final float EPSILON = 1E-6f;

	private Vector3f[] points;
	private Vector3f[] pointsV1;
	private Vector3f[] pointsV2;
	
	private int numVertices;
	private Vector3f direction;
	private boolean containsOrigin;
	
	public Simplex(Volume v1, Volume v2) {
		points = new Vector3f[4];
		pointsV1 = new Vector3f[4];
		pointsV2 = new Vector3f[4];
		
		for(int i = 0; i < points.length; i++) {
			points[i] = new Vector3f();
			pointsV1[i] = new Vector3f();
			pointsV2[i] = new Vector3f();
		}
		
		direction = new Vector3f();
		for(int i = 0; i < AXES.length; i++) {
			direction.set(AXES[i]);
			findSupport(direction, v1, v2, pointsV1[0], pointsV2[0], points[0]);
			if(Math.abs(Vector3f.dot(direction, points[0])) >= points[0].length() * 0.8f) continue;
			
			numVertices = 1;
			break;
		}
		
//		if(numVertices == 0) System.err.println("GJK failed to produce a valid simplex between " + v1 + " and " + v2 + ".");
	}
	
	public boolean evolve(Volume v1, Volume v2) {
		determineDirection();
		findSupport(direction, v1, v2, pointsV1[numVertices], pointsV2[numVertices], points[numVertices]);
		numVertices++;
		
		if(!collisionPossible()) return false;
		
		reduceSimplex();
		return true;
	}
	
	private void reduceSimplex() {
		switch(numVertices) {
			case 4:
				if(!reduceTetrahedron()) return;
			case 3:
				if(!reduceTriangle()) return;
			default:
				return;
		}
	}
	
	private boolean reduceTetrahedron() {
		Vector3f edge32 = Vector3f.sub(points[2], points[3], null);
		Vector3f edge31 = Vector3f.sub(points[1], points[3], null);
		Vector3f edge30 = Vector3f.sub(points[0], points[3], null);
		
		Vector3f normal123 = Vector3f.cross(edge32, edge31, null);
		if(simTest(normal123)) {
			removeVertex(0);
			return true;
		}
		
		Vector3f normal013 = Vector3f.cross(edge31, edge30, null);
		if(simTest(normal013)) {
			removeVertex(2);
			return true;
		}
		
		Vector3f normal023 = Vector3f.cross(edge30, edge32, null);
		if(simTest(normal023)) {
			removeVertex(1);
			return true;
		}
		
		containsOrigin = true;
		return false;
	}
	
	private boolean reduceTriangle() {
		Vector3f edge21 = Vector3f.sub(points[1], points[2], null);
		Vector3f edge20 = Vector3f.sub(points[0], points[2], null);
		Vector3f normal = Vector3f.cross(edge21, edge20, null);
		
		if(simTest(Vector3f.cross(edge21, normal, null))) {
			removeVertex(0);
			return true;
		} else if(simTest(Vector3f.cross(normal, edge20, null))) {
			removeVertex(1);
			return true;
		} else if(simTest(normal)) {
			return false;
		} else {
			swapVertices(0, 1);
			return false;
		}
	}
	
	private void determineDirection() {
		switch(numVertices) {
			case 1:
				directFromPoint();
				break;
			case 2:
				directFromSegment();
				break;
			case 3:
				directFromTriangle();
				break;
		}
	}
	
	private void directFromPoint() {
		points[0].negate(direction);
	}
	
	//TODO: Delete the return statements in these direct functions. We only added them for debugging.
	private void directFromSegment() {
		Vector3f edge10 = Vector3f.sub(points[0], points[1], null);
		Vector3f.cross(Vector3f.cross(edge10, points[1].negate(null), null), edge10, direction);
		return;
	}
	
	private void directFromTriangle() {
		Vector3f edge21 = Vector3f.sub(points[1], points[2], null);
		Vector3f edge20 = Vector3f.sub(points[0], points[2], null);
		Vector3f.cross(edge21, edge20, direction);
		return;
	}
	
	public Vector3f findSupport(Vector3f direction, Volume v1, Volume v2, Vector3f dest1, Vector3f dest2, Vector3f destS) {
		Vector3f normal = direction.normalise(null);
		
		if(direction.lengthSquared() < REDIRECT_THRESHOLD || VectorMath.hasNaN(normal)) {
			dest1.set(v1.getPosition());
			dest2.set(v2.getPosition());
			destS.set(0f, 0f, 0f);
			return new Vector3f();
		}
		
		Vector3f s1 = v1.supportMap(normal, true);
		Vector3f s2 = v2.supportMap(normal.negate(null), true);
		Vector3f s = Vector3f.sub(s1, s2, null);
		
		if(dest1 != null) dest1.set(s1);
		if(dest2 != null) dest2.set(s2);
		if(destS != null) destS.set(s);
		
		return s;
	}
	
	private boolean simTest(Vector3f direction) {
		return Vector3f.dot(direction, points[numVertices - 1].negate(null)) > 0;
	}
	
	//TODO: Make this one statement. We just did this for debugging.
	private boolean collisionPossible() {
		boolean reachedOrigin = Vector3f.dot(points[numVertices - 1], direction) >= 0f;
		return reachedOrigin;
	}
	
	public void epaExpand(Volume v1, Volume v2) {		
		switch(numVertices) {
			case 1:
				expandPoint(v1, v2);
			case 2:
				expandSegment(v1, v2);
			case 3:
				expandTriangle(v1, v2);
		}
		
		
		windCounterClockwise();
	}
	
	//TODO: Put break on the same line as if.
	private void expandPoint(Volume v1, Volume v2) {
		for(Vector3f axis : AXES) {
			findSupport(axis, v1, v2, pointsV1[1], pointsV2[1], points[1]);
			if(Vector3f.sub(points[1], points[0], null).lengthSquared() >= EPSILON) break;
		}
		
		numVertices++;
	}
	
	//TODO: When the two points are the same, we'll have a bad search direction. Figure out what to do in that case.
	//TODO: Put break on the same line as if.
	private void expandSegment(Volume v1, Volume v2) {
		Vector3f segment = Vector3f.sub(points[1], points[0], null);
		Vector3f searchDirection = Vector3f.cross(segment, VectorMath.leastSignificantAxis(segment), null);
		Matrix3f rotation = MatrixMath.createRotation(segment, (float) Math.PI / 3f);
		
		for(int i = 0; i < 6; i++) {
			findSupport(searchDirection, v1, v2, pointsV1[2], pointsV2[2], points[2]);
			if(points[2].lengthSquared() > EPSILON) break;
			
			VectorMath.mulMatrix(rotation, searchDirection, searchDirection);
		}
		
		numVertices++;
	}
	
	private void expandTriangle(Volume v1, Volume v2) {
		Vector3f edge01 = Vector3f.sub(points[1], points[0], null);
		Vector3f edge02 = Vector3f.sub(points[2], points[0], null);
		Vector3f searchDirection = Vector3f.cross(edge01, edge02, null);
		
		findSupport(searchDirection, v1, v2, pointsV1[3], pointsV2[3], points[3]);
		
		if(points[3].lengthSquared() < EPSILON) {
			searchDirection.negate(searchDirection);
			findSupport(searchDirection, v1, v2, pointsV1[3], pointsV2[3], points[3]);
		}
		
		numVertices++;
	}
	
	private void windCounterClockwise() {
		Vector3f edge30 = Vector3f.sub(points[0], points[3], null);
		Vector3f edge31 = Vector3f.sub(points[1], points[3], null);
		Vector3f edge32 = Vector3f.sub(points[2], points[3], null);
		
		float det = Vector3f.dot(edge30, Vector3f.cross(edge31, edge32, null));

		if(det > 0f) swapVertices(0, 1);
	}
	
	private void swapVertices(int index1, int index2) {
		Vector3f temp = new Vector3f(points[index2]);
		points[index2].set(points[index1]);
		points[index1].set(temp);
		
		temp.set(pointsV1[index2]);
		pointsV1[index2].set(pointsV1[index1]);
		pointsV1[index1].set(temp);
		
		temp.set(pointsV2[index2]);
		pointsV2[index2].set(pointsV2[index1]);
		pointsV2[index1].set(temp);
	}
	
	private void removeVertex(int index) {
		for(int i = index; i < points.length - 1; i++) {
			points[i].set(points[i + 1]);
			pointsV1[i].set(pointsV1[i + 1]);
			pointsV2[i].set(pointsV2[i + 1]);
		}
		
		numVertices--;
	}
	
	public Vector3f[] getPoints() {
		return points;
	}
	
	public Vector3f[] getPointsV1() {
		return pointsV1;
	}

	public Vector3f[] getPointsV2() {
		return pointsV2;
	}

	public Vector3f getDirection() {
		return direction;
	}
	
	public boolean containsOrigin() {
		return containsOrigin;
	}
	
	@Override
	public String toString() {
		return "Simplex: " + points[0] + " - " + points[1] + " - " + points[2] + " - " + points[3];
	}
	
}
