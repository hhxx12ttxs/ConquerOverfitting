package net.tortuga.gui.widgets.layout;


import static net.tortuga.util.Align.*;
import net.tortuga.gui.widgets.Widget;

import com.porcupine.coord.Vec;
import com.porcupine.math.Calc;


/**
 * Vertical layout widget
 * 
 * @author OndĹej HruĹĄka (MightyPork)
 */
public class LayoutV extends LayoutBase {

	/** horizontal align */
	private int alignH = CENTER;
	private int alignV = CENTER;


	/**
	 * new Vertical layout widget
	 * 
	 * @param alignH horizontal align
	 * @param alignV vertical align
	 */
	public LayoutV(int alignH, int alignV) {
		this();
		setAlign(alignH, alignV);
	}


	/**
	 * new Vertical layout widget (center, center)
	 */
	public LayoutV() {
		setMargins(0, 0, 0, 0);
	}


	/**
	 * Set align (used if minSize is larger than needed for contents)
	 * 
	 * @param alignH horizontal align
	 * @param alignV vertical align
	 * @return this
	 */
	public LayoutV setAlign(int alignH, int alignV)
	{
		this.alignV = alignV;
		this.alignH = alignH;
		return this;
	}


	@Override
	public void calcChildSizes()
	{
		double lastMargin = 0;
		double totalSize = 0;
		double maxHorizontalSize = 0;

		// measure max width for alignment.
		for (Widget child : children) {
			child.calcChildSizes();
			maxHorizontalSize = Math.max(maxHorizontalSize, child.getSize().x + child.getMargins().left + child.getMargins().right);
		}

		maxHorizontalSize = Math.max(maxHorizontalSize, minSize.x);

		// generate rects
		boolean first = true;
		for (int i = children.size() - 1; i >= 0; i--) {
			Widget child = children.get(i);
			// add whats required by margins.
			if (!first) {
				totalSize += Calc.max(lastMargin, child.getMargins().bottom);
			}
			first = false;
			switch (alignH) {
				case LEFT:
					child.rect.add_ip(new Vec(child.getMargins().left, totalSize));
					break;
				case CENTER:
					child.rect.add_ip(new Vec((maxHorizontalSize - child.getSize().x) / 2, totalSize));
					break;
				case RIGHT:
					child.rect.add_ip(new Vec((maxHorizontalSize - child.getSize().x - child.getMargins().right), totalSize));
			}

			totalSize += child.getSize().y;
			lastMargin = child.getMargins().top;
		}

		if (Math.round(totalSize) % 2 == 1) {
			totalSize += 1;
		}

		if (Math.round(maxHorizontalSize) % 2 == 1) {
			maxHorizontalSize += 1;
		}

		this.rect.setTo(0, 0, (int) Math.round(Math.max(minSize.x, maxHorizontalSize)), (int) Math.round(Math.max(minSize.y, totalSize)));

		if (minSize.y > totalSize) {
			switch (alignV) {
				case TOP:
					for (Widget child : children) {
						child.getRect().add_ip(0, minSize.y - totalSize);
					}
					break;

				case CENTER:
					for (Widget child : children) {
						child.getRect().add_ip(0, (minSize.y - totalSize) / 2);
					}
					break;

				case BOTTOM:
					break;
			}
		}

		if (minSize.x > maxHorizontalSize) {
			switch (alignH) {
				case RIGHT:
					for (Widget child : children) {
						child.getRect().add_ip(minSize.x - maxHorizontalSize, 0);
					}
					break;

				case CENTER:
					for (Widget child : children) {
						child.getRect().add_ip((minSize.x - maxHorizontalSize) / 2, 0);
					}
					break;

				case LEFT:
					break;
			}
		}
	}

}

