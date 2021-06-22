package silverSol.engine.physics.d3.det.narrow.algs;

import org.lwjgl.util.vector.Vector3f;

import silverSol.engine.physics.d3.body.Body;
import silverSol.engine.physics.d3.collider.volume.Capsule;
import silverSol.engine.physics.d3.collider.volume.Planar;
import silverSol.engine.physics.d3.collider.volume.Volume;
import silverSol.engine.physics.d3.collider.volume.Volume.Type;
import silverSol.engine.physics.d3.collision.Collision;
import silverSol.engine.physics.d3.det.narrow.algs.SeparatingAxis.Resolution;

public class SAT {
	
	public static Collision detect(Volume v1, Volume v2) {
		return run(v1, v2, false);
	}
	
	public static Collision run(Volume v1, Volume v2) {
		return run(v1, v2, true);
	}
	
	public static Collision run(Volume v1, Volume v2, boolean detailed) {		
		Collision collision = new Collision();
		collision.setPenetrationDepth(Float.POSITIVE_INFINITY);
		
		checkAxes(collision, v1.getSeparatingAxes(v2), v1, v2, true);
		if(Float.isNaN(collision.getPenetrationDepth())) return null;
		
		checkAxes(collision, v2.getSeparatingAxes(v1), v1, v2, false);
		if(Float.isNaN(collision.getPenetrationDepth())) return null;
		
		collision.setColliderA(v1);
		collision.setColliderB(v2);
		
		Vector3f localA = v1.supportMap(collision.getSeparatingAxis(v2), false);
		collision.setContactA(localA, v1.toGlobalPosition(localA));
		
		Vector3f localB = v2.supportMap(collision.getSeparatingAxis(v1), false);
		collision.setContactB(localB, v2.toGlobalPosition(localB));
		
		return collision;
	}
	
	private static void checkAxes(Collision collision, SeparatingAxis[] axes, Volume v1, Volume v2, boolean sAxis1) {		
		for(SeparatingAxis sAxis : axes) {
			Vector3f axis = new Vector3f(sAxis.getAxis());
			Resolution resolution = sAxis.getResolution();
			
			//sAxis1 is true if the separating axis belongs to v1 and false if it belongs to v2.
			float penetration = sAxis1 ? Math.abs(checkAxis(axis, v1, v2, resolution)) : Math.abs(checkAxis(axis, v2, v1, resolution));
			
			//Maintain convention of pointing from A into B
			if(!sAxis1) {
				axis.negate(axis);
				penetration *= -1f;
			}
			
			if(!Float.isNaN(penetration)) {
				if(penetration < collision.getPenetrationDepth() && resolution != Resolution.NONE) {
					collision.setSeparatingAxis(axis);
					collision.setPenetrationDepth(penetration);
				}
			} else {
				collision.setPenetrationDepth(Float.NaN);
				return;
			}
		}		
	}
	
	private static float checkAxis(Vector3f axis, Volume v1, Volume v2, Resolution resolution) {
		Vector3f nAxis = axis.negate(null);
		
		float min1 = Vector3f.dot(v1.supportMap(nAxis, true), axis);
		float max1 = Vector3f.dot(v1.supportMap(axis, true), axis);
		float min2 = Vector3f.dot(v2.supportMap(nAxis, true), axis);
		float max2 = Vector3f.dot(v2.supportMap(axis, true), axis);
		
		return getPenetration(min1, max1, min2, max2, resolution);
	}
	
	private static float getPenetration(float min1, float max1, float min2, float max2, Resolution resolution) {
		float leftPen = max1 - min2;
		float rightPen = max2 - min1;
		
		if(leftPen < 0) return Float.NaN; //Block 1 to the left of Block 2
		if(rightPen < 0) return Float.NaN; //Block 1 to the right of Block 2
				
		if(resolution == Resolution.FORWARD || resolution == Resolution.NONE) return -leftPen;
		if(resolution == Resolution.BACKWARD) return rightPen;
		return leftPen < rightPen ? -leftPen : rightPen;
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
		System.out.println("B Local Contact = " + collision.getLocalContact(capsule));
	}
	
}
