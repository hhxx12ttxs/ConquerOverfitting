package plugins.adufour.activemeshes.mesh;

import java.util.ArrayList;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * Structural element of triangular mesh
 * 
 * @author Alexandre Dufour
 * 
 */
public class Face
{
	Integer	v1, v2, v3;
	
	/**
	 * Constructs a new mesh face with given vertices indices. Note that vertices must be given
	 * in counter-clockwise order.
	 * 
	 * @param v1
	 *            the first vertex index
	 * @param v2
	 *            the second vertex index
	 * @param v3
	 *            the third vertex index
	 */
	Face(Integer v1, Integer v2, Integer v3)
	{
		this.v1 = v1;
		this.v2 = v2;
		this.v3 = v3;
	}
	
	/**
	 * Returns true if the specified vertex index is referred to by this face
	 * 
	 * @param v
	 *            the vertex index to look for
	 * @return true if the index is contained in this face, false otherwise
	 */
	public boolean contains(Integer v)
	{
		return (v.compareTo(v1) == 0 || v.compareTo(v2) == 0 || v.compareTo(v3) == 0);
	}
	
	/**
	 * Calculates the area of this face.
	 * 
	 * @param points
	 *            - vertex list
	 * @return the area of the face
	 */
	public double getArea(ArrayList<Vertex> points)
	{
		Vector3d a = new Vector3d(points.get(v1).position);
		Vector3d b = new Vector3d(points.get(v2).position);
		
		a.sub(points.get(v3).position);
		b.sub(points.get(v3).position);
		
		a.cross(a, b);
		return 0.5 * a.length();
	}
	
	/**
	 * Calculates the area of this face with unit sphere pre-scaling.
	 * 
	 * @author Michael Reiner
	 * @param points
	 *            - vertex list
	 * @return the area of the face
	 */
	public double getArea(ArrayList<Vertex> points, Point3d center)
	{
		Point3d p1 = new Point3d(points.get(v1).position);
		Point3d p2 = new Point3d(points.get(v2).position);
		Point3d p3 = new Point3d(points.get(v3).position);
		
		// if the vertices are not on the unit-sphere, project the points onto it (for correct
		// integral)
		p1.scale(1. / p1.distance(center));
		p2.scale(1. / p2.distance(center));
		p3.scale(1. / p3.distance(center));
		
		Vector3d a = new Vector3d(p1);
		Vector3d b = new Vector3d(p2);
		
		a.sub(p3);
		b.sub(p3);
		
		a.cross(a, b);
		return 0.5 * a.length();
	}
	
	/**
	 * Returns true if the specified vertex indices are ordered counter-clockwisely in the
	 * current face. An exception is raised if the indices do not belong to this face
	 * 
	 * @param v1
	 *            the first vertex
	 * @param v2
	 *            the second vertex
	 * @return true if the edge v1-v2 is counter-clockwise for the current face, false otherwise
	 * @throws IllegalArgumentException
	 *             thrown if the specified vertex indices do not belong to this face
	 */
	public boolean isCounterClockwise(Integer v1, Integer v2) throws IllegalArgumentException
	{
		if (v1.compareTo(this.v1) == 0)
		{
			if (v2.compareTo(this.v2) == 0) return true;
			
			if (v2.compareTo(this.v3) == 0) return false;
			
			throw new IllegalArgumentException("Vertex index " + v2 + " does not belong to this face");
		}
		
		if (v1.compareTo(this.v2) == 0)
		{
			if (v2.compareTo(this.v3) == 0) return true;
			
			if (v2.compareTo(this.v1) == 0) return false;
			
			throw new IllegalArgumentException("Vertex index " + v2 + " does not belong to this face");
		}
		
		if (v1.compareTo(this.v3) == 0)
		{
			if (v2.compareTo(this.v1) == 0) return true;
			
			if (v2.compareTo(this.v2) == 0) return false;
			
			throw new IllegalArgumentException("Vertex index " + v2 + " does not belong to this face");
		}
		
		throw new IllegalArgumentException("Vertex index " + v1 + " does not belong to this face");
	}
	
	public Point3d[] getCoords(ArrayList<Vertex> points, Point3d[] out)
	{
		out[0] = points.get(v1).position;
		out[1] = points.get(v2).position;
		out[2] = points.get(v3).position;
		
		return out;
	}
	
	public String toString()
	{
		return "Face [" + v1 + "," + v2 + "," + v3 + "]";
	}
}


