package uk.ac.lkl.common.ui;

import java.awt.Dimension;
import java.awt.Font;

import javax.swing.*;

public class ScalingTextLabel extends JLabel {

    public ScalingTextLabel(String text) {
	super(text);
	scaleFont();
    }

    public float scaleFont() {
	Dimension preferredSize = getPreferredSize();
	Dimension currentSize = getSize();

	if (preferredSize.width == 0 || preferredSize.height == 0)
	    return 0;

	if (currentSize.width == 0 || currentSize.height == 0)
	    return 0;

	double xFactor = currentSize.getWidth() / preferredSize.getWidth();
	double yFactor = currentSize.getHeight() / preferredSize.getHeight();

	double factor = Math.min(xFactor, yFactor);

	Font font = getFont();
	float fontSize = font.getSize2D();

	float newFontSize = fontSize * (float) factor;

	setFontSize(newFontSize);

	return newFontSize;
    }

    public void setFontSize(float fontSize) {
	// need this check otherwise font size of zero messes up scaling
	if (fontSize <= 0)
	    return;

	Font newFont = getFont().deriveFont(fontSize);
	setFont(newFont);
    }

}

