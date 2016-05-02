/* jCAE stand for Java Computer Aided Engineering. Features are : Small CAD
   modeler, Finite element mesher, Plugin architecture.
 
    Copyright (C) 2003,2004,2005, by EADS CRC
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

package org.jcae.mesh.xmldata;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.jcae.mesh.cad.CADShapeFactory;
import org.jcae.mesh.cad.CADShapeEnum;
import org.jcae.mesh.cad.CADExplorer;
import org.jcae.mesh.cad.CADGeomSurface;
import org.jcae.mesh.cad.CADShape;
import org.jcae.mesh.cad.CADFace;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import gnu.trove.TIntIntHashMap;
import gnu.trove.TIntObjectHashMap;
import gnu.trove.TIntArrayList;
import java.util.logging.Logger;


public class MeshToSoupConvert implements FilterInterface, JCAEXMLData
{
	private static final Logger logger=Logger.getLogger(MeshToSoupConvert.class.getName());
	private int nrTriangles = 0;
	private int nrIntNodes = 0;
	private int nrNodes = 0;
	private int nrRefs = 0;
	private final String xmlDir;
	private final String soupFile;
	private File rawFile;
	private TIntIntHashMap xrefs;
	private final TIntObjectHashMap<CADFace> mapFaces = new TIntObjectHashMap<CADFace>();
	private double [] coordRefs;
	// Must be a multiple of 8*2, 4*3 and 8*10
	private static final int bufferSize = 15 << 12;
	private final ByteBuffer bb = ByteBuffer.allocate(bufferSize);
	private final IntBuffer bbI = bb.asIntBuffer();
	private final DoubleBuffer bbD = bb.asDoubleBuffer();
	
	/** 
	 * @param args mesh directory, brep path
	 */
	public static void main(String[] args)
	{
		meshToSoup(args[0], CADShapeFactory.getFactory().newShape(args[1]));
	}
	
	/**
	 * Compute raw 3D mesh
	 * @param xmlDir mesh directory
	 * @param shape
	 */
	public static void meshToSoup(String xmlDir, CADShape shape)
	{
		CADExplorer expF = CADShapeFactory.getFactory().newExplorer();		
		int numFace=Integer.getInteger("org.jcae.mesh.Mesher.meshFace", 0).intValue();
		int minFace=Integer.getInteger("org.jcae.mesh.Mesher.minFace", 0).intValue();
		int maxFace=Integer.getInteger("org.jcae.mesh.Mesher.maxFace", 0).intValue();

		int iFace = 0;
		MeshToSoupConvert m2dTo3D = new MeshToSoupConvert("soup", xmlDir, shape);
		logger.info("Read informations on boundary nodes");
		TIntArrayList listOfFaces = new TIntArrayList();
		for (expF.init(shape, CADShapeEnum.FACE); expF.more(); expF.next())
		{
			iFace++;
			if (numFace != 0 && iFace != numFace)
				continue;
			if (minFace != 0 && iFace < minFace)
				continue;
			if (maxFace != 0 && iFace > maxFace)
				continue;
			listOfFaces.add(iFace);
		}
		m2dTo3D.collectBoundaryNodes(listOfFaces.toNativeArray());
		m2dTo3D.beforeProcessingAllShapes(false);
		iFace = 0;
		for (expF.init(shape, CADShapeEnum.FACE); expF.more(); expF.next())
		{
			CADFace F = (CADFace) expF.current();
			iFace++;
			if (numFace != 0 && iFace != numFace)
				continue;
			if (minFace != 0 && iFace < minFace)
				continue;
			if (maxFace != 0 && iFace > maxFace)
				continue;
			logger.info("Importing face "+iFace);
			m2dTo3D.processOneShape(iFace, ""+iFace, iFace);
		}
		m2dTo3D.afterProcessingAllShapes();
	}
	
	private MeshToSoupConvert (String file, String dir, CADShape shape)
	{
		xmlDir = dir;
		soupFile = file;

		CADExplorer expF = CADShapeFactory.getFactory().newExplorer();
		int iFace = 0;
		for (expF.init(shape, CADShapeEnum.FACE); expF.more(); expF.next())
		{
			iFace++;
			mapFaces.put(iFace, (CADFace) expF.current());
		}
	}

	public final void collectBoundaryNodes(int[] faces)
	{
		for (int iFace : faces)
		{
			Document document;
			File xmlFile2d = null;
			try
			{
				xmlFile2d = new File(xmlDir, JCAEXMLData.xml2dFilename+iFace);
				document = XMLHelper.parseXML(xmlFile2d);
			}
			catch(FileNotFoundException ex)
			{
				continue;
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
				throw new RuntimeException(ex);
			}
			XPath xpath = XPathFactory.newInstance().newXPath();
			try
			{
				String formatVersion = xpath.evaluate("/jcae/@version", document);
				if (formatVersion != null && formatVersion.length() > 0)
					throw new RuntimeException("File "+xmlFile2d+" has been written by a newer version of jCAE and cannot be re-read");
				Node submeshElement = (Node) xpath.evaluate("/jcae/mesh/submesh",
					document, XPathConstants.NODE);
				Node submeshNodes = (Node) xpath.evaluate("nodes", submeshElement,
					XPathConstants.NODE);
				
				int numberOfReferences = Integer.parseInt(
					xpath.evaluate("references/number/text()", submeshNodes));
				nrRefs += numberOfReferences;
				int numberOfNodes = Integer.parseInt(
					xpath.evaluate("number/text()", submeshNodes));
				nrIntNodes += numberOfNodes - numberOfReferences;
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
				throw new RuntimeException(ex);
			}
			logger.fine("Total: "+nrRefs+" references");
		}
	}
	
	public final void beforeProcessingAllShapes(boolean writeNormal)
	{
		coordRefs = new double[3*nrRefs];
		xrefs = new TIntIntHashMap(nrRefs);

		rawFile = new File(xmlDir, soupFile);
		rawFile.delete();
	}
	
	public final void afterProcessingAllShapes()
	{
		logger.info("Total number of nodes: "+(nrNodes+nrIntNodes));
		logger.info("Total number of triangles: "+nrTriangles);
	}
	
	public final void processOneShape(int groupId, String groupName, int iFace)
	{
		Document documentIn;
		try
		{
			documentIn = XMLHelper.parseXML(new File(xmlDir, JCAEXMLData.xml2dFilename+iFace));
		}
		catch(FileNotFoundException ex)
		{
			return;
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}

		CADFace F = mapFaces.get(iFace);
		XPath xpath = XPathFactory.newInstance().newXPath();
		CADGeomSurface surface = F.getGeomSurface();
		surface.dinit(0);
		try
		{
			Node submeshElement = (Node) xpath.evaluate("/jcae/mesh/submesh",
				documentIn, XPathConstants.NODE);
			Node submeshNodes = (Node) xpath.evaluate("nodes", submeshElement,
				XPathConstants.NODE);
			Node submeshFaces = (Node) xpath.evaluate("triangles",
				submeshElement, XPathConstants.NODE);
			
			String refFile = xpath.evaluate("references/file/@location", submeshNodes);
			FileChannel fcR = new FileInputStream(xmlDir+File.separator+refFile).getChannel();
			int numberOfReferences = Integer.parseInt(
				xpath.evaluate("references/number/text()", submeshNodes));
			int [] refs = new int[numberOfReferences];
			logger.fine("Reading "+numberOfReferences+" references");
			bb.clear();
			bbI.clear();
			int remaining = numberOfReferences;
			int index = 0;
			int nf = bufferSize / 12;
			for (int nblock = (remaining * 12) / bufferSize; nblock >= 0; --nblock)
			{
				if (remaining <= 0)
					break;
				else if (remaining < nf)
					nf = remaining;
				remaining -= nf;
				bb.rewind();
				fcR.read(bb);
				bbI.rewind();
				for(int nr = 0; nr < nf; nr ++)
				{
					refs[index] = bbI.get();
					index++;
				}
			}
			assert index == numberOfReferences;

			int numberOfNodes = Integer.parseInt(xpath.evaluate(
				"number/text()", submeshNodes));

			logger.fine("Reading " + numberOfNodes + " nodes");
			String nodesFile = xpath.evaluate("file/@location", submeshNodes);
			FileChannel fcN = new FileInputStream(xmlDir+File.separator+nodesFile).getChannel();
			double [] coord = new double[3*numberOfNodes];
			bb.clear();
			bbD.clear();
			index = 0;
			remaining = numberOfNodes;
			nf = bufferSize / 16;
			for (int nblock = (remaining * 16) / bufferSize; nblock >= 0; --nblock)
			{
				if (remaining <= 0)
					break;
				else if (remaining < nf)
					nf = remaining;
				remaining -= nf;
				bb.rewind();
				fcN.read(bb);
				bbD.rewind();
				for(int nr = 0; nr < nf; nr ++)
				{
					double u = bbD.get();
					double v = bbD.get();
					if (index < numberOfNodes - numberOfReferences)
					{
						double [] p3 = surface.value(u, v);
						System.arraycopy(p3, 0, coord, 3 * index, 3);
					}
					else
					{
						int ref = refs[index - numberOfNodes + numberOfReferences];
						if (!xrefs.contains(ref))
						{
							double [] p3 = surface.value(u, v);
							xrefs.put(ref, nrNodes);
							System.arraycopy(p3, 0, coordRefs, 3 * nrNodes, 3);
							nrNodes++;
						}
						ref = xrefs.get(ref);
						System.arraycopy(coordRefs, 3 * ref, coord, 3 * index, 3);
					}
					index++;
				}
			}
			assert index == numberOfNodes;
			fcN.close();
			
			int numberOfFaces = Integer.parseInt(xpath.evaluate(
				"number/text()", submeshFaces));
			logger.fine("Reading " + numberOfFaces + " faces");
			String trianglesFile = xpath.evaluate("file/@location",
				submeshFaces);
			FileChannel fcT = new FileInputStream(xmlDir+File.separator+trianglesFile).getChannel();
			bb.clear();
			bbI.clear();
			FileChannel fcO = new FileOutputStream(rawFile, true).getChannel();
			ByteBuffer bbo = ByteBuffer.allocate(bufferSize * 80 / 12);
			DoubleBuffer bboD = bbo.asDoubleBuffer();
			int ind [] = new int[3];
			int indRef [] = new int[3];
			double [] c = new double[9];
			remaining = numberOfFaces;
			nf = bufferSize / 12;
			for (int nblock = (remaining * 12) / bufferSize; nblock >= 0; --nblock)
			{
				if (remaining <= 0)
					break;
				else if (remaining < nf)
					nf = remaining;
				remaining -= nf;
				bb.clear();
				fcT.read(bb);
				bbI.rewind();
				bbo.clear();
				bboD.rewind();
				for(int nr = 0; nr < nf; nr ++)
				{
					bbI.get(ind);
					if (ind[0] < 0 || ind[1] < 0 || ind[2] < 0)
					{
						// Outer triangle
						continue;
					}
					for (int j = 0; j < 3; j++)
					{
						if (ind[j] < numberOfNodes - numberOfReferences)
							indRef[j] = - j - 1;
						else
							indRef[j] = xrefs.get(refs[ind[j] - numberOfNodes + numberOfReferences]);
					}
					if (indRef[0] == indRef[1] || indRef[1] == indRef[2] || indRef[2] == indRef[0])
					{
						// Triangle bound from a degenerated edge
						continue;
					}
					nrTriangles++;
					for (int j = 0; j < 3; j++)
						System.arraycopy(coord, 3 * ind[j], c, 3 * j, 3);
					if (F.isOrientationForward())
					{
						for (int j = 0; j < 3; j++)
							for (int k = 0; k < 3; k++)
								bboD.put(c[3*j+k]);
					}
					else
					{
						for (int j = 2; j >= 0; j--)
							for (int k = 0; k < 3; k++)
								bboD.put(c[3*j+k]);
					}
					//  Align on 64bit
					bbo.position(8*bboD.position());
					bbo.putInt(iFace);
					bbo.putInt(0);
					bboD.position(1+bboD.position());
				}
				bbo.flip();
				fcO.write(bbo);
			}
			fcT.close();
			fcO.close();
			logger.fine("End reading");
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}
	}
}

