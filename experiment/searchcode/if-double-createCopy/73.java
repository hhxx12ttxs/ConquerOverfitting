/**
 * 
 */
package uk.ac.lkl.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import uk.ac.lkl.common.util.BoundingBox;
import uk.ac.lkl.common.util.Dimension;
import uk.ac.lkl.common.util.Distance2D;
import uk.ac.lkl.common.util.Location;
import uk.ac.lkl.common.util.expression.LocatedExpression;
import uk.ac.lkl.common.util.value.Number;
import uk.ac.lkl.migen.system.expresser.model.AllocatedColor;
import uk.ac.lkl.migen.system.expresser.model.AttributeHandle;
import uk.ac.lkl.migen.system.expresser.model.ExpresserModel;
import uk.ac.lkl.migen.system.expresser.model.Glue;
import uk.ac.lkl.migen.system.expresser.model.Walker;
import uk.ac.lkl.migen.system.expresser.model.shape.block.BlockShape;
import uk.ac.lkl.migen.system.expresser.model.tiednumber.TiedNumberExpression;
import uk.ac.lkl.migen.system.expresser.ui.uievent.BuildingBlockCreationEvent;
import uk.ac.lkl.migen.system.expresser.ui.uievent.TileCreationEvent;
import uk.ac.lkl.migen.system.expresser.ui.uievent.UIEventManager;
import uk.ac.lkl.migen.system.util.gwt.SharedMigenUtilities;
import uk.ac.lkl.migen.system.util.gwt.UUID;

import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.WidgetCollection;

/**
 * This is the panel where shape views, expression panels, and property lists are placed
 * 
 * @author Ken Kahn
 *
 */
public class ExpresserCanvasPanel extends AbsolutePanel {
    
    private int width;
    private int height;
    private PickupDragControllerEnhanced gridConstrainedDragController;
    private PickupDragControllerEnhanced unconstrainedDragController;
    private ExpresserCanvas expresserCanvas;
    protected ExpresserModel model;
    private EventManager eventManager;
    private int gridSize; 
    private GridLinesPanel gridPanel = new GridLinesPanel();
    private ArrayList<ShapeView> selectedShapes = new ArrayList<ShapeView>();
    private ShapeView shapeViewWithPopupMenu;
    private boolean dropEnabled;
    private GridConstrainedSelectionStyleDropController gridConstrainedDropController;
//    private UnlockedTiedNumbersControlPanel unlockedTiedNumbersControlPanel;
    
    public ExpresserCanvasPanel(int gridSize, int width, int height, ExpresserCanvas expresserCanvas, ExpresserModel model, boolean dragAndDropEnabled) {
	super();
	this.width = width;
	this.height = height;
	this.expresserCanvas = expresserCanvas;
	this.model = model;
	if (!URLParameters.isThumbnail())  {
	    eventManager = new EventManager(this);
	}
	setGridSize(gridSize);
	setPixelSize(width, height);
	if (dragAndDropEnabled) {
	    enableDragAndDrop();
	}
	setStylePrimaryName("expresser-canvas-panel");
	// following added due to 	    
	// Warning: com.google.gwt.user.client.ui.AbsolutePanel descendants will be incorrectly positioned, 
	// i.e. not relative to their parent element, when 'position:static', which is the CSS default, 
	// is in effect. One possible fix is to call 'panel.getElement().getStyle().setPosition(Position.RELATIVE)'.)
	// but not clear it helps
	getElement().getStyle().setPosition(Position.RELATIVE);
    }

//    public void positionUnlockedTiedNumbersControlPanel() {
//	if (unlockedTiedNumbersControlPanel == null) {
//	    unlockedTiedNumbersControlPanel = new UnlockedTiedNumbersControlPanel(this);
//	    RootPanel.get().add(unlockedTiedNumbersControlPanel);
//	}
////	int left = getWidth()-unlockedTiedNumbersControlPanel.getOffsetWidth();
//	int left = Window.getClientWidth()-(unlockedTiedNumbersControlPanel.getOffsetWidth()+20);
//	RootPanel.get().setWidgetPosition(unlockedTiedNumbersControlPanel, left, 24);
//    }

    private void enableDragAndDrop() {
	dropEnabled = true;
	gridConstrainedDragController = new PickupDragControllerEnhanced(this, true);
	gridConstrainedDragController.setBehaviorMultipleSelection(true);
//	dragController.setBehaviorConstrainedToBoundaryPanel(true);
	unconstrainedDragController = Expresser.instance().getExpressionDragController(); // new PickupDragControllerEnhanced(this, true);
	// ignore small movements to enable pop up menus
	gridConstrainedDragController.setBehaviorDragStartSensitivity(Expresser.DRAG_SENSITIVITY);
	unconstrainedDragController.setBehaviorDragStartSensitivity(Expresser.DRAG_SENSITIVITY);
    }
    
    @Override
    public void onLoad() {
	// ensure that drop controller is registered when attached
	super.onLoad();
	if (dropEnabled) {
	    gridConstrainedDropController 
		  = new GridConstrainedSelectionStyleDropController(this, gridSize, gridSize); 
	    gridConstrainedDragController.registerDropController(gridConstrainedDropController);
	}
    }
    
    @Override
    public void onUnload() {
	// ensure that drop controller is unregistered when detached
	super.onUnload();
	if (gridConstrainedDropController != null) {
	    gridConstrainedDragController.unregisterDropController(gridConstrainedDropController);
	    gridConstrainedDropController = null;
	}
    }
    
    public void recomputePixelSize() {
	setPixelSize(width, height);
    }
    
    @Override
    public void setPixelSize(int newWidth, int newHeight) {
	if (newWidth < 0 || newHeight < 0) {
	    System.err.println("ignoring negative dimensions in setPixelSize.");
	    return;
	}
	if (URLParameters.isThumbnail()) {
	    super.setPixelSize(newWidth, newHeight);
	    return;
	}
	BoundingBox boundingBox = getBoundingBoxAdjustedForPreferredSize(newWidth, newHeight);
	int minX = boundingBox.getMinX();
	expresserCanvas.setXOffset(Math.max(expresserCanvas.getXOffset(), -minX));
	int minY = boundingBox.getMinY();
	expresserCanvas.setYOffset(Math.max(expresserCanvas.getYOffset(), -minY));
	int widthNeeded = boundingBox.getWidth()-1;
	int heightNeeded = boundingBox.getHeight()-1;
	boolean needExtraWidth = widthNeeded > newWidth;
	boolean needExtraHeight = heightNeeded > newHeight;
	if (needExtraWidth) {
	    newWidth = widthNeeded;
	} else if (needExtraHeight) {
	    // need to scroll vertically so leave room for scroll bar
	    newWidth -= Expresser.VERTICAL_SCROLL_BAR_WIDTH;
	}
	if (needExtraHeight) {
	    newHeight = heightNeeded;
	} else if (horizontallyScrolling()) {
	    // need to scroll horizontally so leave room for scroll bar
	    newHeight -= Expresser.HORIZONTAL_SCROLL_BAR_HEIGHT;
	}
	super.setPixelSize(newWidth, newHeight);
	gridPanel.setPixelSize(newWidth, newHeight);
	if (this.width == newWidth && this.height == newHeight) {
	    return;
	}
	boolean firstTime = this.height == 0 && this.width == 0;
	this.width = newWidth;
	this.height = newHeight;
	if (!firstTime) {
	    // no need to do this if just created grid lines
	    adjustGridLines(newWidth, newHeight);
	}
	ensurePropertyListsStillVisible();
    }
    
    private void ensurePropertyListsStillVisible() {
	WidgetCollection children = getChildren();
	for (Widget child : children) {
	    if (child instanceof PropertyList) {
		PropertyList propertyList = (PropertyList) child;
		int left = propertyList.getLeftInsideCanvas(this);	               
		int top = propertyList.getTopInsideCanvas(this);
		add(propertyList, left, top);
	    }
	}	
    }

    private boolean horizontallyScrolling() {
	if (getParent() instanceof ScrollPanel) {
	    ScrollPanel scrollPanel = (ScrollPanel) getParent();
	    return scrollPanel.getOffsetWidth() < getOffsetWidth();
	}
	return false;
    }

    private BoundingBox getBoundingBox(int w, int h) {
	int left = getAbsoluteLeft();
	int top = getAbsoluteTop();
	BoundingBox boundingBox = new BoundingBox(left, top, left+w, top+h);
	int widgetCount = getWidgetCount();
	for (int i = 0; i < widgetCount; i++) {
	    Widget widget = getWidget(i);
	    if (widget instanceof HasBoundingBox) {
		BoundingBox widgetBoundingBox = ((HasBoundingBox) widget).getBoundingBox();
		// translate from absolute coordinates to canvas relative
//		widgetBoundingBox.translate(-left, -top);
		boundingBox.extendToInclude(widgetBoundingBox);
	    }  // TODO: decide whether it makes sense to add expression and property list panels
	}
	return boundingBox;
    }

    private BoundingBox getBoundingBoxAdjustedForPreferredSize(int w, int h) {
	Dimension preferredSize = expresserCanvas.getPreferredSize();
	BoundingBox canvasBoundingBox = getBoundingBox(w, h);
	if (preferredSize != null) {
	    canvasBoundingBox.setMaxX(canvasBoundingBox.getMinX()+preferredSize.width);
	    canvasBoundingBox.setMaxY(canvasBoundingBox.getMinY()+preferredSize.height);   
	}
	return canvasBoundingBox;
    }

//    protected void addGridLines() {
//	// draw the grid lines at gridSize-1 so that the tiles themselves
//	// can be from 0, 0 and have a size of gridSize-1
////	int bottomLeftBlank = expresserCanvas.getRuleAreaHeight();
////	int visibleHeight = height-bottomLeftBlank;
////	if (visibleHeight < 1) {
////	    return;
////	}
//	for (int i = gridSize-1; i <= width; i += gridSize) {
//	    gridPanel.add(new VerticalGridLine(height), i, 0);
//	}	
//	for (int j = gridSize-1; j <= height; j += gridSize) {
//	    gridPanel.add(new HorizontalGridLine(width), 0, j);
//	}
//    }
    
    /**
     * called when canvas dimensions changed
     * adjusts the length of existing grid lines and
     * removes or adds grid lines as needed
     * @param desiredWidth 
     * @param desiredHeight 
     */
    protected void adjustGridLines(int desiredWidth, int desiredHeight) {
	// this draws grid lines under the rule area -- not a problem
	// and then when resized those lines are there
	int newWidth = desiredWidth;
	int newHeight = desiredHeight;
	if (width < newWidth) {
	    newWidth = width;
	}
	if (height < newHeight) {
	    newHeight = height;
	}
	int lastX = 0;
	int lastY = 0;
	// TODO: determine if it makes sense to do the following if not attached
	int widgetCount = gridPanel.getWidgetCount();
	// count backwards since earlier version might remove some widgets here
	for (int i = widgetCount-1; i >= 0; i--) {
	    Widget widget = gridPanel.getWidget(i);
	    if (widget instanceof VerticalGridLine) {
		widget.setHeight(newHeight + "px");
		lastX = Math.max(lastX, gridPanel.getWidgetLeft(widget)); // widget.getAbsoluteLeft()
	    } else if (widget instanceof HorizontalGridLine) {
		widget.setWidth(newWidth + "px");
		lastY = Math.max(lastY, gridPanel.getWidgetTop(widget)); // widget.getAbsoluteTop()
	    }
	}
	// add any additional grid lines needed
	// convert to canvas relative coordinates
//	lastX -= getAbsoluteLeft();
//	lastY -= getAbsoluteTop();
	// need to have grid lines at multiples of gridSize
	lastX = (lastX/gridSize)*gridSize;
	lastY = (lastY/gridSize)*gridSize;
	for (int i = lastX+gridSize; i <= desiredWidth; i += gridSize) {
	    gridPanel.add(new VerticalGridLine(newHeight), i, 0);
	}
	for (int j = lastY+gridSize; j <= desiredHeight; j += gridSize) {
	    gridPanel.add(new HorizontalGridLine(newWidth), 0, j);
	}
    }
    
    public MenuItem getGroupMenuItem(final PopupPanel popupMenu, final ShapeView shapeView) {
	// this action should apply to the shapes selected when the menu is popped up
	// not when the item is clicked
	final ArrayList<ShapeView> currentSelectedShapes = new ArrayList<ShapeView>(getSelectedShapes());
	Command groupCommand = new CommandWithExceptionHandling("Error occurred in creating a building block") {
		
	    @Override
	    public void executeWithExceptionHandling() {
		popupMenu.hide();
		GroupShapeView groupShape = groupSelectedWidgets(shapeView, currentSelectedShapes);
		UIEventManager.processEvent(new BuildingBlockCreationEvent(groupShape.getId()));
	    }
	    
	};
	MenuItem menuItem = new MenuItem(Expresser.messagesBundle.MultiRepeat(), true, groupCommand);
//	if (!atLeastTwoWidgetsSelected()) {
//	    menuItem.setEnabled(false);
//	}
	return menuItem;
    }
    
    public MenuItem getUngroupMenuItem(final PopupPanel popupMenu, final GroupShapeView groupShapeView) {
	Command ungroupCommand = new CommandWithExceptionHandling("Error occurred in undoing a building block") {
		
	    @Override
	    public void executeWithExceptionHandling() {
		groupShapeView.undoGroup(true);
		popupMenu.hide();
	    }
	    
	};
	MenuItem menuItem = new MenuItem(Expresser.messagesBundle.UnmakeBuildingBlock(), true, ungroupCommand);
//	if (!atLeastTwoWidgetsSelected()) {
//	    menuItem.setEnabled(false);
//	}
	return menuItem;
    }
    
    public MenuItem getCopyMenuItem(final PopupPanel popupMenu) {
	// this action should apply to the shapes selected when the menu is popped up
	// not when the item is clicked
	final ArrayList<ShapeView> currentSelectedShapes = new ArrayList<ShapeView>(getSelectedShapes());
	Command copyCommand = new CommandWithExceptionHandling("Error occurred in copying shape(s).") {
		
	    @Override
	    public void executeWithExceptionHandling() {
		copySelectedWidgets(currentSelectedShapes);
		popupMenu.hide();
	    }
	    
	};
	return new MenuItem(Expresser.messagesBundle.Copy(), true, copyCommand);
    }
    
    public MenuItem getDeleteMenuItem(final PopupPanel popupMenu) {
	// this action should apply to the shapes selected when the menu is popped up
	// not when the item is clicked
	final ArrayList<ShapeView> currentSelectedShapes = new ArrayList<ShapeView>(getSelectedShapes());
	Command deleteCommand = new CommandWithExceptionHandling("Error occurred in deleting shapes.") {
		
	    @Override
	    public void executeWithExceptionHandling() {
		deleteSelectedWidgets(currentSelectedShapes);
		popupMenu.hide();
	    }
	    
	};
	return new MenuItem(Expresser.messagesBundle.Remove(), true, deleteCommand);
    }
    
    public MenuItem getShowPropertiesItem(final PopupPanel popupMenu, final ShapeView shapeView) {
	Command command = new CommandWithExceptionHandling("Error occurred in showing the properties of a shape.") {
		
	    @Override
	    public void executeWithExceptionHandling() {
		int menuLeft = popupMenu.getAbsoluteLeft();
		int menuTop = popupMenu.getAbsoluteTop();
		addPropertyList(shapeView, menuLeft, menuTop);
		popupMenu.hide();
		UIEventManager.processEvent(new PropertyListDisplayedEvent(shapeView.getId()));
	    }
	    
	};
	MenuItem menuItem = new MenuItem(Expresser.messagesBundle.ShowProperties(), true, command);
	return menuItem;
    }
    
    public MenuItem getDiscussMenuItem(final PopupPanel popupMenu, final String myId, final String type) {
	Command shareCommand = new Command() {

	    @Override
	    public void execute() {
		Utilities.createAndSendDiscussCommandToMetafora(true, false, true, false, myId, type, getExpresserCanvas());
		popupMenu.hide();
	    }
	    
	};
	Command helpCommand = new Command() {

	    @Override
	    public void execute() {
		Utilities.createAndSendDiscussCommandToMetafora(true, false, false, false, myId, type, getExpresserCanvas());
		popupMenu.hide();
	    }
	    
	};
	Command postToChatCommand = new Command() {

	    @Override
	    public void execute() {
		Utilities.createAndSendDiscussCommandToMetafora(true, false, true, true, myId, type, getExpresserCanvas());
		popupMenu.hide();
	    }
	    
	};
	MenuBar discussMenuBar = new MenuBar(true);
	discussMenuBar.setAnimationEnabled(true);
	discussMenuBar.setAutoOpen(true);
	discussMenuBar.addItem(Expresser.messagesBundle.ShareWithOthers(), shareCommand);
	discussMenuBar.addItem(Expresser.messagesBundle.GetHelp(), helpCommand);
	discussMenuBar.addItem(Expresser.messagesBundle.PostToChat(), postToChatCommand);
	return new MenuItem(Expresser.messagesBundle.DiscussThis(), discussMenuBar);
    }
    
    public MenuItem getMakePatternItem(final PopupPanel popupMenu, final GroupShapeView buildingBlockView) {
	Command command = new CommandWithExceptionHandling("Error occurred in creating a pattern.") {
		
	    @Override
	    public void executeWithExceptionHandling() {
//		int buildingBlockWidth = buildingBlockView.getOffsetWidth();
		// create it so it repeats the building block once
		// until the numbers are specified
//		gridConstrainedDragController.makeNotDraggable(buildingBlock);
		TiedNumber iterations = new TiedNumber(new Number(4));
		TiedNumber deltaX = new TiedNumber(new Number(buildingBlockView.getWidth()/gridSize+1));
		TiedNumber deltaY = new TiedNumber(new Number(0));
		PatternView patternView = 
		    new PatternView(buildingBlockView, iterations, deltaX,  deltaY);
		int left = buildingBlockView.getAbsoluteLeft()-getAbsoluteLeft();
		int top = buildingBlockView.getAbsoluteTop()-getAbsoluteTop();
		add(patternView, left, top);
		ensureGridPanelIsUnderneath();
		patternView.setDraggable(gridConstrainedDragController);
		if (eventManager != null) {
		    eventManager.updateTiedNumber(iterations, false);
		    eventManager.updateTiedNumber(deltaX, false);
		    eventManager.updateTiedNumber(deltaY, false);
		    eventManager.patternCreatedOrUpdated(patternView, true);
		    BlockShape buildingBlockShape = buildingBlockView.getBlockShape();
		    if (!model.removeObject(buildingBlockShape)) {
			Utilities.warn("When removing the building block from a model because it became part of a pattern could not find it in the model.");
		    }
		}
		PropertyList patternWizard = new PatternWizard(patternView, unconstrainedDragController, ExpresserCanvasPanel.this);
		getExpresserCanvas().updateTiles();
		patternView.updateDisplay();
		int patternLeft = patternView.getAbsoluteLeft()-getAbsoluteLeft()+patternView.getOffsetWidth()+4;
		// add it and then if needed adjust to obtains the wizard's width and height
		add(patternWizard, patternLeft, top-getGridSize());
		Location adjustedPosition = Utilities.adjustPositionToStayVisible(patternLeft, top, patternWizard, ExpresserCanvasPanel.this);
		ExpresserCanvasPanel.this.setWidgetPosition(patternWizard, adjustedPosition.x, adjustedPosition.y);
		ensureGridPanelIsUnderneath();
		gridConstrainedDragController.makeDraggable(patternWizard);
		gridConstrainedDragController.toggleSelection(buildingBlockView);
		gridConstrainedDragController.toggleSelection(patternView);
		popupMenu.hide();
	    }
	    
	};
	return new MenuItem(Expresser.messagesBundle.Repeat(), true, command);
    }
    
    public MenuItem getUndoPatternItem(final PopupPanel popupMenu, final PatternView patternView) {
	Command command = new CommandWithExceptionHandling("Error occurred in undoing a pattern.") {
		
	    @Override
	    public void executeWithExceptionHandling() {
		patternView.undoPattern(true);
		popupMenu.hide();
	    }
	    
	};
	return new MenuItem(Expresser.messagesBundle.StopRepeat(), true, command);
    }
    
    public GroupShapeView groupSelectedWidgets(ShapeView shapeView, List<ShapeView> currentSelectedShapes) {
	clearSelection();
	if (currentSelectedShapes.isEmpty() && shapeView != null) {
	    currentSelectedShapes.add(shapeView);
	}
	return addGroupShape(currentSelectedShapes);
    }

    /**
     * @param shapeViews
     * @return a new GroupShapeView consisting of the shapeViews
     */
    public GroupShapeView addGroupShape(List<ShapeView> shapeViews) {
	GroupShapeView groupShape = new GroupShapeView(shapeViews);
	int left = groupShape.getSubShapesAbsoluteLeft();
	int top = groupShape.getSubShapesAbsoluteTop();
	// adjust for the position of this canvas as well
	int topCanvasCoordinates = top-getAbsoluteTop();
	int leftCanvasCoordinates = left-getAbsoluteLeft();
	add(groupShape, leftCanvasCoordinates, topCanvasCoordinates);
	ensureGridPanelIsUnderneath();
	groupShape.setDraggable(gridConstrainedDragController);
	eventManager.groupCreated(groupShape, true);
	for (ShapeView shapeView : shapeViews) {
	    BlockShape blockShape = shapeView.getBlockShape(); 
	    model.removeObject(blockShape);
	}
	return groupShape;
    }

    protected void ensureGridPanelIsUnderneath() {
	// following is necessary for IE9 and shouldn't hurt otherwise
	// see Issue 1735
	insert(gridPanel, getWidgetCount()-1);
    }
    
    public void copySelectedWidgets(ArrayList<ShapeView> originals) {
	if (originals.isEmpty()) {
	    if (shapeViewWithPopupMenu != null) {
		originals.add(shapeViewWithPopupMenu);
	    } else {
		return;
	    }
	}
	ArrayList<ShapeView> copies = new ArrayList<ShapeView>();
	int countOfShapesBeingCopied = originals.size();
	model.deselectAll();
//	int left = getAbsoluteLeft();
//	int top = getAbsoluteTop();
	for (int i = 0; i < countOfShapesBeingCopied; i++) {
	    ShapeView shapeView = originals.get(i);
	    BlockShape blockShape = shapeView.getBlockShape();
	    Distance2D distanceToOrigin = blockShape.distanceToOrigin(true);
	    // need to compute offsets in case shape has negative deltas
	    // will be zero for positive deltas
	    Number xOffset = distanceToOrigin.getHorizontal();
	    Number yOffset = distanceToOrigin.getVertical();
//	    left += xOffset.intValue()*gridSize;
//	    top += yOffset.intValue()*gridSize;
//	    if (blockShape instanceof PatternShape) {
//		PatternShape patternShape = (PatternShape) blockShape;
//		Number xIncrementValue = patternShape.getXIncrementValue();
//		if (xIncrementValue.isNegative()) {
//		    xOffset = new Number(patternShape.getWidth()-1);
//		}
//		Number yIncrementValue = patternShape.getYIncrementValue();
//		if (yIncrementValue.isNegative()) {
//		    yOffset = new Number(patternShape.getHeight()-1);
//		}
//	    }
	    // last one updates time stamps
	    // copy made at 0, 0 of the canvas -- see Issue 1758
//	    ShapeView copy = shapeView.copy(eventManager, i == countOfShapesBeingCopied-1, left, top);
//	    copies.add(copy);
//	    copy.setLeft(left); // overridden below??
//	    copy.setTop(top);
//	    add(copy, xOffset.intValue()*gridSize, yOffset.intValue()*gridSize);
	    ensureGridPanelIsUnderneath();
//	    BlockShape shape = copy.getBlockShape();
//	    if (shape == null) {
//		shape = copy.createPatternShape(getExpresserCanvas());
		BlockShape shape = shapeView.getBlockShape().createCopy();
		shape.moveTo(xOffset, yOffset);
		ShapeView shapeViewCopy = expresserCanvas.addShape(shape, true, model, null);
		copies.add(shapeViewCopy);
		// since shapes are independent no need to use continuation to serialise their storage
		shapeViewCopy.putInDataStore(eventManager, true, i == countOfShapesBeingCopied-1, null);
//		copy.setBlockShape(shape);
//		copy.synchronizeIds(shape);	
//	    }    
//	    model.addObject(shape);
	    shape.setSelected(true);
//	    shapeViewCopy.setDraggable(gridConstrainedDragController);
	}
	if (countOfShapesBeingCopied == 1) {
	    Utilities.popupMessage(Expresser.messagesBundle.ShapeCopiedToUpperLeftCorner());
	} else {
	    String message = Expresser.messagesBundle.ShapesCopiedToUpperLeftCorner().replace("***number***", Integer.toString(countOfShapesBeingCopied));
	    Utilities.popupMessage(message);
	}
	clearSelection();
	Expresser.instance().setAnyUserEvents(true);
	// swap the selection to the newly created clone
//	for (Widget clone : copies) {
//	    gridConstrainedDragController.toggleSelection(clone);
//	}
    }
    
    protected int nearestGridX(int x) {
	return gridSize * (int) Math.round(((double) x)/gridSize);
    }
    
    protected int nearestGridY(int y) {
	return gridSize * (int) Math.round(((double) y)/gridSize);
    }
    
    public void deleteSelectedWidgets(ArrayList<ShapeView> currentSelectedShapes) {
	if (currentSelectedShapes.isEmpty()) {
	    if (shapeViewWithPopupMenu != null) {
		currentSelectedShapes.add(shapeViewWithPopupMenu);
	    } else {
		return;
	    }
	}
	ShapeView lastShape = currentSelectedShapes.get(currentSelectedShapes.size()-1);
	for (ShapeView shapeView : currentSelectedShapes) {
	    eventManager.shapeDeleted(shapeView, this, (lastShape == shapeView));
	    Widget propertyList = getPropertyList(shapeView);
	    if (propertyList != null) {
		propertyList.removeFromParent();
	    }
	    shapeView.removeFromParent();
	    List<TiedNumberExpression<Number>> unlockedNumbersInExpression = shapeView.getBlockShape().getContainedTiedNumbers(true);
	    if (!unlockedNumbersInExpression.isEmpty()) {
		// check if any of the unlockedNumbersInExpression are the last occurrence
		ExpresserModel model = getModel();
		List<TiedNumberExpression<Number>> unlockedNumbers = model.getUnlockedNumbers();
		for (TiedNumberExpression<Number> unlockedNumberInExpression : unlockedNumbersInExpression) {
		    if (!unlockedNumbers.contains(unlockedNumberInExpression)) {
			model.unlockedTiedNumberRemoved(unlockedNumberInExpression);
		    }
		}
	    }
	}
    }

    public PickupDragControllerEnhanced getGridConstrainedDragController() {
        return gridConstrainedDragController;
    }

    public int getGridSize() {
        return gridSize;
    }

    public void setGridSize(int gridSize) {
	if (this.gridSize == gridSize) {
	    return;
	}
	this.gridSize = gridSize;
	int widgetCount = getWidgetCount();
	// count backwards since removing widgets as it runs
	List<LocatedWidget> widgetsToPutBack = new ArrayList<LocatedWidget>();
	for (int i = widgetCount-1; i >= 0; i--) {
	    Widget widget = getWidget(i);
	    if (widget instanceof ShapeView) {
		ShapeView shapeView = (ShapeView) widget;
		shapeView.setGridSize(gridSize);
		shapeView.updateDisplay(getExpresserCanvas());		
	    }
	    if (widget instanceof GridLine) {
		widget.removeFromParent();
	    } else {
		widgetsToPutBack.add(new LocatedWidget(widget, widget.getAbsoluteLeft(), widget.getAbsoluteTop()));		
	    }
	    // temporarily remove every one
//	    widget.removeFromParent();
	}
	setPixelSize(width, height);
	if (gridSize >= 10) {
	    add(gridPanel, 0, 0);
//	    addGridLines();
	}
	// grid lines are always underneath other widgets
	int canvasAbsoluteLeft = getAbsoluteLeft();
	int canvasAbsoluteTop = getAbsoluteTop();
	// put them back in the right z-order
	int size = widgetsToPutBack.size();
	for (int i = size-1; i >= 0; i--) {
	    LocatedWidget locatedWidget = widgetsToPutBack.get(i);
	    add(locatedWidget.getWidget(), 
		locatedWidget.getAbsoluteLeft()-canvasAbsoluteLeft, 
		locatedWidget.getAbsoluteTop()-canvasAbsoluteTop);
	}
    }

    public void selectWidgetsInRectangle(int rectangleLeft, int rectangleTop, int rectangleWidth, int rectangleHeight) {
	int widgetCount = getWidgetCount();
	for (int i = 0; i < widgetCount; i++) {
	    Widget widget = getWidget(i);
	    if (widget instanceof ShapeView &&
//		rectangleInside(widget.getElement().getOffsetLeft(), widget.getElement().getOffsetTop(), widget.getOffsetWidth(), widget.getOffsetHeight(),
//		                rectangleLeft, rectangleTop, rectangleWidth, rectangleHeight)
		centerInside(widget.getElement().getOffsetLeft(), widget.getElement().getOffsetTop(), widget.getOffsetWidth(), widget.getOffsetHeight(),
		             rectangleLeft, rectangleTop, rectangleWidth, rectangleHeight)
		) {
//		gridConstrainedDragController.toggleSelection(widget);	
		ShapeView shapeView = (ShapeView) widget;
		setShapeViewSelected(shapeView, true);
	    }
	}
    }
    
    protected TileView getTileAtLocation(int clientX, int clientY) {
	int widgetCount = getWidgetCount();
	for (int i = 0; i < widgetCount; i++) {
	    Widget widget = getWidget(i);
	    if (widget instanceof ShapeView) {
		TileView tileAtLocation = ((ShapeView) widget).getTileAtLocation(clientX, clientY);
		if (tileAtLocation != null) {
		    return tileAtLocation;
		}
	    }
	}
	return null;
    }
    
    public void enureWidgetAmongTheFront(Widget widget) {
	int widgetCount = getWidgetCount();
	for (int i = 0; i < widgetCount; i++) {
	    Widget nextWidget = getWidget(i);
	    if (nextWidget == widget) {
		return;
	    }
	    if (!isFront(nextWidget)) {
		insert(widget, widget.getAbsoluteLeft()-getAbsoluteLeft(), widget.getAbsoluteTop()-getAbsoluteTop(), i);
	    }
	}
    }

    private boolean isFront(Widget widget) {
	return widget instanceof PropertyList || widget instanceof ExpressionPanel || widget instanceof BorderPiece;
    }
    
//    private boolean rectangleInside(int widgetLeft, int widgetTop, int widgetWidth, int widgetHeight, 
//	                            int rectangleLeft, int rectangleTop, int rectangleWidth, int rectangleHeight) {
//	if (widgetLeft < rectangleLeft) {
//	    return false;
//	}
//	int widgetRight = widgetLeft+widgetWidth;
//	int rectangleRight = rectangleLeft+rectangleWidth;
//	if (widgetRight > rectangleRight) {
//	    return false;
//	}
//	if (widgetTop < rectangleTop) {
//	    return false;
//	}
//	int widgetBottom = widgetTop+widgetHeight;
//	int rectangleBottom = rectangleTop+rectangleHeight;
//	if (widgetBottom > rectangleBottom) {
//	    return false;
//	}
//	return true;
//    }
    
    private boolean centerInside(int widgetLeft, int widgetTop, int widgetWidth, int widgetHeight, 
	                         int rectangleLeft, int rectangleTop, int rectangleWidth, int rectangleHeight) {
	int widgetX = widgetLeft+widgetWidth/2;
	int widgetY = widgetTop+widgetHeight/2;
	int rectangleRight = rectangleLeft+rectangleWidth;
	int rectangleBottom = rectangleTop+rectangleHeight;
	return widgetX > rectangleLeft && widgetX < rectangleRight &&
	       widgetY > rectangleTop && widgetY < rectangleBottom;
    }

    public void clearDragSelection() {
	if (gridConstrainedDragController != null) {
	    gridConstrainedDragController.clearSelection();
	    gridConstrainedDragController.resetCache();
	}
	if (unconstrainedDragController != null) {
	    unconstrainedDragController.clearSelection();
	    unconstrainedDragController.resetCache();
	}
    }
    
    public void clearSelection() {
	for (ShapeView shapeView : selectedShapes) {
	    setShapeViewSelected(shapeView, false);
	}
	selectedShapes.clear();
    }

    public boolean containsShapeAt(int x, int y) {
	int widgetCount = getWidgetCount();
	for (int i = 0; i < widgetCount; i++) {
	    Widget widget = getWidget(i);
	    if (widget instanceof ShapeView) {
		if (((ShapeView) widget).contains(x, y)) {
		    return true;
		}
	    }
	}
	return false;
    }
    
    public Widget widgetContainingPoint(int x, int y) {
	int widgetCount = getWidgetCount();
	for (int i = 0; i < widgetCount; i++) {
	    Widget widget = getWidget(i);
	    if (widget instanceof GridLinesPanel) {
		// grid lines aren't really objects -- just a way to draw the canvas
		continue;
	    }
	    int left = widget.getAbsoluteLeft();
	    if (x < left) {
		continue;
	    }
	    int right = left+widget.getOffsetWidth();
	    if (x > right) {
		continue;
	    }
	    int top = widget.getAbsoluteTop();
	    if (y < top) {
		continue;
	    }
	    int bottom = top+widget.getOffsetHeight();
	    if (y > bottom) {
		continue;
	    }
	    return widget;
	}
	return null;
    }

    public PickupDragControllerEnhanced getUnconstrainedDragController() {
        return unconstrainedDragController;
    }

    public ExpresserCanvas getExpresserCanvas() {
        return expresserCanvas;
    }
    
    public List<ShapeView> getShapeViewsBeingDragged() {
	return expresserCanvas.getShapeViewsBeingDragged();
    }

    public void updateTilesDisplayMode(Map<Location, ArrayList<AllocatedColor>> map) {
	getModel().setOverlapPainted(false); // unless proven otherwise below
	for (ShapeView shapeView : getShapeViews()) {
	    // more sure all is up-to-date before colouring the tiles
	    BlockShape blockShape = shapeView.getBlockShape();
	    if (blockShape != null) {
		shapeView.setModelLocation(blockShape, getExpresserCanvas());
	    } // else is generated by pattern repetitions and doesn't exist explicitly in the model
	    shapeView.updateDisplay(getExpresserCanvas());
	    if (shapeView.isVisible()) {
		shapeView.updateTilesDisplayMode(map, expresserCanvas, shapeView);
		// may have updated the tiles so make sure they are handles to the appropriate shape view
		if (!expresserCanvas.isReadOnly() && !expresserCanvas.isModelInvalidWhileDragInProgress() && !URLParameters.isThumbnail()) {
		    shapeView.setDraggable(getGridConstrainedDragController());
		} // otherwise not draggable since part of Computer's Model panel
	    }
	}	
    }
    
    public void reportMovedByGlueLocationsToDataStore() {
	int widgetCount = getWidgetCount();
	Glue glue = getModel().getGlue();
	for (int i = 0; i < widgetCount; i++) {
	    Widget widget = getWidget(i);
	    if (widget instanceof ShapeView) {
		ShapeView shapeView = (ShapeView) widget;
		if (glue != null && glue.isMovedShape(shapeView.getBlockShape(), true)) {
		    eventManager.reportShapeMovedToDataStore(
			    shapeView, 
			    Utilities.ensureIsInt(shapeView.getModelX(getExpresserCanvas())), 
			    Utilities.ensureIsInt(shapeView.getModelY(getExpresserCanvas())), 
			    false);
		}
	    }
	}
    }
    
    public List<ShapeView> getShapeViews() {
	ArrayList<ShapeView> shapeViews = new ArrayList<ShapeView>();
	int widgetCount = getWidgetCount();
	for (int i = widgetCount-1; i >= 0; i--) {
	    Widget widget = getWidget(i);
	    if (widget instanceof ShapeView) {
		shapeViews.add((ShapeView) widget);
	    }
	}
	return shapeViews;
    }
    
    public void removeAllSnaphots() {
	int widgetCount = getWidgetCount();
	for (int i = widgetCount-1; i >= 0; i--) {
	    Widget widget = getWidget(i);
	    if (widget instanceof TileViewSnapShot) {
		remove(widget);
	    }
	}	
    }
    
    public List<TileView> getAllTileViews() {
	ArrayList<TileView> tileViews = new ArrayList<TileView>();
	int widgetCount = getWidgetCount();
	for (int i = 0; i < widgetCount; i++) {
	    Widget widget = getWidget(i);
	    if (widget instanceof ShapeView) {
		ShapeView shapeView = (ShapeView) widget;
		shapeView.addTileViews(tileViews);
	    }
	}
	return tileViews;
    }
    
    public void removeAllTileViews(boolean removeFromModel) {
	int widgetCount = getWidgetCount();
	// count down since removing them
	for (int i = widgetCount-1; i >= 0; i--) {
	    Widget widget = getWidget(i);
	    if (widget instanceof ShapeView) {
		remove(widget);
		if (removeFromModel) {
		    ShapeView shapeView = (ShapeView) widget;
		    BlockShape blockShape = shapeView.getBlockShape();
		    getModel().removeObject(blockShape);
		}
	    }
	}
    }
    
    public void removeAllLocatedExpressions(boolean removeFromModel) {
	int widgetCount = getWidgetCount();
	// count down since removing them
	for (int i = widgetCount-1; i >= 0; i--) {
	    Widget widget = getWidget(i);
	    if (widget instanceof ExpressionPanel) {
		ExpressionPanel expressionPanel = (ExpressionPanel) widget;
		expressionPanel.setDropTarget(false);
		remove(widget);
	    }
	}
	if (removeFromModel) {
	    ArrayList<LocatedExpression<Number>> locatedExpressions = 
		new ArrayList<LocatedExpression<Number>>(getModel().getLocatedExpressions());
	    for (LocatedExpression<Number> locatedExpression : locatedExpressions) {
		getModel().removeLocatedExpression(locatedExpression);
	    }
	}
    }
    
    public void removePropertyLists() {
	int widgetCount = getWidgetCount();
	// count down since removing them
	for (int i = widgetCount-1; i >= 0; i--) {
	    Widget widget = getWidget(i);
	    if (isFront(widget)) {
		remove(widget);
	    }
	}
    }
    
    public void moveAllBy(int deltaX, int deltaY) {
	int widgetCount = getWidgetCount();
	for (int i = 0; i < widgetCount; i++) {
	    Widget widget = getWidget(i);
	    if (widget instanceof ShapeView) {
		int left = getWidgetLeft(widget)+deltaX;
		int top = getWidgetTop(widget)+deltaY;
		setWidgetPosition(widget, left, top);
	    }
	}	
    }

    public ExpresserModel getModel() {
        return model;
    }
    
    public void setModel(ExpresserModel model) {
	this.model = model;
	// clear() causes problems because widgets are not unregistered from drag controller
	// for now this is only sensible to call on a fresh canvas
//	int widgetCount = getWidgetCount();
//	for (int i = widgetCount-1; i >= 0; i--) {
//	    Widget widget = getWidget(i);
//	    if (widget instanceof ShapeView) {
//		remove(widget);
//		
//		
//	    } else if (widget instanceof ExpressionPanel) {
//	}
//	addGridLines();
    }

    /**
     * @param shapeView
     * @return true if shapeView is being dragged or its (super) container
     */
    public boolean isDragging(ShapeView shapeView) {
	List<ShapeView> shapeViewsBeingDragged = getShapeViewsBeingDragged();
	for (ShapeView shapeViewDragged : shapeViewsBeingDragged) {
	    Widget ancestor = shapeView;
	    while (ancestor != null) {
		if (ancestor == shapeViewDragged) {
		    return true;
		}
		ancestor = ancestor.getParent();
	    }
	}
	return false;
    }

    public EventManager getEventManager() {
        return eventManager;
    }

    /**
     * Sets the popup menu where the mouse is unless there isn't room
     * 
     * @param mouseX
     * @param mouseY
     * @param popupMenu
     *
     */
    public void setPopupPosition(int mouseX, int mouseY, PopupPanel popupMenu) {
	Location adjustedPosition = Utilities.adjustPositionToStayVisible(mouseX-getAbsoluteLeft(), mouseY-getAbsoluteTop(), popupMenu, this);
	popupMenu.setPopupPosition(adjustedPosition.x+getAbsoluteLeft(), adjustedPosition.y+getAbsoluteTop());
    }
    
    @Override
    public void add(final Widget widget, int left, int top) {
	// if an expression is dropped on a rule it should end up
	// above the rule area
	if (widget instanceof ShapeView) {
	    ShapeView shapeView = (ShapeView) widget;
	    shapeView.setGridSize(gridSize);
	    addShapeViewWithTilesInFront(shapeView, left, top);
	    shapeView.setLeft(left);
	    shapeView.setTop(top);
	    shapeView.addStyleName(shapeView.getTopLevelStyleName());
	    shapeView.updateDisplay(getExpresserCanvas());
	    if (shapeView.getId() == null) {
		// fresh -- isn't just being moved around
		shapeView.setId(UUID.uuid());
		 if (shapeView instanceof TileView) {
		     TileView tile = (TileView) shapeView;
		     UIEventManager.processEvent(new TileCreationEvent(tile.getId(), tile.getColor().toString()));
		 }
	    }
	} else {
	    super.add(widget, left, top);
	}
	// failed attempt to add dynamically created objects to the holes of a stencil if it is active
//	final Stencil stencil = Expresser.instance().getStencil();
//	if (stencil != null && stencil.isAttached() && stencil.isMaskVisible() && widget instanceof ExpressionPanel) {
//	    // need to make new holes for newly added widgets
//	    Scheduler.get().scheduleDeferred(new ScheduledCommand() {
//		    @Override
//		    public void execute() {
//			stencil.addHole(widget);
//			stencil.setMaskVisible(false);
//			stencil.setMaskVisible(true);
//		    }
//		});
//	}
	if (!(widget instanceof ExpressionPanel)) {
	    return;
	}
	Widget menuBar = Expresser.getMenuBar();
	if (menuBar == null) {
	    return;
	}
	if (HighLightingTimer.getTimer(widget) != null) {
	    // highlighting so leave it alone
	    return;
	}
	int canvasHeight = getOffsetHeight();
	int widgetHeight = widget.getOffsetHeight();
	if (top > canvasHeight-widgetHeight) {
	    // expressions shouldn't be dropped here    
	    setWidgetPosition(widget, left, canvasHeight-widgetHeight);    
	}
    }
    
    @Override
    public boolean remove(Widget widget) {
	boolean result = super.remove(widget);
	if (widget instanceof ShapeView) {
	    widget.removeStyleName(((ShapeView) widget).getTopLevelStyleName());
	}
	return result;
    }
    
    @Override
    public void insert(Widget widget, int beforeIndex) {
	if (isFront(widget)) {
	    super.insert(widget, beforeIndex);
	    return;
	}
	if (beforeIndex > 0) {
	    Widget nextWidget = getWidget(beforeIndex-1);
	    if (isFront(nextWidget)) {
		insert(widget, beforeIndex-1);
		return;
	    }
	}
	super.insert(widget, beforeIndex);
    }

    public void addShapeViewWithTilesInFront(ShapeView shapeView, int left, int top) {
	// this is a bit of a hack
	// it ensures that tiles are in front (in the z-ordering sense) of groups or patterns
	// it is tiles that are the drag handles so they shouldn't be obscured by non-tile shape views
	// this resolves Issue 1430
	if (shapeView instanceof TileView) {
	    super.add(shapeView, left, top);
	} else {
	    insert(shapeView, left, top, 0);
	}	
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getXOffset() {
	return expresserCanvas.getXOffset();
    }
    
    public int getYOffset() {
	return expresserCanvas.getYOffset();
    }

    /**
     * @param shapeView
     * @param menuTop 
     * @param menuLeft 
     */
    public PropertyList addPropertyList(ShapeView shapeView, Integer menuLeft, Integer menuTop) {
	PropertyList propertyList = getPropertyListItself(shapeView);
	int left;
	int top;
	if (propertyList != null) {
	    unconstrainedDragController.clearSelection();
	    unconstrainedDragController.toggleSelection(propertyList);
	    // ensure it is on top
	    left = menuLeft == null ? getWidgetLeft(propertyList) : menuLeft-getAbsoluteLeft();
	    top = menuTop == null ? getWidgetTop(propertyList) : menuTop-getAbsoluteTop();
	    insert(propertyList, left, top, getWidgetCount()-1);	    
	} else {
	    propertyList = shapeView.getPropertyList(this);
	    // use relative coordinates for just to the right at the same height
	    // with a 4 pixel gap to look better
	    int canvasLeft = getAbsoluteLeft();
	    left = menuLeft == null ? 
		    shapeView.getAbsoluteLeft()+shapeView.getOffsetWidth()-canvasLeft+4 : 
		    menuLeft-canvasLeft;
	    int canvasTop = getAbsoluteTop();
	    top = menuTop == null ? shapeView.getAbsoluteTop()-canvasTop : menuTop-canvasTop;
	    add(propertyList, left, top);
	    unconstrainedDragController.makeDraggable(propertyList);
	}
//	if (menuLeft == null || menuTop == null) {
	    ensurePropertyListInView(propertyList);
//	}
	return propertyList;
    }

    // makes sure the property list isn't scroll off screen
    public void ensurePropertyListInView(PropertyList propertyList) {
	Widget propertyListWidget;
	if (propertyList.getParent() instanceof ScrollPanel) {
	    propertyListWidget = propertyList.getParent();
	} else {
	    propertyListWidget = propertyList;
	}
	int propertyListWidth = propertyListWidget.getOffsetWidth();
	int left = getWidgetLeft(propertyListWidget);
	int propertyListHeight = propertyListWidget.getOffsetHeight();
	int top = getWidgetTop(propertyListWidget);
	int canvasWidth = getOffsetWidth();
	int absoluteLeft = getAbsoluteLeft();
	int viewableCanvasWidth = Math.min(canvasWidth, Window.getClientWidth()-absoluteLeft);
	int canvasHeight = getOffsetHeight();
	int absoluteTop = getAbsoluteTop();
	int viewableCanvasHeight = Math.min(canvasHeight, Window.getClientHeight()-absoluteTop);
	viewableCanvasHeight -= Expresser.instance().getTranslationBarHeight();
	if (left+propertyListWidth > viewableCanvasWidth) {
	    int scrollLeft = Window.getScrollLeft();
	    left = viewableCanvasWidth+scrollLeft-propertyListWidth;
	}
	if (top+propertyListHeight > viewableCanvasHeight) {
	    int scrollTop = Window.getScrollTop();
	    top = viewableCanvasHeight+scrollTop-propertyListHeight;
	}
	if (left < 0) {
	    left = 0;
	}
	if (top < 0) {
	    top = 0;
	}
	setWidgetPosition(propertyListWidget, left, top);
    }

    public Widget getPropertyList(ShapeView shapeView) {
	Iterator<Widget> children = getChildren().iterator();
	while (children.hasNext()) {
	    Widget widget = children.next();
	    if (widget instanceof PropertyList) {
		PropertyList propertyList = ((PropertyList) widget);
		if (propertyList.getShapeView() == shapeView) {
		    if (propertyList.getParent() instanceof ScrollPanel) {
			return propertyList.getParent();
		    } else {
			return propertyList;
		    }
		}
	    }    
	}
	return null;
    }
    
    public PropertyList getPropertyListItself(ShapeView shapeView) {
	Widget widget = getPropertyList(shapeView);
	if (widget instanceof PropertyList) {
	    return ((PropertyList) widget);
	} else if (widget instanceof ScrollPanel) {
	    return ((PropertyList) ((ScrollPanel) widget).getWidget());
	} else {
	    return null;
	}
    }
    
    public Widget getWidgetWithId(String objectId) {
	return getWidgetWithId(objectId, true);
    }

    public Widget getWidgetWithId(String objectId, boolean ensureVisible) {
	if (objectId.equals("GENERAL_MODEL_ID")) {
	    return Expresser.getMenuBar(); // would be nice to get .getComputerModelMenuItem() but that isn't a widget
	} else if (objectId.equals("HELP_AREA")) {
	    return Expresser.instance().getHelpOptions();
	} else if (objectId.equals("TIED_NUMBER_SLIDERS")) {
	    UnlockedTiedNumbersControlPanel unlockedTiedNumbersControlPanel = Expresser.instance().getUnlockedTiedNumbersControlPanel();
	    if (unlockedTiedNumbersControlPanel != null) {
		return unlockedTiedNumbersControlPanel.getGrid();
	    }
	} else if (objectId.equals("PLAYBUTTON")) {
	    // Play button references are obsolete -- see Issue 1743
//	    UnlockedTiedNumbersControlPanel unlockedTiedNumbersControlPanel = Expresser.instance().getUnlockedTiedNumbersControlPanel();
//	    if (unlockedTiedNumbersControlPanel != null) {
//		return unlockedTiedNumbersControlPanel.getAnimationButton();
//	    }
//	    AnimationPanel animationPanel = getExpresserCanvas().getAnimationPanel();
//	    if (animationPanel != null) {
//		return animationPanel.getAnimationButton();
//	    }
	    return null;
	}
	if (ensureVisible) {
	    String[] parsedUniqueId = SharedMigenUtilities.parseUniqueId(objectId);
	    if (parsedUniqueId.length > 1) {
		// make sure object is visible (e.g. show property list)
		try {
		    Expresser.instance().setVisible(objectId, true);
		} catch (UnsupportedOperationException ignore) {
		    // if it can't find it to make it visible don't let the exception go any further
		}
	    }
	}
	Widget widgetWithIdFromChildren = 
		Utilities.getWidgetWithIdFromChildren(getChildren().iterator(), objectId);
	if (widgetWithIdFromChildren != null) {
	    return widgetWithIdFromChildren;
	} else {
	    List<RulePanel> rules = Expresser.instance().getMyModelRulesPanel().getRules();
	    for (RulePanel rule : rules) {
		Widget widgetWithId = rule.getExpressionPanel().getWidgetWithId(objectId);
		if (widgetWithId != null) {
		    return widgetWithId;
		}
	    }
	    return null;
	}
    }

    public List<PatternPropertyList> getOpenPropertyLists() {
	ArrayList<PatternPropertyList> propertyLists = new ArrayList<PatternPropertyList>();
	Iterator<Widget> children = getChildren().iterator();
	while (children.hasNext()) {
	    Widget widget = children.next();
	    if (widget instanceof PatternPropertyList) {
		propertyLists.add((PatternPropertyList) widget);
	    }
	}
	return propertyLists;
    }

    public List<ShapeView> selectAllShapes() {
	clearSelection();
	int widgetCount = getWidgetCount();
	for (int i = 0; i < widgetCount; i++) {
	    Widget widget = getWidget(i);
	    if (widget instanceof ShapeView) {
		ShapeView shapeView = (ShapeView) widget;
		selectedShapes.add(shapeView);
		shapeView.setSelectionFeedback(true);
	    }
	}
	return selectedShapes;
    }

    public List<String> occurencesOfTiedNumber(final TiedNumber tiedNumber, final List<ExpressionPanel> ancestorExceptions) {
	final ArrayList<String> occurrences = new ArrayList<String>();
	Walker walker = new Walker() {

	    @Override
	    public boolean tiedNumberFound(
		    TiedNumberExpression<?> tiedNumberFound,
		    BlockShape shape, 
		    AttributeHandle<Number> handle,
		    ExpresserModel expresserModel) {
		if (tiedNumberFound != null && tiedNumberFound.getIdString().equals(tiedNumber.getIdString())) {
		    if (shape != null && handle != null) {
			String attributeId = SharedMigenUtilities.createAttributeId(shape, handle, tiedNumber.getIdString());
			Widget propertyWidget = getWidgetWithId(attributeId, false);
			if (!anyAncestorIsMemberOf(propertyWidget, ancestorExceptions)) {
			    occurrences.add(attributeId);
			}
		    } // otherwise is a located expression that we're handling below
		}
		return true;
	    }
	    
	};
	model.walkToTiedNumbers(walker);
	int widgetCount = getWidgetCount();
	for (int i = 0; i < widgetCount; i++) {
	    Widget widget = getWidget(i);
	    if (widget instanceof ExpressionPanel) {
		if (!anyAncestorIsMemberOf(widget, ancestorExceptions)) {
		    ((ExpressionPanel) widget).addOccurencesOfTiedNumber(tiedNumber, occurrences);
		}
	    }
	}
	List<RulePanel> rules = Expresser.instance().getMyModelRulesPanel().getRules();
	for (RulePanel rule : rules) {
	    rule.getExpressionPanel().addOccurencesOfTiedNumber(tiedNumber, occurrences);
	}
	return occurrences;
    }

    private boolean anyAncestorIsMemberOf(Widget widget, List<ExpressionPanel> ancestorExceptions) {
	Widget ancestor = widget;
	while (ancestor != null) {
	    if (ancestorExceptions.contains(ancestor)) {
		return true;
	    }
	    ancestor = ancestor.getParent();
	}
	return false;
    }

    public void setShapeViewSelected(ShapeView shapeView, boolean selected) {
	// TODO: rationalise this
	shapeView.setSelectionFeedback(selected);
//        String style = shapeView.getTopLevelStyleName() + "-selected";
        if (selected) {
//            shapeView.addStyleName(style);
            if (!selectedShapes.contains(shapeView)) {
		selectedShapes.add(shapeView);
            }
//        } else {
//            shapeView.removeStyleName(style);
//            // callers responsibility to update selectedShapes
        }   
    }

    public ArrayList<ShapeView> getSelectedShapes() {
        return selectedShapes;
    }

    public void setShapeViewWithPopupMenu(ShapeView shape) {
	shapeViewWithPopupMenu = shape;
    }
    
    public boolean rectangleSelectionActive() {
	return expresserCanvas.rectangleSelectionActive();
    }

    public void moveIfOnAPropertyList(ExpressionPanel expressionPanel) {
	WidgetCollection children = getChildren();
	for (Widget child : children) {
	    if (child instanceof PropertyList) {
		PropertyList propertyList = (PropertyList) child;
		if (Utilities.widgetsOverlap(propertyList, expressionPanel)) {
		    add(expressionPanel, propertyList.getAbsoluteLeft()+propertyList.getOffsetWidth()+4-getAbsoluteLeft(), getWidgetTop(expressionPanel));
		}
	    }
	}	
    }
    
//    @Override
//    public int getAbsoluteTop() {
//	return super.getAbsoluteTop()-Expresser.instance().getTranslationBarHeight();
//    }

}

