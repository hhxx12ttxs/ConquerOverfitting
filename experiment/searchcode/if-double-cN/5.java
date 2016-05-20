/*
	Sketch Elements: Chemistry molecular diagram drawing tool.
	
	(c) 2009 Dr. Alex M. Clark
	
	Released as GNUware, under the Gnu Public License (GPL)
	
	See www.gnu.org for details.
*/

package net.sf.sketchel.ds;

import net.sf.sketchel.*;

import java.io.*;

/*
	DataSheet is the abstract base class for molecular datasheet implementations. The core data access primitives are abstract,
	and must be implemented by a functional subclass. An additional set of abstract methods definite what the implementation is
	and isn't capable of.
*/

public abstract class DataSheet
{
	public static final int COLTYPE_MOLECULE=1;
	public static final int COLTYPE_STRING=2;
	public static final int COLTYPE_INTEGER=3;
	public static final int COLTYPE_REAL=4;
	public static final int COLTYPE_BOOLEAN=5;
	public static final int COLTYPE_EXTEND=6;
	
	protected class Extension
	{
		String Name;
		String Type;
		String Data;
	}
	
	protected class Column
	{
		String Name;
		int Type;
		String Descr;
	}
	
	// ---------------- capabilities ----------------
	
	public abstract boolean canRead(); // false=write only
	public abstract boolean canWrite(); // false=read only
	public abstract boolean canRandomRead(); // false=can only read at or after the watermark
	public abstract boolean canRandomWrite(); // false=can only write at or after the watermark
	public abstract boolean knowRowCount(); // false=the number of rows reported is unreliable
	public abstract boolean knowColTypes(); // false=the column count and types are not known at the outset
	public abstract boolean hasNativeMolecules(); // false=any molecule fields may have been imported from a foreign format
	public abstract int watermark(); // the limiting position, in the case of sequential access
	
	// ---------------- abstract access to data ----------------
	
	// overall dimensions
	public abstract int numCols();
	public abstract int numRows();
	
	// access to summary
	public abstract String getTitle();
	public abstract String getDescription();
	public abstract void setTitle(String title);
	public abstract void setDescription(String descr);
	
	// access to extensions
	public abstract int numExtensions();
	public abstract String getExtName(int N);
	public abstract String getExtType(int N);
	public abstract String getExtData(int N);
	public abstract String setExtName(int N,String V);
	public abstract String setExtType(int N,String V);
	public abstract String setExtData(int N,String V);
	public abstract void appendExtension(String Name,String Type,String Data);
	public abstract void deleteExtension(int N);
	
	// getting of column data
	public abstract String colName(int N);
	public abstract int colType(int N);
	public abstract String colDescr(int N);
		
	// getting of cell data
	public abstract boolean isNull(int RN,int CN);
	public abstract Molecule getMolecule(int RN,int CN);
	public abstract Molecule getMoleculeRef(int RN,int CN); // without clone; be very careful
	public abstract String getString(int RN,int CN);
	public abstract int getInteger(int RN,int CN);
	public abstract double getReal(int RN,int CN);
	public abstract boolean getBoolean(int RN,int CN);
	public abstract String getExtend(int RN,int CN);

	// setting of cell data
	public abstract void setToNull(int RN,int CN);
	public abstract void setMolecule(int RN,int CN,Molecule V);
	public abstract void setMoleculeRef(int RN,int CN,Molecule V); // without clone; be very careful
	public abstract void setString(int RN,int CN,String V);
	public abstract void setInteger(int RN,int CN,int V);
	public abstract void setReal(int RN,int CN,double V);
	public abstract void setBoolean(int RN,int CN,boolean V);
	public abstract void setExtend(int RN,int CN,String V);

	// cell comparison
	public abstract boolean isEqualMolecule(int RN,int CN,Molecule V);
	public abstract boolean isEqualString(int RN,int CN,String V);
	public abstract boolean isEqualInteger(int RN,int CN,int V);
	public abstract boolean isEqualReal(int RN,int CN,double V);
	public abstract boolean isEqualBoolean(int RN,int CN,boolean V);
	
	// column modification
	public abstract int appendColumn(String Name,int Type,String Descr);
	public abstract void deleteColumn(int CN);
	public abstract void changeColumnName(int CN,String Name,String Descr);
	public abstract boolean changeColumnType(int CN,int NewType,boolean Force);
	public abstract void reorderColumns(int[] Order);
	
	// row modification
	public abstract int appendRow();
	public abstract int appendRow(DataSheet src,int RN);
	public abstract void insertRow(int RN);
	public abstract void deleteRow(int RN);
	public abstract void moveRowUp(int RN);
	public abstract void moveRowDown(int RN);
		
	public void scan(int limit) {} // investigate a rewindable stream, to find content
	public void close() {} // only when connected to an I/O resource
	public void flush() {} // for direct-write streams only
	
	// ---------------- convenience function composites ----------------
	
	// saves switch statements
	public static String typeName(int Type) 
	{
		return Type==COLTYPE_MOLECULE ? "molecule" :
			   Type==COLTYPE_STRING ? "string" :
			   Type==COLTYPE_REAL ? "real" :
			   Type==COLTYPE_INTEGER ? "integer" :
			   Type==COLTYPE_BOOLEAN ? "boolean" : 
			   Type==COLTYPE_EXTEND ? "extend" : "?";
	}

	// convenience: saves regular typing
	public boolean colIsPrimitive(int N) 
	{
		int t=colType(N);
		return t==COLTYPE_STRING || t==COLTYPE_INTEGER || t==COLTYPE_REAL || t==COLTYPE_BOOLEAN;
	}
	
	// saves looping
	public int findColByName(String name) {for (int n=0;n<numCols();n++) if (name.equals(colName(n))) return n; return -1;}    
	
	// converts a cell to a string
	public String toString(int RN,int CN)
	{
		if (isNull(RN,CN)) return "";
		if (colType(CN)==COLTYPE_MOLECULE)
		{
			StringWriter sw=new StringWriter();
			try {MoleculeWriter.writeNative(new BufferedWriter(sw),getMolecule(RN,CN));} catch (IOException ex) {}
			return sw.toString();
		}
		if (colType(CN)==COLTYPE_STRING) return getString(RN,CN);
		if (colType(CN)==COLTYPE_INTEGER) return String.valueOf(getInteger(RN,CN));
		if (colType(CN)==COLTYPE_REAL) return String.valueOf(getReal(RN,CN));
		if (colType(CN)==COLTYPE_BOOLEAN) return getBoolean(RN,CN) ? "true" : "false";
		if (colType(CN)==COLTYPE_EXTEND) return getString(RN,CN);
		return "";
	}
}
