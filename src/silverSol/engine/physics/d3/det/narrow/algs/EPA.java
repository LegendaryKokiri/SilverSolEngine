package silverSol.engine.physics.d3.det.narrow.algs;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import silverSol.engine.physics.d3.body.Body;
import silverSol.engine.physics.d3.collider.volume.Capsule;
import silverSol.engine.physics.d3.collider.volume.Planar;
import silverSol.engine.physics.d3.collider.volume.Volume;
import silverSol.engine.physics.d3.collider.volume.Volume.Type;
import silverSol.engine.physics.d3.collision.Collision;
import silverSol.math.MatrixMath;

public class EPA {
	
	private static final int MAX_ITERATIONS = 50;
	
	public static Collision run(Simplex simplex, Volume v1, Volume v2) {
		if(simplex == null) return null;
		
		simplex.epaExpand(v1, v2);
		
		Polytype polytype = new Polytype(simplex);
		int iterationCount = 0;
		
		while(true) {
			/*
			if(iterationCount == 50) {
				System.out.println("EPA.run(): Hit iteration 50. Colliders:");
				System.out.println(v1);
				System.out.println(v1.getTransformation());
				System.out.println(v2);
				System.out.println(v2.getTransformation());
			}
			
			if(iterationCount >= 50) {
				Collision intermediate = polytype.generateCollision(v1, v2);
				System.out.println("EPA.run(): Iteration " + iterationCount + " yields collision " + intermediate.getSeparatingAxis() + " of depth " + intermediate.getPenetrationDepth());
			}
			*/
			
			if(!polytype.expand(simplex, v1, v2) || iterationCount >= MAX_ITERATIONS) {
//				if(v1 instanceof OBB && v2 instanceof OBB) System.out.println("===EPA.run() on two OBBs===");
				return polytype.generateCollision(v1, v2);
			}
			
			iterationCount++;
		}
	}
	
	
	//TODO: When the spheres are in exactly the same position, the algorithm fails. Fix this.
	public static void main(String[] args) {
		Matrix4f p = MatrixMath.createTransformation(new Vector3f(), new Vector3f());
		Matrix4f c = MatrixMath.createTransformation(new Vector3f(0f, 5f, 0f), new Vector3f());
		
		Planar planar = new Planar(new Vector3f[]{new Vector3f(-5f, 0f, -5f), new Vector3f(-5f, 0f, 5f),
				new Vector3f(5f, 0f, -5f), new Vector3f(5f, 0f, 5f)}, Type.SOLID, null);
		Capsule capsule = new Capsule(4.3f, 1f, Type.SOLID, null);
		
		Body bodyS = new Body();
		bodyS.setTransformation(p);
		bodyS.addVolume(planar);
		Body bodyC = new Body();
		bodyC.setTransformation(c);
		bodyC.addVolume(capsule);
				
		long timer = System.nanoTime();
		System.out.println("---GJK---");
		Simplex simplex = GJK.run(planar, capsule);
		if(simplex != null) System.out.println("Collision detected by GJK.\n" + simplex);
		System.out.println("---EPA---");
		Collision collision = EPA.run(simplex, planar, capsule);
		long elapsed = System.nanoTime() - timer;
		
		System.out.println("Finished in " + (elapsed / 1E9) + " seconds.");
		if(collision != null) {
			System.out.println("Collision found!");
			System.out.println("Separating Axis = " + collision.getSeparatingAxis());
			System.out.println("Penetration Depth = " + collision.getPenetrationDepth());
			System.out.println("Contact A = " + collision.getLocalContactA());
			System.out.println("Contact B = " + collision.getLocalContactB());
		} else {
			System.out.println("No collision found.");
		}
		System.out.println();
	}

}
