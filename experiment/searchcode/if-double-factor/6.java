package uk.ac.lkl.migen.mockup.shapebuilder.ui;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;

import uk.ac.lkl.common.ui.NotifyingElement;
import uk.ac.lkl.common.ui.NotifyingPoint;

import uk.ac.lkl.common.util.event.UpdateEvent;
import uk.ac.lkl.common.util.event.UpdateListener;

import uk.ac.lkl.common.util.value.DoubleValue;
import uk.ac.lkl.migen.mockup.shapebuilder.model.shape.Expressed;
import uk.ac.lkl.migen.mockup.shapebuilder.model.shape.ExpressedShape;
import uk.ac.lkl.migen.mockup.shapebuilder.ui.drag.*;

// should be used by LineExpression as well
public abstract class AbstractThumbnail extends
	DraggableComponent<AbstractThumbnail> {

    private ShapePlotter shapePlotter;

    private Expressed expressed;

    public AbstractThumbnail(Expressed expressed, ShapePlotter shapePlotter) {
	super(AbstractThumbnail.class);
	this.expressed = expressed;
	this.shapePlotter = shapePlotter;

	Toolkit toolkit = Toolkit.getDefaultToolkit();
	Dimension screenSize = toolkit.getScreenSize();
	int thumbNailWidth = screenSize.width / 12;
	int thumbNailHeight = screenSize.height / 12;
	int side = Math.min(thumbNailWidth, thumbNailHeight);

	setPreferredSize(new Dimension(side, side));
	expressed.addUpdateListener(new UpdateListener<NotifyingElement>() {

	    public void objectUpdated(UpdateEvent<NotifyingElement> e) {
		// todo: recalculate thumbnail and then repaint
		repaint();
	    }
	});
	addMouseListener();
    }

    public Expressed getExpressed() {
	return expressed;
    }

    private void addMouseListener() {
	addMouseListener(new MouseAdapter() {

	    public void mouseEntered(MouseEvent e) {
		processMouseEntered(e);
	    }

	    public void mouseExited(MouseEvent e) {
		processMouseExited(e);
	    }
	});
    }

    /**
     * Get the shape represented by this thumbnail.
     * 
     * If is a group, uses a representative.
     * 
     * @return
     * 
     */
    public abstract ExpressedShape getShape();

    protected abstract void processMouseEntered(MouseEvent e);

    protected abstract void processMouseExited(MouseEvent e);

    public ShapePlotter getShapePlotter() {
	return shapePlotter;
    }

    public void paintComponent(Graphics g) {
	super.paintComponent(g);
	paintThumbnail((Graphics2D) g.create());
	paintAnnotations((Graphics2D) g.create());
    }

    public void paintAnnotations(Graphics2D g2) {
	// do nothing by default
    }

    // hack messy. Should be in subclass eventually
    private void paintThumbnail(Graphics2D g2) {
	ExpressedShape shape = getShape();
	if (shape == null)
	    return;

	Dimension size = getSize();
	Rectangle2D.Double bounds = shape.getBounds2D();
	// scale for display in the space available
	double shapeWidth = bounds.getWidth();
	double shapeHeight = bounds.getHeight();
	double widthFactor = (size.width - 10) / shapeWidth;
	double heightFactor = (size.height - 10) / shapeHeight;
	double factor = Math.min(widthFactor, heightFactor);

	// hack to use gridsize
	factor = Math.min(factor, 20);

	g2.scale(factor, factor);

	double widthUsed = shapeWidth * factor;
	double heightUsed = shapeHeight * factor;
	double xOffset = (size.width - widthUsed) / 2;
	double yOffset = (size.height - heightUsed) / 2;

	g2.translate(-bounds.x, -bounds.y);
	g2.translate(xOffset / (float) factor, yOffset / (float) factor);
	g2.setStroke(new BasicStroke(1.0f / (float) factor,
		BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
	getShape().paint(g2, 1);
    }

    public abstract ExpressedShape createShape(NotifyingPoint<DoubleValue> location);

}

