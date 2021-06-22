package silverSol.engine.physics.d3.det.narrow.algs2;

import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Vector3f;

import silverSol.engine.physics.d3.body.Body;
import silverSol.engine.physics.d3.collider.volume.Capsule;
import silverSol.engine.physics.d3.collider.volume.Hull;
import silverSol.engine.physics.d3.collider.volume.OBB;
import silverSol.engine.physics.d3.collider.volume.Planar;
import silverSol.engine.physics.d3.collider.volume.Sphere;
import silverSol.engine.physics.d3.collider.volume.Volume;
import silverSol.engine.physics.d3.collider.volume.Volume.Type;
import silverSol.engine.physics.d3.collision.Collision;
import silverSol.math.MatrixMath;
import silverSol.math.SegmentMath;
import silverSol.math.TriangleMath;
import silverSol.parsers.model.ModelParser;

public class GJK {

	private static final int MAX_ITERATIONS = 50;
	private static final Vector3f ORIGIN = new Vector3f();
	private static final Vector3f INITIAL_DIR = new Vector3f(1f, 0f, 0f);
	private static final float EPSILON = 1E-3f;
	private static final float EPSILON_SQ = EPSILON * EPSILON;
		
	public static Collision detect(Volume v1, Volume v2) {
		if(run(v1, v2) != null) return generateCollision(v1, v2);
		return null;
	}
	
	public static Simplex run(Volume v1, Volume v2) {
		Simplex simplex = new Simplex();
		Vector3f direction = new Vector3f();
		Vector3f closest = null;
		
		updateDirection(simplex, v1, v2, direction);
		addSupport(simplex, v1, v2, direction, ORIGIN);
		
		for(int iterationCount = 0; iterationCount < MAX_ITERATIONS; iterationCount++) {
			closest = reduceAboutClosest(simplex);
			if(closest == null) return null;
			if(closest.lengthSquared() < EPSILON_SQ) return simplex;
			
			updateDirection(simplex, v1, v2, direction);
			if(!addSupport(simplex, v1, v2, direction, closest)) return null;
		}
				
		return null;
	}
	
	private static void updateDirection(Simplex simplex, Volume v1, Volume v2, Vector3f direction) {
		switch(simplex.getNumVertices()) {
			case 0:
				directFromVoid(simplex, v1, v2, direction);
				break;
			case 1:
				directFromPoint(direction);
				break;
			case 2:
				directFromSegment(simplex, direction);
				break;
			case 3:
				directFromTriangle(simplex, direction);
				break;
			default:
				break;
		}
	}
	
	private static Vector3f reduceAboutClosest(Simplex simplex) {
		Support[] cso = simplex.getCso();
		
		switch(simplex.getNumVertices()) {
			case 1:
				return cso[0].getS();
			case 2:
				return SegmentMath.closestPointTo(ORIGIN, cso[0].getS(), cso[1].getS());
			case 3:
				return TriangleMath.closestPointTo(ORIGIN, cso[0].getS(), cso[1].getS(), cso[2].getS());
			case 4:
				if(reduceTetrahedron(simplex)) return ORIGIN;
				return TriangleMath.closestPointTo(ORIGIN, cso[0].getS(), cso[1].getS(), cso[2].getS());
			default:
				return null;
		}
	}
	
	private static boolean reduceTetrahedron(Simplex simplex) {			
		Support[] cso = simplex.getCso();
		
		Vector3f a = cso[0].getS();
		Vector3f b = cso[1].getS();
		Vector3f c = cso[2].getS();
		Vector3f d = cso[3].getS();
		
		Vector3f ad = Vector3f.sub(d, a, null);
		Vector3f bd = Vector3f.sub(d, b, null);
		Vector3f cd = Vector3f.sub(d, c, null);
		Vector3f d0 = d.negate(null);
		
		if(Vector3f.dot(Vector3f.cross(ad, bd, null), d0) > 0) simplex.removeVertex(2);
		else if(Vector3f.dot(Vector3f.cross(bd, cd, null), d0) > 0) simplex.removeVertex(0);
		else if(Vector3f.dot(Vector3f.cross(cd, ad, null), d0) > 0) simplex.removeVertex(1);
		else return true;
		
		return false;
	}
	
	//Picking a direction from one volume to the other tends to lead to fewer iterations.
	private static void directFromVoid(Simplex simplex, Volume v1, Volume v2, Vector3f direction) {
		Vector3f.sub(v2.getPosition(), v1.getPosition(), direction);
		if(direction.lengthSquared() < EPSILON) direction.set(INITIAL_DIR);
		direction.normalise(direction);
	}
	
	//Reverse the previous direction to maximize simplex size.
	private static void directFromPoint(Vector3f direction) {
		direction.negate(direction);
	}
	
	//Direct from the line segment to the origin with a triple cross product
	private static void directFromSegment(Simplex simplex, Vector3f direction) {
		Support[] cso = simplex.getCso();
		Vector3f a = cso[0].getS();
		Vector3f b = cso[1].getS();
		
		Vector3f ab = Vector3f.sub(b, a, null);
		Vector3f.cross(Vector3f.cross(ab, a.negate(null), null), ab, direction);
		
		//Handle the degenerate case where the segment contains the origin by picking any normal vector
		if(direction.lengthSquared() < EPSILON) direction.set(0f, 0f, 0f);
		else direction.normalise(direction);
	}
	
	//Direct from the triangle's face, and point the vector towards the origin.
	private static void directFromTriangle(Simplex simplex, Vector3f direction) {
		Support[] cso = simplex.getCso();
		Vector3f a = cso[0].getS();
		Vector3f b = cso[1].getS();
		Vector3f c = cso[2].getS();
		
		Vector3f ac = Vector3f.sub(c, a, null);
		Vector3f ab = Vector3f.sub(b, a, null);
		
		Vector3f.cross(ac, ab, direction);
		if(Vector3f.dot(direction, a.negate(null)) < 0) direction.negate(direction);
		
//		System.out.println("Direction = " + direction);
		direction.normalise(direction);
	}
	
	private static boolean addSupport(Simplex simplex, Volume v1, Volume v2, Vector3f globalDir, Vector3f closest) {
		Vector3f s1 = v1.supportMap(globalDir, true);
		Vector3f s2 = v2.supportMap(globalDir.negate(null), true);
		Vector3f s = Vector3f.sub(s1, s2, null);
		
		//If the support point doesn't make it past the origin, then this simplex can never contain the origin.
		float searchComponent = Vector3f.dot(globalDir, s);
		if(/*searchComponent < 0 || */searchComponent < Vector3f.dot(closest, s)) return false;
		
		simplex.expand(s, s1, s2);
		return true;
	}
	
	private static Collision generateCollision(Volume v1, Volume v2) {
		Collision collision = new Collision();
		collision.setColliderA(v1);
		collision.setColliderB(v2);
		return collision;
	}
	
	public static void main(String[] args) {
		Sphere sphere1 = new Sphere(1f, Type.SOLID, null);
			sphere1.setID(1);
		Sphere sphere2 = new Sphere(1f, Type.SOLID, null);
			sphere2.setID(2);
		
		Capsule capsule1 = new Capsule(1f, 1f, Type.SOLID, null);
			capsule1.setID(1);
		Capsule capsule2 = new Capsule(4.3f, 1f, Type.SOLID, null);
				capsule2.setBodyOffset(MatrixMath.createTransformation(new Vector3f(0f, 5.3f, 0f), new Vector3f(), new Vector3f(1f, 1f, 1f)));
		
		OBB obb1 = new OBB(1f, 1f, 1f, Type.SOLID, null);
			obb1.setID(1);
		OBB obb2 = new OBB(1f, 1f, 1f, Type.SOLID, null);
			obb2.setID(2);
		
		Planar planar = new Planar(
				new Vector3f[]{
						new Vector3f(-70.0f, -32.156864f, -80.0f),
						new Vector3f(-69.0f, -22.745098f, -81.0f),
						new Vector3f(-70.0f, -32.156864f, -81.0f)
				}, Type.SOLID, null);
			planar.setID(2);
			
		Hull hull = ModelParser.parseHull("/models/Test Cube.ssm", 1f, Type.SOLID, null);
			hull.setID(2);
		
		Body body1 = new Body();
			body1.setPosition(-68.69792f, -20.78523f, -80.747925f);
			body1.setRotation(new Quaternion(0.0f, -0.9720304f, 0.0f, -0.23484148f));
			body1.updateTransformation();
			body1.addVolume(capsule2);
		Body body2 = new Body();
			body2.setPosition(0f, 0f, 0f);
			body2.updateTransformation();
			body2.addVolume(planar);
		
		System.out.println("Straight down: " + capsule2.supportMap(new Vector3f(0f, -1f, 0f), true));
		
		long timer = System.nanoTime();
		Simplex simplex12 = run(capsule2, planar);
		long elapsedTime = System.nanoTime() - timer;
		System.out.println("Finished in " + (elapsedTime / 1E9) + " seconds.");
		
		System.out.println(capsule2.getPosition() + " - " + planar.getPosition());
		System.out.println((simplex12 != null) ? "Collision found!\n" + simplex12 : "No collision found!");
	}
	
}
