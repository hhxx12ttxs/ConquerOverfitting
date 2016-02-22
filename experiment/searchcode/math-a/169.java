/**
 * 
 */
package uk.ac.lkl.expresser.client;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Ken Kahn
 *
 */
public class CompoundExpressionPanelContents extends HorizontalPanel {
    
    /**
     * Removed widgets that are instances of {@link ExpressionPanel} are immediately replaced with a
     * cloned copy of the original. 
     * 
     * @param widget the widget to remove
     * @return true if a widget was removed
     */
    @Override
    public boolean remove(Widget widget) {
	int index = getWidgetIndex(widget);
	if (index != -1 && widget instanceof ExpressionPanel) {
	    ExpressionPanel dragee = (ExpressionPanel) widget;
	    ExpressionPanel copy = dragee.copy();
	    copy.setDraggable(true);
	    copy.setDropTarget(true);
	    insert(copy, index);
	}
	return super.remove(widget);
    }
    
    public boolean removeWithoutCopying(Widget widget) {
	return super.remove(widget);
    }
}

