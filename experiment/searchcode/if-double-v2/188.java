import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
// I worked with Joe Salisbury on this project
/**
 * A Sierpinski Gasket class to draw the gasket on a frame.
 * The gasket is a series of nested equilateral triangles.
 *
 * @author Matthew Boutell.
 *         Created Mar 15, 2007.
 */
public class Sierpinski extends JFrame {

	/** Size of the frame */
	static final Dimension FRAME_SIZE = new Dimension(700, 700);

	/** Space between the triangle and the edge of the frame. */
	static final int BUFFER = 50;
	
	/** Minimum edge length for terminating recursion. */
	static final int MIN_EDGE_LENGTH = 10;


	private static final int ABOVE = 1;

	/* For generality if desired later */
	private static final int BELOW = 2;

	/**
	 * Executes here...
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		new Sierpinski();
	}

	/**
	 * Creates the frame on which we'll draw.
	 *
	 */
	public Sierpinski() {
		System.out.println("Drawing");
		this.setSize(FRAME_SIZE);
		this.setTitle("Sierpinski Gasket");
		this.setLayout(null);
		this.setContentPane(new SierpinskiPanel());
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);
	}

	// An inner class; had references to the enclosing class.
	private class SierpinskiPanel extends JPanel {
		private Point2D.Double v1 = null;
		private Point2D.Double V1 = null;
		
		private Point2D.Double v2 = null;
		private Point2D.Double V2 = null;
		
		private Point2D.Double v3 = null;
		private Point2D.Double V3 = null;
		
		private boolean firstrun = true;
		int i = 1;
		
		/** Creates the panel with the initial triangle */
		public SierpinskiPanel() {
			this.setPreferredSize(FRAME_SIZE);
			this.v1 = new Point2D.Double(BUFFER, FRAME_SIZE.height - BUFFER);
			this.v2 = new Point2D.Double(FRAME_SIZE.width - BUFFER,
					FRAME_SIZE.height - BUFFER);
			this.v3 = findThirdVertexOfEquilTriangle(this.v1, this.v2,
					Sierpinski.ABOVE);
		}

		@Override
		public void paintComponent(Graphics graphics) {
			super.paintComponent(graphics);
			Graphics2D g = (Graphics2D) graphics;
			g.setStroke(new BasicStroke(0.5f));
			g.setColor(Color.BLACK);
			drawGasket(g, this.v1, this.v2, this.v3);
		}

		private void drawGasketHelper(Graphics2D g, Point2D.Double v1,
				Point2D.Double v2, Point2D.Double v3, int n) {
			if(n == 0) {
				fillTriangle(g, getMidpoint(v1,v2),getMidpoint(v2,v3),getMidpoint(v3,v1));
				drawTriangle(g, getMidpoint(v1, v2), getMidpoint(v1, v3), v1);
				drawTriangle(g, getMidpoint(v1, v2), getMidpoint(v2, v3), v2);
				drawTriangle(g, getMidpoint(v1, v3), getMidpoint(v2, v3), v3);
				return;
			}
			n--;
			drawTriangle(g,v1,v2,v3);
			fillTriangle(g, getMidpoint(v1,v2),getMidpoint(v2,v3),getMidpoint(v3,v1));
			drawGasketHelper(g, v1, getMidpoint(v1, v2), getMidpoint(v1, v3), n);
			drawGasketHelper(g, getMidpoint(v1, v2), v2, getMidpoint(v2, v3), n);
			drawGasketHelper(g, getMidpoint(v1, v3), getMidpoint(v2, v3), v3, n);
		}
		/**
		 * This method draws the gasket with vertices at v1, v2, v3 using the 
		 * given graphics object. Start by drawing a triangle from the points.
		 */
		private void drawGasket(Graphics2D g, Point2D.Double v1,
				Point2D.Double v2, Point2D.Double v3) {
				drawTriangle(g,v1,v2,v3);
//					drawGasketHelper(g, getMidpoint(v1,v2), getMidpoint(v2,v3), getMidpoint(v1,v3), 3);
				drawGasketHelper(g,v1,v2,v3,4);
		}

		private Point2D.Double getMidpoint(Point2D.Double v1, Point2D.Double v2) {
			double x = (v1.x + v2.x) / 2.0;
			double y = (v1.y + v2.y) / 2.0;
			return new Point2D.Double(x, y);
		}

		private void drawTriangle(Graphics2D g, Point2D.Double v1,
				Point2D.Double v2, Point2D.Double v3) {
			g.draw(new Line2D.Double(v1, v2));
			g.draw(new Line2D.Double(v2, v3));
			g.draw(new Line2D.Double(v1, v3));
		}

		private void fillTriangle(Graphics2D g, Point2D.Double v1,
				Point2D.Double v2, Point2D.Double v3) {
			int[] xArray = {(int)v1.x, (int)v2.x, (int)v3.x};
			int[] yArray = {(int)v1.y, (int)v2.y, (int)v3.y};
			
			Polygon center = new Polygon(xArray, yArray, 3);
			
			g.fill(center);
		}


		/**
		 * Finds the third point of the equilateral triangle. The first 2 points 
		 * are assumed to lie in a horizontal line. 
		 *
		 * @param v1 First vertex.
		 * @param v2 Second vertex.
		 * @param direction (ABOVE or BELOW).
		 * @return The coordinates of the third vertex.
		 * @throws IllegalArgumentException When the given points don't lie in a 
		 * horizontal line. 
		 */
		protected Point2D.Double findThirdVertexOfEquilTriangle(
				Point2D.Double v1, Point2D.Double v2, int direction)
				throws IllegalArgumentException {
			if (v1.y != v2.y) {
				throw new IllegalArgumentException(
						"Given points must lie in a horizontal line");
			}

			double sideLength = Math.abs(v1.x - v2.x);
			int v3x = (int) Math.ceil((v1.x + v2.x) / 2);
			double height = sideLength * Math.sqrt(3) / 2.0;
			int v3y = (int) (direction == Sierpinski.ABOVE ? v1.y - height
					: v1.y + height);
			return new Point2D.Double(v3x, v3y);
		}
	}
}

