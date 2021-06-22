package silverSol.route;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

import org.lwjgl.util.vector.Vector3f;

public class Route {

	private final int numPoints;
	private List<RoutePoint> routePoints; //HAHA! See what I did there?! ...If you pronounce it right, anyway... see?
	
	private class RoutePoint {
		private Vector3f point;
		private List<Integer> connections;
		
		public RoutePoint(Vector3f point) {
			this.point = point;
			this.connections = new ArrayList<>();
		}
		
		public void addConnection(int routePointIndex) {
			connections.add(routePointIndex);
		}
	}
	
	public Route(Vector3f... points) {
		this.numPoints = points.length;
		this.routePoints = new ArrayList<>();
		for(int i = 0; i < points.length; i++) {
			routePoints.add(new RoutePoint(points[i]));
//			if(i > 0) addConnection(i - 1, i, true);
		}
	}
	
	public Route(List<Vector3f> points) {
		this.numPoints = points.size();
		this.routePoints = new ArrayList<>();
		for(int i = 0; i < points.size(); i++) {
			routePoints.add(new RoutePoint(points.get(i)));
//			if(i > 0) addConnection(i - 1, i, true);
		}
	}
	
	public int getPointCount() {
		return numPoints;
	}
	
	public void addConnection(boolean bothWays, int startPoint, int... endPoints) {
		if(startPoint < 0 || startPoint > routePoints.size() - 1) return;
		
		for(int endPoint : endPoints) {
			if(endPoint < 0 || endPoint > routePoints.size() - 1) continue;
			routePoints.get(startPoint).addConnection(endPoint);
			if(bothWays) routePoints.get(endPoint).addConnection(startPoint);
		}
	}
	
	public Vector3f[] getRoute(int startPoint, int targetPoint) {
		boolean[] visitedPoints = new boolean[routePoints.size()];
		int[] connections = new int[routePoints.size()];
		breadthFirstSearch(startPoint, targetPoint, visitedPoints, connections);
		
		if(!visitedPoints[targetPoint]) return null;
		
		Stack<Vector3f> routeStack = new Stack<>();
		for(int i = targetPoint; i != startPoint; i = connections[targetPoint]) {
			routeStack.push(routePoints.get(i).point);
		}
		routeStack.push(routePoints.get(startPoint).point);
		
		Vector3f[] route = new Vector3f[routeStack.size()];
		for(int i = 0; i < routeStack.size(); i++) {
			route[i] = routeStack.pop();
			System.out.println(i + " = " + route[i]);
		}
		
		return route;
	}
	
	private void breadthFirstSearch(int currentPoint, int targetPoint, boolean[] visitedPoints, int[] connections) {
		Queue<Integer> queue = new LinkedList<Integer>();
		visitedPoints[currentPoint] = true;
		queue.add(currentPoint);
		
		while(!queue.isEmpty()) {
			int point = queue.poll();
			for(int nextPoint : routePoints.get(point).connections) {
				if(!visitedPoints[nextPoint]) {
					connections[nextPoint] = point;
					visitedPoints[nextPoint] = true;
					queue.add(nextPoint);
				}
			}
		}
	}
	
	/*
	public Vector3f[] getRouteTo(Vector3f targetPoint) {
		if(routePoints.size() == 0) return null;
		
		boolean[] visitedPoints = new boolean[routePoints.size()];
		Stack<Vector3f> routeStack = new Stack<>();
		
		depthFirstSearch(targetPoint, 0, visitedPoints, 0, routeStack);
		
		Vector3f[] route = new Vector3f[routeStack.size()];
		for(int i = 0; i < route.length; i++) {
			route[i] = routeStack.pop();
		}
		
		return route;
	}
	
	private boolean depthFirstSearch(Vector3f targetPoint, int currentPoint, boolean[] visitedPoints, int count, Stack<Vector3f> pointStack) {
		visitedPoints[currentPoint] = true;
		count++;
				
		if(routePoints.get(currentPoint).point.equals(targetPoint)) {
			pointStack.add(routePoints.get(currentPoint).point);
			return true;
		}
		
		for(int nextPoint : routePoints.get(currentPoint).connections) {
			if(!visitedPoints[nextPoint]) {
				if(depthFirstSearch(targetPoint, nextPoint, visitedPoints, count, pointStack)) {
					pointStack.add(routePoints.get(currentPoint).point);
					return true;
				}
			}
		}
		
		return false;
	}
	*/
	
	public static void main(String[] args) {
		Vector3f[] routePoints = new Vector3f[6];
		for(int i = 0; i < routePoints.length; i++) {
			routePoints[i] = new Vector3f(i, i, i);
		}
		
		Route route = new Route(routePoints);
		route.addConnection(true, 0, 2, 1, 5);
		route.addConnection(true, 1, 2);
		route.addConnection(true, 2, 3, 4);
		route.addConnection(true, 3, 4, 5);
		
		Vector3f[] routeToPoint = route.getRoute(0, 4);
		
		for(Vector3f point : routeToPoint) {
			System.out.print(" --> " + point );
		}
		System.out.println();
	}
}
