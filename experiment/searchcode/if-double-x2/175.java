package items;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

public class ConnItem extends Item {

	protected Item startItem = null;

	protected Item endItem = null;

	/**
	 * @return the endItem
	 */
	public Item getEndItem() {
		return endItem;
	}

	/**
	 * @param endItem
	 *            the endItem to set
	 */
	public void setEndItem(Item endItem) {
		this.endItem = endItem;
	}

	/**
	 * @return the startItem
	 */
	public Item getStartItem() {
		return startItem;
	}

	/**
	 * @param startItem
	 *            the startItem to set
	 */
	public void setStartItem(Item startItem) {
		this.startItem = startItem;
	}

	@Override
	public void paint(Graphics g) {
		
		Graphics2D g2 = (Graphics2D) g;
		if (isSelected()) {
			g2.setColor(selectedColor);
		} else {
			g.setColor(itemColor);
		}
		g2.setStroke(new BasicStroke(2));

		int y1 = startItem.getY();
		int x1 = startItem.getX();
		
		int x2 = endItem.getX();
		int y2 = endItem.getY();
				
		line1 = new Line2D.Double(x1 + 0.0, y1 + 0.0, x2 + 0.0, y2 + 0.0);


		
		
		drawArrow(g2,x1, y1,x2, y2);

		
		
		g2.draw(line1);
		
		
		g2.setColor(textColor);
		g2.drawString(getWeight() + "", x1 + (x2 - x1)/4 + 10, y1 + (y2 - y1)/4 + 15);
	}

	@Override
	public boolean contains(int x2, int y2) {
		return line1.getBounds().contains(
				new Point2D.Double(x2 + 0.0, y2 + 0.0));
				
	}
	
	private final int ARR_SIZE = 15;
	
	void drawArrow(Graphics g1, int x1, int y1, int x2, int y2) {
        Graphics2D g = (Graphics2D) g1.create();

        double dx = x2 - x1, dy = y2 - y1;
        double angle = Math.atan2(dy, dx);
        int len = (int) Math.sqrt(dx*dx + dy*dy);
        AffineTransform at = AffineTransform.getTranslateInstance(x1, y1);
        at.concatenate(AffineTransform.getRotateInstance(angle));
        g.setTransform(at);

        // Draw horizontal arrow starting in (0, 0)
        g.drawLine(0, 0, (int) len, 0);
        g.fillPolygon(new int[] {len, len-2*ARR_SIZE, len-2*ARR_SIZE},
                      new int[] {0, -ARR_SIZE, ARR_SIZE}, 3);
    }

	

	public String getType() {
		return Item.CONN_ITEM;
	}

	Line2D line1 = null;
	
}

