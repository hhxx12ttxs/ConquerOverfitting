/*
 * Project Info:  http://jcae.sourceforge.net
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
 *
 * (C) Copyright 2009, by EADS France
 */

package org.jcae.mesh.xmldata;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.xml.sax.SAXException;

/**
 * Helper class to write amibe files.
 * The format description can be found at
 * http://jcae.sourceforge.net/amibe.html#Amibe+file+format
 * @author Jerome Robert
 */
public abstract class AmibeWriter {

	private final static Logger LOGGER = Logger.getLogger(AmibeWriter.class.getName());
	private DataOutputStream bGroupChan;
	
	public static class Dim1 extends AmibeWriter {

		public Dim1(String name) throws IOException {
			init(name, true);
		}

		public void addNode(double x) throws IOException
		{
			nodeChan.writeDouble(x);
			numberOfNodes ++;
		}

		@Override
		protected int dim() {
			return 1;
		}
	}

	public static class Dim2 extends AmibeWriter {
		private final String binDirectory, xmlFile;
		public Dim2(String name, int index) throws IOException {			
			binDirectory = "jcae2d."+ index +".files";
			xmlFile = "jcae2d."+index;
			init(name, true);
		}

		public void addNode(double x, double y) throws IOException
		{
			nodeChan.writeDouble(x);
			nodeChan.writeDouble(y);
			numberOfNodes ++;
		}
		@Override
		protected int dim() {
			return 2;
		}

		@Override
		protected String binDirectory() {
			return binDirectory;
		}

		@Override
		protected String xmlFile() {
			return xmlFile;
		}
	}

	public static class Dim3 extends AmibeWriter {
		public Dim3(String name, boolean normal, boolean hasRef) throws IOException {
			init(name, hasRef);
			if(normal)
			{
				File dir3d = new File(name, binDirectory());
				File f = new File(dir3d, JCAEXMLData.normals3dFilename);
				normalChan = createDOS(f);
			}
		}

		public Dim3(String name) throws IOException {
			this(name, false);
		}

		public Dim3(String name, boolean normal) throws IOException {
			this(name, normal, false);
		}

		public void addNode(double x, double y, double z) throws IOException
		{
			nodeChan.writeDouble(x);
			nodeChan.writeDouble(y);
			nodeChan.writeDouble(z);
			numberOfNodes ++;
		}

		@Override
		protected int dim() {
			return 3;
		}

		public void addNormal(double x, double y, double z) throws IOException
		{
			normalChan.writeDouble(x);
			normalChan.writeDouble(y);
			normalChan.writeDouble(z);
		}
	}

	/**
	 * Contains informations about groups, which will be written to the
	 * XML file
	 */
	private static class Group
	{
		String name;
		long offset;
		int nbElement;
		long bOffset;
		int bNbElement;
	}

	private static class NIOutputStream extends OutputStream
	{
		private ByteBuffer bb = ByteBuffer.allocate(1);
		private ByteBuffer bb2;
		private final FileChannel channel;

		public NIOutputStream(FileChannel c) {
			this.channel = c;
		}

		@Override
		public void write(int b) throws IOException {
			bb.put(0, (byte)b);
			bb.rewind();
			channel.write(bb);
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			if(bb2 == null || bb2.capacity() != len)
				bb2 = ByteBuffer.allocate(len);
			else
				bb2.rewind();
			bb2.put(b, off, len);
			bb2.rewind();
			channel.write(bb2);
		}

		@Override
		public void close() throws IOException {
			channel.close();
		}
	}

	private static DataOutputStream createDOS(File f) throws FileNotFoundException
	{
		FileOutputStream fos = new FileOutputStream(f);
		return new DataOutputStream(new BufferedOutputStream(
			new NIOutputStream(fos.getChannel()), 1024*64));
	}
	
	protected DataOutputStream nodeChan, triaChan, groupChan, refChan,
		beamChan, normalChan, nodeGroupChan;
	private XMLWriter xmlWriter;
	protected int numberOfNodes, numberOfTriangles, numberOfRef, numberOfBeams;
	private List<Group> groups = new ArrayList<Group>();
	private List<Group> nodeGroups = new ArrayList<Group>();
	private long groupOffset, bGroupOffset, nodeGroupOffset;
	private Group currentGroup;
	private Group currentNodeGroup;
	private boolean checkNoGroup;
	private String shape;
	private boolean shapeWritten;
	private int subShape;
	private boolean haveSubShape;
	
	/** Set the subShape */
	public void setSubShape(int i)
	{
		haveSubShape = true;
		subShape = i;
	}
	public void setShape(String shape)
	{
		if(shapeWritten)
			//only one shape by file
			throw new IllegalStateException();
		this.shape = shape;
	}
	public void addNodeRef(int n) throws IOException
	{
		if (null == refChan)
			return;
		refChan.writeInt(n);
		numberOfRef++;
	}

	public void addNode(double[] coords) throws IOException
	{
		assert coords.length == dim();
		for(int i = 0; i<coords.length; i++)
			nodeChan.writeDouble(coords[i]);
		numberOfNodes ++;

	}

	/** Add a triangle to the current submesh */
	public void addTriangle(int i, int j, int k) throws IOException
	{
		triaChan.writeInt(i);
		triaChan.writeInt(j);
		triaChan.writeInt(k);
		numberOfTriangles ++;
	}

	/** Add a triangle to the current submesh */
	public void addTriangle(int[] indices) throws IOException
	{
		triaChan.writeInt(indices[0]);
		triaChan.writeInt(indices[1]);
		triaChan.writeInt(indices[2]);
		numberOfTriangles ++;
	}

	public void addBeam(int i, int j) throws IOException
	{
		beamChan.writeInt(i);
		beamChan.writeInt(j);
		numberOfBeams ++;
	}

	/**
	 * create a new sub mesh and make it current.
	 * There is no need to call this method to create the first submesh.
	 * submesh are currently only used when exporting the 1D mesh during the
	 * meshing process (see http://jcae.sourceforge.net/amibe.html#Algorithm)
	 */
	public void nextSubMesh() throws IOException
	{
		writeSubMesh();
		nodesOffset = numberOfNodes;
		triaOffset = numberOfTriangles;
		beamsOffset = numberOfBeams;
		numberOfNodes = 0;
		numberOfTriangles = 0;
		numberOfRef = 0;
		numberOfBeams = 0;
		groups.clear();
		haveSubShape = false;
	}

	/** Create a new group and make it the current group */
	public void nextGroup(String name)
	{
		if(name == null)
			throw new NullPointerException();
		Group g=new Group();
		g.name = name;
		g.offset= groupOffset;
		g.bOffset= bGroupOffset;
		groups.add(g);
		currentGroup = g;
	}

	/** 
	 * Add a triangle to the current group
	 * nextGroup must have been called before calling this method
	 * @param id the ID of the triangle to add
	 */
	public void addTriaToGroup(int id) throws IOException
	{
		groupChan.writeInt(id);
		groupOffset ++;
		currentGroup.nbElement ++;
	}

	/**
	 * Add a beam to the current group
	 * nextGroup must have been called before calling this method
	 * @param id the ID of the beam to add
	 */
	public void addBeamToGroup(int id) throws IOException
	{
		bGroupChan.writeInt(id);
		bGroupOffset ++;
		currentGroup.bNbElement ++;
	}
	
	public void finish() throws IOException
	{
		try {
			writeSubMesh();
			xmlWriter.out.writeEndElement();
			xmlWriter.out.writeEndElement();
			xmlWriter.close();
			nodeChan.close();
			triaChan.close();
			groupChan.close();
			nodeGroupChan.close();
			beamChan.close();
			bGroupChan.close();
			if(refChan != null)
				refChan.close();
		} catch (SAXException ex) {
			LOGGER.log(Level.SEVERE, null, ex);
		} catch (XMLStreamException ex) {
			LOGGER.log(Level.SEVERE, null, ex);
		}
	}

	private String nodeFName, triaFName, refFName, beamsFName;
	private int nodesOffset, beamsOffset, triaOffset;
	private DoubleFileReader nodesReader;
	private File fnode;
	protected final void init(String path, boolean writeReferences) throws IOException
	{
		try {
			new File(path).mkdirs();
			nodeFName = "nodes" + dim() + "d.bin";
			triaFName = "triangles" + dim() + "d.bin";
			beamsFName = "beams" + dim() + "d.bin";
			File dir3d = new File(path, binDirectory());
			dir3d.mkdirs();	
			fnode = new File(dir3d, nodeFName );
			File ftria = new File(dir3d, triaFName );
			File fgrp = new File(dir3d, "groups.bin");
			File fbeams = new File(dir3d, beamsFName);
			File bgroups = new File(dir3d, "bgroups.bin");
			if(writeReferences)
			{
				refFName = "nodes1dref.bin";
				File ref = new File(dir3d, refFName);
				refChan = createDOS(ref);
			}
			nodeChan = createDOS(fnode);
			triaChan = createDOS(ftria);
			groupChan = createDOS(fgrp);
			bGroupChan = createDOS(bgroups);
			nodeGroupChan = createDOS(new File(dir3d, "nodeGroups.bin"));
			beamChan = createDOS(fbeams);
			xmlWriter = new XMLWriter(new File(path, xmlFile()).getPath(),
				getClass().getResource("jcae.xsd"));
			xmlWriter.out.writeStartElement("jcae");
			xmlWriter.out.writeStartElement("mesh");
		} catch (XMLStreamException ex) {
			LOGGER.log(Level.SEVERE, null, ex);
		}
	}

	protected abstract int dim();
	protected String binDirectory()
	{
		return "jcae" + dim() + "d.files";
	}

	protected String xmlFile()
	{
		return "jcae" + dim() + "d";
	}
	
	public void getNode(int i, double[] nc) throws IOException {
		if(nodesReader == null)
		{
			nodesReader = new DoubleFileReaderByDirectBuffer(fnode);
		}
		nodesReader.get(i * dim(), nc);
	}

	private void writeNumber(int i) throws XMLStreamException
	{
		XMLStreamWriter o = xmlWriter.out;
		o.writeStartElement("number");
		o.writeCharacters(Integer.toString(i));
		o.writeEndElement();
	}

	private void writeFile(String format, String location, long offset) throws XMLStreamException
	{
		XMLStreamWriter o = xmlWriter.out;
		o.writeStartElement("file");
		o.writeAttribute("format", format);
		o.writeAttribute("location", location);
		if(offset != 0)
			o.writeAttribute("offset", Long.toString(offset));
		o.writeEndElement();
	}

	private void writeSubMesh() throws IOException
	{
		try
		{
			XMLStreamWriter o = xmlWriter.out;
			if(shape != null && !shapeWritten)
			{
				o.writeStartElement("shape");
				writeFile("brep", shape, 0);
				o.writeEndElement();
				shapeWritten = true;
			}
			
			String dir = binDirectory()+"/";
			o.writeStartElement("submesh");

			if(haveSubShape)
			{
				o.writeStartElement("subshape");
				o.writeCharacters(Integer.toString(subShape));
				o.writeEndElement();
			}
			o.writeStartElement("nodes");
			writeNumber(numberOfNodes);
			writeFile("doublestream", dir + nodeFName, nodesOffset);
			if(refChan != null)
			{
				o.writeStartElement("references");
				writeNumber(numberOfRef);
				writeFile("integerstream", dir + refFName, 0);
				o.writeEndElement();
			}
			o.writeEndElement(); //nodes

			if(numberOfBeams > 0)
			{
				o.writeStartElement("beams");
				writeNumber(numberOfBeams);
				writeFile("integerstream", dir + beamsFName, beamsOffset);
				o.writeEndElement();
			}

			if(numberOfTriangles > 0)
			{
				o.writeStartElement("triangles");
				writeNumber(numberOfTriangles);
				writeFile("integerstream", dir + triaFName, triaOffset);
				if(null != normalChan)
				{
					o.writeStartElement("normals");
					writeFile("doublestream", dir + JCAEXMLData.normals3dFilename, 0);
					o.writeEndElement();
				}
				o.writeEndElement(); //triangles
			}
			
			writeGroups();
			writeNodeGroups();
			o.writeEndElement(); //submesh
		} catch (XMLStreamException ex) {
			Logger.getLogger(AmibeWriter.class.getName()).log(Level.SEVERE, null,
				ex);
		}
	}

	private void writeGroups() throws XMLStreamException, IOException
	{
		if (checkNoGroup) {
			checkNoGroup();
		}
		if(!groups.isEmpty())
		{
			XMLStreamWriter o = xmlWriter.out;
			o.writeStartElement("groups");
			int id = 1;
			for (Group g : groups) {
				o.writeStartElement("group");
				o.writeAttribute("id", Integer.toString(id++));
				o.writeStartElement("name");
				o.writeCharacters(g.name);
				o.writeEndElement();
				if(g.nbElement > 0)
				{
					writeNumber(g.nbElement);
					writeFile("integerstream", binDirectory() + "/groups.bin", g.offset);
				}
				if(g.bNbElement > 0)
				{
					o.writeStartElement("beams");
					writeNumber(g.bNbElement);
					writeFile("integerstream", binDirectory() + "/bgroups.bin", g.bOffset);
					o.writeEndElement();
				}
				o.writeEndElement();
			}
			o.writeEndElement(); //groups
		}
	}

	/**
	 * Enable the fix group property.
	 * When the fix group property is enabled and no group has been added to
	 * the mesh, a default group containing all elements is created.
	 */
	public void setFixNoGroup(boolean b)
	{
		checkNoGroup = b;
	}
	/**
	 * If do not contains any groups, create one with all
	 * elements
	 */
	private void checkNoGroup() throws IOException
	{
		if(groups.isEmpty())
		{
			Group g=new Group();
			g.name="EXT";
			g.nbElement=numberOfTriangles;
			g.offset=0;
			groups.add(g);
			for(int i=0; i<numberOfTriangles; i++)
				groupChan.writeInt(i);
		}
	}

	private void writeNodeGroups() throws XMLStreamException, IOException
	{
		if(!nodeGroups.isEmpty())
		{
			XMLStreamWriter o = xmlWriter.out;
			o.writeStartElement("nodeGroups");
			for (Group g : nodeGroups) {
				o.writeStartElement("group");
				o.writeStartElement("name");
				o.writeCharacters(g.name);
				o.writeEndElement();
				if(g.nbElement > 0)
				{
					writeNumber(g.nbElement);
					writeFile("integerstream", binDirectory() + "/nodeGroups.bin", g.offset);
				}
				o.writeEndElement();
			}
			o.writeEndElement(); //groups
		}
	}

	/** Create a new group and make it the current node group */
	public void nextNodeGroup(String name)
	{
		if(name == null)
			throw new NullPointerException();
		Group g=new Group();
		g.name = name;
		g.offset= nodeGroupOffset;
		nodeGroups.add(g);
		currentNodeGroup = g;
	}

	/**
	 * Add a node to the current node group
	 * nextNodeGroup must have been called before calling this method
	 * @param id the ID of the node to add
	 */
	public void addNodeToGroup(int nodeID) throws IOException
	{
		nodeGroupChan.writeInt(nodeID);
		nodeGroupOffset ++;
		currentNodeGroup.nbElement ++;
	}
}

