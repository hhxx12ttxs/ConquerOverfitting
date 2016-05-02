/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of Power*Architect.
 *
 * Power*Architect is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*Architect is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */
package ca.sqlpower.architect.swingui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.prefs.Preferences;
import java.util.Set;
import java.util.WeakHashMap;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.ProgressMonitor;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectSessionImpl;
import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.architect.UserSettings;
import ca.sqlpower.architect.olap.MondrianModel;
import ca.sqlpower.architect.olap.MondrianModel.Cube;
import ca.sqlpower.architect.olap.MondrianModel.CubeUsage;
import ca.sqlpower.architect.olap.MondrianModel.CubeUsages;
import ca.sqlpower.architect.olap.MondrianModel.DimensionUsage;
import ca.sqlpower.architect.olap.MondrianModel.Hierarchy;
import ca.sqlpower.architect.olap.MondrianModel.Level;
import ca.sqlpower.architect.olap.MondrianModel.Measure;
import ca.sqlpower.architect.olap.MondrianModel.Schema;
import ca.sqlpower.architect.olap.MondrianModel.VirtualCube;
import ca.sqlpower.architect.olap.MondrianModel.VirtualCubeDimension;
import ca.sqlpower.architect.olap.MondrianModel.VirtualCubeMeasure;
import ca.sqlpower.architect.olap.OLAPObject;
import ca.sqlpower.architect.swingui.action.CancelAction;
import ca.sqlpower.architect.swingui.event.PlayPenLifecycleEvent;
import ca.sqlpower.architect.swingui.event.PlayPenLifecycleListener;
import ca.sqlpower.architect.swingui.event.SelectionEvent;
import ca.sqlpower.architect.swingui.event.SelectionListener;
import ca.sqlpower.architect.swingui.olap.CubePane;
import ca.sqlpower.architect.swingui.olap.DimensionPane;
import ca.sqlpower.architect.swingui.olap.DimensionPane.HierarchySection;
import ca.sqlpower.architect.swingui.olap.OLAPPane;
import ca.sqlpower.architect.swingui.olap.OLAPTree;
import ca.sqlpower.architect.swingui.olap.PaneSection;
import ca.sqlpower.architect.swingui.olap.UsageComponent;
import ca.sqlpower.architect.swingui.olap.VirtualCubePane;
import ca.sqlpower.object.ObjectDependentException;
import ca.sqlpower.object.SPChildEvent;
import ca.sqlpower.object.SPListener;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.object.undo.CompoundEventListener;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sqlobject.SQLCatalog;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLObject;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLObjectRuntimeException;
import ca.sqlpower.sqlobject.SQLObjectUtils;
import ca.sqlpower.sqlobject.SQLRelationship;
import ca.sqlpower.sqlobject.SQLRelationship.SQLImportedKey;
import ca.sqlpower.sqlobject.SQLSchema;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.sqlobject.SQLTable.TransferStyles;
import ca.sqlpower.sqlobject.SQLTypePhysicalPropertiesProvider;
import ca.sqlpower.swingui.CursorManager;
import ca.sqlpower.swingui.ProgressWatcher;
import ca.sqlpower.swingui.SPSwingWorker;
import ca.sqlpower.swingui.dbtree.SQLObjectSelection;
import ca.sqlpower.util.SQLPowerUtils;
import ca.sqlpower.util.TransactionEvent;
import ca.sqlpower.util.TransactionEvent.TransactionState;

import com.google.common.collect.ArrayListMultimap;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;


/**
 * The PlayPen is the main GUI component of the SQL*Power Architect.
 */
@SuppressWarnings(
        justification = "PlayPen is not meant to be serializable",
        value = {"SE_BAD_FIELD"})
public class PlayPen extends JPanel
	implements SPListener, SelectionListener, Scrollable {

    public interface CancelableListener {

		public void cancel();

	}
 // actionCommand identifier for actions shared by Playpen
    public static final String ACTION_COMMAND_SRC_PLAYPEN = "PlayPen";
    
	private static Logger logger = Logger.getLogger(PlayPen.class);

	public enum MouseModeType {IDLE,
						CREATING_TABLE,
						CREATING_RELATIONSHIP,
						SELECT_TABLE,
						SELECT_RELATIONSHIP,
						SELECT_ITEM,
						SELECT_SECTION,
						MULTI_SELECT,
						RUBBERBAND_MOVE}
	private MouseModeType mouseMode = MouseModeType.IDLE;

	/**
	 * The cursor manager for this play pen.
	 */
	private final CursorManager cursorManager;

	protected void addImpl(Component c, Object constraints, int index) {
	    throw new UnsupportedOperationException("You can't add swing component for argument"); //$NON-NLS-1$
	}

	// --- Scrollable Methods --- //
	public Dimension getPreferredScrollableViewportSize() {
	    // return getPreferredSize();
	    return new Dimension(800,600);
	}

	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
	    if (orientation == SwingConstants.HORIZONTAL) {
	        return visibleRect.width;
	    } else { // SwingConstants.VERTICAL
	        return visibleRect.height;
	    }
	}

	public boolean getScrollableTracksViewportHeight() {
	    return false;
	}

	public boolean getScrollableTracksViewportWidth() {
	    return false;
	}

	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
	    if (orientation == SwingConstants.HORIZONTAL) {
	        return visibleRect.width/5;
	    } else { // SwingConstants.VERTICAL
	        return visibleRect.height/5;
	    }
	}

	// -------------------------- JComponent overrides ---------------------------

	/**
	 * Calculates the smallest rectangle that will completely
	 * enclose the visible components.
	 *
	 * This is then compared to the viewport size, one dimension
	 * at a time.  To ensure the whole playpen is "live", always
	 * choose the larger number in each Dimension.
	 *
	 * There is also a lower bound on how small the playpen can get.  The
	 * layout manager returns a preferred size of (100,100) when asked.
	 */
	public Dimension getPreferredSize() {

	    Dimension usedSpace = getUsedArea();
	    Dimension vpSize = getViewportSize();
	    Dimension ppSize = null;

	    // viewport seems to never come back as null, but protect anyways...
	    if (vpSize != null) {
	        ppSize = new Dimension(Math.max(usedSpace.width, vpSize.width),
	                Math.max(usedSpace.height, vpSize.height));
	    }

	    if (logger.isDebugEnabled()) {
	        logger.debug("minsize is: " + this.getMinimumSize()); //$NON-NLS-1$
	        logger.debug("unzoomed userDim is: " + unzoomPoint(new Point(usedSpace.width,usedSpace.height))); //$NON-NLS-1$
	        logger.debug("zoom="+zoom+",usedSpace size is " + usedSpace); //$NON-NLS-1$ //$NON-NLS-2$
	    }

	    if (ppSize != null) {
	        logger.debug("preferred size is ppSize (viewport size was null): " + ppSize); //$NON-NLS-1$
	        return ppSize;
	    } else {
	        logger.debug("preferred size is usedSpace: " + usedSpace); //$NON-NLS-1$
	        return usedSpace;
	    }
	}

	public Dimension getUsedArea() {
	    Rectangle cbounds = null;
	    int minx = 0, miny = 0, maxx = 0, maxy = 0;
	    for (PlayPenComponent c : contentPane.getChildren()) {
	        cbounds = c.getBounds(cbounds);
	        minx = Math.min(cbounds.x, minx);
	        miny = Math.min(cbounds.y, miny);
	        maxx = Math.max(cbounds.x + cbounds.width , maxx);
	        maxy = Math.max(cbounds.y + cbounds.height, maxy);
	    }

	    return new Dimension((int) ((double) Math.max(maxx - minx, this.getMinimumSize().width) * zoom),
	            (int) ((double) Math.max(maxy - miny, this.getMinimumSize().height) * zoom));
	}

	// get the size of the viewport that we are sitting in (return null if there isn't one);
	public Dimension getViewportSize() {
	    Container c = SwingUtilities.getAncestorOfClass(JViewport.class, this);
	    if (c != null) {
	        JViewport jvp = (JViewport) c;
	        logger.debug("viewport size is: " + jvp.getSize()); //$NON-NLS-1$
	        return jvp.getSize();
	    } else {
	        logger.debug("viewport size is NULL"); //$NON-NLS-1$
	        return null;
	    }
	}

	// set the size of the viewport that we are sitting in (return null if there isn't one);
	public void setViewportSize(int width, int height) {
	    Container c = SwingUtilities.getAncestorOfClass(JViewport.class, this);
	    if (c != null) {
	        JViewport jvp = (JViewport) c;
	        logger.debug("viewport size set to: " + width + "," + height); //$NON-NLS-1$ //$NON-NLS-2$
	        jvp.setSize(width,height);
	    }
	}

	/**
	 * If some playPen components get dragged into a negative range all tables are then shifted
	 * so that the lowest x and y values are 0.  The tables will retain their relative location.
	 *
	 * If this function is moved into a layout manager it causes problems with undo because we do
	 * no know when this gets called.
	 */
	protected void normalize() {
	    if (normalizing) return;
	    normalizing=true;
	    int minX = 0;
	    int minY = 0;

	    for (PlayPenComponent ppc : contentPane.getChildren()) {
	        minX = Math.min(minX, ppc.getX());
	        minY = Math.min(minY, ppc.getY());
	    }       

	    //Readjusts the playPen's components, since minX and min <= 0,
	    //the adjustments of subtracting minX and/or minY makes sense.
	    if ( minX < 0 || minY < 0 ) {           
	        for (PlayPenComponent ppc : contentPane.getChildren()) {
	            ppc.setLocation(ppc.getX()-minX, ppc.getY()-minY);
	        }

	        // This function may have expanded the playpen's minimum
	        // and preferred sizes, so the original repaint region could be
	        // too small!
	        this.repaint();
	    }
	    normalizing = false;
	}

	//   get the position of the viewport that we are sitting in
	public Point getViewPosition() {
	    Container c = SwingUtilities.getAncestorOfClass(JViewport.class, this);
	    if (c != null) {
	        JViewport jvp = (JViewport) c;
	        Point viewPosition = jvp.getViewPosition();
	        logger.debug("view position is: " + viewPosition); //$NON-NLS-1$
	        return viewPosition;
	    } else {
	        return viewportPosition;
	    }
	}

	// set the position of the viewport that we are sitting in
	public void setViewPosition(Point p) {
	    Container c = SwingUtilities.getAncestorOfClass(JViewport.class, this);
	    if (c != null) {
	        JViewport jvp = (JViewport) c;
	        logger.debug("view position set to: " + p); //$NON-NLS-1$
	        if (p != null) {
	            jvp.setViewPosition(p);
	        }

	    }
	    viewportPosition = p;
	}

	public void setInitialViewPosition() {
	    setViewPosition(viewportPosition);
	}

	/** See {@link #paintingEnabled}. */
	public void setPaintingEnabled(boolean paintingEnabled) {
	    PlayPen.this.paintingEnabled = paintingEnabled;
	}

	/** See {@link #paintingEnabled}. */
	public boolean isPaintingEnabled() {
	    return paintingEnabled;
	}

	public void paintComponent(Graphics g) {
	    if (!paintingEnabled) return;

	    logger.debug("start of paintComponent, width=" + this.getWidth() +
	            ",height=" + this.getHeight()); //$NON-NLS-1$ //$NON-NLS-2$
	    Graphics2D g2 = (Graphics2D) g;
	    g2.setColor(this.getBackground());
	    g2.fillRect(0, 0, this.getWidth(), this.getHeight());
	    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, antialiasSetting);

	    if (isDebugEnabled()) {
	        Rectangle clip = g2.getClipBounds();
	        if (clip != null) {
	            g2.setColor(Color.green);
	            clip.width--;
	            clip.height--;
	            g2.draw(clip);
	            g2.setColor(this.getBackground());
	            logger.debug("Clipping region: "+g2.getClip()); //$NON-NLS-1$
	        } else {
	            logger.debug("Null clipping region"); //$NON-NLS-1$
	        }
	    }

	    Rectangle bounds = new Rectangle();
	    AffineTransform backup = g2.getTransform();
	    g2.scale(zoom, zoom);
	    AffineTransform zoomedOrigin = g2.getTransform();

	    List<PlayPenComponent> relationshipsLast = new ArrayList<PlayPenComponent>();
	    List<Relationship> relations = contentPane.getChildren(Relationship.class);
	    List<UsageComponent> usages = contentPane.getChildren(UsageComponent.class);
	    relationshipsLast.addAll(contentPane.getAllChildren());
	    relationshipsLast.removeAll(relations);
	    relationshipsLast.addAll(relations);
	    relationshipsLast.removeAll(usages);	  
	    relationshipsLast.addAll(usages);
	    
	    // counting down so visual z-order matches click detection z-order
	    for (int i = relationshipsLast.size() - 1; i >= 0; i--) {
	        PlayPenComponent c = relationshipsLast.get(i);
	        c.getBounds(bounds);
	        //expanding width and height by 1 as lines have 0 width or height when vertical/horizontal
	        if ( g2.hitClip(bounds.x, bounds.y, bounds.width + 1, bounds.height + 1)) {
	            if (logger.isDebugEnabled()) logger.debug("Painting visible component "+c); //$NON-NLS-1$
	            g2.translate(c.getLocation().x, c.getLocation().y);
	            Font g2Font = g2.getFont();
	            c.paint(g2);
	            g2.setFont(g2Font);
	            g2.setTransform(zoomedOrigin);
	        } else {
	            if (logger.isDebugEnabled()) logger.debug("paint: SKIPPING "+c); //$NON-NLS-1$
	            logger.debug(" skipped bounds are: x=" + bounds.x + " y=" + bounds.y + " width=" + bounds.width + " height=" + bounds.height);
	            logger.debug(" clipping rectangle: x=" + g2.getClipBounds().x + " y=" + g2.getClipBounds().y + " width=" + g2.getClipBounds().width + " height=" + g2.getClipBounds().height);
	        }
	    }

	    if (rubberBand != null && !rubberBand.isEmpty()) {
	        if (logger.isDebugEnabled()) logger.debug("painting rubber band "+rubberBand); //$NON-NLS-1$
	        g2.setColor(rubberBandColor);
	        Composite backupComp = g2.getComposite();
	        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
	        g2.fillRect(rubberBand.x, rubberBand.y, rubberBand.width-1, rubberBand.height-1);
	        g2.setComposite(backupComp);
	        g2.drawRect(rubberBand.x, rubberBand.y, rubberBand.width-1, rubberBand.height-1);
	    }

	    g2.setTransform(backup);

	    logger.debug("end of paintComponent, width=" + this.getWidth() +
	            ",height=" + this.getHeight()); //$NON-NLS-1$ //$NON-NLS-2$

	}

	/**
	 * Delegates to the content pane.
	 *
	 * <p>Important Note: If you want tooltips to be active on this PlayPen instance,
	 * you have to call <tt>ToolTipManager.sharedInstance().registerComponent(pp)</tt> on this
	 * instance (where <tt>pp</tt> is whatever your reference to this playpen is called).
	 */
	public String getToolTipText(MouseEvent e) {
	    Point zp = unzoomPoint(e.getPoint());
	    MouseEvent zoomedEvent =
	        new MouseEvent((Component) e.getSource(), e.getID(), e.getWhen(), e.getModifiers(),
	                zp.x, zp.y, e.getClickCount(), e.isPopupTrigger(), e.getButton());
	    return contentPane.getToolTipText(zoomedEvent);
	}       
    
	/**
	 * The factory responsible for setting up popup menu contents for this playpen.
	 */
	private PopupMenuFactory popupFactory;

	/**
	 * Maps table names (Strings) to Integers.  Useful for making up
	 * new table names if two tables of the same name are added todrag
	 * this playpen.
	 */
	protected Set<String> tableNames;

	/**
	 * This object receives all mouse and mouse motion events in the
	 * PlayPen.  It tries to dispatch them to the ppcomponents, and
	 * also handles playpen-specific behaviour like rubber band
	 * selection and popup menu triggering.
	 */
	protected PPMouseListener ppMouseListener;

	/**
	 * The RubberBand allows the user to select multiple ppcomponents
	 * by click-and-drag across a region.
	 */
	protected Rectangle rubberBand;

	/**
	 * This is the colour that the rubber band will be painted with.
	 */
	protected Color rubberBandColor = Color.black;

	/**
	 * The visual magnification factor for this playpen.
	 */
	protected double zoom;

	/**
	 * Contains the child components of this playpen.
	 */
	protected PlayPenContentPane contentPane;

	/**
	 * This action brings the selected TablePane or Relationship to
	 * the front/top of the component stack.
	 */
	protected Action bringToFrontAction;

	/**
	 * This action sends the selected TablePane or Relationship to
	 * the back/bottom of the component stack.
	 */
	protected Action sendToBackAction;
	
	/**
	 * The zoom in action used by the mouse listener.
	 */
	protected Action zoomInAction;
	
	/**
     * The zoom out action used by the mouse listener.
     */
	protected Action zoomOutAction;
	
	/**
     * The component that is used my the mouse listener to be scrolled.
     * Will always be a JScrollPane, but since the ArchitectFrame returns
     * it as a Component this field is also a Component.
     */
	protected Component ppScrollPane;
	
	/**
	 * This dialog box is for editting the PlayPen's DB Connection spec.
	 */
	protected JDialog dbcsDialog;

	/**
     * used by mouseReleased to figure out if a DND operation just took place in the
     * playpen, so it can make a good choice about leaving a group of things selected
     * or deselecting everything except the TablePane that was clicked on.
     */
	protected boolean draggingContainerPanes = false;

	private boolean selectionInProgress = false;
	
	/**
	 * A RenderingHints value of VALUE_ANTIALIAS_ON, VALUE_ANTIALIAS_OFF, or VALUE_ANTIALIAS_DEFAULT.
	 */
    private Object antialiasSetting = RenderingHints.VALUE_ANTIALIAS_DEFAULT;

	/**
	 * A graveyard for components that used to be associated with model
	 * components that are no longer in the model.  If the model components
	 * come back from the dead (thanks the the UndoManager), then the
	 * corresponding PlayPenComonent can be revived from this map. The play pen
	 * components are mapped to UUIDs of the objects as some SQLObject classes
	 * implement a different version of equals.
	 *
	 * Allows the garbage collecter to clean up any components not in the undo manager
	 *
	 */
    private Map<String,PlayPenComponent> removedComponents = new WeakHashMap<String, PlayPenComponent>();

    /**
     * Tells whether or not this component will paint its contents.  This was
     * originally added to test the speed of the SpringLayout when it doesn't
     * have to repaint everything for every frame.  It might be useful for
     * other stuff later on too.
     */
    private boolean paintingEnabled = true;

	private boolean normalizing;

    /**
     * The session that contains this playpen
     */
	final ArchitectSwingSession session;
	
	/**
	 * The initial position of the viewport.
	 */
	private Point viewportPosition;
	
	/**
	 * The font render context for cases where the play pen
	 * has no graphics object to get the font render context
	 * but we know it from another panel.
	 */
	private FontRenderContext fontRenderContext;
	
	/**
	 * Flag to prevent recursive selections for selectObjects()
	 */
	private boolean ignoreTreeSelection = false;
	
	public PlayPen(ArchitectSwingSession session) {
	    this(session, session.getTargetDatabase());
	}
	
	public PlayPen(ArchitectSwingSession session, SPObject modelContainer) {
	    this(session, new PlayPenContentPane(modelContainer));
	}
	
	/**
     * Creates a play pen with reasonable defaults.  If you are creating
     * this PlayPen for temporary use (as opposed to creating a session's
     * main PlayPen), don't forget to call {@link #destroy()} when you are
     * done with it.
     * 
     * @param session
     *            The session this play pen belongs to. Null is not allowed.
     * @param modelContainer This is the top-level object of 
     * the model that this PlayPen will represent (ie: SQLDatabase, OLAPSession)     
     */
	public PlayPen(ArchitectSwingSession session, PlayPenContentPane ppcp) {
        if (session == null) throw new NullPointerException("A null session is not allowed here."); //$NON-NLS-1$
		this.session = session;		
		setDatabase(session.getTargetDatabase());
		if (session.isEnterpriseSession()) {
		    zoom = session.getEnterpriseSession().getPrefDouble("zoom", 1.0);
		} else {
	        Preferences p = Preferences.userNodeForPackage(ArchitectSessionImpl.class);
	        Preferences prefs = p.node(session.getWorkspace().getName());
            zoom = prefs.getDouble("zoom", 1.0);
		}
        viewportPosition = new Point(0, 0);
		this.setBackground(java.awt.Color.white);
		contentPane = ppcp;
		contentPane.setPlayPen(this);
		this.setName("Play Pen"); //$NON-NLS-1$
		this.setMinimumSize(new Dimension(1,1));
		if (!GraphicsEnvironment.isHeadless()) {
		    //XXX See http://trillian.sqlpower.ca/bugzilla/show_bug.cgi?id=3036
		    new DropTarget(this, new PlayPenDropListener());
		    new DragSource().createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_MOVE, new TablePaneDragGestureListener());
		    logger.debug("DragGestureRecognizer motion threshold: " + 
		            this.getToolkit().getDesktopProperty("DnD.gestureMotionThreshold")); //$NON-NLS-1$ //$NON-NLS-2$
		}
		bringToFrontAction = new BringToFrontAction(this);
		sendToBackAction = new SendToBackAction(this);
		ppMouseListener = new PPMouseListener();
		this.addMouseListener(ppMouseListener);
		this.addMouseMotionListener(ppMouseListener);
		
		cursorManager = new CursorManager(this);
		fontRenderContext = null;
	}

	/**
	 * Creates a new PlayPen with similar contents to the given PlayPen.  The new copy will have fresh
	 * copies of all the contained PlayPenComponents, but will share the same model as the original
	 * play pen.  This was originally intended for use by the print preview panel, but it may end
	 * up useful for other things too.
     * <p>
     * Remember to call {@link #destroy()} when you are done with this playpen!
	 *
     * @param session The session that this new copy should live in.  If you specify a session other
     * than the session that the given playpen lives in, it should still produce a usable copy, however
     * be aware that the underlying SQLObjects will be shared between the two sessions.
	 * @param pp The playpen to duplicate.
	 */
	public PlayPen(ArchitectSwingSession session, PlayPen pp) {
		this(session);
		logger.debug("Copying PlayPen@" + System.identityHashCode(pp) + " into " + System.identityHashCode(this));
		this.antialiasSetting = pp.antialiasSetting;
				
		this.setFont(pp.getFont());
		this.setForeground(pp.getForeground());
		this.setBackground(pp.getBackground());
		
		// XXX this should be done by making PlayPenComponent cloneable.
		// it's silly that playpen has to know about every subclass of ppc
		logger.debug("Copying " + pp.getContentPane().getChildren().size() + " components...");
		for (int i = 0; i < pp.getContentPane().getChildren().size(); i++) {
		    PlayPenComponent ppc = pp.getContentPane().getChildren().get(i);
		    PlayPenContentPane contentPane = (PlayPenContentPane) this.contentPane;
		    if (ppc instanceof TablePane) {
		        TablePane tp = (TablePane) ppc;
		        addImpl(new TablePane(tp, contentPane), ppc.getPreferredLocation());
		    } else if (ppc instanceof Relationship) {
		        Relationship rel = (Relationship) ppc;
		        addImpl(new Relationship(rel, contentPane), ppc.getPreferredLocation());			    
		    } else if (ppc instanceof CubePane) {
		        CubePane cp = (CubePane) ppc;
		        addImpl(new CubePane(cp, contentPane), ppc.getPreferredLocation());
		    } else if (ppc instanceof DimensionPane) {
		        DimensionPane dp = (DimensionPane) ppc;
		        addImpl(new DimensionPane(dp, contentPane), ppc.getPreferredLocation());
		    } else if (ppc instanceof VirtualCubePane) {
		        VirtualCubePane vcp = (VirtualCubePane) ppc;
		        addImpl(new VirtualCubePane(vcp, contentPane), ppc.getPreferredLocation());
		    } else if (ppc instanceof UsageComponent) {
		        UsageComponent uc = (UsageComponent) ppc;
		        contentPane.addChild(new UsageComponent(uc, contentPane), i);
		    } else {
		        throw new UnsupportedOperationException(
		                "I don't know how to copy PlayPenComponent type " + ppc.getClass().getName());
		    }
		}		
		this.setSize(this.getPreferredSize());
	}
    
    /**
     * Adds the given component to this PlayPen's content pane.  Does
     * NOT add it to the Swing containment hierarchy. The playpen is a
     * leaf in the hierarchy as far as swing is concerned.
     *
     * @param c The component to add.  The PlayPen only accepts
     * Relationship and ContainerPane components.
     * @param constraints The Point at which to add the component
     * @param index ignored for now, but would normally specify the
     * index of insertion for c in the child list.
     */
    protected void addImpl(PlayPenComponent c, Object constraints) {        
        if (c instanceof Relationship || c instanceof UsageComponent || c instanceof PlayPenLabel) {
            contentPane.addChild(c, contentPane.getFirstDependentComponentIndex());
        } else if (c instanceof ContainerPane<?, ?>) {
            if (constraints instanceof Point) {
                c.setLocation((Point) constraints);
                contentPane.addChild(c, 0);
            } else {
                throw new IllegalArgumentException("Constraints must be a Point"); //$NON-NLS-1$
            }
            
            if (c instanceof TablePane) {
                // Makes drag and dropped tables show the proper columns
                ((TablePane) c).updateHiddenColumns();
                ((TablePane) c).updateNameDisplay();
            }
        } else {
            throw new IllegalArgumentException("PlayPen can't contain components of type " //$NON-NLS-1$
                                               +c.getClass().getName());
        }
        Dimension size = c.getPreferredSize();
        c.setSize(size);
        logger.debug("Set size to "+size); //$NON-NLS-1$
        logger.debug("Final state looks like "+c); //$NON-NLS-1$
    }

    /**
     * Disconnects this play pen from everything it's listening to.
     * It is important to do this whenever you make a temporary PlayPen
     * instance for some specific purpose (for example, print preview
     * and the column mapping editor panel create temporary play pens).
     * The primary play pen of the session itself doesn't really need
     * to be destroyed, because all of the listener interconnections are
     * contained within the session, and the whole tangled mess can just
     * go away together.
     * <p>
     * As the method name implies, once you have called this method,
     * this PlayPen instance will not function properly, so you should
     * stop using it.
     */
    public void destroy() {
        logger.debug("Destroying playpen " + System.identityHashCode(this));
        // FIXME the content pane must be notified of this destruction, either explicitly or via a lifecycle event
        firePlayPenLifecycleEvent();
        removeHierarchyListeners(session.getTargetDatabase());
    }

    /**
     * Returns a new list of all tables in this play pen. The list returned will
     * be your own private (shallow) copy, so you are free to modify it.
     */
    public List<SQLTable> getTables() throws SQLObjectException {
        List<SQLTable> tables = new ArrayList<SQLTable>();
        SQLObjectUtils.findDescendentsByClass(session.getTargetDatabase(), SQLTable.class, tables);
        return tables;

    }

	private final void setDatabase(SQLDatabase newdb) {
		if (newdb == null) throw new NullPointerException("db must be non-null"); //$NON-NLS-1$
		
		// Note, this also happens in CoreProject, but that's only helpful when loading a project file
		// And you get fireworks if you call setDataSource() on a non-playpen connection
		newdb.setPlayPenDatabase(true);

		JDBCDataSource dbcs = new JDBCDataSource(session.getDataSources());
        newdb.setDataSource(dbcs);

        SQLPowerUtils.listenToHierarchy(newdb, this);
		tableNames = new HashSet<String>();
	}

    public void setDatabaseConnection(JDBCDataSource dbcs){
        JDBCDataSource tSpec = session.getTargetDatabase().getDataSource();
        tSpec.setDisplayName(dbcs.getDisplayName());
        tSpec.getParentType().setJdbcDriver(dbcs.getDriverClass());
        tSpec.setUrl(dbcs.getUrl());
        tSpec.setUser(dbcs.getUser());
        tSpec.setPass(dbcs.getPass());
        tSpec.setPlSchema(dbcs.getPlSchema());
        tSpec.setPlDbType(dbcs.getPlDbType());
        tSpec.setOdbcDsn(dbcs.getOdbcDsn());
    }

    /**
     * Sets up the generic keyboard actions for this playpen. This should only
     * be called once, which is normally done at the time the playpen is
     * created. If no keyboard actions (zoom, delete selected, cursor up/down
     * for item selection) are desired, just don't call this when creating your
     * playpen.
     */
    public void setupKeyboardActions() {
        InputMap inputMap = this.getInputMap(JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        
        this.getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "CANCEL"); //$NON-NLS-1$
        this.getActionMap().put("CANCEL", new CancelAction(this)); //$NON-NLS-1$

        final Object KEY_SELECT_UPWARD = "ca.sqlpower.architect.PlayPen.KEY_SELECT_UPWARD"; //$NON-NLS-1$
        final Object KEY_SELECT_DOWNWARD = "ca.sqlpower.architect.PlayPen.KEY_SELECT_DOWNWARD"; //$NON-NLS-1$

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), KEY_SELECT_UPWARD);
        this.getActionMap().put(KEY_SELECT_UPWARD, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                List<PlayPenComponent> items = getSelectedItems();
                if (items.size() == 1) {
                    PlayPenComponent item = items.get(0);
                    if (item instanceof TablePane) {
                        TablePane tp = (TablePane) item;
                        int oldIndex = tp.getSelectedItemIndex();
                        
                        try {
                            if (oldIndex < 0) {
                                oldIndex = tp.getModel().getColumns().size();
                            }
                            int newIndex = oldIndex;
                            while (newIndex - 1 >= 0) {
                                newIndex--;
                                if (!tp.getHiddenColumns().contains(tp.getModel().getColumn(newIndex))) {
                                    break;
                                }
                            }
                            if (!tp.getHiddenColumns().contains(tp.getModel().getColumn(newIndex))) {
                                tp.selectNone();
                                tp.selectItem(newIndex);
                            }
                        } catch (SQLObjectException ex) {
                            throw new SQLObjectRuntimeException(ex);
                        }
                    }
                }
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), KEY_SELECT_DOWNWARD);
        this.getActionMap().put(KEY_SELECT_DOWNWARD, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                List<PlayPenComponent> items = getSelectedItems();
                if (items.size() == 1) {
                    PlayPenComponent item = items.get(0);
                    if (item instanceof TablePane) {
                        TablePane tp = (TablePane) item;
                        int oldIndex = tp.getSelectedItemIndex();
                        
                        // If the selected "column" is one of the special values
                        // (title, none, before pk, etc) then pressing down arrow
                        // should select the first column
                        if (oldIndex < 0) {
                            oldIndex = -1;
                        }
                        
                        try {
                            if (oldIndex < tp.getModel().getColumns().size() - 1) {
                                try {
                                    int newIndex = oldIndex;
                                    while (newIndex + 1 < tp.getModel().getColumns().size()) {
                                        newIndex++;
                                        if (!tp.getHiddenColumns().contains(tp.getModel().getColumn(newIndex))) {
                                            break;
                                        }
                                    }
                                    if (!tp.getHiddenColumns().contains(tp.getModel().getColumn(newIndex))) {
                                        tp.selectNone();
                                        tp.selectItem(newIndex);
                                    }
                                } catch (SQLObjectException ex) {
                                    throw new SQLObjectRuntimeException(ex);
                                }
                            }
                        } catch (SQLObjectException e1) {
                            logger.error("Could not get columns of "+ tp.getName(), e1); //$NON-NLS-1$
                        }
                    }
                }
            }
        });
        
        this.addKeyListener(new KeyListener() {

            private void changeCursor(KeyEvent e) {
                if ((e.getModifiersEx() & KeyEvent.ALT_DOWN_MASK) != 0) {
                    cursorManager.dragAllModeStarted();
                } else {
                    cursorManager.dragAllModeFinished();
                }
            }

            public void keyPressed(KeyEvent e) { changeCursor(e); }
            public void keyReleased(KeyEvent e) { changeCursor(e); }
            public void keyTyped(KeyEvent e) { changeCursor(e); }
            
        });
    }

    /**
	 * Tells whether or not this PlayPen instance is in debugging mode.
	 * Currently, this is controlled by log4j settings, but that may change
	 * in the future.
	 */
	public boolean isDebugEnabled() {
	    return logger.isDebugEnabled();
	}
	
	// --------------------- Utility methods -----------------------

	/**
	 * Calls setChildPositionImpl(child, p.x, p.y).
	 */
	public void setChildPosition(PlayPenComponent child, Point p) {
		setChildPositionImpl(child, p.x, p.y);
	}

	/**
	 * Calls setChildPositionImpl(child, x, y).
	 */
	public void setChildPosition(PlayPenComponent child, int x, int y) {
		setChildPositionImpl(child, x, y);
	}

	/**
	 * Scales the given X and Y co-ords from the visible point (x,y)
	 * to the actual internal location, and sets child's position
	 * accordingly.
	 *
	 * @param child a component in this PlayPen's content pane.
	 * @param x the apparent visible X co-ordinate
	 * @param y the apparent visible Y co-ordinate
	 */
	protected void setChildPositionImpl(PlayPenComponent child, int x, int y) {
		child.setLocation((int) ((double) x / zoom), (int) ((double) y / zoom));
	}

	/**
     * Returns the zoom in action that the mouse uses for this PlayPen. If none
     * has been set, it returns the default zoom in action from the
     * ArchitectFrame.
     */
	public Action getMouseZoomInAction(){
        if (zoomInAction == null) {
            return session.getArchitectFrame().getZoomInAction();
        }
        return zoomInAction;
    }
    
	/**
     * Sets the zoom in action for which the mouse uses for this PlayPen.
     * 
     * @param zoomInAction
     *            The zoom in action for the mouse to use in this PlayPen.
     */
	public void setMouseZoomInAction(Action zoomInAction){
        this.zoomInAction = zoomInAction;
    }
    
    /**
     * Returns the zoom out action that the mouse uses for this PlayPen. If none
     * has been set, it returns the default zoom in action from the
     * ArchitectFrame.
     */
	public Action getMouseZoomOutAction(){
        if (zoomOutAction == null) {
            return session.getArchitectFrame().getZoomOutAction();
        }
        return zoomOutAction;
    }
    
    /**
     * Sets the zoom out action for which the mouse uses for this PlayPen.
     * 
     * @param zoomOutAction
     *            The zoom out action for the mouse to use in this PlayPen.
     */
	public void setMouseZoomOutAction(Action zoomOutAction){
        this.zoomOutAction = zoomOutAction;
    }
    
    /**
     * Modifies the given point p in model space to apparent position in screen
     * space.
     * 
     * @param p
     *            The point in model space (the space where the actual
     *            components of the content pane live). THIS PARAMETER IS
     *            MODIFIED.
     * @return The given point p, which has been modified or null if p was null.
     */
	public Point zoomPoint(Point p) {
	    if (p == null) return null;
		p.x = (int) ((double) p.x * zoom);
		p.y = (int) ((double) p.y * zoom);
		return p;
	}

    /**
     * Modifies the given point p from apparent position in screen space to
     * model space.
     * 
     * @param p
     *            The point in visible screen space (the space where mouse
     *            events are reported). THIS PARAMETER IS MODIFIED.
     * @return The given point p, which has been modified or null if p was null.
     */
	public Point unzoomPoint(Point p) {
	    if (p == null) return null;
		p.x = (int) ((double) p.x / zoom);
		p.y = (int) ((double) p.y / zoom);
		return p;
	}

	/**
	 * Modifies the given rect p in model space to apparent position
	 * in screen space.
	 *
	 * @param r The rectangle in model space (the space where the actual
	 * components of the content pane live).  THIS PARAMETER IS MODIFIED.
	 * @return The given rect p, which has been modified.
	 */
	public Rectangle zoomRect(Rectangle r) {
		r.x = (int) ((double) r.x * zoom);
		r.y = (int) ((double) r.y * zoom);
		r.width = (int) ((double) r.width * zoom);
		r.height = (int) ((double) r.height * zoom);
		return r;
	}

	/**
	 * Modifies the given rect r from apparent position in screen
	 * space to model space.
	 *
	 * @param r The rectangle in visible screen space (the space where
	 * mouse events are reported).  THIS PARAMETER IS MODIFIED.
	 * @return The given rect p, which has been modified.
	 */
	public Rectangle unzoomRect(Rectangle r) {
		r.x = (int) ((double) r.x / zoom);
		r.y = (int) ((double) r.y / zoom);
		r.width = (int) ((double) r.width / zoom);
		r.height = (int) ((double) r.height / zoom);
		return r;
	}

	// --------------------- accessors and mutators ----------------------

	public void setZoom(double newZoom) {
		if (newZoom != zoom) {
			double oldZoom = zoom;
			zoom = newZoom;
			if(session.isEnterpriseSession()) {
	            session.getEnterpriseSession().putPref("zoom", zoom);
			} else {
		        UserSettings sprefs = session.getUserSettings().getSwingSettings();
		        if (sprefs != null) {
		            sprefs.setObject("zoom", new Double(zoom));
		        }
			}
			this.firePropertyChange("zoom", oldZoom, newZoom); //$NON-NLS-1$
			this.revalidate();
			this.repaint();
		}
	}

	public double getZoom() {
		return zoom;
	}

	public void setRenderingAntialiased(boolean v) {
	    if (v) {
	        antialiasSetting = RenderingHints.VALUE_ANTIALIAS_ON;
	    } else {
	        antialiasSetting = RenderingHints.VALUE_ANTIALIAS_OFF;
	    }
	    this.repaint();
	}

	public boolean isRenderingAntialiased() {
	    return antialiasSetting == RenderingHints.VALUE_ANTIALIAS_ON;
	}

	public PlayPenContentPane getContentPane() {
		return contentPane;
	}
	
	public void setContentPane(PlayPenContentPane pane) {
	    contentPane = pane;
	    pane.setPlayPen(this);
	}

	public void addRelationship(Relationship r) {
		addImpl(r, null);
	}

    /**
     * This method is primarily for loading project files. Use at your own risk!
     *
     * @param tp
     * @param point
     */
    public void addTablePane(TablePane tp, Point point) {
        addImpl(tp, point);
    }
    
    public void addLabel(PlayPenLabel label, Point point) {
        addImpl(label, point);
    }

    /**
     * This method is primarily for loading project files. Use at your own risk!
     * 
     * @param ppc
     *            The component to add.
     * @param point
     *            The location to add the component at, in logical coordinates.
     *            If you don't care where the component lands, or the
     *            component's position is constrained by other factors
     *            (Relationships are positioned relative to the two table panes
     *            they connect) then this argument can be null.
     */
    public void addPlayPenComponent(PlayPenComponent ppc, Point point) {
        addImpl(ppc, point);
    }

	/**
	 * Searches this PlayPen's children for a TablePane whose model is
	 * t.
	 *
	 * @return A reference to the TablePane that has t as a model, or
	 * null if no such TablePane is in the play pen.
	 */
	public TablePane findTablePane(SQLTable t) {
		return (TablePane) findPPComponent(t);
	}
	
	/**
	 * Searches this PlayPen's children for a PlayPenComponent with the
	 * given model.
	 * 
	 * @return A reference to the PlayPenComponent with the given
	 * model, or null if no such PlayPenComponent is in the play pen 
	 */
	public PlayPenComponent findPPComponent(Object model) {
	    for (PlayPenComponent ppc : contentPane.getChildren()) {            
            if (ppc.getModel() == model) {
                return ppc;
            }
        }
	    return null;
	}

	/**
	 * Returns a TablePane in this PlayPen whose name is <code>name</code>.
	 *
	 * <p>Warning: Unique names are not currently enforced in the
	 * PlayPen's database; results will be unpredictable if there is
	 * more than one table with the name you are searching for.
	 *
	 * <p>Implementation note: This method may benefit from a
	 * Map-based lookup rather than the current linear search
	 * algorithm.
	 *
	 * @return A reference to the TablePane whose model name is
	 * <code>name</code>, or <code>null</code> if no such TablePane is
	 * in the play pen.
	 */
	public TablePane findTablePaneByName(String name) {
		name = name.toLowerCase();
		for (PlayPenComponent c : contentPane.getChildren()) {			
			if (c instanceof TablePane
				&& ((TablePane) c).getModel().getName().toLowerCase().equals(name)) {
				return (TablePane) c;
			}
		}
		return null;
	}

	/**
	 * Searches this PlayPen's children for a Relationship whose model is
	 * r.
	 *
	 * @return A reference to the Relationsip that has r as a model, or
	 * null if no such Relationship is in the play pen.
	 */
	public Relationship findRelationship(SQLRelationship r) {
		return (Relationship) findPPComponent(r);
	}

    /**
     * Returns the already in use table names. Useful for
     * deleting tables so it can be removed from this list as well.
     */
    public Set<String> getTableNames () {
        return tableNames;
    }
    
    /**
     * Reconstructs the set of table names by going through all the tables.
     */
    public void resetTableNames() {
        tableNames.clear();
        for (TablePane tp : contentPane.getChildren(TablePane.class)) {
            tableNames.add(tp.getModel().getName().toLowerCase());
        }
    }
    
	/**
	 * Returns the number of components in this PlayPen's
	 * PlayPenContentPane.
	 */
	public int getPPComponentCount() {
		return contentPane.getChildren().size();
	}

	/**
     * Adds or reverse engineers a copy of the given source table to this playpen, using
     * preferredLocation as the layout constraint.  Tries to avoid
     * adding two tables with identical names.
     *
     * @return A reference to the newly-created TablePane.
     * @see SQLTable#inherit
     * @see PlayPenLayout#addLayoutComponent(Component,Object)
     */
    public synchronized TablePane importTableCopy(SQLTable source, Point preferredLocation, DuplicateProperties duplicateProperties) throws SQLObjectException {
        return importTableCopy(source, preferredLocation, duplicateProperties, true);
    }

    /**
	 * Adds or reverse engineers a copy of the given source table to this playpen, using
	 * preferredLocation as the layout constraint.  Tries to avoid
	 * adding two tables with identical names.
	 * 
	 * @return A reference to the newly-created TablePane.
	 * @see SQLTable#inherit
	 * @see PlayPenLayout#addLayoutComponent(Component,Object)
	 */
	public synchronized TablePane importTableCopy(SQLTable source, Point preferredLocation, DuplicateProperties duplicateProperties, boolean assignTypes) throws SQLObjectException {
	    SQLTable newTable;
	    switch (duplicateProperties.getDefaultTransferStyle()) {
	    case REVERSE_ENGINEER:
	        newTable = source.createInheritingInstance(session.getTargetDatabase()); // adds newTable to db
	        break;
	    case COPY:
	        newTable = source.createCopy(session.getTargetDatabase(), duplicateProperties.isPreserveColumnSource());
	        break;
	    default:
	        throw new IllegalStateException("Unknown transfer style " + duplicateProperties.getDefaultTransferStyle());
	    }
	    
	    //need to add data sources as necessary if a SQLObject was copied and pasted from one session
	    //to another in the same context. Also need to correct the source columns to point to the 
	    //correct session's source database objects.
	    for (SQLColumn column : newTable.getColumns()) {
	        SQLColumn sourceColumn = newTable.getColumnByName(column.getName());
	        ASUtils.correctSourceColumn(sourceColumn, duplicateProperties, column, getSession().getDBTree());
	    }

	    // Although this method is called in AddObjectsTask.cleanup(), it
        // remains here so that tests will use it as well. Columns that have
        // upstream types are ignored, so this is safe.
	    if (assignTypes) {
	        String platform;
	        if (source.getParentDatabase() != null && source.getParentDatabase().getDataSource() != null) {
	            platform = source.getParentDatabase().getDataSource().getParentType().getName();
	        } else {
	            platform = SQLTypePhysicalPropertiesProvider.GENERIC_PLATFORM;
	        }
	        SQLColumn.assignTypes(newTable.getColumns(), newTable.getParentDatabase().getDataSource().getParentCollection(), platform, getSession());
	    }
	    boolean isAlreadyOnPlaypen = false;
		
		// ensure tablename is unique
		if (logger.isDebugEnabled()) logger.debug("before add: " + tableNames); //$NON-NLS-1$
		int suffix = uniqueTableSuffix(source.getName());
		if (suffix != 0) {
		    String newName = source.getName() + "_" + suffix;
		    newTable.setName(newName);
		    isAlreadyOnPlaypen = true;
		}
		if (logger.isDebugEnabled()) logger.debug("after add: " + tableNames); //$NON-NLS-1$

		TablePane tp = new TablePane(newTable, getContentPane());
		logger.info("adding table "+newTable); //$NON-NLS-1$
		addImpl(tp, preferredLocation);
		tp.revalidate();

		if (duplicateProperties.getDefaultTransferStyle() == TransferStyles.REVERSE_ENGINEER) {
		    createRelationshipsFromPP(source, newTable, true, isAlreadyOnPlaypen, suffix);
		    createRelationshipsFromPP(source, newTable, false, isAlreadyOnPlaypen, suffix);
		}
		return tp;
	}
	
	public int uniqueTableSuffix(String base) {
	    int suffix = 0;
	    if (!tableNames.add(base.toLowerCase())) {
            boolean done = false;
            while (!done) {
                suffix++;
                done = tableNames.add(base.toLowerCase() + "_" + suffix); //$NON-NLS-1$
            }
        }
	    return suffix;
	}

	/**
     * Creates exported relationships if the importing tables exist in the
     * PlayPen if isPrimaryKeyTableNew is set to true. Otherwise, it creates
     * imported relationships if the exporting tables exist in the PlayPen if
     * isPrimaryKeyTableNew is set to false.
     * 
     * @param source
     *            SQLTable representation of the table in the source database
     * @param newTable
     *            Newly created SQLTable instance into where the relationships
     *            are copied.
     * @param isPrimaryKeyTableNew
     *            Adds exported key relationships if true, imported keys if
     *            false.
     * @param isAlreadyOnPlaypen
     *            If the new table is already on playpen, its name will be changed
     *            Then we need to make sure its relationships don't point to the old
     *            tables
     * @param suffix
     *            Indicating the number of the copies of the table we have already
     *            on the playpen
     * @throws SQLObjectException
     */
    private void createRelationshipsFromPP(SQLTable source, SQLTable newTable, boolean isPrimaryKeyTableNew, boolean isAlreadyOnPlaypen, int suffix) throws SQLObjectException {
        PlayPenContentPane contentPane;
        if (this.contentPane instanceof PlayPenContentPane) {
            contentPane = (PlayPenContentPane) this.contentPane;
        } else {
            throw new IllegalStateException("Must have a PlayPenContent to make releationships: this PlayPen has " + this.contentPane.getClass().getName());
        }
        // create exported relationships if the importing tables exist in pp
		Iterator<SQLRelationship> sourceKeys = null;
        if (isPrimaryKeyTableNew) {
            sourceKeys = source.getExportedKeys().iterator();
        } else {
            sourceKeys = SQLRelationship.getExportedKeys(source.getImportedKeys()).iterator();
        }
		while (sourceKeys.hasNext()) {
			SQLRelationship r = sourceKeys.next();
			
			// If relationship is self-referencing, then don't add it twice.
			if (r.getFkTable().equals(r.getPkTable()) && !isPrimaryKeyTableNew) continue;
			
			if (logger.isInfoEnabled()) {
				logger.info("Looking for fk table "+r.getFkTable().getName()+" in playpen"); //$NON-NLS-1$ //$NON-NLS-2$
			}

            TablePane tablePane =  null;
            
            if(!isAlreadyOnPlaypen) {
                if (isPrimaryKeyTableNew){
                    tablePane =findTablePaneByName(r.getFkTable().getName());
                } else {
                    tablePane =findTablePaneByName(r.getPkTable().getName());
                }
            }
            else {
                if (isPrimaryKeyTableNew){
                    tablePane =findTablePaneByName(r.getFkTable().getName()+"_"+suffix); //$NON-NLS-1$
                } else {
                    tablePane =findTablePaneByName(r.getPkTable().getName()+"_"+suffix); //$NON-NLS-1$
                }
            }

			if (tablePane != null) {
				logger.info("FOUND IT!"); //$NON-NLS-1$

				SQLRelationship newRel = new SQLRelationship();
				newRel.updateToMatch(r, true);
				
				SQLTable oldTable;
				
				if (r.getFkTable().equals(r.getPkTable())) {
    			    // Prevents relationships from attaching to the wrong table
                    // if a table with a self referencing relationship gets
                    // imported twice.
    			    oldTable = newTable;
				} else {
				    oldTable = tablePane.getModel();
				}
				
				if (isPrimaryKeyTableNew) {
				    newRel.attachRelationship(newTable,oldTable,false);
				} else {
				    newRel.attachRelationship(oldTable,newTable,false);
				}
				
				addImpl(new Relationship(newRel, contentPane), null);

				Iterator<? extends SQLObject> mappings = r.getChildren().iterator();
				while (mappings.hasNext()) {
					SQLRelationship.ColumnMapping m
						= (SQLRelationship.ColumnMapping) mappings.next();
					setupMapping(newTable, oldTable, newRel, m,isPrimaryKeyTableNew);
				}
			} else {
				logger.info("NOT FOUND"); //$NON-NLS-1$
			}
		}
    }

    private void setupMapping(SQLTable newTable, SQLTable otherTable, SQLRelationship newRel, SQLRelationship.ColumnMapping m, boolean newTableIsPk) throws SQLObjectException {
        SQLColumn pkCol = null;
        SQLColumn fkCol = null;

        if (newTableIsPk) {
            pkCol=newTable.getColumnByName(m.getPkColumn().getName());
            fkCol=otherTable.getColumnByName(m.getFkColumn().getName());

            if (pkCol == null) {
                // this shouldn't happen
                throw new IllegalStateException("Couldn't find pkCol "+m.getPkColumn().getName()+" in new table"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            if (fkCol == null) {
                // this might reasonably happen (user deleted the column)
                return;
            }
        } else {
            pkCol=otherTable.getColumnByName(m.getPkColumn().getName());
            fkCol=newTable.getColumnByName(m.getFkColumn().getName());
            if (fkCol == null) {
                // this shouldn't happen
                throw new IllegalStateException("Couldn't find fkCol "+m.getFkColumn().getName()+" in new table"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            if (pkCol == null) {
                // this might reasonably happen (user deleted the column)
                return;
            }
        }

        fkCol.addReference();
        SQLRelationship.ColumnMapping newMapping
        	= new SQLRelationship.ColumnMapping();
        newMapping.setPkColumn(pkCol);
        newMapping.setFkColumn(fkCol);
        newRel.addChild(newMapping);
    }

	/**
	 * Calls {@link #importTableCopy} for each table contained in the given schema.
	 */
	public synchronized void addObjects(List<SQLObject> list, Point preferredLocation, SPSwingWorker nextProcess, TransferStyles transferStyle) throws SQLObjectException {
		ProgressMonitor pm
		 = new ProgressMonitor(this,
		                      Messages.getString("PlayPen.copyingObjectsToThePlaypen"), //$NON-NLS-1$
		                      "...", //$NON-NLS-1$
		                      0,
			                  100);
		
		AddObjectsTask t = new AddObjectsTask(list,
				preferredLocation, pm, session, transferStyle);
		t.setNextProcess(nextProcess);
		new Thread(t, "Objects-Adder").start(); //$NON-NLS-1$
	}

	protected class AddObjectsTask extends SPSwingWorker {
		
        private List<SQLObject> sqlObjects;
		private Point preferredLocation;
		private String errorMessage = null;
		private ProgressMonitor pm;

        private final TransferStyles transferStyle;

		public AddObjectsTask(List<SQLObject> sqlObjects,
				Point preferredLocation,
				ProgressMonitor pm,
                ArchitectSwingSession session,
                TransferStyles transferStyle) {
            super(session);
			this.sqlObjects = sqlObjects;
			this.preferredLocation = preferredLocation;
            this.transferStyle = transferStyle;
			ProgressWatcher.watchProgress(pm, this);
			this.pm = pm;
		}

		/**
		 * Combines the MonitorableWorker's canceled flag with the
		 * ProgressMonitor's.
		 */
		@Override
		public synchronized boolean isCancelled() {
			return super.isCancelled() || pm.isCanceled();
		}

		/**
		 * Makes sure all the stuff we want to add is populated.
		 */
		public void doStuff() {
			logger.info("AddObjectsTask starting on thread "+Thread.currentThread().getName()); //$NON-NLS-1$
			session.getArchitectFrame().getContentPane().setCursor(new Cursor(Cursor.WAIT_CURSOR));
			
			try {
			    Iterator<SQLObject> soIt = sqlObjects.iterator();
			    // first pass: Cause all of the SQLObjects between the given 
			    // ones and the table descendents to populate...
			    while (soIt.hasNext() && !isCancelled()) {
			        SQLObject so = soIt.next();
			        SQLObjectUtils.countTablesSnapshot(so);
			    }
			} catch (SQLObjectException e) {
                logger.error("Unexpected exception during populate", e); //$NON-NLS-1$
                setDoStuffException(e);
                errorMessage = "Unexpected exception during populate: " + e.getMessage(); //$NON-NLS-1$
            }
			
			//Second pass: count the tables. Done in the foreground to 
			//wait for the objects to be fully populated by pass 1.
			session.runInForeground(new Runnable() {
			    public void run() {
			        try {
			            int tableCount = 0;
			            Iterator<SQLObject> soIt = sqlObjects.iterator();
			            while (soIt.hasNext() && !isCancelled()) {
			                SQLObject so = soIt.next();
			                tableCount += SQLObjectUtils.countTablesSnapshot(so);
			            }
			            setJobSize(new Integer(tableCount));
			        } catch (SQLObjectException e) {
			            logger.error("Unexpected exception, objects should be populated by " +
			            		"this pass.", e); //$NON-NLS-1$
			            setDoStuffException(e);
			            errorMessage = "Unexpected exception, objects should be populated " +
			            		"by this pass: " + e.getMessage(); //$NON-NLS-1$
			        }
			    }
			});

			ensurePopulated(sqlObjects);

			logger.info("AddObjectsTask done"); //$NON-NLS-1$
		}

		/**
		 * Ensures the given objects and all their descendants are populated from the database before returning, unless
		 * this worker gets cancelled.
         * 
         * This method is normally called from a worker thread, so don't use any swing API on it.
		 *
		 * @param so
		 */
		private void ensurePopulated(List<? extends SQLObject> soList) {
			for (SQLObject so : soList) {
				if (isCancelled()) break;
				if (so instanceof SQLTable) {
				    //pushing updates to foreground as population happens on the foreground
				    //and this will keep the progress bar more honest with what is happening.
				    session.runInForeground(new Runnable(){
                        public void run() {
                            setProgress(getProgress() + 1);                            
                        }
                    });
				}
                ensurePopulated(so.getChildren());
			}
		}

		/**
		 * Displays error messages or invokes the next process in the chain on a new
		 * thread. The run method asks swing to invoke this method on the event dispatch
		 * thread after it's done.
		 */
		public void cleanup() {
			if (getDoStuffException() != null) {
                ASUtils.showExceptionDialogNoReport(session.getArchitectFrame(),
                        errorMessage, getDoStuffException());
				if (getNextProcess() != null) {
					setCancelled(true);
				}
			}

			session.getPlayPen().startCompoundEdit("Drag to Playpen"); //$NON-NLS-1$
			
			// Filter out objects that would lose ETL lineage against the user's will.
			ImportSafetyChecker checker = new ImportSafetyChecker(session);
			sqlObjects = checker.filterImportedItems(sqlObjects);		
			
			session.getPlayPen().getContentPane().begin("Drag to Playpen");
			try {

				// reset iterator
				Iterator<SQLObject> soIt = sqlObjects.iterator();

				// Track all columns added so we can assign types
				ArrayListMultimap<String, SQLColumn> addedColumns = ArrayListMultimap.create();
				
				resetTableNames();
				while (soIt.hasNext() && !isCancelled()) {
					SQLObject someData = soIt.next();
					DuplicateProperties duplicateProperties = ASUtils.createDuplicateProperties(getSession(), someData);
					if (transferStyle == TransferStyles.COPY && duplicateProperties.isCanCopy()) {
					    duplicateProperties.setDefaultTransferStyle(transferStyle);
					} else if (transferStyle == TransferStyles.REVERSE_ENGINEER && duplicateProperties.isCanReverseEngineer()) {
					    duplicateProperties.setDefaultTransferStyle(transferStyle);
					}
					
					if (someData instanceof SQLTable) {
						TablePane tp = importTableCopy((SQLTable) someData, preferredLocation, duplicateProperties, false);
						setMessage(ArchitectUtils.truncateString(((SQLTable)someData).getName()));
                        preferredLocation.x += tp.getPreferredSize().width + 5;
                        
                        SQLDatabase dbAncestor = SQLPowerUtils.getAncestor(someData, SQLDatabase.class);
                        String platform;
                        if (dbAncestor == null) {
                            platform = null;
                        } else {
                            platform = dbAncestor.getDataSource().getParentType().getName();
                        }
                        addedColumns.putAll(platform, tp.getModel().getChildren(SQLColumn.class));
                        
                        increaseProgress();
					} else if (someData instanceof SQLSchema) {
						SQLSchema sourceSchema = (SQLSchema) someData;
						Iterator<? extends SQLObject> it = sourceSchema.getChildren().iterator();
						while (it.hasNext() && !isCancelled()) {
                            Object nextTable = it.next();
							SQLTable sourceTable = (SQLTable) nextTable;
							setMessage(ArchitectUtils.truncateString(sourceTable.getName()));
							TablePane tp = importTableCopy(sourceTable, preferredLocation, duplicateProperties, false);
							preferredLocation.x += tp.getPreferredSize().width + 5;
							
							String platform = SQLPowerUtils.getAncestor(someData, SQLDatabase.class).getDataSource().getParentType().getName();
	                        addedColumns.putAll(platform, tp.getModel().getChildren(SQLColumn.class));
							
							increaseProgress();
						}
					} else if (someData instanceof SQLCatalog) {
						SQLCatalog sourceCatalog = (SQLCatalog) someData;
						Iterator<? extends SQLObject> cit = sourceCatalog.getChildren().iterator();
						if (sourceCatalog.isSchemaContainer()) {
							while (cit.hasNext() && !isCancelled()) {
								SQLSchema sourceSchema = (SQLSchema) cit.next();
								Iterator<? extends SQLObject> it = sourceSchema.getChildren().iterator();
								while (it.hasNext() && !isCancelled()) {
									Object nextTable = it.next();
                                    SQLTable sourceTable = (SQLTable) nextTable;
									setMessage(ArchitectUtils.truncateString(sourceTable.getName()));
									TablePane tp = importTableCopy(sourceTable, preferredLocation, duplicateProperties, false);
									preferredLocation.x += tp.getPreferredSize().width + 5;
									
									String platform = SQLPowerUtils.getAncestor(someData, SQLDatabase.class).getDataSource().getParentType().getName();
		                            addedColumns.putAll(platform, tp.getModel().getChildren(SQLColumn.class));
									
									increaseProgress();
								}
							}
						} else {
							while (cit.hasNext() && !isCancelled()) {
                                Object nextTable = cit.next();
								SQLTable sourceTable = (SQLTable) nextTable;
								setMessage(ArchitectUtils.truncateString(sourceTable.getName()));
								TablePane tp = importTableCopy(sourceTable, preferredLocation, duplicateProperties, false);
								preferredLocation.x += tp.getPreferredSize().width + 5;
								
								String platform = SQLPowerUtils.getAncestor(someData, SQLDatabase.class).getDataSource().getParentType().getName();
	         
