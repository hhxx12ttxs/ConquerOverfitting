/* jCAE stand for Java Computer Aided Engineering. Features are : Small CAD
   modeler, Finite element mesher, Plugin architecture.
 
    Copyright (C) 2003,2004,2005,2006, by EADS CRC
    Copyright (C) 2007,2008,2009, by EADS France
 
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

package org.jcae.mesh.bora.algo;

import org.jcae.mesh.bora.ds.BCADGraphCell;
import org.jcae.mesh.bora.ds.BDiscretization;
import org.jcae.mesh.amibe.patch.Mesh2D;
import org.jcae.mesh.amibe.patch.Vertex2D;
import org.jcae.mesh.amibe.traits.TriangleTraitsBuilder;
import org.jcae.mesh.amibe.traits.MeshTraitsBuilder;
import org.jcae.mesh.cad.CADShape;
import org.jcae.mesh.cad.CADVertex;
import org.jcae.mesh.cad.CADEdge;
import org.jcae.mesh.cad.CADWire;
import org.jcae.mesh.cad.CADFace;
import org.jcae.mesh.cad.CADShapeEnum;
import org.jcae.mesh.cad.CADShapeFactory;
import org.jcae.mesh.cad.CADWireExplorer;
import org.jcae.mesh.cad.CADGeomCurve2D;
import org.jcae.mesh.cad.CADGeomCurve3D;
import org.jcae.mesh.amibe.algos2d.CheckDelaunay;
import org.jcae.mesh.amibe.algos2d.ConstraintNormal3D;
import org.jcae.mesh.amibe.algos2d.EnforceAbsDeflection;
import org.jcae.mesh.amibe.algos2d.Initial;
import org.jcae.mesh.amibe.algos2d.Insertion;
import org.jcae.mesh.amibe.ds.MNode1D;
import org.jcae.mesh.amibe.ds.SubMesh1D;
import org.jcae.mesh.amibe.ds.MeshParameters;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @see org.jcae.mesh.amibe.algos2d.BasicMesh
 */
public class Basic2d implements AlgoInterface
{
	private static final Logger LOGGER = Logger.getLogger(Basic2d.class.getName());

	private final double maxlen;
	private final double deflection;
	private final boolean relDefl;
	private final boolean isotropic;

	public Basic2d(double len, double defl, boolean rel, boolean iso)
	{
		maxlen = len;
		deflection = defl;
		relDefl = rel;
		isotropic = iso;
	}

	public boolean isAvailable()
	{
		return true;
	}

	public int getOrientation(int o)
	{
		return o;
	}

	public boolean compute(BDiscretization d)
	{
		BCADGraphCell cell = d.getGraphCell();
		CADFace F = (CADFace) cell.getShape();
		if (LOGGER.isLoggable(Level.FINE))
			LOGGER.log(Level.FINE, ""+this+"  shape: "+F);

		TriangleTraitsBuilder ttb = new TriangleTraitsBuilder();
		ttb.addVirtualHalfEdge();
		MeshTraitsBuilder mtb = new MeshTraitsBuilder();
		mtb.addTriangleList();
		mtb.addKdTree(2);
		mtb.add(ttb);

		HashMap<String, String> options = new HashMap<String, String>();
		options.put("size", Double.toString(maxlen));
		options.put("deflection", Double.toString(deflection));
		options.put("relativeDeflection", Boolean.toString(relDefl));
		options.put("isotropic", Boolean.toString(isotropic));
		MeshParameters mp = new MeshParameters(options);
		Mesh2D m = new Mesh2D(mtb, mp, F);
		d.setMesh(m);
		boolean accumulateEpsilon = mp.hasCumulativeEpsilon();
		double epsilon = mp.getEpsilon();

		// Insert interior vertices, if any
		ArrayList<MNode1D> innerV = new ArrayList<MNode1D>();
		for (Iterator<BCADGraphCell> it = cell.shapesIterator(); it.hasNext(); )
		{
			BCADGraphCell sub = it.next();
			CADShape subV = sub.getShape();
			if (subV instanceof CADVertex)
			{
				MNode1D n = new MNode1D(0.0, (CADVertex) subV);
				innerV.add(n);
			}
		}

		// Boundary nodes. See org.jcae.mesh.mesher.ds.MMesh1D.boundaryNodes()
		ArrayList<Vertex2D> bndV = new ArrayList<Vertex2D>();
		CADWireExplorer wexp = CADShapeFactory.getFactory().newWireExplorer();
		for (Iterator<BCADGraphCell> it = cell.shapesExplorer(CADShapeEnum.WIRE); it.hasNext(); )
		{
			BCADGraphCell wire = it.next();
			MNode1D p1 = null;
			Vertex2D p20 = null, p2 = null, lastPoint = null;
			double accumulatedLength = 0.0;
			ArrayList<Vertex2D> nodesWire = new ArrayList<Vertex2D>();
			for (wexp.init((CADWire) wire.getShape(), F); wexp.more(); wexp.next())
			{
				CADEdge te = wexp.current();
				CADGeomCurve2D c2d = CADShapeFactory.getFactory().newCurve2D(te, F);
				CADGeomCurve3D c3d = CADShapeFactory.getFactory().newCurve3D(te);
				BDiscretization dc = cell.getGraph().getByShape(te).getDiscretizationSubMesh(d.getFirstSubMesh());

				SubMesh1D submesh1d = (SubMesh1D) dc.getMesh();
				ArrayList<MNode1D> nodelist = submesh1d.getNodes();
				ArrayList<MNode1D> saveList = new ArrayList<MNode1D>(nodelist);

				if (!te.isOrientationForward())
				{
					//  Sort in reverse order
					int size = saveList.size();
					for (int i = 0; i < size/2; i++)
					{
						MNode1D o = saveList.get(i);
						saveList.set(i, saveList.get(size - i - 1));
						saveList.set(size - i - 1, o);
					}
				}
				Iterator<MNode1D> itn = saveList.iterator();
				//  Except for the very first edge, the first
				//  vertex is constrained to be the last one
				//  of the previous edge.
				p1 = itn.next();
				if (null == p2)
				{
					p2 = Vertex2D.valueOf(p1, c2d, F);
					nodesWire.add(p2);
					p20 = p2;
					lastPoint = p2;
				}
				ArrayList<Vertex2D> newNodes = new ArrayList<Vertex2D>(saveList.size());
				while (itn.hasNext())
				{
					p1 = itn.next();
					p2 = Vertex2D.valueOf(p1, c2d, F);
					newNodes.add(p2);
				}
				// An edge is skipped if all the following conditions
				// are met:
				//   1.  It is not degenerated
				//   2.  It has not been discretized in 1D
				//   3.  Edge length is smaller than epsilon
				//   4.  Accumulated points form a curve with a deflection
				//       which meets its criterion
				boolean canSkip = false;
				if (nodelist.size() == 2 && !te.isDegenerated())
				{
					//   3.  Edge length is smaller than epsilon
					double edgelen = c3d.length();
					if (accumulateEpsilon)
						canSkip = edgelen + accumulatedLength < epsilon;
					else
						canSkip = edgelen < epsilon;
					if (canSkip)
						accumulatedLength += edgelen;
					// 4.  Check whether deflection is valid.
					if (canSkip && mp.hasDeflection())
					{
						double [] uv = lastPoint.getUV();
						double [] start = m.getGeomSurface().value(uv[0], uv[1]);
						uv = p2.getUV();
						double [] end = m.getGeomSurface().value(uv[0], uv[1]);
						double dist = Math.sqrt(
						  (start[0] - end[0]) * (start[0] - end[0]) +
						  (start[1] - end[1]) * (start[1] - end[1]) +
						  (start[2] - end[2]) * (start[2] - end[2]));
						double dmax = m.getMeshParameters().getDeflection();
						if (m.getMeshParameters().hasRelativeDeflection())
							dmax *= accumulatedLength;
						if (accumulatedLength - dist > dmax)
							canSkip = false;
					}
				}

				if (!canSkip)
				{
					nodesWire.addAll(newNodes);
					accumulatedLength = 0.0;
					lastPoint = p2;
				}
			}
			//  If a wire has less than 3 points, it is discarded
			if (nodesWire.size() > 3)
			{
				//  Overwrite the last value to close the wire
				nodesWire.set(nodesWire.size()-1, p20);
				bndV.addAll(nodesWire);
			}
		}
		new Initial(m, mtb, bndV.toArray(new Vertex2D[bndV.size()]), innerV).compute();

		m.pushCompGeom(3);
		new Insertion(m, 16.0 / Math.sqrt(2.0), 16.0 * Math.sqrt(2.0)).compute();
		new ConstraintNormal3D(m).compute();
		new Insertion(m, 4.0 / Math.sqrt(2.0), 4.0 * Math.sqrt(2.0)).compute();
		new ConstraintNormal3D(m).compute();
		new Insertion(m, 1.0 / Math.sqrt(2.0), Math.sqrt(2.0)).compute();
		m.popCompGeom(3);
		
		new ConstraintNormal3D(m).compute();
		new CheckDelaunay(m).compute();
		if (deflection > 0.0 && !relDefl)
			new EnforceAbsDeflection(m).compute();
		return true;
	}
	
	@Override
	public final String toString()
	{
		StringBuilder ret = new StringBuilder("Algo: "+getClass().getName());
		ret.append("\nTarget size: ").append(maxlen);
		ret.append("\nDeflection: ").append(deflection);
		if (relDefl)
			ret.append(" (relative)");
		else
			ret.append(" (absolute)");
		return ret.toString();
	}
}

