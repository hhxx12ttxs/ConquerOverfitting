package uk.ac.lkl.migen.mockup.shapebuilder.ui.view;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.*;

import uk.ac.lkl.common.ui.NotifyingElement;
import uk.ac.lkl.common.ui.NotifyingLine;

import uk.ac.lkl.common.util.event.UpdateEvent;
import uk.ac.lkl.common.util.event.UpdateListener;

import uk.ac.lkl.common.util.value.DoubleValue;

import uk.ac.lkl.migen.mockup.shapebuilder.model.shape.*;

import uk.ac.lkl.migen.mockup.shapebuilder.ui.ShapePlotter;

public class LineExpressionView extends SimpleExpressionView {

    private class LineVariableViewListener extends MouseAdapter {

	@SuppressWarnings("unused")
	public LineExpressionView lineVariableView;

	public LineVariableViewListener(LineExpressionView lineVariableView) {
	    this.lineVariableView = lineVariableView;
	}

	public void mouseEntered(MouseEvent e) {

	    LineExpression lineExpression = (LineExpression) getExpression();
	    // hack to prevent highlighting when shape has been deleted
	    ExpressedShape shape = lineExpression.getShape();
	    if (!getShapePlotter().getModel().containsShape(shape))
		return;

	    getShapePlotter().setHighlightedLine(lineExpression.getLine());
	}

	public void mouseExited(MouseEvent e) {
	    getShapePlotter().clearHighlightedLine();
	}
    };

    private int size = 50;

    public LineExpressionView(LineExpression lineExpression,
	    ShapePlotter shapePlotter) {
	super(lineExpression, shapePlotter);
	lineExpression.getLine().addUpdateListener(
		new UpdateListener<NotifyingLine<DoubleValue>>() {

		    public void objectUpdated(
			    UpdateEvent<NotifyingLine<DoubleValue>> e) {
			// eventually recalculate thumbnail and then repaint
			repaint();
		    }
		});
	ExpressedShape shape = lineExpression.getShape();
	shape.addUpdateListener(new UpdateListener<NotifyingElement>() {

	    public void objectUpdated(UpdateEvent<NotifyingElement> e) {
		// eventually recalculate thumbnail and then repaint
		repaint();
	    }
	});

	// make this dynamic
	setPreferredSize(new Dimension(size, size));
	setOpaque(false);
	createThumbnail();
	addMouseListener(this);
    }

    private void addMouseListener(LineExpressionView view) {
	view.addMouseListener(new LineVariableViewListener(view));
    }

    private void createThumbnail() {
	// todo: cache out work done in paint
    }

    public void paintComponent(Graphics g) {
	super.paintComponent(g);
	Graphics2D g2 = (Graphics2D) g.create();
	paintThumbnail(g2);
    }

    private void paintThumbnail(Graphics2D g2) {
	Dimension size = getSize();
	LineExpression lineExpression = (LineExpression) getExpression();
	NotifyingLine<DoubleValue> line = lineExpression.getLine();
	ExpressedShape shape = lineExpression.getShape();
	if (shape == null) {
	    g2.drawLine(0, 0, size.width, size.height);
	} else {
	    Rectangle2D.Double bounds = shape.getBounds2D();
	    // scale for display in the space available
	    double shapeWidth = bounds.getWidth();
	    double shapeHeight = bounds.getHeight();
	    double widthFactor = (size.width - 10) / shapeWidth;
	    double heightFactor = (size.height - 10) / shapeHeight;
	    double factor = Math.min(widthFactor, heightFactor);

	    // hack to use 20 (gridSize)
	    factor = Math.min(factor, 20);

	    g2.scale(factor, factor);

	    double widthUsed = shapeWidth * factor;
	    double heightUsed = shapeHeight * factor;
	    double xOffset = (size.width - widthUsed) / 2;
	    double yOffset = (size.height - heightUsed) / 2;

	    g2.translate(-bounds.x, -bounds.y);
	    g2.translate(xOffset / (float) factor, yOffset / (float) factor);

	    Graphics2D temp = (Graphics2D) g2.create();
	    temp.setColor(Color.GREEN);
	    temp.setStroke(new BasicStroke(8.0f / (float) factor,
		    BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
	    g2.setStroke(new BasicStroke(1.0f / (float) factor,
		    BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
	    temp.draw(line.getLine());
	    shape.paint(g2, 1);
	}
    }
}

