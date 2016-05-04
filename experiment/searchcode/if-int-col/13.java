/*
	Sketch Elements: Chemistry molecular diagram drawing tool.
	
	(c) 2009 Dr. Alex M. Clark
	
	Released as GNUware, under the Gnu Public License (GPL)
	
	See www.gnu.org for details.
*/

package net.sf.sketchel.ds;

import net.sf.sketchel.*;

import java.io.*;
import java.util.zip.*;

/*
	A dedicated writer class for producing OpenDocument files based on datasheets, which may contain any number of embedded
	graphic images.
	
	Makes use of the ODGBuilder class for individual molecules.
*/

public class ODFComposer
{
	private DataSheet ds;
	private ODGBuilder[][] odg=null;
	private RenderPolicy policy=new RenderPolicy();
	private int[] colWidth;
	private int rowHeight;
	
	private final String MIMETYPE_ODT="application/vnd.oasis.opendocument.text";
	private final String MIMETYPE_ODS="application/vnd.oasis.opendocument.spreadsheet";

	public static final int DOCTYPE_ODT=1; // word processor
	public static final int DOCTYPE_ODS=2; // spreadsheet
	private int doctype;

	public ODFComposer(DataSheet ds,int doctype)
	{
		this.ds=ds;
		this.doctype=doctype;
	}
	
	public void setRenderPolicy(RenderPolicy policy) {this.policy=policy;}
	
	public void build(OutputStream ostr) throws IOException
	{
		ZipOutputStream zip=new ZipOutputStream(ostr);
		CRC32 crc=new CRC32();
		zip.setLevel(6);
		
		// embed the metadata first
		for (int n=0;n<2;n++)
		{
			ByteArrayOutputStream bstr=new ByteArrayOutputStream();
			if (n==0) DataSheetStream.writeXML(bstr,ds); else DataSheetStream.writeSDF(bstr,ds);
			byte[] data=bstr.toByteArray();
			ZipEntry ent=new ZipEntry("structure/"+(n==0 ? "datasheet.ds" : "datasheet.sdf"));
			ent.setSize(data.length);
			crc.reset();
			crc.update(data);
			ent.setCrc(crc.getValue());
			zip.putNextEntry(ent);
			zip.write(data,0,data.length);
		}
		
		// prebuild the graphics
		createGraphics();
		
		// now the ODF content
		final String flist[]={"mimetype","META-INF/manifest.xml","content.xml"};
		for (int n=0;n<3;n++)
		{
			ByteArrayOutputStream bstr=new ByteArrayOutputStream();
			PrintWriter pw=new PrintWriter(bstr);
			if (n==0) outputMimeType(pw);
			else if (n==1) outputManifest(pw);
			else if (n==2) outputContent(pw);
			pw.flush();
			ZipEntry ent=new ZipEntry(flist[n]);
			byte[] data=bstr.toByteArray();
			ent.setSize(data.length);
			crc.reset();
			crc.update(data);
			ent.setCrc(crc.getValue());
			zip.putNextEntry(ent);
			zip.write(data,0,data.length);
		}
				
		zip.finish();
		zip.close();
	}
	
	private void createGraphics()
	{
		odg=new ODGBuilder[ds.numRows()][];
		colWidth=new int[ds.numCols()];
		for (int i=0;i<ds.numCols();i++) colWidth[i]=100;
		rowHeight=30;
		
		for (int i=0;i<ds.numRows();i++)
		{
			odg[i]=new ODGBuilder[ds.numCols()];
			for (int j=0;j<ds.numCols();j++) if (ds.colType(j)==DataSheet.COLTYPE_MOLECULE)
			{
				odg[i][j]=new ODGBuilder(false); // !! zip true or false: doesn't matter; interface is poorly designed
				odg[i][j].setPrefix("r"+(i+1)+"_c"+(j+1)+"_");
				VectorGfxMolecule vgmol=new VectorGfxMolecule(ds.getMolecule(i,j),policy,odg[i][j]);
				vgmol.draw();
				double lox=Math.floor(odg[i][j].lowX()),loy=Math.floor(odg[i][j].lowY());
				double hix=Math.ceil(odg[i][j].highX()),hiy=Math.ceil(odg[i][j].highY());
				int w=(int)(hix-lox),h=(int)(hiy-loy);
				odg[i][j].transform(-lox,-loy,1,1);
				colWidth[j]=Math.max(colWidth[j],w);
				rowHeight=Math.max(rowHeight,h);
			}
		}
	}
	
	private void outputMimeType(PrintWriter out)
	{
		out.print(doctype==DOCTYPE_ODT ? MIMETYPE_ODT : MIMETYPE_ODS);
	}
	
	private void outputManifest(PrintWriter out)
	{
		final String MANIFEST1=
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+
			"<!DOCTYPE manifest:manifest PUBLIC \"-//OpenOffice.org//DTD Manifest 1.0//EN\" \"Manifest.dtd\">\n"+
			"<manifest:manifest xmlns:manifest=\"urn:oasis:names:tc:opendocument:xmlns:manifest:1.0\">\n"+
			"	 <manifest:file-entry manifest:media-type=\"";
		final String MANIFEST2=
			"\" manifest:version=\"1.2\" manifest:full-path=\"/\"/>\n"+
			"	 <manifest:file-entry manifest:media-type=\"text/xml\" manifest:full-path=\"content.xml\"/>\n"+
			"</manifest:manifest>\n";
			
		out.println(MANIFEST1+(doctype==DOCTYPE_ODT ? MIMETYPE_ODT : MIMETYPE_ODS)+MANIFEST2);
	}
	
	private void outputContent(PrintWriter out) throws IOException
	{
		final String HEADER=
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+
			"<office:document-content\n"+
			" xmlns:office=\"urn:oasis:names:tc:opendocument:xmlns:office:1.0\"\n"+
			" xmlns:style=\"urn:oasis:names:tc:opendocument:xmlns:style:1.0\"\n"+
			" xmlns:text=\"urn:oasis:names:tc:opendocument:xmlns:text:1.0\"\n"+
			" xmlns:table=\"urn:oasis:names:tc:opendocument:xmlns:table:1.0\"\n"+
			" xmlns:draw=\"urn:oasis:names:tc:opendocument:xmlns:drawing:1.0\"\n"+
			" xmlns:fo=\"urn:oasis:names:tc:opendocument:xmlns:xsl-fo-compatible:1.0\"\n"+
			" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\"\n"+
			" xmlns:meta=\"urn:oasis:names:tc:opendocument:xmlns:meta:1.0\"\n"+
			" xmlns:number=\"urn:oasis:names:tc:opendocument:xmlns:datastyle:1.0\"\n"+
			" xmlns:svg=\"urn:oasis:names:tc:opendocument:xmlns:svg-compatible:1.0\"\n"+
			" xmlns:chart=\"urn:oasis:names:tc:opendocument:xmlns:chart:1.0\"\n"+
			" xmlns:dr3d=\"urn:oasis:names:tc:opendocument:xmlns:dr3d:1.0\"\n"+
			" xmlns:math=\"http://www.w3.org/1998/Math/MathML\" xmlns:form=\"urn:oasis:names:tc:opendocument:xmlns:form:1.0\"\n"+
			" xmlns:script=\"urn:oasis:names:tc:opendocument:xmlns:script:1.0\" xmlns:ooo=\"http://openoffice.org/2004/office\"\n"+
			" xmlns:ooow=\"http://openoffice.org/2004/writer\" xmlns:oooc=\"http://openoffice.org/2004/calc\"\n"+
			" xmlns:dom=\"http://www.w3.org/2001/xml-events\" xmlns:xforms=\"http://www.w3.org/2002/xforms\"\n"+
			" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"+
			" xmlns:rpt=\"http://openoffice.org/2005/report\" xmlns:of=\"urn:oasis:names:tc:opendocument:xmlns:of:1.2\"\n"+
			" xmlns:rdfa=\"http://docs.oasis-open.org/opendocument/meta/rdfa#\"\n"+
			" xmlns:field=\"urn:openoffice:names:experimental:ooo-ms-interop:xmlns:field:1.0\"\n"+
			" office:version=\"1.2\">\n"+
			"<office:scripts/>\n"+
			"<office:font-face-decls>\n"+
			"<style:font-face style:name=\"DejaVu Sans1\" svg:font-family=\"'DejaVu Sans'\" style:font-family-generic=\"roman\" style:font-pitch=\"variable\"/>\n"+
			"<style:font-face style:name=\"DejaVu Serif\" svg:font-family=\"'DejaVu Serif'\" style:font-family-generic=\"roman\" style:font-pitch=\"variable\"/>\n"+
			"<style:font-face style:name=\"DejaVu Sans\" svg:font-family=\"'DejaVu Sans'\" style:font-family-generic=\"swiss\" style:font-pitch=\"variable\"/>\n"+
			"<style:font-face style:name=\"DejaVu Sans2\" svg:font-family=\"'DejaVu Sans'\" style:font-family-generic=\"system\" style:font-pitch=\"variable\"/>\n"+
			"</office:font-face-decls>\n"+
			"<office:automatic-styles>\n"+
			"<style:style style:name=\"Table1\" style:family=\"table\">\n"+
			"<style:table-properties style:width=\"17.59cm\" table:align=\"margins\"/>\n"+
			"</style:style>\n"+
			"<style:style style:name=\"Table1.A\" style:family=\"table-column\">\n"+
			"<style:table-column-properties style:column-width=\"8.795cm\" style:rel-column-width=\"32767*\"/>\n"+
			"</style:style>\n"+
			"<style:style style:name=\"P1\" style:family=\"paragraph\">\n"+
			"<style:paragraph-properties fo:margin-left=\"0cm\" fo:margin-right=\"0cm\" fo:margin-top=\"0cm\" fo:margin-bottom=\"0cm\" fo:line-height=\"100%\" fo:text-indent=\"0cm\"/>\n"+
			"<style:text-properties style:use-window-font-color=\"true\" style:text-outline=\"false\" style:text-line-through-style=\"none\""+
			" style:font-name=\"DejaVu Sans1\" fo:font-size=\"18pt\" fo:font-style=\"normal\" fo:text-shadow=\"none\" style:text-underline-style=\"none\""+
			" fo:font-weight=\"normal\" style:letter-kerning=\"true\" style:font-name-asian=\"DejaVu Sans2\" style:font-size-asian=\"18pt\" style:font-style-asian=\"normal\""+
			" style:font-weight-asian=\"normal\" style:font-name-complex=\"DejaVu Sans2\" style:font-size-complex=\"18pt\" style:font-style-complex=\"normal\""+
			" style:font-weight-complex=\"normal\" style:text-emphasize=\"none\" style:font-relief=\"none\" style:text-overline-style=\"none\" style:text-overline-color=\"font-color\"/>\n"+
			"</style:style>\n"+
			"<style:default-style style:family=\"graphic\">\n"+
			"<style:graphic-properties draw:shadow-offset-x=\"0.3cm\" draw:shadow-offset-y=\"0.3cm\"\n"+
			" draw:start-line-spacing-horizontal=\"0.283cm\" draw:start-line-spacing-vertical=\"0.283cm\"\n"+
			" draw:end-line-spacing-horizontal=\"0.283cm\" draw:end-line-spacing-vertical=\"0.283cm\" style:flow-with-text=\"false\"/>\n"+
			"<style:paragraph-properties style:text-autospace=\"ideograph-alpha\" style:line-break=\"strict\" style:writing-mode=\"lr-tb\"\n"+
			" style:font-independent-line-spacing=\"false\">\n"+
			"<style:tab-stops/>\n"+
			"</style:paragraph-properties>\n"+
			"<style:text-properties style:use-window-font-color=\"true\" fo:font-size=\"12pt\" fo:language=\"en\" fo:country=\"CA\"\n"+
			" style:letter-kerning=\"true\" style:font-size-asian=\"12pt\" style:language-asian=\"zxx\" style:country-asian=\"none\"\n"+
			"  style:font-size-complex=\"12pt\" style:language-complex=\"zxx\" style:country-complex=\"none\"/>\n"+
			"</style:default-style>\n"+
			"<style:style style:name=\"mol\" style:family=\"graphic\">\n"+
			"<style:graphic-properties style:run-through=\"foreground\"\n"+
			" style:wrap=\"run-through\" style:number-wrapped-paragraphs=\"no-limit\"\n"+
			" style:vertical-pos=\"middle\" style:vertical-rel=\"baseline\" style:horizontal-pos=\"from-left\" style:horizontal-rel=\"paragraph\"\n"+
			"/></style:style>\n";
			
		out.println(HEADER);
		
		for (int n=0;n<ds.numCols();n++)
		{
			out.println("<style:style style:name=\"col"+(n+1)+"\" style:family=\"table-column\">");
			out.println("<style:table-column-properties fo:break-before=\"auto\" style:column-width=\""+colWidth[n]+"pt\"/>");
			out.println("</style:style>");
		}
		for (int n=0;n<2;n++)
		{
			out.println("<style:style style:name=\"row"+n+"\" style:family=\"table-row\">");
			out.println("<style:table-row-properties style:row-height=\""+(n==0 ? 30 : rowHeight)+"pt\"");
			out.println(" fo:break-before=\"auto\" style:use-optimal-row-height=\"false\"/>");
			out.println("</style:style>");
		}
		
		for (int i=0;i<ds.numRows();i++)
		{
			for (int j=0;j<ds.numCols();j++) if (ds.colType(j)==DataSheet.COLTYPE_MOLECULE)
			{
				double lox=Math.floor(odg[i][j].lowX()),loy=Math.floor(odg[i][j].lowY());
				double hix=Math.ceil(odg[i][j].highX()),hiy=Math.ceil(odg[i][j].highY());
				int w=(int)(hix-lox),h=(int)(hiy-loy);
				
				odg[i][j].buildStyles(out,w,h);
			}
		}
		
		out.println("</office:automatic-styles>");
		out.println("<office:body>");
		
		if (doctype==DOCTYPE_ODT) outputDocumentText(out);
		else if (doctype==DOCTYPE_ODS) outputDocumentSheet(out);
		
		out.println("</office:body>");
		out.println("</office:document-content>");
	}
	
	private void outputDocumentText(PrintWriter out) throws IOException
	{
		out.println("<office:text>");
		out.println("<text:sequence-decls>");
		out.println("<text:sequence-decl text:display-outline-level=\"0\" text:name=\"Illustration\"/>");
		out.println("<text:sequence-decl text:display-outline-level=\"0\" text:name=\"Table\"/>");
		out.println("<text:sequence-decl text:display-outline-level=\"0\" text:name=\"Text\"/>");
		out.println("<text:sequence-decl text:display-outline-level=\"0\" text:name=\"Drawing\"/>");
		out.println("</text:sequence-decls>");
	
		// write out banner text
		out.println("<text:p text:style-name=\"Standard\">"+TrivialDOM.escapeText(ds.getTitle())+"</text:p>");
		if (ds.getDescription().length()>0) 
			out.println("<text:p text:style-name=\"Standard\">"+TrivialDOM.escapeText(ds.getDescription())+"</text:p>");
		
		// write the table containing the datasheet
		out.println("<table:table table:name=\"Table1\" table:style-name=\"Table1\">");
		out.println("  <table:table-column table:style-name=\"Table1.A\" table:number-columns-repeated=\""+ds.numCols()+"\"/>");
		
		// write a header for each column
		out.println("  <table:table-row>");
		for (int n=0;n<ds.numCols();n++)
		{
			out.println("	 <table:table-cell table:style-name=\"Table1.A1\" office:value-type=\"string\">");
			out.println("	   <text:p text:style-name=\"Table_20_Contents\">");
			out.println(TrivialDOM.escapeText(ds.colName(n)));
			out.println("	   </text:p>");
			out.println("	 </table:table-cell>");
		}
		out.println("  </table:table-row>");
		
		// write each row & column
		for (int i=0;i<ds.numRows();i++)
		{
			out.println("  <table:table-row>");
			
			for (int j=0;j<ds.numCols();j++)
			{
				out.println("	 <table:table-cell table:style-name=\"Table1.A1\" office:value-type=\"string\">");
				out.println("	   <text:p text:style-name=\"Table_20_Contents\">");
				
				outputTableCell(out,i,j);
				
				out.println("	   </text:p>");
				out.println("	 </table:table-cell>");
			}
			
			out.println("  </table:table-row>");
		}
		
		out.println("</table:table>");
		
		out.println("<text:p text:style-name=\"Standard\">");
		out.println("NOTE: this document contains embedded metadata for the underlying DataSheet.");
		out.println("Saving the document, or moving the molecule diagrams to another document,");
		out.println("will not preserve the chemical structures in a machine-readable form.");
		out.println("</text:p>");
		
		out.println("</office:text>");
	}
	
	private void outputDocumentSheet(PrintWriter out) throws IOException
	{
		out.println("<office:spreadsheet>");
		out.println("<table:table table:name=\"DataSheet\">");
		
		/*out.println("<table:table-column table:number-columns-repeated=\""+ds.numCols()+"\""+
					" table:default-cell-style-name=\"Default\"/>");*/
		for (int n=0;n<=ds.numCols();n++)
		{
			out.println("<table:table-column table:style-name=\"col"+(n+1)+"\" table:default-cell-style-name=\"Default\"/>");
		}

		// banner
		out.println("  <table:table-row table:style-name=\"row0\">"); 
		for (int n=0;n<ds.numCols();n++)
		{
			out.println("	 <table:table-cell office:value-type=\"string\">");
			out.println("	   <text:p>");
			out.println(TrivialDOM.escapeText(ds.colName(n)));
			out.println("	   </text:p>");
			out.println("	 </table:table-cell>");
		}
		out.println("  </table:table-row>");
		
		// write each row & column
		for (int i=0;i<ds.numRows();i++)
		{
			out.println("  <table:table-row table:style-name=\"row1\">");
			
			for (int j=0;j<ds.numCols();j++)
			{
				out.println("	 <table:table-cell office:value-type=\"string\">");
				
				outputSheetCell(out,i,j);
				
				out.println("	 </table:table-cell>");
			}
			
			out.println("  </table:table-row>");
		}
		
		out.println("</table:table>");
		out.println("</office:spreadsheet>");
	}
	
	private void outputTableCell(PrintWriter out,int row,int col) throws IOException
	{
		if (ds.isNull(row,col)) return;
		
		// full in the simple parts of the table
		if (ds.colType(col)==DataSheet.COLTYPE_STRING)
		{
			out.println(TrivialDOM.escapeText(ds.getString(row,col)));
		}
		else if (ds.colType(col)==DataSheet.COLTYPE_INTEGER)
		{
			out.println(String.valueOf(ds.getInteger(row,col)));
		}
		else if (ds.colType(col)==DataSheet.COLTYPE_REAL)
		{
			out.println(String.valueOf(ds.getReal(row,col)));
		}
		else if (ds.colType(col)==DataSheet.COLTYPE_BOOLEAN)
		{
			out.println(ds.getBoolean(row,col) ? "true" : "false");
		}
		else {} // draw anything for COLTYPE_EXTEND?
	
		if (ds.colType(col)!=DataSheet.COLTYPE_MOLECULE) return;
		
		// output the molecule graphic
		double lox=Math.floor(odg[row][col].lowX()),loy=Math.floor(odg[row][col].lowY());
		double hix=Math.ceil(odg[row][col].highX()),hiy=Math.ceil(odg[row][col].highY());
		int w=(int)(hix-lox),h=(int)(hiy-loy);
		
		out.println("<draw:g text:anchor-type=\"as-char\" draw:z-index=\"1\" draw:style-name=\"mol\">");
		odg[row][col].buildContent(out,w,h);
		out.println("</draw:g>");
	}
	
	private void outputSheetCell(PrintWriter out,int row,int col) throws IOException
	{
		if (ds.isNull(row,col)) return;
		
		// full in the simple parts of the table
		if (ds.colType(col)==DataSheet.COLTYPE_STRING)
		{
			out.println("	   <text:p>");
			out.println(TrivialDOM.escapeText(ds.getString(row,col)));
			out.println("	   </text:p>");
		}
		else if (ds.colType(col)==DataSheet.COLTYPE_INTEGER)
		{
			out.println("	   <text:p>");
			out.println(String.valueOf(ds.getInteger(row,col)));
			out.println("	   </text:p>");
		}
		else if (ds.colType(col)==DataSheet.COLTYPE_REAL)
		{
			out.println("	   <text:p>");
			out.println(String.valueOf(ds.getReal(row,col)));
			out.println("	   </text:p>");
		}
		else if (ds.colType(col)==DataSheet.COLTYPE_BOOLEAN)
		{
			out.println("	   <text:p>");
			out.println(ds.getBoolean(row,col) ? "true" : "false");
			out.println("	   </text:p>");
		}
		else {} // draw anything for COLTYPE_EXTEND?
	
		if (ds.colType(col)!=DataSheet.COLTYPE_MOLECULE) return;
		
		// output the molecule graphic
		double lox=Math.floor(odg[row][col].lowX()),loy=Math.floor(odg[row][col].lowY());
		double hix=Math.ceil(odg[row][col].highX()),hiy=Math.ceil(odg[row][col].highY());
		int w=(int)(hix-lox),h=(int)(hiy-loy);
		
		out.println("<draw:g draw:style-name=\"mol\"");
		out.println(" table:end-cell-address=\"DataSheet."+(char)('A'+col)+String.valueOf(row+2)+"\"");
		out.println(" table:end-x=\""+w+"pt\" table:end-y=\""+h+"pt\"");
		out.println(">");
		odg[row][col].buildContent(out,w,h);
		out.println("</draw:g>");
	}
}



