package silverSol.engine.physics.d3.det.narrow.algs;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import silverSol.engine.physics.d3.body.Body;
import silverSol.engine.physics.d3.collider.volume.Capsule;
import silverSol.engine.physics.d3.collider.volume.Sphere;
import silverSol.engine.physics.d3.collider.volume.Volume;
import silverSol.engine.physics.d3.collider.volume.Volume.Type;
import silverSol.engine.physics.d3.collision.Collision;
import silverSol.math.MatrixMath;

public class EPA {
	
	private static final int MAX_ITERATIONS = 100;
	
	public static Collision run(Simplex simplex, Volume v1, Volume v2) {
		if(simplex == null) return null;
		
		simplex.epaExpand(v1, v2);
		Polytype polytype = new Polytype(simplex);
		int iterationCount = 0;
		
		while(true) {
			if(!polytype.expand(simplex, v1, v2) || iterationCount >= MAX_ITERATIONS) {
				return polytype.generateCollision(v1, v2);
			}
			
			iterationCount++;
		}
	}
	
	
	//TODO: When the spheres are in exactly the same position, the algorithm fails. Fix this.
	public static void main(String[] args) {
		Matrix4f s = new Matrix4f();
		s.m00 = -0.34104553f; s.m10 = 0.6572001f; s.m20 = 0.6721427f; s.m30 = 19.362846f;
		s.m01 = 0.3960543f; s.m11 = -0.5479998f; s.m21 = 0.73677516f; s.m31 = -27.18699f;
		s.m02 = 0.8525427f; s.m12 = 0.5174788f; s.m22 = -0.07339409f; s.m32 = 18.300608f;
		s.m03 = 0.0f; s.m13 = 0.0f; s.m23 = 0.0f; s.m33 = 1.0f;
		
		Matrix4f c = MatrixMath.createTransformation(new Vector3f(20f, -22.150982f, 20.0f), new Vector3f());
		
		Sphere sphere = new Sphere(2f, Type.SOLID, null);
		Capsule capsule = new Capsule(4.3f, 1f, Type.SOLID, null);
		
		Body bodyS = new Body();
		bodyS.setTransformation(s);
		bodyS.addVolume(sphere);
		Body bodyC = new Body();
		bodyC.setTransformation(c);
		bodyC.addVolume(capsule);
				
		long timer = System.nanoTime();
		System.out.println("---GJK---");
		Simplex simplex = GJK.run(sphere, capsule);
		if(simplex != null) System.out.println("Collision detected by GJK.\n" + simplex);
		System.out.println("---EPA---");
		Collision collision = EPA.run(simplex, sphere, capsule);
		long elapsed = System.nanoTime() - timer;
		
		System.out.println("Finished in " + (elapsed / 1E9) + " seconds.");
		if(collision != null) {
			System.out.println("Collision found!");
			System.out.println("Separating Axis = " + collision.getSeparatingAxis());
			System.out.println("Penetration Depth = " + collision.getPenetrationDepth());
		} else {
			System.out.println("No collision found.");
		}
		System.out.println();
	}

}
