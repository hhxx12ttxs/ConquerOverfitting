/* jCAE stand for Java Computer Aided Engineering. Features are : Small CAD
   modeler, Finite element mesher, Plugin architecture.
 
    Copyright (C) 2006, by EADS CRC
    Copyright (C) 2007,2008,2009,2010, by EADS France
 
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

package org.jcae.mesh.bora.xmldata;

import org.jcae.mesh.amibe.ds.MEdge1D;
import org.jcae.mesh.amibe.ds.MNode1D;
import org.jcae.mesh.amibe.ds.Mesh;
import org.jcae.mesh.amibe.ds.SubMesh1D;
import org.jcae.mesh.amibe.ds.Triangle;
import org.jcae.mesh.amibe.ds.AbstractHalfEdge;
import org.jcae.mesh.amibe.ds.Vertex;
import org.jcae.mesh.amibe.patch.Mesh2D;
import org.jcae.mesh.bora.ds.BModel;
import org.jcae.mesh.bora.ds.BSubMesh;
import org.jcae.mesh.bora.ds.BCADGraphCell;
import org.jcae.mesh.bora.ds.BDiscretization;
import org.jcae.mesh.bora.ds.Constraint;
import org.jcae.mesh.cad.CADVertex;
import org.jcae.mesh.cad.CADEdge;
import org.jcae.mesh.cad.CADFace;
import org.jcae.mesh.cad.CADSolid;
import org.jcae.mesh.cad.CADGeomCurve3D;
import org.jcae.mesh.cad.CADGeomSurface;
import org.jcae.mesh.cad.CADShapeFactory;
import org.jcae.mesh.cad.CADShapeEnum;
import org.jcae.mesh.xmldata.DoubleFileReader;
import org.jcae.mesh.xmldata.IntFileReader;
import org.jcae.mesh.xmldata.PrimitiveFileReaderFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.PrintStream;
import java.io.IOException;
import gnu.trove.TObjectIntHashMap;
import gnu.trove.TIntObjectHashMap;
import gnu.trove.TIntArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Storage
{
	private static final Logger LOGGER = Logger.getLogger(Storage.class.getName());

	private static void writeId(File dir, int id)
	{
		// Create the output directory if it does not exist
		if(!dir.exists())
			dir.mkdirs();

		File idFile = new File(dir, "id");
		if(idFile.exists())
			idFile.delete();
		try
		{
			PrintStream out=new PrintStream(new FileOutputStream(idFile, true));
			out.println(""+id);
			out.close();
		}
		catch(java.io.FileNotFoundException ex)
		{
		}
	}

	public static void writeEdge(BDiscretization d)
	{
		BCADGraphCell edge = d.getGraphCell();
		CADEdge E = (CADEdge) edge.getShape();
		if (E.isDegenerated())
			return;
		SubMesh1D submesh = (SubMesh1D) d.getMesh();
		if (null == submesh)
			return;

		try
		{
			File dir = new File(edge.getGraph().getModel().getOutputDir(d));
			writeId(dir, edge.getId());
			Collection<MNode1D> nodelist = submesh.getNodes();
			// Write node references and compute local indices
			TObjectIntHashMap<MNode1D> localIdx = write1dNodeReferences(dir, nodelist, edge);
			// Write node coordinates
			write1dCoordinates(dir, nodelist, CADShapeFactory.getFactory().newCurve3D(E));
			// Write edge connectivity
			write1dEdges(dir, submesh.getEdges(), localIdx);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			throw new RuntimeException();
		}
	}

	public static void writeFace(BDiscretization d)
	{
		BCADGraphCell face = d.getGraphCell();
		Mesh2D submesh = (Mesh2D) d.getMesh();
		if (null == submesh)
			return;

		try
		{
			File dir = new File(face.getGraph().getModel().getOutputDir(d));
			writeId(dir, face.getId());
			CADFace F = (CADFace) face.getShape();
			Collection<Triangle> trianglelist = submesh.getTriangles();
			Collection<Vertex> nodelist = submesh.getNodes();
			TObjectIntHashMap<Vertex> localIdx = write2dNodeReferences(dir, face.getId(), nodelist, submesh.outerVertex);
			write2dCoordinates(dir, nodelist, submesh.outerVertex, F.getGeomSurface());
			write2dTriangles(dir, trianglelist, localIdx);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			throw new RuntimeException();
		}
	}

	public static void writeSolid(BDiscretization d)
	{
		BCADGraphCell solid = d.getGraphCell();
		Mesh submesh = (Mesh) d.getMesh();
		if (null == submesh)
			return;

		try
		{
			File dir = new File(solid.getGraph().getModel().getOutputDir(d));
			writeId(dir, solid.getId());
			Collection<Vertex> nodelist = submesh.getNodes();
			TObjectIntHashMap<Vertex> localIdx = write2dNodeReferences(dir, solid.getId(), nodelist, submesh.outerVertex);
			write2dCoordinates(dir, nodelist, submesh.outerVertex, null);
			write2dTriangles(dir, submesh.getTriangles(), localIdx);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			throw new RuntimeException();
		}
	}

	/**
	 * Populates a Mesh instance by reading all faces and edges which have
	 * constraints.
	 * @param m       <code>Mesh</code> instance
	 * @param root    root shape
	 * @throws  RuntimeException if an error occurred
	 */
	public static void readAll(Mesh m, BCADGraphCell root) throws IOException
	{
		TIntObjectHashMap<Vertex> vertMap = new TIntObjectHashMap<Vertex>();
		for (BSubMesh s : root.getGraph().getModel().getSubMeshes())
		{
			int cid = 1;
			Map<String, Integer> groups = new HashMap<String, Integer>();
			for (Constraint c : s.getConstraints()) {
				Integer id = groups.get(c.getGroup());
				if(id == null)
				{
					groups.put(c.getGroup(), cid);
					m.setGroupName(cid++, c.getGroup());
				}
			}
			for (Constraint c : s.getConstraints()) {
				BCADGraphCell cell = c.getGraphCell();
				if(CADShapeEnum.EDGE.equals(cell.getType()))
					readEdge(m, cell, s, vertMap, c.getGroup(), groups.get(c.getGroup()));
				else if(CADShapeEnum.FACE.equals(cell.getType()))
					readFace(m, cell, s, vertMap);
			}
		}
	}

	/**
	 * Populates a Mesh instance by reading all faces.
	 * @param m       <code>Mesh</code> instance
	 * @param root    root shape
	 * @param s       consider discretizations only if they appear in this BSubMesh instance
	 * @throws  RuntimeException if an error occurred
	 */
	public static void readAllFaces(Mesh m, BCADGraphCell root, BSubMesh s)
	{
		TIntObjectHashMap<Vertex> vertMap = new TIntObjectHashMap<Vertex>();
		for (Iterator<BCADGraphCell> it = root.uniqueShapesExplorer(CADShapeEnum.FACE); it.hasNext(); )
			readFace(m, it.next(), s, vertMap);
	}

	public static void readEdge(Mesh mesh, BCADGraphCell edge, BSubMesh s,
		TIntObjectHashMap<Vertex> mapRefVertex, String groupName, int groupId)
		throws IOException
	{
		assert edge.getShape() instanceof CADEdge;
		BModel model = edge.getGraph().getModel();
		boolean reversed = false;
		if (edge.getOrientation() != 0)
		{
			reversed = true;
			if (edge.getReversed() != null)
				edge = edge.getReversed();
		}
		BDiscretization d = edge.getDiscretizationSubMesh(s);
		if (null == d) 
			return;
		int id = edge.getId();
		File dir = new File(model.getOutputDir(d));
		// Read vertex references
		int [] refs = readNodeReferences(d);
		// Create a Vertex array, and insert new references
		// into mapRefVertex.
		Vertex [] nodelist = read2dCoordinates(dir, mesh, refs, mapRefVertex);
		int[] indices = readConnectivity(d);
		for(int i = 0; i<indices.length; i+=2)
			mesh.addBeam(nodelist[indices[i] - 1], nodelist[indices[i+1] - 1], groupId);
	}
	/**
	 * Append a discretized face into a Mesh instance.
	 * @param mesh    original mesh
	 * @param face    cell graph containing a CAD face
	 * @param s       consider discretizations only if they appear in this BSubMesh instance
	 * @param mapRefVertex    map between references and Vertex instances
	 * @throws  RuntimeException if an error occurred
	 */
	public static void readFace(Mesh mesh, BCADGraphCell face, BSubMesh s, TIntObjectHashMap<Vertex> mapRefVertex)
	{
		assert face.getShape() instanceof CADFace;
		BModel model = face.getGraph().getModel();
		boolean reversed = false;
		if (face.getOrientation() != 0)
		{
			reversed = true;
			if (face.getReversed() != null)
				face = face.getReversed();
		}
		BDiscretization d = face.getDiscretizationSubMesh(s);
		if (null == d) 
			return;
		int id = face.getId();
		try
		{
			File dir = new File(model.getOutputDir(d));
			// Read vertex references
			int [] refs = readNodeReferences(d);
			// Create a Vertex array, and insert new references
			// into mapRefVertex.
			Vertex [] nodelist = read2dCoordinates(dir, mesh, refs, mapRefVertex);
			// Read triangles and appends them to the mesh.
			read2dTriangles(dir, id, 3, mesh, reversed, nodelist);
		}
		catch(java.io.FileNotFoundException ex)
		{
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}
		if (LOGGER.isLoggable(Level.FINE))
			LOGGER.log(Level.FINE, "end reading cell "+id);
	}

	/**
	 * Populates a Mesh instance by reading all volumes.
	 * @param m       <code>Mesh</code> instance
	 * @param root    root shape
	 * @throws  RuntimeException if an error occurred
	 */
	@SuppressWarnings("unused")
	private static void readAllVolumes(Mesh m, BCADGraphCell root, BSubMesh s)
	{
		TIntObjectHashMap<Vertex> vertMap = new TIntObjectHashMap<Vertex>();
		for (Iterator<BCADGraphCell> it = root.uniqueShapesExplorer(CADShapeEnum.SOLID); it.hasNext(); )
			readVolume(m, it.next(), s, vertMap);
	}

	/**
	 * Append a discretized solid into a Mesh instance.
	 * @param mesh    original mesh
	 * @param volume    cell graph containing a CAD solid
	 * @param mapRefVertex    map between references and Vertex instances
	 * @throws  RuntimeException if an error occurred
	 */
	private static void readVolume(Mesh mesh, BCADGraphCell volume, BSubMesh s, TIntObjectHashMap<Vertex> mapRefVertex)
	{
		assert volume.getShape() instanceof CADSolid;
		BModel model = volume.getGraph().getModel();
		boolean reversed = false;
		if (volume.getOrientation() != 0)
		{
			reversed = true;
			if (volume.getReversed() != null)
				volume = volume.getReversed();
		}
		BDiscretization d = volume.getDiscretizationSubMesh(s);
		if (null == d)
			return;
		int id = volume.getId();
		try
		{
			File dir = new File(model.getOutputDir(d));
			// Read vertex references
			int [] refs = readNodeReferences(d);
			// Create a Vertex array, and insert new references
			// into mapRefVertex.
			Vertex [] nodelist = read2dCoordinates(dir, mesh, refs, mapRefVertex);
			if (mesh.hasNodes())
			{
				for (int i = 0, n = nodelist.length; i < n; i++)
					mesh.add(nodelist[i]);
			}
			// Read triangles and appends them to the mesh.
			read2dTriangles(dir, id, 4, mesh, reversed, nodelist);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}
		if (LOGGER.isLoggable(Level.FINE))
			LOGGER.log(Level.FINE, "end reading cell "+id);
	}

	private static TObjectIntHashMap<MNode1D> write1dNodeReferences(File dir, Collection<MNode1D> nodelist, BCADGraphCell edge)
		throws IOException
	{
		File refFile = new File(dir, "r");
		if(refFile.exists())
			refFile.delete();
		
		// Save references
		if (LOGGER.isLoggable(Level.FINE))
			LOGGER.log(Level.FINE, "begin writing "+refFile);
		DataOutputStream refsout = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(refFile, true)));
		TObjectIntHashMap<MNode1D> localIdx = new TObjectIntHashMap<MNode1D>(nodelist.size());

		int i = 0;
		for (Iterator<MNode1D> itn = nodelist.iterator(); itn.hasNext(); )
		{
			MNode1D n = itn.next();
			// Set first index to 1; a null index in
			// localIdx is thus an error
			localIdx.put(n, i+1);
			CADVertex v = n.getCADVertex();
			if (null != v)
			{
				// TODO: replace this id by Vertex.getRef()
				BCADGraphCell vv = edge.getGraph().getByShape(v);
				refsout.writeInt(i);
				refsout.writeInt(vv.getId());
			}
			i++;
		}
		refsout.close();
		return localIdx;
	}

	private static TObjectIntHashMap<Vertex> write2dNodeReferences(File dir, int id, Collection<Vertex> nodelist, Vertex outer)
		throws IOException
	{
		File refFile = new File(dir, "r");
		if(refFile.exists())
			refFile.delete();

		// Save references
		if (LOGGER.isLoggable(Level.FINE))
			LOGGER.log(Level.FINE, "begin writing "+refFile+" face "+id);
		DataOutputStream refsout = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(refFile, true)));

		TObjectIntHashMap<Vertex> localIdx = new TObjectIntHashMap<Vertex>(nodelist.size());
		int i = 0;
		for (Iterator<Vertex> itn = nodelist.iterator(); itn.hasNext(); )
		{
			Vertex n = itn.next();
			if (n == outer)
				continue;
			// Set first index to 1; a null index in
			// localIdx is thus an error
			localIdx.put(n, i+1);
			int ref1d = n.getRef();
			if (ref1d != 0)
			{
				refsout.writeInt(i);
				refsout.writeInt(Math.abs(ref1d));
			}
			i++;
		}
		refsout.close();
		return localIdx;
	}

	private static void write1dCoordinates(File dir, Collection<MNode1D> nodelist, CADGeomCurve3D curve)
		throws IOException
	{
		File nodesFile = new File(dir, "n");
		if(nodesFile.exists())
			nodesFile.delete();
		File parasFile = new File(dir, "p");
		if(parasFile.exists())
			parasFile.delete();
		
		if (LOGGER.isLoggable(Level.FINE))
			LOGGER.log(Level.FINE, "begin writing "+nodesFile+" and "+parasFile);
		DataOutputStream nodesout = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(nodesFile, true)));
		DataOutputStream parasout = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(parasFile, true)));
		for (Iterator<MNode1D> itn = nodelist.iterator(); itn.hasNext(); )
		{
			MNode1D n = itn.next();
			double p = n.getParameter();
			parasout.writeDouble(p);
			double [] xyz = curve.value(p);
			for (int k = 0; k < 3; k++)
				nodesout.writeDouble(xyz[k]);
		}
		nodesout.close();
		parasout.close();
	}

	private static void write2dCoordinates(File dir, Collection<Vertex> nodelist, Vertex outer, CADGeomSurface surface)
		throws IOException
	{
		File nodesFile = new File(dir, "n");
		if(nodesFile.exists())
			nodesFile.delete();
		File parasFile = new File(dir, "p");
		if(parasFile.exists())
			parasFile.delete();

		// Save nodes
		if (LOGGER.isLoggable(Level.FINE))
			LOGGER.log(Level.FINE, "begin writing "+nodesFile+" and "+parasFile);
		DataOutputStream nodesout = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(nodesFile, true)));
		DataOutputStream parasout = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(parasFile, true)));
		double [] xyz;
		for (Iterator<Vertex> itn = nodelist.iterator(); itn.hasNext(); )
		{
			Vertex n = itn.next();
			if (n == outer)
				continue;
			if (surface == null)
				xyz = n.getUV();
			else
			{
				double [] p = n.getUV();
				for (int d = 0; d < p.length; d++)
					parasout.writeDouble(p[d]);
				xyz = surface.value(p[0], p[1]);
			}
			for (int k = 0; k < 3; k++)
				nodesout.writeDouble(xyz[k]);
		}
		nodesout.close();
		parasout.close();
	}

	private static void write1dEdges(File dir, Collection<MEdge1D> edgelist, TObjectIntHashMap<MNode1D> localIdx)
		throws IOException
	{
		File beamsFile=new File(dir, "b");
		if(beamsFile.exists())
			beamsFile.delete();
		
		if (LOGGER.isLoggable(Level.FINE))
			LOGGER.log(Level.FINE, "begin writing "+beamsFile);
		DataOutputStream beamsout = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(beamsFile, true)));
		for (Iterator<MEdge1D> ite = edgelist.iterator(); ite.hasNext(); )
		{
			MEdge1D e = ite.next();
			MNode1D pt1 = e.getNodes1();
			MNode1D pt2 = e.getNodes2();
			beamsout.writeInt(localIdx.get(pt1));
			beamsout.writeInt(localIdx.get(pt2));
		}
		beamsout.close();
	}

	private static void write2dTriangles(File dir, Collection<Triangle> trianglelist, TObjectIntHashMap<Vertex> localIdx)
		throws IOException
	{
		File facesFile=new File(dir, "f");
		if(facesFile.exists())
			facesFile.delete();

		// Save faces
		if (LOGGER.isLoggable(Level.FINE))
			LOGGER.log(Level.FINE, "begin writing "+facesFile);
		DataOutputStream facesout = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(facesFile, true)));
		for (Triangle f: trianglelist)
		{
			if (f.hasAttributes(AbstractHalfEdge.OUTER))
				continue;
			for (int j = 0, n = f.vertex.length; j < n; j++)
				facesout.writeInt(localIdx.get(f.vertex[j]));
		}
		facesout.close();
	}

	public static int [] readNodeReferences(BDiscretization d)
		throws IOException
	{
		File refFile = new File(d.getGraphCell().getGraph().getModel().getOutputDir(d), "r");
		IntFileReader ifrR = new PrimitiveFileReaderFactory().getIntReader(refFile);
		int numberOfReferences = (int) refFile.length() / 4;
		int [] refs = new int[numberOfReferences];
		ifrR.get(refs);
		ifrR.close();
		return refs;
	}

	public static int getNumberOfNodes(BDiscretization d)
	{
		File nodesFile = new File(d.getGraphCell().getGraph().getModel().getOutputDir(d), "n");
		return (int) nodesFile.length() / 24;
	}

	public static double[] readNodeCoordinates(BDiscretization d)
		throws IOException
	{
		File nodesFile = new File(d.getGraphCell().getGraph().getModel().getOutputDir(d), "n");
		DoubleFileReader dfrN = new PrimitiveFileReaderFactory().getDoubleReader(nodesFile);
		int numberOfNodes = (int) nodesFile.length() / 24;
		double [] coord = new double[3*numberOfNodes];
		dfrN.get(coord);
		dfrN.close();
		return coord;
	}

	private static Vertex [] read2dCoordinates(File dir, Mesh mesh, int [] refs, TIntObjectHashMap<Vertex> mapRefVertex)
		throws IOException
	{
		File nodesFile = new File(dir, "n");
		DoubleFileReader dfrN = new PrimitiveFileReaderFactory().getDoubleReader(nodesFile);

		int numberOfNodes = (int) nodesFile.length() / 24;
		int numberOfReferences = refs.length / 2;
		Vertex [] nodelist = new Vertex[numberOfNodes];
		double [] coord = new double[3];
		if (LOGGER.isLoggable(Level.FINE))
			LOGGER.log(Level.FINE, "Reading "+numberOfNodes+" nodes");
		mesh.ensureCapacity(2*numberOfNodes);
		for (int i = 0; i < numberOfNodes; i++)
		{
			dfrN.get(coord);
			nodelist[i] = mesh.createVertex(coord);
		}
		if (mesh.hasNodes())
		{
			for (int i=0; i < numberOfNodes; i++)
				mesh.add(nodelist[i]);
		}
		for (int i = 0; i < numberOfReferences; i++)
		{
			int ind = refs[2*i];
			int label = refs[2*i+1];
			Vertex v = mapRefVertex.get(label);
			if (v == null)
				mapRefVertex.put(label, nodelist[ind]);
			else
				nodelist[ind] = v;
			nodelist[ind].setRef(label);
		}
		dfrN.close();
		if (LOGGER.isLoggable(Level.FINE))
			LOGGER.log(Level.FINE, "end reading "+dir+File.separator+"n");
		return nodelist;
	}

	public static int[] readConnectivity(BDiscretization d)
		throws IOException
	{
		File trianglesFile = null;
		int nodesByElement;
		if (d.getGraphCell().getType().equals(CADShapeEnum.EDGE))
		{
			trianglesFile = new File(d.getGraphCell().getGraph().getModel().getOutputDir(d), "b");
			nodesByElement = 2;
		}
		else if (d.getGraphCell().getType().equals(CADShapeEnum.FACE))
		{
			trianglesFile = new File(d.getGraphCell().getGraph().getModel().getOutputDir(d), "f");
			nodesByElement = 3;
		}
		else if (d.getGraphCell().getType().equals(CADShapeEnum.SOLID))
		{
			trianglesFile = new File(d.getGraphCell().getGraph().getModel().getOutputDir(d), "f");
			nodesByElement = 4;
		}
		else
		{
			throw new IllegalArgumentException();
		}
		IntFileReader ifr = new PrimitiveFileReaderFactory().getIntReader(trianglesFile);

		int[] connectivity = new int[(int) trianglesFile.length() / 4];
		ifr.get(connectivity);
		ifr.close();
		return connectivity;
	}

	private static void read2dTriangles(File dir, int id, int nr, Mesh mesh, boolean reversed, Vertex [] nodelist)
		throws IOException
	{
		File trianglesFile = new File(dir, "f");
		IntFileReader ifr = new PrimitiveFileReaderFactory().getIntReader(trianglesFile);

		int numberOfTriangles = (int) trianglesFile.length() / (4*nr);
		if (LOGGER.isLoggable(Level.FINE))
			LOGGER.log(Level.FINE, "Reading "+numberOfTriangles+" elements");
		mesh.ensureCapacity(numberOfTriangles);
		Triangle face;
		Vertex [] pts = new Vertex[nr];
		for (int i = 0; i < numberOfTriangles; i++)
		{
			for (int j = 0; j < nr; j++)
				pts[j] = nodelist[ifr.get()-1];
			// Remove triangles incident to degenerated edges.
			// These triangles are only useful in parameter space.
			boolean degenerated = false;
			for (int j = 0; j < nr; j++)
			{
				if (pts[j].getRef() == 0)
					continue;
				for (int k = j + 1; k < nr; k++)
				{
					if (pts[j] == pts[k])
					{
						j = nr;
						k = nr;
						degenerated = true;
					}
				}
			}
			if (degenerated)
				continue;
			if (reversed)
			{
				Vertex temp = pts[1];
				pts[1] = pts[2];
				pts[2] = temp;
			}
			face = mesh.createTriangle(pts);
			mesh.add(face);
			face.setGroupId(id);
		}
		ifr.close();
	}

	private static void read2dEdges(File dir, int id, int nr, Mesh mesh, boolean reversed, Vertex [] nodelist)
		throws IOException
	{
		File edgesFile = new File(dir, "f");
		IntFileReader ifr = new PrimitiveFileReaderFactory().getIntReader(edgesFile);

		int numberOfBeams = (int) edgesFile.length() / (4*2);
		if (LOGGER.isLoggable(Level.FINE))
			LOGGER.log(Level.FINE, "Reading "+numberOfBeams+" elements");
		Triangle face;
		Vertex [] pts = new Vertex[nr];
		for (int i = 0; i < numberOfBeams; i++)
		{
			for (int j = 0; j < 2; j++)
				pts[j] = nodelist[ifr.get()-1];
			// Remove triangles incident to degenerated edges.
			// These triangles are only useful in parameter space.
			boolean degenerated = false;
			for (int j = 0; j < nr; j++)
			{
				if (pts[j].getRef() == 0)
					continue;
				for (int k = j + 1; k < nr; k++)
				{
					if (pts[j] == pts[k])
					{
						j = nr;
						k = nr;
						degenerated = true;
					}
				}
			}
			if (degenerated)
				continue;
			if (reversed)
			{
				Vertex temp = pts[1];
				pts[1] = pts[2];
				pts[2] = temp;
			}
			face = mesh.createTriangle(pts);
			mesh.add(face);
			face.setGroupId(id);
		}
		ifr.close();
	}
}


