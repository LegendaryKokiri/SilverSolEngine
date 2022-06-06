package silverSol.utils.algs;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import silverSol.utils.structs.Heap;
import silverSol.utils.structs.Heap.HeapType;

public abstract class Pathfinder<T extends Object> {
		
	private int maxNodes;
	
	public Pathfinder(int maxNodes) {
		this.maxNodes = maxNodes;
	}
	
	/**
	 * Finds a path from the start to the goal using the A* search algorithm
	 * @param start The starting node
	 * @param goal The goal node
	 * @param componentType The component type of an array of this Pathfinder's generic type
	 * @return An array listing any shortest path of steps to start from goal and route to goal, or null if no such path exists
	 */
	public T[] buildRoute(T start, T goal, Class<?> componentType) {
		Set<T> openSet = new HashSet<>();
		Heap<T> openHeap = new Heap<>(HeapType.MIN_HEAP, this.maxNodes);
		Map<T, T> parentNodes = new HashMap<>();
		
		Map<T, Float> fCost = new HashMap<>();
		Map<T, Float> gCost = new HashMap<>();
		
		fCost.put(start, this.costHeuristic(start, goal));
		gCost.put(start, 0f);
		
		openSet.add(start);
		openHeap.insert(start, 0f);
		
		while(!openSet.isEmpty()) {
			T current = openHeap.pop();
			if(this.equality(current, goal)) {
				return this.reconstructRoute(parentNodes, start, current, componentType);
			}
			
			openSet.remove(current);
			for(T neighbor : this.getNeighbors(current)) {
				float potentialG = gCost.get(current) + this.travelCost(current, neighbor);
				if(!gCost.containsKey(neighbor) || potentialG < gCost.get(neighbor)) {
					parentNodes.put(neighbor, current);
					gCost.put(neighbor, potentialG);
					fCost.put(neighbor, potentialG + costHeuristic(neighbor, goal));
					
					if(!openSet.contains(neighbor)) {
						openSet.add(neighbor);
						openHeap.insert(neighbor, fCost.get(neighbor));
					}
				}
			}
			
		}
		
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private T[] reconstructRoute(Map<T, T> parentNodes, T start, T goal, Class<?> componentType) {
		List<T> routeList = new ArrayList<>();
		routeList.add(goal);
		
		while((goal = parentNodes.get(goal)) != start) {
			routeList.add(goal);
		}
		
		Collections.reverse(routeList);
		T[] emptyT = (T[]) Array.newInstance(componentType, 0);
		T[] route = routeList.toArray(emptyT);
		return route;
	}
	
	/**
	 * Given two neighboring nodes, return the true cost of traveling between those nodes.
	 * @return The exact cost of traveling between neighboring nodes `start` and `end`.
	 */
	protected abstract float travelCost(T start, T end);
	
	/**
	 * Given a start and end node that may not be connected, return the estimated cost of traveling between those nodes.
	 * @return The estimated cost of traveling between `start` and `end`.
	 */
	protected abstract float costHeuristic(T start, T end);
	
	/**
	 * Given an element, return its neighbors
	 * @param element The element whose neighbors need to be found
	 * @return An array of all of that element's neighbors
	 */
	protected abstract T[] getNeighbors(T element);
	
	/**
	 * The equality operator that will be used for comparing nodes
	 * This defaults to element1.equals(element2);
	 * @param element1 The first element to evaluate
	 * @param element2 The second element to evaluate
	 * @return true if the two elements are equal, false otherwise
	 */
	protected boolean equality(T element1, T element2) {
		return element1.equals(element2);
	}
	
}
