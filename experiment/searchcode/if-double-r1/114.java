package vehicles;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import javax.swing.JComponent;

/**
 * This component displays a sailboat that can be moved.
 */
public class Sailboat extends JComponent {

	private BasicStroke bs;
	private Ellipse2D face;
	private int xpos;
	private int ypos;
	private boolean flag;	

	/**
	 * Constructs a sailboat with given top-left corner.
	 * 
	 * @param x
	 *            the x-coordinate of the top-left corner
	 * @param y
	 *            the y coordinate of the top-left corner
	 */	
	public Sailboat(int x, int y) {
		xpos = x;
		ypos = y;
		flag = true;
	}

	/**
     * Draws the sailboat.
     * @param g the graphics context
     */		
	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		setBackground(Color.white); // set background
		bs = new BasicStroke(2, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND); // set line bold		
		g2.setStroke(bs);
		// The rectangle that the paint method draws

		Point2D.Double r1 = new Point2D.Double(xpos, ypos );
		Point2D.Double r2 = new Point2D.Double(xpos + 10, ypos + 20);
		Point2D.Double r3 = new Point2D.Double(xpos + 50, ypos + 20);
		Point2D.Double r4 = new Point2D.Double(xpos + 60, ypos );
		Point2D.Double r5 = new Point2D.Double(xpos + 20, ypos);
		Point2D.Double r6 = new Point2D.Double(xpos + 40, ypos );	
		Point2D.Double r7 = new Point2D.Double(xpos + 40, ypos - 40);
		if(!flag){
			r7 = new Point2D.Double(xpos + 20, ypos - 40);		
			}		
		Line2D.Double frontW = new Line2D.Double(r1, r2);
		Line2D.Double bottom = new Line2D.Double(r2, r3);
		Line2D.Double rearW = new Line2D.Double(r3, r4);
		Line2D.Double top = new Line2D.Double(r1, r4);
		Line2D.Double sail1 = new Line2D.Double(r5, r7);
		Line2D.Double sail2 = new Line2D.Double(r6, r7);
		g2.draw(frontW);
		g2.draw(rearW);
		g2.draw(bottom);
		g2.draw(top);	
		g2.draw(sail1);
		g2.draw(sail2);		
	}

	/**
	 * Moves the components by a given amount.
	 * 
	 * @param dx
	 *            the amount to move in the x-direction
	 */
	public void moveBy(int dx) {
		xpos = xpos + dx;
		repaint();
	}
	
	/**
	 * Get the x-coordinate of the top-left corner.
	 * 
	 * @return xpos
	 *            the x-coordinate of the top-left corner
	 */	
	public int getXpos() {
		return xpos;
	}	

	public void setFlag(boolean flag) {
		this.flag = flag;
	}	
}
