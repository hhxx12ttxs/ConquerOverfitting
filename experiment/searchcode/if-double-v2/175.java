package plugins.adufour.activemeshes.mesh;

import icy.system.thread.ThreadUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Stack;

import javax.vecmath.Point3d;

/**
 * Utility class handling various operations on a mesh, including topological operations
 * 
 * @author Alexandre Dufour
 * 
 */
public class MeshTopology
{
	private static final double	MIN_RESAMPLE_FACTOR	= 0.6;
	private static final double	MAX_RESAMPLE_FACTOR	= 1.4;
	
	private final Mesh			mesh;
	private double				resolution;
	
	private final ArrayList<Vertex>	tmpVertices = new ArrayList<Vertex>();
	private final ArrayList<Face>		tmpFaces = new ArrayList<Face>();
	
	public MeshTopology(Mesh mesh, double resolution)
	{
		this.mesh = mesh;
		this.resolution = resolution;
	}
	
	public double getMeshResolution()
	{
		return resolution;
	}
	
	public void setResolution(double desiredResolution)
	{
		while (this.resolution > 2 * desiredResolution)
			refine();
		
		// this.resolution = desiredResolution;
		// reSample(MIN_RESAMPLE_FACTOR, MAX_RESAMPLE_FACTOR, 1);
	}
	
	private static <T> void copy(ArrayList<T> source, ArrayList<T> dest)
	{
		dest.clear();
		dest.ensureCapacity(source.size());
		
		for (T item : source)
			dest.add(item);
	}
	
	public boolean	updating	= false;
	
	public boolean	lock		= false;
	
	public boolean beginUpdate(boolean returnNow)
	{
		if ((this.lock) && (returnNow))
		{
			return false;
		}
		this.updating = true;
		
		synchronized (this)
		{
			copy(this.mesh.vertices, this.tmpVertices);
			
			copy(this.mesh.faces, this.tmpFaces);
		}
		
		return true;
	}
	
	public void endUpdate()
	{
		synchronized (this)
		{
			copy(this.tmpVertices, this.mesh.vertices);
			
			copy(this.tmpFaces, this.mesh.faces);
		}
		
		this.updating = false;
	}
	
	/**
	 * Refine the mesh by doubling its resolution
	 * 
	 * @throws MeshException
	 */
	public void refine() throws MeshException
	{
		beginUpdate(false);
		
		{
			tmpFaces.clear();
			tmpFaces.ensureCapacity(mesh.faces.size() * 4);
			
			for (Vertex v : tmpVertices)
				if (v != null) v.neighbors.clear();
			
			for (Face f : tmpFaces)
			{
				int centerv1v2 = addVertexBetween(f.v1, f.v2);
				
				int centerv2v3 = addVertexBetween(f.v2, f.v3);
				
				int centerv3v1 = addVertexBetween(f.v3, f.v1);
				
				addFace(f.v1, centerv1v2, centerv3v1);
				addFace(centerv1v2, f.v2, centerv2v3);
				addFace(centerv2v3, f.v3, centerv3v1);
				addFace(centerv1v2, centerv2v3, centerv3v1);
			}
			
			resolution *= 0.5f;
		}
		
		endUpdate();
	}
	
	/**
	 * Multiplies all vertex coordinates by the given factor
	 * 
	 * @param factor
	 */
	public void scale(double factor)
	{
		for (Vertex v : tmpVertices)
			v.position.scale(factor);
	}
	
	public void reSample(double minVolume) throws MeshException, MeshSplittingException
	{
		while (!beginUpdate(true))
			ThreadUtil.sleep(10);
		
		{
			updating = true;
			double minLength = resolution * MIN_RESAMPLE_FACTOR, maxLength = resolution * MAX_RESAMPLE_FACTOR;
			
			// if there are 2 faces only in the mesh, it should be destroyed
			
			if (tmpFaces.size() == 2)
			{
				throw new MeshException(mesh, "The mesh is now empty");
			}
			
			boolean change = true;
			
			int cpt = -1;
			
			while (change)
			{
				cpt++;
				
				// we are looking for 2 faces f1 = a-b-c1 and f2 = b-a-c2
				// such that they share an edge a-b that is either
				// - lower than the low-threshold (Resolution * min)
				// or
				// - higher than the high-threshold (Resolution * max)
				
				change = false;
				
				for (int i = 0; i < tmpFaces.size(); i++)
				{
					boolean split = false, merge = false;
					
					Face f1 = tmpFaces.get(i);
					Integer v1 = 0, v2 = 0, f1v3 = 0;
					
					// Look first for f1 = a-b-c1
					
					Integer[] f1v123 = { f1.v1, f1.v2, f1.v3 };
					Integer[] f1v231 = { f1.v2, f1.v3, f1.v1 };
					Integer[] f1v312 = { f1.v3, f1.v1, f1.v2 };
					
					for (int v = 0; v < 3; v++)
					{
						v1 = f1v123[v];
						v2 = f1v231[v];
						f1v3 = f1v312[v];
						
						double edgeLength = tmpVertices.get(v1).position.distance(tmpVertices.get(v2).position);
						
						if (edgeLength < minLength)
						{
							merge = true;
							break;
						}
						
						if (edgeLength > maxLength)
						{
							split = true;
							break;
						}
					}
					
					if (split == merge)
					{
						// they are necessarily both false
						continue; // to the next face
					}
					
					// If the code runs here, f1 has been found,
					// so now we must find f2 and c2
					change = true;
					Face f2 = null;
					Integer f2v3 = -1;
					
					for (int j = i + 1; j < tmpFaces.size(); j++)
					{
						f2 = tmpFaces.get(j);
						
						// check if f2 contains edge a-b in any way possible
						if (v1.compareTo(f2.v1) == 0 && v2.compareTo(f2.v3) == 0)
						{
							f2v3 = f2.v2;
							break;
						}
						else if (v1.compareTo(f2.v2) == 0 && v2.compareTo(f2.v1) == 0)
						{
							f2v3 = f2.v3;
							break;
						}
						else if (v1.compareTo(f2.v3) == 0 && v2.compareTo(f2.v2) == 0)
						{
							f2v3 = f2.v1;
							break;
						}
					}
					
					if (f2v3.compareTo(0) < 0)
					{
						// here, the mesh is inconsistent: vA and vB are linked in a
						// unique face (it should be 2)
						// so remove the face and the link between vA and vB
						
						System.err.println("Problem in face " + i + ":");
						System.err.print("  " + f1.v1.intValue() + " : ");
						for (Integer nn : tmpVertices.get(v1).neighbors)
							System.err.print(nn.intValue() + "  ");
						System.err.println();
						System.err.print("  " + f1.v2.intValue() + " : ");
						for (Integer nn : tmpVertices.get(v2).neighbors)
							System.err.print(nn.intValue() + "  ");
						System.err.println();
						System.err.print("  " + f1.v3.intValue() + " : ");
						for (Integer nn : tmpVertices.get(f1.v3).neighbors)
							System.err.print(nn.intValue() + "  ");
						System.err.println();
						
						tmpFaces.remove(f1);
						
						// pointsTMP.get(v1).neighbors.remove(v2);
						// pointsTMP.get(v2).neighbors.remove(v1);
					}
					else if (merge)
					{
						merge(f1, f2, v1, v2, f1v3, f2v3, minVolume);
					}
					else
					// split
					{
						// check if the edge should be split or inverted
						if (!tmpVertices.get(f1v3).neighbors.contains(f2v3) && tmpVertices.get(f1v3).position.distance(tmpVertices.get(f2v3).position) < maxLength)
						{
							// invert the edge
							
							tmpFaces.remove(f1);
							tmpFaces.remove(f2);
							
							// the two vertices must not be neighbors anymore
							tmpVertices.get(v1).neighbors.remove(v2);
							tmpVertices.get(v2).neighbors.remove(v1);
							
							// create the two new faces
							addFace(f1v3, v1, f2v3);
							addFace(f2v3, v2, f1v3);
						}
						else
						{
							// split the edge
							
							tmpFaces.remove(f1);
							tmpFaces.remove(f2);
							
							// create the vertex in the middle of the edge and add its new neighbors
							Integer c = addVertexBetween(v1, v2);
							if (c < 0) c = -(c + 1);
							
							// the two vertices must not be neighbors anymore
							tmpVertices.get(v1).neighbors.remove(v2);
							tmpVertices.get(v2).neighbors.remove(v1);
							
							// create 2 new faces per old face
							addFace(v1, c, f1v3);
							addFace(f1v3, c, v2);
							addFace(v1, f2v3, c);
							addFace(c, f2v3, v2);
							
						}
					}
					
					break;
				}
				
				// prevent infinite loop
				if (cpt > tmpVertices.size()) change = false;
			}
		}
		endUpdate();
	}
	
	/**
	 * Merges two points of an edge (supposedly too close) by replacing the edge and its two
	 * corresponding faces by a single point in its center
	 * 
	 * @param f1
	 *            the first face containing the edge
	 * @param f2
	 *            the second face containing the edge
	 * @param v1
	 *            the first vertex of the edge
	 * @param v2
	 *            the second vertex of the edge
	 * @param f1v3
	 *            the third vertex of f1
	 * @param f2v3
	 *            the third vertex of f2
	 * @param minVolume
	 *            the minimum volume of a mesh (used in case a mesh division is detected)
	 * @throws ContourSplittingException
	 *             if the merge operation leads to a mesh splitting
	 */
	private void merge(Face f1, Face f2, Integer v1, Integer v2, Integer f1v3, Integer f2v3, double minVolume) throws MeshSplittingException
	{
		// Check if the edge to merge is the base of a tetrahedron
		// if so, delete the whole tetrahedron
		if (tmpVertices.get(f1v3).neighbors.size() == 3)
		{
			deleteTetrahedron(f1v3, v1, v2);
			return;
		}
		if (tmpVertices.get(f2v3).neighbors.size() == 3)
		{
			deleteTetrahedron(f2v3, v2, v1);
			return;
		}
		
		Vertex vx1 = tmpVertices.get(v1);
		Vertex vx2 = tmpVertices.get(v2);
		
		for (Integer n : vx1.neighbors)
			if (vx2.neighbors.contains(n) && n.compareTo(f1v3) != 0 && n.compareTo(f2v3) != 0)
			{
				splitContourAtVertices(v1, v2, n, minVolume);
				return;
			}
		
		// Here, the normal merge operation can be implemented
		
		// remove the 2 faces
		tmpFaces.remove(f1);
		tmpFaces.remove(f2);
		
		// move v1 to the middle of v1-v2
		vx1.position.interpolate(vx2.position, 0.5);
		
		// remove v2 from its neighborhood...
		for (Integer n : vx2.neighbors)
		{
			Vertex vxn = tmpVertices.get(n);
			if (vxn == null) continue;
			vxn.neighbors.remove(v2);
			
			// ...and add v2's neighbors to v1
			// except for f1v3 and f2v3 and ... v1 !
			if (n.compareTo(f1v3) != 0 && n.compareTo(f2v3) != 0 && n.compareTo(v1) != 0)
			{
				vx1.neighbors.add(n);
				vxn.neighbors.add(v1);
			}
		}
		
		// all the faces pointing to v2 must now point to v1
		for (Face f : tmpFaces)
		{
			if (f.v1.compareTo(v2) == 0)
			{
				f.v1 = v1;
			}
			else if (f.v2.compareTo(v2) == 0)
			{
				f.v2 = v1;
			}
			else if (f.v3.compareTo(v2) == 0)
			{
				f.v3 = v1;
			}
		}
		
		// delete everything
		tmpVertices.set(v2, null);
	}
	
	/**
	 * If it doens't exist, adds a new vertex in the center of the specified vertices to the vertex
	 * list
	 * 
	 * @param vIndex1
	 *            the first vertex index
	 * @param vIndex2
	 *            the second vertex index
	 * @return A signed integer i defined as follows: <br>
	 *         i>=0 : vertex is new and stored at position i <br>
	 *         i<0 : vertex already existed at position -(i+1)
	 */
	private Integer addVertexBetween(Integer vIndex1, Integer vIndex2)
	{
		Point3d newPosition = new Point3d(tmpVertices.get(vIndex1).position);
		newPosition.interpolate(tmpVertices.get(vIndex2).position, 0.5);
		
		return addVertex(newPosition);
	}
	
	/**
	 * If it doens't exist, adds a new vertex in the center of the specified vertices to the vertex
	 * list
	 * 
	 * @param point
	 *            the point to add
	 * @return A signed integer i defined as follows: <br>
	 *         i>=0 : vertex is new and stored at position i <br>
	 *         i<0 : vertex already existed at position -(i+1)
	 */
	public Integer addVertex(Point3d point)
	{
		Integer index, nullIndex = -1;
		
		for (index = 0; index < tmpVertices.size(); index++)
		{
			Vertex v = tmpVertices.get(index);
			
			if (v == null)
			{
				// The current position in the vertex list is null.
				// To avoid growing the data array, this position
				// can be reused to store a new vertex if needed
				nullIndex = index;
				continue;
			}
			
			if (v.position.distanceSquared(point) < resolution * 0.00001) return -index - 1;
		}
		
		// if code runs until here, the vertex must be created
		Vertex v = new Vertex(new Point3d(point));
		
		// if there is a free spot in the ArrayList, use it
		if (nullIndex >= 0)
		{
			index = nullIndex;
			tmpVertices.set(index, v);
		}
		else
		{
			tmpVertices.add(v);
		}
		
		return index;
	}
	
	public void addFace(Integer v1, Integer v2, Integer v3) throws MeshException
	{
		if (v1.compareTo(v2) == 0 || v1.compareTo(v3) == 0 || v2.compareTo(v3) == 0) throw new MeshException(mesh, "wrong face declared: (" + v1 + "," + v2 + "," + v3 + ")");
		
		if (v1 < 0) v1 = -v1 - 1;
		if (v2 < 0) v2 = -v2 - 1;
		if (v3 < 0) v3 = -v3 - 1;
		
		{
			Vertex v = tmpVertices.get(v1);
			if (!v.neighbors.contains(v2)) v.neighbors.add(v2);
			if (!v.neighbors.contains(v3)) v.neighbors.add(v3);
		}
		{
			Vertex v = tmpVertices.get(v2);
			if (!v.neighbors.contains(v1)) v.neighbors.add(v1);
			if (!v.neighbors.contains(v3)) v.neighbors.add(v3);
		}
		{
			Vertex v = tmpVertices.get(v3);
			if (!v.neighbors.contains(v1)) v.neighbors.add(v1);
			if (!v.neighbors.contains(v2)) v.neighbors.add(v2);
		}
		
		tmpFaces.add(new Face(v1, v2, v3));
	}
	
	/**
	 * Splits the current contour using the 'cutting' face defined by the given vertices. <br>
	 * 
	 * <pre>
	 * How this works:
	 *  - separate all vertices on each side of the cutting face (without considering the vertices of the cutting face), 
	 *  - separate all faces touching at least one vertex of each group (will include faces touching the cutting face),
	 *  - create a contour with each group of vertices and faces,
	 *  - add the cutting face and its vertices to each created contour
	 * </pre>
	 * 
	 * @param v1
	 * @param v2
	 * @param v3
	 * @param minVolume
	 * @throws ContourSplittingException
	 * 
	 */
	private void splitContourAtVertices(Integer v1, Integer v2, Integer v3, double minVolume) throws MeshSplittingException
	{
		Mesh[] children = new Mesh[2];
		
		ArrayList<Integer> visitedIndexes = new ArrayList<Integer>(tmpVertices.size());
		visitedIndexes.add(v1);
		visitedIndexes.add(v2);
		visitedIndexes.add(v3);
		
		int seed;
		
		for (int child = 0; child < 2; child++)
		{
			// pick any non-null and non-visited vertex as seed
			for (seed = 0; seed < tmpVertices.size(); seed++)
				if (tmpVertices.get(seed) != null && !visitedIndexes.contains(seed)) break;
			
			if (seed == tmpVertices.size())
			{
				System.err.println("Mesh splitting error (pass " + (child + 1) + "): no valid seed found");
				throw new MeshSplittingException(mesh, new Mesh[0]);
			}
			
			ArrayList<Face> newFaces = new ArrayList<Face>();
			ArrayList<Vertex> newPoints = new ArrayList<Vertex>(tmpVertices.size());
			for (int i = 0; i < tmpVertices.size(); i++)
				newPoints.add(null);
			
			extractVertices(seed, visitedIndexes, tmpVertices, newPoints);
			extractFaces(newPoints, tmpFaces, newFaces);
			
			// Add the vertices of the cutting face
			for (Integer v : new Integer[] { v1, v2, v3 })
			{
				Vertex vx = tmpVertices.get(v);
				
				if (vx == null) System.err.println(v.intValue() + " is null");
				
				// create a clone for each vertex (position and neighbors)
				Vertex newV = new Vertex(tmpVertices.get(v));
				
				// check the neighborhood to remove the neighbors that belong to
				// the other mesh
				// (these neighbors will point to null in the current point
				// list)
				for (int i = 0; i < newV.neighbors.size(); i++)
				{
					Integer n = newV.neighbors.get(i);
					if (n.compareTo(v1) != 0 && n.compareTo(v2) != 0 && n.compareTo(v3) != 0 && newPoints.get(n) == null)
					{
						newV.neighbors.remove(n);
						i--;
					}
				}
				newPoints.set(v, newV);
			}
			
			for (Face f : newFaces)
				if (f.contains(v1) && f.contains(v2))
				{
					// if the edge v1-v2 appears counter-clockwisely in f,
					// the new face must be clockwise and vice-versa
					newFaces.add(f.isCounterClockwise(v1, v2) ? new Face(v1, v3, v2) : new Face(v1, v2, v3));
					break;
				}
			
			Mesh newContour = new Mesh(newPoints, newFaces, mesh.getResolution(), mesh.getVTKMesh() != null);
			
			newContour.setT(mesh.getT());
			newContour.updateMassCenter();
			
			if (newContour.getDimension(2) >= minVolume) children[child] = newContour;
		}
		
		if (children[0] == null)
		{
			if (children[1] == null) throw new MeshSplittingException(mesh, new Mesh[0]);
			
			copy(children[1].vertices, this.tmpVertices);
			copy(children[1].faces, this.tmpFaces);
		}
		else
		{
			if (children[1] != null) throw new MeshSplittingException(mesh, children);
			
			copy(children[0].vertices, this.tmpVertices);
			copy(children[0].faces, this.tmpFaces);
		}
	}
	
	private void extractVertices(Integer seedIndex, ArrayList<Integer> visitedIndices, ArrayList<Vertex> oldPoints, ArrayList<Vertex> newPoints)
	{
		Stack<Integer> seeds = new Stack<Integer>();
		seeds.add(seedIndex);
		
		while (!seeds.isEmpty())
		{
			Integer seed = seeds.pop();
			
			if (visitedIndices.contains(seed)) continue;
			
			visitedIndices.add(seed);
			Vertex v = oldPoints.get(seed);
			newPoints.set(seed, v);
			
			for (int i = 0; i < v.neighbors.size(); i++)
			{
				Integer n = v.neighbors.get(i);
				if (oldPoints.get(n) == null)
				{
					v.neighbors.remove(n);
					i--;
					continue;
				}
				seeds.push(n);
			}
		}
	}
	
	private void extractFaces(ArrayList<Vertex> pointsList, ArrayList<Face> sourceFacesList, ArrayList<Face> targetFacesList)
	{
		for (int i = 0; i < sourceFacesList.size(); i++)
		{
			Face f = sourceFacesList.get(i);
			
			if (pointsList.get(f.v1) != null || pointsList.get(f.v2) != null || pointsList.get(f.v3) != null)
			{
				targetFacesList.add(f);
				sourceFacesList.remove(i--);
			}
		}
	}
	
	/**
	 * Deletes a tetrahedron from the mesh, and fill the hole with a new face
	 * 
	 * @param topv
	 *            the vertex at the top of the tetrahedron
	 * @param v1
	 *            one of the three vertices at the base of the tetrahedron
	 * @param v2
	 *            another of the vertices at the base of the tetrahedron
	 */
	private void deleteTetrahedron(Integer topv, Integer v1, Integer v2)
	{
		// check for mesh inconsistency
		if (v1.compareTo(v2) == 0) throw new IllegalArgumentException("Invalid topology detected in deleteTetrahedron()");
		
		// find the third bottom vertex
		Integer v3 = -1;
		for (Integer n : tmpVertices.get(topv).neighbors)
		{
			if (n.compareTo(v1) == 0 || n.compareTo(v2) == 0) continue;
			v3 = n;
			break;
		}
		
		// remove the top vertex from the neighborhood
		tmpVertices.get(v1).neighbors.remove(topv);
		tmpVertices.get(v2).neighbors.remove(topv);
		tmpVertices.get(v3).neighbors.remove(topv);
		
		// delete the top vertex
		tmpVertices.set(topv, null);
		
		// find the three faces and delete them
		for (int i = 0; i < tmpFaces.size(); i++)
		{
			Face f = tmpFaces.get(i);
			if (f.v1.compareTo(topv) == 0 || f.v2.compareTo(topv) == 0 || f.v3.compareTo(topv) == 0) tmpFaces.remove(i--);
		}
		
		// create the new face to replace the tetrahedron base
		tmpFaces.add(new Face(v1, v2, v3));
	}
	
	/**
	 * Extract the individual meshes bundled into this mesh structure. If the current topology only
	 * contains a single mesh, this mesh is returned. This method can be used if the result of an
	 * initialization yields a unique mesh structure containing multiple non-connected meshes
	 * representing different objects
	 * 
	 * @return
	 */
	public Collection<Mesh> extractMeshes()
	{
		ArrayList<Mesh> meshes = new ArrayList<Mesh>();
		
		ArrayList<Integer> visitedIndexes = new ArrayList<Integer>(mesh.vertices.size());
		
		int seed;
		
		while (true)
		{
			for (seed = 0; seed < mesh.vertices.size(); seed++)
				if (mesh.vertices.get(seed) != null && !visitedIndexes.contains(seed)) break;
			
			if (seed == mesh.vertices.size()) break;
			
			ArrayList<Face> newFaces = new ArrayList<Face>();
			ArrayList<Vertex> newPoints = new ArrayList<Vertex>(mesh.vertices.size());
			for (int i = 0; i < mesh.vertices.size(); i++)
				newPoints.add(null);
			
			extractVertices(seed, visitedIndexes, mesh.vertices, newPoints);
			extractFaces(newPoints, mesh.faces, newFaces);
			
			meshes.add(new Mesh(newPoints, newFaces, mesh.getResolution(), mesh.getVTKMesh() != null));
		}
		
		return meshes;
	}
	
}

