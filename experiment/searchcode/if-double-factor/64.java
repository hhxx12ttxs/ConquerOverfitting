package uk.ac.lkl.migen.system.expresser.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.awt.geom.*;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import uk.ac.lkl.common.ui.SwingUtilities2;

import uk.ac.lkl.common.util.Location;
import uk.ac.lkl.common.util.JREXMLUtilities;
import uk.ac.lkl.common.util.config.MiGenConfiguration;
import uk.ac.lkl.common.util.datafile.DataFile;
import uk.ac.lkl.common.util.event.UpdateEvent;
import uk.ac.lkl.common.util.event.UpdateListener;
import uk.ac.lkl.common.util.expression.Expression;
import uk.ac.lkl.common.util.expression.LocatedExpression;

import uk.ac.lkl.common.util.value.Number;

import uk.ac.lkl.migen.system.ExpresserLauncher;
import uk.ac.lkl.migen.system.MiGenContext;
import uk.ac.lkl.migen.system.expresser.model.*;
import uk.ac.lkl.migen.system.expresser.model.shape.block.BlockShape;
import uk.ac.lkl.migen.system.expresser.model.shape.block.ModelGroupShape;
import uk.ac.lkl.migen.system.expresser.model.shape.block.PatternShape;
import uk.ac.lkl.migen.system.expresser.model.tiednumber.TiedNumberExpression;
import uk.ac.lkl.migen.system.expresser.model.tiednumber.TiedNumberExpressionProxy;
import uk.ac.lkl.migen.system.expresser.model.event.*;
import uk.ac.lkl.migen.system.expresser.model.exception.ExpresserModelXMLException;

import uk.ac.lkl.migen.system.expresser.ui.ecollaborator.ActivityDocument;
import uk.ac.lkl.migen.system.expresser.ui.uievent.ExpressionDragEndEvent;
import uk.ac.lkl.migen.system.expresser.ui.uievent.ShapeProxyDragEndEvent;
import uk.ac.lkl.migen.system.expresser.ui.uievent.TiedNumberDragEndEvent;
import uk.ac.lkl.migen.system.expresser.ui.uievent.UIEventManager;
import uk.ac.lkl.migen.system.expresser.ui.view.AbstractExpressedObjectView;
import uk.ac.lkl.migen.system.expresser.ui.view.AbstractLocatedObjectView;
import uk.ac.lkl.migen.system.expresser.ui.view.shape.block.BasicShapeView;
import uk.ac.lkl.migen.system.util.MiGenUtilities;

/**
 * Carries information about how an object is rendered on the screen.
 * 
 * A canvas for rendering objects of a certain type. Can be intermediate node in
 * hierarchy of course.
 * 
 * Objects must be located.
 * 
 * @author $Author: toontalk@gmail.com $
 * @version $Revision: 12010 $
 * @version $Date: 2012-12-02 19:07:58 +0100 (Sun, 02 Dec 2012) $
 * 
 */
public abstract class ObjectSetCanvas	
       extends ObjectSetView 
       implements SwingUtilities2.PreferredSizeListener {

    private static final int SELECTION_OUTER_PATTERN_BORDER_WIDTH = 4;

    private static final int SELECTION_BORDER_WIDTH = 3;
    
    protected static Color crossColor = new Color(166, 20, 177); // purplish

    private boolean gridShowing;
    
    private boolean addZoomButtons;
    
    private boolean mirrorChangesInMyWorld;

    // grid size in pixels
    protected int gridSize;

    // number of grid subdivisions
    private int gridSubdivisions;

    private ArrayList<CanvasBehaviour> behaviours;

    // x offset for negative values in model
    private int xOffset;

    // y offset for negative values in model
    private int yOffset;
    
    // the slave universe has a canvas that for example creates property lists 
    // that are appropriate for readOnly
    private boolean readOnly = false;
    
    private boolean dirtyManifests = false;

    private Rectangle totalViewBounds = new Rectangle();
    
    // white background is important for transparency to look right
    protected Color backgroundColor = Color.WHITE;
    
    // for implementing show where this is used in activity documents
    protected ArrayList<ExpresserModel> modelsOfHighlightedProxies =
	new ArrayList<ExpresserModel>();
    
    // for highlighting tied number panels 
    private ArrayList<java.util.Timer> timersOfHighlightedTiedNumberPanels =
	new ArrayList<java.util.Timer>();
    
    // for efficient repaint
    private HashMap<BlockShape, Rectangle> screenArea = new HashMap<BlockShape, Rectangle>();
        
    private static BasicStroke outerPatternStroke = 
	new BasicStroke(4, 
		        BasicStroke.CAP_BUTT,
		        BasicStroke.JOIN_BEVEL,
		        0,
		        new float[] {5}, 
		        0);
    
    private static BasicStroke highlightStroke = new BasicStroke(2);
    private static BasicStroke temporaryHighlightStroke = new BasicStroke(4);
    
    private static Color highlightColor = Color.MAGENTA;
    
    protected static Color temporaryHighlightColors[] = 
        {Color.YELLOW, Color.ORANGE, Color.RED, Color.ORANGE};
    
    private javax.swing.Timer highlightTimer = null;

    private int highlightCounter = 0;
    
    // for replay of previously saved files (usually the autosaved ones)
    private File[] autoSaveFiles = null;
    private int autoSaveIndex = -1;
       
    private DropTargetListener dropTargetListener = new DropTargetListener() {

	    @Override
	    public void dragEnter(DropTargetDragEvent dtde) {
		Transferable transferable = dtde.getTransferable();
		if (transferable.isDataFlavorSupported(TiedNumberPanel.getFlavor()) ||
		    transferable.isDataFlavorSupported(ExpressionPanel.getFlavor()) ||
		    transferable.isDataFlavorSupported(TileButton.getFlavor()) ||
		    transferable.isDataFlavorSupported(ShapeProxy.getFlavor())) {
		    dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
		}
	    }

	    @Override
	    public void dragExit(DropTargetEvent dte) {
	    }

	    @Override
	    public void dragOver(DropTargetDragEvent dtde) {
	    }

	    @Override
	    @SuppressWarnings("unchecked")
	    public void drop(DropTargetDropEvent dtde) {
		ExpressionPanel.setDragStarted(false);
		Transferable transferable = dtde.getTransferable();
		Point dropLocation = dtde.getLocation();
		Component dropTargetComponent = 
		    dtde.getDropTargetContext().getDropTarget().getComponent();
		if (dropTargetComponent != ObjectSetCanvas.this) {
		    // this listener is also added to "drop forwarders" 
		    // such as ShapeProxy
		    // dropLocation needs to be translated to my coordinates
		    dropLocation = 
			SwingUtilities.convertPoint(dropTargetComponent, 
				                    dropLocation, 
				                    ObjectSetCanvas.this);
		}
//		Rectangle bounds = ObjectSetCanvas.this.getBounds();
//		ObjectSetCanvas.constrainWithInBounds(dropLocation, bounds);
		try {
		    if (transferable.isDataFlavorSupported(TiedNumberPanel.getFlavor())) {
			TiedNumberPanel<Number> tiedNumberPanel = 
			    (TiedNumberPanel<Number>) 
			    transferable.getTransferData(TiedNumberPanel.getFlavor());
			Container tiedNumberParent = tiedNumberPanel.getParent();
			TiedNumberPanel<Number> numberPanelStartingDrag = 
			    tiedNumberPanel.getNumberPanelStartingDrag();
			Container sourceContainer = null;
			if (numberPanelStartingDrag != null) {
			    sourceContainer = numberPanelStartingDrag.getParent();
			}
			if (tiedNumberParent != ObjectSetCanvas.this) {
			    TiedNumberPanel<Number> tiedNumberPanelToAdd;
			    ExpressionPanel<Number> dragee = tiedNumberPanel.getDragee();
			    if (dragee == null) {
				// is already a copy but copy again to fix Issue 600
				// tried many more straight-forward fixes and 
				// mysteriously the name field remained invisible
				if (tiedNumberParent != null) {
				    tiedNumberParent.remove(tiedNumberPanel);
				}
				if (numberPanelStartingDrag != null && 
				    sourceContainer == ObjectSetCanvas.this) {
				    sourceContainer.remove(numberPanelStartingDrag);
				}
				// don't create a proxy here
				// only if dropped on a document canvas
				// no longer read only
//				getModel().removeLocatedExpression(tiedNumberPanel.getLocatedExpression());
				tiedNumberPanelToAdd = tiedNumberPanel.createTiedNumberPanelCopy(false);
//				getModel().addLocatedExpression(tiedNumberPanelToAdd.getLocatedExpression());
			    } else {
				tiedNumberPanelToAdd = tiedNumberPanel;
			    }    
			    tiedNumberDropped(tiedNumberPanelToAdd, 
				              sourceContainer,
				              tiedNumberPanel.isReadOnly(), 
				              dropLocation);
			}
		    } else if (transferable.isDataFlavorSupported(ExpressionPanel.getFlavor())) {
			ExpressionPanel<Number> expressionPanel = 
			    (ExpressionPanel<Number>) transferable.getTransferData(ExpressionPanel.getFlavor());
			if (expressionPanel.getParent() != ObjectSetCanvas.this) {
			    JPanel panelStartingDrag = 
				expressionPanel.getPanelStartingDrag();
			    boolean sameCanvas = false;
			    if (panelStartingDrag != null) {
				sameCanvas = partOfSameCanvas(panelStartingDrag);
				Component whoToRemoveDueToMove = 
				    whoToRemoveDueToMove(panelStartingDrag);
				if (whoToRemoveDueToMove != null) {
				    ObjectSetCanvas.this.remove(whoToRemoveDueToMove);
				}
			    }
			    expressionDropped(expressionPanel, dropLocation, sameCanvas);
			}
		    } else if (transferable.isDataFlavorSupported(ShapeProxy.getFlavor())) {
			ShapeProxy shapeProxy = 
			    (ShapeProxy) transferable.getTransferData(ShapeProxy.getFlavor());
			ShapeProxy shapeProxyStartingDrag = shapeProxy.getShapeProxyStartingDrag();
			boolean sameCanvas = 
			    shapeProxyStartingDrag != null && 
			    shapeProxyStartingDrag.getParent() == ObjectSetCanvas.this;
			if (sameCanvas) {
			    shapeProxyStartingDrag.getParent().remove(shapeProxyStartingDrag);
			} else if (shapeProxyStartingDrag != null) {
			    shapeProxyStartingDrag.setVisible(true);
			}
			addShapeProxy(shapeProxy, dropLocation, sameCanvas);
			UIEventManager.processEvent(
				new ShapeProxyDragEndEvent(shapeProxy.getId().toString(), getId().toString()));
//		    } else if (transferable.isDataFlavorSupported(TileButton.getFlavor())) {
			// handled in TileButton.
		    }    
		    dtde.dropComplete(true);
		} catch (Exception e) {
		    e.printStackTrace();
		    dtde.rejectDrop();
		}
		ExpresserLauncher.repaint();
	    }

	    @Override
	    public void dropActionChanged(DropTargetDragEvent dtde) {
	    }
	    
	};
    
    private Color gridColor = Color.LIGHT_GRAY;
    
    public class ThumbNailObjectPainter extends ObjectPainter {

	private ThumbNailObjectPainter() {
	    // private ctor so that only sub-classes of ObjectSetView can create
	    // instances
	}

	@Override
	public void paintObject(Graphics2D g2, BlockShape shape) {
	    translateToThumbNailShape(g2);
	    ObjectSetCanvas.this.paintObjects(g2);
	    ObjectSetCanvas.this.paintColorGrid(g2);
	}
    }
    
    public ObjectPainter createObjectThumbNailPainter() {
	return new ThumbNailObjectPainter();
    }

    protected Component whoToRemoveDueToMove(JPanel panelStartingDrag) {
	Container parent = panelStartingDrag.getParent();
	if (parent == this) {
	    return panelStartingDrag;
	}
	if (panelStartingDrag instanceof TiedNumberPanel<?> &&
	    parent instanceof ExpressionPanel<?>) {
	    Container grandparent = parent.getParent();
	    if (grandparent == this) {
		return parent;
	    }
	}
	return null;
    }

    protected boolean partOfSameCanvas(JPanel panelStartingDrag) {
	Container panelStartingDragParent = panelStartingDrag.getParent();
	if (panelStartingDragParent == this) {
	    // same canvas
	    return true;
	}
	if (isAncestorOf(panelStartingDragParent)) {
	    // e.g. panel starting drag was in a property list
	    return true;
	}
	GlobalColorAllocationPanel containingGlobalColorAllocationPanel = 
	    containingGlobalColorAllocationPanel(panelStartingDragParent);
	if (containingGlobalColorAllocationPanel != null) {
	    // panel came from the rule area (of this canvas presumably)
	    return containingGlobalColorAllocationPanel.getCanvas() == this;
	}
	return false;
    }

    public static GlobalColorAllocationPanel containingGlobalColorAllocationPanel(Container container) {
	if (container == null) {
	    return null;
	}
	if (container instanceof GlobalColorAllocationPanel) {
	    return (GlobalColorAllocationPanel) container;
	}
	Container parent = container.getParent();
	if (parent != null) {
	    return containingGlobalColorAllocationPanel(parent);
	}
	return null;
    }
    
    protected static void constrainWithInBounds(Point point, Rectangle bounds) {
	// moves point minimally (horizontally and vertically) to be
	// inside of the bounds
	if (point.getX() < bounds.getMinX()) {
	    point.x = (int) (bounds.getMinX());
	} else if (point.getX() > bounds.getMaxX()) {
	    point.x = (int) (bounds.getMaxX());
	}
	if (point.getY() < bounds.getMinY()) {
	    point.y = (int) (bounds.getMinY());
	} else if (point.getY() > bounds.getMaxY()) {
	    point.y = (int) (bounds.getMaxY());
	}
    }
    
    public ObjectSetCanvas(ExpresserModel model, int gridSize) {
	this(model, gridSize, 1);
    }

    public ObjectSetCanvas(ExpresserModel model, int gridSize, int gridSubdivisions) {
	super(ObjectSetCanvas.class, model);
	setLayout(null);
	this.behaviours = new ArrayList<CanvasBehaviour>();
	setGridShowing(true);
	setGridSize(gridSize);
	setGridSubdivisions(gridSubdivisions);

	xOffset = 0;
	yOffset = 0;
    }

    public void addUserInterface() {
	addModelListeners();
	enableKeyboardCommands();
	if (MiGenConfiguration.isEnableReplay()) {
	    enableReplay();
	}
    }

    protected void addModelListeners() {
	addMouseListener(new MouseAdapter() {

	    @Override
	    public void mouseClicked(MouseEvent e) {
		requestFocusInWindow();
	    }
	});

	addUpdateListener(new UpdateListener<ObjectSetView>() {
	    @Override
	    public void objectUpdated(UpdateEvent<ObjectSetView> e) {
		repaint();
	    }
	});

	// hack: should do better than repaint all
	getModel().addObjectUpdateListener(new UpdateListener<BlockShape>() {
	    @Override
	    public void objectUpdated(UpdateEvent<BlockShape> e) {
		BlockShape object = e.getSource();
		repaintObject(object);
	    }
	});
	
	getModel().addObjectListener(new ObjectListener() {

	    @Override
	    public void objectAdded(ObjectEvent e) {
	    }

	    @Override
	    public void objectRemoved(ObjectEvent e) {
		removeManifestOf(e.getObject());		
	    }

	});
	
	ContainerListener containerListener = new ContainerListener() {

	    @SuppressWarnings("unchecked")
	    @Override
	    public void componentAdded(ContainerEvent e) {
		Component component = e.getChild();
		if (component instanceof ExpressionPanel<?>) {
		    getModel().addLocatedExpression(((ExpressionPanel<Number>) component).getLocatedExpression());
		} else if (component instanceof TiedNumberPanel<?>) {
		    getModel().addLocatedExpression(((TiedNumberPanel<Number>) component).getLocatedExpression());
		}
	    }

	    @SuppressWarnings("unchecked")
	    @Override
	    public void componentRemoved(ContainerEvent e) {
		Component component = e.getChild();
		if (component instanceof ExpressionPanel<?>) {
		    getModel().removeLocatedExpression(((ExpressionPanel<Number>) component).getLocatedExpression());
		} else if (component instanceof TiedNumberPanel<?>) {
		    getModel().removeLocatedExpression(((TiedNumberPanel<Number>) component).getLocatedExpression());
		}
	    }
	    
	};
	addContainerListener(containerListener);
    }

    protected void removeManifestOf(BlockShape shape) {
	int componentCount = getComponentCount();
	for (int i = 0; i < componentCount; i++) {
	    Component component = getComponent(i);
	    if (component instanceof AttributeManifest) {
		AttributeManifest manifest = (AttributeManifest) component;
		if (manifest.getShape() == shape) {
//		    remove(manifest);
		    manifest.dispose();
		    removeManifestOf(shape); // look for more
		    return;
		}    
	    }
	}
    }

    private void repaintObject(BlockShape object) {
	AbstractLocatedObjectView view =
		(AbstractLocatedObjectView) getView(object);
	if (view == null) {
	    // For example, can happen while debugging switching between 
	    // painting the canvas and Eclipse
	    return;
	}
	Rectangle bounds = view.getViewBounds();
	// offsets negative
	bounds.x -= xOffset;
	bounds.y -= yOffset;
	repaintScreenArea(bounds);
    }

    private void repaintScreenArea(Rectangle bounds) {
	// hack to hard-wire selection boundaries
//	repaint(bounds.x - 4, bounds.y - 4, bounds.width + 8, bounds.height + 8);
	// for selection boundaries
	// they are wider than before because they grown to avoid drawing over shapes
	int borderWidth = 2*Math.max(SELECTION_OUTER_PATTERN_BORDER_WIDTH, SELECTION_BORDER_WIDTH);
	bounds.grow(borderWidth, borderWidth); 
	repaint(bounds);
	refreshManifestsIfDirty();
    }

    protected void refreshManifestsIfDirty() {
	if (dirtyManifests) {
	    dirtyManifests = false;
	    refreshManifests();
	}
    }

    public void refreshManifests() {
	int componentCount = getComponentCount();
	for (int i = 0; i < componentCount; i++) {
	    Component component = getComponent(i);
	    if (component instanceof AttributeManifest) {
		AttributeManifest manifest = (AttributeManifest) component;
		manifest.refreshManifest();
	    }
	}
	dirtyManifests = false;
    }

    // uses a ghost view
    public Area getStrictBoundingBox(BlockShape object) {
	AbstractExpressedObjectView nullView = getNullView(object);
	// todo: don't cast. Use generics.
	return ((AbstractLocatedObjectView) nullView)
		.getStrictBoundingArea(object);
    }

    // uses a ghost view
    public boolean strictlyContains(BlockShape object, int x, int y) {
	AbstractExpressedObjectView nullView = getNullView(object);
	// todo: don't cast. Use generics.
	return ((AbstractLocatedObjectView) nullView).strictlyContains(object, x, y);
    }

    public void addBehaviour(CanvasBehaviour behaviour) {
	behaviours.add(behaviour);
	// hack: always true for now
	behaviour.setActive(true);
    }

    public Rectangle getTotalViewBounds() {
	return new Rectangle(totalViewBounds);
    }

    @Override
    protected void processAttributeChanged(AttributeChangeEvent<BlockShape> e) {
	// could be more efficient and only listen to x/y/width/height changes
	Rectangle totalViewBounds = new Rectangle();
	for (AbstractExpressedObjectView view : getViews()) {
	    // hack to cast - pass view type to base-class as generic param
	    AbstractLocatedObjectView locatedView =
		(AbstractLocatedObjectView) view;
	    Rectangle viewBounds = locatedView.getViewBounds();
	    totalViewBounds = totalViewBounds.union(viewBounds);
	}
	Component parent = getParent();
	if (!isPreferredSizeFixed()) {
	    xOffset = Math.min(totalViewBounds.x, 0);
	    yOffset = Math.min(totalViewBounds.y, 0);

	    // at least as big as parent with offsets taken into consideration

	    int preferredWidth;
	    int preferredHeight;
	    if (parent == null) {
		preferredWidth = totalViewBounds.width;
		preferredHeight = totalViewBounds.height;
	    } else {
		preferredWidth = Math.max(totalViewBounds.width, parent.getWidth()-xOffset);
		preferredHeight = Math.max(totalViewBounds.height, parent.getHeight()-yOffset);
	    }
	    setPreferredSize(new Dimension(preferredWidth, preferredHeight));
	}
	this.totalViewBounds.setRect(totalViewBounds);

	invalidate();
	if (parent != null)
	    parent.validate(); // hack
    }

    protected boolean isPreferredSizeFixed() {
	// overriden by subclasses whose preferred size should not change
	// e.g. thumb nails.
	return false;
    }

    public final void setGridSize(int gridSize) {
	if (gridSize <= 0)
	    return;

	if (gridSize == this.gridSize)
	    return;

	this.gridSize = gridSize;
	fireObjectUpdated();
    }

    public boolean isGridShowing() {
	return gridShowing;
    }

    public final void setGridShowing(boolean gridShowing) {
	if (gridShowing == this.gridShowing)
	    return;

	this.gridShowing = gridShowing;
	fireObjectUpdated();
    }

    public final void setGridSubdivisions(int gridSubdivisions) {
	if (gridSubdivisions <= 0)
	    return;

	if (gridSubdivisions == this.gridSubdivisions)
	    return;
	this.gridSubdivisions = gridSubdivisions;
	fireObjectUpdated();
    }

    @Override
    protected void processViewAdded(AbstractExpressedObjectView view) {
	// initialise view area hashmap entry for efficient redraw
	Rectangle bounds =
		((AbstractLocatedObjectView) view).getViewBounds();
	screenArea.put(view.getObject(), bounds);

	// repaint appropriate part
	repaintScreenArea(bounds);
    }

    @Override
    protected void processViewRemoved(AbstractExpressedObjectView view) {
	if (view != null) {
	    Rectangle bounds = screenArea.remove(view.getObject());
	    repaintScreenArea(bounds);
	}
    }
    
    @Override
    public void paint(Graphics g) {
	super.paint(g);
	paintGridAndHighlights((Graphics2D) g.create());
	// need to repaint expressions and property lists since they are on top of the shapes
	paintChildren(g);
    }

    protected void paintGridAndHighlights(Graphics2D g2) {
	// hack: only object painting is translated. Need to use getOrigin()
	// more consistently and not do this at all really.
	g2.translate(-xOffset, -yOffset);
	paintColorGrid(g2);
	// paint tiles first and then grid so
	// that grid lines are never obscured 
	if (!MiGenConfiguration.isPatternsDrawnOverGrid()) {
	    paintGrid((Graphics2D) g2.create());
	}
	paintTemporaryHighlightedBoundaries((Graphics2D) g2.create());
	paintSelectionBoundaries((Graphics2D) g2.create());
	paintHighlightedBoundaries((Graphics2D) g2.create());
	paintBehaviourMarks((Graphics2D) g2.create());
    }

    @Override
    public void paintComponent(Graphics g) {
	super.paintComponent(g);
	Graphics2D g2 = (Graphics2D) g.create(); // Why create this so many times? 
	paintBackground(g2);
	if (MiGenConfiguration.isPatternsDrawnOverGrid()) {
	    paintGrid((Graphics2D) g2.create());
	}
	paintObjects((Graphics2D) g2.create());
    }

    private void paintBehaviourMarks(Graphics2D g2) {
	for (CanvasBehaviour behaviour : behaviours)
	    behaviour.paintMarks(g2);
    }

    protected void paintObjects(Graphics2D g2) {
	getModel().clearColorGridMap();
	ExpresserModel model = getModel();
	Collection<BlockShape> shapes = model.getShapes();
	if (!MiGenConfiguration.isNoColourAllocation()) {
	    ArrayList<ModelColor> incorrectlyAllocatedColors = new ArrayList<ModelColor>();
	    ModelGroupShape modelAsAGroup = model.getModelAsAGroup();
	    ExpresserModel modelWithGrid = getModelWithGrid();
	    Rectangle viewRect = getViewRectangle();
	    uk.ac.lkl.common.util.Rectangle viewRectangle = 
		viewRect == null ? null :
		                   new uk.ac.lkl.common.util.Rectangle(viewRect.x, viewRect.y, viewRect.width, viewRect.height);
	    if (isMirrorChangesInMyWorld()) {
		modelAsAGroup.addToIncorrectColorAllocations(incorrectlyAllocatedColors);
		modelAsAGroup.updateColorGrid(modelWithGrid, 0, 0, true, incorrectlyAllocatedColors, false, viewRectangle);
	    } else {
		// do the following after all color usage computed
		// maintains a list of tiles that are at each grid location
		for (BlockShape shape : shapes) {
		    incorrectlyAllocatedColors = new ArrayList<ModelColor>();
		    shape.updateColorGrid(modelWithGrid, 0, 0, true, incorrectlyAllocatedColors, true, viewRectangle);
		}
	    }
	    xOffset = getModel().getxOffset();
	    yOffset = getModel().getyOffset();
	    if (modelAsAGroup != null) {
		// need to provide rule feedback 
		provideRuleFeedback(modelAsAGroup);
	    }
	}
    }
    
    protected ExpresserModel getModelWithGrid() {
	// overriden by ShapeProxy
	return getModel();
    }

    /**
     * @return the viewing rectangle in grid coordinates
     */
    public Rectangle getViewRectangle() {
	JViewport viewPort = getViewPort();
	Rectangle viewRect = viewPort == null ? null : viewPort.getViewRect();
	if (viewRect != null) {
	    // convert to grid coordinates but make it one larger in all directions
	    // to avoid round off problems
	    viewRect.width = 2+viewRect.width/gridSize;
	    viewRect.height = 2+viewRect.height/gridSize;
	    viewRect.x = viewRect.x/gridSize;
	    viewRect.y = viewRect.y/gridSize;
	    viewRect.translate(-1, -1);
	}
	return viewRect;
    }

    protected void paintColorGrid(Graphics2D g2) {
	Color grayWithOpacity = getGrayWithOpacity();
	getModel().setOverlapPainted(false); // unless proven otherwise below
	getModel().setNegativeTilePainted(false);
	Map<Location, ArrayList<AllocatedColor>> colorGridMap = getModel().getColorGridMap();
	Set<Entry<Location, ArrayList<AllocatedColor>>> coloredLocations = colorGridMap.entrySet();
	for (Entry<Location, ArrayList<AllocatedColor>> coloredLocation : coloredLocations) {
	    Location point = coloredLocation.getKey();
	    ArrayList<AllocatedColor> colors = coloredLocation.getValue();
	    if (colors == null || colors.isEmpty()) {
		// nothing to do -- can this happen?
	    } else {
		int viewX = point.x * gridSize;
		int viewY = point.y * gridSize;
		if (colors.size() == 1) {
		    AllocatedColor allocatedColor = colors.get(0);
		    if (allocatedColor.isNegative()) {
			getModel().setNegativeTilePainted(true);
			Color blackColor;
			if (allocatedColor.isCorrectlyAllocated()) {
			    blackColor = Color.BLACK;
			} else {
			    blackColor = Color.DARK_GRAY;
			}
			BasicShapeView.paintUncoloredTile(
				g2, gridSize, viewX, viewY, gridSize, gridSize, blackColor, allocatedColor);
//			g2.setColor(blackColor);
//			g2.fillRect(viewX, viewY, gridSize, gridSize);
//			g2.setColor(color);
//			g2.fillRect(viewX+xOffset, viewY+yOffset, innerWidth, innerHeight);
		    } else if (allocatedColor.isCorrectlyAllocated()) {
			Color color = new Color(allocatedColor.getRGB());
			paintColoredTile(g2, gridSize, viewX, viewY, color);
		    } else {
			BasicShapeView.paintUncoloredTile(
				g2, gridSize, viewX, viewY, gridSize, gridSize, grayWithOpacity, allocatedColor);
		    }
		} else {
		    getModel().setOverlapPainted(true);
		    g2.setColor(crossColor);
		    g2.setStroke(new BasicStroke(gridSize/4));
		    g2.drawLine(viewX, viewY, viewX+gridSize, viewY+gridSize);
		    g2.drawLine(viewX+gridSize, viewY, viewX, viewY+gridSize);
		}
	    }
	}
    }

    /**
     * @param g2
     * @param gridSize
     * @param viewX
     * @param viewY
     * @param color
     */
    public void paintColoredTile(Graphics2D g2, int gridSize, int viewX, int viewY, Color color) {
	g2.setColor(color);
	g2.fillRect(viewX, viewY, gridSize, gridSize);
    }

    protected void provideRuleFeedback(ModelGroupShape modelAsAGroup) {
	BlockShapeCanvasPanel selectedMasterPanel = ExpresserLauncher.getSelectedMasterPanel();
	if (selectedMasterPanel == null) {
	    // presumably not in eXpresser
	    return;
	}
	GlobalColorAllocationPanel globalAllocationPanel = 
	    selectedMasterPanel.getGlobalAllocationPanel(0);
	if (globalAllocationPanel != null) {
	    ModelColor color = globalAllocationPanel.getSelectedColor();
	    boolean correct;
	    if (color == null) {
		correct = modelAsAGroup.isTotalTileExpressionCorrect();
	    } else {
		correct = modelAsAGroup.allLocalColorAllocationsCorrect(color);
	    }
	    selectedMasterPanel.provideCorrectnessFeedback(null, !correct);
	    modelAsAGroup.logChangesInAllocationCorrectness(correct);
	}
    }

    private void paintSelectionBoundaries(Graphics2D g2) {
	Collection<BlockShape> selectedObjects = getModel().getSelectedObjects();
	paintColourBoundaries(g2, selectedObjects, Color.BLACK, Color.WHITE);
    }
    
    protected void paintHighlightedBoundaries(Graphics2D g2) {
	paintHighlights(g2, getModel().getHighlightedShapes(), highlightStroke, highlightColor, false, true);
    }
    
    protected void paintTemporaryHighlightedBoundaries(final Graphics2D g2) {
	ExpresserModel model = getModel();
	final Collection<BlockShape> temporaryHighlightedShapes = model.getTemporaryHighlightedShapes();
	if (temporaryHighlightedShapes.isEmpty()) {
	    if (highlightTimer != null) {
		highlightTimer.stop();
		highlightTimer = null;
	    }
	} else {
	    if (highlightTimer == null) {
		ActionListener actionListener = new ActionListener() {

		    @Override
		    public void actionPerformed(ActionEvent e) {
			repaint();
		    }

		};
		highlightTimer = new javax.swing.Timer(100, actionListener);
		highlightTimer.start();
	    }
	    paintHighlights(
		    g2, 
		    temporaryHighlightedShapes, 
		    temporaryHighlightStroke, 
		    temporaryHighlightColors[highlightCounter++%temporaryHighlightColors.length],
	            true,
	            false);
	}	
    }
  
    // FIXME: @Hack(who = "MM", why = "blockTaskPresentation", issues = 0)

    @Override
    public void paintColourBoundaries(
	    Graphics2D g2, 
	    Collection<BlockShape> selectedObjects, 
	    Color outerColor, 
	    Color innerColor) {
	// store bounds in list since doing this in two-pass algorithm. Don't
	// want to get them twice since not currently cached.
	ArrayList<Area> boundingAreas = new ArrayList<Area>();
	ArrayList<Area> patternOuterBoundingAreas = new ArrayList<Area>();

	// should be caching clip somehow I think
	for (BlockShape selectedObject : selectedObjects) {
	    // todo: maybe use view type as generic arg to prevent cast?
	    AbstractLocatedObjectView view =
		    (AbstractLocatedObjectView) getView(selectedObject);
	    if (view != null) {
		if (selectedObject instanceof PatternShape) {
		    PatternShape selectedPattern = (PatternShape) selectedObject;
		    Area subBoundingBox = null;
		    subBoundingBox = getStrictBoundingBox(selectedPattern.getShape());
		    boundingAreas.add(subBoundingBox);
	 	    //creates the outer bounding box of the whole pattern 
		    if (selectedObject instanceof PatternShape && 
			((PatternShape) selectedObject).getIterations().intValue() > 1) {
			Area patternBoundingArea = new Area(view.getViewBounds(selectedObject));
			patternOuterBoundingAreas.add(patternBoundingArea);
		    }
		}
	    }
	}
	BasicStroke outerStroke = new BasicStroke(SELECTION_BORDER_WIDTH);
	BasicStroke innerStroke = new BasicStroke(SELECTION_BORDER_WIDTH/3);
	for (Area boundingArea : boundingAreas) {
	    Rectangle bounds = boundingArea.getBounds();
	    // expand bounds by half of outer stroke width
	    // so selection feedback doesn't overwrite any of the shapes
	    bounds.grow(SELECTION_BORDER_WIDTH/2, SELECTION_BORDER_WIDTH/2);
	    g2.setStroke(outerStroke);
	    g2.setColor(outerColor);
	    g2.draw(bounds);
	    g2.setStroke(innerStroke);
	    g2.setColor(innerColor);
	    g2.draw(bounds);
	}
	g2.setStroke(outerPatternStroke);
	g2.setColor(Color.GRAY);
	for (Area boundingArea : patternOuterBoundingAreas) {
	    Rectangle bounds = boundingArea.getBounds();
	    // expand bounds by outerPatternStroke width
	    // so selection feedback doesn't overwrite any of the shapes
	    int lineWidth = (int) outerPatternStroke.getLineWidth();
	    bounds.grow(lineWidth, lineWidth);
	    // drawing a dashed rectangle millions of pixels across is VERY slow
	    // since Java isn't clipping cleverly need to crop the bounds
	    Window windowAncestor = SwingUtilities.getWindowAncestor(this);
	    Dimension frameSize = windowAncestor.getSize();
	    bounds.width = Math.min(frameSize.width, bounds.width);
	    bounds.height = Math.min(frameSize.height, bounds.height);
	    g2.draw(bounds);
	}
    }
    
    public void paintHighlights(
	    Graphics2D g2, 
	    Collection<BlockShape> highlightedShapes, 
	    BasicStroke stroke, 
	    Color color,
	    boolean shadeEachShapeDifferently,
	    boolean exteriorHighlight) {
	ArrayList<Area> patternOuterBoundingAreas = new ArrayList<Area>();
	for (BlockShape shape : highlightedShapes) {
	    if (getNullView(shape) != null) {
		Area strictBoundingBox = getStrictBoundingBox(shape);
		patternOuterBoundingAreas.add(strictBoundingBox);
	    }
	}
	g2.setStroke(stroke);
	g2.setColor(color);
	double factor = 1.0;
	// factor should range from 1.0 to .5
	double factorDelta = 0.5/highlightedShapes.size();
	for (Area boundingArea : patternOuterBoundingAreas) {
	    Rectangle bounds = boundingArea.getBounds();
	    if (bounds.getWidth() > 0 && bounds.getHeight() > 0) {
		// if exteriorHighlight expand bounds by stroke width
		// so selection feedback doesn't overwrite any of the shapes
		int lineWidth = (int) stroke.getLineWidth();
		if (exteriorHighlight) {
		    bounds.grow(lineWidth, lineWidth);
		} else {
		    bounds.grow(lineWidth/-2, lineWidth/-2);
		}
		if (shadeEachShapeDifferently) {
		    Color newColor = 
			new Color(
				Math.max((int)(color.getRed()  *factor), 0), 
				Math.max((int)(color.getGreen()*factor), 0),
				Math.max((int)(color.getBlue() *factor), 0));
		    g2.setColor(newColor);
		    factor -= factorDelta;
		}
		g2.draw(bounds);
	    }
	}
    }
   
    private void paintBackground(Graphics2D g2) {
	Dimension size = getSize();
	g2.setColor(backgroundColor);
	g2.fillRect(0, 0, size.width, size.height);
    }

    // todo: cache image
    protected void paintGrid(Graphics2D g2) {
	int gridSize = getGridSize();
	if (gridSize < 10)
	    return;

	Dimension size = getSize();
	if (isGridShowing()) {
	    paintGridSubdivisions((Graphics2D) g2.create());
	    g2.setColor(getGridColor());
	    g2.setStroke(new BasicStroke(1.5f));
	    for (int i = 0; i < size.width; i += gridSize)
		g2.drawLine(i, 0, i, size.height);
	    for (int j = 0; j < size.height; j += gridSize)
		g2.drawLine(0, j, size.width, j);
	}
    }

    // todo: cache image
    private void paintGridSubdivisions(Graphics2D g2) {
	if (getGridSubdivisions() == 1)
	    return;

	int gridSize = getGridSize();
	int gridSubdivisions = getGridSubdivisions();
	if (gridSubdivisions <= 1)
	    return;
	Number step = new Number(gridSize, gridSubdivisions);
	g2.setColor(gridColor);
	g2.setStroke(new BasicStroke(1.0f));
	Dimension size = getSize();
	for (Number i = new Number(0); 
	     i.isLessThan(new Number(size.width)); 
	     i = i.add(new Number(step)))
	    g2.drawLine(i.intValue(), 0, i.intValue(), size.height);
	for (Number j = new Number(0); 
	     j.isLessThan(new Number(size.height)); 
	     j = j.add(new Number(step)))
	    g2.drawLine(0, j.intValue(), size.width, j.intValue());
    }

    public int getGridSize() {
	return gridSize;
    }

    public int getGridSubdivisions() {
	return gridSubdivisions;
    }

    // hack: scrollpane should be integrated into this class
    public JViewport getViewPort() {
	Component parent = getParent();
	if (!(parent instanceof JViewport))
	    return null;
	return (JViewport) parent;
    }

    public Point getCanvasOrigin() {
	return new Point(xOffset, yOffset);
    }

    // get the model point corresponding to the point in the given mouse event
    public Point getModelPoint(MouseEvent e) {
	return getModelPoint(e.getPoint());
    }

    public Point getModelPoint(Point point) {
	return getModelPoint(point.x, point.y);
    }

    // note: xOffset and yOffset are negative (or zero)
    public Point getModelPoint(int x, int y) {
	x += xOffset;
	y += yOffset;
	int modelX = x / gridSize;
	int modelY = y / gridSize;
	return new Point(modelX, modelY);
    }

    public BlockShape getEnclosingObject(MouseEvent e) {
	return getEnclosingObject(e.getPoint());
    }

    public BlockShape getEnclosingObject(Point point) {
	return getEnclosingObject(point.x, point.y);
    }

    public BlockShape getNextUnselectedObject(int viewX, int viewY) {
	viewX += xOffset;
	viewY += yOffset;
	Collection<AbstractExpressedObjectView> views = getViews();

	boolean selectedFound = false;
	// hack so can revert to this if none found or lowest is currently
	// selected
	BlockShape firstEnclosingObject = null;

	for (AbstractExpressedObjectView view : views) {
	    // hack due to issue with bounds in getViews (or the
	    // ObjectSetView class more generally)
	    AbstractLocatedObjectView locatedView =
		    (AbstractLocatedObjectView) view;
	    Rectangle viewBounds = locatedView.getViewBounds();

	    if (!viewBounds.contains(viewX, viewY))
		continue;

	    if (firstEnclosingObject == null)
		firstEnclosingObject = view.getObject();

	    boolean selected = view.getObject().isSelected();

	    if (selected) {
		if (selectedFound) {
		    continue;
		} else {
		    selectedFound = true;
		    continue;
		}
	    } else {
		if (selectedFound)
		    return view.getObject();
	    }
	}

	if (firstEnclosingObject != null)
	    return firstEnclosingObject;

	return null;
    }

    // null if not inside any
    // todo: make this return Collection<AbstractLocatedObjectView>.
    public BlockShape getEnclosingObject(int viewX, int viewY) {
	viewX += xOffset;
	viewY += yOffset;
	Collection<AbstractExpressedObjectView> views = getViews();
	for (AbstractExpressedObjectView view : views) {
	    // hack due to issue with bounds in getViews (or the
	    // ObjectSetView
	    // class more generally)
	    AbstractLocatedObjectView locatedView =
		    (AbstractLocatedObjectView) view;
	    if (locatedView.strictlyContains(viewX, viewY)) {
		// Rectangle viewBounds = locatedView.getViewBounds();
		// if (viewBounds.contains(viewX, viewY))
		// return view.getObject();
		return view.getObject();
	    }
	}
	return null;
    }

    /**
     * Clears all behaviours of the canvas.
     * 
     * The original use of this method was preventing the user from performing
     * actions on "slave" universes, in a convenient way.
     */
    public void clearAllBehaviours() {
	for (CanvasBehaviour behaviour : behaviours) {
	    behaviour.setActive(false);
	}
	behaviours.clear();
    }
    
    public void enableDrop() {
	new DropTarget(this, dropTargetListener);
	final TransferHandler transferHandler = new TransferHandler() {

	    @Override
	    public boolean canImport(JComponent comp, DataFlavor[] drageeDataFlavors) {
		return true;
	    }

	    @Override
	    public boolean importData(JComponent comp, Transferable t) {
		return true;
	    }

	    @Override
	    public int getSourceActions(JComponent c) {
		return TransferHandler.MOVE;
	    }

	    @Override
	    public Transferable createTransferable(JComponent c) {
		return null;
	    }
	    
	};
	setTransferHandler(transferHandler);
    }

    @Deprecated
    // We no longer support dragging from My Model to the Computer's Model
    protected void addShapeProxy(ShapeProxy shapeProxy, Point dropLocation, boolean sameCanvas) {
	// rather than add the proxy to the canvas this adds
	// the shape it represents because this is a master canvas
	Container parent = shapeProxy.getParent();
	if (parent != null) {
	    parent.remove(shapeProxy);
	}
	BlockShape shape = shapeProxy.getOriginalShapeCopy();
	// round to nearest grid location
	shape.moveTo(new Number((dropLocation.x+gridSize/2)/gridSize),
	             new Number((dropLocation.y+gridSize/2)/gridSize));
	getModel().addObject(shape);
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }
    
    public boolean overlapAt(int x, int y) {
	Location location = new Location(x, y);
	Map<Location, ArrayList<AllocatedColor>> colorGridMap = getModel().getColorGridMap();
	ArrayList<AllocatedColor> colors = colorGridMap.get(location);
	return colors != null && colors.size() > 1;
    }
    
    /**
     * @param x
     * @param y
     * @return the list of remaining colors at x, y.     
     * Equal numbers of positive and negative versions of the same color are not included.
     */
    public List<AllocatedColor> getColorsAt(int x, int y) { 
	Map<Location, ArrayList<AllocatedColor>> colorGridMap = getModel().getColorGridMap();
	Location location = new Location(x, y);
	ArrayList<AllocatedColor> colors = colorGridMap.get(location);    
	if (colors == null) {     
	    return new ArrayList<AllocatedColor>();     
	} else {
	    return colors;      
	}
    }
    
    /**
     * @param x
     * @param y
     * @return the list of shapes that have at least one tile at x, y.     
     */
    public List<BlockShape> getShapesAt(int x, int y) { 
	Rectangle viewRectangle = getViewRectangle();
	Point location = new Point(x, y);
	if (viewRectangle.contains(location)) {
	    Map<Location, ArrayList<BlockShape>> shapeGridMap = getModel().getShapeGridMap();
	    ArrayList<BlockShape> shapes = shapeGridMap.get(new Location(x, y));
	    if (shapes == null) {
		return new ArrayList<BlockShape>();
	    } else {
		return shapes;
	    }
	} else {
	    return getModel().getShapesAt(x, y);
	}
    }
    
    public AllocatedColor getColorOfGrid(int x, int y) {
	Location location = new Location(x, y);
	Map<Location, ArrayList<AllocatedColor>> colorGridMap = getModel().getColorGridMap();
	ArrayList<AllocatedColor> colors = colorGridMap.get(location);
	if (colors == null) {
	    return AllocatedColor.WHITE; // don't paint anything (or paint white) -- nothing here
	}
	int size = colors.size();
	if (size == 0) {
	    return AllocatedColor.WHITE; // don't paint anything -- the colors balance
	}
	AllocatedColor color = colors.get(0);
	for (int i = 1; i < size; i++) {
	    if (!color.equals(colors.get(i))) {
		// can't combine different colors
		return null;
	    }
	}
	// make it darker the more copies on the same location
	// and make completely opaque
	int red = color.getRed();
	int green = color.getGreen();
	int blue = color.getBlue();
	final int darkness = 64;
	for (int i = 1; i < size; i++) {
	    if (red <= darkness && green <= darkness && blue <= darkness) {
		break; // getting too dark
	    }
	    red -= darkness;
	    green -= darkness;
	    blue -= darkness;
	}
	if (red < 0) {
	    red = 0;
	}
	if (green < 0) {
	    green = 0;
	}
	if (blue < 0) {
	    blue = 0;
	}
	return new AllocatedColor(
		red, green, blue, 255, color.isNegative(), color.getName(), color.isCorrectlyAllocated());
    }

    public boolean isDirtyManifests() {
        return dirtyManifests;
    }

    public void setDirtyManifests(boolean dirtyManifests) {
        this.dirtyManifests = dirtyManifests;
        if (dirtyManifests) {
            Runnable refreshLater = new Runnable() {

		@Override
		public void run() {
		    refreshManifestsIfDirty();		    
		}
        	
            };
	    SwingUtilities.invokeLater(refreshLater);
        }
    }
    
    public List<Component> getComponentsOfClass(Class<?> desiredClass) {
	ArrayList<Component> components = new ArrayList<Component>();
	int componentCount = getComponentCount();
	for (int i = 0; i < componentCount; i++) {
	    Component component = getComponent(i);
	    if (desiredClass.isInstance(component)) {
		components.add(component);    
	    }
	}
	return components;
    }
    
//    public ObjectSetCanvas findMasterCanvas() {
//	if (!getModel().isSlaved()) {
//	    return this;
//	} else {
//	    Container parent = getParent();
//	    Container ancestor = parent;
//	    while (ancestor != null) {
//		if (ancestor instanceof ObjectCanvasPanel) {
//		    ObjectCanvasPanel objectCanvasPanel = (ObjectCanvasPanel) ancestor;
//		    return objectCanvasPanel.getMasterPanel().getCanvas();
//		}
//		ancestor = ancestor.getParent();
//	    }
//	    return null;
//	}
//    }
//    
//    public ObjectSetCanvas findSlaveCanvas() {
//	if (getModel().isSlaved()) {
//	    return this;
//	} else {
//	    Container parent = getParent();
//	    Container ancestor = parent;
//	    while (ancestor != null) {
//		if (ancestor instanceof ObjectCanvasPanel) {
//		    ObjectCanvasPanel objectCanvasPanel = (ObjectCanvasPanel) ancestor;
//		    return objectCanvasPanel.getSlavePanel().getCanvas();
//		}
//		ancestor = ancestor.getParent();
//	    }
//	    return null;
//	}
//    }

    public DropTargetListener getDropTargetListener() {
        return dropTargetListener;
    }

    protected void tiedNumberDropped(TiedNumberPanel<Number> tiedNumberPanelToAdd, 
	                             Container sourceContainer, 
	                             boolean readOnly, 
	                             Point dropLocation) {
	TiedNumberExpression<Number> tiedNumberExpression = 
	    tiedNumberPanelToAdd.getTiedNumberExpression();
	TiedNumberExpression<Number> replacementForTiedNumber = 
	    replacementForTiedNumber(tiedNumberExpression);
	tiedNumberPanelToAdd.setTiedNumber(replacementForTiedNumber);
	String name = tiedNumberPanelToAdd.getTiedNumberName().trim();
	boolean hasName = !name.equals(TiedNumberPanel.NAME_IF_NO_NAME_GIVEN);
	TiedNumberExpression<?> anotherWithName = null;
	if (hasName) {
	    anotherWithName = isNameInUse(tiedNumberPanelToAdd.getTiedNumberExpression());
	}
//	boolean nameConflictButDifferentAuthors = isNameInUse(tiedNumberExpression, true) != null;
	add(tiedNumberPanelToAdd);
	tiedNumberPanelToAdd.setDragCreatesCopy(false);
	tiedNumberPanelToAdd.setLocation(dropLocation);
	tiedNumberPanelToAdd.setDragEnabled(true);
	if (anotherWithName != null) {
//	    if (nameConflictButDifferentAuthors) {
//		tiedNumberExpression.setDisplayAuthorInfo(true);
//		anotherWithName.setDisplayAuthorInfo(true);
//	    } else {
		tiedNumberPanelToAdd.createDialog("NameInUse");
		tiedNumberPanelToAdd.editName();
//	    }
	}
	UIEventManager.processEvent(new TiedNumberDragEndEvent(tiedNumberPanelToAdd.getId().toString(), getId().toString()));
    }

    protected void expressionDropped(
	    ExpressionPanel<Number> expressionPanel, Point dropLocation, boolean sameCanvas) {
	ExpressionPanel<Number> copy = expressionPanel.createExpressionPanelCopy(false);
	if (!sameCanvas && !expressionPanel.displayCurrentValue()) {
	    replaceTiedNumbers(copy);
	}
	add(copy);
	UIEventManager.processEvent(new ExpressionDragEndEvent(copy.getId().toString(), getId().toString()));
	copy.setDragCreatesCopy(false);
	copy.setLocation(dropLocation);
	copy.setDragEnabled(true);
	copy.setSize(copy.getPreferredSize());
//	getModel().addLocatedExpression(copy.getLocatedExpression());
//	getModel().removeLocatedExpression(expressionPanel.getLocatedExpression());
    }
    
    protected void replaceTiedNumbersWithProxies(
	    ExpressionPanel<Number> expressionPanel, 
	    final boolean replaceWithProxies) {
	StandaloneWalker walker = new StandaloneWalker() {

	    @Override
	    public boolean tiedNumberFound(
		    TiedNumberExpression<?> tiedNumber,
		    BlockShape shape,
		    AttributeHandle<Number> handle,
		    ExpresserModel expresserModel) {
		return true; // nothing to do
	    }
	    
	    @Override
	    public boolean tiedNumberPanelFound(String expressionPanelOfTiedNumberId) {
		ExpressionPanel<Number> expressionPanelOfTiedNumber = 
		    ExpressionPanel.fetchExpressionPanelByIdString(expressionPanelOfTiedNumberId);
		TiedNumberExpression<Number> oldTiedNumberExpression = 
		    expressionPanelOfTiedNumber.getTiedNumberExpression();
		TiedNumberExpressionProxy<Number> proxy = 
		    new TiedNumberExpressionProxy<Number>(oldTiedNumberExpression);
		expressionPanelOfTiedNumber.setExpression(proxy);
		return true;
	    }
	    
	};
	expressionPanel.walkToTiedNumbers(walker);
    }
    
    protected void replaceTiedNumbers(ExpressionPanel<Number> expressionPanel) {
	StandaloneWalker walker = new StandaloneWalker() {

	    @Override
	    public boolean tiedNumberFound(
		    TiedNumberExpression<?> tiedNumber,
		    BlockShape shape,
		    AttributeHandle<Number> handle,
		    ExpresserModel expresserModel) {
		return true; // nothing to do
	    }
	    
	    @Override
	    public boolean tiedNumberPanelFound(String expressionPanelOfTiedNumberId) {
		ExpressionPanel<Number> expressionPanelOfTiedNumber = 
		    ExpressionPanel.fetchExpressionPanelByIdString(expressionPanelOfTiedNumberId);
		TiedNumberExpression<Number> oldTiedNumberExpression = 
		    expressionPanelOfTiedNumber.getTiedNumberExpression();
		TiedNumberExpression<Number> replacementNumber = 
		    replacementForTiedNumber(oldTiedNumberExpression);
		expressionPanelOfTiedNumber.setExpression(replacementNumber);
		return true;
	    }
	    
	};
	expressionPanel.walkToTiedNumbers(walker);
    }
    
    public TiedNumberExpression<Number> replacementForTiedNumber(
	    TiedNumberExpression<Number> tiedNumberExpression) {
	// returns this or if a proxy the original value
	// subclass DocumentCanvas creates a replacement proxy
	return tiedNumberExpression.getOriginal();
    }

    public void setManifestLocation(BlockShape shape, AttributeManifest manifest) {
	// hack to cast here -- need to pass in view type to base class
	AbstractLocatedObjectView view =
	    (AbstractLocatedObjectView) getView(shape);
	if (view == null) {
	    return;
	}
	Rectangle bounds = view.getViewBounds();

	JViewport viewport = getViewPort();
	Rectangle viewRect = viewport==null ? bounds : viewport.getViewRect();

	setManifestLocation(manifest, bounds, viewRect);
    }

    public void setManifestLocation(AttributeManifest manifest,
	                            Rectangle shapeBounds, 
	                            Rectangle canvasViewPortBounds) {
	// desired location relative to canvas
	int locationX = shapeBounds.x;
	int locationY = shapeBounds.y + shapeBounds.height;

	manifest.setSize(manifest.getPreferredSize());
	manifest.validate();
	Dimension manifestSize = manifest.getSize();

	// do +width and +height first so that if view rect is smaller than
	// attribute manifest then still aligns sensibly
	if ((locationX + manifestSize.width) > (canvasViewPortBounds.x + canvasViewPortBounds.width))
	    locationX = canvasViewPortBounds.x + canvasViewPortBounds.width - manifestSize.width;
	if ((locationY + manifestSize.height) > (canvasViewPortBounds.y + canvasViewPortBounds.height))
	    locationY = canvasViewPortBounds.y + canvasViewPortBounds.height - manifestSize.height;
	if (locationX < 0) {
	    locationX = 0;
	}
	if (locationY < 0) {
	    locationY = 0;
	}
	// + 2 to be below selection rect...
	manifest.setLocation(locationX, locationY + 2);
    }

    public Color getGridColor() {
        return gridColor;
    }

    public void setGridColor(Color gridColor) {
        this.gridColor = gridColor;
    }
    
    /**
     * @param oldModel -- model with the old state and located expressions
     * @param newModel -- model with the new state and located expressions
     * Old model's state is updated to the state of the new model
     */
    
    public void replaceModelWith(ExpresserModel oldModel, ExpresserModel newModel) {
	oldModel.removeAllObjects();
	removeAll();
	addExpressionPanels(newModel.getLocatedExpressions(), false);
	oldModel.transferStateFrom(newModel);
	ObjectCanvasPanel.updateAllocationPanels();
    }
    
    public boolean walkToTiedNumbers(StandaloneWalker walker, boolean includeOpenAttributeManifests) {
	// first walk the model
	ExpresserModel model = getModel();
	if (model.walkToTiedNumbers(walker)) {
	    // then the model as a group to access global color allocations
	    ModelGroupShape modelAsAGroup = model.getModelAsAGroup();
	    if (modelAsAGroup == null || modelAsAGroup.walkToTiedNumbers(walker, null, null, model)) {
		// and then the canvas
		if (walkContainerToTiedNumbers(this, walker, includeOpenAttributeManifests)) {
		    // walk to total (and other global) expressions   
		    ObjectCanvasPanel containingPanel = getContainingObjectCanvasPanel();
		    if (containingPanel != null) {
			ArrayList<GlobalColorAllocationPanel> globalColorAllocationPanels = containingPanel.getGlobalColorAllocationPanels();
			for (GlobalColorAllocationPanel globalColorAllocationPanel : globalColorAllocationPanels) {
			    if (!globalColorAllocationPanel.getExpressionPanel().walkToTiedNumbers(walker)) {
				return false;
			    }
			}
		    }
		}
	    }
	}
	return true;
    }

    private ObjectCanvasPanel getContainingObjectCanvasPanel() {
	Container ancestor = getParent();
	while (ancestor != null) {
	    if (ancestor instanceof ObjectCanvasPanel) {
		return ((ObjectCanvasPanel) ancestor);
	    }
	    ancestor = ancestor.getParent();
	}
	return null;
    }

    @SuppressWarnings("unchecked")
    public static boolean walkContainerToTiedNumbers(
	    Container container, 
	    StandaloneWalker walker, 
	    boolean includeOpenAttributeManifests) {
	Component[] components = container.getComponents();
	for (Component component : components) {
	    if (component instanceof TiedNumberPanel<?> && container instanceof ExpressionPanel<?>) {
		// container is an expression containing just a tied number
		ExpressionPanel<Number> expressionPanel = (ExpressionPanel<Number>) container;
		expressionPanel.register();
		if (!walker.tiedNumberPanelFound(expressionPanel.getId().toString())) {
		    return false;
		}
	    } else if (component instanceof ExpressionPanel<?>) {
		ExpressionPanel<?> expressionPanel = (ExpressionPanel<?>) component;
		if (!expressionPanel.walkToTiedNumbers(walker)) {
		    return false;
		}
	    } else if (component instanceof AttributeManifestTabbedPane) {
		AttributeManifestTabbedPane tabbedPane = (AttributeManifestTabbedPane) component;
		if (!tabbedPane.walkToTiedNumbers(walker)) {
		    return false;
		}
	    } else if (component instanceof Container && 
		       (includeOpenAttributeManifests || !(component instanceof AttributeManifest))) {
		// don't walk attribute manifests since they will be reached via the shape
		// and don't want to count things twice
		if (!walkContainerToTiedNumbers((Container) component, walker, includeOpenAttributeManifests)) {
		    return false;
		}
	    }
	}
	return true;
    }

    public boolean existsInCanvas(final TiedNumberExpression<?> other) {
	// returns true if other is somewhere in the canvas
	final boolean inUse[] = {false};
	StandaloneWalker walker = new StandaloneWalker() {

	    @Override
	    public boolean tiedNumberFound(
		    TiedNumberExpression<?> tiedNumber, 
		    BlockShape shape, 
		    AttributeHandle<Number> handle,
		    ExpresserModel expresserModel) {
		if (tiedNumber == other) {
		    inUse[0] = true;
		    return false;
		}
		return true;
	    }
	    	    
	};
	walkToTiedNumbers(walker, true);
	return inUse[0];
    }
    
    public int countOccurrencesInCanvas(final TiedNumberExpression<?> other) {
	// returns number of times other occurs in the canvas (including the model)  
	final int count[] = {0};
	StandaloneWalker walker = new StandaloneWalker() {

	    @Override
	    public boolean tiedNumberFound(
		    TiedNumberExpression<?> tiedNumber, 
		    BlockShape shape, 
		    AttributeHandle<Number> handle,
		    ExpresserModel expresserModel) {
		if (tiedNumber == other && expresserModel != null) {
		    // don't count the canvas and model both since the model
		    // includes located expressions
		    count[0]++;
		}
		return true;
	    }
	    	    
	};
	walkToTiedNumbers(walker, false);
	return count[0];
    }
    
    /**
     * No longer supports the byDifferentAuthors flag
     * @param other
     * another tied number possibly with the same name
     * @return
     * true if other tied numbers are using the name 
     * and considerDifferentAuthors is interpreted appropriately
     */
    public TiedNumberExpression<?> isNameInUse(final TiedNumberExpression<?> other) {
	// returns other somewhere in the canvas if it shares the same name
	// considerDifferentAuthors controls whether the author should be the same or different
	final TiedNumberExpression<?> otherWithName[] = {null};
	StandaloneWalker walker = new StandaloneWalker() {

	    @Override
	    public boolean tiedNumberFound(
		    TiedNumberExpression<?> found, 
		    BlockShape shape, 
		    AttributeHandle<Number> handle,
		    ExpresserModel expresserModel) {
		String name = found.getName();
		if (found != other &&
	            name != null &&
		    name.equals(other.getName()) && 
		    !other.getIdString().equals(found.getIdString())) {
		   otherWithName[0] = found;
		   return false;
//		    UserSet author = found.getAuthor();
//		    UserSet otherAuthor = other.getAuthor();
//		    if (author != null &&
//			otherAuthor != null &&
//			(byDifferentAuthors == null ||
//	                 (byDifferentAuthors ? !author.equals(otherAuthor) :
//	        	                       author.equals(otherAuthor)))) {
//			otherWithName[0] = found;
//			return false;
//		    }
		}
		return true;
	    }
	    	    
	};
	walkToTiedNumbers(walker, true);
	return otherWithName[0];
    }

    /**
     * @param tiedNumber
     * @return a tied number panel
     * document canvases override this 
     * to return a tied number panel proxy
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected TiedNumberPanel createTiedNumberPanel(TiedNumberExpression tiedNumber, boolean readOnly) {
	return new TiedNumberPanel(tiedNumber, readOnly);
    }
    
    protected ExpressionPanelProxy<Number> addTiedNumber(
	    TiedNumberExpression<Number> tiedNumberExpression,
	    boolean readOnly) {
	ExpressionPanelProxy<Number> tiedNumberPanel = 
	    new ExpressionPanelProxy<Number>(
		    tiedNumberExpression, readOnly, false);
	add(tiedNumberPanel);
	return tiedNumberPanel;
    }

    /**
     * @param outsideTiedNumberExpression
     * @param readOnly
     * @return a tied number panel with either a fresh tied number
     * or one that was previously created for outsideTiedNumberExpression
     */
    public ExpressionPanelProxy<Number> replacementPanelForOutsideTiedNumber(
	    TiedNumberExpression<Number> tiedNumberExpression,
	    boolean readOnly) {
	return new ExpressionPanelProxy<Number>(tiedNumberExpression.getProxy(), readOnly, false);
    }

//    public TiedNumberExpression<IntegerValue> replacementProxyForTiedNumber(
//	    TiedNumberExpression<IntegerValue> tiedNumberExpression) {
//	tiedNumberExpression = tiedNumberExpression.createFreshCopy();
//	if (tiedNumberExpression.isChangeable()) {
//	    ExpresserModel model = getModel();
//	    if (model != null) {
//		model.addTaskVariable(tiedNumberExpression);
//	    }
//	}
//	tiedNumberExpression.setUnlockable(tiedNumberExpression.isUnlockable());
//	return tiedNumberExpression;
//    }
    
    public void clearHighlighting() {
	ExpresserModel model = getModel();
	if (model != null) {
	    model.clearTemporaryHighlightedShapes();
//	    model.setDirtyModel(true);
	}
	for (ExpresserModel containingModel : modelsOfHighlightedProxies) {
	    containingModel.clearTemporaryHighlightedShapes();
	}
	modelsOfHighlightedProxies.clear();
	for (java.util.Timer timer : timersOfHighlightedTiedNumberPanels) {
	    timer.cancel();
	}
	timersOfHighlightedTiedNumberPanels.clear();
    }

    public ActivityDocument getActivityDocument() {
	// overriden by sub classes
	return null;
    }

    public ArrayList<ExpresserModel> getModelsOfHighlightedProxies() {
        return modelsOfHighlightedProxies;
    }

    public List<java.util.Timer> getTimersOfHighlightedTiedNumberPanels() {
        return timersOfHighlightedTiedNumberPanels;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List<LocatedExpression<Number>> getLocatedExpressions() {
	ArrayList<LocatedExpression<Number>> locatedExpressions =
	    new ArrayList<LocatedExpression<Number>>();
	for (Component component : getComponents()) {
	    if (component instanceof ExpressionPanel<?>) {
		locatedExpressions.add(((ExpressionPanel) component).getLocatedExpression());
	    } else if (component instanceof TiedNumberPanel<?>) {
		locatedExpressions.add(((TiedNumberPanel) component).getLocatedExpression());
	    }
	}
	return locatedExpressions;
    }
    
    public void enableKeyboardCommands() {
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "deleteSelected");
	Action deleteAction = new AbstractAction() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		// create copy of list to avoid concurrent access exceptions
		List<BlockShape> shapes = new ArrayList<BlockShape>(getModel().getSelectedObjects());
		for (BlockShape shape : shapes) {
		    if (!getModel().isUndeletableShape(shape)) {
			getModel().removeObject(shape);
		    }
		}
	    }

	};
	getActionMap().put("deleteSelected", deleteAction);
	getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control Z"), "undoLast");
	Action unconditionalUndoAction = new AbstractAction() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		undo();
	    }

	};
	getActionMap().put("undoLast", unconditionalUndoAction);
	// undo action
	Action undoAction = new AbstractAction() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		if (autoSaveFiles == null) {
		    // undo the last action done
		    undo();
		} else {
		    // move backwards in loaded auto save files
		    loadAutoSavedFile(false);
		}	
	    }

	};
	getActionMap().put("undo", undoAction);
	getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control U"), "undo");
	// redo action
	Action redoAction = new AbstractAction() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		if (autoSaveFiles == null) {
		    // redo the last action undone
		    redo();
		} else {
		    // replay loaded autosave files
		    loadAutoSavedFile(true);
		}
//		if (MiGenConfiguration.isAutoSaveToServer() && MiGenConfiguration.isConnectedToServer()) {
//		    getModel().redo();		    
//		    return;
//		}
//		//if there are no autosaved files in list at beginning 
//		//load them first - this is only convenience
//		if (autoSaveFiles == null) {
//		    selectAutoSavedModelFiles();
//		}
		//load next autos
//		loadAutoSavedFile(true);
	    }

	};
	getActionMap().put("redo", redoAction);
	getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control R"), "redo");
    }
    
    public void enableReplay() {
	// select files action
	Action loadModelFiles = new AbstractAction() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		//prompt to select files 
		selectAutoSavedModelFiles(); 
		//and will play the first if successful
	    }

	};	
	getActionMap().put("selectModelFiles", loadModelFiles);
	getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control L"), "selectModelFiles");
	if (MiGenConfiguration.isEnableReplay()) {    
	    getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control B"), "undoAll");
	    Action undoAllAction = new AbstractAction() {

		@Override
		public void actionPerformed(ActionEvent e) {
		    undoAll();
		}

	    };
	    getActionMap().put("undoAll", undoAllAction);
	}
    }
    
    private void selectAutoSavedModelFiles() {
	JFileChooser fileChooser = new JFileChooser();
	//fileChooser.addChoosableFileFilter(new FolderFileFilter());
	fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
	fileChooser.setMultiSelectionEnabled(true);
	fileChooser.showOpenDialog(this);
	autoSaveFiles = fileChooser.getSelectedFiles();

	if (autoSaveFiles.length == 1) {

	    //if selection is directory
	    if (autoSaveFiles[0].isDirectory()) {
		String dirName = autoSaveFiles[0].getName();

		//set all files in directory as list
		autoSaveFiles = autoSaveFiles[0].listFiles();

		int howMany = autoSaveFiles.length;
		if (howMany > 0) {
		    JOptionPane.showMessageDialog(null,
			    howMany + " files in Folder " + dirName,"Replaying Files", JOptionPane.INFORMATION_MESSAGE);
		} else {
		    JOptionPane.showMessageDialog(null,
			    "No files in Folder","Replaying Files", JOptionPane.ERROR_MESSAGE);
		}
	    }
	} else {
	    int howMany = autoSaveFiles.length;
	    JOptionPane.showMessageDialog(null,
		    howMany + " files selected.","Replaying Files", JOptionPane.INFORMATION_MESSAGE);
	    return;

	}
	if (autoSaveFiles == null) {
	    JOptionPane.showMessageDialog(null,
		    " No files selected.","Replaying Files", JOptionPane.ERROR_MESSAGE);
	    return;
	}
	autoSaveIndex = -1;
	//load the first of these
	loadAutoSavedFile(true);
    }
    

    /**
     * @param next boolean whether loading next or previous
     */
    private void loadAutoSavedFile(boolean next) {
	boolean possible = false;
	if (next) {
	    //load next 
	    if (autoSaveIndex+1 < autoSaveFiles.length) {
		autoSaveIndex++;
		possible = true;
	    }    
	} else {
	    //load previous
	    if (autoSaveIndex > 0) {
		autoSaveIndex--;
		possible = true;
	    } 
	}
	if (possible) {
	    try {
		loadAutoSavedModel(autoSaveFiles[autoSaveIndex]);
		Container ancestor = this;
		while ((ancestor = ancestor.getParent()) != null) {
		    if (ancestor instanceof JFrame) {
			((JFrame) ancestor).setTitle(autoSaveFiles[autoSaveIndex].getName());
			break;
		    }
		}
	    } catch (ExpresserModelXMLException e) {
		e.printStackTrace();
		loadAutoSavedFile(next);
	    }    
	} else {
	    JOptionPane.showMessageDialog(null,
		    "No more files to replay " + (next ? "forwards": "backwards"),"Replaying Files", JOptionPane.ERROR_MESSAGE);
	}
    }

    /**
     * @param restoredLocatedExpressions
     * @param replaceWithProxies -- if true substitute the proxies of the tied numbers before adding
     * 
     * Turns the locatedExpressions into panels on the canvas.
     *  
     */
    public void addExpressionPanels(
	    List<LocatedExpression<Number>> restoredLocatedExpressions,
	    boolean replaceWithProxies) {
	if (restoredLocatedExpressions == null) {
	    return;
	}
	for (LocatedExpression<Number> locatedExpression : restoredLocatedExpressions) {
	    Expression<Number> expression = locatedExpression.getExpression();
	    ExpressionPanel<Number> expressionPanel =
		createExpressionPanel(expression.createCopy(), false, false);
	    expressionPanel.setLocation(locatedExpression.getX(), locatedExpression.getY());
	    if (replaceWithProxies) {
		replaceTiedNumbersWithProxies(expressionPanel, replaceWithProxies);
	    }
	    add(expressionPanel);
	}
    }

    protected ExpressionPanel<Number> createExpressionPanel(
	    Expression<Number> expressionCopy, boolean readOnly, boolean displayCurrentValue) {
	return new ExpressionPanel<Number>(expressionCopy, readOnly, displayCurrentValue);
    }
    
    public void loadXMLDocument(File file) throws ExpresserModelXMLException {
	loadXMLDocument(file, getModel());
    }
    
    /**
     * Loads the XML in the new format generated by ExpresserModelXMLConverter
     * 
     * @param file
     * @param model - model to be updated by contents of file
     * @throws ExpresserModelXMLException 
     */
    public void loadXMLDocument(File file, ExpresserModel model) throws ExpresserModelXMLException {
	try {
	    loadXMLDocument(JREXMLUtilities.createDocument(file), model);
	} catch (ParserConfigurationException e) {
	    e.printStackTrace();
	} catch (SAXException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }
    
    public void loadXMLDocument(DataFile dataFile) {
	loadXMLDocument(dataFile, getModel());
    }
    
    public void loadXMLDocument(DataFile dataFile, ExpresserModel model) {
	// pretty wasteful but don't know how else to work around the limitations of DataFile
	try {
	    String contents = dataFile.readContents();
	    if (contents != null) {
		DocumentBuilder newDocumentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		StringReader stringReader = new StringReader(contents);
		Document document = newDocumentBuilder.parse(new InputSource(stringReader));
		loadXMLDocument(document, model);
	    } 
	} catch (Exception e) {
	    MiGenUtilities.printError("error trying to open " + dataFile);
	    e.printStackTrace();
	} 
    }
       	
    public void loadXMLDocument(Document document, ExpresserModel model) throws ExpresserModelXMLException {
	ExpresserModel newModel;
	newModel = MiGenUtilities.createModelFromXMLDocument(document);
	replaceModelWith(model, newModel);
   }
    
    public void addAutosavedName(String fileName) {
	getModel().addAutosavedName(fileName);
    }
    
    public boolean undo() {
	if (getLastUndoIndex() < 0){
	    if (getAutosavedNames().size() <= 1) {
		return false;
	    }
	    // skip the most recently saved one that 
	    // is the state we are in
	    setLastUndoIndex(getAutosavedNames().size()-2);
	} else if (getLastUndoIndex() == 0) {
	    return false;
	} else {
	    setLastUndoIndex(getLastUndoIndex()-1);
	}
	int savedLastUndoIndex = getLastUndoIndex();
	ArrayList<String> savedAutosavedNames = getAutosavedNames();
	loadAutoSavedModel();
	// loading may make the model dirty and reset the lastUndoIndex and autosavedNames
	setLastUndoIndex(savedLastUndoIndex);
	setAutosavedNames(savedAutosavedNames);
	return true;
    }
    
    /**
     * Undoes everything
     */
    public void undoAll() {
	while (undo()) {};
    }

    private boolean loadAutoSavedModel() {
	// returns true if there was no error
	String savedStateName = getAutosavedNames().get(getLastUndoIndex());
	if (MiGenContext.getServerCommunicator() != null) {
	    ExpresserModel model = 
		MiGenUtilities.fetchModelFromServer(savedStateName, ExpresserLauncher.getUserSet());
	    if (model == null) {
		return false;
	    }
	    ExpresserModel oldModel = getModel();
	    replaceModelWith(oldModel, model);
	    oldModel.setAutoSaveModel(false);
	} else {  
	    File file = new File(savedStateName);
	    try {
		loadAutoSavedModel(file);
	    } catch (ExpresserModelXMLException e) {
		e.printStackTrace();
		return false;
	    }
	}
	return true;
    }

    public void loadAutoSavedModel(File file) throws ExpresserModelXMLException {
	// save it since might be reset by setDirtyModel while loading the model
	int lastUndoIndexSaved = getLastUndoIndex();
//	ExpresserModel savedModel = ExpresserModel.createModelFromXMLDocument(file);
//	removeAllObjects();
//	transferStateFrom(savedModel);
//	List<LocatedExpression<IntegerValue>> locatedExpressions = savedModel.getRestoredLocatedExpressions();
//	this.getAccessToLocatedExpressions().removeAll();
//	this.getAccessToLocatedExpressions().addExpressionPanels(locatedExpressions, false);
	ObjectSetCanvas canvas = ExpresserLauncher.getSelectedPanel().getCanvas();
	canvas.loadXMLDocument(file);
	setLastUndoIndex(lastUndoIndexSaved);
	// ignore changes due to loading previous state that may require auto saving
	canvas.getModel().setAutoSaveModel(false);
    }
    
    public void redo() {
	if (!canRedo()) {
	    return;
	}
	setLastUndoIndex(getLastUndoIndex()+1);
	int lastUndoIndexSaved = getLastUndoIndex();
	ArrayList<String> savedAutosavedNames = getAutosavedNames();
	// following may clobber lastUndoIndex and autosavedNames
	loadAutoSavedModel();
	setLastUndoIndex(lastUndoIndexSaved);
	setAutosavedNames(savedAutosavedNames);
    }

    public boolean canUndo() {
	return getAutosavedNames().size() > 1 && getLastUndoIndex() != 0;
    }
    
    public boolean canRedo() {
	return getLastUndoIndex() >= 0 && getLastUndoIndex() < getAutosavedNames().size()-1;
    }
    
    protected ArrayList<String> getAutosavedNames() {
        return getModel().getAutosavedNames();
    }
    
    protected void setAutosavedNames(ArrayList<String> autosavedNames) {
	getModel().setAutosavedNames(autosavedNames);
    }
    
    protected int getLastUndoIndex() {
	return getModel().getLastUndoIndex();
    }
    
    protected void setLastUndoIndex(int lastUndoIndex) {
	getModel().setLastUndoIndex(lastUndoIndex);
    }

    protected void translateToThumbNailShape(Graphics2D g2) {
//	ThumbNailShapeCanvas.translateToThumbNailShape does the real work
    }

    public boolean isAddZoomButtons() {
        return addZoomButtons;
    }

    public void setAddZoomButtons(boolean addZoomButtons) {
        this.addZoomButtons = addZoomButtons;
    }

    public boolean isMirrorChangesInMyWorld() {
        return mirrorChangesInMyWorld;
    }

    public void setMirrorChangesInMyWorld(boolean mirrorChangesInMyWorld) {
        this.mirrorChangesInMyWorld = mirrorChangesInMyWorld;
    }

}

