/*
	Sketch Elements: Chemistry molecular diagram drawing tool.
	
	(c) 2008-2009 Dr. Alex M. Clark
	
	Released as GNUware, under the Gnu Public License (GPL)
	
	See www.gnu.org for details.
*/

package net.sf.sketchel.ds;

import net.sf.sketchel.*;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
import javax.swing.*;

/*
	Molecular spreadsheet widget: a somewhat high-octane version of JTable, with molecular editing features. This class depends
	on SpreadSheetBase for the low-level features such as layout and drawing. This class focuses on editing of cells and
	user-actions.
*/

public class SpreadSheet extends SpreadSheetBase implements DataPopupMaster, ClipboardOwner
{
	protected ArrayList<StateListener> statelstn=new ArrayList<StateListener>();
	
	protected TransferDataSheet transf=null;
	protected String protID=null;
	protected int protAreaX=-1,protAreaY=-1,protAreaW=0,protAreaH=0; // protected area
	protected boolean protAllowMove=false;
	
	// editing
	protected int editCol=-1,editRow=-1,editPreW=0,editPreH=0;
	protected boolean compKeylock=false,compClaimEnter=false,compClaimEscape=false;
	protected JComponent compInPlace=null;
	protected JFrame editPopup=null;
	
	// right-button
	protected JPopupMenu rightPopup=null;
	
	// Internal class used to remember column widths;
	
	class StoreColumnWidth
	{
		String[] name;
		int[] type;
		int[] width;
	}

	// ----------------------- setup and data access -----------------------

	public SpreadSheet(DataSheetHolder ds,DataSheetCache cache)
	{
		super(ds,cache);
		
		// drag'n'drop in the spreadsheet needs features not implemented until Java 1.6
		if (Util.javaVersion()>=6)
		{
			transf=new TransferDataSheet();
			setTransferHandler(transf);
		}
	}
	
	// event notification
	public void addStateListener(StateListener slist) {statelstn.add(slist);}
	
	// display of molecules
	public RenderPolicy getRenderPolicy() {return policy;}
	public void setRenderPolicy(RenderPolicy policy) {this.policy=policy; repaint();}

	// access to underlying data content
	public DataSheetHolder getDataSheet() {return ds;}
	public DataSheetCache getCache() {return cache;}
	public void cacheUndo() {cache.cacheUndo(getCachedItem());}
	public void setDataSheet(DataSheetHolder DS,DataSheetCache Cache) 
	{
		ds=DS; 
		cache=Cache;
		
		layoutSheet();

		if (curCol>=ds.numCols()) curCol=ds.numCols()-1;
		if (curRow>=ds.numRows()) curRow=ds.numRows()-1;
		if (selCol>=ds.numCols()) selCol=ds.numCols()-1;
		if (selRow>=ds.numRows()) selRow=ds.numRows()-1;
		if (selCol+selWidth>=ds.numCols()) selWidth=ds.numCols()-selCol;
		if (selRow+selHeight>=ds.numRows()) selHeight=ds.numRows()-selRow;
	}
	public int currentColumn() {return curCol;}
	public int currentRow() {return curRow;}
	public int[] getSelectedCols()
	{
		int[] cols=new int[selWidth];
		for (int n=0;n<selWidth;n++) cols[n]=selCol+n;
		return cols;
	}
	public int[] getSelectedRows()
	{
		int[] rows=new int[selHeight];
		for (int n=0;n<selHeight;n++) rows[n]=selRow+n;
		return rows;
	}
	public int getLowerSelectedCol() {return selCol;}
	public int getUpperSelectedCol() {return selCol+selWidth-1;}
	public int getLowerSelectedRow() {return selRow;}
	public int getUpperSelectedRow() {return selRow+selHeight-1;}
	public void setSelectedCols(int curCol,int selCol,int selWidth)
	{
		this.curCol=curCol;
		this.selCol=selCol;
		this.selWidth=selWidth;
		repaint();
		scrollTo(curCol,curRow);
	}
	public void setSelectedRows(int curRow,int selRow,int selHeight)
	{
		this.curRow=curRow;
		this.selRow=selRow;
		this.selHeight=selHeight;
		repaint();
		scrollTo(curCol,curRow);
	}
	
	// translates the mouse position into a cell index (row,col) if a cell is under the position, or returns null otherwise; a
	// bit more legible than the internal method
	public int[] pickCell(int x,int y)
	{
		int[] cr=whichCell(x,y);
		if (cr[0]>=0 && cr[1]>=0 && cr[2]<0 && cr[3]<0) return new int[]{cr[0],cr[1]};
		return null;
	}
	
	// cached items: the spreadsheet makes a summary of the current state, which includes the datasheet, layout and selection;
	// note that all of the properties are copies, but the datasheet copy is a shallow one (which is appropriate for stashing
	// undo/redo data)
	public CachedItem getCachedItem()
	{
		CachedItem ci=new CachedItem();
		ci.ds=new DataSheetHolder(ds);
		ci.curCol=curCol;
		ci.curRow=curRow;
		ci.selCol=selCol;
		ci.selRow=selRow;
		ci.selWidth=selWidth;
		ci.selHeight=selHeight;
		ci.colWidth=new int[colWidth.length];
		for (int n=0;n<colWidth.length;n++) ci.colWidth[n]=colWidth[n];
		ci.rowHeight=new int[rowHeight.length];
		for (int n=0;n<rowHeight.length;n++) ci.rowHeight[n]=rowHeight[n];
		return ci;
	}
	
	// similar to setDataSheet(..), except that layout and selection information are reused; returns the duplicated datasheet
	// instance, so that the calling code has the option of pointing to the same copy
	public DataSheetHolder setCachedItem(CachedItem ci)
	{
		ds=new DataSheetHolder(ci.ds);

		curCol=ci.curCol;
		curRow=ci.curRow;
		selCol=ci.selCol;
		selRow=ci.selRow;
		selWidth=ci.selWidth;
		selHeight=ci.selHeight;
		
		colX=new int[ds.numCols()];
		colWidth=new int[ds.numCols()];
		for (int n=0;n<ds.numCols();n++) colWidth[n]=ci.colWidth[n];
		
		rowY=new int[ds.numRows()];
		rowHeight=new int[ds.numRows()];
		for (int n=0;n<ds.numRows();n++) rowHeight[n]=ci.rowHeight[n];

		recalcPositions();
		
		return ds;
	}	 
	
	// perform a uniform resizing of rows (and possibly columns): depending on the magnification, homogenize the row/column sizes
	public void sizeByMolecules(int mag)
	{
		tidyDefinitely();
	
		// want molecules to be collapsed in height so that the non-molecule fields are determining
		if (mag==0)
		{
			int h=0;
			for (int n=0;n<ds.numCols();n++)
			{
				DataUI dui=manufactureUI(ds,n,fontMetrics);
				if (ds.colType(n)==DataSheet.COLTYPE_STRING || ds.colType(n)==DataSheet.COLTYPE_INTEGER ||
					ds.colType(n)==DataSheet.COLTYPE_REAL || ds.colType(n)==DataSheet.COLTYPE_BOOLEAN)
					h=Math.max(h,dui.preferredHeight());
				else
					h=Math.max(h,dui.minimumHeight());
			}
			for (int n=0;n<ds.numRows();n++) rowHeight[n]=h;
			recalcPositions();
			scrollTo(curCol,curRow);
			return;
		}

		// want rows/columns to be adjusted so that they are set to the specified magnification factor, for molecule fields
		int h=0;
		for (int n=0;n<ds.numCols();n++)
		{
			DataUI dui=manufactureUI(ds,n,fontMetrics);
			if (ds.colType(n)==DataSheet.COLTYPE_MOLECULE)
			{
				colWidth[n]=Math.min(dui.preferredWidth()*mag,dui.maximumWidth());
				h=Math.max(h,Math.min(dui.preferredHeight()*mag,dui.maximumHeight()));
			}
			else h=Math.max(h,dui.preferredHeight());
		}
		for (int n=0;n<ds.numRows();n++) rowHeight[n]=h;
		recalcPositions();
		scrollTo(curCol,curRow);
	}

	// retrieves sizes for the columns, so they can be partially recreated later
	public StoreColumnWidth recordColumnWidth()
	{
		StoreColumnWidth store=new StoreColumnWidth();
		
		store.name=new String[ds.numCols()];
		store.type=new int[ds.numCols()];
		store.width=new int[ds.numCols()];
		for (int n=0;n<ds.numCols();n++)
		{
			store.name[n]=ds.colName(n);
			store.type[n]=ds.colType(n);
			store.width[n]=colWidth[n];
		}
		
		return store;
	}

	// the underlying datasheet has been restructured, in some unknown way... regenerate all the layout info; when appropriate,
	// use information about the previous arrangement
	public void reactRestructure() {reactRestructure(null);}
	public void reactRestructure(StoreColumnWidth store)
	{
		if (curCol>=ds.numCols()) curCol=ds.numCols()-1;
		if (curRow>=ds.numRows()) curRow=ds.numRows()-1;
		if (selCol>=ds.numCols()) selCol=ds.numCols()-1;
		if (selRow>=ds.numRows()) selRow=ds.numRows()-1;
		if (selCol+selWidth>=ds.numCols()) selWidth=ds.numCols()-selCol;
		if (selRow+selHeight>=ds.numRows()) selHeight=ds.numRows()-selRow;
	
		colX=new int[ds.numCols()];
		colWidth=new int[ds.numCols()];
		for (int n=0;n<ds.numCols();n++) colWidth[n]=0;
		
		// trace back prespecified values
		if (store!=null)
		{
			for (int i=0;i<ds.numCols();i++) for (int j=0;j<store.name.length;j++)
				if (ds.colName(i).equals(store.name[j]) && ds.colType(i)==store.type[j]) colWidth[i]=store.width[j];
		}
		for (int n=0;n<ds.numCols();n++) if (colWidth[n]==0)
		{
			DataUI dui=manufactureUI(ds,n,fontMetrics);
			colWidth[n]=dui.preferredWidth();
		}
		
		recalcPositions();
	}

	// a row has been added to the dataset, but otherwise no changes have been made
	public void reactAddRow()
	{
		int h=0;
		if (ds.numRows()>1) h=rowHeight[ds.numRows()-2]; // take height from the row before it
		else
		{
			for (int n=0;n<ds.numCols();n++)
			{
				DataUI dui=manufactureUI(ds,n,fontMetrics);
				h=Math.max(h,dui.preferredHeight());
			}
		}
		int[] newRH=new int[ds.numRows()];
		for (int n=0;n<ds.numRows()-1;n++) newRH[n]=rowHeight[n];
		newRH[ds.numRows()-1]=h;
		rowHeight=newRH;
		rowY=new int[ds.numRows()];
		recalcPositions();

		curRow=ds.numRows()-1;
		selRow=curRow;
		selHeight=1;
		scrollTo(curCol,curRow);
	}
	
	// a row has been inserted at the given position, so fixup everything else accordingly
	public void reactInsertRow(int idx)
	{
		int h=rowHeight[idx];
		int[] newRH=new int[ds.numRows()];
		for (int n=0;n<ds.numRows();n++) newRH[n]=rowHeight[n-(n>idx?1:0)];
		newRH[idx]=h;
		rowHeight=newRH;
		rowY=new int[ds.numRows()];
		recalcPositions();

		curRow=idx;
		selRow=curRow;
		selHeight=1;
		scrollTo(curCol,curRow);
	}

	// the given row indices have been removed from the datasheet, so update accordingly
	public void reactDeleteRows(int[] idx)
	{
		int[] newRH=new int[ds.numRows()];
		for (int n=0,p=0;n<rowHeight.length;n++)
		{
			boolean chop=false;
			for (int i=0;i<idx.length;i++) if (idx[i]==n) {chop=true; break;}
			if (!chop) newRH[p++]=rowHeight[n];
			else 
			{
				if (curRow==n) curRow=p-1; 
			}
		}
		rowHeight=newRH;
		rowY=new int[ds.numRows()];
		
		if (curRow<0) curRow=0;
		selRow=curRow;
		selHeight=1;
		
		recalcPositions();
		scrollTo(curCol,curRow);
	}
	
	// given that a number of cells have been inserted, restructure accordingly
	public void reactInsertRows(int[] idx)
	{
		int h=0;
		for (int n=0;n<ds.numCols();n++)
		{
			DataUI dui=manufactureUI(ds,n,fontMetrics);
			h=Math.max(h,dui.preferredHeight());
		}
		int[] newRH=new int[ds.numRows()];
		for (int n=0,pos=0;n<ds.numRows();n++) 
		{
			boolean inserted=false;
			for (int i=0;i<idx.length;i++) if (idx[i]==n) {inserted=true; break;}
			if (inserted) newRH[n]=h; else newRH[n]=rowHeight[pos++];
		}
		
		rowHeight=newRH;
		rowY=new int[ds.numRows()];
		recalcPositions();
		repaintCellRange(0,idx[0],ds.numCols()-1,ds.numRows()-1);
	}
	
	// a column has been stuck onto the end, so respond accordingly
	public void reactAppendColumn(int colNum)
	{
		DataUI dui=manufactureUI(ds,colNum,fontMetrics);
		int[] newCW=new int[ds.numCols()];
		for (int n=0;n<ds.numCols();n++) newCW[n]=n<colWidth.length ? colWidth[n] : dui.preferredWidth();
		
		colWidth=newCW;
		colX=new int[ds.numCols()];
		recalcPositions();
		repaintCellRange(ds.numCols()-1,0,ds.numCols()-1,ds.numRows()-1);
	}
	
	// notice that the cell region has been altered in content, and needs to be redisplayed
	public void reactChangeCells(int[] colIdx,int[] rowIdx)
	{
		for (int i=0;i<colIdx.length;i++) for (int j=0;j<rowIdx.length;j++) repaintCell(colIdx[i],rowIdx[j]);
	}

	// ----------------------- events -----------------------

	public void keyPressed(KeyEvent e) 
	{
		super.keyPressed(e);
	
		int key=e.getKeyCode(),mod=e.getModifiers();

		//Util.writeln("Pressed... KEY:"+key+" MOD:"+mod);
		
		if (mod==0 && key==KeyEvent.VK_LEFT) moveCursor(-1,0,false);
		else if (mod==0 && key==KeyEvent.VK_RIGHT) moveCursor(1,0,false);
		/* !! handled by hotkeys instead
		else if (mod==0 && key==KeyEvent.VK_UP) moveCursor(0,-1,false);
		else if (mod==0 && key==KeyEvent.VK_DOWN) moveCursor(0,1,false);*/
		else if (mod==KeyEvent.SHIFT_MASK && key==KeyEvent.VK_LEFT) moveCursor(-1,0,true);
		else if (mod==KeyEvent.SHIFT_MASK && key==KeyEvent.VK_RIGHT) moveCursor(1,0,true);
		else if (mod==KeyEvent.SHIFT_MASK && key==KeyEvent.VK_UP) moveCursor(0,-1,true);
		else if (mod==KeyEvent.SHIFT_MASK && key==KeyEvent.VK_DOWN) moveCursor(0,1,true);
		else if (mod==0 && key==KeyEvent.VK_TAB) moveCursor(2,0,false);
		else if (mod==KeyEvent.SHIFT_MASK && key==KeyEvent.VK_TAB) moveCursor(-2,0,false);
		else if (mod==0 && key==KeyEvent.VK_F2) startEdit((char)0,null);
		else if (mod==0 && key==KeyEvent.VK_PAGE_UP) movePageUp();
		else if (mod==0 && key==KeyEvent.VK_PAGE_DOWN) movePageDown();
		else if (mod==0 && key==KeyEvent.VK_HOME) gotoCell(0,curRow);
		else if (mod==0 && key==KeyEvent.VK_END) gotoCell(ds.numCols()-1,curRow);
		else if (mod==KeyEvent.CTRL_MASK && key==KeyEvent.VK_LEFT) gotoCell(0,curRow);
		else if (mod==KeyEvent.CTRL_MASK && key==KeyEvent.VK_RIGHT) gotoCell(ds.numCols()-1,curRow);
		else if (mod==KeyEvent.CTRL_MASK && key==KeyEvent.VK_UP) movePageUp();
		else if (mod==KeyEvent.CTRL_MASK && key==KeyEvent.VK_DOWN) movePageDown();
		else if (mod==KeyEvent.CTRL_MASK && key==KeyEvent.VK_HOME) gotoCell(0,0);
		else if (mod==KeyEvent.CTRL_MASK && key==KeyEvent.VK_END) gotoCell(ds.numCols()-1,ds.numRows()-1);
	}

	public void keyTyped(KeyEvent e)
	{
		super.keyTyped(e);
	
		int key=e.getKeyCode(),mod=e.getModifiers();
		char ch=e.getKeyChar();
		boolean shift=(e.getModifiers()&MouseEvent.SHIFT_MASK)>0;
		boolean ctrl=(e.getModifiers()&MouseEvent.CTRL_MASK)>0;
		boolean alt=(e.getModifiers()&MouseEvent.ALT_MASK)>0;

		if (compKeylock && compInPlace!=null && !ctrl && !alt) return;

		//Util.writeln("Typed... KEY:"+key+" MOD:"+mod+" CHAR:"+(int)ch);

		if (curCol<0 || curCol>=ds.numCols()) return;
		DataUI dui=manufactureUI(ds,curCol,fontMetrics);
		if (dui.editType()==DataUI.EDIT_INPLACE)
		{
			if (!ctrl && !alt)
				if (ch==8 || ch==127 || ch==32 || ch=='.' ||
				   (ch>='A' && ch<='Z') || (ch>='a' && ch<='z') || (ch>='0' && ch<='9'))
					startEdit(ch,dui);
		}
		else
		{
			if (ch==32) startEdit((char)0,dui);
		}
	}

	public void focusGained(FocusEvent e) 
	{
		super.focusGained(e);
		if (e.getSource()==this) 
		{
			//if (compInPlace!=null) acceptEdit();
			if (compInPlace!=null) compInPlace.grabFocus();
		}
	}
	public void focusLost(FocusEvent e)
	{
		super.focusLost(e);
		if (e.getSource()==compInPlace) tidyMaybe();
	}

	public void mouseWheelMoved(MouseWheelEvent e)
	{
		if (compInPlace!=null) return;
		super.mouseWheelMoved(e);
	}
	
	// ----------------------- action responses -----------------------

	// performs the undo action, if possible
	public void actionUndo()
	{
		if (!cache.canUndo()) return;

		rejectEdit();

		CachedItem ci=cache.performUndo(getCachedItem());
		setCachedItem(ci);

		notifyChanged();
	}
	
	// performs the redo action, if possible
	public void actionRedo()
	{
		if (!cache.canRedo()) return;

		rejectEdit();
		
		CachedItem ci=cache.performRedo(getCachedItem());
		setCachedItem(ci);

		notifyChanged();
	}
	
	// adds a single row to the end
	public void actionAddRow()
	{
		tidyDefinitely();
	
		cacheUndo();
		ds.appendRow();
		reactAddRow();
		ds.setDirty();
		
		notifyChanged();
	}

	// inserts a single row before the current one
	public void actionInsertRow()
	{
		if (ds.numRows()<1 || curRow<0) {actionAddRow(); return;}
		
		tidyDefinitely();
	
		cacheUndo();
		ds.insertRow(curRow);
		reactInsertRow(curRow);
		ds.setDirty();
		
		notifyChanged();
	}

	// deletes all of the selected rows
	public void actionDeleteRows()
	{
		tidyDefinitely();
		
		int[] idx=getSelectedRows();
		if (idx.length==0) return;
		cacheUndo();
		for (int i=idx.length-1;i>=0;i--)
		{
			ds.deleteRow(idx[i]);
			for (int j=i+1;j<idx.length;j++) if (idx[j]>idx[i]) idx[j]--;
		}
		reactDeleteRows(getSelectedRows());
		ds.setDirty();
		
		notifyChanged();
	}
	
	// move selected rows up one
	public void actionMoveRowsUp()
	{
		tidyDefinitely();
		
		int[] idx=getSelectedRows();
		if (idx.length==0) return;
		if (idx[0]==0) return; // top one is already at the top
		cacheUndo();
		for (int n=0;n<idx.length;n++) ds.moveRowUp(idx[n]);
		setSelectedRows(currentRow()-1,idx[0]-1,idx[idx.length-1]-idx[0]+1);
		ds.setDirty();
		
		notifyChanged();
	}
	
	// move selected rows down one
	public void actionMoveRowsDown()
	{
		tidyDefinitely();
		
		int[] idx=getSelectedRows();
		if (idx.length==0) return;
		if (idx[idx.length-1]==ds.numRows()-1) return; // bottom one is already at the end
		cacheUndo();
		for (int n=idx.length-1;n>=0;n--) ds.moveRowDown(idx[n]);
		setSelectedRows(currentRow()+1,idx[0]+1,idx[idx.length-1]-idx[0]+1);
		ds.setDirty();
		
		notifyChanged();
	}
	
	public void actionMoveColumn(int col,int dir)
	{
		if (col+dir<0 || col+dir>=ds.numCols()) return;
	
		tidyDefinitely();
		StoreColumnWidth store=recordColumnWidth();
		cacheUndo();
		
		int[] order=new int[ds.numCols()];
		for (int n=0;n<ds.numCols();n++) order[n]=n;
		int i=order[col];
		order[col]=order[col+dir];
		order[col+dir]=i;
		ds.reorderColumns(order);
		
		curCol=col+dir;
		selCol=curCol;
		selWidth=1;
		
		ds.setDirty();
		notifyChanged();
		reactRestructure(store);
	}
	public void actionDeleteColumn(int col)
	{
		if (col<0 || col>=ds.numCols()) return;
		if (JOptionPane.showConfirmDialog(null,
			"Delete column #"+(col+1)+"?","Confirm",
			JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION) return;
		
		tidyDefinitely();
		StoreColumnWidth store=recordColumnWidth();
		cacheUndo();
	
		ds.deleteColumn(col);
		
		curCol=Math.min(ds.numCols()-1,col);
		selCol=curCol;
		selWidth=1;
		
		ds.setDirty();
		notifyChanged();
		reactRestructure(store);
	}
	public void actionInsertRow(int row,int dir)
	{
		if (row<0 || row>=ds.numRows()) return;
		
		tidyDefinitely();
		
		cacheUndo();
		if (dir==-1) 
		{
			ds.insertRow(row);
			reactInsertRow(row);
		}
		else if (row<ds.numRows()-1) 
		{
			row++; 
			ds.insertRow(row);
			reactInsertRow(row);
		}
		else 
		{
			ds.appendRow();
			reactAddRow();
		}
		
		ds.setDirty();
		notifyChanged();
	}
	public void actionMoveRow(int row,int dir)
	{
		if (row+dir<0 || row+dir>=ds.numRows()) return;
		
		tidyDefinitely();
		cacheUndo();

		if (dir<0) ds.moveRowUp(row);
		else ds.moveRowDown(row);
		
		curRow=row+dir;
		selRow=curRow;
		selHeight=1;

		ds.setDirty();
		notifyChanged();
		repaint();
	}
	public void actionDeleteRow(int row)
	{
		if (row<0 || row>=ds.numRows()) return;
		
		tidyDefinitely();
		cacheUndo();
		ds.deleteRow(row);
		
		reactDeleteRows(new int[]{row});
		ds.setDirty();
		notifyChanged();
	}
	
	// ----------------------- general use -----------------------
	
	// copies the current selection onto the clipboard
	public void copySelection()
	{
		int[] rn=getSelectedRows(),cn=getSelectedCols();
		if (rn.length==1 && cn.length==1) 
			copySingleCell(rn[0],cn[0]);
		else
			copyMultipleCells(rn,cn);
	}
	
	private void copySingleCell(int RN,int CN)
	{
		String str=null;
		
		if (ds.isNull(RN,CN)) {}
		else if (ds.colType(CN)==DataSheet.COLTYPE_MOLECULE)
		{
			Clipboard clip=Toolkit.getDefaultToolkit().getSystemClipboard();
			clip.setContents(new ClipboardMolecule(ds.getMolecule(RN,CN),null),null);
		}
		else if (ds.colType(CN)==DataSheet.COLTYPE_STRING) str=ds.getString(RN,CN);
		else if (ds.colType(CN)==DataSheet.COLTYPE_INTEGER) str=String.valueOf(ds.getInteger(RN,CN));
		else if (ds.colType(CN)==DataSheet.COLTYPE_REAL) str=String.valueOf(ds.getReal(RN,CN));
		else if (ds.colType(CN)==DataSheet.COLTYPE_BOOLEAN) str=ds.getBoolean(RN,CN) ? "true" : "false";

		if (str!=null)
		{
			Clipboard clip=Toolkit.getDefaultToolkit().getSystemClipboard();
			clip.setContents(new StringSelection(str),this);
		}
	}
	
	private void copyMultipleCells(int[] RN,int[] CN)
	{
		DataSheetHolder copy=new DataSheetHolder();
		for (int n=0;n<CN.length;n++) 
		{
			copy.appendColumn(ds.colName(CN[n]),ds.colType(CN[n]),ds.colDescr(CN[n]));
		}
		for (int i=0;i<RN.length;i++)
		{
			copy.appendRow();
			for (int j=0;j<CN.length;j++) copy.setObject(i,j,ds.getObject(RN[i],CN[j]));
		}
		
		Clipboard clip=Toolkit.getDefaultToolkit().getSystemClipboard();
		clip.setContents(new ClipboardDataSheet(copy,policy),null);
	}

	// copies the selection onto the clipboard, and nukes the underlying data
	public void cutSelection()
	{
		tidyDefinitely();
		
		copySelection();
		int[] rn=getSelectedRows(),cn=getSelectedCols();
		cacheUndo();
		if (cn.length==ds.numCols())
		{
			for (int n=rn.length-1;n>=0;n--) ds.deleteRow(rn[n]);
			reactDeleteRows(rn);
		}
		else
		{
			for (int r=0;r<rn.length;r++) for (int c=0;c<cn.length;c++) ds.setToNull(rn[r],cn[c]);
			repaint();
		}
		
		ds.setDirty();
		notifyChanged();
	}
	
	// pastes the contents of the clipboard into the datasheet
	public void pasteSelection()
	{
		tidyDefinitely();
		StoreColumnWidth store=recordColumnWidth();
				
		// setup the handler class, ready to receive the pasting
		ImportTable imp=new ImportTable(ds);
		imp.setSelection(curRow,curCol,getSelectedRows(),getSelectedCols());
		
		try
		{
			DataSheetHolder clipsheet=ClipboardDataSheet.extract();
			if (clipsheet!=null) imp.importData(clipsheet);
		}
		catch (DataSheetIOException ex) 
		{
			JOptionPane.showMessageDialog(null,ex.toString(),"Import Failed",JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		cacheUndo();
		ds=imp.getResult();
		ds.setDirty();
		notifyChanged();
		
		reactRestructure(store);

		// !! affected rows, what to do exactly...?
	}
	
	// inserts a datasheet at the given top-left corner position; same effect as pasting when one cell is selected
	public void pasteAtLocation(DataSheet paste,int cn,int rn)
	{
		cacheUndo();
		StoreColumnWidth store=recordColumnWidth();
		
		ImportTable imp=new ImportTable(ds);
		try
		{
			int[] rows=new int[paste.numRows()],cols=new int[paste.numCols()];
			for (int n=0;n<rows.length;n++) rows[n]=rn+n;
			for (int n=0;n<cols.length;n++) cols[n]=cn+n;
			imp.setSelection(rn,cn,rows,cols);
			imp.setStrictColumn(true);
			imp.importData(paste);
		}
		catch (DataSheetIOException ex) 
		{
			ex.printStackTrace();
			return;
		}
		
		ds=imp.getResult();
		ds.setDirty();
		notifyChanged();
		reactRestructure(store);
		
		// note that the pasted-into area is temporarily protected from the drag-move deletion
		protAreaX=cn;
		protAreaY=rn;
		protAreaW=paste.numCols();
		protAreaH=paste.numRows();
	}
	
	// called after a drag-move; scrubs out the indicated area, except for the protected parts
	public void deleteDragArea(int[] cn,int[] rn)
	{
		if (!protAllowMove) return;
	
		cacheUndo();
		
		for (int i=0;i<cn.length;i++) for (int j=0;j<rn.length;j++)
		{
			if (cn[i]>=protAreaX && cn[i]<protAreaX+protAreaW && rn[j]>=protAreaY && rn[j]<protAreaY+protAreaH) continue;
			ds.setToNull(rn[j],cn[i]);
			ds.setDirty();
		}
		
		notifyChanged();
		
		repaint();
		
		protAllowMove=false;
	}
	
	// removes the protected area, which is used for drag'n'drop move operations
	public void clearProtectedArea()
	{
		protAreaX=-1;
		protAreaY=-1;
		protAreaW=0;
		protAreaH=0;
		
		protAllowMove=false;
	}
	
	// returns the "protected ID" code, which is used to figure out when drag'n'drop is accessing the same spreadsheet
	public String getProtectedID() {return protID;}
	public void clearProtectedID() {protID=null;}
	
	// access to the "allow move" operation: this allows drag'n'drop to signal whether a drag-move operation is occurring on
	// the same datasheet, and hence it is permitted to clear out the source data
	public boolean getAllowMove() {return protAllowMove;}
	public void setAllowMove() {protAllowMove=true;}

	// nukes the currently selected data, turning it all into null
	public void clearSelection()
	{
		tidyDefinitely();
		
		cacheUndo();
		
		int[] cn=getSelectedCols(),rn=getSelectedRows();
		for (int i=0;i<cn.length;i++) for (int j=0;j<rn.length;j++) ds.setToNull(rn[j],cn[i]);
		reactChangeCells(cn,rn);
		
		ds.setDirty();
		notifyChanged();
	}

	// modifies the column structure, in a vaguely graceful way
	public void modifyColumns(int[] OldPos,int[] NewPos,String[] Name,int[] Type,String[] Descr)
	{
		tidyDefinitely();
		StoreColumnWidth store=recordColumnWidth();
		
		int sz=OldPos.length;
		
		cacheUndo();

		// delete those which need to be chopped out
		for (int n=0;n<sz;n++) if (NewPos[n]<0)
		{
			ds.deleteColumn(OldPos[n]);
			for (int i=0;i<sz;i++) if (OldPos[i]>OldPos[n]) OldPos[i]--;
			for (int i=n;i<sz-1;i++) 
			{
				OldPos[i]=OldPos[i+1];
				NewPos[i]=NewPos[i+1];
				Name[i]=Name[i+1];
				Type[i]=Type[i+1];
				Descr[i]=Descr[i+1];
			}
			n--;
			sz--;
		}
		
		// add the new ones
		for (int n=0;n<sz;n++) if (OldPos[n]<0)
		{
			OldPos[n]=ds.appendColumn(Name[n],Type[n],Descr[n]);
		}
		
		// modify any existing content
		for (int n=0;n<sz;n++)
		{
			ds.changeColumnName(OldPos[n],Name[n],Descr[n]);
			ds.changeColumnType(OldPos[n],Type[n],true);
		}
		
		// now redefine the column order
		int[] reord=new int[sz];
		for (int n=0;n<sz;n++) reord[NewPos[n]]=OldPos[n];
		ds.reorderColumns(reord);
		
		ds.setDirty();
		reactRestructure(store);

		notifyChanged();
	}

	// tries to start an edit for the current cell, whether that be in-place or popup; the DataUI parameter is for convenience,
	// null means recreate
	public void startEdit() {startEdit((char)0,null);}
	public void startEdit(char ch,DataUI dui)
	{
		super.startEdit(ch,dui);
	
		acceptEdit();
		
		if (dui==null) dui=manufactureUI(ds,curCol,fontMetrics);
		
		if (dui.editType()==DataUI.EDIT_INPLACE)
		{
			JComponent edcomp=dui.beginEdit(curRow,ch);
			if (edcomp==null) 
			{
				repaintCell(curCol,curRow); // just in case the contents were updated, in lieu of returning an editor
				return;
			}
			
			blockScrollEvents=true;
			vscroll.setEnabled(false);
			hscroll.setEnabled(false);
			
			editCol=curCol;
			editRow=curRow;
			compKeylock=dui.claimKeyboard();
			
			expandCellSize(dui.expansionWidth(),dui.expansionHeight());

			add(compInPlace=edcomp);
			compInPlace.setBackground(colLight /*colSelected*/);
			compInPlace.setBounds(colX[curCol]-offsetX,rowY[curRow]-offsetY,colWidth[curCol],rowHeight[curRow]);
			compInPlace.setVisible(true);
			compInPlace.grabFocus();
			compInPlace.addFocusListener(this);
			
			blockScrollEvents=false;
			
			StateListener.EditInfo info=new StateListener.EditInfo();
			info.dui=dui;
			for (int n=0;n<statelstn.size();n++) statelstn.get(n).notifyEditStart(info);
		}
		else if (dui.editType()==DataUI.EDIT_POPUP)
		{
			editPopup=dui.beginPopup(curRow);
			if (editPopup==null) return;
			
			editCol=curCol;
			editRow=curRow;
			
			StateListener.EditInfo info=new StateListener.EditInfo();
			info.dui=dui;
			for (int n=0;n<statelstn.size();n++) statelstn.get(n).notifyEditStart(info);
		}
		else if (dui.editType()==DataUI.EDIT_DIRECT)
		{
			cacheUndo();
			boolean changed=dui.directEdit(curRow);
			if (changed) 
			{
				ds.setDirty(); 
				notifyChanged();
				repaintCell(curCol,curRow);
			} // !! else undo the undo (ick)
		}
	}

	// if an edit is currently in progress, apply any change that it may have made, the shut it down
	public void acceptEdit()
	{
		if (compInPlace!=null) 
		{
			DataUI dui=manufactureUI(ds,editCol,fontMetrics);
			cacheUndo();
			if (dui.saveEdit(editRow,compInPlace)) 
			{
				ds.setDirty();
				notifyChanged();
			} // !! else undo the undo (ick)

			dui.endEdit(compInPlace);
			compInPlace.setVisible(false);
			remove(compInPlace);
			compInPlace=null;
			
			StateListener.EditInfo info=new StateListener.EditInfo();
			info.dui=dui;
			for (int n=0;n<statelstn.size();n++) statelstn.get(n).notifyEditStop(info);
			repaint();
		}
		if (editPopup!=null)
		{
			DataUI dui=manufactureUI(ds,editCol,fontMetrics);
			cacheUndo();
			if (dui.savePopup(editRow,editPopup))
			{
				ds.setDirty();
				notifyChanged();
				repaint();
			} // !! else undo the undo (ick)
			
			editPopup.dispose();
			editPopup=null;
			
			StateListener.EditInfo info=new StateListener.EditInfo();
			info.dui=dui;
			for (int n=0;n<statelstn.size();n++) statelstn.get(n).notifyEditStop(info);
		}
		contractCellSize();
		editCol=editRow=-1;
		grabFocus();
		vscroll.setEnabled(true);
		hscroll.setEnabled(true);
	}
	
	// if an edit is currently in progress, shut it down without applying any changes
	public void rejectEdit()
	{
		if (compInPlace!=null) 
		{
			DataUI dui=manufactureUI(ds,editCol,fontMetrics);
			dui.endEdit(compInPlace);
			
			remove(compInPlace);
			compInPlace.setVisible(false);
			compInPlace=null;
			
			StateListener.EditInfo info=new StateListener.EditInfo();
			info.dui=manufactureUI(ds,editCol,fontMetrics);
			for (int n=0;n<statelstn.size();n++) statelstn.get(n).notifyEditStop(info);
			repaint();
		}
		if (editPopup!=null)
		{
			editPopup.dispose();
			editPopup=null;
			
			StateListener.EditInfo info=new StateListener.EditInfo();
			info.dui=manufactureUI(ds,editCol,fontMetrics);
			for (int n=0;n<statelstn.size();n++) statelstn.get(n).notifyEditStop(info);
		}
		contractCellSize();
		editCol=editRow=-1;
		grabFocus();
		vscroll.setEnabled(true);
		hscroll.setEnabled(true);
	}
	
	// returns true if there is an editing component which needs to hog more keystrokes than usual
	protected boolean keyboardLocked()
	{
		return compKeylock && compInPlace!=null;
	}
	
	// given an "expansion size" for the about-to-be-edited cell, ensure that the cell becomes at least this big; if no expansion,
	// or the cell was big enough already, do nothing; otherwise, make it bigger, ensure it's visible, and record the original
	// size for subsequent contraction
	private void expandCellSize(int cw,int ch)
	{
		editPreW=editPreH=0;
		if (cw==0 && ch==0) return;
		cw=Math.min(cw,visWidth-titleWidth);
		ch=Math.min(ch,visHeight-titleHeight);
		if (cw<=colWidth[editCol] && ch<=rowHeight[editRow]) return;
		
		editPreW=colWidth[editCol];
		editPreH=rowHeight[editRow];
		
		colWidth[editCol]=Math.max(colWidth[editCol],cw);
		rowHeight[editRow]=Math.max(rowHeight[editRow],ch);		
		recalcPositions();
		scrollTo(editCol,editRow);
		repaint();
	}
	
	// if an expansion was called for in the previous edit, then restore to original size
	protected void contractCellSize()
	{
		if (editPreW==0 && editPreH==0) return;
		
		colWidth[editCol]=editPreW;
		rowHeight[editRow]=editPreH;
		recalcPositions();
	
		editPreW=editPreH=0;
	}
	
	// the scroller has moved, so any editing component has to move too
	protected void repositionEdit()
	{
		if (compInPlace==null) return;
		compInPlace.setBounds(colX[editCol]-offsetX,rowY[editRow]-offsetY,colWidth[editCol],rowHeight[editRow]);
	}
	
	// informs that some change was made to the display state, which could possibly be grounds for dismissing an edit-in-place
	protected void tidyMaybe()
	{
		if (compInPlace==null) return;
		
		// if current row has changed, this is a good reason
		if (curCol!=editCol || curRow!=editRow) {tidyDefinitely(); return;}
		
		// if the editing cell has gone out of the visible area, this will do
		if (colX[editCol]-offsetX<titleWidth || colX[editCol]+colWidth[editCol]-offsetX>visWidth ||
			rowY[editRow]-offsetY<titleHeight || rowY[editRow]+rowHeight[editRow]-offsetY>visHeight)
			{tidyDefinitely(); return;}
	}
	
	// informs that the state changed, and it is definitely necessary to get rid of any editing
	protected void tidyDefinitely()
	{
		if (compInPlace==null && editPopup==null) return;
		acceptEdit();
	}
	
	// (implemented from DataPopupMaster)
	public void popupNotifySave(DataUI dui,int rowNum)
	{
		ds.setDirty();
		cacheUndo();
		notifyChanged();
		repaint();
	}
	
	// (implemented from DataPopupMaster)
	public void popupNotifyClosed(DataUI dui,int rowNum)
	{
		editPopup=null;
	}
	
	// (implemented from DataPopupMaster)
	public void requestClose(DataUI dui,boolean apply)
	{
		if (apply) acceptEdit(); else rejectEdit();
	}

	protected void notifyChanged()
	{
		for (int n=0;n<statelstn.size();n++)
		{
			statelstn.get(n).replaceTitle();
			statelstn.get(n).dataModified();
		}
	}
	
	// called when the right mouse button is invoked; when e==null, this is a button-up
	protected void handleRightButton(MouseEvent e)
	{
		if (e==null)
		{
			if (rightPopup!=null) {rightPopup.setVisible(false); rightPopup=null;}
			return;
		}
		
		if (rightPopup!=null) 
		{
			rightPopup.setVisible(false); 
			rightPopup=null;
		}
			
		rightPopup=new JPopupMenu();
		int[] cr=whichCell(e.getX(),e.getY());
		
		if (cr[0]>=0 && cr[1]<0) 
		{
			for (int n=0;n<statelstn.size();n++) statelstn.get(n).populateRightMouseColumn(rightPopup,cr[0]);
		
		}
		else if (cr[1]>=0 && cr[0]<0)
		{
			for (int n=0;n<statelstn.size();n++) statelstn.get(n).populateRightMouseRow(rightPopup,cr[1]);
		}
		else if (cr[0]>=0 && cr[1]>=0) 
		{
			if (cr[0]<selCol || cr[0]>=selCol+selWidth || cr[1]<selRow || cr[1]>=selRow+selHeight)
			{
				curCol=selCol=cr[0];
				curRow=selRow=cr[1];
				selWidth=1;
				selHeight=1;
				repaint();
			}
			for (int n=0;n<statelstn.size();n++) statelstn.get(n).populateRightMouseCell(rightPopup,cr[0],cr[1]);
		}
		
		if (rightPopup.getComponentCount()==0) rightPopup=null;
		else rightPopup.show(this,e.getX()-3,e.getY()-3);
	}
	
	
	// creates a data user interface editing element for a cell, the type of which depends on the column type
	protected DataUI manufactureUI(DataSheetHolder ds,int colNum,FontMetrics fm)
	{
		int type=ds.colType(colNum);
		if (type==DataSheet.COLTYPE_MOLECULE) return new DataUIMolecule(ds,colNum,fm,this,policy);
		if (type==DataSheet.COLTYPE_STRING) return new DataUIString(ds,colNum,fm,this);
		if (type==DataSheet.COLTYPE_INTEGER || type==DataSheet.COLTYPE_REAL) return new DataUINumber(ds,colNum,fm,this);
		if (type==DataSheet.COLTYPE_BOOLEAN) return new DataUIBoolean(ds,colNum,fm,this);
		
		// the default is the viewer for unknown types (such as COLTYPE_EXTEND)
		return new DataUIUnknown(ds,colNum,fm,this);
	}
			
	protected void initiateDrag(MouseEvent e,int action,DataSheetHolder copy,int[] rn,int[] cn)
	{
		if (transf==null) return;
		protID=UUID.randomUUID().toString();
		protAllowMove=false;
		
		// encode information about the source
		copy.setTitle(protID);
		copy.setDescription(cn[0]+","+rn[0]+","+copy.numCols()+","+copy.numRows());
		
		transf.exportAsDrag(this,e,action,copy,rn,cn,policy);
	}
}

