package uk.ac.lkl.migen.mockup.polydials.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

import uk.ac.lkl.common.util.event.UpdateEvent;
import uk.ac.lkl.common.util.event.UpdateListener;
import uk.ac.lkl.migen.mockup.polydials.model.CounterGroup;
import uk.ac.lkl.migen.mockup.polydials.model.ModuloCounter;

public class CounterGroupButton extends SelectableComponent {

    private static Color SELECTED_COLOR = Color.YELLOW;

    private PolyDialsCanvas dialCanvas;

    private CounterGroup counterGroup;

    private boolean active = false;

    private Border upBorder =
	    BorderFactory.createCompoundBorder(BorderFactory
		    .createBevelBorder(BevelBorder.RAISED), BorderFactory
		    .createLineBorder(Color.BLACK));

    private Border downBorder =
	    BorderFactory.createCompoundBorder(BorderFactory
		    .createBevelBorder(BevelBorder.LOWERED), BorderFactory
		    .createLineBorder(Color.BLACK));

    public CounterGroupButton(PolyDialsCanvas dialCanvas,
	    CounterGroup counterGroup) {
	this.dialCanvas = dialCanvas;
	this.counterGroup = counterGroup;
	addMouseListeners();
	setBorder(upBorder);
	dialCanvas.addUpdateListener(new UpdateListener<PolyDialsCanvas>() {

	    public void objectUpdated(UpdateEvent<PolyDialsCanvas> e) {
		repaint();
	    }
	});
    }

    public CounterGroup getCounterGroup() {
	return counterGroup;
    }

    public void delete() {
	dialCanvas.getModel().removeCounterGroup(counterGroup);
    }

    private void addMouseListeners() {
	addMouseListener(new MouseAdapter() {

	    public void mousePressed(MouseEvent e) {
		active = true;
		// should cache this
		setBorder(downBorder);
	    }

	    public void mouseReleased(MouseEvent e) {
		// temporary measure for now
		if (!active)
		    return;

		if ((e.getModifiers() & ActionEvent.SHIFT_MASK) > 0)
		    counterGroup.decrement();
		else
		    counterGroup.increment();
		active = false;

		// repeated work
		setBorder(upBorder);
	    }
	});
	addMouseMotionListener(new MouseMotionAdapter() {

	    public void mouseDragged(MouseEvent e) {
		if (!contains(e.getPoint())) {
		    // repeated work - should be listening to model
		    setBorder(upBorder);
		    active = false;
		}
	    }
	});
    }

    public Dimension getPreferredSize() {
	return new Dimension(50, 50);
    }

    public void paintComponent(Graphics g) {
	((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
		RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		RenderingHints.VALUE_ANTIALIAS_ON);
	super.paintComponent(g);
	paintMiniatureDials((Graphics2D) g.create());
    }

    private void paintMiniatureDials(Graphics2D g2) {
	// Insets insets = getInsets();

	Dimension buttonSize = getSize();
	Rectangle usedSpace = dialCanvas.getDialsUsedSpace();

	int usedWidth = usedSpace.width;
	int usedHeight = usedSpace.height;

	int buttonWidth = buttonSize.width - 10;
	int buttonHeight = buttonSize.height - 10;

	double widthMultiple = (double) usedWidth / buttonWidth;
	double heightMultiple = (double) usedHeight / buttonHeight;

	double multiple = Math.max(widthMultiple, heightMultiple);

	// use original button size not '-10' versions
	int xIndent = (int) (buttonSize.width - usedWidth / multiple) / 2;
	int yIndent = (int) (buttonSize.height - usedHeight / multiple) / 2;

	// hack to simulate press better
	// if (active) {
	// xIndent += 2;
	// yIndent += 2;
	// }

	if (isSelected()) {
	    // for some reason using setBackground doesn't work in way expected
	    // so do selected status here
	    g2.setColor(SELECTED_COLOR);
	    g2.fillRect(0, 0, buttonSize.width, buttonSize.height);
	    // g2.fillRect(xIndent, yIndent, (int) (usedWidth / multiple),
	    // (int) (usedHeight / multiple));
	}

	// // could do with compound border perhaps
	// // hack here
	// g2.setColor(Color.BLACK);
	// if (active)
	// g2.drawRect(6, 6, buttonSize.width - 8, buttonSize.height - 8);
	// else
	// g2.drawRect(4, 4, buttonSize.width - 8, buttonSize.height - 8);
	// // g2.drawRect(xIndent, yIndent, (int) (usedWidth / multiple),
	// // (int) (usedHeight / multiple));

	Collection<ModuloCounter> groupCounters = counterGroup.getCounters();

	Collection<Dial> dials = dialCanvas.getDials();
	for (Dial dial : dials) {
	    Point dialLocation = dial.getLocation();
	    // coords relative to used space
	    int x = (int) ((dialLocation.x - usedSpace.x) / multiple);
	    int y = (int) ((dialLocation.y - usedSpace.y) / multiple);

	    int dialSize = dial.getDialSize();
	    int scaledDialSize = (int) (dialSize / multiple);

	    ModuloCounter counter = dial.getCounter();
	    if (groupCounters.contains(counter)) {
		Color fillColor = dial.getFillColor();
		g2.setColor(fillColor);
		g2.fillOval(xIndent + x, yIndent + y, scaledDialSize,
			scaledDialSize);
		g2.setColor(Color.BLACK);
		g2.drawOval(xIndent + x, yIndent + y, scaledDialSize,
			scaledDialSize);
	    } else {
		g2.setColor(Color.BLACK);
		g2.drawOval(xIndent + x, yIndent + y, scaledDialSize,
			scaledDialSize);
	    }

	}
    }
}

