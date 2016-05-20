package uk.ac.lkl.migen.system.expresser.ui.view.shape.block;

import java.awt.*;

import java.awt.geom.*;

import uk.ac.lkl.common.util.value.Number;

import uk.ac.lkl.migen.system.expresser.model.ModelColor;
import uk.ac.lkl.migen.system.expresser.model.shape.block.BasicShape;
import uk.ac.lkl.migen.system.expresser.model.shape.block.BlockShape;

import uk.ac.lkl.migen.system.expresser.ui.ObjectSetCanvas;
import uk.ac.lkl.migen.system.expresser.ui.ObjectSetView;
import uk.ac.lkl.migen.system.expresser.ui.view.AbstractLocatedObjectView;

public class BasicShapeView extends
	AbstractLocatedObjectView {
    
    protected static Color crossColor = new Color(166, 20, 177); // purplish
       
    public BasicShapeView(ObjectSetCanvas view) {
	this(null, view);
    }

    public BasicShapeView(BasicShape shape, ObjectSetCanvas view) {
	super(shape, view);
    }

    // todo: area should be cached
    @Override
    public Area getStrictBoundingArea(BlockShape object) {
	return new Area(this.getViewBounds(object));
    }

    @Override
    public boolean strictlyContains(BlockShape object, int x, int y) {
	// similar to contains()
	BasicShape shape = (BasicShape) object;
	Number xValue = shape.getAttributeValue(BlockShape.X);
	Number yValue = shape.getAttributeValue(BlockShape.Y);
	Number widthValue = shape.getAttributeValue(BlockShape.WIDTH);
	Number heightValue = shape.getAttributeValue(BlockShape.HEIGHT);
	int gridSize = getGridSize();
	int viewX = xValue.intValue() * gridSize;
	int viewY = yValue.intValue() * gridSize;
	int viewWidth = widthValue.intValue() * gridSize;
	int viewHeight = heightValue.intValue() * gridSize;
	Rectangle rectangle = new Rectangle(viewX, viewY, viewWidth, viewHeight);
	return rectangle.contains(x, y);
    }
    
    @Override
    public void paintObject(ObjectSetView.ObjectPainter objectPainter,
	                    BlockShape object, 
	                    Graphics2D g2) {
//	BasicShape shape = (BasicShape) object;
//	IntegerValue x = shape.getAttributeValue(BlockShape.X);
//	IntegerValue y = shape.getAttributeValue(BlockShape.Y);
//	int xInt = x.getInt();
//	int yInt = y.getInt();
//	AllocatedColor color = null;
//	if (MiGenConfiguration.isDrawCrossForAnyOverlap()) {
//	    if (getCanvas().overlapAt(xInt, yInt)) {
//		// if drawing crosses and there is an overlap here then
//		// color doesn't matter 
//		// null color used to indicate a cross is needed
//		color = null;
//	    } else {
//		// calculate a colour that depends upon 
//		//  what other tiles are on this grid location
//		color = getCanvas().getColorOfGrid(xInt, yInt);
//	    }
//	} else if (!MiGenConfiguration.isNoColourAllocation()) {
//	    // if colour allocations is turned off then 
//	    // the color of this tile should be used
//	    color = getCanvas().getColorOfGrid(xInt, yInt);
//	}
//	if (color != null && color.equals(AllocatedColor.WHITE)) {
//	    // WHITE is used to indicate that positive and negative colours
//	    // exactly cancel so nothing needs to be drawn
//	    return; 
//	}
//	ModelColor shapeColor = shape.getColor();
//	ExpresserModel model = getCanvas().getModel();
//	boolean slaved = model.isSlaved();
//	// the following has the side effect of logging changes
//	// in the correctness of an allocation
//	boolean anyNonZeroRemainingUsage = 
//	    shape.anyNonZeroRemainingUsage(shapeColor, slaved, true, true);
//	if (color == null && 
//	    (!MiGenConfiguration.isDrawCrossForMultipleColors() && 
//	     !MiGenConfiguration.isDrawCrossForAnyOverlap()) ||
//	     MiGenConfiguration.isNoColourAllocation()) {
//	    // can't combine colours so fall back on alpha blending  
//	    color = new AllocatedColor(shapeColor.getRed(), 
//		                       shapeColor.getGreen(), 
//		                       shapeColor.getBlue(), 
//		                       MiGenConfiguration.getOpacity(), 
//		                       shapeColor.isNegative(),
//		                       shapeColor.getName(),
//		                       !anyNonZeroRemainingUsage); 
//	}
//	IntegerValue width = shape.getAttributeValue(BlockShape.WIDTH);
//	IntegerValue height = shape.getAttributeValue(BlockShape.HEIGHT);
//	int gridSize = getGridSize();
//	int viewX = xInt * gridSize;
//	int viewY = yInt * gridSize;
//	int viewWidth = width.getInt() * gridSize;
//	int viewHeight = height.getInt() * gridSize;
//	paintTileInternal(g2, shape, color, shapeColor, model, slaved,
//		gridSize, viewX, viewY, viewWidth, viewHeight);
    }

    /**
     * @param g2
     * @param gridSize
     * @param viewX
     * @param viewY
     * @param viewWidth
     * @param viewHeight
     * @param grayWithOpacity
     * @param modelColor
     */
    public static void paintUncoloredTile(Graphics2D g2, int gridSize, int viewX, int viewY, int viewWidth, int viewHeight,
	                                  Color grayWithOpacity, ModelColor modelColor) {
	Color innerColor;
	Color outerColor;
	float strokeWidth;
	// Uncolored negative tiles should be a draker gray but otherwise identical to positive ones
//	if (modelColor.isNegative()) {
//	    innerColor = new Color(modelColor.getRGB());
//	    outerColor = grayWithOpacity;
//	    strokeWidth = gridSize * 0.4f;
//	    int strokeWidthAsInt = (int) strokeWidth;
//	    int extraWidth = modelColor.isNegative() ? strokeWidthAsInt : 0;
//	    int doubleStrokeWidth = strokeWidthAsInt*2;
//	    g2.setStroke(new BasicStroke(strokeWidth));
//	    g2.setColor(outerColor);
//	    g2.fillRect(viewX, viewY, 
//		        viewWidth, viewHeight);
//	    g2.setColor(innerColor); // no need for modelColor.grayColor() now that we have colored borders
//	    g2.fillRect(viewX+strokeWidthAsInt-extraWidth/2, viewY+strokeWidthAsInt, 
//		        viewWidth+extraWidth-doubleStrokeWidth, viewHeight-doubleStrokeWidth);
//	} else {
	    innerColor = grayWithOpacity;
	    outerColor = new Color(modelColor.getRGB());
	    strokeWidth = gridSize * 0.1f;
	    g2.setColor(innerColor); // no need for modelColor.grayColor() now that we have colored borders
	    int strokeWidthAsInt = (int) strokeWidth;
	    int strokeOffset = strokeWidthAsInt/2;
	    g2.fillRect(viewX+strokeWidthAsInt, viewY+strokeWidthAsInt, 
		        viewWidth-strokeWidthAsInt*2, viewHeight-strokeWidthAsInt*2);
	    g2.setColor(outerColor);
	    g2.setStroke(new BasicStroke(strokeWidth));
	    g2.drawRect(viewX+strokeOffset, viewY+strokeOffset, 
		        viewWidth-strokeWidthAsInt, viewHeight-strokeWidthAsInt);
//	}
    }

    @Override
    protected void processViewChange() {
    }

    @Override
    protected void processAttributeChange() {
    }

}

