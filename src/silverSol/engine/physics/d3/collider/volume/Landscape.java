package silverSol.engine.physics.d3.collider.volume;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import silverSol.engine.physics.d3.body.Body;
import silverSol.engine.physics.d3.collision.Collision;
import silverSol.engine.physics.d3.det.narrow.algs.GJK;
import silverSol.engine.physics.d3.det.narrow.algs.SAT;
import silverSol.engine.physics.d3.det.narrow.algs.SeparatingAxis;
import silverSol.math.MatrixMath;
import silverSol.math.NumberMath;
import silverSol.math.VectorMath;

public class Landscape extends Volume {
	
	private static final float EPSILON = 1E-3f;
	
	private float width, depth, height;
	private float halfWidth, halfDepth, halfHeight;
	private float gridSizeX, gridSizeZ;
	private int xPoints, zPoints;
	
	private float heights[][];
	
	private static class DepthComparator implements Comparator<Collision> {
		@Override
		public int compare(Collision c1, Collision c2) {
			float diff = c1.getPenetrationDepth() - c2.getPenetrationDepth();
			if(diff < 0f) return -1;
			if(diff > 0f) return 1;
			return 0;
		}
	}
	private static DepthComparator depthComparator = new DepthComparator();
	
	public Landscape(Type collisionType, float width, float height, float depth, float[][] heights, Object colliderData) {
		super(collisionType, colliderData);
		
		this.width = width;
		this.depth = depth;
		this.height = height;
		this.halfWidth = width * 0.5f;
		this.halfDepth = depth * 0.5f;
		this.halfHeight = height * 0.5f;
		
		this.heights = heights;
		
		this.xPoints = heights.length;
		this.zPoints = heights[0].length;
		
		if(xPoints < 2 || zPoints < 2) {
			System.err.println("Landscapes must have at least four heights defined (two in the x and two in the z).");
			return;
		}
		
		this.gridSizeX = width / (float)(xPoints - 1);
		this.gridSizeZ = depth / (float)(zPoints - 1);
	}
	
	private Planar generatePlanar(int gridX, int gridZ, boolean upperLeft) {
		Vector3f[] t = new Vector3f[3];
		
		float leftX = (float) gridX * gridSizeX - halfWidth;
		float rightX = (float) (gridX + 1) * gridSizeX - halfWidth;
		float backZ = (float) gridZ * gridSizeZ - halfDepth;
		float frontZ = (float) (gridZ + 1) * gridSizeZ - halfDepth;
		
		if(upperLeft) {
			t[0] = new Vector3f(leftX, heights[gridX][gridZ + 1], frontZ);
			t[1] = new Vector3f(rightX, heights[gridX + 1][gridZ], backZ);
			t[2] = new Vector3f(leftX, heights[gridX][gridZ], backZ);
		} else {
			t[0] = new Vector3f(leftX, heights[gridX][gridZ + 1], frontZ);
			t[1] = new Vector3f(rightX, heights[gridX + 1][gridZ + 1], frontZ);
			t[2] = new Vector3f(rightX, heights[gridX + 1][gridZ], backZ);
		}
		
//		System.out.println("Planar from " + leftX + " - " + rightX + ", " + backZ + " - " + frontZ + " (" + upperLeft + ")");
				
		Vector3f centroid = VectorMath.mean(t[0], t[1], t[2]);
		Vector3f.sub(t[0], centroid, t[0]);
		Vector3f.sub(t[1], centroid, t[1]);
		Vector3f.sub(t[2], centroid, t[2]);
//		System.out.println("Centroid = " + centroid);
				
		Planar planar = new Planar(new Vector3f[]{t[0], t[1], t[2]}, Type.SOLID, null);
		planar.setBodyOffset(MatrixMath.createTransformation(centroid, new Vector3f(), new Vector3f(1f, 1f, 1f)));
		planar.setBody(this.body);
		planar.setID(this.ID);
		return planar;
	}
	
	@Override
	public void calculateEndpoints() {
		//TODO: This fails for a rotated terrain (same with Topography)
		endpoints[0].value = position.x - halfWidth;
		endpoints[1].value = position.x + halfWidth;
		endpoints[2].value = position.y - halfHeight;
		endpoints[3].value = position.y + halfHeight;
		endpoints[4].value = position.z - halfDepth;
		endpoints[5].value = position.z + halfDepth;
	}
	
	@Override
	public Vector3f supportMap(Vector3f globalDirection, boolean global) {
		return global ? this.toGlobalPosition(new Vector3f()) : new Vector3f();
	}

	@Override
	public Vector3f[] raycast(Vector3f globalOrigin, Vector3f globalDirection, float maxLength, boolean global) {
		Vector3f origin = toLocalPosition(globalOrigin);
		Vector3f direction = toLocalDirection(globalDirection);
		
		//Planar Selection
		int gridX = getGridX(origin.x);
		int gridZ = getGridZ(origin.z);
		int gridStepX = (int) Math.signum(direction.x);
		int gridStepZ = (int) Math.signum(direction.z);
		
		//Next Planar Selection
		float nextX = getNext(origin.x, direction.x, halfWidth, gridSizeX);
		float nextZ = getNext(origin.z, direction.z, halfDepth, gridSizeZ);
		
		//Parametrization
		float t = 0f;
		float tToX = Float.isNaN(nextX) ? Float.POSITIVE_INFINITY : (nextX - origin.x) / direction.x;
		float tToZ = Float.isNaN(nextZ) ? Float.POSITIVE_INFINITY : (nextZ - origin.z) / direction.z;
		float tStepX = Math.abs(gridSizeX / direction.x);
		float tStepZ = Math.abs(gridSizeZ / direction.z);
//		float tStepDiagonal = ;
		
		Vector3f[] intersection = null;
		
		while(t < maxLength) {
			boolean validSquare = validGridX(gridX) && validGridZ(gridZ);
			
			if(validSquare) {
				Planar p = generatePlanar(gridX, gridZ, true);
				intersection = p.raycast(origin, direction, maxLength, true); //TODO: Did we really construct the planars such that origin and direction would be considered local?
			
				if(intersection != null) {
					if(global) return new Vector3f[]{toGlobalPosition(intersection[0]), toGlobalDirection(intersection[1])};
					return intersection;
				}
				
				p = generatePlanar(gridX, gridZ, false);
				intersection = p.raycast(origin, direction, maxLength, true); //TODO: Did we really construct the planars such that origin and direction would be considered local?
			
				if(intersection != null) {
					if(global) return new Vector3f[]{toGlobalPosition(intersection[0]), toGlobalDirection(intersection[1])};
					return intersection;
				}
			}
			
			float tToNext = Math.min(tToX, tToZ);
			
			if(tToX == tToNext) {
				gridX += gridStepX;
				tToX += tStepX;
			}
			
			if(tToZ == tToNext) {
				gridZ += gridStepZ;
				tToZ += tStepZ;
			}
			
			t += tToNext;
			tToX -= tToNext;
			tToZ -= tToNext;
			
			if(t < 0f) return null;			
		}
		
		return null;
	}
	
	/**
	 * Returns the local-space coordinate of the grid edge in the negative direction
	 * of the origin point, or of the origin point if it lies on the edge.
	 * @param origin The coordinate to check
	 * @param halfDimension Half of the length of the landscape along this dimension
	 * @param gridSize The size of each grid square in this dimension
	 * @return The local-space coordinate of the grid edge in the negative direction
	 */
	private float getEdge(float origin, float halfDimension, float gridSize) {
		int n = (int) ((origin + halfDimension) / gridSize);
		return -halfDimension + gridSize * n;
	}
	
	/**
	 * Returns the local-space coordinate of the next grid edge in the passed direction
	 * relative to the origin point.
	 * @param origin The coordinate to check
	 * @param direction The direction along this axis to check for the next edge
	 * @param halfDimension Half of the length of the landscape along this dimension
	 * @param gridSize The size of each grid square in this dimension
	 * @return The local-space coordinate of the grid edge in the negative direction
	 */
	private float getNext(float origin, float direction, float halfDimension, float gridSize) {
		float dirSign = Math.signum(direction);
		if(dirSign == 0f) return Float.NaN;
		
		/* To get the exact grid position, we want to round up or increment for direction > 0
		 and round down or decrement for direction < 0*/
		float gridSteps = (origin + halfDimension) / gridSize;
		if(dirSign > 0) gridSteps += 1f;
		else if(dirSign < 0 && gridSteps % 1f == 0f) gridSteps -= 1f;
		int n = (int) gridSteps;
		
		return -halfDimension + gridSize * n;
	}
	
	@Override
	public Collision[] testForCollisions(Volume volume) {
		if(volume instanceof Landscape) return null;
		
		List<Planar> planars = getTestPlanars(volume);
		List<Collision> collisions = new ArrayList<>();
		
		for(Planar planar : planars) {
			Collision collision = testCollision(volume, planar);
			if(collision != null) collisions.add(collision);
		}
				
		Collision[] c = new Collision[collisions.size()];
		for(int i = 0; i < c.length; i++) {
			c[i] = collisions.get(i);
		}
		
		return c;
	}
	
	@Override
	public Collision[] testForResolutions(Volume volume) {
		if(volume instanceof Landscape) return null;
		
		List<Planar> triangles = getTestPlanars(volume);
		
		if(triangles.size() == 0) return null;
		
		List<Collision> collisions = new ArrayList<>();
				
		for(Planar triangle : triangles) {
			Collision collision = testResolution(volume, triangle);
			if(collision != null) collisions.add(collision);
		}
		
		Collision[] c = new Collision[collisions.size()];
		for(int i = 0; i < c.length; i++) {
			c[i] = collisions.get(i);
		}
		
		return c;
	}
	
	private Collision testCollision(Volume volume, Planar planar) {
		if(planar == null) return null;
		return GJK.detect(planar, volume);
	}
	
	private Collision testResolution(Volume volume, Planar planar) {
		Collision collision = SAT.run(planar, volume);
		if(collision == null) return null;
		
		Vector3f localA = toLandscapeLocal(planar, collision.getLocalContactB());
		Vector3f globalA = this.toGlobalPosition(localA);
		collision.setContactA(localA, globalA);
		
		return collision;
	}
	
	private Vector3f toLandscapeLocal(Planar planar, Vector3f planarPoint) {
		Vector4f local4 = new Vector4f(planarPoint.x, planarPoint.y, planarPoint.z, 1f);
		Vector4f global4 = VectorMath.mulMatrix(planar.getBodyOffset(), local4, null);
		return new Vector3f(global4.x, global4.y, global4.z);
	}
	
	private List<Planar> getTestPlanars(Volume volume) {
		List<Planar> planars = new ArrayList<>();
		
		Vector3f leftPosition = toLocalPosition(volume.supportMap(MatrixMath.getRight(body.getTransformation()), true));
		Vector3f rightPosition = toLocalPosition(volume.supportMap(MatrixMath.getLeft(body.getTransformation()), true));
		Vector3f backPosition = toLocalPosition(volume.supportMap(MatrixMath.getBackward(body.getTransformation()), true));
		Vector3f frontPosition = toLocalPosition(volume.supportMap(MatrixMath.getForward(body.getTransformation()), true));
		
		int xMin = getGridX(leftPosition.x);	
		int xMax = getGridX(rightPosition.x);
		int zMin = getGridZ(backPosition.z);
		int zMax = getGridZ(frontPosition.z);
		
		int leftIndex = upperLeft(leftPosition.x, backPosition.z) ? 0 : 1;
		int rightIndex = upperLeft(rightPosition.x, frontPosition.z) ? 0 : 1;
		int startX = xMin * 2 + leftIndex;
		int endX = xMax * 2 + rightIndex;
				
		for(int z = NumberMath.max(0, zMin); z <= zMax; z++) {
			if(z >= zPoints - 1) break;
			for(int x = NumberMath.max(0, startX); x <= endX; x++) {
				if(x >= (xPoints - 1) * 2) break;
				planars.add(generatePlanar(x / 2, z, x % 2 == 0));
			}
		}
		
		return planars;
	}
	
	private boolean validGridX(int gridX) {
		return -1 < gridX && gridX < xPoints - 1;
	}
	
	private boolean validGridZ(int gridZ) {
		return -1 < gridZ && gridZ < zPoints - 1;
	}
	
	//TODO: xPosition needs to be localized with respect to the Landscape for this to work with any input. Make sure all calls localize.
	private int getGridX(float localX) {
		int gridX = (int) ((localX + halfWidth) / gridSizeX);
		if(gridX < 0) return -1;
		if(gridX >= xPoints - 1) return xPoints - 1;
		return gridX;
	}
	
	//TODO: zPosition needs to be localized with respect to the Landscape for this to work with any input. Make sure all calls localize.
	private int getGridZ(float localZ) {
		int gridZ = (int) ((localZ + halfDepth) / gridSizeZ);
		if(gridZ < 0) return -1;
		if(gridZ >= zPoints - 1) return zPoints - 1;
		return gridZ;
	}
	
	private float getGridSpaceX(float localX) {
		return localX - getEdge(localX, halfWidth, gridSizeX);
	}
	
	private float getGridSpaceZ(float localZ) {
		return localZ - getEdge(localZ, halfDepth, gridSizeZ);
	}
	
	//TODO: x and z need to be localized with respect to the Landscape for this to work with any input. Make sure all calls localize.
	private boolean upperLeft(float localX, float localZ) {
		float xCoordinate = getGridSpaceX(localX);
		float zCoordinate = getGridSpaceZ(localZ);
		return upperLeftCoords(xCoordinate, zCoordinate);
	}
	
	private boolean upperLeftCoords(float gridSpaceX, float gridSpaceZ) {
		return (gridSpaceX / gridSizeX) <= 1f - (gridSpaceZ / gridSizeZ);
	}
	
	//TODO: x and z need to be localized with respect to the Landscape for this to work with any input. Make sure all calls localize.
	public float getHeight(float localX, float localZ) {					
		int gridX = getGridX(localX);
		int gridZ = getGridZ(localZ);
		
		if(gridX >= xPoints - 1 || gridZ >= zPoints - 1|| gridX < 0 || gridZ < 0) return Float.NaN;
		
		float height = Float.NaN;
		Vector2f gridSpaceCoords = new Vector2f(getGridSpaceX(localX) / gridSizeX, getGridSpaceZ(localZ) / gridSizeZ);
		
		if(gridSpaceCoords.x <= 1 - gridSpaceCoords.y) {
			height = barryCentric3D(new Vector3f(0, heights[gridX][gridZ], 0), new Vector3f(1, heights[gridX + 1][gridZ], 0),
					new Vector3f(0, heights[gridX][gridZ + 1], 1), gridSpaceCoords);
		} else {
			height = barryCentric3D(new Vector3f(1, heights[gridX + 1][gridZ], 0),
					new Vector3f(1, heights[gridX + 1][gridZ + 1], 1), new Vector3f(0, heights[gridX][gridZ + 1], 1),
					gridSpaceCoords);
		}
		
		return height;
	}
	
	private float barryCentric3D(Vector3f p1, Vector3f p2, Vector3f p3, Vector2f pos) {
		float det = (p2.z - p3.z) * (p1.x - p3.x) + (p3.x - p2.x) * (p1.z - p3.z);
		float l1 = ((p2.z - p3.z) * (pos.x - p3.x) + (p3.x - p2.x) * (pos.y - p3.z)) / det;
		float l2 = ((p3.z - p1.z) * (pos.x - p3.x) + (p1.x - p3.x) * (pos.y - p3.z)) / det;
		float l3 = 1.0f - l1 - l2;
		
		return l1 * p1.y + l2 * p2.y + l3 * p3.y;
	}
	
	@Override
	public SeparatingAxis[] getSeparatingAxes(Volume other) {
		return new SeparatingAxis[0];
	}
	
	public float getWidth() {
		return width;
	}

	public void setWidth(float width) {
		this.width = width;
		this.halfWidth = width * 0.5f;
		this.gridSizeX = (xPoints > 0) ? width / (float)(xPoints - 1) : 0;
	}

	public float getDepth() {
		return depth;
	}

	public void setDepth(float depth) {
		this.depth = depth;
		this.halfDepth = depth * 0.5f;
		this.gridSizeZ = (zPoints > 0) ? depth / (float)(zPoints - 1) : 0;
	}
	
	public float getHeight() {
		return height;
	}
	
	public void setHeight(float height) {
		float scale = height / this.height;
		for(int i = 0; i < xPoints; i++) {
			for(int j = 0; j < zPoints; j++) {
				heights[i][j] *= scale;
			}
		}
		
		this.height = height;
		this.halfHeight = height * 0.5f;
	}

	public float[][] getHeights() {
		return heights;
	}
	
	public static void main(String[] args) {
		//TODO: Modify the landscape such that the height indices correspond to what you visually see when making such a 2-D array as heights
		Landscape landscape = new Landscape(Type.SOLID, 10f, 40f, 10f, new float[][]{
			{0f,0f,0f,0f,0f},{0f,40f,40f,40f,0f},{0f,40f,40f,40f,0f},{0f,40f,40f,40f,0f},{0f,0f,0f,0f,0f}}, null);
		landscape.setID(2);
		
		Body terrainBody = new Body();
		terrainBody.addVolume(landscape);
		
		/*
		Body sphereBody = new Body();
		Sphere sphere = new Sphere(1f, Type.SOLID, null);
		sphere.setID(1);
		sphereBody.addVolume(sphere);
		sphereBody.setPosition(-2f, 10.9f, 0f);
				
		Collision[] collisions = landscape.testForResolutions(sphere);
		
		if(collisions != null) {
			for(Collision collision : collisions) {
				System.out.println(collision + " with normal " + collision.getSeparatingAxis(sphere) + " and depth " + collision.getPenetrationDepth());
				System.out.println(collision.getColliderA());
				System.out.println("Global A: " + collision.getGlobalContactA());
				System.out.println("Global B: " + collision.getGlobalContactB());
			}
		}
		*/
		
		/*
		Vector2f[] pollPoints = new Vector2f[] {new Vector2f(-4.9f, -4.9f), new Vector2f(-1.8f, -1.8f), new Vector2f(-1.5f, -3.633f)};
		for(Vector2f poll : pollPoints) System.out.println("Upper Left? " + landscape.upperLeft(poll.x, poll.y));
		*/
		
		Vector3f testCases[][] = new Vector3f[][] {
			{new Vector3f(4f, 9f, -9f), new Vector3f(0f, 0f, 1f)},
			{new Vector3f(0f, 15f, 5.01f), new Vector3f(0f, 0f, -1f)},
			{new Vector3f(0f, 50f, 0f), new Vector3f(0f, -1f, 0f)}};
		
		
		Vector3f intersection[] = null;
		int i = 1;
		for(Vector3f[] testCase : testCases) {
			System.out.println("---Test Case " + i + "---");
			
			intersection = landscape.raycast(testCase[0], testCase[1], 20f, true);
			
			if(intersection == null) {
				System.out.println("Raycast collided with nothing");
			} else {
				System.out.println("Raycast intersection point = " + intersection[0]);
				System.out.println("\tNormal = " + intersection[1]);
			}
			
			System.out.println("===End of Case " + i++ + "===\n");
		}
	}
	
}
