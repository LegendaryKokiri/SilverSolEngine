package silverSol.engine.physics.d3.det.narrow.algs;

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
import silverSol.parsers.model.ModelParser;

public class GJK {
	
	private static final int MAX_ITERATIONS = 50;
	
	public static Collision detect(Volume v1, Volume v2) {
		if(run(v1, v2) != null) {
			Collision collision = new Collision();
			collision.setColliderA(v1);
			collision.setColliderB(v2);
			return collision;
		}
		
		return null;
	}
	
	public static Simplex run(Volume v1, Volume v2) {
		Simplex simplex = new Simplex(v1, v2);
		int iterationCount = 0;
		
		while(iterationCount < MAX_ITERATIONS) {
			if(!simplex.evolve(v1, v2)) return null;
			if(simplex.containsOrigin()) return simplex;
			iterationCount++;
		}
				
		return null;
	}
	
	
	public static void main(String[] args) {
		Sphere sphere1 = new Sphere(1f, Type.SOLID, null);
			sphere1.setID(1);
		
		Capsule capsule1 = new Capsule(1f, 1f, Type.SOLID, null);
			capsule1.setID(1);
		Capsule capsule2 = new Capsule(4.3f, 1f, Type.SOLID, null);
				capsule2.setBodyOffset(MatrixMath.createTransformation(new Vector3f(0f, 5.3f, 0f), new Vector3f(), new Vector3f(1f, 1f, 1f)));
		
		OBB obb1 = new OBB(1f, 1f, 1f, Type.SOLID, null);
			obb1.setID(1);
		
		Planar planar = new Planar(
				new Vector3f[]{
						new Vector3f(-0.25f, -30.588236f, 0.75f),
						new Vector3f(0.75f, -33.72549f, -0.25f),
						new Vector3f(-0.25f, -30.588236f, -0.25f)
				}, Type.SOLID, null);
			planar.setBodyOffset(MatrixMath.createTransformation(
					new Vector3f(32.25f, 0f, 69.25f), new Vector3f(), new Vector3f(1f, 1f, 1f)));
			planar.setID(2);
			
		Hull hull = ModelParser.parseHull("/models/Test Cube.ssm", 1f, Type.SOLID, null);
			hull.setID(2);
		
		Body body1 = new Body();
			body1.setPosition(0f, 0f, 0f);
			body1.updateTransformation();
			body1.addVolume(sphere1);
		Body body2 = new Body();
			body2.setPosition(0f, 0f, 0f);
			body2.updateTransformation();
			body2.addVolume(obb1);
		
		long timer = System.nanoTime();
		Simplex simplex12 = run(sphere1, obb1);
		long elapsedTime = System.nanoTime() - timer;
		System.out.println("Finished in " + (elapsedTime / 1E9) + " seconds.");
		
		System.out.println((simplex12 != null) ? "Collision found! (12)" : "No collision found! (12)");
		if(simplex12 != null) System.out.println(simplex12);
	}
	
}
