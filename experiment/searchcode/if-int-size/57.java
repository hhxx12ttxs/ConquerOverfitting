/**
 * 
 */
package uk.ac.lkl.client;

import java.util.ArrayList;
import java.util.List;

import uk.ac.lkl.migen.system.expresser.model.ModelColor;

import com.google.gwt.user.client.ui.AbsolutePanel;

/**
 * Implements icons of stacks of tiles
 * 
 * @author Ken Kahn
 *
 */
public class TileStackIcon extends AbsolutePanel {
      
    private ModelColor firstColor;

    public TileStackIcon(List<ModelColor> colors, int size) {
	super();
	if (colors.size() == 1) {
	    colors = repeatColor(colors.get(0), 3);
	}
	firstColor = colors.get(0);
	final int distanceBetweenTiles = Math.max(2, (int) (Math.round(size*0.4)/colors.size()));
	int top = distanceBetweenTiles-1;
	int left = distanceBetweenTiles*colors.size()-1;
	for (ModelColor color : colors) {
	    TileIcon tileIcon = new TileIcon(color);
	    tileIcon.setPixelSize(size/2, size/2);
	    add(tileIcon, left, top);
	    top += distanceBetweenTiles;
	    left -= distanceBetweenTiles;
	}
	setPixelSize(size, size);
	setStylePrimaryName("expresser-tile-stack-icon");
    }
    
    public TileStackIcon(ModelColor color, int repeatCount, int size) {
	this(repeatColor(color, repeatCount), size);
    }

    private static List<ModelColor> repeatColor(ModelColor color, int repeatCount) {
	ArrayList<ModelColor> colors = new ArrayList<ModelColor>();
	for (int i = 0; i < repeatCount; i++) {
	    colors.add(color);
	}
	return colors;
    }

    public ModelColor getFirstColor() {
        return firstColor;
    }

}

