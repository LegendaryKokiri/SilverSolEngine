package silverSol.engine.physics.d3.collider.volume;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.lwjgl.util.vector.Vector3f;

import silverSol.engine.physics.d3.collider.Collider;
import silverSol.engine.physics.d3.collision.Collision;
import silverSol.engine.physics.d3.det.narrow.algs.SepEdge;
import silverSol.engine.physics.d3.det.narrow.algs.SepPlane;
import silverSol.math.geometry.Triangle;

public class Mesh extends Volume {
		
	private Set<Hull> constituents;
	
	public Mesh(Type collisionType, Object colliderData) {
		super(collisionType, colliderData);
	}
	
	public Mesh(Vector3f[] vertices, int[] indices, Type collisionType, Object colliderData) {
		super(collisionType, colliderData);
		this.constituents = new HashSet<>();
		this.buildHulls(vertices, indices);
	}
	
	private void buildHulls(Vector3f[] vertices, int[] indices) {
		if(indices.length % 3 != 0) System.err.println("Mesh.buildHulls() WARNING: indices.length is not a multiple of 3. Geometry will be lost.");
		
		// Generate faces and adjacency map
		Set<Triangle> faces = new HashSet<>();
		Map<Triangle, Integer> faceIndices = new HashMap<>();
		Map<Triangle, Set<Triangle>> adjacencies = new HashMap<>();
		this.generateFaces(vertices, indices, faces, faceIndices, adjacencies);
		
		while(faces.size() > 0) {
			// Decompose mesh and separate hull
			Set<Triangle> hull = buildHull(faces, adjacencies);
			
			List<Vector3f> usedHullVertices = new ArrayList<>();
			Map<Integer, Integer> hullIndexMap = new HashMap<>();
			int numHullVertices = 0;
			
			// Build constituent hull from triangle set
			int[] hullIndices = new int[hull.size() * 3];
			int i = 0;
			for(Triangle face : hull) {
				// Find starting index and build face
				int startingIndex = faceIndices.get(face);
				for(int v = 0; v < 3; v++) {
					int index = indices[startingIndex + v];
					
					if(!hullIndexMap.containsKey(index)) {
						usedHullVertices.add(vertices[index]);
						hullIndexMap.put(index, numHullVertices++);
					}
					
					hullIndices[i++] = hullIndexMap.get(index);
				}
			}
			
			Hull constituent = new Hull(usedHullVertices.toArray(new Vector3f[0]), hullIndices, this.type, null);
			constituent.setID(this.ID);
			this.constituents.add(constituent);
		}
		
		return;
	}
	
	/**
	 * Given a list of vertices and indices connecting them into triangular faces,
	 * generate a map that keys a given face in the mesh to all other faces adjacent to that face.
	 * @param vertices The vertices of the mesh
	 * @param indices The indices connecting the vertices into triangular faces
	 * @param faces The set in which to save the faces
	 * @param faceIndices The map in which to key faces to their starting index in indices
	 * @param adjacencies The map in which to key faces to all other adjacent faces
	 */
	private void generateFaces(Vector3f[] vertices, int[] indices, Set<Triangle> faces, Map<Triangle, Integer> faceIndices, Map<Triangle, Set<Triangle>> adjacencies) {
		Triangle[] faceArray = new Triangle[indices.length / 3];
		Map<Integer, Map<Integer, Set<Triangle>>> edgeLookup = new HashMap<>();
		
		// Loop over all triangles to initialize adjacency list and populate lookup table
		for(int i = 0; i + 2 < indices.length; i += 3) {
			// Build face
			Triangle face = new Triangle(vertices[indices[i]], vertices[indices[i+1]], vertices[indices[i+2]], false);
			
			// Populate collections
			faceArray[i / 3] = face;
			faces.add(face);
			faceIndices.put(face, i);
			adjacencies.put(face, new HashSet<>());
			
			// Key face in lookup table by reversed edge indices
			for(int e = 0; e < 3; e++) {
				int e0 = indices[i + e];
				int e1 = indices[i + ((e + 1) % 3)];
				if(!edgeLookup.containsKey(e1)) edgeLookup.put(e1, new HashMap<>());
				if(!edgeLookup.get(e1).containsKey(e0)) edgeLookup.get(e1).put(e0, new HashSet<>());
				edgeLookup.get(e1).get(e0).add(face);
			}
		}
		
		// Loop over all triangles to populate adjacency list
		for(int i = 0; i + 2 < indices.length; i += 3) {
			for(int e = 0; e < 3; e++) {
				int e0 = indices[i + e];
				int e1 = indices[i + ((e + 1) % 3)];
				
				if(!edgeLookup.containsKey(e0)) continue;
				if(!edgeLookup.get(e0).containsKey(e1)) continue;
				
				Triangle face = faceArray[i / 3];
				for(Triangle adjacentFace : edgeLookup.get(e0).get(e1)) {
					adjacencies.get(face).add(adjacentFace);
				}
			}
		}
	}
	
	private Set<Triangle> buildHull(Set<Triangle> meshFaces, Map<Triangle, Set<Triangle>> adjacencies) {
		Set<Triangle> hull = new HashSet<>();
		Set<Triangle> candidates = new HashSet<>(); // Triangles that may belong in the hull
		Set<Triangle> considered = new HashSet<>(); // Triangles that have already been evaluated to be added to the hull
		
		// Pop an item out of the mesh set and add it to candidates
		Triangle starter = null;
		for(Triangle meshFace : meshFaces) {
			starter = meshFace;
			break;
		}
		
		if(starter == null) throw new IllegalArgumentException("Cannot build a hull from zero mesh faces.");
		meshFaces.remove(starter);
		candidates.add(starter);
		
		// Construct a convex hull from a subset of the mesh faces
		hullLoop:
		while(candidates.size() > 0) {
			// Select an arbitrary face from candidate set
			Triangle candidate = null;
			for(Triangle nextCandidate : candidates) {
				candidate = nextCandidate;
				break;
			}
			
			// Pop selected face from the set and mark it as considered
			candidates.remove(candidate);
			considered.add(candidate);
			
			// Check the candidate for convexity with the hull
			for(Triangle hullFace : hull) {
				if(!convexTriangles(hullFace, candidate)) continue hullLoop;
			}
			
			// If all convexity tests are passed, add the candidate to the hull
			hull.add(candidate);
			
			// Mark all unconsidered adjacent faces to new hull faceas candidates
			for(Triangle adjacency : adjacencies.get(candidate)) {
				if(considered.contains(adjacency)) continue;
				candidates.add(adjacency);
			}
		}
		
		// Decompose mesh by removing hull faces from mesh
		for(Triangle hullFace : hull) {
			meshFaces.remove(hullFace);
		}
		
		return hull;
	}
	
	/**
	 * Returns whether or not two triangles with a shared edge are convex to one another
	 * @param t1 The first triangle
	 * @param t2 The second triangle
	 * @return true if the two triangles are convex to one another, false otherwise
	 */
	private boolean convexTriangles(Triangle t1, Triangle t2) {
		Vector3f planePoint = t1.getT0();
		Vector3f normal = t1.getNormal();
		return Vector3f.dot(Vector3f.sub(t2.getT0(), planePoint, null), normal) <= 0 &&
				Vector3f.dot(Vector3f.sub(t2.getT1(), planePoint, null), normal) <= 0 &&
				Vector3f.dot(Vector3f.sub(t2.getT2(), planePoint, null), normal) <= 0;
	}

	@Override
	public Vector3f supportMap(Vector3f globalDirection, boolean global) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Vector3f[] raycast(Vector3f globalOrigin, Vector3f globalDirection, float maxLength, boolean global) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collision[] testForCollisions(Volume volume) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collision[] testForResolutions(Volume volume) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SepPlane[] getSeparatingPlanes(Planar planar) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SepEdge[] getSeparatingEdges(Planar planar) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collider clone() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void calculateEndpoints() {
		// TODO Auto-generated method stub
		
	}
	
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		Vector3f[] vertices = new Vector3f[]{
				new Vector3f(0f, 0f, 0f), new Vector3f(1f, 0f, 0f), new Vector3f(0f, 0f, 1f), new Vector3f(1f, 0f, 1f), new Vector3f(0.5f, 1f, 0.5f),
				new Vector3f(2f, 0f, 0f), new Vector3f(2f, 0f, 2f), new Vector3f(1.5f, 1f, 1.5f)};
		int[] indices = new int[] {
				4, 0, 2, 4, 1, 0, 4, 3, 1, 4, 2, 3,
				7, 1, 3, 7, 5, 1, 7, 6, 5, 7, 3, 6};
		Mesh mesh = new Mesh(vertices, indices, Type.SOLID, null);
		return;
	}

}
