/**
 * 
 */
package uk.ac.lkl.migen.system.expresser.ui;

import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;

import uk.ac.lkl.common.util.ID;
import uk.ac.lkl.common.util.IDFactory;
import uk.ac.lkl.common.util.IDObject;
import uk.ac.lkl.common.util.ObjectWithID;
import uk.ac.lkl.common.util.config.MiGenConfiguration;
import uk.ac.lkl.common.util.value.Number;
import uk.ac.lkl.migen.system.ExpresserLauncher;
import uk.ac.lkl.migen.system.expresser.model.ExpresserModel;
import uk.ac.lkl.migen.system.expresser.model.shape.block.BasicShape;
import uk.ac.lkl.migen.system.expresser.model.shape.block.BlockShape;
import uk.ac.lkl.migen.system.expresser.ui.behaviour.AttributeManifestBehaviour;
import uk.ac.lkl.migen.system.expresser.ui.ecollaborator.DocumentCanvasContainer;
import uk.ac.lkl.migen.system.expresser.ui.uievent.ShapeProxyDragStartEvent;
import uk.ac.lkl.migen.system.expresser.ui.uievent.ShapeProxyMenuItemEvent;
import uk.ac.lkl.migen.system.expresser.ui.uievent.ShapeProxyMenuPopupEvent;
import uk.ac.lkl.migen.system.expresser.ui.uievent.UIEventManager;
import uk.ac.lkl.migen.system.expresser.ui.view.shape.block.BasicShapeProxyView;
import uk.ac.lkl.migen.system.util.MiGenUtilities;

/**
 * This implements proxies for shapes
 * Used in ActivityDocuments to represent shapes added to canvases
 * 
 * 
 * @author Ken Kahn
 *
 */
@Deprecated
// We no longer support dragging from My Model to the Computer's Model
public class ShapeProxy extends ThumbNailShapeCanvas
       implements Transferable, DragGestureListener, DragSourceListener, IDObject, ObjectWithID {
    
    // for drag and drop need a flavor for TiedNumShapeProxyberPanels
    static public DataFlavor shapeProxyFlavor =
	createShapeProxyDataFlavor();

    private static DataFlavor[] dataFlavors = { shapeProxyFlavor };

    // can only move it -- not copy it implicitly
    private int transferMode = TransferHandler.MOVE;

    private DragGestureRecognizer dragGestureRecognizer = null;
    
    // only one drag at a time so static is appropriate
    private static boolean dragStarted = false; 
    
    private ShapeProxy dragee = null;
    
    private ShapeProxy shapeProxyStartingDrag = null;

    private DragSource dragSource = null;

    private DisplayDragSourceDragSourceMotionListener dragSourceMotionListener = null;
    
    private ID id;

    private boolean dragEnabled;
    
    /**
     * This is identical to the shape on the master canvas
     * where it came from.
     */
    private BlockShape originalShapeCopy;
    
    /**
     * This is a frozen snapshot of the shape where all its
     * unlocked tied numbers have been replaced by tied number
     * proxies that are snapshots of the tied number at the time
     * the frozen shape was created.
     */
    private BlockShape shapeCopy;
    
    private ExpresserModel thumbNailModel;
    
//    private Timer highlightTimer = null;
//    
//    private int highlightCounter = 0;
     
    public ShapeProxy(ExpresserModel thumbNailModel, 
	              BlockShape originalShapeCopy, // to restore when dragged to master canvas
	              BlockShape frozenCopy, // unchanging version
	              int maxWidth, 
	              int maxHeight, 
	              int gridSize) {
	super(thumbNailModel, maxWidth, maxHeight, gridSize);
	this.thumbNailModel = thumbNailModel;
	setAdjustableGridSize(false); // don't change the grid size
	this.originalShapeCopy = originalShapeCopy;
	this.shapeCopy = frozenCopy;
	setViewClass(BasicShape.class, BasicShapeProxyView.class);
	id = IDFactory.newID(this);
	setDragEnabled(true);
	addPopupMenuOnClick();
	setOpaque(false); // ignored -- see isOpaque below
	setDropTarget(null);
    }
    
    public String getIdName() {
	return "ShapeProxy";
    }
    
    public static ShapeProxy createShapeProxy(
	    BlockShape originalShape,
	    int gridSize) {
	return createShapeProxy(
		originalShape,
		createCopyWithTiedNumberProxies(originalShape),	    
		gridSize);
    }
	
    public static ShapeProxy createShapeProxy(
	    BlockShape originalShape,
	    BlockShape frozenCopy,
	    int gridSize) {
	ExpresserModel thumbNailModel = createThumbNailModel();
	thumbNailModel.addObject(frozenCopy);
	ShapeProxy shapeProxy = 
	    new ShapeProxy(
		    thumbNailModel,
		    originalShape.createCopy(),
		    frozenCopy,
		    originalShape.getWidth()*gridSize, 
		    originalShape.getHeight()*gridSize, 
		    gridSize);
	shapeProxy.setShape(frozenCopy);
	frozenCopy.generateCorrectColorExpressions();
	return shapeProxy;
    }

    protected static BlockShape createCopyWithTiedNumberProxies(BlockShape shape) {
	// by using a proxy model the copy will not share tied numbers
	// with those in shape (accessed via masterModel)
	BlockShape shapeCopy = shape.createCopy();
        // replace all unlocked tied numbers with proxies
	shapeCopy.replaceTiedNumbersWithProxies();
	return shapeCopy;
    }

    protected ShapeProxy createCopy() {
	return createShapeProxy(getOriginalShapeCopy(), getGridSize());
    }
    
    @Override
    protected void paintThumbNailBackground(Graphics2D g2) {
	// don't want a background for proxies
    }
    
    @Override
    protected void paintComponentInternal(Graphics2D g2) {
	paintObjects(g2);
	paintColorGrid(g2);
	// why translate now??
	g2.translate(getShape().getX()*getGridSize(), getShape().getY()*getGridSize());
	paintGrid(g2);
    }
    
    @Override
    public Color getGridColor() {
        return new Color(240, 240, 240); // very light gray
    }
    
    @Override
    // JDesktopPane ignores the opaque setting
    // it is an ancestor to contain JInternalFrames
    public boolean isOpaque() {
	return false;
    }
    
    protected void addPopupMenuOnClick() {
	addMouseListener(new MouseListener() {

	    public void mouseClicked(MouseEvent e) {
	    }

	    public void mouseEntered(MouseEvent e) {
		ShapeProxy.this.setCursor(new Cursor(Cursor.HAND_CURSOR));
	    }

	    public void mouseExited(MouseEvent e) {
		ShapeProxy.this.setCursor(Cursor.getDefaultCursor());
	    }

	    public void mousePressed(MouseEvent e) {
	    }

	    public void mouseReleased(MouseEvent e) {
		addPopupMenu();
	    }

	});
    }

    protected void addPopupMenu() {
	UIEventManager.processEvent(new ShapeProxyMenuPopupEvent(getId().toString()));
	JPopupMenu menu = new JPopupMenu();
	JMenuItem item;
	final String copyLabel = MiGenUtilities.getLocalisedMessage("Copy");
	item = new JMenuItem(copyLabel);
	item.addActionListener(new ActionListener() {

	    public void actionPerformed(ActionEvent e) {
		UIEventManager.processEvent(new ShapeProxyMenuItemEvent(getId().toString(), copyLabel));
		ShapeProxy copy = ShapeProxy.this.createCopy();
		getParent().add(copy);
		copy.setSize(copy.getPreferredSize());
		Point location = ShapeProxy.this.getLocation();
		int gridSize = getGridSize();
		location.translate(gridSize/4, gridSize/-4);
		copy.setLocation(location);
	    }
	});
	menu.add(item);
	final String removeLabel = MiGenUtilities.getLocalisedMessage("Remove");
	item = new JMenuItem(removeLabel);
	item.addActionListener(new ActionListener() {

	    public void actionPerformed(ActionEvent e) {
		UIEventManager.processEvent(new ShapeProxyMenuItemEvent(getId().toString(), removeLabel));
		getParent().remove(ShapeProxy.this);
	    }
	});
	menu.add(item);
	final String showPropertieslabel = MiGenUtilities.getLocalisedMessage("ShowProperties");
	item = new JMenuItem(showPropertieslabel);
	item.addActionListener(new ActionListener() {

	    public void actionPerformed(ActionEvent e) {
		ObjectSetCanvas canvas = getCanvas();
		UIEventManager.processEvent(new ShapeProxyMenuItemEvent(getId().toString(), showPropertieslabel));
		if (canvas != null) {
		    // read-only but does enable iteration buttons
		    AttributeManifestBehaviour attributeManifestBehaviour = 
			new AttributeManifestBehaviour(canvas, true, true);
		    BlockShape shape = getModel().getShapes().get(0);
		    // need to move the shape so that property lists
		    // are located correctly
		    int gridSize = canvas.getGridSize();
		    int x = ShapeProxy.this.getX() / gridSize;
		    shape.setXValue(new Number(x));
		    int y = ShapeProxy.this.getY() / gridSize;
		    shape.setYValue(new Number(y));
		    AttributeManifest manifest = 
			attributeManifestBehaviour.createManifest(shape);
		    canvas.setManifestLocation(manifest, ShapeProxy.this.getBounds(), canvas.getBounds());
		    manifest.removeHighlightListener();
		    manifest.setSize(manifest.getPreferredSize());
		    manifest.setClosable(true); // even though read-only
		}
	    }});
	menu.add(item);
	if (MiGenConfiguration.isAddBoxMenuToActivityDocumentCanvasItems()) {
	    DocumentCanvasContainer activityDocumentCanvas = getActivityDocumentCanvas();
	    if (activityDocumentCanvas != null) {
		Point location = getLocation();
		location = 
		    SwingUtilities.convertPoint(getParent(), location, activityDocumentCanvas.getDocumentCanvas());
		item = ExpressionPanelProxy.coloredBoxHighlightMenu(getWidth(), getHeight(), location, activityDocumentCanvas);
		menu.add(item);
	    }
	}
	menu.show(this, 0, 0);
    }
    
    protected DocumentCanvasContainer getActivityDocumentCanvas() {
	Container parent = getParent();
	while (parent != null) {
	    if (parent instanceof DocumentCanvasContainer) {
		return (DocumentCanvasContainer) parent;
	    }
	    parent = parent.getParent();
	}
	return null;
    }

    public ObjectSetCanvas getCanvas() {
	Container parent = getParent();
	if (parent instanceof ObjectSetCanvas) {
	    return (ObjectSetCanvas) parent;
	} else {
	    return null;
	}
    }
    
    @Override
    protected ExpresserModel getModelWithGrid() {
	// if this proxy is on another canvas (e.g. not being dragged around) then
	// need to use the colour grid of the ancestor canvas
	ObjectSetCanvas ancestorCanvas = getAncestorCanvas();
	if (ancestorCanvas != null) {
	    return ancestorCanvas.getModel();
	} else {
	    return super.getModelWithGrid();
	}
    }
    
//    @Override
//    public void addColorToGrid(
//	    int x, int y, ModelColor color, boolean correctAllocation, BasicShape basicShape) {
//	// if this proxy is on another canvas (e.g. not being dragged around) then
//	// need to use the colour gird of the ancestor canvas
//	ObjectSetCanvas ancestorCanvas = getAncestorCanvas();
//	if (ancestorCanvas != null) {
//	    int xOffset = 0; //getShape().getX();
//	    int yOffset = 0; //getShape().getY();
//	    ancestorCanvas.addColorToGrid(x+xOffset, y+yOffset, color, correctAllocation, basicShape);
//	    return;
//	}
//	super.addColorToGrid(x, y, color, correctAllocation, basicShape);
//    }

    protected ObjectSetCanvas getAncestorCanvas() {
	if (dragStarted) {
	    return null;
	}
	Container ancestor = this;
	while ((ancestor = ancestor.getParent()) != null && !(ancestor instanceof ObjectSetCanvas));
	return (ObjectSetCanvas) ancestor;
    }
    
    @Override
    public void paintComponent(Graphics g) {
	if (getAncestorCanvas() == null) {
	    // if on a canvas it'll paint me
	    g.translate(-getShape().getX()*getGridSize(), -getShape().getY()*getGridSize());
	}
	super.paintComponent(g);
	paintTemporaryHighlightedBoundaries((Graphics2D) g.create());
    }

//    protected void highlightEntireProxy() {
//	ExpresserModel model = getModel();
//	Collection<BlockShape> temporaryHighlightedShapes = model.getTemporaryHighlightedShapes();
//	if (temporaryHighlightedShapes.isEmpty()) {
//	    if (highlightTimer != null) {
//		highlightTimer.stop();
//		highlightTimer = null;
//	    }
//	    setBorder(null);
//	} else {
//	    if (highlightTimer == null) {
//		ActionListener actionListener = new ActionListener() {
//
//		    public void actionPerformed(ActionEvent e) {
//			Color color = 
//			    temporaryHighlightColors[
//			        highlightCounter++%temporaryHighlightColors.length];
//			setBorder(BorderFactory.createLineBorder(color, 4));
//			if (!isShowing()) {
//			    highlightTimer.stop();
//			}
//		    }
//
//		};
//		highlightTimer = new Timer(100, actionListener);
//		highlightTimer.start();       
//	    } else if (!highlightTimer.isRunning()) {
//		highlightTimer.start();
//	    }
//	}
//    }
    
//    @Override
//    protected void collectOuterBoundingAreas(
//	    Collection<BlockShape> highlightedShapes,
//	    ArrayList<Area> patternOuterBoundingAreas) {
//	// ignore sub-shapes for now
//	patternOuterBoundingAreas.add(new Area(getBounds()));
//    }
    
    // following needed to support drag and drop:

    private void enableDragAndDrop() {
	final TransferHandler transferHandler = new TransferHandler() {

	    public boolean canImport(JComponent comp, DataFlavor[] drageeDataFlavors) {
		return false;
	    }

	    public boolean importData(JComponent comp, Transferable t) {
		return false;
	    }

	    public int getSourceActions(JComponent c) {
		return transferMode;
	    }

	    public Transferable createTransferable(JComponent c) {
		try {
		    return ShapeProxy.this.getTransferData(shapeProxyFlavor);
		} catch (Exception e) {
		    e.printStackTrace();
		    return null;
		}
	    }
	};
	// initialise transfer handler so the data (i.e. the tied number) can be
	// transfered to the drop object
	setTransferHandler(transferHandler);
	// set up listener to drag events
	dragSource = DragSource.getDefaultDragSource();
	dragGestureRecognizer =
	    dragSource.createDefaultDragGestureRecognizer(this, transferMode, this);
	// following needed to see tied number while being dragged
	dragSourceMotionListener =
	    new DisplayDragSourceDragSourceMotionListener(shapeProxyFlavor);
	dragSource.addDragSourceMotionListener(dragSourceMotionListener);
    }

    private void disableDragAndDrop() {
	setTransferHandler(null);
	if (dragGestureRecognizer != null) {
	    dragGestureRecognizer.removeDragGestureListener(this);
	    dragGestureRecognizer = null;
	}
    }

    protected Transferable createTransferable() {
	try {
	    return getTransferData(shapeProxyFlavor);
	} catch (Exception e) {
	    e.printStackTrace();
	    return null;
	}
    }

    static public DataFlavor createShapeProxyDataFlavor() {
	try {
	    return new DataFlavor(
		    DataFlavor.javaJVMLocalObjectMimeType
			    + ";class=uk.ac.lkl.migen.system.expresser.ui.ShapeProxy");
	} catch (Exception e) {
	    e.printStackTrace();
	    return null;
	}
    }

    public ShapeProxy getTransferData(DataFlavor flavor)
	    throws UnsupportedFlavorException, IOException {
	return this;
    }

    public DataFlavor[] getTransferDataFlavors() {
	return dataFlavors;
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
	return flavor.equals(shapeProxyFlavor);
    }

    public void dragGestureRecognized(final DragGestureEvent event) {
	if (dragStarted) {
	    return;
	}
	dragStarted = true;
	boolean onADocumentCanvas = getParent() instanceof DocumentCanvas;
	if (onADocumentCanvas) {
	    setVisible(false);
	    dragee = createShapeProxy(getOriginalShapeCopy(), getShapeCopy().createCopy(), getGridSize());
	} else {
	    dragee = this;
	}
	dragee.setDragEnabled(false); // until dropped
	ObjectSetCanvas canvas = getCanvas();
	ExpresserLauncher.add(dragee);
	dragee.setSize(dragee.getPreferredSize());
	Point location = getLocation();
	location = SwingUtilities.convertPoint(getParent(), location, ExpresserLauncher.getFrame());
	dragee.setLocation(location);
	if (!dragCreatesCopy() && onADocumentCanvas) { 
	    // added a copy; so remove the original to behave like a move
	    // but postpone this in case cross canvas drag which acts like a copy
	    dragee.setShapeProxyStartingDrag(this);
	}
	UIEventManager.processEvent(new ShapeProxyDragStartEvent(getId().toString(), canvas.getId().toString(), dragee.getId().toString()));
//	dragee.logAction("Drag started");
	try {
	    event.startDrag(DragSource.DefaultCopyDrop, dragee.createTransferable(), dragee);
	} catch (InvalidDnDOperationException e) {
	    System.out.println("Ignoring drag error.");
	    e.printStackTrace();
	}
    }

    public void dragDropEnd(DragSourceDropEvent dsde) {
//	logAction("Drag ended");
	dragStarted = false;
	// if it was not dropped on something willing to receive it
	// e.g. the slave panel
	ExpresserLauncher.remove(this);
	if (getParent() instanceof DocumentCanvas) {
	    // need to pass up to parent the drop event
	    DocumentCanvas documentCanvas = (DocumentCanvas) getParent();
	    new DropTarget(this, documentCanvas.getDropTargetListener());
	}
	// if left on the canvas should not create a copy when dragged
//	boolean onACanvas = onACanvas();
//	setDragCreatesCopy(!onACanvas);
//	if (onACanvas) {
//	    setDragEnabled(true);
//	}
//	Container topLevelAncestor = getTopLevelAncestor();
//	if (topLevelAncestor != null) {
//	    // see Issue 582
//	    topLevelAncestor.repaint();
//	}
	ExpresserLauncher.setCurrentPanelDirty();
    }

    public void dragEnter(DragSourceDragEvent dsde) {
    }

    public void dragExit(DragSourceEvent dse) {
    }

    public void dragOver(DragSourceDragEvent dsde) {
    }

    public void dropActionChanged(DragSourceDragEvent dsde) {
    }

    public static DataFlavor getFlavor() {
	return shapeProxyFlavor;
    }

    public void setDragCreatesCopy(boolean flag) {
	if (flag) {
	    transferMode = TransferHandler.COPY;
	} else {
	    transferMode = TransferHandler.MOVE;
	}
    }

    public boolean dragCreatesCopy() {
	return transferMode == TransferHandler.COPY;
    }

    public void setDragEnabled(boolean dragEnabled) {
	this.dragEnabled = dragEnabled;
	if (dragEnabled) {
	    enableDragAndDrop();
	} else {
	    disableDragAndDrop();
	}
    }

    public boolean isDragEnabled() {
	return dragEnabled;
    }
    
//    private void logAction(String message) {
//	StandaloneViewLogger.sessionJuly08Log(message + logIdentity());
//    }
    
//    private void logMenuPoppedUp(String kind) {
//	logAction(kind + " menu popped up");
//    }

//    private void logActionPerformed(String menuLabel) {
//	logAction("Clicked '" + menuLabel + "'");
//    }
    
    protected String logIdentity() {
	return " (" + getId() + ")";
    }
    
    public ID getId() {
	return id;
    }

    public BlockShape getOriginalShapeCopy() {
        return originalShapeCopy;
    }

    public ShapeProxy getShapeProxyStartingDrag() {
        return shapeProxyStartingDrag;
    }

    public void setShapeProxyStartingDrag(ShapeProxy shapeProxyStartingDrag) {
        this.shapeProxyStartingDrag = shapeProxyStartingDrag;
    }

    public ExpresserModel getThumbNailModel() {
        return thumbNailModel;
    }

    public BlockShape getShapeCopy() {
        return shapeCopy;
    }
    
}

