package silverSol.engine.physics.d3.det.narrow.algs2;

import org.lwjgl.util.vector.Matrix3f;
import org.lwjgl.util.vector.Vector3f;

import silverSol.engine.physics.d3.body.Body;
import silverSol.engine.physics.d3.collider.volume.Capsule;
import silverSol.engine.physics.d3.collider.volume.Planar;
import silverSol.engine.physics.d3.collider.volume.Sphere;
import silverSol.engine.physics.d3.collider.volume.Volume;
import silverSol.engine.physics.d3.collider.volume.Volume.Type;
import silverSol.engine.physics.d3.collision.Collision;
import silverSol.engine.physics.d3.det.narrow.algs2.Polytype.Face;
import silverSol.math.MatrixMath;
import silverSol.math.TriangleMath;
import silverSol.math.VectorMath;

public class EPA {
	
	private static final int MAX_ITERATIONS = 100;
	private static final Vector3f ORIGIN = new Vector3f();
	private static final Vector3f[] AXES = new Vector3f[]{
		new Vector3f(1f, 0f, 0f), new Vector3f(0f, 1f, 0f), new Vector3f(0f, 0f, 1f),
		new Vector3f(-1f, 0f, 0f), new Vector3f(0f, -1f, 0f), new Vector3f(0f, 0f, -1f)};
	private static final float THIRD_PI = (float) Math.PI / 3f;
	private static final float EPSILON = 1E-3f;

	public static Collision run(Simplex simplex, Volume v1, Volume v2) {
		if(simplex == null) return null;
		
		expandSimplex(simplex, v1, v2);
		simplex.orientFaces();
		
		Polytype polytype = new Polytype(simplex);
				
		for(int iterationCount = 0; iterationCount < MAX_ITERATIONS; iterationCount++) {
			Face closestFace = polytype.getClosestFace();
			
			if(closestFace == null) {
				System.err.println("EPA destroyed the polytype.");
				break;
			}
			
			Support s = findSupport(v1, v2, closestFace.getNormal());
			if(Vector3f.dot(s.getS(), closestFace.getNormal()) - closestFace.getDistance() < EPSILON) break;
			
			polytype.removeVisible(s);
			polytype.patch(s);
		}
		
		return generateCollision(polytype.getClosestFace(), v1, v2);
	}
	
	private static void expandSimplex(Simplex simplex, Volume v1, Volume v2) {
		switch(simplex.getNumVertices()) {
			case 1:
				expandPoint(simplex, v1, v2);
			case 2:
				expandSegment(simplex, v1, v2);
			case 3:
				expandTriangle(simplex, v1, v2);
			default:
				break;
		}
	}
	
	private static void expandPoint(Simplex simplex, Volume v1, Volume v2) {
		Support s0 = simplex.getCso()[0];
		
		for(Vector3f axis : AXES) {
			Support s = findSupport(v1, v2, axis);
			if(Vector3f.sub(s.getS(), s0.getS(), null).lengthSquared() > EPSILON) {
				simplex.expand(s);
				break;
			}
		}
	}
	
	private static void expandSegment(Simplex simplex, Volume v1, Volume v2) {
		Support[] cso = simplex.getCso();
		Vector3f ab = Vector3f.sub(cso[1].getS(), cso[0].getS(), null);
		
		Vector3f axis = VectorMath.leastSignificantAxis(ab);
		Vector3f searchDir = Vector3f.cross(ab, axis, null).normalise(null);
		Matrix3f rotation = MatrixMath.createRotation(ab, THIRD_PI);
		
		for(int i = 0; i < 6; i++) {
			Support s = findSupport(v1, v2, searchDir);
			if(s.getS().lengthSquared() > EPSILON) {
				simplex.expand(s);
				break;
			}
			
			VectorMath.mulMatrix(rotation, searchDir, searchDir);
		}
	}
	
	private static void expandTriangle(Simplex simplex, Volume v1, Volume v2) {
		Support[] cso = simplex.getCso();
		Vector3f ab = Vector3f.sub(cso[1].getS(), cso[0].getS(), null);
		Vector3f ac = Vector3f.sub(cso[2].getS(), cso[0].getS(), null);
		Vector3f direction = Vector3f.cross(ab, ac, null).normalise(null);
		
		Support s = findSupport(v1, v2, direction);
		if(s.getS().lengthSquared() <= EPSILON) {
			direction.negate(direction);
			s = findSupport(v1, v2, direction);
		}
		
		simplex.expand(s);
	}
	
	private static Collision generateCollision(Face closest, Volume v1, Volume v2) {		
		if(closest == null) return null;
		
		Collision collision = new Collision();
		collision.setColliderA(v1);
		collision.setColliderB(v2);
		
		//TODO: Replace these contact calculations with a single call to TriangleMath.getParametricWeights() and doing the math.
		Vector3f closestPoint = TriangleMath.closestPointTo(ORIGIN, closest.getT1().getS(), closest.getT2().getS(), closest.getT3().getS());
		Vector3f contactA = TriangleMath.closestPointTo(ORIGIN, closest.getT1().getS1(), closest.getT2().getS1(), closest.getT3().getS1());
		Vector3f contactB = TriangleMath.closestPointTo(ORIGIN, closest.getT1().getS2(), closest.getT2().getS2(), closest.getT3().getS2());
		
		Vector3f normal = new Vector3f(closest.getNormal());
		
		collision.setSeparatingAxis(normal);
		collision.setPenetrationDepth(closestPoint.length());
		collision.setContactA(v1.toLocalPosition(contactA), contactA);
		collision.setContactB(v2.toLocalPosition(contactB), contactB);
		
		return collision;
	}
	
	private static Support findSupport(Volume v1, Volume v2, Vector3f globalDir) {
		Vector3f s1 = v1.supportMap(globalDir, true);
		Vector3f s2 = v2.supportMap(globalDir.negate(null), true);
		Vector3f s = Vector3f.sub(s1, s2, null);
		return new Support(s, s1, s2);
	}
	
	//TODO: When the spheres are in exactly the same position, the algorithm fails. Fix this.
	public static void main(String[] args) {
		Sphere sphere1 = new Sphere(2f, Type.SOLID, null);
		Sphere sphere2 = new Sphere(2f, Type.SOLID, null);
		
		Capsule capsule2 = new Capsule(4.3f, 1f, Type.SOLID, null);
		capsule2.setBodyOffset(MatrixMath.createTransformation(new Vector3f(0f, 5.3f, 0f), new Vector3f(), new Vector3f(1f, 1f, 1f)));
		
		Planar planar = new Planar(
				new Vector3f[]{
						new Vector3f(10f, -10f, 0f),
						new Vector3f(0f, 0f, 0f),
						new Vector3f(-10f, -10f, 0f)
				}, Type.SOLID, null);
			planar.setID(2);
		
		Body body1 = new Body();
		body1.setPosition(0f, -0.1f, 0f);
		body1.updateTransformation();
		body1.addVolume(capsule2);
		
		Body body2 = new Body();
		body2.setPosition(0f, 0f, 0f);
		body2.updateTransformation();
		body2.addVolume(planar);
		
		long timer = System.nanoTime();
		System.out.println("---GJK---");
		Simplex simplex = GJK.run(capsule2, planar);
		if(simplex != null) System.out.println("Collision detected by GJK.\n" + simplex);
		System.out.println("---EPA---");
		Collision collision = EPA.run(simplex, capsule2, planar);
		long elapsed = System.nanoTime() - timer;
		
		System.out.println("Finished in " + (elapsed / 1E9) + " seconds.");
		if(collision != null) {
			System.out.println("Collision found!");
			System.out.println("Separating Axis = " + collision.getSeparatingAxis());
			System.out.println("Penetration Depth = " + collision.getPenetrationDepth());
			System.out.println("Contact Points: " + collision.getGlobalContactA() + "; " + collision.getGlobalContactB());
		} else {
			System.out.println("No collision found.");
		}
		System.out.println();
	}
	
}
