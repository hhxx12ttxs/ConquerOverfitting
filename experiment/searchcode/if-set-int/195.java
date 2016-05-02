/**
 * 
 */
package minime.ui.menu;

import javax.microedition.lcdui.Graphics;

import minime.Composite;
import minime.Drawable;
import minime.Portability;
import minime.core.BoxLayout;
import minime.core.CanSelect;
import minime.core.Dimension;
import minime.core.painter.BackgroundPainter;
import minime.core.painter.SingleColorRoundedPainter;

/**
 * 
 */
public abstract class MenuItem extends Drawable implements CanSelect {
	
	public MenuItem() {
		 bgPainter = new SingleColorRoundedPainter(true, Portability.TRANSPARENT);
		 framePainter = new SingleColorRoundedPainter(false, Portability.C_GREY);
		 setFramed(false);
	}

	/**
	 * Set drawables to MenuItem. Classes that extend MenuItem should implement
	 * this method to set their drawables that are put in their MenuItem.
	 * 
	 * @param drawables
	 */
	abstract public void setContent(Drawable[] drawables);

	/**
	 * Set the layout of this MenuItem
	 * @param newLO
	 */
	public void setItemLayout(BoxLayout newLO) {
		this.itemLayout = newLO;
	}

	/**
	 * (non-Javadoc)
	 * Used by Menu to set its MenuItem's width during the creation of Menu
	 * @param w the item's width
	 */
	final void setItemWidth(int w) {
		itemWidth = w;
		setWidth(w);
	}

	/**
	 * (non-Javadoc)
	 * Used by Menu to set its MenuItem's size during the creation of Menu
	 * @param w the item's width
	 * @Param h the item's height
	 */
	final void setItemSize(int w, int h) {
		itemWidth = w;
		itemHeight = h;
		setSize(new Dimension(itemWidth, itemHeight));
	}

	/** Methods that Implement CanSelect */
	public boolean isInitSelected() {
		return initSelected;
	}

	public boolean isSelected() {
		return selected;
	}

	public void renderImplSelected(Graphics gc) {
		if (itemLayout == null) 
			return;
		
		// paint background
//		if (bgPainter != null) {
//			((ColoredPainter) bgPainter).setColor((selected)? selectedBgColor : unselectedBgColor);
//			bgPainter.paint(gc, 1, 1, itemLayout.getTarget().getWidth() - 2, itemLayout.getTarget().getHeight() - 2);
//		}
		
		// render content
		renderImpl(gc);
		
		// render frame
//		if (framePainter != null) {
//			for (int i = 0; i < frameThickness; i++) {
//				framePainter.paint(gc, i, i, itemLayout.getTarget().getWidth() - 2 - 2*i,
//						itemLayout.getTarget().getHeight() - 2 - 2*i);
////				framePainter.paint(gc, 1 - i, 1 - i, itemLayout.getTarget().getWidth() - 2 + 2*i,
////						itemLayout.getTarget().getHeight() - 2 + 2*i);
//			}
//		}
	}

	public void setInitSeleted(boolean initSelect) {
		this.initSelected = initSelect;
	}

	public void setSelected(boolean select) {
		setNeedLayout();
		selected = select;
//		((ColoredPainter) bgPainter).setColor((selected)? selectedBgColor : unselectedBgColor);
		setFramed(selected);
//		TODO: should change the bgPainter color here
		
	}

	public void layoutImp() {
		Composite cmp = itemLayout.getTarget();
		if (cmp != null) {
			cmp.setWidth(itemWidth);
			cmp.setHeight(itemHeight);
			cmp.layout();
		}
	}

	/** Method that Implements Drawable */
	public void renderImpl(Graphics gc) {
		itemLayout.getTarget().render(gc);
	}

	public boolean hasComposite() {
		return true;
	}

	/**
	 * Check if the menu is checked.
	 * @return true if checked
	 */
	public boolean isChecked() {
		return isChecked;
	}

	/**
	 * Set the checked status of this MenuItem
	 * @param isChecked true if it is checked
	 */
	public void setChecked(boolean isChecked) {
		this.isChecked = isChecked;
	}

	/**
	 * Set the background painter of this item. If set to null, no background 
	 * will be painted
	 * 
	 * @param painter
	 */
	public void setBgPainter(BackgroundPainter painter) {
		bgPainter = painter;
	}

	/**
	 * Set the background painter of this item. If set to null, no frame 
	 * will be painted
	 * 
	 * @param painter
	 */
//	public void setFramePainter(BasicFramePainter painter) {
//		framePainter = painter;
//	}
	
	public void setSelectedBackgroundColor(int color) {
		selectedBgColor = color;
	}
	
	public void setUnselectedBackgroundColor(int color) {
		unselectedBgColor = color;
	}
	
//	public void setFrameColor(int color) {
//		frameColor = color;
//		framePainter.setColor(frameColor);
//	}
//	
//	public void setFrameThickness(int thick) {
//		frameThickness = thick;
//	}
//	
	private int selectedBgColor = Portability.TRANSPARENT;
	private int unselectedBgColor = Portability.TRANSPARENT;
//	private int frameColor = Portability.C_BLACK;
//	private int frameThickness = 2;

	private boolean isChecked;
	private boolean selected;
	private boolean initSelected;
	protected int itemWidth;
	protected int itemHeight;
	
	/** background painter, default as filled rounded rectangle */
	//TODO: should use the Drawable's background painter
//	protected ColoredPainter bgPainter;
	
	/** frame painter, default color as grey */
//	protected ColoredPainter framePainter;
	protected BoxLayout itemLayout;

}

