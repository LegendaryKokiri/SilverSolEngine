package silverSol.engine.physics.d3.collider.volume;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import silverSol.engine.physics.d3.body.Body;
import silverSol.engine.physics.d3.collision.Collision;
import silverSol.engine.physics.d3.det.narrow.algs.GJK;
import silverSol.engine.physics.d3.det.narrow.algs.SAT;
import silverSol.engine.physics.d3.det.narrow.algs.SeparatingAxis;
import silverSol.math.MatrixMath;
import silverSol.math.VectorMath;

public class Landscape extends Volume {
	
	private static final Vector3f LEFT_VECTOR = new Vector3f(-1f, 0f, 0f);
	private static final Vector3f RIGHT_VECTOR = new Vector3f(1f, 0f, 0f);
	private static final Vector3f FRONT_VECTOR = new Vector3f(0f, 0f, 1f);
	private static final Vector3f BACK_VECTOR = new Vector3f(0f, 0f, -1f);
		
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
				
		Vector3f centroid = VectorMath.mean(t[0], t[1], t[2]);
		Vector3f.sub(t[0], centroid, t[0]);
		Vector3f.sub(t[1], centroid, t[1]);
		Vector3f.sub(t[2], centroid, t[2]);
				
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
		Vector3f support = new Vector3f();
		return global ? this.toGlobalPosition(support) : support;
	}

	@Override
	public Vector3f[] raycast(Vector3f globalOrigin, Vector3f globalDirection, float maxLength, boolean global) {
		Vector3f origin = toLocalPosition(globalOrigin);
		Vector3f direction = toLocalDirection(globalDirection);
		
		//a ---c---> b
		float aLength = 0f;
		float bLength = maxLength;
		
		Vector3f a = new Vector3f(origin);
		Vector3f b = new Vector3f(Vector3f.add(a, VectorMath.mul(direction, bLength, null), null));
		Vector3f c = new Vector3f();
		
		while(true) {
			float aHeight = getHeight(a.x, a.z);
			if(a.y < aHeight) return null;
		
			float bHeight = getHeight(b.x, b.z);
			if(b.y > bHeight) return null;
			
			int aGridX = getGridX(a.x);
			if(aGridX == -1 || aGridX == heights[0].length - 1) return null;
			
			int bGridX = getGridX(b.x);
			if(bGridX == -1 || bGridX == heights[0].length - 1) return null;
			
			if(aGridX != bGridX) {
				float midLength = getMidLength(aLength, bLength);
				c.set(getRayPoint(origin, direction, midLength));
				float cHeight = getHeight(c.x, c.z);
				
				if(c.y < cHeight) {
					bLength = midLength;
					b.set(c);
					continue;
				} else {
					aLength = midLength;
					a.set(c);
					continue;
				}
			}
			
			int aGridZ = getGridZ(a.z);
			if(aGridZ == -1 || aGridZ == heights[0].length - 1) return null;
			
			int bGridZ = getGridZ(b.z);
			if(bGridZ == -1 || bGridZ == heights[0].length - 1) return null;
			
			if(aGridZ != bGridZ) {
				float midLength = getMidLength(aLength, bLength);
				c.set(getRayPoint(origin, direction, midLength));
				float cHeight = getHeight(c.x, c.z);
				
				if(c.y < cHeight) {
					bLength = midLength;
					b.set(c);
					continue;
				} else {
					aLength = midLength;
					a.set(c);
					continue;
				}
			}
			
			boolean aLeft = upperLeft(a.x, a.z);
			boolean bLeft = upperLeft(b.x, b.z);
			
			if(aLeft != bLeft) {
				float midLength = getMidLength(aLength, bLength);
				c.set(getRayPoint(origin, direction, midLength));
				float cHeight = getHeight(c.x, c.z);
				
				if(c.y < cHeight) {
					bLength = midLength;
					b.set(c);
					continue;
				} else {
					aLength = midLength;
					a.set(c);
					continue;
				}
			}
			
			Planar planar = generatePlanar(aGridX, aGridZ, aLeft);			
			Vector3f[] intersection = planar.raycast(origin, direction, maxLength, true);
			if(intersection == null) return null;
			
			if(global) return new Vector3f[]{toGlobalPosition(intersection[0]), toGlobalDirection(intersection[1])};
			return intersection;
		}
	}
	
	private float getMidLength(float aLength, float bLength) {
		return aLength + (bLength - aLength) * 0.5f;
	}
	
	private Vector3f getRayPoint(Vector3f origin, Vector3f direction, float length) {
		return Vector3f.add(origin, VectorMath.mul(direction, length, null), null);
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
		Map<Collision, Planar> colToPlanar = new HashMap<>();
		
		PriorityQueue<Collision> colPQ = new PriorityQueue<>(triangles.size(), depthComparator);
		
		for(Planar triangle : triangles) {
			Collision collision = testResolution(volume, triangle);
			if(collision != null) {
				collisions.add(collision);
				colPQ.add(collision);
				colToPlanar.put(collision, triangle);
			}
		}
		
		Collision[] c = new Collision[collisions.size()];
		for(int i = 0; i < c.length; i++) {
			c[i] = collisions.get(i);
		}
		return c;
		
		/*
		List<Collision> cpqList = new ArrayList<>();
		List<Planar> cpqPlanars = new ArrayList<>();
		
		iter: while(!colPQ.isEmpty()) {
			Collision collision = colPQ.poll();
//			System.out.println("Landscape.testForResolutions(): Iterating over collision with normal " + collision.getSeparatingAxis(volume) + " and depth " + collision.getPenetrationDepth() + ")");
			Planar planar = colToPlanar.get(collision);
			
			for(Planar p : cpqPlanars) {
				if(planar.convexTo(p) || planar.makesFaceWith(p)) {
					continue iter;
				}				
			}
			
			cpqList.add(collision);
			cpqPlanars.add(planar);
		}
		
		return cpqList.toArray(new Collision[0]);
		*/
	}
	
	private Collision testCollision(Volume volume, Planar planar) {
		if(planar == null) return null;
		return GJK.detect(planar, volume);
	}
	
	private Collision testResolution(Volume volume, Planar planar) {
		Collision collision = SAT.run(planar, volume);
		
		/*
		if(GJK.detect(volume, planar) == null) return null;
		
		Vector3f[] p = planar.getVertices();
		Vector3f[] vertices = new Vector3f[p.length * 2];
		Vector3f normal = planar.getNormal();
		Vector3f nNormal = normal.negate(null);
		
		Vector3f top = volume.supportMap(normal, true);
		Vector3f bottom = volume.supportMap(nNormal, true);
		float topLength = Vector3f.dot(Vector3f.sub(top, p[0], null), normal) + 1f;
		float bottomLength = Vector3f.dot(Vector3f.sub(bottom, p[0], null), nNormal) + 1f;
		Vector3f topNormal = VectorMath.mul(normal, topLength, null);
		Vector3f bottomNormal = VectorMath.mul(nNormal, bottomLength, null);
		
		for(int i = 0; i < p.length; i++) {
			Vector3f v = planar.getVertices()[i];
			vertices[i * 2] = Vector3f.add(v, topNormal, null);
			vertices[i * 2 + 1] = Vector3f.add(v, bottomNormal, null);
		}
		
		Hull hull = new Hull(vertices, new int[0], this.getType(), null);
		hull.setBodyOffset(planar.getBodyOffset());
		hull.setBody(this.body);
		
		Collision collision = EPA.run(GJK.run(volume, hull), volume, hull);	
//		Collision collision = EPA.run(GJK.run(volume, planar), volume,  planar);
		*/
		
		if(collision == null) return null;
		
		/*
		Vector3f globalA = collision.getGlobalContactA();
		Vector3f rayOrigin = planar.toLocalPosition(globalA);
		Vector3f intersection = PlaneMath.rayIntersection(rayOrigin, normal, p[0], normal);
		if(intersection == null) return null;
		
		Vector3f localB = toLandscapeLocal(planar, intersection);
//		Vector3f localB = toLandscapeLocal(planar, collision.getLocalContactB());
		Vector3f globalB = this.toGlobalPosition(localB);
		
		Vector3f lNormal = planar.toGlobalDirection(normal);
		collision.setSeparatingAxis(lNormal.negate(null)); //Negate to maintain A into B convention
		collision.setPenetrationDepth(Vector3f.dot(Vector3f.sub(globalA, globalB, null), lNormal));
		collision.setContactB(localB, globalB);			
		collision.setColliderB(this);
		*/
		
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
		
		Vector3f leftPosition = volume.supportMap(LEFT_VECTOR, true);
		Vector3f rightPosition = volume.supportMap(RIGHT_VECTOR, true);
		Vector3f frontPosition = volume.supportMap(FRONT_VECTOR, true);
		Vector3f backPosition = volume.supportMap(BACK_VECTOR, true);
		
		int xMin = getGridX(leftPosition.x);	
		int xMax = getGridX(rightPosition.x);
		int zMin = getGridZ(backPosition.z);
		int zMax = getGridZ(frontPosition.z);
		
		int leftIndex = upperLeft(leftPosition.x, backPosition.z) ? 0 : 1;
		int rightIndex = upperLeft(rightPosition.x, frontPosition.z) ? 0 : 1;
		int startX = xMin * 2 + leftIndex;
		int endX = xMax * 2 + rightIndex;
		
		for(int z = zMin; z <= zMax; z++) {
			if(z < 0) continue;
			if(z >= heights[0].length - 1) break;
			for(int x = startX; x <= endX; x++) {
				if(x < 0) continue;
				if(x >= (heights.length - 1) * 2) break;
				planars.add(generatePlanar(x / 2, z, x % 2 == 0));
			}
		}
		
		return planars;
	}
	
	//TODO: xPosition needs to be localized with respect to the Landscape for this to work with any input. Make sure all calls localize.
	private int getGridX(float localX) {
		float x = localX - position.x + halfWidth;
		int gridX = (int) Math.floor(x / gridSizeX);
		if(gridX < 0) return -1;
		if(gridX >= heights.length - 1) return heights.length - 1;
		return gridX;
	}
	
	//TODO: zPosition needs to be localized with respect to the Landscape for this to work with any input. Make sure all calls localize.
	private int getGridZ(float localZ) {
		float z = localZ - position.z + halfDepth;
		int gridZ = (int) Math.floor(z / gridSizeZ);
		if(gridZ < 0) return -1;
		if(gridZ >= heights[0].length - 1) return heights[0].length - 1;
		return gridZ;
	}
	
	//TODO: x and z need to be localized with respect to the Landscape for this to work with any input. Make sure all calls localize.
	private boolean upperLeft(float localX, float localZ) {
		float xCoordinate = (localX + halfWidth) % gridSizeX / gridSizeX;
		float zCoordinate = (localZ + halfDepth) % gridSizeZ / gridSizeZ;
		return upperLeftCoords(xCoordinate, zCoordinate);
	}
	
	private boolean upperLeftCoords(float xCoordinate, float zCoordinate) {
		return xCoordinate <= 1f - zCoordinate;
	}
	
	//TODO: x and z need to be localized with respect to the Landscape for this to work with any input. Make sure all calls localize.
	public float getHeight(float localX, float localZ) {
		float x = localX - position.x + halfWidth;
		float z = localZ - position.z + halfDepth;
						
		int gridX = (int) Math.floor(x / gridSizeX);
		int gridZ = (int) Math.floor(z / gridSizeZ);
		
		if(gridX >= xPoints - 1 || gridZ >= zPoints - 1|| gridX < 0 || gridZ < 0) return Float.NaN;
		
		float xCoordinate = x % gridSizeX / gridSizeX;
		float zCoordinate = z % gridSizeZ / gridSizeZ;
		Vector2f pollCoordinates = new Vector2f(xCoordinate, zCoordinate);
				
		float height = Float.NaN;
		if(xCoordinate <= 1 - zCoordinate) {
			height = barryCentric3D(new Vector3f(0, heights[gridX][gridZ], 0), new Vector3f(1, heights[gridX + 1][gridZ], 0),
					new Vector3f(0, heights[gridX][gridZ + 1], 1), pollCoordinates);
		} else {
			height = barryCentric3D(new Vector3f(1, heights[gridX + 1][gridZ], 0),
					new Vector3f(1, heights[gridX + 1][gridZ + 1], 1), new Vector3f(0, heights[gridX][gridZ + 1], 1),
					pollCoordinates);
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
		this.gridSizeX = (heights.length > 0) ? width / (float)(heights.length - 1) : 0;
	}

	public float getDepth() {
		return depth;
	}

	public void setDepth(float depth) {
		this.depth = depth;
		this.halfDepth = depth * 0.5f;
		this.gridSizeZ = (heights.length > 0) ? depth / (float)(heights[0].length - 1) : 0;
	}
	
	public float getHeight() {
		return height;
	}
	
	public void setHeight(float height) {
		float scale = height / this.height;
		for(int i = 0; i < heights.length; i++) {
			for(int j = 0; j < heights[0].length; j++) {
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
		Body terrainBody = new Body();
		Landscape landscape = new Landscape(Type.SOLID, 2f, 1f, 10f, new float[][]{{1f,1f,10f,10f},{1f,1f,10f,10f}}, null);
		landscape.setID(2);
		terrainBody.addVolume(landscape);
		
		Body sphereBody = new Body();
		Sphere sphere = new Sphere(1f, Type.SOLID, null);
		sphere.setID(1);
		sphereBody.addVolume(sphere);
		sphereBody.setPosition(0.5f, 5f, -0.5f);
		
		Collision[] collisions = landscape.testForResolutions(sphere);
		if(collisions == null) return;
		
		for(Collision collision : collisions) {
			System.out.println(collision + " with normal " + collision.getSeparatingAxis(sphere) + " and depth " + collision.getPenetrationDepth());
			System.out.println(collision.getColliderA());
			System.out.println("Global A: " + collision.getGlobalContactA());
			System.out.println("Global B: " + collision.getGlobalContactB());
		}
	}
	
}
