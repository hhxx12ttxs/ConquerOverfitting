package uk.ac.lkl.client;

import java.util.ArrayList;

import uk.ac.lkl.migen.system.expresser.model.AvailableTileColors;
import uk.ac.lkl.migen.system.expresser.model.ModelColor;
import uk.ac.lkl.migen.system.expresser.ui.uievent.TileCreationEvent;
import uk.ac.lkl.migen.system.expresser.ui.uievent.UIEventManager;
import uk.ac.lkl.migen.system.util.gwt.UUID;

import com.google.gwt.user.client.ui.Widget;

/**
 * @author Ken Kahn
 * 
 * This implements the palette that is the source of fresh tiles
 *
 */
public class TilePalette extends Palette {

    public TilePalette(PickupDragControllerEnhanced dragController) {
	super(dragController);
	setSpacing(4);
	setStylePrimaryName("expresser-tile-palette");
    }

    /**
     * Removed widgets that are instances of {@link TileView} are immediately replaced with a
     * cloned copy of the original. Also changes the dragged tile to become "uncolored".
     * 
     * @param widget the widget to remove
     * @return true if a widget was removed
     */
    @Override
    public boolean remove(Widget widget) {
      int index = getWidgetIndex(widget);
      if (index != -1 && widget instanceof ShapeView) {
	  TileView tile = (TileView) widget;
	  Widget clone = tile.copy();
	  dragController.makeDraggable(clone);
	  insert(clone, index);
	  tile.setTitle(Expresser.messagesBundle.ClickToOpenAMenuOfOperations());
      }
      return super.remove(widget);
    }

    public void fillWithTiles(int gridSize) {
	// can't use clear since palette overrides remove
	// copy the collection since children changes as they are removed
	ArrayList<Widget> children = new ArrayList<Widget>();
	for (Widget widget : getChildren()) {
	    children.add(widget);
	}
	for (Widget widget : children) {
	    super.remove(widget);
	}
	ModelColor[] colors = AvailableTileColors.getDefaultPositiveColors();
	for (ModelColor color : colors) {
	    TileView tile = new TileView(color, gridSize);
	    tile.setTitle(Expresser.messagesBundle.DragToCreateANewTile().replace("***color***", color.getName()));
	    add(tile);
	}
	
    }
}

