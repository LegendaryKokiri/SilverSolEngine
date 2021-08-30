package silverSol.engine.physics.d3.det.narrow.algs;

import org.lwjgl.util.vector.Vector3f;

import silverSol.engine.physics.d3.body.Body;
import silverSol.engine.physics.d3.collider.volume.Capsule;
import silverSol.engine.physics.d3.collider.volume.OBB;
import silverSol.engine.physics.d3.collider.volume.Planar;
import silverSol.engine.physics.d3.collider.volume.Volume;
import silverSol.engine.physics.d3.collider.volume.Volume.Type;
import silverSol.engine.physics.d3.collision.Collision;
import silverSol.math.SegmentMath;

public class SAT {
	
	private static final float EPSILON = 1e-3f;
	
	private static class MinFeatures {
		Separator s1;
		Separator s2;
		
		public MinFeatures() {
			s1 = null;
			s2 = null;
		}
		
		public void setMinFace(Separator separator) {
			s1 = separator;
			s2 = null;
		}
		
		public void setMinEdges(Separator s1, Separator s2) {
			this.s1 = s1;
			this.s2 = s2;
		}
		
		public void updateCollision(Collision collision, Volume v1, Volume v2) {
			collision.setColliderA(v1);
			collision.setColliderB(v2);
			
			Vector3f[] globals = null;
			
			if(s2 == null) { //Face normal is minimum separation direction
				
				//TODO: For this prototypical face, we will accept any point on the incident face.
				//TODO: However, the proper thing to do would be to clip the plane. You saved the PowerPoint for reference.
				Vector3f localA = v1.supportMap(collision.getSeparatingAxis(v2), false);
				Vector3f localB = v2.supportMap(collision.getSeparatingAxis(v1), false);
				globals = new Vector3f[] {v1.toGlobalPosition(localA), v2.toGlobalPosition(localB)};
				collision.setContactA(localA, globals[0]);
				collision.setContactB(localB, globals[1]);
				
			} else { //Edge normal is minimum separation direction
				SepEdge e1 = (SepEdge) s1;
				SepEdge e2 = (SepEdge) s2;
				globals = SegmentMath.closestPoints(e1.getEnd1(), e1.getEnd2(), e2.getEnd1(), e2.getEnd2());
				collision.setContactA(v1.toLocalPosition(globals[0]), globals[0]);
				collision.setContactB(v2.toLocalPosition(globals[1]), globals[1]);
			}
			
			//Maintain convention of pointing from A into B for cross-product edges
			if(s2 != null && Vector3f.dot(collision.getSeparatingAxis(), Vector3f.sub(globals[1], globals[0], null)) < 0f) {
				collision.setSeparatingAxis(collision.getSeparatingAxis().negate(null));
				System.out.println("Negating face");
			}
		}
	}
	
	/**
	 * Detects whether or not a collision occurs with the separating axis theorem
	 * @param v1 The first volume
	 * @param v2 The second volume
	 * @return A Collision object if a collision occurs, else null
	 */
	public static Collision detect(Planar p, Volume v) {
		return run(p, v, false, false);
	}
	
	/**
	 * Detects whether or not a collision occurs with the separating axis theorem
	 * and calculates resolution information
	 * @param v1 The first volume
	 * @param v2 The second volume
	 * @return A Collision object if a collision occurs, else null
	 */
	public static Collision run(Planar p, Volume v) {
		return run(p, v, false, true);
	}
	
	public static Collision run(Planar p, Volume v, boolean resolveOnNormal) {
		return run(p, v, resolveOnNormal, true);
	}
	
	private static Collision run(Planar p, Volume v, boolean normalResolve, boolean detailed) {		
		Collision c = new Collision();
		c.setPenetrationDepth(Float.POSITIVE_INFINITY);
		
		MinFeatures min = new MinFeatures();
		
		Vector3f disp = Vector3f.sub(v.getPosition(), p.getPosition(), null);
				
		if(!checkFaces(c, min, p.getSeparatingPlanes(null), p, v, disp, false, true)) return null;
		if(!checkFaces(c, min, v.getSeparatingPlanes(p), p, v, disp, true, !normalResolve)) return null;
		if(!checkEdges(c, min, p.getSeparatingEdges(null), v.getSeparatingEdges(p), p, v, disp, !normalResolve)) return null;
		
		min.updateCollision(c, p, v);
		
		return c;
	}
	
	private static boolean checkFaces(Collision collision, MinFeatures min, SepPlane[] faces, Volume v1, Volume v2, Vector3f disp,
			boolean negateAxes, boolean resolveDirections) {		
		for(SepPlane face : faces) {
			Vector3f axis = negateAxes ? new Vector3f(face.getDirection().negate(null)) : new Vector3f(face.getDirection());
			float penetration = checkAxis(collision, axis, v1, v2, disp);
			
			if(Float.isNaN(penetration)) return false;
			
			if(penetration < collision.getPenetrationDepth() && resolveDirections) {
				collision.setSeparatingAxis(axis);
				collision.setPenetrationDepth(penetration);
				min.setMinFace(face);
			}
		}
		
		return true;
	}
	
	private static boolean checkEdges(Collision collision, MinFeatures min, SepEdge[] edges1, SepEdge[] edges2, Volume v1, Volume v2,
			Vector3f disp, boolean resolveDirections) {
		for(SepEdge edge1 : edges1) {
			for(SepEdge edge2 : edges2) {
				Vector3f axis = Vector3f.cross(edge1.getDirection(), edge2.getDirection(), null);
 				if(axis.lengthSquared() < EPSILON) continue; //Ignore parallel edges
				axis.normalise(axis);
								
				float penetration = checkAxis(collision, axis, v1, v2, disp);
				
				if(Float.isNaN(penetration)) return false;
				
				if(penetration < collision.getPenetrationDepth() && resolveDirections) {
					collision.setSeparatingAxis(axis);
					collision.setPenetrationDepth(penetration);
					min.setMinEdges(edge1, edge2);
				}
			}
		}
		
		return true;
	}
	
	private static float checkAxis(Collision collision, Vector3f axis, Volume v1, Volume v2, Vector3f disp) {
		Vector3f nAxis = axis.negate(null);
				
		float min1 = Vector3f.dot(v1.supportMap(nAxis, true), axis);
		float max1 = Vector3f.dot(v1.supportMap(axis, true), axis);
		float min2 = Vector3f.dot(v2.supportMap(nAxis, true), axis);
		float max2 = Vector3f.dot(v2.supportMap(axis, true), axis);
				
		return getPenetration(min1, max1, min2, max2);
	}
	
	private static float getPenetration(float min1, float max1, float min2, float max2) {
		float leftPen = max1 - min2;
		float rightPen = max2 - min1;
		
		if(leftPen < 0) return Float.NaN; //Block 1 to the left of Block 2
		if(rightPen < 0) return Float.NaN; //Block 1 to the right of Block 2
		
		return Math.min(leftPen, rightPen);
	}
	
	public static void main(String[] args) {
		Planar planar = new Planar(new Vector3f[]{new Vector3f(-1f, 0f, 0f), new Vector3f(0f, 1f, 0f), new Vector3f(1f, 0f, 0f)},
				Type.SOLID, null);
		Capsule capsule = new Capsule(4f, 1f, Type.SOLID, null);
		OBB obb = new OBB(1f, 1f, 1f, Type.SOLID, null);
		
		Body body1 = new Body();
		body1.addVolume(planar);
		
		Body body2 = new Body();
		body2.setPosition(0f, 2f, 0f);
		body2.addVolume(capsule);
		
		Body body3 = new Body();
		body3.setPosition(1.4f, 1.4f, 0f);
		body3.addVolume(obb);
		
		/*
		Collision collision = SAT.run(planar, capsule);
		System.out.println(collision);
		System.out.println("Separating Axis = " + collision.getSeparatingAxis());
		System.out.println("Penetration Depth = " + collision.getPenetrationDepth());
		System.out.println("Local Contact = " + collision.getLocalContact(capsule));	
		*/
		
		Collision collision = SAT.run(planar, obb);
		System.out.println(collision);
		if(collision != null) {
			System.out.println("Separating Axis = " + collision.getSeparatingAxis());
			System.out.println("Penetration Depth = " + collision.getPenetrationDepth());
			System.out.println("Local Contact = " + collision.getLocalContact(obb));
		}
	}
	
}
