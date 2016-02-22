package uk.ac.lkl.expresser.client;

import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Ken Kahn
 * 
 * This implements the palette that is the source of fresh tiles
 *
 */
public class TilePalette extends Palette {

    public TilePalette(PickupDragController dragController) {
      super(dragController);
      this.setSpacing(4);
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
	  ShapeView shape = (ShapeView) widget;
	  Widget clone = (Widget) shape.copy();
	  dragController.makeDraggable(clone);
	  insert(clone, index);
      }
      return super.remove(widget);
    }
}

