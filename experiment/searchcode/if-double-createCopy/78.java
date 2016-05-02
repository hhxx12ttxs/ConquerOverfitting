package uk.ac.lkl.expresser.client;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.gwt.user.client.ui.RequiresResize;

import uk.ac.lkl.common.util.expression.Expression;
import uk.ac.lkl.common.util.value.IntegerValue;
import uk.ac.lkl.migen.system.expresser.model.Attribute;
import uk.ac.lkl.migen.system.expresser.model.AttributeHandle;
import uk.ac.lkl.migen.system.expresser.model.ExpresserModel;
import uk.ac.lkl.migen.system.expresser.model.ExpresserModelImpl;
import uk.ac.lkl.migen.system.expresser.model.ExpressionValueSource;
import uk.ac.lkl.migen.system.expresser.model.Walker;
import uk.ac.lkl.migen.system.expresser.model.shape.block.BlockShape;
import uk.ac.lkl.migen.system.expresser.model.tiednumber.TiedNumberExpression;

/**
 * The computer's model is maintained by this class
 * It is a fresh copy of the My Model and then each variable is given an offset
 * in a manner similar to how ExpresserModelSlaveCopy works.
 * 
 * @author Ken Kahn
 *
 */
public class ComputersModelPanel extends ExpresserVerticalPanel implements CanvasUpdatedListener, RequiresResize {

    private ExpresserModel myModel;
    private ExpresserModel computersModel;
    private ExpresserCanvasSlave canvas;
    
    private HashMap<TiedNumberExpression<?>, TiedNumberExpression<?>> mappingOldVariablesToNew =
	    new HashMap<TiedNumberExpression<?>, TiedNumberExpression<?>>();
    
    // offsets used to minimise the possibility of two offsets being the same
    private ArrayList<Integer> offsetsInUse = new ArrayList<Integer>();
    private ComputersModelRulesPanel modelRulesPanel;
//    private ExpresserCanvas myModelCanvas;
    
    // tied numbers in the slave universe differ from the master
    // by one of randomOffsets chosen randomly
    static private int randomOffsets[] = { 2, -3, -5, 5, 4, -2, 3, -4 };
    // following used only if all the above are inappropriate
    static private int emergencyRandomOffsets[] =
	    { 6, 12, 8, -8, 10, 7, 11, -6, 9, -9, -12, -10, -7, -11 };

    public ComputersModelPanel(ExpresserCanvas myModelCanvas, double width) {
	super();
	this.myModel = myModelCanvas.getModel();
	int gridSize = myModelCanvas.getGridSize()/2; // default half size
	computersModel = new ExpresserModelImpl();
	canvas = new ExpresserCanvasSlave(gridSize, (int) width, myModelCanvas.getOffsetHeight(), computersModel);
	ComputersModelCanvasToolBar computersModelCanvasToolBar = new ComputersModelCanvasToolBar(canvas);
	add(computersModelCanvasToolBar);
	int toolBarHeight = myModelCanvas.getToolBar().getOffsetHeight();
	computersModelCanvasToolBar.setHeight(toolBarHeight + "px");
	add(canvas);
	myModelCanvas.addCanvasUpdatedListener(this);
	modelRulesPanel = 
	    new ComputersModelRulesPanel(canvas, (MyModelRulesPanel) myModelCanvas.getModelRulesPanel());
	canvas.addModelRulesPanel(modelRulesPanel);
    }

    @Override
    public void canvasUpdated() {
	canvas.removeAllTileViews();
	canvas.clearPreviouslyMappedTiedNumbers();
	computersModel = createComputersModel(myModel);
	ExpresserModel model = canvas.getModel();
	model.removeAllObjects();
	canvas.addModel(computersModel, null);
	canvas.updateTiles();
	modelRulesPanel.updateModelRulesPanel(model);
    }

    /**
     * @param model 
     * @return
     */
    protected ExpresserModel createComputersModel(ExpresserModel model) {
	ExpresserModel modelCopy = model.createCopy();
	replaceUnlockedTiedNumbersWithNewValues(modelCopy);
	return modelCopy;
    }
    
    /**
     * Replaces all unlocked tied number with fresh ones maintaining
     * the same relationships but adding offsets to the values
     * @param model 
     */
    public void replaceUnlockedTiedNumbersWithNewValues(final ExpresserModel model) {
		
	Walker walker = new Walker() {

	    @SuppressWarnings({ "unchecked", "rawtypes" })
	    public boolean tiedNumberFound(
		    TiedNumberExpression<?> oldTiedNumber, 
		    BlockShape shape, 
		    AttributeHandle<IntegerValue> handle,
		    ExpresserModel expresserModel) {
		if (!oldTiedNumber.isLocked()) {
		    TiedNumberExpression<?> newTiedNumber = mappingOldVariablesToNew.get(oldTiedNumber);
		    if (newTiedNumber == null) {
			newTiedNumber = oldTiedNumber.createFreshCopy(false);
			newOffset((TiedNumberExpression<IntegerValue>) oldTiedNumber, 
				  (TiedNumberExpression<IntegerValue>) newTiedNumber);
			mappingOldVariablesToNew.put(oldTiedNumber, newTiedNumber);
		    } else {
			newTiedNumber.setName(oldTiedNumber.getName());
			newTiedNumber.setNamed(oldTiedNumber.isNamed());
			if (oldTiedNumber.getValue().equals(newTiedNumber.getValue())) {
			    // need new offset
			    newOffset((TiedNumberExpression<IntegerValue>) oldTiedNumber, 
				      (TiedNumberExpression<IntegerValue>) newTiedNumber);
			}
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
			Expression<IntegerValue> totalAllocationExpression = model.getTotalAllocationExpression();
			if (totalAllocationExpression == oldTiedNumber) {
			    model.setTotalAllocationExpression((Expression<IntegerValue>) newTiedNumber);
			} else {
			    totalAllocationExpression.replaceAll(oldTiedNumber, newTiedNumber);
			}
		    }
		}
		return true;
	    }
	    
	};
	model.walkToTiedNumbers(walker);
    }
    
    protected void newOffset(TiedNumberExpression<IntegerValue> oldTiedNumber, TiedNumberExpression<IntegerValue> newTiedNumber) {
	int intValue = oldTiedNumber.getValue().getInt();
	int offset = computeValueOffsetForSlave(oldTiedNumber.hashCode(), intValue);
	newTiedNumber.setValue(new IntegerValue(intValue + offset));
    }

    public int computeValueOffsetForSlave(int hashCode, int intValue) {
	// by adding the hash code of the tied number and the value multiplied
	// by a small prime
	// we should get different offsets for different values
	// see Issue 384
	int hashCodeRemaining = hashCode;
	int key = hashCode + intValue * 7;
	int offset = randomOffsets[key % randomOffsets.length];
	if (intValue + offset <= 1) {
	    // don't want a value less than 2
	    offset = Math.abs(offset);
	}
	int offsetThatMayBeInUse = offset;
	// try to avoid having the same offsets for different tied numbers
	while (isOffsetInUse(offset) && hashCodeRemaining > 0) {
	    hashCodeRemaining = hashCodeRemaining / randomOffsets.length;
	    offset = randomOffsets[hashCodeRemaining % randomOffsets.length];
	    if (intValue + offset <= 1) {
		offset = Math.abs(offset);
	    }
	}
	if (hashCodeRemaining == 0) {
	    // try again with emergency numbers
	    hashCodeRemaining = hashCode;
	    key = hashCode + intValue * 7;
	    offset = emergencyRandomOffsets[key % emergencyRandomOffsets.length];
	    if (intValue + offset <= 1) {
		// don't want a value less than 2
		offset = Math.abs(offset);
	    }
	    // try to avoid having the same offsets for different tied numbers
	    while (isOffsetInUse(offset) && hashCodeRemaining > 0) {
		hashCodeRemaining =
			hashCodeRemaining / emergencyRandomOffsets.length;
		offset = emergencyRandomOffsets[hashCodeRemaining % emergencyRandomOffsets.length];
		if (intValue + offset <= 1) {
		    offset = Math.abs(offset);
		}
	    }
	}
	if (hashCodeRemaining == 0) {
	    offset = firstUnusedOffset(2 - intValue);
	    if (offset == 0) {
		return offsetThatMayBeInUse;
	    }
	}
	usingOffset(offset);
	return offset;
    }
    
    public boolean isOffsetInUse(Integer offset) {
	return offsetsInUse.contains(offset);
    }

    public void usingOffset(Integer offset) {
	offsetsInUse.add(offset);
    }

    public int firstUnusedOffset(int minimum) {
	for (int offset : randomOffsets) {
	    if (offset >= minimum && !isOffsetInUse(offset)) {
		return offset;
	    }
	}
	for (int offset : emergencyRandomOffsets) {
	    if (offset >= minimum && !isOffsetInUse(offset)) {
		return offset;
	    }
	}
	return 0;
    }
    
    public void onResize() {
	canvas.refresh();
    }

    public ExpresserCanvasSlave getCanvas() {
        return canvas;
    }

}

