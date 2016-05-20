package mw.client.gui.arrow;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Random;

import mw.mtgforge.MyRandom;

public class BezierArrow extends LineArrowWithArrowHead {

	final static float arrowSize = 5.0f;
	private boolean inverted = false;

	protected BezierArrow(int from) {
		super(from);
	}

	@Override
	protected void paintComponent(Graphics g) {
		//Graphics2D g2D = (Graphics2D) g;
		BufferedImage doubleBuffer = new BufferedImage(1024, 768,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2D = (Graphics2D) doubleBuffer.getGraphics();
        g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2D.setColor(super.color);

		BezierLine points = new BezierLine();

		points.addPoint(x1, y1);
		points.addPoint((int) ((x1 + x2) / 2.0 + 0.2 * Math.abs(y2 - y1)), (int) ((y1 + y2) / 2.0 + 0.2 * Math.abs(x2 - x1)));
		points.addPoint(x2, y2);
		points.addPoint(x2, y2);

		points.done();
		points.showLine = false;
		points.draw(g2D);
		
		if (inverted) {
			Point p = points.getSecondPoint();
			if (p == null) {
				p = new Point(x1, x2);
			}
			drawArrowHead(g2D, p.x, p.y, x1, y1);
		} else {
			Point p = points.getNextToLastPoint();
			if (p == null) {
				p = new Point(x1, x2);
			}
			drawArrowHead(g2D, p.x, p.y, x2, y2);
		}
		
		g2D.setColor(super.color);
		points.updateFirst(x1+1, y1);
		points.createFinal();
		points.draw(g2D);
		points.updateFirst(x1+2, y1);
		points.createFinal();
		points.draw(g2D);
		points.updateFirst(x1+3, y1);
		points.createFinal();
		points.draw(g2D);
		points.updateFirst(x1+4, y1);
		points.createFinal();
		points.draw(g2D);
		points.updateFirst(x1-1, y1);
		points.createFinal();
		points.draw(g2D);
		points.updateFirst(x1-2, y1);
		points.createFinal();
		points.draw(g2D);
		points.updateFirst(x1-3, y1);
		points.createFinal();
		points.draw(g2D);
		points.updateFirst(x1-4, y1);
		points.createFinal();
		points.draw(g2D);
		
		
		g.drawImage(doubleBuffer, 0, 0, 1024, 768, this);
	}

	class Point {
		int x, y;

		Point(Point p) {
			x = p.x;
			y = p.y;
		}

		Point(int _x, int _y) {
			x = _x;
			y = _y;
		}

		Point() {
			x = 0;
			y = 0;
		}

		void copy(Point p) {
			x = p.x;
			y = p.y;
		}

	}

	class PointList {
		Point pt[];
		int num;
		int x, y, z; // color
		boolean showLine;
		int curPt;
		final int MAXCNTL = 50;
		final int range = 5;

		PointList() {
			num = 0;
			curPt = -1;
			pt = new Point[MAXCNTL];
			x = MyRandom.random.nextInt(255);
			y = MyRandom.random.nextInt(255);
			z = MyRandom.random.nextInt(255);
		}

		boolean addPoint(int x, int y) {
			if (num == MAXCNTL)
				return false;
			pt[num] = new Point(x, y);
			num++;
			return true;
		}
		
		void updateFirst(int x, int y) {
			pt[0].x = x;
			pt[0].y = y;
		}

		void changePoint(int x, int y) {
			pt[num - 1].x = x;
			pt[num - 1].y = y;
		}

		void changeModPoint(int x, int y) {
			pt[curPt].x = x;
			pt[curPt].y = y;
		}

		boolean createFinal() {
			return true;
		}

		boolean done() {
			return true;
		}

		void setShow(boolean show) {
			showLine = show;
		}

		int inRegion(int x, int y) {
			int i;
			for (i = 0; i < num; i++)
				if (Math.abs(pt[i].x - x) < range && Math.abs(pt[i].y - y) < range) {
					curPt = i;
					return i;
				}
			curPt = -1;
			return -1;
		}

		void draw(Graphics g) {
			int i;
			int l = 3;
			for (i = 0; i < num - 1; i++) {
				g.drawLine(pt[i].x - l, pt[i].y, pt[i].x + l, pt[i].y);
				g.drawLine(pt[i].x, pt[i].y - l, pt[i].x, pt[i].y + l);
				drawDashLine(g, pt[i].x, pt[i].y, pt[i + 1].x, pt[i + 1].y); // Draw
				// segment
			}
			// g.drawLine(pt[i].x - l, pt[i].y, pt[i].x + l, pt[i].y);
			g.drawLine(pt[i].x, pt[i].y - l, pt[i].x, pt[i].y + l);
		}

		// draw dash lines
		protected void drawDashLine(Graphics g, int x1, int y1, int x2, int y2) {
			final float seg = 8;
			double x, y;

			if (x1 == x2) {
				if (y1 > y2) {
					int tmp = y1;
					y1 = y2;
					y2 = tmp;
				}
				y = (double) y1;
				while (y < y2) {
					double y0 = Math.min(y + seg, (double) y2);
					g.drawLine(x1, (int) y, x2, (int) y0);
					y = y0 + seg;
				}
				return;
			} else if (x1 > x2) {
				int tmp = x1;
				x1 = x2;
				x2 = tmp;
				tmp = y1;
				y1 = y2;
				y2 = tmp;
			}
			double ratio = 1.0 * (y2 - y1) / (x2 - x1);
			double ang = Math.atan(ratio);
			double xinc = seg * Math.cos(ang);
			double yinc = seg * Math.sin(ang);
			x = (double) x1;
			y = (double) y1;

			while (x <= x2) {
				double x0 = x + xinc;
				double y0 = y + yinc;
				if (x0 > x2) {
					x0 = x2;
					y0 = y + ratio * (x2 - x);
				}
				g.drawLine((int) x, (int) y, (int) x0, (int) y0);
				x = x0 + xinc;
				y = y0 + yinc;
			}
		}
	}

	class BezierLine extends PointList {
		Point bpt[];
		int bnum;
		boolean ready;
		final int MAXPOINT = 1800;
		final int ENOUGH = 2;
		final int RECURSION = 900;
		int nPointAlloc;
		int enough; // control how well we draw the curve.
		int nRecur; // counter of number of recursion
		Point buffer[][];
		int nBuf, nBufAlloc;

		BezierLine() {
			bpt = new Point[MAXPOINT];
			nPointAlloc = MAXPOINT;
			bnum = 0;
			enough = ENOUGH;
			showLine = true;
			ready = false;
			buffer = null;
		}

		protected int distance(Point p0, Point p1, Point p2) {
			int a, b, y1, x1, d1, d2;

			if (p1.x == p2.x && p1.y == p2.y)
				return Math.min(Math.abs(p0.x - p1.x), Math.abs(p0.y - p1.y));
			a = p2.x - p1.x;
			b = p2.y - p1.y;
			y1 = b * (p0.x - p1.x) + a * p1.y;
			x1 = a * (p0.y - p1.y) + b * p1.x;
			d1 = Math.abs(y1 - a * p0.y);
			d2 = Math.abs(x1 - b * p0.x);
			if (a == 0)
				return Math.abs(d2 / b);
			if (b == 0)
				return Math.abs(d1 / a);
			return Math.min(Math.abs(d1 / a), Math.abs(d2 / b));
		}

		protected void curve_split(Point p[], Point q[], Point r[], int num) {
			int i, j;

			// for (i=0;i<num;i++) q[i] = new Point(p[i]);
			for (i = 0; i < num; i++)
				q[i].copy(p[i]);
			for (i = 1; i <= num - 1; i++) {
				// r[num-i] = new Point(q[num-1]);
				r[num - i].copy(q[num - 1]);
				for (j = num - 1; j >= i; j--) {
					// q[j] = new Point((q[j-1].x+q[j].x)/2,
					// (q[j-1].y+q[j].y)/2);
					q[j].x = (q[j - 1].x + q[j].x) / 2;
					q[j].y = (q[j - 1].y + q[j].y) / 2;
				}
			}
			// r[0] = new Point(q[num-1]);
			r[0].copy(q[num - 1]);
		}

		// reuse buffer
		private Point get_buf(int num)[] {
			Point b[];
			if (buffer == null) {
				buffer = new Point[500][num];
				nBufAlloc = 500;
				nBuf = 0;
			}
			if (nBuf == 0) {
				b = new Point[num];
				for (int i = 0; i < num; i++)
					b[i] = new Point();
				return b;
			} else {
				nBuf--;
				b = buffer[nBuf];
				return b;
			}
		}

		private void put_buf(Point b[]) {
			if (nBuf >= nBufAlloc) {
				Point newBuf[][] = new Point[nBufAlloc + 500][num];
				for (int i = 0; i < nBuf; i++)
					newBuf[i] = buffer[i];
				nBufAlloc += 500;
				buffer = newBuf;
			}
			buffer[nBuf] = b;
			nBuf++;
		}

		protected boolean bezier_generation(Point pt[], int num, Point result[], int n[]) {
			Point qt[], rt[]; // for split
			int d[], i, max;

			nRecur++;
			if (nRecur > RECURSION)
				return false;

			d = new int[MAXCNTL];
			for (i = 1; i < num - 1; i++)
				d[i] = distance(pt[i], pt[0], pt[num - 1]);
			max = d[1];
			for (i = 2; i < num - 1; i++)
				if (d[i] > max)
					max = d[i];
			if (max <= enough || nRecur > RECURSION) {
				if (n[0] == 0) {
					if (bnum > 0)
						result[0].copy(pt[0]);
					else
						result[0] = new Point(pt[0]);
					n[0] = 1;
				}
				// reuse
				if (bnum > n[0])
					result[n[0]].copy(pt[num - 1]);
				else
					result[n[0]] = new Point(pt[num - 1]);
				n[0]++;
				if (n[0] == MAXPOINT - 1)
					return false;
			} else {
				// qt = new Point[num];
				// rt = new Point[num];
				qt = get_buf(num);
				rt = get_buf(num);
				curve_split(pt, qt, rt, num);
				if (!bezier_generation(qt, num, result, n))
					return false;
				put_buf(qt);
				if (!bezier_generation(rt, num, result, n))
					return false;
				put_buf(rt);
			}
			return true;
		}

		public boolean try_bezier_generation(Point pt[], int num, Point result[], int n[]) {
			int oldN = n[0];

			if (enough == ENOUGH && num > 6)
				enough += 3;
			// if (enough > ENOUGH) enough -= 5;
			nRecur = 0;
			// in case of recursion stack overflow, relax "enough" and keep
			// trying
			while (!bezier_generation(pt, num, bpt, n)) {
				n[0] = oldN;
				enough += 5;
				nRecur = 0;
			}
			return true;
		}

		boolean createFinal() {
			int n[];
			n = new int[1];
			if (!try_bezier_generation(pt, num, bpt, n)) {
				bnum = 0;
				return false;
			} else {
				bnum = n[0];
				return true;
			}
		}

		boolean done() {
			num--;
			showLine = false;
			ready = true;
			return createFinal();
		}

		void draw(Graphics g) {
			Graphics2D g2D = (Graphics2D) g;
			g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			// g2D.setColor(new Color(x, y, z));
			if (showLine) {
				super.draw(g);
				if (curPt != -1)
					g2D.drawRect(pt[curPt].x - range, pt[curPt].y - range, 2 * range + 1, 2 * range + 1);
			}

			if (ready) {
				for (int i = 0; i < bnum - 1; i++) {
					g2D.drawLine(bpt[i].x, bpt[i].y, bpt[i + 1].x, bpt[i + 1].y);
				}
			}
		}

		public Point getNextToLastPoint() {
			if (ready) {
				return new Point(bpt[bnum - 2].x, bpt[bnum - 2].y);
			} else {
				return null;
			}
		}

		public Point getSecondPoint() {
			if (ready) {
				return new Point(bpt[1].x, bpt[1].y);
			} else {
				return null;
			}
		}
	}

	public void setInverted(boolean inverted) {
		this.inverted = inverted;
	}

	private static final long serialVersionUID = 1L;
}

