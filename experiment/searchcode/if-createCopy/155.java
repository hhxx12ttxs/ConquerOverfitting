package uk.ac.lkl.migen.system.expresser.model;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import uk.ac.lkl.common.util.BoundingBox;
import uk.ac.lkl.common.util.ID;
import uk.ac.lkl.common.util.IDFactory;
import uk.ac.lkl.common.util.IDObject;
import uk.ac.lkl.common.util.Location;
import uk.ac.lkl.common.util.ObjectWithID;
import uk.ac.lkl.common.util.config.MiGenConfiguration;
import uk.ac.lkl.common.util.event.UpdateEvent;
import uk.ac.lkl.common.util.event.UpdateListener;
import uk.ac.lkl.common.util.event.UpdateSupport;
import uk.ac.lkl.common.util.expression.Expression;
import uk.ac.lkl.common.util.expression.LocatedExpression;
import uk.ac.lkl.common.util.value.Number;
import uk.ac.lkl.migen.system.expresser.model.event.AttributeChangeEvent;
import uk.ac.lkl.migen.system.expresser.model.event.AttributeChangeListener;
import uk.ac.lkl.migen.system.expresser.model.event.ObjectEvent;
import uk.ac.lkl.migen.system.expresser.model.event.ObjectListener;
import uk.ac.lkl.migen.system.expresser.model.event.UntiedNumberAdditionOrRemovalUpdateEvent;
import uk.ac.lkl.migen.system.expresser.model.shape.block.BasicShape;
import uk.ac.lkl.migen.system.expresser.model.shape.block.BlockShape;
import uk.ac.lkl.migen.system.expresser.model.shape.block.GroupShape;
import uk.ac.lkl.migen.system.expresser.model.shape.block.ModelGroupShape;
import uk.ac.lkl.migen.system.expresser.model.shape.block.PatternShape;
import uk.ac.lkl.migen.system.expresser.model.tiednumber.AnimationSettings;
import uk.ac.lkl.migen.system.expresser.model.tiednumber.TiedNumberExpression;
import uk.ac.lkl.migen.system.expresser.ui.uievent.ChangeInDisplayOfAnyOverlaps;
import uk.ac.lkl.migen.system.expresser.ui.uievent.UIEventManager;
import uk.ac.lkl.migen.system.task.TaskIdentifier;

/**
 * The main class for the eXpresser's model. 
 * 
 * Contains the information about the user's construction, rule, etc. 
 * 
 * Depending on the context, "construction" and "model" can be synonyms to 
 * "ExpresserModel". 
 * 
 * @author darren.pearce, toontalk, sergut
 */
public class ExpresserModelImpl implements ExpresserModel, IDObject, ObjectWithID {

    /**
     * The objects used in this instance.
     * 
     * Note that this was a set so that duplicates could not be added.
     * 
     * Note: this is a list now. Could do with being a set to prevent duplicates
     * but need objects to be processed in strict order for resource allocation.
     * Need work to address this.
     * 
     * addObjects checks for duplicates so that is not a problem.
     * 
     * Since resource allocations are no longer used could go back to using a set.
     * But the list does determine the z-ordering when displaying tiles.
     * But we normally configure things to use crosses or colour blending
     * so the z-order probably doesn't matter anymore.
     * 
     */
    private ArrayList<BlockShape> objects;
       
    // these are the expressions that were placed on the canvas
    private List<LocatedExpression<Number>> locatedExpressions = 
	new ArrayList<LocatedExpression<Number>>();
    
    private String name = "";

    /**
     * The ID of this model.
     */
    private ID id;
   
    // following needed when configured so that requireOnlyTotalTileCount is true
    private Expression<Number> totalAllocationExpression = 
	new TiedNumberExpression<Number>(BlockShape.IMPOSSIBLE_ALLOCATION_VALUE);

    private ArrayList<ObjectListener> objectListeners;

    private ArrayList<AttributeChangeListener<BlockShape>> attributeChangeListeners;

    // support for chaining calls from the constituent objects to listeners on
    // this class
    private UpdateSupport<BlockShape> objectUpdateSupport =
	    new UpdateSupport<BlockShape>();

    // used internally when the selection changes
    private UpdateSupport<BlockShape> selectionUpdateSupport =
	    new UpdateSupport<BlockShape>();
    
    // support for changes to the list of task variables
    private UpdateSupport<TiedNumberExpression<?>> unlockedTiedNumberListUpdateSupport =
	    new UpdateSupport<TiedNumberExpression<?>>();
    
    // support for changes to the total tiles expression
    private UpdateSupport<Expression<Number>> totalAllocationExpressionUpdateSupport =
	    new UpdateSupport<Expression<Number>>();

    // the set of objects currently selected
    // todo: use TreeSet
    private ArrayList<BlockShape> selectedShapes;
    
    private ArrayList<BlockShape> previousSelectedShapes = new ArrayList<BlockShape>();

    // for example when the property list is selected the shape becomes highlighted
    private List<BlockShape> highlightedShapes = new ArrayList<BlockShape>();
    
    // for example when containers of tied number is selected
    // this list is reset when the model is dirty
    private List<BlockShape> temporaryHighlightedShapes = new ArrayList<BlockShape>();
       
    private Palette palette;
    
    // used to deal with global colour allocations
    private ModelGroupShape modelAsAGroup;
    
    // for maintaining gluing of shape boundary constraints
    private Glue glue = null;
    
    // true if changed in a way that necessitates the view to be updated
    // the view can accumulate several changes and periodically update
    // itself and reset this back to false
    private boolean dirtyModel = false;
    
    // true if changed in a way that autosaving is appropriate
    private boolean autoSaveModel = true;
    
    // for undo using auto saving
    private ArrayList<String> autosavedNames = new ArrayList<String>();
    
    // index of last undo file name since last change to the model
    private int lastUndoIndex = -1;
    
    // if indicator has been detected since last saved then should be set to true
    private boolean indicatorSinceLastSave = false;
    
    // true if the master panel has changed in ways that the slave should mirror
    // e.g. moving a property list or adding an expression to the canvas
    private boolean dirtyPanel = false;
    
    private boolean masterModel = false;
    
    private Boolean overlapPainted = false;
    
    // previously reported via a UIEvent or null if never reported
    private Boolean previousOverlapsReport = null;

    private boolean negativeTilePainted; 
       
    private TaskIdentifier taskIdentifier;
    
    /**
     * Those shapes that the user should not be able to delete.
     * Used to disable the menu item of these shapes.
     */
    private HashSet<BlockShape> undeletableShapes = new HashSet<BlockShape>();
    
    private List<TiedNumberExpression<?>> animatingTiedNumbers = null;
    
    // chains updates from the objects to listeners in this class
    private UpdateListener<BlockShape> chainingObjectUpdateListener =
	new UpdateListener<BlockShape>() {
	
	@Override
	public void objectUpdated(UpdateEvent<BlockShape> e) {
	    objectUpdateSupport.fireObjectUpdated(e);
	    lastModifiedShape = e.getSource();
	}
    };

    private UpdateListener<BlockShape> objectSelectedUpdateListener =
	new UpdateListener<BlockShape>() {
	
	@Override
	public void objectUpdated(UpdateEvent<BlockShape> e) {
	    processObjectSelectedUpdate(e);
	}
    };

    private class ChainingAttributeChangeListener implements
	    AttributeChangeListener<BlockShape> {

	//private BlockShape object;

	public ChainingAttributeChangeListener(BlockShape object) {
	    //this.object = object;
	}

	// todo: does this really need a class? Originally retargetted the
	// event. But the source was only ever a blockshape so no need for
	// retargetting or indeed this class.
	@Override
	public void attributesChanged(AttributeChangeEvent<BlockShape> e) {
	    processAttributesChanged(e);
	}
    };
    
    // so can detach properly
    private HashMap<BlockShape, ChainingAttributeChangeListener> attributeChangeListenerMap =
	    new HashMap<BlockShape, ChainingAttributeChangeListener>();

    // to manage color combinations need to know all the colors at each grid location
    private HashMap<Location, ArrayList<AllocatedColor>> colorGridMap =
        new HashMap<Location, ArrayList<AllocatedColor>>();

    // used to query about what shapes are at a point
    private HashMap<Location, ArrayList<BlockShape>> shapeGridMap = 
        new HashMap<Location, ArrayList<BlockShape>>();

    private int xOffset;

    private int yOffset;

    private BlockShape lastModifiedShape;

    /**
     * Create a new instance with its own palette of colors and clipboard (not currently used).
     * 
     */
    public ExpresserModelImpl() {
	this(AvailableTileColors.getDefaultPalette());
    } 

    public ExpresserModelImpl(Palette palette) {	
	this.objects = new ArrayList<BlockShape>();
	this.objectListeners = new ArrayList<ObjectListener>();
	this.attributeChangeListeners = new ArrayList<AttributeChangeListener<BlockShape>>();
	this.selectedShapes = new ArrayList<BlockShape>();
	this.palette = palette;
	this.id = IDFactory.newID(this);
	modelAsAGroup = new ModelGroupShape(this);
	if (MiGenConfiguration.isEnableVelcro() || MiGenConfiguration.isEnableAutoVelcro()) {
	    glue = new Glue();
	}
    }

    @Override
    public String getIdName() {
	return "Model";
    }

    private void processObjectSelectedUpdate(UpdateEvent<BlockShape> e) {
	BlockShape object = e.getSource();
	if (object.isSelected()) {
	    selectedShapes.add(object);
	    previousSelectedShapes.clear();
	} else {
	    selectedShapes.remove(object);
	    previousSelectedShapes.add(object);
	}
	selectionUpdateSupport.fireObjectUpdated(e);
    }

    @Override
    public void selectAll() {
	for (BlockShape object : objects) {
	    object.setSelected(true);
	}
    }

    @Override
    public void deselectAll() {
	// make a copy so can iterate neatly otherwise get
	// ConcurrentModificationException
	Collection<BlockShape> selectedObjects = new HashSet<BlockShape>(this.selectedShapes);
	for (BlockShape selectedObject : selectedObjects)
	    selectedObject.setSelected(false);
    }

    @Override
    public ID getId() {
	return this.id;
    }

    @Override
    public List<BlockShape> getSelectedObjects() {
	return Collections.unmodifiableList(selectedShapes);
    }

    /**
     * Add an object to the model.
     * 
     * @param object
     *            the object to add
     * @return <code>true</code> if the object was added; <code>false</code>
     *         otherwise
     * 
     */
    @Override
    public boolean addObject(BlockShape object) {
	if (containsObject(object)) {
	    return false;
	}
	objects.add(object);
	object.addSelectedUpdateListener(objectSelectedUpdateListener);
	ChainingAttributeChangeListener attributeChangeListener =
	    new ChainingAttributeChangeListener(object);
	attributeChangeListenerMap.put(object, attributeChangeListener);
	object.addAttributeChangeListener(weakListener(attributeChangeListener, object));
	if (getGlue() != null) {
	    AttributeChangeListener<BlockShape> glueListener = getGlue().getGlueListener();
	    object.addAttributeChangeListener(weakListener(glueListener, object));
	}
	object.addSelectedUpdateListener(chainingObjectUpdateListener);
	if (modelAsAGroup != null) {
	    modelAsAGroup.addShape(object);
	}
	fireObjectAdded(object);
	lastModifiedShape = object;
	return true;
    }

    /**
     * @param object
     * @return true if object is already contained in the model
     */
    @Override
    public boolean containsObject(BlockShape shape) {
	return objects.contains(shape);
    }

    @Override
    public void removeSelectedObjects() {
	removeShapes(getSelectedObjects());
    }
    
    @Override
    public void removeShapes(List<BlockShape> shapes) {
	// need to copy so don't get concurrent modification exception
	Collection<BlockShape> shapesCopy = new ArrayList<BlockShape>(shapes);
	for (BlockShape shape : shapesCopy) {
	    removeObject(shape);
	}
    }
    
    /**
     * Remove all objects from the model.
     * 
     * @param object
     *            the model to remove
     * 
     */
    @Override
    public void removeAllObjects() {
	// to avoid concurrent modification exception this
	// does not use for (BlockShape selectedObject : objects)
	for (int i = objects.size()-1; i >= 0; i--) {
	    removeObject(objects.get(i));
	}
    }

    /**
     * Remove an object from the model.
     * 
     * @param shape
     *            the object to remove
     * 
     */
    @Override
    public boolean removeObject(BlockShape shape) {
	if (!objects.remove(shape)) {
//	    System.err.println("Warning. A shape removed from a model that it wasn't part of.");
	    return false;
	}
	shape.setSelected(false);
	shape.removeSelectedUpdateListener(objectSelectedUpdateListener);
	ChainingAttributeChangeListener attributeChangeListener =
	    attributeChangeListenerMap.remove(shape);
	shape.removeAttributeChangeListener(attributeChangeListener);
	shape.removeSelectedUpdateListener(chainingObjectUpdateListener);
	if (modelAsAGroup != null) {
	    modelAsAGroup.removeShape(shape);
	    shape.setSuperShape(null);
	}
	fireObjectRemoved(shape);
	if (glue != null) {
	    glue.removeShape(shape);
	}
	setShapeHighlight(shape, false); // just in case
	previousSelectedShapes.remove(shape);
	return true;
    }
    
    /**
     * Returns an unmodifiable collection of all shapes in the model. 
     * 
     * Please not that some of them might be in process of being transformed from
     * building blocks to patterns (i.e. isWizardOpen() returns true for them).
     * 
     * @return an unmodifiable collection of all shapes in the model
     */
    @Override
    public List<BlockShape> getShapes() {
	return Collections.unmodifiableList(objects);
    }
    
     /**
     * Process an attribute change.
     * 
     * This chains the attribute event to all registered attribute listeners.
     * 
     * @param e
     *            the attribute event to chain
     * 
     */
    private void processAttributesChanged(AttributeChangeEvent<BlockShape> e) {
	// if at least one attribute mayHaveChangeListener then respond
	// otherwise ignore the change (e.g. may be a
	// ResourceUsageAttributeHandle
	Collection<AttributeHandle<?>> changedAttributeHandles =
		e.getChangedAttributeHandles();
	for (AttributeHandle<?> handle : changedAttributeHandles) {
	    if (handle.mayHaveChangeListener()) {
		for (AttributeChangeListener<BlockShape> listener : attributeChangeListeners) {
		    listener.attributesChanged(e);
		}
	    }
	}
    }

    @Override
    public void addObjectUpdateListener(UpdateListener<BlockShape> listener) {
	objectUpdateSupport.addListener(listener);
    }

    /**
     * @param listener -- remove this UpdateListener from the model
     */
    @Override
    public void removeObjectUpdateListener(UpdateListener<BlockShape> listener) {
	objectUpdateSupport.removeListener(listener);
    }

    @Override
    public void addSelectionUpdateListener(UpdateListener<BlockShape> listener) {
	selectionUpdateSupport.addListener(listener);
    }

    @Override
    public void removeSelectionUpdateListener(UpdateListener<BlockShape> listener) {
	selectionUpdateSupport.removeListener(listener);
    }
    
    @Override
    public void addUnlockedTiedNumberAddedOrRemovedListener(UpdateListener<TiedNumberExpression<?>> listener) {
	unlockedTiedNumberListUpdateSupport.addListener(listener);
    }

    @Override
    public void removeUnlockedTiedNumberAddedOrRemovedListener(UpdateListener<TiedNumberExpression<?>> listener) {
	unlockedTiedNumberListUpdateSupport.removeListener(listener);
    }

    @Override
    public void addAttributeChangeListener(AttributeChangeListener<BlockShape> listener) {
	attributeChangeListeners.add(listener);
    }

    @Override
    public void removeAttributeChangeListener(AttributeChangeListener<BlockShape> listener) {
	attributeChangeListeners.remove(listener);
    }

    /**
     * @param listener -- a ObjectListener that will be invoked if a shape is added or removed from the model.
     */
    @Override
    public void addObjectListener(ObjectListener listener) {
	objectListeners.add(listener);
    }

    @Override
    public void removeObjectListener(ObjectListener listener) {
	objectListeners.remove(listener);
    }
    
    @Override
    public void addTotalAllocationExpressionListener(UpdateListener<Expression<Number>> listener) {
	totalAllocationExpressionUpdateSupport.addListener(listener);
    }
    
    @Override
    public void removeTotalAllocationExpressionListener(UpdateListener<Expression<Number>> listener) {
	totalAllocationExpressionUpdateSupport.removeListener(listener);
    }
    
    @Override
    public void addGlobalAllocationExpressionListener(AttributeChangeListener<BlockShape> listener) {
	this.getModelAsAGroup().addAttributeChangeListener(listener);
    }
    
    @Override
    public void removeGlobalAllocationExpressionListener(AttributeChangeListener<BlockShape> listener) {
	this.getModelAsAGroup().removeAttributeChangeListener(listener);
    }
    
    private void fireObjectAdded(BlockShape object) {
	ObjectEvent e = new ObjectEvent(this, object);
	for (ObjectListener listener : objectListeners)
	    listener.objectAdded(e);
    }

    private void fireObjectRemoved(BlockShape object) {
	ObjectEvent e = new ObjectEvent(this, object);
	for (ObjectListener listener : objectListeners)
	    listener.objectRemoved(e);
    }
    
    @Override
    public Glue getGlue() {
	return glue;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public boolean unlockedTiedNumberAdded(TiedNumberExpression<?> tiedNumberExpression) {
//	if (getTaskVariables().contains(tiedNumberExpression)) {
//	    return false;
//	}
//	taskVariables.add(tiedNumberExpression);
	UntiedNumberAdditionOrRemovalUpdateEvent event = 
	    new UntiedNumberAdditionOrRemovalUpdateEvent((TiedNumberExpression<Number>) tiedNumberExpression, true);
	unlockedTiedNumberListUpdateSupport.fireObjectUpdated(event);
	tiedNumberExpression.setLocked(false);
	setAutoSaveModel(true); // so slave is updated soon
	return true; // was really added
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public void unlockedTiedNumberRemoved(TiedNumberExpression<?> tiedNumberExpression) {
	tiedNumberExpression.setLocked(true);
	UntiedNumberAdditionOrRemovalUpdateEvent event = 
	    new UntiedNumberAdditionOrRemovalUpdateEvent((TiedNumberExpression<Number>) tiedNumberExpression, false);
	unlockedTiedNumberListUpdateSupport.fireObjectUpdated(event);
	setAutoSaveModel(true); // so slave is updated soon
    }

    @Override
    @Deprecated 
    public ArrayList<TiedNumberExpression<?>> getTaskVariables() {
	return new ArrayList<TiedNumberExpression<?>>(getContainedTiedNumbers(true));
    }

    @Override
    public void setGlue(Glue glue) {
        this.glue = glue;
    }

    @Override
    public ModelGroupShape getModelAsAGroup() {
        return modelAsAGroup;
    }

    @Override
    public void setModelAsAGroup(ModelGroupShape modelAsAGroup) {
        this.modelAsAGroup = modelAsAGroup;
    }
    
    /**
     * @return the list of handles to attributes that pair colors
     * with expressions allocating the total amount of that color.
     */
    @Override
    public List<ColorResourceAttributeHandle> getColorResourceAttributeHandles() {
	return modelAsAGroup.getAllColorResourceAttributeHandles();
    }

//    protected void copyRulesToSlave() {
//	// copy things over to the slave model
//	ModelGroupShape slaveModelAsAGroup = slaveModel.getModelAsAGroup();
//	ModelGroupShape masterModelAsAGroup = getModelAsAGroup();
//	List<ColorResourceAttributeHandle> masterColorResourceAttributeHandles = masterModelAsAGroup.getAllColorResourceAttributeHandles();
//	for (ColorResourceAttributeHandle masterHandle : masterColorResourceAttributeHandles) {
//	    ColorResourceAttributeHandle slaveHandle = 
//		slaveModelAsAGroup.getAttributeHandleForColorResource(masterHandle.getColor());
//	    if (slaveHandle != null) {
//		ValueSource<IntegerValue> valueSource = 
//		    masterModelAsAGroup.getValueSource(masterHandle);
//		Expression<IntegerValue> slaveEquivalent = 
//		    slaveModel.createSlaveEquivalent(valueSource.getExpression());
//		slaveModelAsAGroup.setValueSource(
//			slaveHandle, 
//			new ExpressionValueSource<IntegerValue>(slaveEquivalent));
//	    }
//	}
//	slaveModel.setTotalAllocationExpression(slaveModel.createSlaveEquivalent(totalAllocationExpression));
//    }

    @Override
    public void maintainGlue(PatternShape patternShape) {
	if (getGlue() != null) {
	    getGlue().maintainGlue(patternShape, getShapes());
	}
    }
    
    @Override
    public void maintainGlue() {
	if (getGlue() != null) {
	    List<BlockShape> shapes = getShapes();
	    // check all borders between all shapes
	    getGlue().maintainGlue(shapes, shapes);
	}
    }

    /**
     * @param shapes
     * @param pattern
     * 
     * removes shapes and adds pattern
     */
    @Override
    public void replaceShapesWithPattern(List<BlockShape> shapes, PatternShape pattern) {
	removeShapes(shapes);
	// compute the glue if iterated once
	Attribute<Number> attribute = pattern.getAttribute(PatternShape.ITERATIONS);
	Expression<Number> iterations = attribute.getValueSource().getExpression();
	attribute.setValueSource(new SimpleValueSource<Number>(Number.ONE));
	addObject(pattern);
	pattern.setSelected(true);
	maintainGlue(pattern);
        // trigger glue
	attribute.setValue(iterations.evaluate());
	// restore iteration count expression
	attribute.setValueSource(new ExpressionValueSource<Number>(iterations));
    }

    @Override
    public AttributeChangeListener<BlockShape> weakListener(AttributeChangeListener<BlockShape> listener, Object source) {
	// subclass ExpresserModelSlaveCopy overrides this to make it weak
	return listener;
    }
      
    @Override
    public void setShapeHighlight(BlockShape shape, boolean on) {
	if (on) {
	    if (!highlightedShapes.contains(shape)) {
		highlightedShapes.add(shape);
	    }
	} else {
	    highlightedShapes.remove(shape);
	    if (highlightedShapes.isEmpty()) {
		// add the last shape highlighted due to property lists
		// to the list of previously selected shapes
		previousSelectedShapes.clear();
		previousSelectedShapes.add(shape);
	    }
	}
    }
    
    @Override
    public void removeAllHighlights() {
	highlightedShapes.clear();
    }

    @Override
    public List<BlockShape> getHighlightedShapes() {
        return highlightedShapes;
    }
    
    /**
     * @param walker instance of a subclass of Walker
     * @return true if the walk was not terminated by the walker returning false
     */
    @Override
    public boolean walkToTiedNumbers(Walker walker) {
	for (BlockShape shape : objects) {
	    if (!shape.walkToTiedNumbers(walker, null, null, this)) {
		return false;
	    }
	}
	if (!getModelAsAGroup().walkToTiedNumbers(walker, null, null, this)) {
	    // to reach the color specific rules
	    return false;
	}
	if (locatedExpressions != null) {
	    for (LocatedExpression<Number> locatedExpression : locatedExpressions) {
		if (!locatedExpression.getExpression().walkToTiedNumbers(walker, this)) {
		    return false;
		}
	    }
	}
	return getTotalAllocationExpression().walkToTiedNumbers(walker, null, null, this);
    }

    @Override
    public List<BlockShape> getTemporaryHighlightedShapes() {
        return temporaryHighlightedShapes;
    }

    @Override
    public void setTemporaryHighlightedShapes(List<BlockShape> temporaryHighlightedShapes) {
//	dirtyModel = true;
        this.temporaryHighlightedShapes = temporaryHighlightedShapes;
    }
    
    @Override
    public void addTemporaryHighlightedShape(BlockShape shape) {
	if (shape != null && !temporaryHighlightedShapes.contains(shape)) { 
//	    dirtyModel = true;
	    temporaryHighlightedShapes.add(shape);
	}
    }
    
    @Override
    public void clearTemporaryHighlightedShapes() {
	temporaryHighlightedShapes.clear();
    }

    @Override
    public Expression<Number> getTotalAllocationExpression() {
        return totalAllocationExpression;
    }

    @Override
    public void setTotalAllocationExpression(Expression<Number> newTotalAllocationExpression) {
	boolean sameValue = totalAllocationExpression.equals(newTotalAllocationExpression);
	totalAllocationExpression = newTotalAllocationExpression;
	if (!sameValue) {
	    UpdateEvent<Expression<Number>> event = 
		new UpdateEvent<Expression<Number>>(totalAllocationExpression);
	    totalAllocationExpressionUpdateSupport.fireObjectUpdated(event);
	}
    }

    /**
     * @param shapes
     * 
     * restores the shape or shapes to their state before 
     * being made into a pattern
     */
    @Override
    public void undoPattern(final List<BlockShape> shapes) {
	final boolean singular = shapes.size() == 1;
	final boolean treatAsATile = singular ? shapes.get(0).treatAsTile() : false;
	final boolean treatAsBuildingBlock = singular ? shapes.get(0).treatAsBuildingBlock() : false;
	if (treatAsATile) {
	    PatternShape pattern = (PatternShape) shapes.get(0);
	    pattern.setTreatAsTileIfRepeatsTileOnce(false);
	    pattern.removeColorAllocations();
	} else {
	    boolean distinguishTilesFromPatterns = MiGenConfiguration.isDistinguishTilesFromPatterns();
	    for (BlockShape shape : shapes) {
		shape.removeColorAllocations();
		PatternShape pattern = (PatternShape) shape;
		// if made to remove rather than add need to restore before 
		// turning the pattern back into a building block
		pattern.setPositive(true);
		BlockShape baseShape = pattern.getShape();
		if (baseShape instanceof GroupShape) {
		    if (treatAsBuildingBlock) {
			GroupShape groupShape = (GroupShape) baseShape;
			// copy to array list to avoid concurrent access exceptions
			List<BlockShape> groupsShapes = new ArrayList<BlockShape>(groupShape.getShapes());
			for (BlockShape groupsShape : groupsShapes) {
			    groupShape.removeShape(groupsShape);
			    if (groupsShape.isTile()) {
				groupsShape.removeColorAllocations();
			    }
			    addObject(groupsShape);
			    if (distinguishTilesFromPatterns) {    
				treatAsTileIfNeeded(groupsShape);
			    }
			}
			removeObject(pattern);				    
		    } else {
			pattern.setBuildingBlockStatus(PatternShape.TREAT_AS_A_BUILDING_BLOCK);
			pattern.setIterations(1);
		    }
		} else if (baseShape instanceof PatternShape) {
		    pattern.setShape(null);
		    addObject(baseShape);
		    removeObject(pattern);
		    if (distinguishTilesFromPatterns) {    
			treatAsTileIfNeeded(baseShape);
		    }
		} else {
		    Attribute<Number> attribute = pattern.getAttribute(PatternShape.ITERATIONS);
		    attribute.setValueSource(new ExpressionValueSource<Number>(Number.ONE));
		    if (distinguishTilesFromPatterns) {    
			treatAsTileIfNeeded(pattern);
		    }
		}
	    }
	}
    }
    
    private static void treatAsTileIfNeeded(BlockShape groupsShape) {
	PatternShape groupsPatternShape = (PatternShape) groupsShape;
	if (groupsPatternShape.getShape() instanceof BasicShape) {
	    groupsPatternShape.setTreatAsTileIfRepeatsTileOnce(true);
	}
    }
    
    @Override
    public ExpresserModelImpl createCopy() {
	ExpresserModelImpl copy = new ExpresserModelImpl();
	copy.transferStateFrom(this);
	return copy;
    }
    
    @Override
    public ExpresserModelImpl createCopyAndReplaceUnlockedTiedNumbersWithNewOnes() {
	ExpresserModelImpl copy = new ExpresserModelImpl();
	copy.transferStateFrom(this);
	copy.replaceUnlockedTiedNumbersWithNewOnes();
	return copy;
    }
    
    @Override
    public boolean isDirtyPanel() {
        return dirtyPanel;
    }

    @Override
    public void setDirtyPanel(boolean dirtyPanel) {
        this.dirtyPanel = dirtyPanel;
    }

    @Override
    public boolean isDirtyModel() {
        return dirtyModel;
    }

    @Override
    public void setDirtyModel(boolean dirtyModel) {
        this.dirtyModel = dirtyModel;
        if (dirtyModel) {
            lastUndoIndex = -1;
//            if (slaveModel != null) {
//        	slaveModel.setDirtyModel(true);
//            }
        }
    }

    @Override
    public boolean isAutoSaveModel() {
        return autoSaveModel;
    }

    @Override
    public void setAutoSaveModel(boolean autoSaveModel) {
        this.autoSaveModel = autoSaveModel;
        if (autoSaveModel) {
            setDirtyModel(true);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
	// Server doesn't accept names longer than 50 characters;
	if (name != null && name.length() > 50) {
	    name = name.substring(0, 50);
	}
        this.name = name;
    }

    @Override
    public Palette getPalette() {
        return palette;
    }
    
    /**
     * @return true if all the global model rules (color-specific and the rule for unruled colors)
     * are correct.
     */
    @Override
    public boolean areAllGlobalAllocationsCorrect() {
	getModelAsAGroup().setCorrectExpressionsUpToDate(false); // force recomputation
	return getModelAsAGroup().areAllGlobalAllocationsCorrect();
    }
    
    /**
     * @return true if all colors each shape has correct local color allocations.
     */
    @Override
    public boolean areAllLocalColorAllocationsCorrect() { 
	for (BlockShape shape : getShapes()) {
	    if (!shape.allLocalColorAllocationsCorrect()) {
		return false;
	    }
	}
	return true;
    }
    
    /**
     * @return true if all the allocations for the entire model
     * and each shape are correct for all colors.
     */
    @Override
    public boolean areAllLocalAndTotalAllocationsCorrect() {
	if (!getModelAsAGroup().areAllGlobalAllocationsCorrect()) {
	    return false;
	}
	return areAllLocalColorAllocationsCorrect();
    }

    /**
     * @param onlyUnlockedOnes -- true to include only those that are unlocked
     * @return a list of the tied numbers that can be reached from this 
     */
    @Override
    public ArrayList<TiedNumberExpression<Number>> getContainedTiedNumbers(boolean onlyUnlockedOnes) {
	final ArrayList<TiedNumberExpression<Number>> tiedNumbers =
	    new ArrayList<TiedNumberExpression<Number>>();
	Walker walker = tiedNumbersWalker(onlyUnlockedOnes, tiedNumbers);
	walkToTiedNumbers(walker);
	return tiedNumbers;
    }
    
    @Override
    public ArrayList<TiedNumberExpression<Number>> getUnlockedNumbersInRules(boolean onlyUnlockedOnes) {
	final ArrayList<TiedNumberExpression<Number>> tiedNumbers =
	    new ArrayList<TiedNumberExpression<Number>>();
	Walker walker = tiedNumbersWalker(onlyUnlockedOnes, tiedNumbers);
	if (getModelAsAGroup().walkToTiedNumbers(walker, null, null, this)) {
	    getTotalAllocationExpression().walkToTiedNumbers(walker, null, null, this);
	}
	return tiedNumbers;
    }

    protected Walker tiedNumbersWalker(final boolean onlyUnlockedOnes,
	                               final ArrayList<TiedNumberExpression<Number>> tiedNumbers) {
	return new Walker() {

	    @Override
	    @SuppressWarnings("unchecked")
	    public boolean tiedNumberFound(
		    TiedNumberExpression<?> tiedNumber, 
		    BlockShape shape, 
		    AttributeHandle<Number> handle,
		    ExpresserModel expresserModel) {
		if (onlyUnlockedOnes && tiedNumber.isLocked()) {
		    return true;
		}
		if (tiedNumber.getOriginal() != tiedNumber) {
		    // is a proxy -- not a real tied number
		    return true;
		}
		if (!TiedNumberExpression.containsTiedNumberWithSameId(tiedNumber, tiedNumbers)) {
		    // can't use !tiedNumbers.contains(tiedNumber) since
		    // both TiedNumberExpression (model) and TiedNumber (view)
		    // can be encountered
		    tiedNumbers.add((TiedNumberExpression<Number>) tiedNumber);
		}
		return true;
	    }
	};
    }    
    
    /**
     * Returns a list with all tied numbers in the model, both locked and unlocked. 
     * 
     * This convenience method is equivalent to getContainedTiedNumbers(false).
     * 
     * @return a list with all unlocked numbers in the model.
     */
    @Override
    public List<TiedNumberExpression<Number>> getAllTiedNumbers() {
	return getContainedTiedNumbers(false);
    }
    
    /**
     * Returns a list with all unlocked numbers in the model. 
     * 
     * This convenience method is equivalent to getContainedTiedNumbers(true).
     * 
     * @return a list with all unlocked numbers in the model.
     */
    @Override
    public List<TiedNumberExpression<Number>> getUnlockedNumbers() {
	return getContainedTiedNumbers(true);
    }
    
    /**
     * Replaces all unlocked tied number with fresh ones maintaining
     * the same relationships.
     */
    @Override
    public void replaceUnlockedTiedNumbersWithNewOnes() {
	final HashMap<TiedNumberExpression<?>, TiedNumberExpression<?>> mappingOldToNew =
	    new HashMap<TiedNumberExpression<?>, TiedNumberExpression<?>>();
		
	Walker walker = new Walker() {

	    @Override
	    @SuppressWarnings({ "unchecked", "rawtypes" })
	    public boolean tiedNumberFound(
		    TiedNumberExpression<?> oldTiedNumber, 
		    BlockShape shape, 
		    AttributeHandle<Number> handle,
		    ExpresserModel expresserModel) {
		if (!oldTiedNumber.isLocked()) {
		    TiedNumberExpression<?> newTiedNumber = mappingOldToNew.get(oldTiedNumber);
		    if (newTiedNumber == null) {
			newTiedNumber = oldTiedNumber.createFreshCopy(false);
			mappingOldToNew.put(oldTiedNumber, newTiedNumber);
		    }
		    if (handle != null) {
			if (shape != null) {
			    Attribute<?> attribute = shape.getAttribute(handle);
			    if (attribute != null) {
				Expression<?> attributeValueExpression = 
				    attribute.getValueSource().getExpression();
				if (attributeValueExpression == oldTiedNumber) {
				    attribute.setValueSource(new ExpressionValueSource(newTiedNumber));
				} else {
				    attributeValueExpression.replaceAll(oldTiedNumber, newTiedNumber);
				}
			    }
			}
		    } else if (shape == null) {
			// total allocation expression
			if (totalAllocationExpression == oldTiedNumber) {
			    setTotalAllocationExpression((Expression<Number>) newTiedNumber);
			} else {
			    totalAllocationExpression.replaceAll(oldTiedNumber, newTiedNumber);
			}
		    }
		}
		return true;
	    }
	    
	};
	walkToTiedNumbers(walker);
    }

    /**
     * Moves the state from otherModel to this.
     * 
     * @param otherModel
     */
    @Override
    public void transferStateFrom(ExpresserModel otherModel) {
	setTotalAllocationExpression(otherModel.getTotalAllocationExpression().createCopy());
	// palettes are immutable so sharing should be fine
	setPalette(otherModel.getPalette());
	setName(otherModel.getName());
	for (BlockShape shape : otherModel.getShapes()) {
	    addObject(shape.createCopy());
	}
	transferColorSpecificRules(otherModel);
	setAutosavedNames(new ArrayList<String>(otherModel.getAutosavedNames()));
	setTotalAllocationExpression(otherModel.getTotalAllocationExpression().createCopy());
	if (getGlue() != null) {
	    getGlue().initializeGlue(getShapes());
	}
	// following needed to walk the model for all tied numbers. E.g. Issue 1793.
	locatedExpressions = otherModel.getLocatedExpressions();
    }

    /**
     * @param otherModel
     */
    @Override
    public void transferColorSpecificRules(ExpresserModel otherModel) {
	ModelGroupShape otherModelAsAGroup = otherModel.getModelAsAGroup();
	List<ColorResourceAttributeHandle> colorResourceAttributeHandles = otherModelAsAGroup.getAllColorResourceAttributeHandles();
	for (ColorResourceAttributeHandle colorResourceAttributeHandle : colorResourceAttributeHandles) {
	    modelAsAGroup.addAttribute(otherModelAsAGroup.getAttribute(colorResourceAttributeHandle).createCopy());
	}
    }

    public void setPalette(Palette palette) {
        this.palette = palette;
    }

    @Override
    public List<LocatedExpression<Number>> getLocatedExpressions() {
	return locatedExpressions;
    }

    @Override
    public List<ModelColor> getColorsWithoutRules() {
	return getModelAsAGroup().getColorsWithoutRules();
    }

    /**
     * @param color
     * @return if color is null then an icon for the remaining unruled colors
     * otherwise the icon for the color.
     */
    
    /**
     * All of the top-level shapes of the model are marked
     * as not being deletable by the user.
     */
    @Override
    public void currentShapesAreUndeletable() {
	Collection<BlockShape> shapes = getShapes();
	for (BlockShape shape : shapes) {
	    addUndeletableShape(shape);
	}
    }
    
    @Override
    public void addUndeletableShape(BlockShape shape) {
	undeletableShapes.add(shape);
    }
    
    @Override
    public void removeUndeletableShape(BlockShape shape) {
	undeletableShapes.remove(shape);
    }
    
    @Override
    public boolean isUndeletableShape(BlockShape shape) {
	return undeletableShapes.contains(shape);
    }

    /**
    * Returns the set of patterns that cannot be deleted 
    * in this model (maybe empty). 
    */
    @Override
    public Set<BlockShape> getUndeletableShapes() {
        return undeletableShapes;
    }
    
    /* 
     * @returns true if other has the same objects and model as a group
     */
    @Override
    public boolean equals(Object other) {
	if (this == other) {
	    return true;
	}
	if (!(other instanceof ExpresserModelImpl)) {
	    return false;
	}
	ExpresserModelImpl otherAsExpresserModel = (ExpresserModelImpl) other;
	if (!this.getModelAsAGroup().equals(otherAsExpresserModel.getModelAsAGroup())) {
	    return false;
	}
	if (!this.getTotalAllocationExpression().equals(otherAsExpresserModel.getTotalAllocationExpression())) {
	    return false;
	}
	return this.getShapes().equals(otherAsExpresserModel.getShapes());
    }

    @Override
    public boolean isIndicatorSinceLastSave() {
        return indicatorSinceLastSave;
    }

    @Override
    public void setIndicatorSinceLastSave(boolean indicatorSinceLastSave) {
        this.indicatorSinceLastSave = indicatorSinceLastSave;
    }

    /**
     * @param x
     * @param y
     * @return the list of shapes that have at least one tile at x, y.     
     */
    @Override
    public List<BlockShape> getShapesAt(int x, int y) {
	// a much more expensive way of computing this compared to accessToLocatedExpressions.getShapesAt(x, y)
	// the cost of this is proportional to the number of iterations while accessToLocatedExpressions.getShapesAt(x, y)
	// has a cost proportional to the number of tiles currently in view
	// this works for locations that are not being viewed (scrolled off panel)
	ArrayList<BlockShape> shapes = new ArrayList<BlockShape>();
	for (BlockShape shape : getShapes()) {
	    if (shape.hasTileAt(x, y)) {
		shapes.add(shape);
	    }
	}
	return shapes;
    }
    
    /**
     * @return true if at least one shape of this model is removed (i.e. negative)
     */
    @Override
    public boolean containsAnyNegativePatterns() {
	for (BlockShape shape : getShapes()) {
	    if (!shape.isCompletelyPositive()) {
		return true;
	    }
	}
	return false;
    }
    
    @Override
    public void addColorToGrid(int x, int y, ModelColor color, boolean correctAllocation, BasicShape basicShape) {
	Location location = new Location(x, y);
	ArrayList<AllocatedColor> colorList = colorGridMap.get(location);
	if (colorList == null) {
	    colorList = new ArrayList<AllocatedColor>();
	    xOffset = Math.min(x, 0);
	    yOffset = Math.min(y, 0);
	}
	AllocatedColor allocatedColor = new AllocatedColor(color, correctAllocation);
	// this used to cancel positive and negative colors but handled in TileView now
//	AllocatedColor negativeColor = allocatedColor.getNegatedCopy();
//	if (colorList.contains(negativeColor)) {
//	    colorList.remove(negativeColor);
//	} else {
	colorList.add(allocatedColor);
	colorGridMap.put(location, colorList);
	ArrayList<BlockShape> shapesAtLocation = shapeGridMap.get(location);
	if (shapesAtLocation == null) {
	    shapesAtLocation = new ArrayList<BlockShape>();
	}
	shapesAtLocation.add(basicShape.getUltimateSuperShape());
	shapeGridMap.put(location, shapesAtLocation);
    }
    
    @Override
    public void clearColorGridMap() {
	colorGridMap.clear();
	shapeGridMap.clear();
    }
    
    /**
     * @return true if overlaps were painted for this model 
     *         false if it was painted without overlaps
     *         null if it wasn't painted
     */
    @Override
    public Boolean anyUncorrectedOverlapsPainted() {
	return overlapPainted;
    }
    
    /**
     * @return true if any negatively coloured tiles were painted for this model 
     *         false if only positive tiles were painted
     *         null if it wasn't painted
     */
    @Override
    public Boolean negativeTilePainted() {
	return negativeTilePainted;
    }
    
    @Override
    public ArrayList<TiedNumberExpression<?>> getAnimationTiedNumbers() {
	return new ArrayList<TiedNumberExpression<?>>(getContainedTiedNumbers(true));
    }
    
    @Override
    public boolean isAnyTiedNumberAnimating() {
	return animatingTiedNumbers != null && !animatingTiedNumbers.isEmpty();
    }

    @Override
    public List<TiedNumberExpression<?>> getAnimatingTiedNumbers() {
        return animatingTiedNumbers;
    }

    @Override
    public void setAnimatingTiedNumbers(List<TiedNumberExpression<?>> animatingTiedNumbers) {
        this.animatingTiedNumbers = animatingTiedNumbers;
    }

    @Override
    public void addLocatedExpression(LocatedExpression<Number> locatedExpression) {
	locatedExpressions.add(locatedExpression);	
    }
    
    @Override
    public void removeLocatedExpression(LocatedExpression<Number> locatedExpression) {
	locatedExpressions.remove(locatedExpression);	
    }

    @Override
    public void setOverlapPainted(boolean overlapPainted) {
        this.overlapPainted = overlapPainted;
        if (previousOverlapsReport != this.overlapPainted) {
            previousOverlapsReport = this.overlapPainted;
            UIEventManager.processEvent(new ChangeInDisplayOfAnyOverlaps(this, overlapPainted));
        }
    }

    @Override
    public boolean isNegativeTilePainted() {
        return negativeTilePainted;
    }

    @Override
    public void setNegativeTilePainted(boolean negativeTilePainted) {
        this.negativeTilePainted = negativeTilePainted;
    }
    
    @Override
    public ArrayList<String> getAutosavedNames() {
        return autosavedNames;
    }

    @Override
    public void setAutosavedNames(ArrayList<String> autosavedNames) {
        this.autosavedNames = autosavedNames;
    }
    
    @Override
    public void addAutosavedName(String fileName) {
	autosavedNames.add(fileName);
    }

    @Override
    public int getLastUndoIndex() {
        return lastUndoIndex;
    }

    @Override
    public void setLastUndoIndex(int lastUndoIndex) {
        this.lastUndoIndex = lastUndoIndex;
    }

    @Override
    public TaskIdentifier getTaskIdentifier() {
        return taskIdentifier;
    }

    @Override
    public void setTaskIdentifier(TaskIdentifier taskIdentifier) {
        this.taskIdentifier = taskIdentifier;
    }

    @Override
    public HashMap<Location, ArrayList<AllocatedColor>> getColorGridMap() {
        return colorGridMap;
    }

    @Override
    public HashMap<Location, ArrayList<BlockShape>> getShapeGridMap() {
        return shapeGridMap;
    }

    @Override
    public int getxOffset() {
        return xOffset;
    }

    @Override
    public int getyOffset() {
        return yOffset;
    }

    /**
     * @return a list of the selected shapes if there are any
     * otherwise returns the list of shapes that are highlighted
     * due to selection of their property list if there are any
     * otherwise returns what was selected or highlighted last
     */
    @Override
    public List<BlockShape> getPreviousSelectedShapes() {
	if (selectedShapes.isEmpty()) {
	    if (highlightedShapes.isEmpty()) {
		return previousSelectedShapes;
	    } else {
		return highlightedShapes;
	    }
	} else {
	    return selectedShapes;
	}
    }
    
    @Override
    public BlockShape getLastModifiedShape() {
	return lastModifiedShape;
    }
    
    /**
     * @return a correct expression for the total number of tiles for any value
     * of the unlocked tied numbers involved
     */
    @Override
    public Expression<Number> getCorrectTotalTilesExpression() {
	return getModelAsAGroup().getCorrectTotalTilesExpression();
    }
    
    /**
     * @param color - the color of the tiles to be analysed
     * 
     * @return a correct expression for the total number of tiles of the specified color for any value
     * of the unlocked tied numbers involved
     */
    @Override
    public Expression<Number> getCorrectColorTileExpression(ModelColor color) {
	return getModelAsAGroup().getCorrectColorTileExpression(color);
    }
    
    /**
     * Recomputes the color grid that maintains what tiles and their local color correctness
     * are at each grid location
     */
    @Override
    public void updateColorGrid() {
	clearColorGridMap();
	for (BlockShape shape : getShapes()) {
	    ArrayList<ModelColor> incorrectlyAllocatedColors = new ArrayList<ModelColor>();
	    shape.updateColorGrid(this, 0, 0, true, incorrectlyAllocatedColors, true, null);
	}
    }

    private boolean shouldBePolled = true;

    private HashMap<TiedNumberExpression<Number>, AnimationSettings> animationSettings; 
    
    @Override
    public void setAsPollable(boolean b) {
	this.shouldBePolled = b;
    }

    @Override
    public boolean isPollable() {
	return shouldBePolled;
    }

    @Override
    public synchronized BoundingBox getBoundingBox() {
	BoundingBox result = null;
	if (this.hasNoShapes())
	    return null;
	for (BlockShape shape : getShapes()) {
	    if (result == null) {
		result = shape.getBoundingBox();
	    } else {
		BoundingBox box = shape.getBoundingBox();
		if (box != null) {
		    result.extendToInclude(box);
		}
	    }
	}
	return result;
    }
    
    /**
     * This method is deprecated. Use hasNoShapes() or hasNoShapesOrRules() instead.
     */
    @Override
    @Deprecated
     public boolean isEmpty() {
	return this.hasNoShapes();
    }

    @Override
    public boolean hasNoShapes() {
	if (getShapes().size() == 0) {
	    return true; 
	} else {
	    return false;
	}
    }
    
    @Override
    public boolean hasNoShapesRulesOrExpressions() {
	return getShapes().isEmpty() && 
	       locatedExpressions.isEmpty() &&
	       getModelAsAGroup().isEmpty();
    }

    @Override
    /**
     * Replaces all unlocked tied number with fresh ones maintaining
     * the same relationships but adding offsets to the values
     * @param model 
     */
    public void replaceUnlockedTiedNumbersWithNewValues() {
	walkToTiedNumbers(new ReplaceUnlockedTiedNumbersWithNewValuesWalker());
    }

    @Override
    public BlockShape getShapeWithId(String shapeId) {
	List<BlockShape> shapes = getShapes();
	for (BlockShape shape : shapes) {
	    if (shapeId.equals(shape.getUniqueId())) {
		return shape;
	    }
	    BlockShape subShape = shape.getSubShapeWithId(shapeId);
	    if (subShape != null) {
		return subShape;
	    }
	}
	return null;
    }

    @Override
    public TiedNumberExpression<?> getTiedNumberWithId(final String expressionId) {
	final TiedNumberExpression<?>[] resultHolder = new TiedNumberExpression<?>[1];
	Walker walker = new Walker() {

	    @Override
	    public boolean tiedNumberFound(TiedNumberExpression<?> tiedNumber,
		                           BlockShape shape, 
		                           AttributeHandle<Number> handle,
		                           ExpresserModel expresserModel) {
		if (expressionId.equals(tiedNumber.getIdString())) {
		    resultHolder[0] = tiedNumber;
		    return false;
		}
		return true;
	    }

	};
	walkToTiedNumbers(walker);
	return resultHolder[0];
    }

    @Override
    public boolean setExpressionWithIdVisible(String expressionId, boolean visible) {
	TiedNumberExpression<?> tiedNumber = getTiedNumberWithId(expressionId);
	if (tiedNumber != null) {
	    for (LocatedExpression<Number> locatedExpression : locatedExpressions) {
		if (locatedExpression.getExpression() == tiedNumber) {
		    if (visible) {
			// already visible nothing more to do
			// note that it might be under some component or scrolled off screen 
		    } else {
			removeLocatedExpression(locatedExpression);
		    }
		    return true;
		}
	    }
	    // need to add it
	    @SuppressWarnings("unchecked")
	    LocatedExpression<Number> locatedExpression = 
		new LocatedExpression<Number>((Expression<Number>) tiedNumber, 0, 0);
	    addLocatedExpression(locatedExpression);
	    return true;
	} else {
	    return false; // unable to find an expression with the expressionId
	}
    }
    
    @Override
    public void finalize() throws Throwable {
	removeAll();
	super.finalize();
    }

    /**
     * Removes all objects and anything those objects reference
     */
    @Override
    public void removeAll() {
	for (int i = objects.size()-1; i >= 0; i--) {
	    // to ensure there are no memory leaks
	    BlockShape object = objects.get(i);
	    removeObject(object);
	    object.removeAll();
	}
	for (int i = locatedExpressions.size()-1; i >= 0; i--) {
	    LocatedExpression<Number> locatedExpression = locatedExpressions.get(i);
	    locatedExpression.removeAll();
	    removeLocatedExpression(locatedExpression);
	}
	modelAsAGroup.removeAll();	
    }

    @Override
    public boolean isMasterModel() {
        return masterModel;
    }

    @Override
    public void setMasterModel(boolean masterModel) {
        this.masterModel = masterModel;
    }

    @Override
    public HashMap<TiedNumberExpression<Number>, AnimationSettings> getAnimationSettings() {
	if (animationSettings == null) {
	    animationSettings = new HashMap<TiedNumberExpression<Number>, AnimationSettings>();
	}
	return animationSettings;
    }

    @Override
    public void setAnimationSettings(HashMap<TiedNumberExpression<Number>, AnimationSettings> settings) {
	this.animationSettings = settings;	
    }
    
}

