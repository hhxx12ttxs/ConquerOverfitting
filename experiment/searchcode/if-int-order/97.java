/*
	Sketch Elements: Chemistry molecular diagram drawing tool.
	
	(c) 2008-2009 Dr. Alex M. Clark
	
	Released as GNUware, under the Gnu Public License (GPL)
	
	See www.gnu.org for details.
*/

package net.sf.sketchel.ds;

import net.sf.sketchel.*;

import java.util.*;

/*
	DataSheetHolder is a container class for storing a row/column format collection of molecular data. This implementation stores 
	the entirety of the collection in memory, and is not intended to be used for large datasets. Its file format is expected to be 
	an XML format, or imported/exported from various others, such as SD files.
	
	This class is a complete implementation of the DataSheet abstract class, and has no limitations.
	
	Internal storage: all cells are stored as objects which, if not null, will be typecasted into convenient types. Although there
	are methods for obtaining the actual objects stored in the sheet, the normal means of access involves cloning anything that
	goes in/comes out, to avoid modifying references by accident.
*/

public class DataSheetHolder extends DataSheet
{	 
	protected String title="",descr="";
	
	protected ArrayList<Extension> ext=new ArrayList<Extension>();
	
	protected ArrayList<Column> cols=new ArrayList<Column>();
	protected ArrayList<Object[]> rows=new ArrayList<Object[]>();

	protected boolean isDirty=false;

	// create a blank datasheet
	public DataSheetHolder()
	{
	}
	
	// create a datasheet which has its own heading information and container arrays, but has shallow copies of all the actual
	// data; it is generally safe to do this, because the normal methods for modifying the data itself involves replacing with
	// references to new and different objects, rather than poking them directly; because of this, the constructor has minimal
	// overhead; note the optional boolean invocations, which allows row data and extension data to be included or excluded
	public DataSheetHolder(DataSheet ds) {cloneInit(ds,true,true);}
	public DataSheetHolder(DataSheet ds,boolean inclData) {cloneInit(ds,inclData,true);}
	public DataSheetHolder(DataSheet ds,boolean inclData,boolean inclExtn) {cloneInit(ds,inclData,inclExtn);}
	
	public void cloneInit(DataSheet ds,boolean inclData,boolean inclExtn)
	{
		DataSheetHolder dsh=ds instanceof DataSheetHolder ? (DataSheetHolder)ds : null;
		
		isDirty=dsh==null ? false : dsh.isDirty();
		title=ds.getTitle();
		descr=ds.getDescription();
		int ncols=ds.numCols();
		for (int n=0;n<ncols;n++)
		{
			Column c=new Column();
			c.Name=ds.colName(n);
			c.Type=ds.colType(n);
			c.Descr=ds.colDescr(n);
			cols.add(c);
		}
		if (inclData) 
		{
			for (int i=0;i<ds.numRows();i++)
			{
				Object[] r=new Object[ncols];
				for (int j=0;j<ncols;j++) 
				{
					if (dsh!=null) r[j]=dsh.getObject(i,j);
					else if (ds.isNull(i,j)) r[j]=null;
					else if (ds.colType(j)==COLTYPE_MOLECULE) r[j]=ds.getMolecule(i,j);
					else if (ds.colType(j)==COLTYPE_STRING) r[j]=ds.getString(i,j);
					else if (ds.colType(j)==COLTYPE_INTEGER) r[j]=ds.getInteger(i,j);
					else if (ds.colType(j)==COLTYPE_REAL) r[j]=ds.getReal(i,j);
					else if (ds.colType(j)==COLTYPE_BOOLEAN) r[j]=ds.getBoolean(i,j);
					else if (ds.colType(j)==COLTYPE_EXTEND) r[j]=ds.getExtend(i,j);
				}
				rows.add(r);
			}
		}
		if (inclExtn) for (int i=0;i<ds.numExtensions();i++)
		{
			appendExtension(ds.getExtName(i),ds.getExtType(i),ds.getExtData(i));
		}
	}
	
	public DataSheetHolder clone() {return new DataSheetHolder(this);}
	
	public boolean canRead() {return true;}
	public boolean canWrite() {return true;}
	public boolean canRandomRead() {return true;}
	public boolean canRandomWrite() {return true;}
	public boolean knowRowCount() {return true;}
	public boolean knowColTypes() {return true;}
	public boolean hasNativeMolecules() {return true;}
	public int watermark() {return -1;}
	
	public int numCols() {return cols.size();}
	public int numRows() {return rows.size();}
	
	public boolean isDirty() {return isDirty;}
	public void setDirty() {isDirty=true;} 
	public void clearDirty() {isDirty=false;}
	
	// summary info, for the database overall
	public String getTitle() {return title;}
	public String getDescription() {return descr;}
	public void setTitle(String title) {this.title=title.trim();} // (whitespace removed)
	public void setDescription(String descr) {this.descr=descr;} // (whitespace allowed)
	
	// extension fields
	public int numExtensions() {return ext.size();}
	public String getExtName(int N) {return ext.get(N).Name;}
	public String getExtType(int N) {return ext.get(N).Type;}
	public String getExtData(int N) {return ext.get(N).Data;}
	public String setExtName(int N,String V) {return ext.get(N).Name=V;}
	public String setExtType(int N,String V) {return ext.get(N).Type=V;}
	public String setExtData(int N,String V) {return ext.get(N).Data=V;}
	public void appendExtension(String Name,String Type,String Data)
	{
		Extension ex=new Extension();
		ex.Name=Name;
		ex.Type=Type;
		ex.Data=Data;
		ext.add(ex);
	}
	public void deleteExtension(int N) {ext.remove(N);}
	
	// reading column info
	public String colName(int N) {return cols.get(N).Name;}
	public int colType(int N) {return cols.get(N).Type;}
	public String colDescr(int N) {return cols.get(N).Descr;}
		
	// returns whether a cell is null; should always be checked for primitive types
	public boolean isNull(int RN,int CN) {return (rows.get(RN))[CN]==null;}

	// fetching row data; note that the correct type must be use, else exception
	public Molecule getMolecule(int RN,int CN) {Molecule mol=(Molecule)(rows.get(RN))[CN]; return mol==null ? null : mol.clone();}
	public Molecule getMoleculeRef(int RN,int CN) {Molecule mol=(Molecule)(rows.get(RN))[CN]; return mol==null ? null : mol;}
	public String getString(int RN,int CN) {return (String)(rows.get(RN))[CN];}
	public int getInteger(int RN,int CN) {return ((Integer)(rows.get(RN))[CN]).intValue();}
	public double getReal(int RN,int CN) {return ((Double)(rows.get(RN))[CN]).doubleValue();}
	public boolean getBoolean(int RN,int CN) {return ((Boolean)(rows.get(RN))[CN]).booleanValue();}
	public String getExtend(int RN,int CN) {return (String)(rows.get(RN))[CN];}

	// gets the untyped object for a cell; use with care
	public Object getObject(int RN,int CN) {return (rows.get(RN))[CN];}

	// sets a cell to null, which is valid for all types
	public void setToNull(int RN,int CN) {(rows.get(RN))[CN]=null;}

	// setting row data; fails silently if the type is wrong
	public void setMolecule(int RN,int CN,Molecule V) {if (colType(CN)==COLTYPE_MOLECULE) (rows.get(RN))[CN]=V==null ? V : V.clone();}
	public void setMoleculeRef(int RN,int CN,Molecule V) {if (colType(CN)==COLTYPE_MOLECULE) (rows.get(RN))[CN]=V==null ? V : V;}
	public void setString(int RN,int CN,String V) {if (colType(CN)==COLTYPE_STRING) (rows.get(RN))[CN]=V;}
	public void setInteger(int RN,int CN,int V) {if (colType(CN)==COLTYPE_INTEGER) (rows.get(RN))[CN]=new Integer(V);}
	public void setReal(int RN,int CN,double V) {if (colType(CN)==COLTYPE_REAL) (rows.get(RN))[CN]=new Double(V);}
	public void setBoolean(int RN,int CN,boolean V) {if (colType(CN)==COLTYPE_BOOLEAN) (rows.get(RN))[CN]=new Boolean(V);}
	public void setExtend(int RN,int CN,String V) {if (colType(CN)==COLTYPE_EXTEND) (rows.get(RN))[CN]=V;}

	// sets the object for a cell, without any type checking; use with care
	public void setObject(int RN,int CN,Object V) {(rows.get(RN))[CN]=V;}

	// each return true if the current data is equal to that being compared to
	public boolean isEqualMolecule(int RN,int CN,Molecule V)
	{
		Molecule v=(Molecule)(rows.get(RN))[CN];
		if (v==null && V==null) return true;
		if (v==null || V==null) return false;
		return v.compareTo(V)==0;
	}
	public boolean isEqualString(int RN,int CN,String V)
	{
		String v=(String)(rows.get(RN))[CN];
		if (v==null && V==null) return true;
		if (v==null || V==null) return false;
		return v.equals(V);
	}
	public boolean isEqualInteger(int RN,int CN,int V)
	{
		Integer v=(Integer)(rows.get(RN))[CN];
		if (v==null) return false;
		return v.intValue()==V;
	}
	public boolean isEqualReal(int RN,int CN,double V)
	{
		Double v=(Double)(rows.get(RN))[CN];
		if (v==null) return false;
		return v.doubleValue()==V;
	}
	public boolean isEqualBoolean(int RN,int CN,boolean V)
	{
		Boolean v=(Boolean)(rows.get(RN))[CN];
		if (v==null) return false;
		return v.booleanValue()==V;
	}
	
	// appends a new column to the end of the list, and updates the underlying data accordingly
	public int appendColumn(String Name,int Type,String Descr)
	{
		Column c=new Column();
		c.Name=Name;
		c.Type=Type;
		c.Descr=Descr;
		cols.add(c);
		for (int n=0;n<rows.size();n++)
		{
			Object[] d1=rows.get(n);
			Object[] d2=new Object[d1.length+1];
			for (int i=0;i<d1.length;i++) d2[i]=d1[i];
			d2[d1.length]=null;
			rows.set(n,d2);
		}
		return cols.size()-1;
	}
	
	// appends a row containing all-nulls to the end of the list, and returns the new index position
	public int appendRow()
	{
		rows.add(new Object[cols.size()]);
		return rows.size()-1;
	}
	
	// a special convenience feature for when we want to copy a row from one datasheet to another, and we are really sure that
	// the columns are identical; if this condition is not met, then bad things will happen; note also that the data is a shallow
	// copy, which is not a problem unless the caller insists on manipulating the objects directly
	public int appendRow(DataSheet src,int RN)
	{
		DataSheetHolder srch=src instanceof DataSheetHolder ? (DataSheetHolder)src : null;
		Object[] r=new Object[numCols()];
		for (int n=0;n<numCols();n++) 
		{
			if (srch!=null) r[n]=srch.getObject(RN,n);
			else if (src.isNull(RN,n)) r[n]=null;
			else if (src.colType(n)==COLTYPE_MOLECULE) r[n]=src.getMolecule(RN,n);
			else if (src.colType(n)==COLTYPE_STRING) r[n]=src.getString(RN,n);
			else if (src.colType(n)==COLTYPE_INTEGER) r[n]=src.getInteger(RN,n);
			else if (src.colType(n)==COLTYPE_REAL) r[n]=src.getReal(RN,n);
			else if (src.colType(n)==COLTYPE_BOOLEAN) r[n]=src.getBoolean(RN,n);
			else if (src.colType(n)==COLTYPE_EXTEND) r[n]=src.getExtend(RN,n);
		}
		rows.add(r);
		return rows.size()-1;
	}
	
	// puts a row in before that which is indicated; inserting after a non-existent row is equivalent to append
	public void insertRow(int RN)
	{
		rows.add(RN,new Object[cols.size()]);
	}
	
	// deletes the given row, moves everything else u p
	public void deleteRow(int RN) {rows.remove(RN);}
	
	
	// shuffles a single row upward
	public void moveRowUp(int RN)
	{
		if (RN==0 || RN>=rows.size()) return;
		Object[] o=rows.get(RN-1);
		rows.set(RN-1,rows.get(RN));
		rows.set(RN,o);
	}
	
	// shuffles a single row upward
	public void moveRowDown(int RN)
	{
		if (RN<0 || RN>=rows.size()-1) return;
		Object[] o=rows.get(RN+1);
		rows.set(RN+1,rows.get(RN));
		rows.set(RN,o);
	}
	
	// removes a column, and adjusts all the data accordingly
	public void deleteColumn(int CN) 
	{
		cols.remove(CN);
		for (int n=0;n<rows.size();n++)
		{
			Object[] prev=rows.get(n),cur=new Object[cols.size()];
			for (int i=0,j=0;i<prev.length;i++) if (i!=CN) cur[j++]=prev[i];
			rows.set(n,cur);
		}
	}
	
	// modifies name and/or description (null=do nothing)
	public void changeColumnName(int CN,String Name,String Descr)
	{
		Column c=cols.get(CN);
		if (Name!=null) c.Name=Name;
		if (Descr!=null) c.Descr=Descr;
	}
	
	// dynamically modifies the column type, correcting the existing data and reformulating; returns true if the conversion was
	// successful (for example, can't switch between molecule & other); if Force is set, then incompatible conversions will result
	// in null, otherwise the operation will fail
	public boolean changeColumnType(int CN,int NewType,boolean Force)
	{
		if (CN<0 || CN>=numCols()) return false;
		if (colType(CN)==NewType) return true;
		
		boolean incompatible=colType(CN)==COLTYPE_MOLECULE || NewType==COLTYPE_MOLECULE ||
							 colType(CN)==COLTYPE_EXTEND || NewType==COLTYPE_EXTEND;
		if (incompatible && !Force) return false;
		
		Column col=cols.get(CN);
		int prevType=col.Type;
		col.Type=NewType;
		
		for (int n=0,nrows=rows.size();n<nrows;n++)
		{
			Object[] row=rows.get(n);
			
			if (row[CN]==null) continue;
			if (incompatible) {row[CN]=null; continue;}
			
			String val="";
			if (prevType==COLTYPE_STRING) val=(String)row[CN];
			else if (prevType==COLTYPE_INTEGER) val=String.valueOf(((Integer)row[CN]).intValue());
			else if (prevType==COLTYPE_REAL) val=String.valueOf(((Double)row[CN]).intValue());
			else if (prevType==COLTYPE_BOOLEAN) val=((Boolean)row[CN]).booleanValue() ? "true" : "false";
			
			row[CN]=null;
			
			try
			{
				if (NewType==COLTYPE_STRING) row[CN]=val;
				else if (NewType==COLTYPE_INTEGER) row[CN]=new Integer(val);
				else if (NewType==COLTYPE_REAL) row[CN]=new Double(val);
				else if (NewType==COLTYPE_BOOLEAN) row[CN]=val.toLowerCase().compareTo("true")==0 ? Boolean.TRUE : Boolean.FALSE;
			}
			catch (NumberFormatException e) {} // stays null
		}
		
		return true;
	}
	
	// reorders the columns; each value of Order[n] defines the index into the original list which this should now be
	public void reorderColumns(int[] Order)
	{
		if (Order.length!=numCols()) 
			throw new IndexOutOfBoundsException("Reordering columns requires a complete parameter array.");
		boolean identity=true;
		for (int n=0;n<Order.length-1;n++) if (Order[n]!=Order[n+1]-1) {identity=false; break;}
		if (identity) return; // nothing to do
		
		ArrayList<Column> newcols=new ArrayList<Column>();
		for (int n=0;n<cols.size();n++) newcols.add(cols.get(Order[n]));
		cols=newcols;
		
		for (int n=0;n<rows.size();n++)
		{
			Object[] row=rows.get(n),newrow=new Object[row.length];
			for (int i=0;i<row.length;i++) newrow[i]=row[Order[i]];
			rows.set(n,newrow);
		}
	}
	
	// a post-import convenience feature: string columns are examined on the off chance that all of their data is in fact
	// more appropriately considered to be integer/real/boolean, and if so, changes the column type
	public void upcastStringColumns()
	{
		for (int i=0;i<numCols();i++) if (colType(i)==COLTYPE_STRING)
		{
			boolean allnull=true,allreal=true,allint=true,allbool=true;
			for (int j=0;j<numRows();j++)
			{
				if (!allreal && !allint && !allbool) break;
				if (isNull(j,i)) continue;
				
				allnull=false;
				
				String val=getString(j,i);
				if (allbool)
				{
					String lc=val.toLowerCase();
					if (!lc.equals("true") && !lc.equals("false")) allbool=false;
				}
				if (allint)
				{
					if (!val.equals("0") && !val.matches("\\-?[1-9]\\d*")) allint=false;
				}
				if (allreal)
				{
					try {Double.parseDouble(val);}
					catch (NumberFormatException e) {allreal=false;}
				}
			}
			if (allnull) {} // do nothing
			else if (allint) changeColumnType(i,COLTYPE_INTEGER,false);
			else if (allreal) changeColumnType(i,COLTYPE_REAL,false);
			else if (allbool) changeColumnType(i,COLTYPE_BOOLEAN,false);
		}
	}
}



