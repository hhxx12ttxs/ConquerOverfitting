package uk.ac.lkl.migen.system.expresser.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

import uk.ac.lkl.common.util.ID;
import uk.ac.lkl.common.util.IDFactory;
import uk.ac.lkl.common.util.IDObject;
import uk.ac.lkl.common.util.ObjectWithID;
import uk.ac.lkl.common.util.config.MiGenConfiguration;
import uk.ac.lkl.common.util.event.SourcedUpdateSupport;
import uk.ac.lkl.common.util.event.UpdateEvent;
import uk.ac.lkl.common.util.event.UpdateListener;
import uk.ac.lkl.common.util.expression.Expression;
import uk.ac.lkl.common.util.expression.ExpressionList;
import uk.ac.lkl.common.util.expression.LocatedExpression;
import uk.ac.lkl.common.util.expression.ModifiableOperation;
import uk.ac.lkl.common.util.expression.Operator;
import uk.ac.lkl.common.util.expression.operator.NumberAdditionOperator;
import uk.ac.lkl.common.util.expression.operator.NumberMultiplicationOperator;
import uk.ac.lkl.common.util.expression.operator.NumberSubtractionOperator;
import uk.ac.lkl.common.util.value.Number;
import uk.ac.lkl.common.util.value.Value;
import uk.ac.lkl.migen.system.ExpresserLauncher;
import uk.ac.lkl.migen.system.expresser.model.Attribute;
import uk.ac.lkl.migen.system.expresser.model.AttributeHandle;
import uk.ac.lkl.migen.system.expresser.model.ColorResourceAttributeHandle;
import uk.ac.lkl.migen.system.expresser.model.ExpresserModel;
import uk.ac.lkl.migen.system.expresser.model.ExpressionValueSource;
import uk.ac.lkl.migen.system.expresser.model.ModelColor;
import uk.ac.lkl.migen.system.expresser.model.StandaloneWalker;
import uk.ac.lkl.migen.system.expresser.model.Walker;
import uk.ac.lkl.migen.system.expresser.model.shape.block.BlockShape;
import uk.ac.lkl.migen.system.expresser.model.tiednumber.TiedNumberExpression;
import uk.ac.lkl.migen.system.expresser.model.tiednumber.UnspecifiedTiedNumberExpression;
import uk.ac.lkl.migen.system.expresser.ui.behaviour.AttributeManifestBehaviour;
import uk.ac.lkl.migen.system.expresser.ui.uievent.DropExpressionOnExpressionEvent;
import uk.ac.lkl.migen.system.expresser.ui.uievent.DropExpressionOnGlobalColorAllocationPanelEvent;
import uk.ac.lkl.migen.system.expresser.ui.uievent.DropTiedNumberOnGlobalColorAllocationPanelEvent;
import uk.ac.lkl.migen.system.expresser.ui.uievent.ExpressionDragStartEvent;
import uk.ac.lkl.migen.system.expresser.ui.uievent.ExpressionMenuItemEvent;
import uk.ac.lkl.migen.system.expresser.ui.uievent.ExpressionMenuPopupEvent;
import uk.ac.lkl.migen.system.expresser.ui.uievent.UIEvent;
import uk.ac.lkl.migen.system.expresser.ui.uievent.UIEventManager;
import uk.ac.lkl.migen.system.util.MiGenUtilities;

/**
 * View of an expression. Supports drag and drop.
 * 
 * @author Ken Kahn
 *
 */

public class ExpressionPanel<V extends Value<V>> extends JPanel
	implements Transferable, DragGestureListener, DragSourceListener, IDObject, ObjectWithID {
    
    private static final int CONTINUE_REPLACEMENT = 0;
    private static final int SKIP_REPLACEMENT = 1;
    private static final int ABORT_REPLACEMENT = 2;
    
    private static HashMap<String, ExpressionPanel<?>> registeredExpressionPanels = 
	new HashMap<String, ExpressionPanel<?>>();

    private Expression<V> expression = null;
    
    private ID id;
       
    private SourcedUpdateSupport<ExpressionPanel<V>> updateSupport =
	new SourcedUpdateSupport<ExpressionPanel<V>>(this);
       
    // needed so wizard can find the panels that need user-supplied values
    private ArrayList<UnspecifiedTiedNumberPanel<Number>> unspecifiedTiedNumberPanels = 
	new ArrayList<UnspecifiedTiedNumberPanel<Number>>();
    
    // To enable dropping of TiedNumberPanels or ExpressionPanels on ExpressionPanels
    // Also used for dragging expression panels
    private boolean dropEnabled = false;  
   
    // following needed to enable dragging
    public static DataFlavor expressionPanelDropTargetFlavor = createExpressionPanelDropTargetDataFlavor();
    
    private static DataFlavor[] dataFlavors = {expressionPanelDropTargetFlavor};
    
    // by default copy the panel when dragging but becomes move when left on the canvas
    private int transferMode = TransferHandler.COPY;
    
    private DragGestureRecognizer dragGestureRecognizer = null;
    
    private boolean dragEnabled;

    private boolean readOnly;
    
    protected boolean displayCurrentValue = false;
    
    // only used if displayCurrentValue is true
    TiedNumberPanel<Number> currentValuePanel = null;

    // used to maintain the location that the slave of this was moved to
    private Point slaveLocation = null;
    
    private static boolean dragStarted = false; // only one drag at a time so static is fine
    
    private ExpressionPanel<Number> dragee = null;

    @SuppressWarnings("rawtypes")
    private UpdateListener preferredSizeUpdateListener = null;

    private DisplayDragSourceDragSourceMotionListener motionListener = null;

    private ExpressionPanel<Number> leftSubExpressionPanel = null;

    private ExpressionPanel<Number> rightSubExpressionPanel = null;
    
    private JPanel panelStartingDrag = null;
    
    // used for logging
    private Boolean containedInMasterPanel = null;
    
//    protected UpdateListener<Expression<V>> expressionListener = 
//	new UpdateListener<Expression<V>>() {
//
//	public void objectUpdated(UpdateEvent<Expression<V>> e) {
//	    recomputePanels();		
//	}
//
//    };
    
    // border used when drag is enabled
    private static Border draggableBorder = 
	BorderFactory.createBevelBorder(BevelBorder.RAISED, Color.GRAY, Color.DARK_GRAY);
    public static Color DARK_GREEN = new Color(0, 127, 0);
    private static Border goodBorder = 
	BorderFactory.createBevelBorder(BevelBorder.RAISED, Color.GREEN, DARK_GREEN);
    public static Color DARK_RED = new Color(127, 0, 0);
    private static Border badBorder = 
	BorderFactory.createBevelBorder(BevelBorder.RAISED, Color.RED, DARK_RED);
    
    private static Color backgroundColor = new Color(240, 240, 240); // very light gray
    
    private static Color displayCurrentValueBackgroundColor = new Color(255, 192, 0);
    
    private UpdateListener<TiedNumberPanel<Number>> tiedNumberPanelUpdateListener = 
	new UpdateListener<TiedNumberPanel<Number>>() {

	@Override
	@SuppressWarnings("unchecked")
	public void objectUpdated(UpdateEvent<TiedNumberPanel<Number>> e) {
	    setExpression((Expression<V>) e.getSource().getReplacement());
	}
	
    };
    
    // need to maintain the identity of the locatedExpression
    // so model can be informed of its removal
    private LocatedExpression<V> locatedExpression = null;
    
    public ExpressionPanel(Expression<V> expression) {
	this(expression, false, false);
    }
    
    public ExpressionPanel(Expression<V> expression, boolean readOnly) {
	this(expression, readOnly, false);
    }
    
    public ExpressionPanel(Expression<V> expression, boolean readOnly, boolean displayCurrentValue) {
	super(new FlowLayout());
	this.expression = expression;
//	if displayCurrentValue then is also read only
	this.readOnly = readOnly || displayCurrentValue;
	this.displayCurrentValue = displayCurrentValue;
	id = IDFactory.newID(this);
	if (displayCurrentValue) {
	    setBackground(displayCurrentValueBackgroundColor);
	} else {
	    setBackground(backgroundColor);
	}
	addToPanel();
	// drag enabled even if readOnly
	// see http://code.google.com/p/migen/source/detail?r=2813
	setDragEnabled(true);
	setDropEnabled(!this.readOnly);
	if (!this.readOnly) {
	    addMouseListener(); // for drop cursor feedback when entering
//	    addListenerToRecomputeOnUpdate();
	}
	addPopupMenuOnClick();
	ContainerListener containerListener = new ContainerListener() {

	    @Override
	    public void componentAdded(ContainerEvent arg0) {
		// no need since registered by Walker code
//		register();
	    }

	    @Override
	    public void componentRemoved(ContainerEvent arg0) {
		deregister();
	    }
	    
	};
	addContainerListener(containerListener);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected void addToPanel() {
	if (displayCurrentValue) {
	    TiedNumberExpression<Number> tiedNumber = 
		new TiedNumberExpression<Number>((Number) expression.evaluate());
	    if (currentValuePanel != null) {
		currentValuePanel.setTiedNumber(tiedNumber);
	    } else {
		currentValuePanel = createTiedNumberPanel(tiedNumber);
		currentValuePanel.setDropEnabled(false);
		updateWhenAnyTiedNumberChanges(currentValuePanel);
		UpdateListener<Expression<V>> updateListener =
		    new UpdateListener<Expression<V>>() {

			@Override
			public void objectUpdated(UpdateEvent<Expression<V>> e) {
			    updateCurrentValue(currentValuePanel);
			}
		    
		};
		expression.addUpdateListener(updateListener);
	    }
	    add(currentValuePanel);
	} else if (expression.isTiedNumber()) {    
	    TiedNumberExpression<Number> tiedNumber;
	    if (expression.isTiedNumber()) {
		tiedNumber = (TiedNumberExpression<Number>) expression;
	    } else {
		tiedNumber = new TiedNumberExpression<Number>((Number) expression.evaluate());
	    }
	    final TiedNumberPanel<Number> tiedNumberPanel;
	    if (tiedNumber.isSpecified()) {
		tiedNumberPanel = createTiedNumberPanel(tiedNumber);
	    } else {
		tiedNumberPanel = new UnspecifiedTiedNumberPanel<Number>(tiedNumber, readOnly);
	    }
	    tiedNumberPanel.addUpdateListener(tiedNumberPanelUpdateListener);
	    preferredSizeUpdateListener = new UpdateListener() {

		@Override
		public void objectUpdated(UpdateEvent e) {
		    tiedNumberPanel.setSize(tiedNumberPanel.getPreferredSize());
		    Container ancestor = tiedNumberPanel.getParent();
		    while (ancestor != null && ancestor instanceof ExpressionPanel) {
			ancestor.setSize(ancestor.getPreferredSize());
			ancestor = ancestor.getParent();
		    }
		    while (ancestor != null) {
			if (ancestor instanceof AttributeManifest) {
			    ancestor.setSize(ancestor.getPreferredSize());
			}
			ancestor = ancestor.getParent();
		    }
		}
		
	    };
	    tiedNumber.addDisplayModeUpdateListener(preferredSizeUpdateListener);
	    tiedNumber.addUpdateListener(preferredSizeUpdateListener);
	    tiedNumber.addNameFieldUpdateListener(preferredSizeUpdateListener);
	    add(tiedNumberPanel);
	} else if (expression.isOperation()) {
	    ModifiableOperation<Number, Number> operation = 
		(ModifiableOperation<Number, Number>) expression;
	    int arity = operation.getNumOperands();
	    if (arity == 2) {
		leftSubExpressionPanel = 
		    createExpressionPanel(operation.getOperand(0), readOnly);
		leftSubExpressionPanel.addSubExpressionListener(0, operation, (ExpressionPanel<Number>) this);
		// following simplified things and looked nicer but prevented the replacement
		// of tied numbers inside of expressions
//		if (subExpression1.getComponentCount() == 1) {
//		    add(subExpression1.getComponent(0));
//		} else {
		add(leftSubExpressionPanel);
//		}
		leftSubExpressionPanel.setSize(leftSubExpressionPanel.getPreferredSize());
		JLabel operatorLabel = new JLabel(operation.getSymbol());
		operatorLabel.setFont(AttributeManifest.operatorFont);
		add(operatorLabel);
		rightSubExpressionPanel = 
		    createExpressionPanel(operation.getOperand(1), readOnly);
		rightSubExpressionPanel.addSubExpressionListener(1, operation, (ExpressionPanel<Number>) this);
//		if (subExpression2.getComponentCount() == 1) {
//		    add(subExpression2.getComponent(0));
//		} else {
		add(rightSubExpressionPanel);
//		}
		rightSubExpressionPanel.setSize(rightSubExpressionPanel.getPreferredSize());
	    } else {
		// TODO: make this work for any arity
		// currently there are none with arity other than 2
	    }
	    // can be a ValueExpression for the global color allocation
	    // but no need to do anything in that case
	}
	// there isn't any other kind of expression that should turn up
	setSize(getPreferredSize());
    }

    private void updateWhenAnyTiedNumberChanges(
	    final TiedNumberPanel<Number> currentValuePanel) {
	Walker walker = new Walker() {

	    @SuppressWarnings("unchecked")
	    @Override
	    public boolean tiedNumberFound(
		    TiedNumberExpression<?> tiedNumber,
		    BlockShape shape, 
		    AttributeHandle<Number> handle,
		    ExpresserModel expresserModel) {
		UpdateListener<Expression<Number>> updateListener =
		    new UpdateListener<Expression<Number>>() {

			@Override
			public void objectUpdated(
				UpdateEvent<Expression<Number>> e) {
			    updateCurrentValue(currentValuePanel);
			}
		    
		};
		((TiedNumberExpression<Number>) tiedNumber).addUpdateListener(updateListener);
		return false;
	    }
	    
	};
	expression.walkToTiedNumbers(walker, null, null, null);
    }

//    public void addListenerToRecomputeOnUpdate() {
//	expression.addUpdateListener(expressionListener);
//    }

    protected ExpressionPanel<Number> createExpressionPanel(
	    Expression<Number> expression, boolean readOnly) {
	return new ExpressionPanel<Number>(expression, readOnly, false);
    }

    protected TiedNumberPanel<Number> createTiedNumberPanel(
	    TiedNumberExpression<Number> tiedNumber) {
	return new TiedNumberPanel<Number>(tiedNumber, readOnly);
    }
    
//    @Override
//    public void setSize(Dimension d) {
//	super.setSize(d);
//	System.out.println(d);
//    }
    
    protected void addMouseListener() {
	addMouseListener(new MouseListener() {

	    @Override
	    public void mouseClicked(MouseEvent e) {	
	    }

	    @Override
	    public void mouseEntered(MouseEvent e) {
		ExpressionPanel.this.setCursor(new Cursor(Cursor.HAND_CURSOR));
	    }

	    @Override
	    public void mouseExited(MouseEvent e) {
		ExpressionPanel.this.setCursor(Cursor.getDefaultCursor());
	    }

	    @Override
	    public void mousePressed(MouseEvent e) {
	    }

	    @Override
	    public void mouseReleased(MouseEvent e) {
	    }

	});
    }
    
    public ExpressionPanel<V> createCopy() {
	return createExpressionPanelCopy(isReadOnly());
    }
    
    /**
     * @return a copy that is an ExpressionPanel 
     * (and not an instance of a subclass)
     */
    public ExpressionPanel<V> createExpressionPanelCopy(boolean isReadOnly) {
	Expression<V> expressionCopy = getExpression().createCopy();
	ExpressionPanel<V> copy = 
	    new ExpressionPanel<V>(expressionCopy, isReadOnly, displayCurrentValue());
	copy.setDragEnabled(dragEnabled);
	copy.setDragCreatesCopy(dragCreatesCopy());
	return copy;
    }
    
    public void setDropEnabled(boolean dropEnabled) {
	if (this.dropEnabled == dropEnabled) {
	    return;
	}
	this.dropEnabled = dropEnabled;
	if (dropEnabled) {
	    DropTargetListener dropTargetListener = new DropTargetListener() {

		@Override
		public void dragEnter(DropTargetDragEvent dtde) {
		    Transferable transferable = dtde.getTransferable();
		    if (//!dragStarted &&
			!isReadOnly() && //|| 
//			 findGlobalColorAllocationPanelAncestor() != null) &&
			(transferable.isDataFlavorSupported(TiedNumberPanel.getFlavor()) ||
			 transferable.isDataFlavorSupported(expressionPanelDropTargetFlavor))) {
			ExpressionPanel.this.displayDropFeedback(true, true);
			dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
		    }
		}

		@Override
		public void dragExit(DropTargetEvent dte) {
		    ExpressionPanel.this.displayDropFeedback(false, true);  
		}

		@Override
		public void dragOver(DropTargetDragEvent dtde) {
		}

		@Override
		@SuppressWarnings("unchecked")
		// this warning is a serious problem if we ever had tied numbers of more than one kind of Value
		// since the drag doesn't preserve the generic information
		public void drop(DropTargetDropEvent dtde) {
		    dragStarted = false;
		    ExpressionPanel.this.setBackground(backgroundColor);
		    Transferable transferable = dtde.getTransferable();
		    try {
			Component transferData = 
			    (Component) transferable.getTransferData(TiedNumberPanel.getFlavor());
			Expression<Number> currentExpression = 
			    (Expression<Number>) getExpression();
			double dropX = dtde.getLocation().getX();
			boolean droppedOnLeftHalf = dropX < getWidth()/2;
			acquireFocusForAncestorAttributeManifest(ExpressionPanel.this);
			GlobalColorAllocationPanel globalColorAllocationPanel = 
			    ExpressionPanel.this.findGlobalColorAllocationPanelAncestor();
			if (transferData instanceof TiedNumberPanelProxy) {
			    TiedNumberPanelProxy<V> tiedNumberPanelProxy = (TiedNumberPanelProxy<V>) transferData;
			    if (globalColorAllocationPanel != null) {
				UIEvent<?> event = 
				    new DropTiedNumberOnGlobalColorAllocationPanelEvent(
					getId().toString(),
					tiedNumberPanelProxy.getId().toString(),
					globalColorAllocationPanel.getId().toString());
				UIEventManager.processEvent(event); 
			    }
			    tiedNumberPanelProxy.setDragCreatesCopy(true);
			    Expression<Number> droppedExpression = 
				tiedNumberPanelProxy.getTiedNumberExpression().getOriginal();
			    handleDropOfExpressionOnExpression(currentExpression, 
				                               droppedExpression, 
				                               droppedOnLeftHalf, 
				                               tiedNumberPanelProxy, 
				                               null);
			} else if (transferData instanceof TiedNumberPanel) {
			    TiedNumberPanel<V> tiedNumberPanel = (TiedNumberPanel<V>) transferData;
			    if (!tiedNumberPanel.isReadOnly()) {
				if (globalColorAllocationPanel != null) {
				    UIEvent<?> event = 
					new DropTiedNumberOnGlobalColorAllocationPanelEvent(
						getId().toString(),
						tiedNumberPanel.getId().toString(),
						globalColorAllocationPanel.getId().toString());
				    UIEventManager.processEvent(event);
				}
				tiedNumberPanel.setDragCreatesCopy(true);
				Expression<Number> droppedExpression = 
				    tiedNumberPanel.getTiedNumberExpression();
				handleDropOfExpressionOnExpression(currentExpression, 
					                           droppedExpression, 
					                           droppedOnLeftHalf, 
					                           tiedNumberPanel, 
					                           null);
			    }
			} else if (transferData instanceof ExpressionPanel) {
			    // generics gets in the way here
			    // following only works for IntegerValue
			    ExpressionPanel<Number> expressionPanel = 
				(ExpressionPanel<Number>) transferData;
			    if (!expressionPanel.isReadOnly() || 
				globalColorAllocationPanel != null || 
				expressionPanel.displayCurrentValue()) {
				if (globalColorAllocationPanel != null) {
				    UIEvent<?> event = 
					new DropExpressionOnGlobalColorAllocationPanelEvent(
						getId().toString(),
						expressionPanel.getId().toString(),
						globalColorAllocationPanel.getId().toString());
				    UIEventManager.processEvent(event);
				}
				Expression<Number> droppedExpression = expressionPanel.getExpression();
				handleDropOfExpressionOnExpression(currentExpression, 
					                           droppedExpression, 
					                           droppedOnLeftHalf, 
					                           expressionPanel, 
					                           expressionPanel);
//				expressionPanel.removeSlaveDragee();
			    }
			} else {
			    Container parent = transferData.getParent();
			    if (parent != null) {
				parent.remove(transferData);
				parent.repaint();
			    }
			}
			dtde.dropComplete(true);
		    } catch (Exception e) {
			e.printStackTrace();
			dtde.rejectDrop();
		    }
		}

		@Override
		public void dropActionChanged(DropTargetDragEvent dtde) {
		}

	    };
	    // this adds the drop listener to this
	    new DropTarget(this, dropTargetListener);
	    if (expression instanceof TiedNumberExpression<?>) {
//		DropTargetListener forwardingListener = 
//		    TiedNumberPanel.forwardToParentDropTargetListener(dropTargetListener);
		TiedNumberPanel<?> tiedNumberPanel = (TiedNumberPanel<?>) getComponent(0);
//		tiedNumberPanel.addDropTargetListener(forwardingListener);
		// the following now wraps a forwarder automatically
		tiedNumberPanel.addDropTargetListener(dropTargetListener);
	    }
	} else {
	    setDropTarget(null);
	}
    }
    
    public void displayDropFeedback(boolean on, boolean topLevel) {
	if (on) {
	    String dropFeedbackColor = MiGenConfiguration.getDropFeedbackColor();
	    try {	
		setBackground(Color.decode(dropFeedbackColor));
	    } catch (NumberFormatException e) {
		MiGenUtilities.printError("Unable to parse " + dropFeedbackColor + " as a color.");
		e.printStackTrace();
	    }
	} else {
	    setBackground(backgroundColor);
	}
//	int componentCount = getComponentCount();
//	for (int i = 0; i < componentCount; i++) {
//	    Component component = getComponent(i);
//	    if (component instanceof TiedNumberPanel<?>) {
//		((TiedNumberPanel<?>) component).displayDropFeedback(on, false);
//	    } else if (component instanceof ExpressionPanel<?>) {
//		((ExpressionPanel<?>) component).displayDropFeedback(on);
//	    }
//	}
    }

    public boolean isDropEnabled() {
	return dropEnabled;
    }
    
   // following needed to support drag and drop:
    
    private void enableDragAndDrop() {
	final TransferHandler transferHandler = new TransferHandler() {
	    
	    @Override
	    public boolean canImport(JComponent comp, DataFlavor[] drageeDataFlavors) {
		for (int i = 0; i < drageeDataFlavors.length; i++) {
		    if (drageeDataFlavors[i].equals(expressionPanelDropTargetFlavor)) {
			return true;
		    }
		    if (drageeDataFlavors[i].equals(TiedNumberPanel.tiedNumberPanelFlavor)) {
			return true;
		    }
		}
		return false;
	    }

	    @Override
	    public boolean importData(JComponent comp, Transferable t) {
		return true;
	    }

	    @Override
	    public int getSourceActions(JComponent c) {
		return transferMode;
	    }

	    @Override
	    public Transferable createTransferable(JComponent c) {
		try {
		    return ExpressionPanel.this.getTransferData(expressionPanelDropTargetFlavor);
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
	DragSource dragSource = DragSource.getDefaultDragSource();
	dragGestureRecognizer =
	     dragSource.createDefaultDragGestureRecognizer(this, transferMode, this);
	// following needed to see tied number while being dragged
	motionListener  = 
	    new DisplayDragSourceDragSourceMotionListener(expressionPanelDropTargetFlavor);
	dragSource.addDragSourceMotionListener(motionListener);
    }
    
    public void removeDragSourceListener() {
	DragSource dragSource = DragSource.getDefaultDragSource();
	dragSource.removeDragSourceMotionListener(motionListener);
    }

    protected Transferable createTransferable() {
	try {
	    return getTransferData(expressionPanelDropTargetFlavor);
	} catch (Exception e) {
	    e.printStackTrace();
	    return null;
	}
    }

    static public DataFlavor createExpressionPanelDropTargetDataFlavor() {
	try {
	    return new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + 
	                          ";class=uk.ac.lkl.migen.system.expresser.ui.ExpressionPanel");
	} catch (Exception e) {
	    e.printStackTrace();
	    return null;
	}
    }

    @Override
    public ExpressionPanel<V> getTransferData(DataFlavor flavor) 
        throws UnsupportedFlavorException, IOException {
	return this;
    }
    
    @Override
    public DataFlavor[] getTransferDataFlavors() {
	return dataFlavors;
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
	return flavor.equals(expressionPanelDropTargetFlavor);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public void dragGestureRecognized(DragGestureEvent event) {
	// DragSource.isDragImageSupported() is false so can't drag images here 
	// need to workaround by using addDragSourceMotionListener
	// see http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4874070
	if (isDragStarted() || getExpression() == null) {
	    return;
	}
	setDragStarted(true);
	ObjectSetCanvas canvas = findCanvas();
//	ExpresserModel model = canvas==null ? null : canvas.getModel();
	dragee = (ExpressionPanel<Number>) createCopy();
	ExpresserLauncher.add(dragee);
	Point location = getLocation();
	location = SwingUtilities.convertPoint(getParent(), location, SwingUtilities.getWindowAncestor(this));
	dragee.setLocation(location);
	dragee.setDragEnabled(false); // until dropped
	dragee.setPanelStartingDrag(this);
	if (canvas != null) {
	    UIEventManager.processEvent(
		    new ExpressionDragStartEvent(getId().toString(), canvas.getId().toString(), dragee.getId().toString()));
	}
//      dragee.logMessage("Drag started");
	event.startDrag(DragSource.DefaultCopyDrop, dragee.createTransferable(), dragee);
    }

    private ObjectSetCanvas findCanvas() {
	// a expression panel can be on a canvas directly
	// or in a container that "knows" the canvas
	// Ideally the dragging should happen on a layer above the entire panel
	// but attempts to do some had multiple problems.
	Container parent = getParent();
	ObjectSetCanvas canvas = null;
	Container ancestor = parent;
	while (ancestor != null) {
	    if (ancestor instanceof ObjectSetCanvas) {
		canvas = (ObjectSetCanvas) ancestor;
		break;
	    } else if (ancestor instanceof GlobalColorAllocationPanel) {
		GlobalColorAllocationPanel globalColorAllocationPanel = (GlobalColorAllocationPanel) ancestor;
		canvas = globalColorAllocationPanel.getCanvas();
		break;
	    }
	    ancestor = ancestor.getParent();
	}
	return canvas;
    }
    
    protected GlobalColorAllocationPanel findGlobalColorAllocationPanelAncestor() {
	Container parent = getParent();
	Container ancestor = parent;
	while (ancestor != null) {
	    if (ancestor instanceof GlobalColorAllocationPanel) {
		return (GlobalColorAllocationPanel) ancestor;
	    }
	    ancestor = ancestor.getParent();
	}
	return null;
    }
    
    @Override
    public void dragDropEnd(DragSourceDropEvent dsde) {
//	logAction("Drag ended");
	if (dragee != null) {
	    dragee.setDragEnabled(true);
	    dragee = null;
	}
	setDragStarted(false);
	ExpresserLauncher.remove(this); 
	// if left on the canvas should not create a copy when dragged
//	setDragCreatesCopy(!onACanvas() && !isReadOnly());
//	setDropEnabled(true);
//	Container topLevelAncestor = getTopLevelAncestor();
//	if (topLevelAncestor != null) {
//	    // see Issue 582
//	    topLevelAncestor.repaint();
//	}
	ExpresserLauncher.setCurrentPanelDirty();
    }

    private boolean onACanvas() {
	Container parent = getParent();
	return parent != null && parent instanceof ObjectSetCanvas;
    }

    @Override
    public void dragEnter(DragSourceDragEvent dsde) {
    }

    @Override
    public void dragExit(DragSourceEvent dse) {
    }

    @Override
    public void dragOver(DragSourceDragEvent dsde) {
    }

    @Override
    public void dropActionChanged(DragSourceDragEvent dsde) {
    }

    public static DataFlavor getFlavor() {
        return expressionPanelDropTargetFlavor;
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
	if (expression.isSpecified()) {
	    this.dragEnabled = dragEnabled;
	    if (dragEnabled) {
		enableDragAndDrop();
		// ensure it has a border so there is somewhere to grab
		setBorder(draggableBorder);
	    } else {
		disableDragAndDrop();
	    }
	}
	// don't allow drag of unspecified numbers
    }
    
    private void disableDragAndDrop() {
	setTransferHandler(null);
	if (dragGestureRecognizer != null) {
	    dragGestureRecognizer.removeDragGestureListener(this);
	    dragGestureRecognizer = null;
	}
    }

    public boolean isDragEnabled() {
	return dragEnabled;
    }

    private void handleDropOfExpressionOnExpression(final Expression<Number> currentExpression, 
	                                            final Expression<Number> droppedExpression,
	                                            final boolean droppedOnLeftHalf,
	                                            final JComponent droppedExpressionComponent,
	                                            // TODO: combine the above and below
	                                            final ExpressionPanel<Number> droppedExpressionPanel) {
	// TODO: determine if currentExpression is identical to the expression field
	processDropEvent(currentExpression, droppedExpressionPanel);
	if (!currentExpression.isSpecified()) {
	    // is an UnspecifiedTiedNumberExpression
	    replaceWithDroppedExpression(droppedExpression, droppedExpressionComponent, droppedExpressionPanel);
	    return;
	}
	UIEventManager.processEvent(new ExpressionMenuPopupEvent(getId().toString(), toString(), "Drop"));
	JPopupMenu menu = new JPopupMenu();
	JMenuItem item;
	final String replaceLabel = MiGenUtilities.getLocalisedMessage("ReplaceExpressionLabel");
	item = new JMenuItem(replaceLabel);
	item.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		ExpressionMenuItemEvent event = 
		    new ExpressionMenuItemEvent(getId().toString(), replaceLabel);
		UIEventManager.processEvent(event);
//		logActionPerformed(replaceLabel);
		replaceWithDroppedExpression(droppedExpression, droppedExpressionComponent, droppedExpressionPanel);
	    }
	    
	});
	menu.add(item);
	final String addLabel = MiGenUtilities.getLocalisedMessage("AddToTiedNumberLabel"); 
	item = new JMenuItem(addLabel);
	item.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		ExpressionMenuItemEvent event = 
		    new ExpressionMenuItemEvent(getId().toString(), 
			                        addLabel + " " + (droppedOnLeftHalf?"(left)":"(right)"));
		UIEventManager.processEvent(event);
		if (droppedOnLeftHalf) {
		    replaceWithOperation(droppedExpression, currentExpression, new NumberAdditionOperator(), droppedExpressionPanel);
		} else {
		    replaceWithOperation(currentExpression, droppedExpression, new NumberAdditionOperator(), droppedExpressionPanel);
		}
		removeComponent(droppedExpressionComponent);
	    }
	    
	});
	menu.add(item);
	final String subtractLabelLeft = subtractToHTML(currentExpression, droppedExpression);
	item = new JMenuItem(subtractLabelLeft);
	item.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		ExpressionMenuItemEvent event = 
		    new ExpressionMenuItemEvent(getId().toString(), subtractLabelLeft);
		UIEventManager.processEvent(event);
		replaceWithOperation(droppedExpression, currentExpression, new NumberSubtractionOperator(), droppedExpressionPanel);
		removeComponent(droppedExpressionComponent);
	    }
	    
	});
	menu.add(item);
	final String subtractLabelRight = subtractToHTML(droppedExpression, currentExpression);
	item = new JMenuItem(subtractLabelRight);
	item.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		ExpressionMenuItemEvent event = 
		    new ExpressionMenuItemEvent(getId().toString(), subtractLabelRight);
		UIEventManager.processEvent(event);
		replaceWithOperation(currentExpression, droppedExpression, new NumberSubtractionOperator(), droppedExpressionPanel);
		removeComponent(droppedExpressionComponent);
	    }
	    
	});
	menu.add(item);
	final String multiplyLabel = MiGenUtilities.getLocalisedMessage("MultiplyTiedNumberLabel"); 
	item = new JMenuItem(multiplyLabel);
	item.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		ExpressionMenuItemEvent event = 
		    new ExpressionMenuItemEvent(getId().toString(), 
			                        multiplyLabel + " " + (droppedOnLeftHalf?"(left)":"(right)"));
		UIEventManager.processEvent(event);
//		logActionPerformed(multiplyLabel + " " + (droppedOnLeftHalf?"(left)":"(right)"));
		if (droppedOnLeftHalf) {
		    replaceWithOperation(droppedExpression, currentExpression, new NumberMultiplicationOperator(), droppedExpressionPanel);
		} else {
		    replaceWithOperation(currentExpression, droppedExpression, new NumberMultiplicationOperator(), droppedExpressionPanel);
		}
		removeComponent(droppedExpressionComponent);
	    }
	    
	});
	menu.add(item);
	menu.show(this, 0, getHeight());
    }

    protected String subtractToHTML(Expression<Number> expression1,
	                            Expression<Number> expression2) {
	// uses color to distinguish the two expressions
	// see Issue 1417
	return "<html>" +
	       MiGenUtilities.getLocalisedMessage("Subtract") + 
	       " (<font color='Red'>" + expression2.toHTMLString() + "</font> \u2212 " + 
	       "<font color='Blue'>" + expression1.toHTMLString() + "</font>)" +
	       "</html>";
    }

    protected void processDropEvent(
	    Expression<Number> currentExpression,
	    ExpressionPanel<Number> droppedExpressionPanel) {
	if (!currentExpression.isSpecified()) {
	    DropExpressionOnExpressionEvent event = 
		new DropExpressionOnExpressionEvent(
			getId().toString(), 
			droppedExpressionPanel.getIdentityForLogMessage(),
			getExpression().isSpecified());
	    UIEventManager.processEvent(event);
	} // otherwise other code will take care of events
    }

    @SuppressWarnings("unchecked")
    protected void replaceWithDroppedExpression(
	    Expression<Number> droppedExpression,
	    JComponent droppedExpressionComponent,
	    ExpressionPanel<Number> droppedExpressionPanel) {
//	boolean droppedOnGlobalColorAllocationPanel = partOfAColorRule();
	ObjectSetCanvas canvas = findCanvas();
	Expression<V> oldExpression = getExpression();
	boolean containsOnlyATiedNumber = oldExpression.isTiedNumber();
	removeComponent(droppedExpressionComponent);
	if (containsOnlyATiedNumber && canvas != null && MiGenConfiguration.isQueryReplaceTiedNumberEnabled()) {
	    // cast is safe since containsOnlyATiedNumber
	    TiedNumberExpression<?> tiedNumber = (TiedNumberExpression<?>) oldExpression;
	    if (tiedNumber.isSpecified()) {
		int occurrences = canvas.countOccurrencesInCanvas(tiedNumber);
		if (occurrences > 1) {
		    offerToReplaceOtherOccurrences(tiedNumber, droppedExpression, this, canvas);
		}
	    }
	}
//	if (droppedOnGlobalColorAllocationPanel) {  
//	    ExpresserModel model = canvas.getModel();
//	    setExpression((Expression<V>) slaveEquivalent(droppedExpression, model));
//	    setDragEnabled(true);
//	} else {
	    setExpression((Expression<V>) droppedExpression);
//	}
	// force the mirror in the slave universe to update
	ExpresserLauncher.setCurrentPanelDirty();
	resizeParentExpressionPanels();
    }

    protected boolean partOfAColorRule() {
	Container parent = getParent();
	return parent != null && 
	       parent instanceof GlobalColorAllocationPanel;
    }

    protected void offerToReplaceOtherOccurrences(
	    TiedNumberExpression<?> tiedNumber,
	    final Expression<Number> droppedExpression,
	    ExpressionPanel<V> expressionPanelDropTarget, 
	    ObjectSetCanvas canvas) {
	String[] options = null; //new String[3];
//	options[0] = MiGenUtilities.getLocalisedMessage("ReplaceAllOption");
//	options[1] = MiGenUtilities.getLocalisedMessage("ReplaceOneAtATimeOption");
//	options[2] = MiGenUtilities.getLocalisedMessage("DoNotReplaceOption");
	int n = JOptionPane.showOptionDialog(
		SwingUtilities.getWindowAncestor(this),
		MiGenUtilities.getLocalisedMessage("ThereAreOtherTiedNumberInstances"),
		MiGenUtilities.getLocalisedMessage("ReplaceDialogTitle"),
		JOptionPane.YES_NO_OPTION,
		JOptionPane.QUESTION_MESSAGE,
		null,
		options,
		null);
	if (n == 0) {  // yes
	    replaceAll(canvas, tiedNumber, droppedExpression, expressionPanelDropTarget, true);
	}
//	switch (n) {
//	case 0:
//	    replaceAll(canvas, tiedNumber, droppedExpression, expressionPanelDropTarget, false);
//	    break;
//	case 1:
//	    replaceAll(canvas, tiedNumber, droppedExpression, expressionPanelDropTarget, true);
//	    break;
//	}
    }
    
    protected void replaceAll(final ObjectSetCanvas canvas,
	                      final TiedNumberExpression<?> otherTiedNumber,
	                      final Expression<Number> droppedExpression,
	                      final ExpressionPanel<V> expressionPanelDropTarget, 
	                      final boolean query) {
	final ArrayList<BlockShape> shapesOpened = new ArrayList<BlockShape>();
	final ArrayList<ExpressionPanel<Number>> panelsToReplace =
	    new ArrayList<ExpressionPanel<Number>>();
	StandaloneWalker walker = new StandaloneWalker() {

	    @SuppressWarnings({ "unchecked", "rawtypes" })
	    @Override
	    public boolean tiedNumberFound(
		    TiedNumberExpression<?> tiedNumber,
		    BlockShape shape,
		    AttributeHandle<Number> handle,
		    ExpresserModel expresserModel) {
		if (otherTiedNumber == tiedNumber) {
		    if (handle != null) {
			if (shape != null) {
			    Attribute<?> attribute = shape.getAttribute(handle);
			    if (attribute != null) {
				Expression<?> attributeValueExpression = 
				    attribute.getValueSource().getExpression();
				if (query) {
				    int yesNoAbort = okToReplace(handle, shape, this, canvas, shapesOpened);
				    switch (yesNoAbort) {
				    case ABORT_REPLACEMENT:
					return false;
				    case SKIP_REPLACEMENT:
					return true;
				    }
				} else {
				    if (attributeValueExpression == tiedNumber) {
					attribute.setValueSource(new ExpressionValueSource(droppedExpression.createCopy()));
				    } else {
					attributeValueExpression.replaceAll(tiedNumber, droppedExpression);
				    }
				}
			    }
			}
		    }
		}
		return true;
	    }
	    
	    @Override
	    public boolean tiedNumberPanelFound(String expressionPanelOfTiedNumberId) {
		ExpressionPanel<Number> expressionPanelOfTiedNumber = 
		    ExpressionPanel.fetchExpressionPanelByIdString(expressionPanelOfTiedNumberId);
		if (otherTiedNumber == expressionPanelOfTiedNumber.getTiedNumberExpression()) {			
		    if (query && expressionPanelOfTiedNumber != expressionPanelDropTarget) {
			// don't ask the original target since the user has already requested that it be) {
			int yesNoAbort = okToReplace(expressionPanelOfTiedNumber);
			switch (yesNoAbort) {
			case ABORT_REPLACEMENT:
			    return false;
			case SKIP_REPLACEMENT:
			    return true;
			}
		    }
		    panelsToReplace.add(expressionPanelOfTiedNumber);
		    // updating immediately changes the model and potentially
		    // property lists that are being gone through
		    // by postponing the updates we avoid this problem
//		    expressionPanelOfTiedNumber.setExpression(droppedExpression.createCopy());
		    return true;
		}
		return super.tiedNumberPanelFound(expressionPanelOfTiedNumberId);
	    }
	    
	};
	canvas.walkToTiedNumbers(walker, false);
	for (ExpressionPanel<Number> expressionPanel  : panelsToReplace) {
	    expressionPanel.setExpression(droppedExpression.createCopy());
	}
//	BlockShapeCanvasPanel masterPanel = ExpresserLauncher.getSelectedMasterPanel();
//	masterPanel.copyAllExpressionsFromMasterToSlave();
	canvas.refreshManifests();
	ExpresserLauncher.setCurrentModelDirty();
    }

    protected int okToReplace(AttributeHandle<Number> handle, 
	                      BlockShape shape, 
	                      StandaloneWalker walker, 
	                      ObjectSetCanvas canvas,
	                      ArrayList<BlockShape> shapesOpened) {
	if (shapesOpened.contains(shape)) {
	    return CONTINUE_REPLACEMENT;
	}
	shapesOpened.add(shape);
	if (shape.isEntireModel()) {
	    // first make sure the rule is visible
	    if (handle instanceof ColorResourceAttributeHandle) {
		ColorResourceAttributeHandle colorHandle = 
		    (ColorResourceAttributeHandle) handle;
		ModelColor color = colorHandle.getColor();
		BlockShapeCanvasPanel masterPanel = ExpresserLauncher.getSelectedMasterPanel();
		if (masterPanel != null) {
		    ArrayList<GlobalColorAllocationPanel> globalColorAllocationPanels = 
			masterPanel.getGlobalColorAllocationPanels();
		    if (!globalColorAllocationPanels.isEmpty()) {
//			TotalTileAllocationPanel globalTotalAllocationPanel = 
//			    masterPanel.getGlobalTotalAllocationPanel();
//			if (globalTotalAllocationPanel != null) {
//			    ExpressionPanel<IntegerValue> expressionPanel = 
//				globalTotalAllocationPanel.getExpressionPanel();
//			    if (!expressionPanel.walkToTiedNumbers(walker)) {
//				return ABORT_REPLACEMENT;
//			    }
//			}
//		    } else {
			// first one is good enough...
			GlobalColorAllocationPanel globalColorAllocationPanel = 
			    globalColorAllocationPanels.get(0);
			if (!globalColorAllocationPanel.getColorModel().equals(color)) {
			    globalColorAllocationPanel.colorChosen(color);
			}
			ExpressionPanel<Number> expressionPanel = 
			    globalColorAllocationPanel.getExpressionPanel();
			if (!expressionPanel.walkToTiedNumbers(walker)) {
			    return ABORT_REPLACEMENT;
			}
		    }	    
		}	
	    }
	} else {
	    boolean newManifest = 
		(AttributeManifest.manifestOpenForShape(canvas, shape) == null);
	    AttributeManifest manifest = 
		AttributeManifestBehaviour.createManifest(shape, canvas, true, false, true);
	    canvas.setManifestLocation(shape, manifest);
	    boolean keepGoing = 
		ObjectSetCanvas.walkContainerToTiedNumbers(manifest.getContentPane(), 
			                                   walker,
			                                   false);
	    if (newManifest) {
		canvas.remove(manifest);
	    }
	    if (!keepGoing) {
		return ABORT_REPLACEMENT;
	    }
	}
	return CONTINUE_REPLACEMENT;
    }

    protected int okToReplace(final ExpressionPanel<Number> expressionPanelOfTiedNumber) {
	Color originalBackground = expressionPanelOfTiedNumber.getBackground();
	java.util.Timer timer = expressionPanelOfTiedNumber.createHighlightTimer(originalBackground);
	int result = yesNoAbort("OKToReplace", "OKToReplaceTitle");
	timer.cancel();
	expressionPanelOfTiedNumber.setBackground(originalBackground);  
	return result;
    }
    
//    protected Timer createHighlightTimer(final Color originalBackground) {
//	final Runnable alternateVisibility = new Runnable() {
//	    @Override
//	    public void run() {
//		Color background = getBackground();
//		if (background == originalBackground) {
//		    setBackground(Color.RED);
//		} else {
//		    setBackground(originalBackground);    
//		}
//	    }};
//	// Note to KK: please try to decouple the call to setBackground(originalBackground)
//	// from the timer. Having a timer inside a timer is quite a hack (and we cannot
//	// use java.util.Timer directly due to Gwt incompatibility). :-( -- SG, Jul 2011
//	Timer wrappingTimer = new Timer() {
//	    TimerFactory tf = FactoryRepository.getTimerFactory();
//	    Timer wrappedTimer = tf.getTimer(alternateVisibility);
//	    @Override
//	    public void schedule(int period) {
//		wrappedTimer.schedule(period);
//	    }
//	    @Override
//	    public void cancel() {
//		wrappedTimer.cancel();
//		setBackground(originalBackground);
//	    }};
//	return wrappingTimer;
//    }
    
    protected java.util.Timer createHighlightTimer(final Color originalBackground) {
	java.util.Timer timer = new java.util.Timer() {
	    
	    @Override
	    public void cancel() {
		super.cancel();
		setBackground(originalBackground); 
	    }
	};
	TimerTask alternateVisibility = new TimerTask() {

	    @Override
	    public void run() {
		Color background = getBackground();
		if (background == originalBackground) {
		    setBackground(Color.RED);
		} else {
		    setBackground(originalBackground);    
		}
	    }
	    
	};
	timer.scheduleAtFixedRate(alternateVisibility, 500, 500);
	return timer;
    }
    
    private int yesNoAbort(String messageName, String titleName) {
	String[] options = null;
	return JOptionPane.showOptionDialog(
		SwingUtilities.getWindowAncestor(this),
		MiGenUtilities.getLocalisedMessage(messageName),
		MiGenUtilities.getLocalisedMessage(titleName),
		JOptionPane.YES_NO_CANCEL_OPTION,
		JOptionPane.QUESTION_MESSAGE,
		null,
		options,
		null);
    }

    public static void acquireFocusForAncestorAttributeManifest(Component ancestor) {
	AttributeManifest manifest = findAncestorAttributeManifest(ancestor);
	if (manifest != null) {
	    manifest.acquireFocus();
	}
    }

    public static AttributeManifest findAncestorAttributeManifest(Component ancestor) {
	while (ancestor != null) {
	    if (ancestor instanceof AttributeManifest) {
		return (AttributeManifest) ancestor;
	    }
	    ancestor = ancestor.getParent();
	}
	return null;	
    }

    @SuppressWarnings("unchecked")
    private void replaceWithOperation(Expression<Number> leftExpression, 
	                              Expression<Number> rightExpression, 
	                              Operator<Number, Number> operator,
	                              ExpressionPanel<Number> droppedExpressionPanel) {
	ExpressionList<Number> operands;
//	boolean droppedOnGlobalColorAllocationPanel = partOfAColorRule();
//	if (droppedOnGlobalColorAllocationPanel) {
//	    ObjectSetCanvas canvas = findCanvas();
//	    ExpresserModel model = canvas.getModel();
//	    Expression<IntegerValue> rightSlaveEquivalent = slaveEquivalent(rightExpression, model);
//	    Expression<IntegerValue> leftSlaveEquivalent = slaveEquivalent(leftExpression, model);
//	    operands = new ExpressionList<IntegerValue>(leftSlaveEquivalent, rightSlaveEquivalent);
//	} else {
	    operands = new ExpressionList<Number>(leftExpression, rightExpression);
//	}
	@SuppressWarnings("rawtypes")
	Expression<V> replacementExpression = new ModifiableOperation(operator, operands);
	setExpression(replacementExpression);
	// force the mirror in the slave universe to update
	ExpresserLauncher.setCurrentPanelDirty();
//	if (slaveDragee != null) {
//	    if (droppedOnGlobalColorAllocationPanel) {
//		removeSlaveDragee();
//	    } else if (droppedExpressionPanel != null) {
//		ObjectSetCanvas slaveCanvasOfDroppedPanel = droppedExpressionPanel.getSlaveCanvas();
//		if (slaveCanvasOfDroppedPanel != null) {
//		    ExpresserModel model = slaveCanvasOfDroppedPanel.getModel();
//		    slaveDragee.setExpression(slaveEquivalent((Expression<IntegerValue>) replacementExpression, model));
//		}
//	    }
//	    droppedExpressionPanel.removeSlaveDragee();
//	}
	resizeParentExpressionPanels();
    }
    
    @SuppressWarnings("unchecked")
    private void resizeParentExpressionPanels() {
	Container parent = getParent();
	if (parent != null) {
	    if (parent instanceof ExpressionPanel) {
		ExpressionPanel<Number> parentExpressionPanel = 
		    (ExpressionPanel<Number>) parent;
		parentExpressionPanel.setSize(parentExpressionPanel.getPreferredSize());
		parentExpressionPanel.resizeParentExpressionPanels();
	    }
	} else {
	    ExpresserLauncher.repaint();
	}
    }
    
    // TODO: move this to some library
    public static void removeComponent(JComponent droppedExpressionComponent) {
	Container parent = droppedExpressionComponent.getParent();
	if (parent != null) {
//	    if (parent instanceof ObjectSetCanvas) {
//		ObjectSetCanvas canvas = (ObjectSetCanvas) parent;
//		ExpresserModel model = canvas.getModel();
//		if (droppedExpressionComponent instanceof ExpressionPanel) {
//		    model.removeLocatedExpression(((ExpressionPanel) droppedExpressionComponent).getLocatedExpression());
//		} else if (droppedExpressionComponent instanceof TiedNumberPanel) {
//		    model.removeLocatedExpression(((TiedNumberPanel) droppedExpressionComponent).getLocatedExpression());
//		}
//	    }
	    parent.remove(droppedExpressionComponent);
	    parent.repaint();
	}
    }

    public Expression<V> getExpression() {
        return expression;
    }
    
    /**
     * This differs from setExpression in that it may create UnspecifiedTiedNumbers
     * 
     * @param expression
     */
    public void initialiseExpression(Expression<V> expression) {
	this.expression = null;
        unspecifiedTiedNumberPanels.clear();
	setExpression(expression);
    }

    @SuppressWarnings("unchecked")
    public void setExpression(Expression<V> expression) {
	if (this.expression != null) {
	    removeExpressionListeners();
//	    this.expression = null;
//	    fireObjectUpdated();
	}
        this.expression = expression;
        removeListenersFromChildren();
        removeAll();
        ObjectSetCanvas canvas = findCanvas();
        ExpresserModel model = canvas.getModel();
	if (canvas != null) {
            model.removeLocatedExpression((LocatedExpression<Number>) locatedExpression);      
        }
        locatedExpression = null; // recomputed as needed
        if (expression != null) {
            addToPanel();
//            getLocatedExpression().setExpression(expression);
//        } else {
//            ObjectSetCanvas canvas = findCanvas();
//            if (canvas != null) {
//        	canvas.getModel().removeLocatedExpression((LocatedExpression<IntegerValue>) getLocatedExpression());
//            }
        }
        fireObjectUpdated();
        if (canvas != null && getParent() == canvas) {
            model.addLocatedExpression((LocatedExpression<Number>) getLocatedExpression());
        }
    }

    protected void removeListenersFromChildren() {
	int componentCount = getComponentCount();
	for (int i = 0; i < componentCount; i++) {
	    Component component = getComponent(i);
	    if (component instanceof ExpressionPanel) {
		@SuppressWarnings("rawtypes")
		ExpressionPanel expressionPanel = (ExpressionPanel) component;
		expressionPanel.removeDragSourceListener();
	    } else if (component instanceof TiedNumberPanel) {
		@SuppressWarnings("rawtypes")
		TiedNumberPanel tiedNumberPanel = (TiedNumberPanel) component;
		tiedNumberPanel.removeListeners();
	    }
	}
    }

    @SuppressWarnings("unchecked")
    private void removeExpressionListeners() {
	if (expression instanceof TiedNumberExpression) {
	    @SuppressWarnings("rawtypes")
	    TiedNumberExpression tiedNumber = (TiedNumberExpression) expression;
	    tiedNumber.removeDisplayModeUpdateListener(preferredSizeUpdateListener);
	    tiedNumber.removeUpdateListener(preferredSizeUpdateListener);
	    tiedNumber.removeNameFieldUpdateListener(preferredSizeUpdateListener);
	}
    }
    
    public void fireObjectUpdated() {
	updateSupport.fireObjectUpdated();
    }
    
    public void addUpdateListener(UpdateListener<ExpressionPanel<V>> listener) {
	updateSupport.addListener(listener);
    }

    public void removeUpdateListener(UpdateListener<ExpressionPanel<V>> listener) {
	updateSupport.removeListener(listener);
    }
    
    /**
     * adds a listener to a sub-expression so that when it is updated
     * this changes correspondingly
     * 
     * @param operandIndex
     */
    public void addSubExpressionListener(final int operandIndex, 
	                                 final ModifiableOperation<Number, Number> operation,
	                                 final ExpressionPanel<Number> parentPanel) {
	// TODO: make this weak?
	UpdateListener<ExpressionPanel<V>> listener =
	    new UpdateListener<ExpressionPanel<V>>() {

		@Override
		@SuppressWarnings("unchecked")
		public void objectUpdated(UpdateEvent<ExpressionPanel<V>> e) {
		    Expression<Number> subExpression = (Expression<Number>) e.getSource().getExpression();
		    if (subExpression != null) {
			operation.setOperand(operandIndex, subExpression);
			parentPanel.fireObjectUpdated();
		    }
		}
	    
	};
	addUpdateListener(listener);
    }
    
    protected void addPopupMenuOnClick() {
	addMouseListener(new MouseListener() {

	@Override
	public void mouseClicked(MouseEvent e) {	
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	    ExpressionPanel.this.setCursor(new Cursor(Cursor.HAND_CURSOR));
	}

	@Override
	public void mouseExited(MouseEvent e) {
	    ExpressionPanel.this.setCursor(Cursor.getDefaultCursor());
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	    addPopupMenu();
	}

	});
    }
    
    public JPopupMenu addPopupMenu() {
	if (!expression.isSpecified()) {
	    // no menu for unspecified numbers
	    return null;
	}
	TiedNumberPanel<Number> tiedNumberPanel = getTiedNumberPanel();
	if (tiedNumberPanel != null) {
	    if (!isReadOnly()) {
		tiedNumberPanel.addPopupMenu();
	    }
	    return null;
	}
	UIEventManager.processEvent(new ExpressionMenuPopupEvent(getId().toString(), toString(), "Main"));
	JPopupMenu menu = new JPopupMenu();
	menu.add(evaluateMenuItem());
	JMenuItem copyExpressionMenuItem = copyExpressionMenuItem();
	copyExpressionMenuItem.setEnabled(!readOnly || displayCurrentValue);
	menu.add(copyExpressionMenuItem);
	JMenuItem removeExpressionMenuItem = removeExpressionMenuItem();
	menu.add(removeExpressionMenuItem);
	// if displayCurrentValue then need to be able to delete it
	removeExpressionMenuItem.setEnabled(!readOnly || displayCurrentValue);
	menu.show(this, 0, 0);
	return menu;
//	final String evaluateLabel = MiGenUtilities.getLocalisedMessage("EvaluateExpression");
//	item = new JMenuItem(evaluateLabel);
//	if (getExpression().isConstant()) {
//	    item.addActionListener(new ActionListener() {
//
//		public void actionPerformed(ActionEvent e) {
//		    logActionPerformed(evaluateLabel);
//		    // TODO: super expressions need to listen to this
//		    setExpression(new TiedNumberExpression<V>(getExpression().evaluate()));
//		    Container topLevelAncestor = ExpressionPanel.this.getTopLevelAncestor();
//		    ExpressionPanel<V> topLevelExpressionPanel = getTopLevelExpressionPanel();
//		    if (topLevelExpressionPanel != null) {
//			topLevelExpressionPanel.recompute();
//		    }
//		    if (topLevelAncestor != null) {
//			topLevelAncestor.repaint();
//		    }
//		    LauncherModelCopier.setCurrentModelDirty();
//		}
//	    });
//	} else {
//	    item.setEnabled(false);
//	}
//	menu.add(item);
    }

    protected boolean okToRemoveExpression() {
	// subclasses may disable it
	return true;
    }

    protected JMenuItem copyExpressionMenuItem() {
	final String copyLabel = MiGenUtilities.getLocalisedMessage("CopyExpression");
	JMenuItem item = new JMenuItem(copyLabel);
	item.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		ExpressionMenuItemEvent event = 
		    new ExpressionMenuItemEvent(getId().toString(), copyLabel);
		UIEventManager.processEvent(event);
//		logActionPerformed(copyLabel);
		ExpressionPanel<V> copy = ExpressionPanel.this.createCopy();
		ObjectSetCanvas canvas = getCanvas();
		canvas.add(copy);
		copy.setLocation(locationForCopy(canvas));
		copy.setDragCreatesCopy(false);
		canvas.setComponentZOrder(copy, 0);
//		if (copy.getSlaveCanvas() != null) {
//		    copy.createSlaveMirror();
//		}
	    }
	});
	return item;
    }

    /**
     * @return the menu item for adding the
     * current value to the canvas of this panel
     */
    protected JMenuItem evaluateMenuItem() {
	final String evaluateLabel = MiGenUtilities.getLocalisedMessage("AddValueToCanvas");
	JMenuItem item = new JMenuItem(evaluateLabel);
	item.addActionListener(new ActionListener() {

	    @Override
	    @SuppressWarnings("unchecked")
	    public void actionPerformed(ActionEvent e) {
		ExpressionMenuItemEvent event = 
		    new ExpressionMenuItemEvent(getId().toString(), evaluateLabel);
		UIEventManager.processEvent(event);
//		logActionPerformed(evaluateLabel);
		ObjectSetCanvas canvas = getCanvas();
		@SuppressWarnings("rawtypes")
		ExpressionPanel tiedNumberPanel = new ExpressionPanel(getExpression(), false, true);
		tiedNumberPanel.setDragCreatesCopy(false);
//		tiedNumberPanel.setDropEnabled(true);
		canvas.add(tiedNumberPanel);
		// make sure it is on top
		canvas.setComponentZOrder(tiedNumberPanel, 0);
		tiedNumberPanel.setLocation(locationForCopy(canvas));
		tiedNumberPanel.setSize(tiedNumberPanel.getPreferredSize());
//		LauncherModelCopier.setCurrentModelDirty();
	    }
	});
	return item;
    }
    
    @SuppressWarnings("unchecked")
    protected JMenuItem removeExpressionMenuItem() {
	final String removeExpressionLabel = 
	    MiGenUtilities.getLocalisedMessage("RemoveExpressionFromCanvas");
	JMenuItem item = new JMenuItem(removeExpressionLabel);
	final Container parent = ExpressionPanel.this.getParent();
	final boolean onACanvas = onACanvas();
	if (isReadOnly() && 
		// if top-level can delete even if read-only
	     parent instanceof ExpressionPanel) {
	    item.setEnabled(false);
	} else {
	    item.addActionListener(new ActionListener() {

		@Override
		@SuppressWarnings("rawtypes")
		public void actionPerformed(ActionEvent e) {
		    ExpressionMenuItemEvent event = 
			new ExpressionMenuItemEvent(getId().toString(), removeExpressionLabel);
		    UIEventManager.processEvent(event);
		    if (!onACanvas && parent instanceof ExpressionPanel) {
			ExpressionPanel<Number> expressionPanelParent = (ExpressionPanel<Number>) parent;
			if (expressionPanelParent.okToRemoveExpression()) {
			    removeSubExpression(
				    (ExpressionPanel<Number>) ExpressionPanel.this, 
				    expressionPanelParent);
			} else {
			    setExpression(new UnspecifiedTiedNumberExpression());
			}
		    } else if (partOfAColorRule() || parent instanceof AttributeManifestRow || !okToRemoveExpression()) {
			setExpression(new UnspecifiedTiedNumberExpression());
		    } else {
			parent.remove(ExpressionPanel.this);
			setExpression(null); // removes listeners etc.
			removeDragSourceListener();
		    }
		    parent.validate();
		    parent.repaint();
		    removeExpressionListeners();
		    ExpresserLauncher.setCurrentModelDirty();
		}
	    });
	}
	return item;
    }
    
    protected Point locationForCopy(ObjectSetCanvas canvas) {
	int expressionWidth = getWidth();
	int expressionHeight = getHeight();
	Point location = getLocation();
	location = SwingUtilities.convertPoint(getParent(), location, canvas);
	Rectangle canvasBounds = canvas.getBounds();
	location.translate(expressionWidth, expressionHeight);
	boolean containedOnCanvas = canvasBounds.contains(location);
	if (!containedOnCanvas) {
	    location.x = canvasBounds.x + canvasBounds.width/2;
	    location.y = canvasBounds.y + canvasBounds.height/2;
//	    int canvasMaxX = canvasBounds.x+canvasBounds.width;
//	    if (location.x > canvasMaxX) {
//		location.x = canvasMaxX-expressionWidth;
//	    } else if (location.x < canvasBounds.x) {
//		location.x = canvasBounds.x;
//	    }
//	    int canvasMaxY = canvasBounds.y+canvasBounds.height;
//	    if (location.y > canvasMaxY) {
//		location.y = canvasMaxY-expressionHeight;
//	    } else if (location.y < canvasBounds.y) {
//		location.y = canvasBounds.y;
//	    }
	}
	return location;
    }
    
    @SuppressWarnings("unchecked")
    protected ExpressionPanel<V> getTopLevelExpressionPanel() {
	Container parent = getParent();
	while (parent != null) {
	    if (parent instanceof ExpressionPanel) {
		return (ExpressionPanel<V>) parent;
	    }
	    parent = parent.getParent();
	}
	return null;
    }

    private ObjectSetCanvas getCanvas() {
	Container parent = getParent();
	while (parent != null) {
	    if (parent instanceof ObjectSetCanvas) {
		return (ObjectSetCanvas) parent;
	    } else if (parent instanceof ObjectCanvasPanel) {
		// e.g. the expression panel is in the rule area
		return ((ObjectCanvasPanel) parent).getCanvas();
	    }
	    parent = parent.getParent();
	}
	return null;
    }

    public ArrayList<UnspecifiedTiedNumberPanel<Number>> getUnspecifiedTiedNumberPanels() {
        return unspecifiedTiedNumberPanels;
    }

    public boolean isReadOnly() {
        return readOnly;
    }
    
    public Point getSlaveLocation() {
	if (slaveLocation  == null) {
	    return getLocation();
	} else {
	    return slaveLocation;
	}
    }

    public void setSlaveLocation(Point slaveLocation) {
        this.slaveLocation = slaveLocation;
    }
    
//    private void logActionPerformed(String menuLabel) {
//	addIdentityToLogMessage("Clicked '" + menuLabel + "'");
//    }
    
    public String getIdentityForLogMessage() {
	if (expression == null) {
	    return null;
	}
	if (expression.isTiedNumber()) {
	    if (containedInMasterPanel == null) {
		ExpresserModelPanel selectedModelPanel = 
		    ExpresserLauncher.getModelCopyTabbedPanel().getSelectedModelPanel();
		if (selectedModelPanel instanceof MasterSlaveUniverseMicroworldPanel) {
		    BlockShapeCanvasPanel masterPanel = 
			((MasterSlaveUniverseMicroworldPanel) selectedModelPanel).getMasterPanel();
		    containedInMasterPanel = masterPanel.isAncestorOf(this);
		} else {
		    containedInMasterPanel = false;
		}    
	    }
	    if (containedInMasterPanel) {
		return logIdentity();
//	    } else {
//		return null;
	    }
	}
	return " (" + expression.getId() + " " + expression.toString() + ")";
    }
    
    protected String logIdentity() {
	TiedNumberExpression<Number> tiedNumber = getTiedNumberExpression();
	if (tiedNumber != null) {
	    return " (" + tiedNumber.getId() + " TiedNumber" + tiedNumber.getIdString()
	           + " value: " + tiedNumber.toString() + " name: "
	           + tiedNumber.getName() + ")";    
	} else {
	    return " (" + expression.getId() + " " + expression.toString() + ")";
	}
    }
    
//    protected void logMenuPoppedUp(String kind) {
//	StandaloneViewLogger.sessionJuly08Log(kind + " menu popped up (" + expression.getId() + " " + expression.toString() + ")");
//    }

    public ExpressionPanel<Number> getLeftSubExpressionPanel() {
        return leftSubExpressionPanel;
    }

    public ExpressionPanel<Number> getRightSubExpressionPanel() {
        return rightSubExpressionPanel;
    }

    public boolean walkToTiedNumbers(StandaloneWalker walker) {
	return ObjectSetCanvas.walkContainerToTiedNumbers(this, walker, false);
    }

    @SuppressWarnings("unchecked")
    public TiedNumberExpression<Number> getTiedNumberExpression() {
	if (expression != null && expression.isTiedNumber()) {
	    return (TiedNumberExpression<Number>) expression;
	} else {
	    return null;
	}
    }
    
    @SuppressWarnings("unchecked")
    public TiedNumberPanel<Number> getTiedNumberPanel() {
	if (expression != null && expression.isTiedNumber()) {
	    Component component = this.getComponent(0);
	    if (component instanceof TiedNumberPanel) {
		return (TiedNumberPanel<Number>) component;
	    }
	}
	return null;
    }

    public JPanel getPanelStartingDrag() {
        return panelStartingDrag;
    }

    public void setPanelStartingDrag(JPanel panelStartingDrag) {
        this.panelStartingDrag = panelStartingDrag;
    }

    public static boolean isDragStarted() {
        return dragStarted;
    }

    public static void setDragStarted(boolean dragStarted) {
        ExpressionPanel.dragStarted = dragStarted;
    }

    public void updateSizeOfAncestorExpressionPanels() {
	setSize(getPreferredSize());
	Container parent = getParent();
	if (parent instanceof ExpressionPanel<?>) {
	    ((ExpressionPanel<?>) parent).updateSizeOfAncestorExpressionPanels();
	}
    }
    
    public boolean isOnACanvas() {
	Container parent = getParent();
	return parent != null && parent instanceof ObjectSetCanvas;
    }

    public boolean displayCurrentValue() {
        return displayCurrentValue;
    }

    protected void updateCurrentValue(
	    final TiedNumberPanel<Number> currentValuePanel) {
	if (expression != null) {
	TiedNumberExpression<Number> tiedNumberValue =
	    new TiedNumberExpression<Number>((Number) expression.evaluate());
	currentValuePanel.setTiedNumber(tiedNumberValue);
	}
    }

    public void wrong(boolean wrong) {
	if (expression == null) {
	    return;
	}
	if (!expression.isSpecified()) {
	    return;
	}
	if (wrong) {
	    setBorder(badBorder);
	} else {
	    setBorder(goodBorder);
	}
    }
    
    public void recompute() {
	ObjectSetCanvas canvas = findCanvas();
	ExpresserModel model = canvas==null ? null : canvas.getModel();
	if (model != null) {
	    setExpression(expression);
	}
    }

    @Override
    public String getIdName() {
	return "ExpressionPanel";
    }

    @Override
    public ID getId() {
        return id;
    }

    public LocatedExpression<V> getLocatedExpression() {
	if (locatedExpression == null) {
	    locatedExpression = new LocatedExpression<V>(expression, getX(), getY());
	    // and make sure it is kept up-to-date
	    ComponentListener componentListener = new ComponentListener() {

		@Override
		public void componentHidden(ComponentEvent e) {	    
		}

		@Override
		public void componentMoved(ComponentEvent e) {
		    getLocatedExpression().setX(getX());
		    getLocatedExpression().setY(getY());
		}

		@Override
		public void componentResized(ComponentEvent e) {
		}

		@Override
		public void componentShown(ComponentEvent e) {

		}
	    };
	    this.addComponentListener(componentListener);
	}
	return locatedExpression;
    }

    public static void removeSubExpression(
	    ExpressionPanel<Number> removee, 
	    ExpressionPanel<Number> parentPanel) {
	if (parentPanel.getLeftSubExpressionPanel() == removee) {
	    parentPanel.setExpression(parentPanel.getRightSubExpressionPanel().getExpression());
	} else if (parentPanel.getRightSubExpressionPanel() == removee) {
	    parentPanel.setExpression(parentPanel.getLeftSubExpressionPanel().getExpression());
	}
	Container grandParent = parentPanel.getParent();
	if (grandParent != null) {
	    grandParent.setSize(grandParent.getPreferredSize());
	}
	ExpresserLauncher.setCurrentPanelDirty();
    }
    
    public void register() {
	registeredExpressionPanels.put(id.toString(), this);
    }
    
    public void deregister() {
	registeredExpressionPanels.remove(id.toString());
    }

    @SuppressWarnings("unchecked")
    public static ExpressionPanel<Number> fetchExpressionPanelByIdString(String id) {
	return (ExpressionPanel<Number>) registeredExpressionPanels.get(id.toString());
    }
    
}

