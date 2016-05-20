/* jCAE stand for Java Computer Aided Engineering. Features are : Small CAD
   modeler, Finite element mesher, Plugin architecture.
 
    Copyright (C) 2003,2004,2005, by EADS CRC
    Copyright (C) 2007,2008, by EADS France
 
    This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either
    version 2.1 of the License, or (at your option) any later version.
 
    This library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Lesser General Public License for more details.
 
    You should have received a copy of the GNU Lesser General Public
    License along with this library; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.jcae.mesh.amibe.algos1d;

import org.jcae.mesh.amibe.ds.MEdge1D;
import org.jcae.mesh.amibe.ds.MMesh1D;
import org.jcae.mesh.amibe.ds.MNode1D;
import org.jcae.mesh.amibe.ds.SubMesh1D;
import org.jcae.mesh.cad.CADGeomCurve3D;
import org.jcae.mesh.cad.CADVertex;
import org.jcae.mesh.cad.CADEdge;
import org.jcae.mesh.cad.CADShapeFactory;

import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Computes a new discretization so that all edges have a uniform length.
 * On each edge, compute the number of subdivisions so that all segments
 * have the same length, which must be less than the given criterion.
 * The previous discretization nodes and edges are deleted, and replaced
 * by newer ones.
 */
public class UniformLengthDeflection
{
	private static final Logger LOGGER = Logger.getLogger(UniformLengthDeflection.class.getName());
	private final MMesh1D mesh1d;
	private double maxlen = -1.0;
	private double deflection = 1.0;
	private boolean relativeDeflection = false;
	
	/**
	 * Creates a <code>UniformLengthDeflection</code> instance.
	 *
	 * @param m  the <code>MMesh1D</code> instance to refine.
	 */
	public UniformLengthDeflection(MMesh1D m, final Map<String, String> options)
	{
		mesh1d = m;

		for (final Map.Entry<String, String> opt: options.entrySet())
		{
			final String key = opt.getKey();
			final String val = opt.getValue();
			if (key.equals("deflection"))
				deflection = Double.valueOf(val).doubleValue();
			else if (key.equals("relativeDeflection"))
				relativeDeflection = Boolean.valueOf(val).booleanValue();
			else if (key.equals("size"))
				maxlen = Double.valueOf(val).doubleValue();
			else
				throw new RuntimeException("Unknown option: "+key);
		}
	}


	/**
	 * Explores each edge of the mesh and calls the discretisation method.
	 */
	public final void compute()
	{
		int nbTEdges = 0, nbNodes = 0, nbEdges = 0;
		/*  First compute current nbNodes and nbEdges  */
		for (CADEdge E : mesh1d.getTEdges())
		{
			SubMesh1D submesh1d = mesh1d.getSubMesh1DFromMap(E);
			nbNodes += submesh1d.getNodes().size();
			nbEdges += submesh1d.getEdges().size();
		}
		/* Explore the shape for each edge */
		for (CADEdge E : mesh1d.getTEdges())
		{
			SubMesh1D submesh1d = mesh1d.getSubMesh1DFromMap(E);
			nbNodes -= submesh1d.getNodes().size();
			nbEdges -= submesh1d.getEdges().size();
			if (computeEdge(submesh1d))
				nbTEdges++;
			nbNodes += submesh1d.getNodes().size();
			nbEdges += submesh1d.getEdges().size();
		}
		LOGGER.fine("TopoEdges discretisees "+nbTEdges);
		LOGGER.fine("Edges   "+nbEdges);
		LOGGER.fine("Nodes   "+nbNodes);
		assert(mesh1d.isValid());
	}

	/*
	 * Discretizes a topological edge so that all edges have a uniform length.
	 * For a given topological edge, its previous discretization is first
	 * removed.  Then the number of segments is computed such that segment
	 * length is inferior to the desired length.  The geometrical edge is then
	 * divided into segments of uniform lengths.
	 *
	 * @param maxlen  the maximal length admitted,
	 * @param submesh1d  the 1D mesh being updated.
	 * @return <code>true</code> if this edge was successfully discretized,
	 * <code>false</code> otherwise.
	 */
	public final boolean computeEdge(SubMesh1D submesh1d)
	{
		int nbPoints;
		boolean isCircular = false;
		boolean isDegenerated = false;
		double[] paramOnEdge;
		double range[];
		CADEdge E = submesh1d.getGeometry();
		
		//  See also org.jcae.mesh.amibe.ds.Mesh.tooSmall()
		//if (BRep_Tool.degenerated(E))
		//	return false;
		
		ArrayList<MEdge1D> edgelist = submesh1d.getEdges();
		ArrayList<MNode1D> nodelist = submesh1d.getNodes();
		if (edgelist.size() != 1 || nodelist.size() != 2)
			return false;
		edgelist.clear();
		nodelist.clear();
		CADVertex[] V = E.vertices();
		if (V[0].isSame(V[1]))
			isCircular=true;
		
		CADGeomCurve3D curve = CADShapeFactory.getFactory().newCurve3D(E);
		if (curve == null)
		{
			if (!E.isDegenerated())
				throw new java.lang.RuntimeException("Curve not defined on edge, but this  edge is not degenerated.  Something must be wrong.");
			
			isDegenerated = true;
			/*
			 * Degenerated edges should not be discretized, but then
			 * their vertices have very low connectivity.  So let
			 * discretize them until a solution is found.
			 */
			range = E.range();
			nbPoints=2;
			paramOnEdge = new double[nbPoints];
			for (int i = 0; i < nbPoints; i++)
				paramOnEdge[i] = range[0] + (range[1] - range[0])*i/(nbPoints-1);
		}
		else
		{
			range = curve.getRange();
			curve.discretize(maxlen, deflection, relativeDeflection);
			nbPoints = curve.nbPoints();
			int saveNbPoints =  nbPoints;
			if (nbPoints <= 2 && !isCircular)
			{
				//  Compute the deflection
				double mid1[], pnt1[], pnt2[];

				mid1 = curve.value((range[0] + range[1])/2.0);
				pnt1 = V[0].pnt();
				pnt2 = V[1].pnt();
				double d1 =
					(mid1[0] - 0.5*(pnt1[0]+pnt2[0])) * (mid1[0] - 0.5*(pnt1[0]+pnt2[0])) +
					(mid1[1] - 0.5*(pnt1[1]+pnt2[1])) * (mid1[1] - 0.5*(pnt1[1]+pnt2[1])) +
					(mid1[2] - 0.5*(pnt1[2]+pnt2[2])) * (mid1[2] - 0.5*(pnt1[2]+pnt2[2]));
				double d2 =
					(pnt1[0] - pnt2[0]) * (pnt1[0] - pnt2[0]) +
					(pnt1[1] - pnt2[1]) * (pnt1[1] - pnt2[1]) +
					(pnt1[2] - pnt2[2]) * (pnt1[2] - pnt2[2]);
				if (d1 > 0.01 * d2) {
					nbPoints=3;
				} else {
					nbPoints=2;
				}
			}
			else if (nbPoints <= 3 && isCircular)
				nbPoints=4;
			if (saveNbPoints != nbPoints)
				curve.discretize(nbPoints);
			paramOnEdge = new double[nbPoints];
			for (int i = 0; i < nbPoints; i++)
				paramOnEdge[i] = curve.parameter(i+1);
		}

		MNode1D n1, n2;
		double param;

		//  First vertex
		CADVertex GPt = mesh1d.getGeometricalVertex(V[0]);
		MNode1D firstNode = new MNode1D(paramOnEdge[0], GPt);
		n1 = firstNode;
		n1.isDegenerated(isDegenerated);
		nodelist.add(n1);
		if (!isDegenerated)
			GPt = null;

		//  Other points
		for (int i = 0; i < nbPoints - 1; i++)
		{
			param = paramOnEdge[i+1];
			if (i == nbPoints - 2)
				GPt = mesh1d.getGeometricalVertex(V[1]);
			n2 = new MNode1D(param, GPt);
			n2.isDegenerated(isDegenerated);
			nodelist.add(n2);
			MEdge1D e=new MEdge1D(n1, n2);
			edgelist.add(e);
			n1 = n2;
		}
		return true;
	}
}

