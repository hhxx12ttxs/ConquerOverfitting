/**
 * 
 */
package uk.ac.lkl.migen.system.expresser.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import uk.ac.lkl.migen.system.expresser.model.ExpresserModel;
import uk.ac.lkl.migen.system.expresser.model.ExpresserModelImpl;
import uk.ac.lkl.migen.system.expresser.model.event.AttributeChangeEvent;
import uk.ac.lkl.migen.system.expresser.model.shape.block.BasicShape;
import uk.ac.lkl.migen.system.expresser.model.shape.block.BlockShape;
import uk.ac.lkl.migen.system.expresser.model.shape.block.GroupShape;
import uk.ac.lkl.migen.system.expresser.model.shape.block.PatternShape;
import uk.ac.lkl.migen.system.expresser.ui.view.shape.block.BasicShapeThumbNailView;
import uk.ac.lkl.migen.system.expresser.ui.view.shape.block.GroupShapeView;
import uk.ac.lkl.migen.system.expresser.ui.view.shape.block.PatternShapeView;
import uk.ac.lkl.migen.system.expresser.ui.view.shape.block.UnresourcedBasicShapeView;

/**
 * @author Ken Kahn
 * For making a canvas suited for generating thumb nails
 *
 */
public class ThumbNailShapeCanvas extends ObjectSetCanvas {
    private BlockShape shape;
    private int maxWidth;
    private int maxHeight;
    // if true then adjust grid size to fit in the given dimensions
    private boolean adjustableGridSize = true;
    
    public ThumbNailShapeCanvas(ExpresserModel model, int maxWidth, int maxHeight, int gridSize) {
	super(model, gridSize);
	setViewClass(BasicShape.class, BasicShapeThumbNailView.class);
	setViewClass(PatternShape.class, PatternShapeView.class);
	setViewClass(GroupShape.class, GroupShapeView.class);
	setUnresourcedView(BasicShape.class, new UnresourcedBasicShapeView(this));
	this.maxWidth = maxWidth;
	this.maxHeight = maxHeight;
	setPreferredSize(new Dimension(maxWidth, maxHeight));
    }
    
    public ThumbNailShapeCanvas(ExpresserModel model, int maxWidth, int maxHeight) {
	this(model, maxWidth, maxHeight, 1);
    }
    
    public static ThumbNailShapeCanvas createThumbNail(BlockShape shape, int width, int height) {
	ExpresserModel model = createThumbNailModel();
	BlockShape shapeCopy = shape.createCopy();
	model.addObject(shapeCopy);
	ThumbNailShapeCanvas thumbNailBlockCanvas = new ThumbNailShapeCanvas(model, width, height);
	thumbNailBlockCanvas.setShape(shapeCopy);
	return thumbNailBlockCanvas;
    }

    public static ExpresserModel createThumbNailModel() {
	ExpresserModel model = new ExpresserModelImpl();
	model.setModelAsAGroup(null);
	model.setGlue(null);
	return model;
    }
    
    @Override
    public void paint(Graphics g) {
	Graphics2D g2 = (Graphics2D) g.create();
	paintThumbNailBackground(g2);
	if (adjustableGridSize) {
	    int shapeWidth = shape.getWidthValue().intValue();
	    int shapeHeight = shape.getHeightValue().intValue();
	    int gridSizeX = Math.max(1, maxWidth/shapeWidth);
	    int gridSizeY = Math.max(1, maxHeight/shapeHeight);
	    setGridSize(Math.min(gridSizeX, gridSizeY));
	}
	paintComponentInternal(g2);
    }

    protected void paintComponentInternal(Graphics2D g2) {
	ObjectPainter objectPainter = createObjectThumbNailPainter();
	objectPainter.paintObject(g2, null);
    }

    /**
     * Overriden by some subclasses
     * 
     * @param g2
     */
    protected void paintThumbNailBackground(Graphics2D g2) {
	// white background is important for transparency to look right
	g2.setColor(Color.WHITE);
	g2.fillRect(0, 0, maxWidth, maxHeight);
    }
    
    public BlockShape getShape() {
        return shape;
    }

    protected void setShape(BlockShape shape) {
        this.shape = shape;
    }
    
    // @Hack() -- annotations not working
    // This should be annotated as something that could be cleaner (after version 1.0)
    // Probably need to refactor ObjectSetCanvas
    
    @Override
    protected void processAttributeChanged(AttributeChangeEvent<BlockShape> e) {
	// do nothing
    }
    
    @Override
    protected boolean isPreferredSizeFixed() {
	return true;
    }

    public boolean isAdjustableGridSize() {
        return adjustableGridSize;
    }

    public void setAdjustableGridSize(boolean adjustableGridSize) {
        this.adjustableGridSize = adjustableGridSize;
    }
    
    public String getIdName() {
	return "ThumbNailShapeCanvas";
    }
    
    @Override
    protected void translateToThumbNailShape(Graphics2D g2) {
	// need to crop to the location of the shape in the thumb nail
	g2.translate(-shape.getX()*getGridSize(),
		     -shape.getY()*getGridSize());
    }
    
    @Override
    public void paintColoredTile(Graphics2D g2, int gridSize, int viewX, int viewY, Color color) {
	super.paintColoredTile(g2, gridSize, viewX, viewY, color);
	// a gray border to the tile
	g2.setColor(Color.GRAY);
	float strokeWidth = gridSize * 0.1f;
	int strokeWidthAsInt = (int) strokeWidth;
	int strokeOffset = strokeWidthAsInt/2;
	g2.setStroke(new BasicStroke(strokeWidth));
	g2.drawRect(viewX+strokeOffset, viewY+strokeOffset, 
		    gridSize-strokeWidthAsInt, gridSize-strokeWidthAsInt);
    }

}

