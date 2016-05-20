/*
	Sketch Elements: Chemistry molecular diagram drawing tool.
	
	(c) 2009 Dr. Alex M. Clark
	
	Released as GNUware, under the Gnu Public License (GPL)
	
	See www.gnu.org for details.
*/

package net.sf.sketchel.ds;

import net.sf.sketchel.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
import java.awt.geom.*;
import javax.swing.*;

/*
	Molecular spreadsheet widget: the base class, which handles the low level functionality, like layout and drawing.
*/

public class SpreadSheetBase extends JPanel 
	implements KeyListener, ComponentListener, AdjustmentListener, FocusListener,
			   MouseListener, MouseMotionListener, MouseWheelListener, ActionListener
{
	protected DataSheetHolder ds;
	protected DataSheetCache cache;
	
	protected RenderPolicy policy=new RenderPolicy(); // how molecules are displayed

	protected Font font,italFont;
	protected FontMetrics fontMetrics,italFontMetrics;
	
	// pleasant defaults; to be overridden with platform settings
	protected Color colBackgr=getBackground();
	protected Color colBorder=Color.BLACK;
	protected Color colHardBorder=Color.BLACK;
	protected Color colMain=Color.BLACK;
	protected Color colDark=new Color(168,168,168);
	protected Color colMedium=new Color(192,192,192);
	protected Color colLight=new Color(224,224,224);
	protected Color colUnselected=new Color(255,255,255);
	protected Color colSelected=new Color(128,192,224);

	// cell layout; widths and heights of title & cells do not include the border, but the position & total size does
	protected final int BORDER_SIZE=1;
	protected int titleWidth=0,titleHeight=0;
	protected int[] colWidth=null,rowHeight=null;
	protected int[] colX=null,rowY=null;
	protected int totalWidth=0,totalHeight=0;
	
	// scrolling, and offsets
	protected int visWidth=0,visHeight=0;
	protected int offsetX=0,offsetY=0;
	protected boolean blockScrollEvents=false;
	protected JScrollBar vscroll,hscroll;
	
	// current selection state
	protected int curCol=0,curRow=0;
	protected int selCol=0,selRow=0,selWidth=1,selHeight=1;
	
	// pertaining to dragging of things
	protected final int DRAGTYPE_NONE=0;
	protected final int DRAGTYPE_SELECT=1;
	protected final int DRAGTYPE_COL_RESIZE=2;
	protected final int DRAGTYPE_ROW_RESIZE=3;
	protected final int DRAGTYPE_MOVE_DROP=4;
	protected final int DRAGTYPE_COPY_DROP=5;
	protected int dragType=DRAGTYPE_NONE;
	protected int dragX,dragY,dragCol,dragRow;
	protected final int DRAGTAB_WIDTH=15,DRAGTAB_HEIGHT=15;
	protected boolean showDragTab=false;
		
	// ----------------------- hotkey stuff -----------------------
	
	private final String HOTKEY_UPARROW="UpArrow";
	private final String HOTKEY_DOWNARROW="DownArrow";
	
	class HotKeyAction extends AbstractAction
	{
		String hotkey;
		ActionListener listen;
		HotKeyAction(String hotkey,ActionListener listen) {this.hotkey=hotkey; this.listen=listen;}
		
		public void actionPerformed(ActionEvent e) 
		{
			listen.actionPerformed(new ActionEvent(hotkey,0,hotkey));
		}
	}
	
	// ----------------------- constructor -----------------------
	
	public SpreadSheetBase(DataSheetHolder ds,DataSheetCache cache)
	{
		this.ds=ds;
		this.cache=cache;
	
		setLayout(null);
		
		UIDefaults defaults=UIManager.getDefaults();
		colBorder=(Color)defaults.get("Table.gridColor");
		colMain=(Color)defaults.get("Table.foreground");
		colMedium=((Color)defaults.get("Table.background")).darker();
		//colDark=colMedium.darker();
		//colLight=colMedium.brighter();
		colDark=Util.tintCol(colMedium,-48,-48,-48);
		colLight=Util.tintCol(colMedium,48,48,48);
		colUnselected=(Color)defaults.get("Table.background");
		colSelected=(Color)defaults.get("Table.selectionBackground");
		
		if (colBorder==null) colBorder=Util.mergeCols(colMain,colDark);
		if (colSelected==null) colSelected=Util.tintCol(colUnselected,-24,0,48);
		
		font=getFont();
		italFont=new Font(font.getName(),Font.ITALIC,font.getSize());
		fontMetrics=getFontMetrics(font);
		italFontMetrics=getFontMetrics(italFont);

		add(hscroll=new JScrollBar(JScrollBar.HORIZONTAL));
		add(vscroll=new JScrollBar(JScrollBar.VERTICAL));
		hscroll.setVisible(false);
		vscroll.setVisible(false);
		hscroll.setUnitIncrement(1);
		vscroll.setUnitIncrement(1);
		
		addKeyListener(this);
		addComponentListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
		addFocusListener(this);

		ActionMap am=getActionMap();
		InputMap im=getInputMap(WHEN_IN_FOCUSED_WINDOW);

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP,0),HOTKEY_UPARROW);
		am.put(HOTKEY_UPARROW,new HotKeyAction(HOTKEY_UPARROW,this));
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN,0),HOTKEY_DOWNARROW);
		am.put(HOTKEY_DOWNARROW,new HotKeyAction(HOTKEY_DOWNARROW,this));
		
		hscroll.addAdjustmentListener(this);
		vscroll.addAdjustmentListener(this);
		
		setFocusable(true);
		setFocusTraversalKeysEnabled(false);

		layoutSheet();
	}
	
	// ----------------------- general -----------------------
	
	// changes the current cell, and trashes the selected region, setting it to only the current cell
	protected void setCurrentCell(int colNum,int rowNum)
	{
		repaintCellRange(selCol,selRow,selCol+selWidth-1,selRow+selHeight-1);
		curCol=colNum;
		curRow=rowNum;
		selCol=curCol;
		selRow=curRow;
		selWidth=1;
		selHeight=1;
		tidyMaybe();
		updateDragTab(null);
		repaintCell(curCol,curRow);
	}
	
	// extends the selection so that it includes the indicated cell
	protected void extendCurrentSelection(int colNum,int rowNum)
	{
		if (colNum<selCol) {selWidth+=selCol-colNum; selCol=colNum;}
		else if (colNum>=selCol+selWidth) selWidth=colNum-selCol+1;
		
		if (rowNum<selRow) {selHeight+=selRow-rowNum; selRow=rowNum;}
		else if (rowNum>=selRow+selHeight) selHeight=rowNum-selRow+1;
	
		curCol=colNum;
		curRow=rowNum;
		
		repaintCellRange(selCol,selRow,selCol+selWidth-1,selRow+selHeight-1);
	}

	// does as much scrolling as is necessary to ensure that the indicated cell is visible
	protected void scrollTo(int colNum,int rowNum)
	{
		if (colNum<0 || colNum>=ds.numCols() || rowNum<0 || rowNum>=ds.numRows() || ds.numRows()==0) return;
	
		if (colX[colNum]-offsetX-1<titleWidth) 
		{
			int newX=colX[colNum]-colX[0];
			if (newX!=offsetX) {offsetX=newX; hscroll.setValue(offsetX); repaint();}
		}
		else if (colX[colNum]+colWidth[colNum]-offsetX>visWidth)
		{
			int newX=Math.max(0,colX[colNum]+colWidth[colNum]-visWidth);
			if (newX!=offsetX) {offsetX=newX; hscroll.setValue(offsetX); repaint();}
		}

		if (rowY[rowNum]-offsetY-1<titleHeight) 
		{
			int newY=rowY[rowNum]-rowY[0];
			if (newY!=offsetY) {offsetY=newY; vscroll.setValue(offsetY); repaint();}
		}
		else if (rowY[rowNum]+rowHeight[rowNum]-offsetY>visHeight)
		{
			int newY=Math.max(0,rowY[rowNum]+rowHeight[rowNum]-visHeight);
			if (newY!=offsetY) {offsetY=newY; vscroll.setValue(offsetY); repaint();}
		}
	}	 
	
	// ----------------------- layout methods -----------------------
	
	// redetermine the row/column layout from scratch; try not to use this unless absolutely necessary
	protected void layoutSheet()
	{
		titleWidth=recommendedTitleWidth()+2*BORDER_SIZE;
		titleHeight=recommendedTitleHeight()+2*BORDER_SIZE;

		colX=new int[ds.numCols()];
		rowY=new int[ds.numRows()];
		colWidth=new int[ds.numCols()];
		rowHeight=new int[ds.numRows()];
		
		totalWidth=titleWidth;
		totalHeight=titleHeight;

		int h=10;
		for (int n=0;n<ds.numCols();n++)
		{
			DataUI dui=manufactureUI(ds,n,fontMetrics);
			colWidth[n]=dui.preferredWidth();
			colX[n]=totalWidth;
			totalWidth+=colWidth[n]+BORDER_SIZE;
			h=Math.max(h,dui.preferredHeight());
		}
		
		for (int n=0;n<ds.numRows();n++)
		{
			rowY[n]=totalHeight;
			rowHeight[n]=h;
			totalHeight+=rowHeight[n]+BORDER_SIZE;
		}
		
		adaptScrollers();
		
		updateDragTab(null);
		repaint();
	}

	// given that heights/widths may have changed, redetermine the positions and repaint
	protected void recalcPositions()
	{
		totalWidth=titleWidth;
		//int h=10;
		for (int n=0;n<ds.numCols();n++)
		{
			DataUI dui=manufactureUI(ds,n,fontMetrics);
			colX[n]=totalWidth;
			totalWidth+=colWidth[n]+BORDER_SIZE;
			//h=Math.max(h,dui.preferredHeight());
		}
		
		if (rowY.length!=ds.numRows())
		{
			int maxh=0;
			for (int n=0;n<ds.numCols();n++) maxh=Math.max(manufactureUI(ds,n,fontMetrics).preferredHeight(),maxh);
			rowY=new int[ds.numRows()];
			rowHeight=new int[ds.numRows()];
			for (int n=0;n<ds.numRows();n++) rowHeight[n]=maxh;
		}
		
		totalHeight=titleHeight;
		for (int n=0;n<ds.numRows();n++)
		{
			rowY[n]=totalHeight;
			totalHeight+=rowHeight[n]+BORDER_SIZE;
		}
		
		adaptScrollers();
		
		updateDragTab(null);
		repaint();
	}

	// layout suggestions
	protected int recommendedTitleWidth() {return fontMetrics.stringWidth(String.valueOf(ds.numRows())+4);}
	protected int recommendedTitleHeight() {return fontMetrics.getHeight()+4;}

	// convenience ways to repaint only certain cells
	protected void repaintCell(int colNum,int rowNum)
	{
		repaint(new Rectangle(colX[colNum]-1-offsetX,rowY[rowNum]-1-offsetY,colWidth[colNum]+2,rowHeight[rowNum]+2));
	}
	protected void repaintCellRange(int col1,int row1,int col2,int row2)
	{
		if (ds.numCols()==0 || ds.numRows()==0) return;
		col1=Math.min(Math.max(col1,0),ds.numCols()-1);
		col2=Math.min(Math.max(col2,0),ds.numCols()-1);
		row1=Math.min(Math.max(row1,0),ds.numRows()-1);
		row2=Math.min(Math.max(row2,0),ds.numRows()-1);
		
		int x=colX[col1],y=rowY[row1];
		int w=colX[col2]+colWidth[col2]-x,h=rowY[row2]+rowHeight[row2]-y;
		x-=offsetX; y-=offsetY;
		repaint(new Rectangle(x-1,y-1,w+2,h+2));
	}
	
	// given that something about the sizing has changed, makes sure that the scrollbars are positioned on the panel,
	// and assigned the appropriate domain
	protected void adaptScrollers()
	{
		// special little check inserted in here: after adding rows, sometimes the space needed for the title block increases;
		// this is as good a place as any to adjust it
		int tw=recommendedTitleWidth();
		if (tw>titleWidth)
		{
			int dx=tw-titleWidth;
			titleWidth=tw;
			totalWidth+=dx;
			for (int n=0;n<ds.numCols();n++) colX[n]+=dx;
		}
	
		// work out the details of the visible width, less scrollbars if necessary
		visWidth=getWidth();
		visHeight=getHeight();

		if (visWidth>=totalWidth && offsetX>0) {offsetX=0; repaint();}
		if (visHeight>=totalHeight && offsetY>0) {offsetY=0; repaint();}
		
		boolean hscrollOn=false,vscrollOn=false;
		int sw=vscroll.getPreferredSize().width;
		int sh=hscroll.getPreferredSize().height;
		
		if (vscrollOn=totalHeight>visHeight) visWidth-=sw;
		if (hscrollOn=totalWidth>visWidth) visHeight-=sh;
		if (!vscrollOn && totalHeight>visHeight) {visWidth-=sw; vscrollOn=true;}

		hscroll.setVisible(hscrollOn);
		if (hscrollOn) hscroll.setBounds(0,visHeight,visWidth,sh);
		vscroll.setVisible(vscrollOn);
		if (vscrollOn) vscroll.setBounds(visWidth,0,sw,visHeight);

		// then for those which are, set the domains
		if (hscrollOn)
		{
			hscroll.setMinimum(0);
			hscroll.setMaximum(totalWidth);
			hscroll.setVisibleAmount(visWidth);
			hscroll.setBlockIncrement(visWidth);
		}
		if (vscrollOn)
		{
			vscroll.setMinimum(0);
			vscroll.setMaximum(totalHeight);
			vscroll.setVisibleAmount(visHeight);
			vscroll.setBlockIncrement(visHeight);
		}
	}
	
	// returns the {col,row,tcol,trow} position indicated by the screen point; title areas are indicated by setting the value of 
	// the other index to -1; a return value of {-1,-1,-1,-1} indicates nothing at the point; the values {tcol,trow} are other
	// than -1 if either of the resize sections have been clicked on
	protected int[] whichCell(int x,int y)
	{
		int[] cr=new int[]{-1,-1,-1,-1};
		if (x<titleWidth && y<titleHeight) return cr;
		if (x>=visWidth || x>=totalWidth-offsetX || y>=visHeight || y>=totalHeight-offsetY) return cr;

		if (x>=titleWidth)
		{
			for (int n=0;n<ds.numCols();n++)
			{
				int nx=colX[n]+colWidth[n]-offsetX;
				if (y<titleHeight && x>=nx-2 && x<=nx+2) {cr[2]=n; return cr;}
				if (x<nx) {cr[0]=n; break;}
			}
		}
		if (y>=titleHeight)
		{
			for (int n=0;n<ds.numRows();n++)
			{
				int ny=rowY[n]+rowHeight[n]-offsetY;
				if (x<titleWidth && y>=ny-2 && y<=ny+2) {cr[3]=n; return cr;}
				if (y<ny) {cr[1]=n; break;}
			}
		}
		
		return cr;
	}

	// changes the selected cell to that indicated; trashes selection; updates painting area
	public void gotoCell(int CN,int RN)
	{
		if (CN<0 || RN<0 || CN>=ds.numCols() || RN>=ds.numRows()) return; // possible if data is empty
		if (curCol==CN && curRow==RN) return;
		repaintCellRange(selCol,selRow,selCol+selWidth-1,selRow+selHeight-1);
		curCol=CN;
		curRow=RN;
		selCol=CN;
		selRow=RN;
		selWidth=1;
		selHeight=1;
		repaintCell(curCol,curRow);
		scrollTo(curCol,curRow);
		updateDragTab(null);
	}

	// translates the cursor to an adjacent cell, and changes the selected set to the new cell, or drags it out further;
	// values of dx=2/-2 are encoding for the "tab" key behaviour, which will move past the last column to the next row (or v.v.)
	public void moveCursor(int dx,int dy,boolean extendSel)
	{
		int tabDir=0;
		if (dx==2) {dx=1; tabDir=1;}
		else if (dx==-2) {dx=-1; tabDir=-1;}
	
		Rectangle pr=null;
		
		if (!extendSel)
		{
			repaintCellRange(selCol,selRow,selCol+selWidth-1,selRow+selHeight-1);
		}
	
		if (curCol+dx<0) dx=0;
		if (curCol+dx>=ds.numCols()) dx=0;
		if (curRow+dy<0) dy=0;
		if (curRow+dy>=ds.numRows()) dy=0;
		
		if (dx==0 && dy==0 && extendSel) return; // nop
		
		if (dx!=0 || dy!=0)
		{
			tidyDefinitely();
			repaintCell(curCol,curRow);
			curCol+=dx;
			curRow+=dy;
			repaintCell(curCol,curRow);
		}
		else if (tabDir==1 && curRow<ds.numRows()-1)
		{
			tidyDefinitely();
			repaintCell(curCol,curRow);
			curCol=0;
			curRow++;
			repaintCell(curCol,curRow);
		}
		else if (tabDir==-1 && curRow>0)
		{
			tidyDefinitely();
			repaintCell(curCol,curRow);
			curCol=ds.numCols()-1;
			curRow--;
			repaintCell(curCol,curRow);
		}
		
		if (extendSel)
		{
			if (curCol<selCol) 
			{
				selCol--; selWidth++;
				repaintCellRange(curCol,selRow,curCol,selRow+selHeight-1);
			}
			if (curCol>=selCol+selWidth)
			{
				selWidth++;
				repaintCellRange(curCol,selRow,curCol,selRow+selHeight-1);
			}
			if (curRow<selRow) 
			{
				selRow--; selHeight++;
				repaintCellRange(selCol,curRow,selCol+selWidth-1,curRow);
			}
			if (curRow>=selRow+selHeight)
			{
				selHeight++;
				repaintCellRange(selCol,curRow,selCol+selWidth-1,curRow);
			}
		}
		else {selCol=curCol; selRow=curRow; selWidth=selHeight=1;}
		
		scrollTo(curCol,curRow);
		
		updateDragTab(null);
		repaint(pr);
	}

	protected void movePageUp()
	{
		int xtra=rowY[curRow]-offsetY<=titleHeight ? visWidth : 0;
		for (int n=curRow-1;n>=0;n--) if (rowY[n]-offsetY<=titleHeight-xtra || n==0) {gotoCell(curCol,n); break;}
	}
	
	protected void movePageDown()
	{
		int xtra=rowY[curRow]+rowHeight[curRow]-offsetY>=visHeight ? visHeight : 0;
		for (int n=curRow+1;n<ds.numRows();n++) if (rowY[n]+rowHeight[n]-offsetY>=visHeight+xtra || n==ds.numRows()-1) 
			{gotoCell(curCol,n); break;}
	}

	// set the cursor to an appropriate shape, given the position, and absence of special events such as dragging
	protected void normalCursor(int x,int y)
	{	 
		int[] cr=whichCell(x,y);
		boolean col=cr[0]>=0,row=cr[1]>=0,tcol=cr[2]>=0,trow=cr[3]>=0;
		
		if (col && row)
		{
			if (showDragTab && cr[0]==selCol && cr[1]==selRow && 
				x<colX[selCol]+DRAGTAB_WIDTH && y<rowY[selRow]+DRAGTAB_HEIGHT)
				setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR)); // grab-for-drag area
			else
				setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)); // in cell area
		}
		else if (col) setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // column heading
		else if (row) setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // row heading
		else if (tcol) setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR)); // column resizor
		else if (trow) setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR)); // column resizor
		else setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)); // not above anything interesting
	}

	// drags a row or column to the new position	
	protected void dragColumn(int col,int x)
	{
		DataUI dui=manufactureUI(ds,col,fontMetrics);
		x=Math.max(x,colX[col]+dui.minimumWidth());
		x=Math.min(x,colX[col]+dui.maximumWidth());
		int newW=x-colX[col];
		if (newW==colWidth[col]) return;

		colWidth[col]=newW;
		recalcPositions();
		dragX=colX[col]+colWidth[col];
	}
	protected void dragRow(int row,int y)
	{
		int minH=0,maxH=0;
		for (int n=0;n<ds.numCols();n++) 
		{
			DataUI dui=manufactureUI(ds,n,fontMetrics);
			minH=Math.max(minH,dui.minimumHeight());
			maxH=Math.max(maxH,dui.maximumHeight());
		}
		y=Math.max(y,rowY[row]+minH);
		y=Math.min(y,rowY[row]+maxH);
		int newH=y-rowY[row];
		if (newH==rowHeight[row]) return;
		
		rowHeight[row]=newH;
		recalcPositions();
		dragY=rowY[row]+rowHeight[row];
	}

	// entertains the possibility that it might be necessary to switch the "drag tab" state
	protected void updateDragTab(MouseEvent e)
	{
		boolean newShow=false;
		if (e!=null)
		{
			int[] cr=whichCell(e.getX(),e.getY());
			if (cr[0]>=0 && cr[1]>=0 && cr[2]<0 && cr[3]<0)
				newShow=cr[0]>=0 && cr[1]>=0 && cr[0]>=selCol && cr[1]>=selRow && cr[0]<selCol+selWidth && cr[1]<selRow+selHeight;
		}
		if (newShow==showDragTab) return;
		repaintCell(selCol,selRow);
		showDragTab=newShow;
	}
	
	// ----------------------- drawing methods -----------------------
	
	protected void paintComponent(Graphics gr)
	{
		if (visWidth<0 || visHeight<0) adaptScrollers();
	
		//long time=System.currentTimeMillis(); // for benchmarking, optional
	
		int width=getWidth(),height=getHeight();
		int ncols=ds.numCols(),nrows=ds.numRows();
		int tw=titleWidth,th=titleHeight;
	
		gr.setColor(colBackgr);
		gr.fillRect(0,0,width,height);

		Graphics2D g=(Graphics2D)gr;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);

		Rectangle initclip=g.getClipBounds();
		Rectangle clip=initclip.intersection(new Rectangle(0,0,visWidth,visHeight));
		g.setClip(clip);

		// figure out the range of cells that is of interest
		int minCol=0,maxCol=ncols-1;
		for (int n=0;n<ncols;n++) {minCol=n; if (colX[n]+colWidth[n]-offsetX>=clip.x) break;}
		for (int n=minCol;n<ncols;n++) {maxCol=n; if (colX[n]-offsetX>clip.x+clip.width) break;}
		int minRow=0,maxRow=nrows-1;
		for (int n=0;n<nrows;n++) {minRow=n; if (rowY[n]+rowHeight[n]-offsetY>=clip.y) break;}
		for (int n=minRow;n<nrows;n++) {maxRow=n; if (rowY[n]-offsetY>clip.y+clip.height) break;}

		// draw the titles
		g.setClip(clip.intersection(new Rectangle(tw-1,0,visWidth-tw+1,th)));
		for (int n=0;n<ncols;n++)
		{
			int x=colX[n]-offsetX,y=1,w=colWidth[n],h=titleHeight-2*BORDER_SIZE;
			drawTitleBox(g,x,y,w,h,ds.colName(n),ds.colType(n));
		}
		g.setClip(clip.intersection(new Rectangle(0,th-1,tw,visHeight-th+1)));
		for (int n=minRow;n<=maxRow;n++)
		{
			int x=1,y=rowY[n]-offsetY,w=titleWidth-2*BORDER_SIZE,h=rowHeight[n];
			drawTitleBox(g,x,y,w,h,String.valueOf(n+1),0);
		}

		clip=clip.intersection(new Rectangle(tw,th,visWidth-tw,visHeight-th));
		g.setClip(clip);

		// draw each of the cells
		for (int c=minCol;c<=maxCol;c++)
		{
			DataUI ui=manufactureUI(ds,c,fontMetrics);

			for (int r=minRow;r<=maxRow;r++)
			{
				int x=colX[c]-offsetX,y=rowY[r]-offsetY,w=colWidth[c],h=rowHeight[r];

				boolean isSelected=c>=selCol && c<selCol+selWidth && r>=selRow && r<selRow+selHeight;
				Color cellBackgr=isSelected ? colSelected : colUnselected;
				g.setColor(cellBackgr);
				g.fillRect(x,y,w,h);

				g.setStroke(new BasicStroke(1));

				g.setColor(colBorder);
				g.drawLine(x,y+h,x+w,y+h);
				g.drawLine(x+w,y,x+w,y+h);

				if (c==curCol && r==curRow)
				{
					g.setColor(colHardBorder);
					g.drawRect(x,y,w-1,h-1);
				}

				// setup the translation & clip, and farm it out to the DataUI descendent of choice

				g.setClip(clip.intersection(new Rectangle(x,y,w,h)));
				g.translate(x,y);

				g.setFont(font);
				g.setColor(colMain);
				ui.draw(g,r,w,h,cellBackgr);

				if (c==selCol && r==selRow && showDragTab)
				{
					Rectangle tr=new Rectangle(0,0,DRAGTAB_WIDTH-1,DRAGTAB_HEIGHT-1);
					g.setColor(colUnselected);
					g.fill(tr);
					g.setColor(colBorder);
					g.setStroke(new BasicStroke(1.5f));
					g.draw(tr);
					
					int cx=DRAGTAB_WIDTH/2,cy=DRAGTAB_HEIGHT/2;
					double ext=0.15*(DRAGTAB_WIDTH+DRAGTAB_HEIGHT);
					int d0=Util.iround(ext);
					int d1=Util.iround(ext*Math.cos(Math.PI/6));
					int d2=Util.iround(ext*Math.sin(Math.PI/6));
					int[] hx=new int[]{cx,cx,cx-d1,cx+d1,cx-d1,cx+d1};
					int[] hy=new int[]{cy-d0,cy+d0,cy-d2,cy+d2,cy+d2,cy-d2};
					g.setColor(colHardBorder);
					for (int n=0;n<6;n+=2) g.drawLine(hx[n],hy[n],hx[n+1],hy[n+1]);
					for (int n=0;n<6;n++) g.draw(new Ellipse2D.Float(hx[n]-1,hy[n]-1,2,2));
				}
				
				g.translate(-x,-y);
				g.setClip(clip);
			}
		}

		g.setClip(initclip);

		//time=System.currentTimeMillis()-time;
		//Util.writeln("redraw time:"+(1E-3*time));
	}
	
	private void drawTitleBox(Graphics2D g,int x,int y,int w,int h,String ttl,int type)
	{
		g.setColor(colMedium);
		g.fillRect(x,y,w,h);

		g.setColor(colHardBorder);
		g.drawRect(x-1,y-1,w+1,h+1);

		g.setColor(colLight);
		g.drawLine(x,y+h-1,x,y);
		g.drawLine(x,y,x+w-1,y);
		
		g.setColor(colDark);
		g.drawLine(x+w-1,y,x+w-1,y+h-1);
		g.drawLine(x+w-1,y+h-1,x,y+h-1);

		String str=ttl;
		Font f=font;
		FontMetrics fm=fontMetrics;
		if (str.length()>1 && str.charAt(0)=='.') {str=str.substring(1); f=italFont; fm=italFontMetrics;}
		
		int sw=fm.stringWidth(str);
		int tmod=type==0 ? 0 : fontMetrics.stringWidth("X"); // reserve space for type, if possible
		if (w<tmod*4) {tmod=0; type=0;} // extremely cramped, so don't show type
		if (sw>w-2-tmod)
		{
			int dw=fm.stringWidth("..");
			while (str.length()>0 && fm.stringWidth(str)+dw>w-4-tmod) {str=str.substring(0,str.length()-1);}
			str+="..";
			sw=fm.stringWidth(str);
		}
		g.setFont(f);
		g.setColor(colMain);
		g.drawString(str,x+(w-sw-tmod)/2,y+(h+fm.getAscent())/2);
		
		// indicate the type of title, if requested
		Color typecol=new Color(Math.max(0,colMedium.getRed()-64),
								Math.max(0,colMedium.getGreen()-64),
								Math.max(0,colMedium.getBlue()-64));
		if (type==0) {}
		else if (type==DataSheet.COLTYPE_MOLECULE)
		{
			float hr=fontMetrics.getHeight()*0.35f;
			float hx=x+w-2-hr,hy=y+h*0.5f;
			Path2D.Float p=new Path2D.Float(),pi=new Path2D.Float();
			for (int n=0;n<6;n++)
			{
				double th=Math.PI*n*(1.0/3);
				double px=hx+Math.sin(th)*hr,py=hy+Math.cos(th)*hr;
				double pxi=hx+Math.sin(th)*(hr-1),pyi=hy+Math.cos(th)*(hr-1);
				if (n==0) {p.moveTo(px,py); pi.moveTo(pxi,pyi);} else {p.lineTo(px,py); pi.lineTo(pxi,pyi);}
			}
			p.closePath();
			g.setColor(typecol);
			g.fill(p);
			g.setColor(colMedium);
			g.fill(pi);
		}
		else
		{
			String ch=null;
			if (type==DataSheet.COLTYPE_STRING) ch="A";
			else if (type==DataSheet.COLTYPE_INTEGER) ch="I";
			else if (type==DataSheet.COLTYPE_REAL) ch="R";
			else if (type==DataSheet.COLTYPE_BOOLEAN) ch="B";
			if (ch!=null)
			{
				g.setFont(font);
				g.setColor(typecol);
				g.drawString(ch,x+w-fm.stringWidth(ch)-2,y+(h+fm.getAscent())/2);
			}
		}
	}
   
   // ----------------------- overrides -----------------------

	// special version which does nothing if given a null value
	public void repaint(Rectangle r)
	{
		if (r!=null) super.repaint(r);
	}
	
	public Dimension getPreferredSize() 
	{
		int sw=vscroll.getPreferredSize().width,sh=hscroll.getPreferredSize().height;
		int w=totalWidth,h=totalHeight;
		if (h>500) w+=sw;
		if (w>500) h+=sh;
		return new Dimension(Math.max(300,Math.min(500,w)),Math.max(300,Math.min(500,h)));
	}
	
	// ----------------------- events -----------------------
	
	public void keyPressed(KeyEvent e) {}
	public void keyReleased(KeyEvent e) {}
	public void keyTyped(KeyEvent e) {}
	
	public void componentHidden(ComponentEvent e) {}
	public void componentMoved(ComponentEvent e) {}
	public void componentShown(ComponentEvent e) {}
	public void componentResized(ComponentEvent e) {adaptScrollers();}

	public void adjustmentValueChanged(AdjustmentEvent e)
	{
		if (blockScrollEvents) return;
		
		if (e.getSource()==hscroll && offsetX!=e.getValue()) 
		{
			offsetX=e.getValue(); 
			repositionEdit();
			tidyMaybe(); 
			repaint();
		}
		if (e.getSource()==vscroll && offsetY!=e.getValue()) 
		{
			offsetY=e.getValue();
			repositionEdit();
			tidyMaybe(); 
			repaint();
		}
	}
		
	public void mouseClicked(MouseEvent e)
	{
		int mod=e.getModifiers()&(MouseEvent.SHIFT_MASK|MouseEvent.CTRL_MASK|MouseEvent.ALT_MASK);
		boolean dblclk=e.getClickCount()>1;
		int[] cr=whichCell(e.getX(),e.getY());

		if (e.getButton()==1)
		{
			if (mod==0 && !dblclk && cr[0]>=0 && cr[1]>=0) // shift-clicked on a cell
			{
				tidyDefinitely();
				setCurrentCell(cr[0],cr[1]);
				scrollTo(curCol,curRow);
			}
			else if (mod==MouseEvent.SHIFT_MASK && !dblclk && cr[0]>=0 && cr[1]>=0)
			{
				tidyDefinitely();
				extendCurrentSelection(cr[0],cr[1]);
				scrollTo(cr[0],cr[1]);
			}
			else if (mod==0 && !dblclk && cr[0]>=0 && cr[1]<0) // clicked on a column
			{
				tidyDefinitely();
				repaintCellRange(selCol,selRow,selCol+selWidth-1,selRow+selHeight-1);
				curCol=cr[0];
				selCol=cr[0];
				selWidth=1;
				selRow=0;
				selHeight=ds.numRows();
				repaintCellRange(selCol,selRow,selCol+selWidth-1,selRow+selHeight-1);
			}
			else if (mod==0 && !dblclk && cr[0]<0 && cr[1]>=0) // clicked on a row
			{
				tidyDefinitely();
				repaintCellRange(selCol,selRow,selCol+selWidth-1,selRow+selHeight-1);
				curRow=cr[1];
				selRow=cr[1];
				selHeight=1;
				selCol=0;
				selWidth=ds.numCols();
				repaintCellRange(selCol,selRow,selCol+selWidth-1,selRow+selHeight-1);
			}
			else if (mod==0 && dblclk && cr[0]>=0 && cr[1]>=0) // double clicked on a cell
			{
				tidyDefinitely();
				setCurrentCell(cr[0],cr[1]);
				scrollTo(curCol,curRow);
				startEdit((char)0,null);
			}
		}
	}
	public void mouseEntered(MouseEvent e) 
	{
		normalCursor(e.getX(),e.getY());
		updateDragTab(e);
	}
	public void mouseExited(MouseEvent e) 
	{
		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		updateDragTab(e);
	}
	public void mousePressed(MouseEvent e)
	{
		grabFocus(); // lest there be any doubt
		
		boolean shift=(e.getModifiers()&MouseEvent.SHIFT_MASK)>0;
		boolean ctrl=(e.getModifiers()&MouseEvent.CTRL_MASK)>0;
		boolean alt=(e.getModifiers()&MouseEvent.ALT_MASK)>0;
		
		dragType=DRAGTYPE_NONE;

		if (e.getButton()==1 && !shift /*&& !ctrl*/ && !alt)
		{
			dragX=e.getX();
			dragY=e.getY();
			int[] cr=whichCell(dragX,dragY);
			
			if (cr[2]>=0) {dragCol=cr[2]; dragType=DRAGTYPE_COL_RESIZE;}
			else if (cr[3]>=0) {dragRow=cr[3]; dragType=DRAGTYPE_ROW_RESIZE;}
			else if (cr[0]<0 || cr[1]<0) {} // out of range; do nothing
			else if (cr[0]>=selCol && cr[0]<selCol+selWidth && cr[1]>=selRow && cr[1]<selRow+selHeight &&
					 e.getX()<colX[selCol]+DRAGTAB_WIDTH && e.getY()<rowY[selRow]+DRAGTAB_HEIGHT)
			{
				dragType=ctrl ? DRAGTYPE_COPY_DROP : DRAGTYPE_MOVE_DROP;
			}
			else 
			{
				dragType=DRAGTYPE_SELECT;
				curCol=dragCol=cr[0];
				curRow=dragRow=cr[1];
				selCol=curCol;
				selRow=curRow;
				selWidth=1;
				selHeight=1;
				repaint();
			}
		}
		else if (e.getButton()==3) handleRightButton(e);
	}
	public void mouseReleased(MouseEvent e)
	{
		if (dragType==DRAGTYPE_SELECT)
		{
			// (selection is already changed during the dragging process)
		}
		else if (dragType==DRAGTYPE_COL_RESIZE) dragColumn(dragCol,e.getX()+offsetX);
		else if (dragType==DRAGTYPE_ROW_RESIZE) dragRow(dragRow,e.getY()+offsetY);
		
		dragType=DRAGTYPE_NONE;
		updateDragTab(e);
		normalCursor(e.getX(),e.getY());
		
		if (e.getButton()==MouseEvent.BUTTON3) handleRightButton(null);
	}
	public void mouseDragged(MouseEvent e)
	{
		if (dragType==DRAGTYPE_SELECT)
		{
			int[] cr=whichCell(e.getX(),e.getY());
			if (cr[0]!=dragCol || cr[1]!=dragRow)
			{
				if (cr[0]<0 || cr[1]<0)
				{
					selCol=dragCol=curCol;
					selRow=dragRow=curRow;
					selWidth=1;
					selHeight=1;
				}
				else
				{
					dragCol=cr[0];
					dragRow=cr[1];
					if (cr[0]>=curCol) {selCol=curCol; selWidth=cr[0]-curCol+1;} else {selCol=cr[0]; selWidth=curCol-cr[0]+1;}
					if (cr[1]>=curRow) {selRow=curRow; selHeight=cr[1]-curRow+1;} else {selRow=cr[1]; selHeight=curRow-cr[1]+1;}
				}
				repaint();
			}
		}
		else if (dragType==DRAGTYPE_COL_RESIZE) dragColumn(dragCol,e.getX()+offsetX);
		else if (dragType==DRAGTYPE_ROW_RESIZE) dragRow(dragRow,e.getY()+offsetY);
		else if (dragType==DRAGTYPE_MOVE_DROP || dragType==DRAGTYPE_COPY_DROP)
		{
			int[] cn=new int[selWidth],rn=new int[selHeight];
			DataSheetHolder copy=new DataSheetHolder();
			
			for (int i=0;i<cn.length;i++) 
			{
				cn[i]=selCol+i;
				copy.appendColumn(ds.colName(cn[i]),ds.colType(cn[i]),ds.colDescr(cn[i]));
			}
			
			for (int i=0;i<rn.length;i++)
			{
				copy.appendRow();
				rn[i]=selRow+i;
				for (int j=0;j<cn.length;j++) copy.setObject(i,j,ds.getObject(rn[i],cn[j]));
			}
			
			int action=dragType==DRAGTYPE_MOVE_DROP ? TransferHandler.MOVE : TransferHandler.COPY;
			initiateDrag(e,action,copy,rn,cn);
			dragType=DRAGTYPE_NONE; // we have made the hand-off
		}
	}
	public void mouseMoved(MouseEvent e)
	{
		updateDragTab(e);		
		normalCursor(e.getX(),e.getY());
	}
	
	public void mouseWheelMoved(MouseWheelEvent e)
	{
		int q=e.getWheelRotation();
		if (visHeight>=totalHeight) return;
		int incr=fontMetrics.getHeight()*3;
		int newY=Math.min(Math.max(0,offsetY+incr*q),totalHeight-visHeight);
		if (newY!=offsetY)
		{
			offsetY=newY;
			vscroll.setValue(offsetY);
			repaint();
			repositionEdit();
			tidyMaybe();
		}
		updateDragTab(e);
		normalCursor(e.getX(),e.getY());
	}

	public void focusGained(FocusEvent e) {}
	public void focusLost(FocusEvent e) {}

	public void lostOwnership(Clipboard clipboard,Transferable contents) {} // don't care
	
	public void actionPerformed(ActionEvent e) 
	{
		String cmd=e.getActionCommand();
		
		boolean keylock=keyboardLocked();
		
		if (cmd.equals(HOTKEY_UPARROW) && !keylock) moveCursor(0,-1,false);
		else if (cmd.equals(HOTKEY_DOWNARROW) && !keylock) moveCursor(0,1,false);
		
		handleRightButton(null);
	}	 

	// ----------------------- surrogates -----------------------

	protected void tidyMaybe() {}
	protected void tidyDefinitely() {}
	protected void contractCellSize() {}
	protected void repositionEdit() {}
	protected DataUI manufactureUI(DataSheetHolder ds,int colNum,FontMetrics fm) {return null;}
	protected void startEdit(char ch,DataUI dui) {}
	protected void handleRightButton(MouseEvent e) {}
	protected void initiateDrag(MouseEvent e,int action,DataSheetHolder copy,int[] rn,int[] cn) {}
	protected boolean keyboardLocked() {return false;}
}
