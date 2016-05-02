// $Id: Grid.java 3486 2009-10-29 15:48:13Z nguyenda $

package tripod.ui.base;

import java.beans.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;

import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.event.ListEvent;

import net.sf.ehcache.Element;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.CacheConfiguration;

import tripod.util.RangeSearch;
import tripod.ui.util.ColorUtil;

/**
 * A poor's man version of iTunes' grid view.  This is a lazy implementation
 * in that mvc architecture isn't followed... sigh.
 */
public class Grid extends JComponent 
    implements MouseMotionListener, MouseListener,
	       CellEditorListener, PropertyChangeListener,
	       ListSelectionListener, ListEventListener,
	       ComponentListener {
    private static final Logger logger = 
	Logger.getLogger(Grid.class.getName());


    static final CacheManager manager;
    static {
	CacheConfiguration cc = new CacheConfiguration ();
	cc.eternal(false)
	    .diskPersistent(false)
	    .overflowToDisk(false)
	    .timeToIdleSeconds(1200)
	    .timeToLiveSeconds(1200)
	    .timeoutMillis(0)
	    ;

	Configuration config = new Configuration ();
	config.updateCheck(false)
	    .dynamicConfig(true)
	    .defaultCache(cc);

	manager = CacheManager.create//(config);
	    (Grid.class.getResource("resources/ehcache.xml"));
    }

    protected static final Color BORDER_COLOR = ColorUtil.Default;
    protected static final Color SELECTED_COLOR = ColorUtil.Highlighter;
    protected static final Stroke BORDER_STROKE = new BasicStroke (2.f);

    //protected static final Color START_COLOR = new Color (0x949494);
    protected static final Color START_COLOR = new Color (0x474747);
    protected static final Color END_COLOR = new Color (0x6a6a6a);

    protected static final int CELL_SIZE = 100; // default cell size
    protected static final int CELL_EXT_GAP = 4;
    protected static final int CELL_EXT_SIZE = 15;

    static final Font LABEL_FONT =
	UIManager.getFont("Label.font").deriveFont(11.0f);

    static final String CACHE_CELL = "CellCache";
    static final String CACHE_BADGE = "BadgeCache";
    static final String CACHE_ANNO = "AnnoCache";

    int cellSize;
    int vgap = 10, hgap = 20;

    Color borderColor = BORDER_COLOR, selectionColor = SELECTED_COLOR;
    Stroke borderStroke = BORDER_STROKE;

    // set to false to use component's background instead
    boolean useDefaultBackground = true; 

    GridCellRenderer renderer = new DefaultGridCellRenderer ();
    GridCellEditor editor;
    GridCellAnnotator annotator;
    GridCellTipAnnotator tooltip;
    GridCellHighlighter highlighter;

    EventList cells;
    DefaultListSelectionModel selectionModel = 
	new DefaultListSelectionModel ();

    Component editorComp; // editor component
    int editingCell = -1;

    // rubber band
    Point startPt = null;
    Point endPt = null;
    Rectangle rubberBand = new Rectangle ();

    CellRendererPane rendererPane = new CellRendererPane ();
    BadgeLabel badgeLabel = new BadgeLabel ();
    JLabel annoLabel = null;
    JPopupMenu popupMenu = null;


    int cellsPerRow = 0; // computed value
    int cellExtSize = 0; // = cellSize + CELL_EXT_GAP + CELL_EXT_SIZE
    int _width, _height;
    int hover = -1;

    javax.swing.Timer listChangeTimer = new javax.swing.Timer 
	(1000, new ActionListener () {
		public void actionPerformed (ActionEvent e) {
		    clearAndRevalidate ();
		}
	    });

    // random string used as cache instance
    private String cacheInstance;

    RangeSearch<Integer> ranges = new RangeSearch<Integer>();

    static class BadgeLabel extends JLabel {
	Color badgeColor = BORDER_COLOR;
	CellRendererPane rendererPane = new CellRendererPane ();
	JLabel offscreenLabel = new JLabel ();

	public BadgeLabel () {
	    setOpaque (true);
	    Font font = UIManager.getFont("Button.font");
	    setFont (font.deriveFont(Font.BOLD, 11.f));
	    setForeground (Color.white);
	    offscreenLabel.setHorizontalAlignment(JLabel.CENTER);
	    offscreenLabel.setForeground(getForeground ());
	    offscreenLabel.setFont(getFont());
	}

	public void setBadgeColor (Color c) { this.badgeColor = c; }
	public Color getBadgeColor () { return badgeColor; }

        @Override
        protected void paintComponent(Graphics g) {
            // create a buffered image to draw the component into. this lets us
            // draw "out" an area, making it transparent.
            BufferedImage image = new BufferedImage
		(getWidth(), getHeight(),  BufferedImage.TYPE_INT_ARGB);

            // create the graphics and set its initial state.
            Graphics2D g2d = image.createGraphics();
            g2d.setFont(getFont());
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				 RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(badgeColor);

            // draw the badge.
            g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 
			      getHeight(), getHeight());

            // set the color to use for the text - note this color is always
            // the same, though it won't always show because of the composite
            // set below.
            g2d.setColor(getForeground ());
            // if the badge is selected, punch out the text so that the
            //    underlying color shows through as the font color.
            // else use use a standard alpha composite to simply draw on top of
            //    whatever is currently there.
            g2d.setComposite(AlphaComposite.SrcOver);

	    String text = getText ();
	    if (text != null) {
		offscreenLabel.setText(text);
		offscreenLabel.setPreferredSize(getPreferredSize ());
		//label.setFont(getFont ());
		rendererPane.paintComponent
		    (g2d, offscreenLabel, this, 
		     2, 0, getWidth()-2, getHeight());
	    }

            // draw the image into this component.
            g.drawImage(image, 0, 0, null);

            // dispose of the buffered image graphics.
            g2d.dispose();
        }
    }
    
    public Grid () {
	this (new BasicEventList ());
    }

    public Grid (EventList source) {
	initCaches ();

	if (source == null) {
	    throw new IllegalArgumentException ("Input list is null");
	}
	source.addListEventListener(this);
	this.cells = source;

	add (rendererPane);

	//addComponentListener (this);
	//setAutoscrolls (true);
	addMouseMotionListener (this);
	addMouseListener (this);
	addPropertyChangeListener (this);
	setCellSize (CELL_SIZE);

	addComponentListener (this);
	listChangeTimer.setRepeats(false);
	//selectionModel.addListSelectionListener(this);
    }

    void initCaches () {
        byte[] b8 = new byte[8];
        new Random().nextBytes(b8);
        StringBuilder sb = new StringBuilder ();
        for (int i = 0; i < b8.length; ++i) {
            sb.append(String.format("%1$02x", b8[i]));
        }

	cacheInstance = sb.toString();
	Cache cache = new Cache (CACHE_CELL+cacheInstance, 1000, 
				 false, false, 10*60, 60*5);
	manager.addCache(cache);
	cache = new Cache (CACHE_BADGE+cacheInstance, 1000, 
			   false, false, 60*60, 60*30);
	manager.addCache(cache);
	cache = new Cache (CACHE_ANNO+cacheInstance, 1000, 
			   false, false, 60*60, 60*30);
	manager.addCache(cache);
    }

    Cache getCache (String which) {
	return CacheManager.getInstance().getCache(which+cacheInstance);
    }

    Cache getCellCache () { return getCache (CACHE_CELL); }
    Cache getBadgeCache () { return getCache (CACHE_BADGE); }
    Cache getAnnoCache () { return getCache (CACHE_ANNO); }

    public void listChanged (ListEvent e) {
	//logger.info("ListEvent: "+e);
	while (e.nextBlock()) {
	    if (e.isReordering() 
		|| e.getType() == ListEvent.INSERT
		|| e.getType() == ListEvent.DELETE) {
		// only care about the last change
		//clearAndRevalidate ();
		listChangeTimer.restart();
	    }
	}
    }

    public void addListSelectionListener (ListSelectionListener l) {
	selectionModel.addListSelectionListener(l);
    }

    public void removeListSelectionListener (ListSelectionListener l) {
	selectionModel.removeListSelectionListener(l);
    }

    public ListSelectionModel getListSelectionModel () {
	return selectionModel; 
    }

    public void setPopupMenu (JPopupMenu popupMenu) {
	this.popupMenu = popupMenu;
    }
    public JPopupMenu getPopupMenu () { return popupMenu; }

    public void componentHidden (ComponentEvent e) {
    }
    public void componentMoved (ComponentEvent e) {
    }
    public void componentResized (ComponentEvent e) {
	editingStopped ();
    }
    public void componentShown (ComponentEvent e) {
    }

    public void mouseDragged (MouseEvent e) {
	if ((e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) 
	    == MouseEvent.BUTTON1_DOWN_MASK) {
	    endPt = e.getPoint();
	    selectionModel.setValueIsAdjusting(true);

	    //clearSelection ();
	    Rectangle rb = getRubberBand ();
	    Collection<Integer> ov = ranges.findOverlaps(getVisibleRect ());
	    Rectangle r = new Rectangle ();
	    for (Integer c : ov) {
		getCellRect (r, c);
		setCellSelected (c, rb.intersects(r));
	    }
	    repaint ();
	}
    }

    protected Rectangle getRubberBand () {
	if (startPt == null  || endPt == null) {
	    return null;
	}
	int x = Math.min(startPt.x, endPt.x);
	int y = Math.min(startPt.y, endPt.y);
	int w = Math.max(startPt.x, endPt.x) - x + 1;
	int h = Math.max(startPt.y, endPt.y) - y + 1;
	rubberBand.x = x;
	rubberBand.y = y;
	rubberBand.width = w;
	rubberBand.height = h;
	return rubberBand;
    }

    String getToolTipText (int cell) {
	String text = null;
	if (cell >= 0) {
	    if (tooltip != null) {
		cells.getReadWriteLock().readLock().lock();
		try {
		    text = tooltip.getGridCellTipAnnotation
			(this, cells.get(cell), cell);
		}
		finally {
		    cells.getReadWriteLock().readLock().unlock();
		}
	    }
	}
	return text;
    }

    public void mouseMoved (MouseEvent e) {
	int cell = getCellAt (e.getPoint());

	setToolTipText (getToolTipText (cell));
	if (cell == getEditingCell ()) {
	}
	else if (cell >= 0 && editCellAt (cell, e)) {
	    repaint ();
	    //System.out.println("editing cell: " + cell);
	}
	else if (editor != null && editor.stopCellEditing()) { // clean up 
	    editingStopped ();
	    removeEditor ();
	}
	hover = cell;
	//selectionModel.setValueIsAdjusting(true);
    }

    public void mouseClicked (MouseEvent e) {
	//clickGesture (e);
	popupGesture (e);
    }
    public void mouseEntered (MouseEvent e) {
    }
    public void mouseExited (MouseEvent e) {
	popupGesture (e);
    }
    public void mousePressed (MouseEvent e) {
	if (!popupGesture (e)) {
	    clickGesture (e);
	}
    }

    void clickGesture (MouseEvent e) {
	if ((e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) 
	    != MouseEvent.BUTTON1_DOWN_MASK) {
	    return;
	}
	selectionModel.setValueIsAdjusting(true);

	// make sure we're not currently editing
	editingStopped ();
	removeEditor ();

	startPt = e.getPoint();

	boolean isCtrl = e.isControlDown();
	int cell = getCellAt (e.getPoint());

	if (cell < 0) {
	    clearSelection ();
	}
	else if (isCtrl) {
	    setCellSelected (cell, !isCellSelected (cell));
	}
	else {
	    clearSelection ();
	    setCellSelected (cell, true);
	}
	repaint ();
    }

    boolean popupGesture (MouseEvent e) {
	if (!e.isPopupTrigger() || popupMenu == null) {
	    return false;
	}

	popupMenu.show(this, e.getX(), e.getY());
	return true;
    }

    public void mouseReleased (MouseEvent e) {
	if (e.isPopupTrigger() && popupMenu != null) {
	    popupMenu.show(this, e.getX(), e.getY());
	}
	else {
	    startPt = null;
	    endPt = null;
	    selectionModel.setValueIsAdjusting(false);
	    repaint ();
	}
    }

    public void valueChanged (ListSelectionEvent e) {
	if (cells.isEmpty()) {
	    return;
	}

	Rectangle first = getCellRect (e.getFirstIndex());
	Rectangle last = getCellRect (e.getLastIndex());
	Rectangle dirty = first.union(last);

	// force these cells to be repainted
	for (int i = e.getFirstIndex(); i <= e.getLastIndex(); ++i) {
	    removeCache (i);
	}

	repaint (dirty);
    }

    public void clearSelection () { 
	int[] selected = getSelectedCells ();
	for (int i = 0; i < selected.length; ++i) {
	    removeCache (selected[i]);
	}
	selectionModel.clearSelection();
    }

    public void editingStopped (ChangeEvent e) {
	editingStopped ();
    }

    public void editingStopped () {
	if (isEditing ()) {
	    Object value = editor.getCellEditorValue();
	    // update this value... 
	    int cell = getEditingCell ();
	    setValueAt (value, cell);
	    removeEditor ();
	    repaint ();
	}
    }

    public void propertyChange (PropertyChangeEvent e) {
	String name = e.getPropertyName();
	if (name.equals("gridCellEditor")) {
	    GridCellEditor old = (GridCellEditor)e.getOldValue();
	    if (old != null) {
		old.removeCellEditorListener(this);
	    }
	}
    }

    public void editingCanceled (ChangeEvent e) {
	if (isEditing ()) {
	    removeEditor ();
	}
    }

    public void removeEditor () {
	if (editorComp != null) {
	    remove (editorComp);
	    if (editorComp instanceof JComponent) {
		((JComponent)editorComp).setToolTipText(null);
	    }
	    editorComp = null;
	}
	
	if (editingCell >= 0 && editingCell < cells.size()) {
	    Rectangle r = getCellRect (editingCell);
	    repaint (r);
	}
	setEditingCell (-1);
	//removeAll ();
    }

    public boolean isEditing () { return editingCell >=  0; }
    public void setEditingCell (int cell) { 
	this.editingCell = cell; 
    }
    public int getEditingCell () { return editingCell; }

    public Color getBorderColor () { return borderColor; }
    public void setBorderColor (Color c) { 
	borderColor = c;
	repaint ();
    }
    public Color getSelectionColor () { return selectionColor; }
    public void setSelectionColor (Color c) {
	selectionColor = c;
	repaint ();
    }

    public void setCellRenderer (GridCellRenderer renderer) {
	this.renderer = renderer;
    }
    public GridCellRenderer getCellRenderer () { return renderer; }

    public void setCellEditor (GridCellEditor editor) {
	GridCellEditor old = this.editor;
	this.editor = editor;
	editor.addCellEditorListener(this);
	firePropertyChange ("gridCellEditor", old, editor);
    }
    public GridCellEditor getCellEditor () { return editor; }

    public void setCellAnnotator (GridCellAnnotator annotator) {
	GridCellAnnotator old = this.annotator;
	this.annotator = annotator;
	clearCache ();
	firePropertyChange ("gridCellAnnotator", old, annotator);
    }
    public GridCellAnnotator getCellAnnotator () { return annotator; }

    public void setCellTipAnnotator (GridCellTipAnnotator tooltip) {
	GridCellTipAnnotator old = this.tooltip;
	this.tooltip = tooltip;
	firePropertyChange ("gridCellTipAnnotator", old, tooltip);
    }
    public GridCellTipAnnotator getCellTipAnnotator () { return tooltip; }

    public void setCellHighlighter (GridCellHighlighter highlighter) {
	GridCellHighlighter old = this.highlighter;
	this.highlighter = highlighter;
	firePropertyChange ("gridCellHighlighter", old, highlighter);
    }
    public GridCellHighlighter getCellHighlighter () { return highlighter; }

    public boolean isCellSelected (int cell) { 
	return selectionModel.isSelectedIndex(cell);
    }
    public void setCellSelected (int cell, boolean selected) {
	if (selected) {
	    selectionModel.addSelectionInterval(cell, cell);
	}
	else {
	    selectionModel.removeSelectionInterval(cell, cell);
	}
	removeCache (cell);
    }
    public void setSelections (Object[] values) {
	clearSelection ();

	if (values != null) {
	    cells.getReadWriteLock().readLock().lock();
	    try {
		selectionModel.setValueIsAdjusting(true);
		for (Object obj : values) {
		    int c = cells.indexOf(obj);
		    if (c >= 0) {
			setCellSelected (c, true);
		    }
		}
		selectionModel.setValueIsAdjusting(false);
	    }
	    finally {
		cells.getReadWriteLock().readLock().unlock();
	    }
	    repaint ();
	}
    }

    public Object[] getSelectedValues () {
	return getSelections ();
    }

    public Object[] getSelections () {
	int[] selected = getSelectedCells ();

	Object[] selections = new Object[selected.length];
	cells.getReadWriteLock().readLock().lock();
	try {
	    for (int i = 0; i < selected.length; ++i) {
		selections[i] = cells.get(selected[i]);
	    }
	}
	finally {
	    cells.getReadWriteLock().readLock().unlock();
	}
	return selections;
    }

    public void removeSelectedValues () {
	Object[] values = getSelectedValues ();
	for (Object v : values) {
	    remove (v);
	}
	clearCache ();
	repaint ();
    }

    protected void removeCache (int cell) {
	getCellCache().remove(cell);
	getBadgeCache().remove(cell);
	getAnnoCache().remove(cell);
    }

    protected void clearCache () {
	getCellCache().removeAll();
	getBadgeCache().removeAll();
	getAnnoCache().removeAll();
    }	

    public int[] getSelectedCells () {
	BitSet set = new BitSet (cells.size());
	for (int i = 0; i < cells.size(); ++i) {
	    if (isCellSelected (i)) {
		set.set(i);
	    }
	}
	int[] cs = new int[set.cardinality()];
	for (int i = 0, j = set.nextSetBit(0); 
	     j >= 0; j = set.nextSetBit(j+1), ++i) {
	    cs[i] = j;
	}
	return cs;
    }

    public int getSelectedCell () {
	return selectionModel.getMinSelectionIndex();
    }

    public boolean editCellAt (int cell, EventObject e) {
	if (editor == null || !editor.stopCellEditing()) {
	    return false;
	}

	cells.getReadWriteLock().readLock().lock();
	try {
	    Object value = cells.get(cell);
	    Component comp = editor.getGridCellEditorComponent
		(this, value, isCellSelected (cell), cell);
	
	    comp.setBounds(getCellRect (cell));
	    setEditingCell (cell);
	    add (comp);
	    if (comp instanceof JComponent) {
		((JComponent)comp).setToolTipText(getToolTipText (cell));
	    }
	    comp.validate();
	    editorComp = comp;
	}
	finally {
	    cells.getReadWriteLock().readLock().unlock();
	}
	return true;
    }
    
    public Rectangle getCellRect (int cell) {
	return getCellRect (null, cell);
    }

    public int getCellsPerRow () { return cellsPerRow; }

    public Rectangle getCellRect (Rectangle r, int cell) {
	/*
	if (cell < 0 || cell >= getCellCount ()) {
	    throw new IllegalArgumentException ("Invalid cell index: " + cell);
	}
	*/	

	Rectangle b = getBounds ();
	if (r == null) {
	    r = new Rectangle ();
	}

	if (cellsPerRow > 0) {
	    int row = cell / cellsPerRow;
	    int col = cell % cellsPerRow;
	    int vh = annotator!=null ? cellExtSize : cellSize;
	    //int h = row*(cellExtSize+vgap);
	    int h = row*(vh+vgap);
	    int w = cellsPerRow*cellSize + (cellsPerRow-1)*hgap;
	    int x = Math.max(2, (b.width - w)/2);
	    int nrows = (cells.size()+cellsPerRow-1) / cellsPerRow;
	    int y = Math.max(vgap, (b.height - (nrows*vh+(nrows-1)*vgap))/2);
	    r.x = x + col*(cellSize+hgap);
	    //r.y = h + vgap;
	    r.y = h + y;
	    r.height = cellSize;
	    r.width = cellSize;
	}

	return r;
    }

    public void setHgap (int hgap) { 
	this.hgap = hgap;
	adjustSize ();
    }
    public int getHgap () { return hgap; }
    public void setVgap (int vgap) {
	this.vgap = vgap;
	adjustSize ();
    }
    public int getVgap () { return vgap; }

    public void setListSource (EventList source) {
	if (cells != null) {
	    cells.removeListEventListener(this);
	}
	this.cells = source;
	source.addListEventListener(this);

	//repaint ();
	if (listChangeTimer.isRunning()) {
	    listChangeTimer.stop();
	}
	clearAndRevalidate ();
    }

    public Object getSelectedValue () {
	int index = getSelectedCell();
	if (index >= 0) {
	    return getValueAt (index);
	}
	return null;
    }

    public Object getValueAt (int index) {
	cells.getReadWriteLock().readLock().lock();
	try {
	    return cells.get(index);
	}
	finally {
	    cells.getReadWriteLock().readLock().unlock();
	}
    }
	
    public void remove (Object value) {
	cells.getReadWriteLock().writeLock().lock();
	try {
	    int index = cells.indexOf(value);
	    if (index >= 0) {
		removeCache (index);
		cells.remove(value);
	    }
	}
	finally {
	    cells.getReadWriteLock().writeLock().unlock();
	}
    }

    public void setValueAt (Object value, int index) {
	if (index < getCellCount ()) {
	    removeCache (index);
	    
	    cells.getReadWriteLock().writeLock().lock();
	    try {
		
		cells.set(index, value);
	    }
	    finally {
		cells.getReadWriteLock().writeLock().unlock();
	    }
	
	    // repaint this cell...
	    Rectangle r = getCellRect (index);
	    r.height += CELL_EXT_GAP+CELL_EXT_SIZE;
	    repaint (r.x, r.y, r.width, r.height);
	}
    }

    public void addValues (Collection values) {
	cells.getReadWriteLock().writeLock().lock();
	try {
	    cells.addAll(values);
	}
	finally {
	    cells.getReadWriteLock().writeLock().unlock();
	}
    }

    public void addValue (Object value) {
	cells.getReadWriteLock().writeLock().lock();
	try {
	    cells.add(value);
	}
	finally {
	    cells.getReadWriteLock().writeLock().unlock();
	}
    }

    public Enumeration getValues () { 
	cells.getReadWriteLock().readLock().lock();
	try {
	    return Collections.enumeration(cells); 
	}
	finally {
	    cells.getReadWriteLock().readLock().unlock();
	}
    }

    protected void adjustSize () {
	Rectangle bounds = getBounds ();
	//System.out.println("bounds: " + bounds);

	int count, size = getCellCount ();
	cellsPerRow = 0;
	_width = 0;
	for (count = 0; count < size
		 && _width < (bounds.width - cellSize - hgap); ++count) {
	    _width += cellSize + hgap;
	    ++cellsPerRow;
	}
	_width -= hgap; // remove the last gap
	//System.out.println("cellsPerRow: " + cellsPerRow);

	_height = 0;
	if (cellsPerRow > 0) {
	    int nrows = (size + cellsPerRow-1) / cellsPerRow;
	    //System.out.println("rows: " + nrows);

	    int h = annotator != null ? cellExtSize : cellSize;
	    for (count = 0; count < nrows; ++count) {
		_height += h + vgap;
	    }
	    _height += vgap;
	    
	    //System.out.println("preferred: " + _width + " " + _height);
	}

	_width = Math.max(_width, cellSize+2*hgap);
	_height = Math.max(_height, cellExtSize+2*vgap);

	setPreferredSize (new Dimension (_width, _height));
	refreshRanges ();
	//clearCache ();
    }

    @Override
    public Dimension getPreferredSize () {
	Dimension dim = super.getPreferredSize();
	adjustSize ();
	if (cellsPerRow > 0) {
	    dim.width = _width;
	    dim.height = _height;
	}
	return dim;
    }

    public int getCellAt (Point pt) {
	return getCellAt (pt, true);
    }

    public int getCellAt (Point pt, boolean extend) {
	return getCellAt (pt.x, pt.y, extend);
    }

    public int getCellAt (int x, int y) {
	return getCellAt (x, y, true);
    }

    public int getCellAt (int x, int y, boolean extend) {
	Collection<Integer> ov = ranges.findOverlaps(x, y);
	if (!ov.isEmpty()) {
	    return ov.iterator().next();
	}

	return -1;
    }

    public int getCellCount () { 
	cells.getReadWriteLock().readLock().lock();
	try {
	    return cells.size(); 
	}
	finally {
	    cells.getReadWriteLock().readLock().unlock();
	}
    }

    public void setCellSize (int size) {
	this.cellSize = size;
	this.cellExtSize = size + CELL_EXT_GAP + CELL_EXT_SIZE;

	// clear caches...
	clearAndRevalidate ();
    }
    public int getCellSize () { return cellSize; }

    public void clear () {
	if (editor != null) {
	    editor.cancelCellEditing();
	}

	if (cells != null) {
	    cells.getReadWriteLock().writeLock().lock();
	    try {
		cells.clear();
	    }
	    finally {
		cells.getReadWriteLock().writeLock().unlock();
	    }
	}

	clearSelection ();
	clearAndRevalidate ();
    }

    public void setUseDefaultBackground (boolean useDefaultBackground) {
	this.useDefaultBackground = useDefaultBackground;
    }
    public boolean getUseDefaultBackground () { 
	return useDefaultBackground; 
    }

    void clearAndRevalidate () {
	clearCache ();
	removeEditor ();
	adjustSize ();
	revalidate ();
	repaint ();
    }

    void refreshRanges () {
	ranges.clear();

	Rectangle r = new Rectangle ();
	Rectangle ext = new Rectangle (0, 0, cellSize, cellExtSize);
	
	// r is the rectangle that doesn't include the extent value
	// while ext  does
	for (int c = 0; c < cells.size(); ++c) {
	    getCellRect (r, c);
	    ext.x = r.x;
	    ext.y = r.y;
	    ranges.put(ext, c);
	}
    }

    /*
    @Override
    public void repaint () {
	adjustSize ();
	revalidate ();
	super.repaint();
    }
    */

    public void refresh () {
	clearCache ();
	repaint ();
    }

    @Override
    protected void paintComponent (Graphics g) {
	//super.paintComponent(g);
        Rectangle clip = g.getClipBounds();

	Rectangle bounds = getBounds ();
	bounds.x = bounds.y = 0;
	if (cellSize == 0 || cells.isEmpty()
	    || !bounds.intersects(clip)) {
	    return;
	}

	if (cellsPerRow == 0) {
	    adjustSize ();
	    revalidate ();
	}

	Graphics2D g2  = (Graphics2D)g;
	g2.setRenderingHint(RenderingHints.KEY_RENDERING, 
			    RenderingHints.VALUE_RENDER_QUALITY);
	g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
			    RenderingHints.VALUE_ANTIALIAS_ON);

	if (useDefaultBackground) {
	    paintGradientBackground (g2, clip, START_COLOR, END_COLOR);
	}

	int size = getCellCount ();
	Rectangle r = new Rectangle ();
	Rectangle ext = new Rectangle (0, 0, cellSize, cellExtSize);
	for (int c = 0; c < size; ++c) {
	    getCellRect (r, c);

	    ext.x = r.x;
	    ext.y = r.y;
	    if (clip.intersects(ext)) {
		if (isEditing () && getEditingCell() == c) {
		    
		}
		else {
		    Image img = getCachedCell (g, c, r);
		    if (img != null) {
			g.drawImage(img, r.x, r.y, null);
		    }
		}
		
		paintExtContent (g, c, r);
	    }
	    else if (r.y > (clip.y+clip.height)) {
		break;
	    }
	    else if ((r.y+ext.height) < clip.y) {
		// outside of the clip area... 
	    }
	}

	Rectangle rb = getRubberBand ();
	if (rb != null) {
	    g.setXORMode(Color.white);
	    g.drawLine(rb.x, rb.y, rb.x+rb.width, rb.y);
	    g.drawLine(rb.x+rb.width, rb.y, rb.x+rb.width, rb.y+rb.height);
	    g.drawLine(rb.x+rb.width, rb.y+rb.height, rb.x, rb.y+rb.height);
	    g.drawLine(rb.x, rb.y+rb.height, rb.x, rb.y);

	    g2.setColor(getBackground ());
	    g2.setComposite(AlphaComposite.getInstance
			    (AlphaComposite.SRC_OVER, 0.5f));
	    g2.fill(rb);
	}

	rendererPane.removeAll();

	/*
	if (hover >= 0) {
	    getCellRect (r, hover);
	    g.setColor(Color.white);
	    g.drawRect(r.x, r.y, r.width, r.height);
	}
	*/
    }

    protected String getBadgeText (int c) {
	String text = null;
	if (annotator == null) {
	    // if no annotator is specified, we just have a counter
	    //text = String.valueOf(c+1);
	}
	else {
	    cells.getReadWriteLock().readLock().lock();
	    try {
		if (cells.isEmpty() || c >= cells.size()) {
		    return null;
		}

		text = annotator.getGridCellBadgeAnnotation
		    (this, cells.get(c), c);
	    }
	    finally {
		cells.getReadWriteLock().readLock().unlock();
	    }
	}
	return text;
    }

    protected Image getCachedBadge (Graphics g, int c) {

	String text = getBadgeText (c);
	if (text == null) {
	    return null;
	}

	Element el = getBadgeCache().get(c);
	Image img = null;

	if (el != null) {
	    img = (Image)el.getObjectValue();
	}
	else {
	    GraphicsConfiguration gc = ((Graphics2D)g)
		.getDeviceConfiguration();

	    badgeLabel.setText(text);
	    badgeLabel.setBadgeColor
		(isCellSelected (c) ? selectionColor: borderColor);

	    FontMetrics fm = g.getFontMetrics(badgeLabel.getFont());
	    //Dimension d = new Dimension (25, CELL_EXT_SIZE);
	    Dimension d = badgeLabel.getPreferredSize();
	    d.width = Math.min
		(cellSize, (int)(fm.getStringBounds(text, g).getWidth()
				 +2*badgeLabel.getFont().getSize2D()));
	    badgeLabel.setPreferredSize(d);

	    img = gc.createCompatibleImage
		(d.width, d.height, Transparency.TRANSLUCENT);
	    Graphics2D g2 = ((BufferedImage)img).createGraphics();

	    rendererPane.paintComponent
		(g2, badgeLabel, this, 0, 0, d.width, d.height);
	    g2.dispose();

	    getBadgeCache().put(new Element (c, img));
	}
	return img;
    }

    protected Image getCachedCell (Graphics g, int c, Rectangle r) {
	Element el = getCellCache().get(c);
	Image img = null;

	if (el != null) {
	    img = (Image)el.getObjectValue();
	}
	else {
	    GraphicsConfiguration gc = ((Graphics2D)g)
		.getDeviceConfiguration();

	    img = gc.createCompatibleImage
		(Math.max(r.width, 1), Math.max(r.height, 1), 
		 Transparency.TRANSLUCENT);
	    Graphics2D g2 = ((BufferedImage)img).createGraphics();
	    g2.setRenderingHint(RenderingHints.KEY_RENDERING, 
				RenderingHints.VALUE_RENDER_QUALITY);
	    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
				RenderingHints.VALUE_ANTIALIAS_ON);

	    RoundRectangle2D rect = new RoundRectangle2D.Double
		(0., 0., (double)r.width, (double)r.height, 
		 r.width/4., r.height/4.);

	    // Clear the image so all pixels have zero alpha
	    g2.setComposite(AlphaComposite.Clear);
	    g2.fillRect(0, 0, r.width, r.height);

	    g2.setComposite(AlphaComposite.Src);
	    g2.fill(rect);

	    g2.setComposite(AlphaComposite.SrcAtop);
	    g2.fill(rect);

	    Object value = null;
	    cells.getReadWriteLock().readLock().lock();
	    try {
		paintCellContent 
		    (g2, c, value = cells.get(c), 0, 0, r.width, r.height);
	    }
	    finally {
		cells.getReadWriteLock().readLock().unlock();
	    }
	    //paintCellBorder (g2, c, 0, 0, r.width-1, r.height-1);

	    if (isCellSelected (c)) {
		//borderColor
		paintCellShadowBorder (g2, selectionColor, 
				       0, 0, r.width-1, r.height-1, 6);
	    }
	    else if (highlighter != null) {
		Color color = highlighter.getCellColor(this, value, c);
		if (color != null) {
		    paintCellShadowBorder
			(g2, color, 0, 0, r.width-1, r.height-1, 6);
		}
		
		/*
		//g2.setComposite(AlphaComposite.Xor);
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.XOR, .5f));
		Color shadowColor = Color.black;
		g2.setPaint(new Color(shadowColor.getRed(), shadowColor.getGreen(), shadowColor.getBlue(), 150));
		g2.fillRoundRect(5, 5, r.width, r.height, r.width/4, r.height/4);
		*/
	    }
	    g2.dispose();
	    
	    getCellCache().put(new Element (c, img));
	}
	return img;
    }


    protected void paintCellContent (Graphics g, int cell, 
				     Object value, int x, 
				     int y, int width, int height) {
	Component c = renderer.getGridCellRendererComponent
	    (this, value, isCellSelected (cell), cell);
	if (c != null) {
	    rendererPane.paintComponent
		(g, c, this, x+1, y+1, width-1, height-1, true);
	}
    }

    static protected Color mixColor (Color c1, Color c2, float p) {
	float[] a1 = c1.getComponents(null);
	float[] a2 = c2.getComponents(null);
	for (int i = 0; i < 4; ++i) {
	    a1[i] = (a1[i]*p) + (a2[i]*(1.f-p));
	}
	return new Color (a1[0], a1[1], a1[2], a1[3]);
    }

    protected void paintCellShadowBorder (Graphics2D g2, Color c, 
					  int x, int y, int width, 
					  int height, int shadow) {
	int sw = shadow*2;
	for (int i=sw; i >= 2; i-=2) {
	    float pct = (float)(sw - i) / (sw - 1);
	    g2.setColor(mixColor (c, Color.WHITE, pct));
	    g2.setStroke(new BasicStroke(i));
	    g2.drawRoundRect(x, y, width, height, width/4, height/4);
	}
    }

    protected void paintCellBorder (Graphics g, int cell, 
				    int x, int y, int width, int height) {
	((Graphics2D)g).setStroke(borderStroke);
	g.setColor(isCellSelected (cell) ? selectionColor : borderColor);
	g.drawRoundRect(x, y, width, height, width/3, height/3);
    }

    protected void paintExtContent (Graphics g, int c, Rectangle r) {
	Image badge = getCachedBadge (g, c);

	/*
	if (badge == null) {
	    // if no annotator, then we center the label
	    g.drawImage(label, r.x+(r.width-label.getWidth(null))/2, 
			r.y+r.height+CELL_EXT_GAP, null);
	    return;
	}
	*/
	String text = null;
	if (annotator != null) {
	    cells.getReadWriteLock().readLock().lock();
	    try {
		text = annotator.getGridCellAnnotation(this, cells.get(c), c);
	    }
	    finally {
		cells.getReadWriteLock().readLock().unlock();
	    }
	}
	boolean center = annotator == null || text == null;

	int offset = 0;
	if (badge != null) {
	    int width = badge.getWidth(null);
	    g.drawImage(badge, r.x + (center ? (r.width-width)/2 : 0), 
			r.y+r.height+CELL_EXT_GAP, null);
	    offset += width;
	}

	// now draw annotation 
	/*
	  Image anno = getCachedAnno (g, c);
	  if (anno != null) {
	  g.drawImage(anno, r.x+label.getWidth(null)+CELL_EXT_GAP, 
	  r.y+r.height+CELL_EXT_GAP, null);
	  }
	*/

	if (annotator == null || text == null) {
	    return;
	}

	if (annoLabel == null) {
	    annoLabel = new JLabel ();
	    annoLabel.setFont(LABEL_FONT);
	}
	annoLabel.setForeground
	    (getUseDefaultBackground () ? Color.white : Color.black);

	annoLabel.setHorizontalAlignment
	    (badge != null ? JLabel.LEADING : JLabel.CENTER);
	annoLabel.setText(text);
	
	Dimension d = annoLabel.getPreferredSize();
	d.width = cellSize-offset;
	if (d.width > 0 && d.height > 0) {
	    annoLabel.setPreferredSize(d);
	    if (offset > 0) {
		offset += CELL_EXT_GAP;
	    }
	    rendererPane.paintComponent
		(g, annoLabel, this,  r.x+offset/*+CELL_EXT_GAP*/,
		 r.y+r.height+CELL_EXT_GAP, d.width, d.height);
	}
    }

    protected void paintGradientBackground 
	(Graphics g, Rectangle r, Color from, Color to) {
	Graphics2D g2 = (Graphics2D)g;
	/*
	GradientPaint gp = new GradientPaint 
	    (r.x, r.y, from, 1, r.y+r.height, to);
	g2.setPaint(gp);
	*/
	g2.setPaint(from);
	g2.fillRect(r.x, r.y, r.width, r.height);
    }

    protected Image getCachedAnno (Graphics g, int c) {
	Element el = getAnnoCache().get(c);
	Image img = null;
	if (el != null) {
	    img = (Image)el.getObjectValue();
	}
	else {
	    GraphicsConfiguration gc = ((Graphics2D)g)
		.getDeviceConfiguration();
	    JLabel label = new JLabel ();
	    Font f = label.getFont();
	    label.setForeground(Color.white);
	    label.setFont(f.deriveFont(f.getSize2D()-1.f));
	    //.deriveFont(Font.BOLD));

	    String text = "";
	    cells.getReadWriteLock().readLock().lock();
	    try {
		text = annotator.getGridCellAnnotation
		    (this, cells.get(c), c);
		label.setText(text);
	    }
	    finally {
		cells.getReadWriteLock().readLock().unlock();
	    }

	    Dimension d = label.getPreferredSize();
	    if (d.width > 0 && d.height > 0) {
		img = gc.createCompatibleImage
		    (cellSize, d.height, Transparency.TRANSLUCENT);
		Graphics2D g2 = ((BufferedImage)img).createGraphics();
		
		rendererPane.paintComponent
		    (g2, label, this, 0, 0, cellSize, d.height);
		g2.dispose();
		
		getAnnoCache().put(new Element (c, img));
	    }
	    else if (text != null && text.length() > 0) {
		logger.log(Level.WARNING, "Can't render cell annotation \""
			   + label.getText() + "\" at cell " + c);
	    }
	}
	return img;
    }

    public static void main (String[] argv) throws Exception {
	Grid gp = new Grid ();
	gp.setUseDefaultBackground(false);
	gp.setCellSize(80);

	/*
	for (int c = 0; c < 120; ++c) {
	    gp.addValue(String.valueOf(c+1));
	}
	*/
	for (String s : argv) {
	    gp.addValue(s);
	}

	JFrame f = new JFrame ();
	f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	f.getContentPane().add(new JScrollPane (gp));
	f.setSize(200, 200);
	f.pack();
	f.setVisible(true);
    }
}

