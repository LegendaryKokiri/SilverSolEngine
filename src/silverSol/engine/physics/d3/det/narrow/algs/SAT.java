package silverSol.engine.physics.d3.det.narrow.algs;

import org.lwjgl.util.vector.Vector3f;

import silverSol.engine.physics.d3.body.Body;
import silverSol.engine.physics.d3.collider.volume.Capsule;
import silverSol.engine.physics.d3.collider.volume.Planar;
import silverSol.engine.physics.d3.collider.volume.Volume;
import silverSol.engine.physics.d3.collider.volume.Volume.Type;
import silverSol.engine.physics.d3.collision.Collision;

public class SAT {
	
	/**
	 * Detects whether or not a collision occurs with the separating axis theorem
	 * @param v1 The first volume
	 * @param v2 The second volume
	 * @return A Collision object if a collision occurs, else null
	 */
	public static Collision detect(Planar p, Volume v) {
		return run(p, v, false);
	}
	
	/**
	 * Detects whether or not a collision occurs with the separating axis theorem
	 * and calculates resolution information
	 * @param v1 The first volume
	 * @param v2 The second volume
	 * @return A Collision object if a collision occurs, else null
	 */
	public static Collision run(Planar p, Volume v) {
		return run(p, v, true);
	}
	
	public static Collision run(Planar p, Volume v, boolean detailed) {		
		Collision collision = new Collision();
		collision.setPenetrationDepth(Float.POSITIVE_INFINITY);
		
		Vector3f disp = Vector3f.sub(v.getPosition(), p.getPosition(), null);
				
		if(!checkAxes(collision, p.getSeparatingPlanes(null), p, v, disp)) return null;
		if(!checkAxes(collision, v.getSeparatingPlanes(p), p, v, disp)) return null;
				
		for(SepEdge pEdge : p.getSeparatingEdges(null)) {
			for(SepEdge vEdge : v.getSeparatingEdges(p)) {
				Vector3f axis = Vector3f.cross(pEdge.getDirection(), vEdge.getDirection(), null).normalise(null);
				if(!checkAxis(collision, axis, p, v, disp)) return null;
			}
		}
		
		collision.setColliderA(p);
		collision.setColliderB(v);
		
		Vector3f localA = p.supportMap(collision.getSeparatingAxis(v), false);
		collision.setContactA(localA, p.toGlobalPosition(localA));
		
		Vector3f localB = v.supportMap(collision.getSeparatingAxis(p), false);
		collision.setContactB(localB, v.toGlobalPosition(localB));
		
		return collision;
	}
	
	private static boolean checkAxes(Collision collision, SepPlane[] axes, Volume v1, Volume v2, Vector3f disp) {		
		for(SepPlane sAxis : axes) {
			Vector3f axis = new Vector3f(sAxis.getNormal());
			if(!checkAxis(collision, axis, v1, v2, disp)) return false;
		}
		
		return true;
	}
	
	private static boolean checkAxis(Collision collision, Vector3f axis, Volume v1, Volume v2, Vector3f disp) {
		//Maintain convention of pointing from A into B
		if(Vector3f.dot(axis, disp) < 0f) axis.negate(axis);
		Vector3f nAxis = axis.negate(null);
				
		float min1 = Vector3f.dot(v1.supportMap(nAxis, true), axis);
		float max1 = Vector3f.dot(v1.supportMap(axis, true), axis);
		float min2 = Vector3f.dot(v2.supportMap(nAxis, true), axis);
		float max2 = Vector3f.dot(v2.supportMap(axis, true), axis);
				
		float penetration = getPenetration(min1, max1, min2, max2);
		
		if(!Float.isNaN(penetration)) {			
			if(penetration < collision.getPenetrationDepth()) {
				collision.setSeparatingAxis(axis);
				collision.setPenetrationDepth(penetration);
			}
			
			return true;
		}
		
		return false;
	}
	
	private static float getPenetration(float min1, float max1, float min2, float max2) {
		float leftPen = max1 - min2;
		float rightPen = max2 - min1;
		
		if(leftPen < 0) return Float.NaN; //Block 1 to the left of Block 2
		if(rightPen < 0) return Float.NaN; //Block 1 to the right of Block 2
		
		return Math.min(leftPen, rightPen);
	}
	
	public static void main(String[] args) {
		Planar planar = new Planar(new Vector3f[]{new Vector3f(-1f, 0f, 0f), new Vector3f(0f, 0f, 1f), new Vector3f(1f, 0f, 0f)},
				Type.SOLID, null);
		Capsule capsule = new Capsule(4f, 1f, Type.SOLID, null);
		
		Body body1 = new Body();
		body1.addVolume(planar);
		
		Body body2 = new Body();
		body2.setPosition(0f, 2f, 0f);
		body2.addVolume(capsule);
		
		Collision collision = SAT.run(planar, capsule);
		System.out.println(collision);
		System.out.println("Separating Axis = " + collision.getSeparatingAxis());
		System.out.println("Penetration Depth = " + collision.getPenetrationDepth());
		System.out.println("Local Contact = " + collision.getLocalContact(capsule));
	}
	
}
