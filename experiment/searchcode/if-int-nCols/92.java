/*
	Sketch Elements: Chemistry molecular diagram drawing tool.
	
	(c) 2008 Dr. Alex M. Clark
	
	Released as GNUware, under the Gnu Public License (GPL)
	
	See www.gnu.org for details.
*/

package net.sf.sketchel.ds;

import net.sf.sketchel.*;

import java.io.*;
import java.util.*;
import java.util.zip.*;

/*
	Readers and writers of the DataSheet format.
	
	The native format is XML (or at least, the subset of XML used by the TrivialDOM class), and is structured as follows:
	
		<?xml version="1.0" encoding="UTF-8"?>
		<DataSheet>
			<Summary>
				<Title>{title}</Title>
				<Description><![CDATA[{description}]]></Description>
			</Summary>
			<Extension>
				<Ext name=~ type=~><![CDATA[{extension}]]></Ext>
				...
			</Extension>
			<Header ncols=~ nrows=~>
				<Column id="1" name=~ type=~>{description}</Column>
				<Column id="2" name=~ type=~>{description}</Column>
				...
				<Column id="{ncols}" name=~ type=~>{description}</Column>
			</Header>
			<Content>
				<Row id="1">
					<Cell id="1">{data}</Cell>
					<Cell id="2">{data}</Cell>
					...
					<Cell id="{ncols}">{data}</Cell>
				</Row>
				...
				<Row id="{nrows}">
					...
				</Row>
			</Content>
		</DataSheet>
			
	All indices are 1-based.
				
*/

public class DataSheetStream
{
	// read-ahead code to try to figure out a filetype

	// returns true if stream is the native datasheet format; preserves file position
	public static boolean examineIsXMLDS(FileInputStream istr)
	{
		boolean ret=false;
		try
		{
			long lastpos=istr.getChannel().position();
			BufferedReader rdr=new BufferedReader(new InputStreamReader(istr));
			ret=examineIsXMLDS(rdr);
			istr.getChannel().position(lastpos);
		}
		catch (IOException e) {}
		return ret;
	}
	
	// as above, except this version loses the stream position
	public static boolean examineIsXMLDS(BufferedReader rdr)
	{
		try
		{
			for (int n=0;n<2;n++)
			{
				String str=rdr.readLine();
				if (str==null) break;
				if (str.startsWith("<DataSheet>")) return true;
			}
		}
		catch (IOException e) {}
		return false;
	}
	
	// returns true if stream appears to be an MDL SDfile; preserves file position
	public static boolean examineIsMDLSDF(FileInputStream istr)
	{
		boolean ret=false;
		try
		{
			long lastpos=istr.getChannel().position();
			BufferedReader rdr=new BufferedReader(new InputStreamReader(istr));
			ret=examineIsMDLSDF(rdr);
			istr.getChannel().position(lastpos);
		}
		catch (IOException e) {}
		return ret;
	}

	// as above, except this version loses the stream position
	public static boolean examineIsMDLSDF(BufferedReader rdr)
	{
		try
		{
			for (int n=0;n<3000;n++)
			{
				String str=rdr.readLine();
				if (str==null) break;
				if (str.compareTo("$$$$")==0 /* || str.compareTo("M  END")==0*/) return true;
			}
		}
		catch (IOException e) {}
		return false;
	 }

   // reading of datasheets from the SketchEl XML format

	public static DataSheetHolder readXML(InputStream istr) throws IOException 
	{
		return readXML(new BufferedReader(new InputStreamReader(istr)));
	}
	public static DataSheetHolder readXML(BufferedReader in) throws IOException
	{
		TrivialDOM xml=TrivialDOM.readXML(in);

		if (xml.document().nodeName().compareTo("DataSheet")!=0) 
			throw new DataSheetIOException("Input stream is XML, but the root node is not <DataSheet>.");
		
		DataSheetHolder ds=new DataSheetHolder();
		
		// do a precursory check
		TrivialDOM.Node doc=xml.document(),summary=null,extension=null,header=null,content=null;
		for (int n=0;n<doc.numChildren();n++) if (doc.childType(n)==TrivialDOM.TYPE_NODE)
		{
			TrivialDOM.Node node=doc.getChildNode(n);
			if (node.nodeName().equals("Summary")) summary=node;
			if (node.nodeName().equals("Extension")) extension=node;
			if (node.nodeName().equals("Header")) header=node;
			if (node.nodeName().equals("Content")) content=node;
		}
		if (header==null) throw new DataSheetIOException("XML document lacks a <Header> element.");
		if (content==null) throw new DataSheetIOException("XML document lacks a <Content> element.");
		
		parseXMLSummary(ds,summary);
		parseXMLExtension(ds,extension);
		parseXMLHeader(ds,header);
		
		int nrows=Util.safeInt(header.attribute("nrows"),-1);
		if (nrows<0) throw new DataSheetIOException("Header@nrows attribute absent or improperly specified.");
		
		// append a row for each claimed case, then fill in the data as it is encountered
		for (int n=0;n<nrows;n++) ds.appendRow();
		for (int i=0;i<content.numChildren();i++) if (content.childType(i)==TrivialDOM.TYPE_NODE)
		{
			TrivialDOM.Node row=content.getChildNode(i);
			if (row.nodeName().compareTo("Row")!=0) continue;
			
			int rid=Util.safeInt(row.attribute("id"),0);
			if (rid<1 || rid>nrows) throw new DataSheetIOException("Row@id out of range.");
			parseXMLRow(ds,row,rid-1);
		}

		return ds;
	}

	// subcategory: interpret the XML block containing the summary
	public static void parseXMLSummary(DataSheetHolder ds,TrivialDOM.Node summary) throws IOException
	{
		if (summary==null) return;
	
		for (int n=0;n<summary.numChildren();n++) if (summary.childType(n)==TrivialDOM.TYPE_NODE)
		{
			TrivialDOM.Node node=summary.getChildNode(n);
			if (node.nodeName().equals("Title")) ds.setTitle(node.getText());
			else if (node.nodeName().equals("Description")) ds.setDescription(node.getText());
		}
	}
	
	// subcategory: interpret the XML block containing the extension
	public static void parseXMLExtension(DataSheetHolder ds,TrivialDOM.Node extension) throws IOException
	{
		if (extension==null) return;
		
		for (int n=0;n<extension.numChildren();n++) if (extension.childType(n)==TrivialDOM.TYPE_NODE)
		{
			TrivialDOM.Node node=extension.getChildNode(n);
			if (node.nodeName().equals("Ext"))
				ds.appendExtension(node.attribute("name"),node.attribute("type"),node.getText());
		}
	}
	
	// subcategory: interpret the XML block containing the header
	public static void parseXMLHeader(DataSheetHolder ds,TrivialDOM.Node header) throws IOException
	{
		int ncols=Util.safeInt(header.attribute("ncols"),-1);
		if (ncols<0 || ncols>5000) throw new DataSheetIOException("Header@ncols attribute absent or improperly specified.");
		
		// put the columns into an array, then create in datasheet
		String[] colName=new String[ncols],colDescr=new String[ncols];
		int[] colType=new int[ncols];
		for (int n=0;n<ncols;n++) colName[n]=null; // means unspecified in source
		for (int n=0;n<header.numChildren();n++) if (header.childType(n)==TrivialDOM.TYPE_NODE)
		{
			TrivialDOM.Node node=header.getChildNode(n);
			if (node.nodeName().compareTo("Column")!=0) continue;
			int id=Util.safeInt(node.attribute("id"),0);
			if (id<1 || id>ncols) throw new DataSheetIOException("Column@id out of range.");
			String strName=node.attribute("name"),strType=node.attribute("type");
			if (strName==null) throw new DataSheetIOException("Column name not specified.");
			if (strType==null) throw new DataSheetIOException("Column type not specified.");
			int type=0;
			if (strType.compareTo("molecule")==0) type=DataSheet.COLTYPE_MOLECULE;
			else if (strType.compareTo("string")==0) type=DataSheet.COLTYPE_STRING;
			else if (strType.compareTo("integer")==0) type=DataSheet.COLTYPE_INTEGER;
			else if (strType.compareTo("real")==0) type=DataSheet.COLTYPE_REAL;
			else if (strType.compareTo("boolean")==0) type=DataSheet.COLTYPE_BOOLEAN;
			else if (strType.compareTo("extend")==0) type=DataSheet.COLTYPE_EXTEND;
			else throw new DataSheetIOException("Column type ["+strType+"] not recognised.");
			
			colName[id-1]=strName;
			colType[id-1]=type;
			colDescr[id-1]=node.getText();
		}
		for (int n=0;n<ncols;n++) if (colName[n]==null) throw new DataSheetIOException("Column id#"+(n+1)+" is not defined.");
		for (int n=0;n<ncols;n++) ds.appendColumn(colName[n],colType[n],colDescr[n]);
	}

	// subcategory: interprets and sets data from a row node; the row is self-indexing; more rows will be added if necessary
	public static void parseXMLRow(DataSheetHolder ds,TrivialDOM.Node row,int rowPos) throws IOException
	{
		for (int j=0;j<row.numChildren();j++) if (row.childType(j)==TrivialDOM.TYPE_NODE)
		{
			TrivialDOM.Node cell=row.getChildNode(j);
			if (cell.nodeName().compareTo("Cell")!=0) continue;
			int cid=Util.safeInt(cell.attribute("id"),0);
			if (cid<1 || cid>ds.numCols()) throw new DataSheetIOException("Cell@id out of range.");
			
			String data=cell.getText();
			int type=ds.colType(cid-1);
			
			if (type==DataSheet.COLTYPE_MOLECULE)
			{
				Molecule mol=null;
				try 
				{
					mol=MoleculeReader.readNative(new BufferedReader(new StringReader(data)));
				}
				catch (IOException e) {} // leave it null
				ds.setMolecule(rowPos,cid-1,mol);
			}
			else if (type==DataSheet.COLTYPE_STRING)
			{
				ds.setString(rowPos,cid-1,data);
			}
			else if (type==DataSheet.COLTYPE_INTEGER)
			{
				try
				{
					int v=new Integer(data).intValue();
					ds.setInteger(rowPos,cid-1,v);
				}
				catch (NumberFormatException e) {ds.setToNull(rowPos,cid-1);}
			}
			else if (type==DataSheet.COLTYPE_REAL)
			{
				try
				{
					double v=new Double(data).doubleValue();
					ds.setReal(rowPos,cid-1,v);
				}
				catch (NumberFormatException e) {ds.setToNull(rowPos,cid-1);}
			}
			else if (type==DataSheet.COLTYPE_BOOLEAN)
			{
				if (data.length()==0) ds.setToNull(rowPos,cid-1);
				else ds.setBoolean(rowPos,cid-1,data.toLowerCase().compareTo("true")==0);
			}
			else if (type==DataSheet.COLTYPE_EXTEND)
			{
				ds.setExtend(rowPos,cid-1,data);
			}
		}
	}

	// reading of datasheets from the MDL SD file format

	public static DataSheetHolder readSDF(InputStream istr) throws IOException 
	{
		return readSDF(new BufferedReader(new InputStreamReader(istr)));
	}
	public static DataSheetHolder readSDF(BufferedReader in) throws IOException
	{
		DataSheetHolder ds=new DataSheetHolder();
		
		ds.appendColumn("mol",DataSheet.COLTYPE_MOLECULE,"Molecule");

		ArrayList<String> entry=new ArrayList<String>();

		// read the lines from the SD file, and every time a field is encountered, add it as type "string"
		while (true)
		{
			String line=in.readLine();
			if (line==null) break;
			if (!line.startsWith("$$$$")) {entry.add(line); continue;}
			
			int rn=ds.appendRow();
			
			StringBuffer sb=new StringBuffer();
			int pos=0;
			while (pos<entry.size())
			{
				line=entry.get(pos);
				if (line.startsWith("> ")) break;
				sb.append(line+"\n"); 
				pos++; 
				if (line.startsWith("M	END")) {break;}
			}

			Molecule mol=null;
			try {mol=MoleculeReader.readMDLMOL(new BufferedReader(new StringReader(sb.toString())));}
			catch (IOException e) {} // leave it null
			if (mol!=null) ds.setMolecule(rn,0,mol);
			
			for (;pos+2<entry.size();pos+=3)
			{
				String key=entry.get(pos),val=entry.get(pos+1);
				if (!key.startsWith(">")) continue;
				int z=key.indexOf("<"); if (z<0) continue;
				key=key.substring(z+1);
				z=key.indexOf(">"); if (z<0) continue;
				key=key.substring(0,z);
				if (key.length()==0) continue;
				
				int cn=-1;
				for (int n=0;n<ds.numCols();n++) if (ds.colName(n).compareTo(key)==0) {cn=n; break;}
				if (cn<0) cn=ds.appendColumn(key,DataSheet.COLTYPE_STRING,"");
				
				if (val.length()==0) ds.setToNull(rn,cn);
				else ds.setString(rn,cn,val);
			}
		 
			entry.clear();
		}
		
		ds.upcastStringColumns();
				
		return ds;
	}
	
	// reading datasheets which may have been embedded in an OpenDocument file (e.g. ODT or ODS)
	
	public static DataSheetHolder readODF(InputStream istr) throws IOException 
	{
		ZipInputStream zip=new ZipInputStream(istr);
		
		while (true)
		{
			ZipEntry ent=zip.getNextEntry();
			if (ent==null) break;
			DataSheetHolder ds=null;
			if (ent.getName().equals("structure/datasheet.ds") && !ent.isDirectory())
			{
				ds=readXML(zip);
			}
			zip.closeEntry();
			if (ds!=null) return ds;
		}
		
		throw new MoleculeIOException("ODG file does not contain embedded sketch.");
	}

	// writing of datasheets to the SketchEl XML format
	
	public static void writeXML(OutputStream ostr,DataSheet ds) throws IOException
	{
		writeXML(new BufferedWriter(new OutputStreamWriter(ostr)),ds);
	}
	public static void writeXML(BufferedWriter out,DataSheet ds) throws IOException
	{
		TrivialDOM xml=new TrivialDOM("DataSheet");

		int ncols=ds.numCols(),nrows=ds.numRows();

		TrivialDOM.Node summary=xml.document().appendNode("Summary");
		summary.appendNode("Title").setText(ds.getTitle(),false);
		summary.appendNode("Description").setText(ds.getDescription(),true);
		
		if (ds.numExtensions()>0)
		{
			TrivialDOM.Node extension=xml.document().appendNode("Extension");
			for (int n=0;n<ds.numExtensions();n++)
			{
				TrivialDOM.Node ext=extension.appendNode("Ext");
				ext.setAttribute("type",ds.getExtType(n));
				ext.setAttribute("name",ds.getExtName(n));
				ext.setText(ds.getExtData(n),true);
			}
		}

		TrivialDOM.Node header=xml.document().appendNode("Header");
		header.setAttribute("ncols",ds.numCols()+"");
		header.setAttribute("nrows",ds.numRows()+"");
		for (int n=0;n<ncols;n++)
		{
			TrivialDOM.Node col=header.appendNode("Column");
			col.setAttribute("id",String.valueOf(n+1));
			col.setAttribute("name",ds.colName(n));
			int type=ds.colType(n);
			if (type==DataSheet.COLTYPE_MOLECULE) col.setAttribute("type","molecule");
			else if (type==DataSheet.COLTYPE_STRING) col.setAttribute("type","string");
			else if (type==DataSheet.COLTYPE_INTEGER) col.setAttribute("type","integer");
			else if (type==DataSheet.COLTYPE_REAL) col.setAttribute("type","real");
			else if (type==DataSheet.COLTYPE_BOOLEAN) col.setAttribute("type","boolean");
			else if (type==DataSheet.COLTYPE_EXTEND) col.setAttribute("type","extend");

			col.setText(ds.colDescr(n),false);
		}

		TrivialDOM.Node content=xml.document().appendNode("Content");
		for (int i=0;i<nrows;i++)
		{
			TrivialDOM.Node row=content.appendNode("Row");
			row.setAttribute("id",String.valueOf(i+1));
			for (int j=0;j<ncols;j++)
			{
				TrivialDOM.Node col=row.appendNode("Cell");
				col.setAttribute("id",String.valueOf(j+1));
				int type=ds.colType(j);
				if (ds.isNull(i,j)) {} // do nothing (stays blank)
				else if (type==DataSheet.COLTYPE_MOLECULE)
				{
					try
					{
						StringWriter sw=new StringWriter();
						BufferedWriter bw=new BufferedWriter(sw);
						MoleculeWriter.writeNative(bw,ds.getMolecule(i,j));
						col.setText(sw.toString(),true);
					}
					catch (IOException e) {} // entry stays blank
				}
				else if (type==DataSheet.COLTYPE_STRING) col.setText(ds.getString(i,j),true);
				else if (type==DataSheet.COLTYPE_INTEGER) col.setText(String.valueOf(ds.getInteger(i,j)),false);
				else if (type==DataSheet.COLTYPE_REAL) col.setText(String.valueOf(ds.getReal(i,j)),false);
				else if (type==DataSheet.COLTYPE_BOOLEAN) col.setText(ds.getBoolean(i,j) ? "true" : "false",false);
				else if (type==DataSheet.COLTYPE_EXTEND) col.setText(ds.getExtend(i,j),true);
			}
		}

		TrivialDOM.writeXML(out,xml);
	}
	
	// writing of datasheets to the MDL SD file format
	
	public static void writeSDF(OutputStream ostr,DataSheet ds) throws IOException
	{
		writeSDF(new BufferedWriter(new OutputStreamWriter(ostr)),ds);
	}
	public static void writeSDF(BufferedWriter out,DataSheet ds) throws IOException
	{
		int molfld=-1;
		for (int n=0;n<ds.numCols();n++) if (ds.colType(n)==DataSheet.COLTYPE_MOLECULE) {molfld=n; break;}
		
		for (int i=0;i<ds.numRows();i++)
		{
			// output the primary record
			if (molfld>=0) if (!ds.isNull(i,molfld))
			{
				MoleculeWriter.writeMDLMOL(out,ds.getMolecule(i,molfld));
			}
			for (int j=0;j<ds.numCols();j++) if (ds.colType(j)!=DataSheet.COLTYPE_MOLECULE && !ds.isNull(i,j))
			{
				String line="";
				
				if (ds.colType(j)==DataSheet.COLTYPE_STRING) line=ds.getString(i,j);
				else if (ds.colType(j)==DataSheet.COLTYPE_INTEGER) line=String.valueOf(ds.getInteger(i,j));
				else if (ds.colType(j)==DataSheet.COLTYPE_REAL) line=String.valueOf(ds.getReal(i,j));
				else if (ds.colType(j)==DataSheet.COLTYPE_BOOLEAN) line=ds.getBoolean(i,j) ? "true" : "false";

				if (line.length()==0) continue;
				String[] bits=line.split("\n");
				boolean anything=false;
				for (int n=0;n<bits.length;n++) if (bits[n].length()>0) anything=true;
				if (!anything) continue;
				
				out.write("> <"+ds.colName(j)+">\n");
				for (int n=0;n<bits.length;n++) if (bits[n].length()>0) 
				{
					if (bits[n].length()>78) bits[n]=bits[n].substring(0,78); // tuff
					out.write(bits[n]+"\n");
				}
				out.write("\n");
			}
			
			out.write("$$$$\n");
			
			// now, if there are any additional non-null molecule fields, write them out too
			for (int j=0;j<ds.numCols();j++) if (j!=molfld && ds.colType(j)==DataSheet.COLTYPE_MOLECULE && !ds.isNull(i,j))
			{
				MoleculeWriter.writeMDLMOL(out,ds.getMolecule(i,j));
				out.write("> <$$MOLFLD>\n");
				out.write(ds.colName(j)+"\n\n");
				out.write("$$$$\n");
			}
		}
		
		out.flush();
	}
}

