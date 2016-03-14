package laboratory.plugin.visualizator.sAnt.automaton;

import laboratory.plugin.visualizator.sAnt.runner.AutomatonRunner;
import laboratory.plugin.task.ant.individual.Automaton;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;

/**
 * @author Yulia Chebotareva
 */
public class DocumentPanel extends JPanel implements MouseListener,
		MouseMotionListener, IDocumentPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static final int EPS_DOUBLE_CLICK = 3;

	static final int DEFAULT_CURSOR = 0;
	static final int CROSSHAIR_CURSOR = 1;
	static final int HAND_CURSOR = 2;
	static final int HAND_MOVE_CURSOR = 3;

	static final int PLAIN_MODE = 0;
	static final int MOVE_MODE = 1;

	static final int EPS = 3;
	static final int EDGE_EPS = 3;
	static final double LOOP_EPS = 0.1;
	static final int RADIUS = 18;
	static final int HOW_MANY = 30;
	static final double DEPS = 0.00001;
	private static final int ARROW1 = 10;
	private static final int ARROW2 = 15;
	static final int SELECTED = 2;
	static final int SELECTED_EDGE = 3;

	private static final Color SELECTEDCOLOR = new Color(200, 100, 100);

	private static final int DARKER = 50;

	private static final int NODE_EPS = 5;

	public static int ELLIPSE_HEIGHT = 16;

	Graph graph;
	Point edgeStart;
	Point edgeEnd;
	Set<InternalNode> selectedInternalNodes;
	Map<InternalNode, Map<InternalNode, List<Edge>>> selectedEdges;

	Set<InternalNode> selectedNodesCopies;
	Map<InternalNode, Map<InternalNode, List<Edge>>> selectedEdgesCopy;

	ArrayList<EdgeStructure> newEdges;

	boolean isShiftDown;
	boolean needRect;
	InternalNode startInternalNode, endNode;
	boolean needEdge;
	boolean needLoop;
	boolean rightClick, moveOn;
	private double scale;
	private Point center;
	private int mode;
	private Point offset;
	private Point lastPoint;
	int w, h;

	private javax.swing.Timer timer;
	private boolean singleClick;
	private final int delay = 200;

	private boolean white;

	private boolean edit;

	private AutomatonRunner m;

	// private AdditionalPanelLayout nodePanelLayout;

	private MouseEvent lastEvent;

	private boolean wasNeedEdge;

	// private AutomataEditor editor;

	private double angle(int x1, int y1, int x2, int y2) {
		double ang;
		if (x2 - x1 == 0) {
			if (y1 - y2 > 0) {
				ang = Math.PI / 2;
			} else {
				ang = 3 * Math.PI / 2;
			}
		} else if (x2 - x1 > 0) {
			ang = Math.atan(((float) y1 - y2) / (x2 - x1));
		} else if (y1 >= y2) {
			ang = Math.atan(((float) y1 - y2) / (x2 - x1)) + Math.PI;
		} else {
			ang = Math.atan(((float) y1 - y2) / (x2 - x1)) - Math.PI;
		}
		while (ang < 0) {
			ang = ang + 2 * Math.PI;
		}
		while (ang > 2 * Math.PI) {
			ang = ang - 2 * Math.PI;
		}
		return ang;
	}

	public void drawArrow(Graphics g, int x1, int y1, int x2, int y2,
			int radius1, int radius2, int border) {
		int xa;
		int ya;

		xa = (int) Math.round(x1 + (x2 - x1)
				/ Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2))
				* (radius1 + border));
		ya = (int) Math.round(y1 + (y2 - y1)
				/ Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2))
				* (radius1 + border));

		int xb;
		int yb;
		xb = (int) Math.round(x2 - (x2 - x1)
				/ Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2))
				* (radius2 + border));
		yb = (int) Math.round(y2 - (y2 - y1)
				/ Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2))
				* (radius2 + border));
		g.drawLine(xa, ya, xb, yb);
		double ang = angle(xa, ya, xb, yb);

		double x12 = xa + Math.cos(ang - Math.PI / 12) * ARROW2 * scale;
		double y12 = ya - Math.sin(ang - Math.PI / 12) * ARROW2 * scale;
		double x13 = xa + (ARROW1 * scale) * Math.cos(ang);
		double y13 = ya - (ARROW1 * scale) * Math.sin(ang);
		double x14 = xa + Math.cos(ang + Math.PI / 12) * ARROW2 * scale;
		double y14 = ya - Math.sin(ang + Math.PI / 12) * ARROW2 * scale;
		g.fillPolygon(new int[] { Math.round(xa), (int) Math.round(x12),
				(int) Math.round(x13), (int) Math.round(x14) }, new int[] {
				Math.round(ya), (int) Math.round(y12), (int) Math.round(y13),
				(int) Math.round(y14) }, 4);
	}

	private void drawLoopArrow(Graphics g, InternalNode internalNode) {
		int x1 = internalNode.getX() + 37 + offset.x;
		int y1 = internalNode.getY() + 9 + offset.y;

		int x2 = internalNode.getX() + 35 + offset.x;
		int y2 = internalNode.getY() - 4 + offset.y;

		int x3 = internalNode.getX() + 40 + offset.x;
		int y3 = internalNode.getY() + offset.y;

		int x4 = internalNode.getX() + 44 + offset.x;
		int y4 = internalNode.getY() - 4 + offset.y;

		g.fillPolygon(
				new int[] { Math.round(x1), Math.round(x2), Math.round(x3),
						Math.round(x4) },
				new int[] { Math.round(y1), Math.round(y2), Math.round(y3),
						Math.round(y4) }, 4);
	}

	private void drawDoubleSideEdge(Graphics2D g, InternalNode node1,
			InternalNode node2) {
		double angle = Edge.GetAngle(new Point(node1.getX(), node1.getY()),
				new Point(node2.getX(), node2.getY()));
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g.translate((int) Math.round(center.getX()
				+ (node1.getX() + node1.getWidth() / 2 - center.getX())
				+ offset.getX()), (int) Math.round(center.getY()
				+ (node1.getY() + node1.getHeight() / 2 - center.getY())
				+ offset.getY()));
		if (angle < 0) {
			angle += 360;
		} else if (angle > 360) {
			angle -= 360;
		}
		g.rotate((float) angle * Math.PI / 180);
		double d = Math.sqrt(Math.pow(node1.getX() - node2.getX(), 2)
				+ Math.pow(node1.getY() - node2.getY(), 2));

		if (selectedEdges.containsKey(node1)
				&& selectedEdges.get(node1).containsKey(node2)
				&& !selectedEdges.get(node1).get(node2).isEmpty()) {
			drawArcSelection(g, node1, node2);
		}

		if (selectedEdges.containsKey(node2)
				&& selectedEdges.get(node2).containsKey(node1)
				&& !selectedEdges.get(node2).get(node1).isEmpty()) {
			drawArcSelection(g, node2, node1);
		}

		Edge forwardEdge = graph.get(node1).get(node2).get(0);
		Edge backwardEdge = graph.get(node2).get(node1).get(0);

		g.setColor(forwardEdge.getColor());
		BasicStroke pen = new BasicStroke(forwardEdge.getWidth(),
				BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL);
		g.setStroke(pen);

		g.drawArc(node1.getWidth() / 2, -ELLIPSE_HEIGHT / 2,
				(int) d - node1.getWidth(), ELLIPSE_HEIGHT, 0, 360);

		drawDoubleEdgeArrows(g, node1, node2);

		g.rotate(-(float) angle * Math.PI / 180);
		g.translate(-(int) Math.round(center.getX()
				+ (node1.getX() + node1.getWidth() / 2 - center.getX())
				+ offset.getX()), -(int) Math.round(center.getY()
				+ (node1.getY() + node1.getHeight() / 2 - center.getY())
				+ offset.getY()));

		ExtractEdgeStrings(backwardEdge, node1, node2, g, 0,
				-ELLIPSE_HEIGHT / 2);
		ExtractEdgeStrings(forwardEdge, node2, node1, g, 0, -ELLIPSE_HEIGHT / 2);
		// ExtractEdgeStrings(forwardEdge, node1, node2, g, 0, -ELLIPSE_HEIGHT /
		// 2);
		// ExtractEdgeStrings(backwardEdge, node2, node1, g, 0, -ELLIPSE_HEIGHT
		// / 2);
	}

	private void drawArcSelection(Graphics2D g, InternalNode node1,
			InternalNode node2) {
		double d = Math.sqrt(Math.pow(node1.getX() - node2.getX(), 2)
				+ Math.pow(node1.getY() - node2.getY(), 2));

		Edge edge = graph.get(node1).get(node2).get(0);
		g.setColor(node1.getBgColor());
		BasicStroke pen = new BasicStroke(edge.getWidth() + 4,
				BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL);
		g.setStroke(pen);

		if (node1.getId() > node2.getId())
			g.drawArc(node1.getWidth() / 2, -ELLIPSE_HEIGHT / 2, (int) d
					- node1.getWidth(), ELLIPSE_HEIGHT, 0, 180);
		else
			g.drawArc(node1.getWidth() / 2, -ELLIPSE_HEIGHT / 2, (int) d
					- node1.getWidth(), ELLIPSE_HEIGHT, 180, 180);
	}

	private void drawDoubleEdgeArrows(Graphics2D g, InternalNode node1,
			InternalNode node2) {
		double d = Math.sqrt(Math.pow(node1.getX() - node2.getX(), 2)
				+ Math.pow(node1.getY() - node2.getY(), 2));
		int x1 = (int) (d - node2.getWidth() / 2);
		int y1 = 0;

		double a = (d - node1.getWidth()) / 2;
		double b = ELLIPSE_HEIGHT / 2;
		double di = 4 * a * a - 4 * (1 - b * b / a / a)
				* (a * a + b * b - ARROW1 * ARROW1);

		if (di >= 0) {
			double x_1 = (2 * a - Math.sqrt(di)) / 2 / (1 - b * b / a / a);
			double x_2 = (2 * a + Math.sqrt(di)) / 2 / (1 - b * b / a / a);

			double y_1 = b / a * Math.sqrt(a * a - x_1 * x_1);
			double y_2 = b / a * Math.sqrt(a * a - x_2 * x_2);

			int x3;
			int y3;
			if (y_1 > 0) {
				x3 = (int) (x_1 + d / 2);
				y3 = (int) y_1;
			} else {
				x3 = (int) (x_2 + d / 2);
				y3 = (int) y_2;
			}

			double ang = angle(x1, y1, x3, y3);

			double x12 = x1 + Math.cos(ang - Math.PI / 12) * ARROW2 * scale;
			double y12 = y1 - Math.sin(ang - Math.PI / 12) * ARROW2 * scale;
			// double x13 = x1 + (ARROW1 * scale) * Math.cos(ang);
			// double y13 = y1 - (ARROW1 * scale) * Math.sin(ang);
			double x14 = x1 + Math.cos(ang + Math.PI / 12) * ARROW2 * scale;
			double y14 = y1 - Math.sin(ang + Math.PI / 12) * ARROW2 * scale;
			g.fillPolygon(new int[] { Math.round(x1), (int) Math.round(x12),
					(int) Math.round(x3), (int) Math.round(x14) }, new int[] {
					Math.round(y1), (int) Math.round(y12),
					(int) Math.round(y3), (int) Math.round(y14) }, 4);

			x1 = (int) d - x1;
			x12 = (int) d - x12;
			x3 = (int) d - x3;
			x14 = (int) d - x14;

			y1 = -y1;
			y12 = -y12;
			y3 = -y3;
			y14 = -y14;

			g.fillPolygon(new int[] { Math.round(x1), (int) Math.round(x12),
					(int) Math.round(x3), (int) Math.round(x14) }, new int[] {
					Math.round(y1), (int) Math.round(y12),
					(int) Math.round(y3), (int) Math.round(y14) }, 4);
		}
	}

	private void drawLoop(Graphics g, InternalNode internalNode, int angle1,
			int angle2) {
		g.drawArc(internalNode.getX() - 20 + internalNode.getWidth() / 2
				+ (int) offset.getX(),
				internalNode.getY() - 30 + (int) offset.getY(), 40, 55, angle1,
				angle2);
	}

	private void drawStartLabel(Graphics2D g, InternalNode temp) {
		g.setColor(temp.getFontColor());
		g.setFont(temp.getFont());
		FontRenderContext context = g.getFontRenderContext();
		Font font = new Font(temp.getFont().getFamily(), temp.getFont()
				.getStyle(), temp.getFont().getSize() - 4);
		GlyphVector vector = font.createGlyphVector(context, "START");
		double angle = Math.PI * (-30) / 180;
		vector.setGlyphTransform(
				0,
				new AffineTransform(Math.cos(angle), Math.sin(angle), -Math
						.sin(angle), Math.cos(angle), 0, 6));
		angle = Math.PI * (-15) / 180;
		vector.setGlyphTransform(
				1,
				new AffineTransform(Math.cos(angle), Math.sin(angle), -Math
						.sin(angle), Math.cos(angle), 3, -2));
		vector.setGlyphPosition(2, new Point2D.Double(17, -2));
		angle = Math.PI * (15) / 180;
		vector.setGlyphTransform(
				3,
				new AffineTransform(Math.cos(angle), Math.sin(angle), -Math
						.sin(angle), Math.cos(angle), 5, 0));
		angle = Math.PI * (30) / 180;
		vector.setGlyphTransform(
				4,
				new AffineTransform(Math.cos(angle), Math.sin(angle), -Math
						.sin(angle), Math.cos(angle), 7, 5));
		doWhite(g);
		g.drawGlyphVector(vector, (float) (temp.getX() + offset.getX()),
				(float) (temp.getY() + offset.getY()));
	}

	private void drawEdgeLabel(Graphics2D g, Edge edge, InternalNode temp,
			InternalNode neighbour, String[] strings, int xOff, int yOff) {
		int height = 0;

		for (int i = strings.length - 1; i >= 0; i--) {
			double angle = Edge.GetAngle(new Point(temp.getX(), temp.getY()),
					new Point(neighbour.getX(), neighbour.getY()));
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			g.translate(
					(int) Math.round(center.getX()
							+ (temp.getX() + temp.getWidth() / 2 - center
									.getX()) * 1 + offset.getX()),
					(int) Math.round(center.getY()
							+ (temp.getY() + temp.getHeight() / 2 - center
									.getY()) * 1 + offset.getY()));
			if (angle < 0) {
				angle += 360;
			} else if (angle > 360) {
				angle -= 360;
			}
			g.rotate((float) angle * Math.PI / 180);
			Font font = edge.getLabelFont();

			if ((angle > 90) && (angle <= 270) && yOff == 0) {
				g.rotate(Math.PI);

				g.setColor(edge.getLabelColor());
				doWhite(g);
				g.setFont(edge.getLabelFont());

				FontRenderContext context = g.getFontRenderContext();
				Rectangle2D bounds = font.getStringBounds(strings[i], context);

				g.drawString(
						strings[i],
						-(int) Math.round(1
								* Math.sqrt(Math.pow(
										(neighbour.getX() - temp.getX()), 2)
										+ Math.pow(
												(neighbour.getY() - temp.getY()),
												2)) / 2 + bounds.getWidth() / 2)
								+ xOff,
						height
								+ (int) Math.round(-(double) font.getSize() * 1 / 2)
								+ yOff);

				height += (int) Math.round(-(double) font.getSize() * 1 / 2) - 7;

				g.rotate(-Math.PI);
			} else {
				g.setFont(font);
				g.setColor(edge.getLabelColor());

				doWhite(g);

				FontRenderContext context = g.getFontRenderContext();
				Rectangle2D bounds = font.getStringBounds(strings[i], context);

				g.drawString(
						strings[i],
						(int) Math.round(1
								* Math.sqrt(Math.pow(
										(neighbour.getX() - temp.getX()), 2)
										+ Math.pow(
												(neighbour.getY() - temp.getY()),
												2)) / 2 - bounds.getWidth() / 2)
								+ xOff,
						height
								+ (int) Math.round(-(double) font.getSize() * 1 / 2)
								+ yOff);
				height += (int) Math.round(-(double) font.getSize() * 1 / 2) - 7;
			}
			g.rotate(-(float) angle * Math.PI / 180);
			g.translate(
					-(int) Math.round(center.getX()
							+ (temp.getX() + temp.getWidth() / 2 - center
									.getX()) + offset.getX()),
					-(int) Math.round(center.getY()
							+ (temp.getY() + temp.getHeight() / 2 - center
									.getY()) + offset.getY()));
		}
	}

	public void setWhiterMode(boolean white) {
		this.white = white;
		repaint();
	}

	// public void setNewPropertyPanelPosition(int x, int y) {
	// nodePanelLayout.setLocation(x, y);
	// doLayout();
	// }

	public void unselectStartNode() {
		for (InternalNode internalNode : graph.keySet()) {
			if (internalNode.getIsStart()) {
				internalNode.setIsStart(false);
			}
		}
	}

	public void doWhite(Graphics g) {
		if (white) {
			Color color = g.getColor();
			for (int i = 0; i < 100; i++) {
				color = new Color(Math.min(255, color.getRed() + 1), Math.min(
						255, color.getGreen() + 1), Math.min(255,
						color.getBlue() + 1));
			}
			g.setColor(color);
		}
	}

	public void update(Graphics g) {
		if (m != null) {
			for (InternalNode i : graph.keySet()) {
				if (i.getId() == m.getState()) {
					i.setColor(SELECTEDCOLOR);
				} else {
					i.setColor(i.getBgColor());
				}
			}
		}
		w = getSize().width;
		h = getSize().height;
		center.setLocation(w / 2, h / 2);
		Image offImg = createImage(w, h);
		Graphics2D offGr = (Graphics2D) offImg.getGraphics();

		offGr.setPaintMode();
		offGr.setColor(Color.WHITE);
		offGr.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_OFF);
		offGr.fillRect(0, 0, w, h);

		offGr.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		for (InternalNode temp : graph.keySet()) {
			offGr.setColor(temp.getBorderColor());
			doWhite(offGr);
			offGr.fillOval((int) (center.getX() + (temp.getX() - center.getX())
					* scale + offset.getX()),
					(int) (center.getY() + (temp.getY() - center.getY())
							* scale + offset.getY()),
					(int) (temp.getWidth() * scale),
					(int) (temp.getHeight() * scale));

			Color tempColor;

			if (selectedInternalNodes.contains(temp)) {
				if (edit) {
					offGr.setColor(Color.BLACK);
					doWhite(offGr);
					offGr.drawRect(
							(int) (center.getX()
									+ (temp.getX() - center.getX()) * scale + offset
										.getX()) - 2 * SELECTED,
							(int) (center.getY()
									+ (temp.getY() - center.getY()) * scale + offset
										.getY()) - 2 * SELECTED, 2 * SELECTED,
							2 * SELECTED);
					offGr.drawRect(
							(int) (center.getX()
									+ (temp.getX() - center.getX() + temp
											.getWidth() / 2) * scale + offset
										.getX()) - SELECTED,
							(int) (center.getY()
									+ (temp.getY() - center.getY()) * scale + offset
										.getY()) - 2 * SELECTED, 2 * SELECTED,
							2 * SELECTED);
					offGr.drawRect(
							(int) (center.getX()
									+ (temp.getX() - center.getX() + temp
											.getWidth()) * scale + offset
									.getX()),
							(int) (center.getY()
									+ (temp.getY() - center.getY()) * scale + offset
										.getY()) - 2 * SELECTED, 2 * SELECTED,
							2 * SELECTED);
					offGr.drawRect(
							(int) (center.getX()
									+ (temp.getX() - center.getX()) * scale + offset
										.getX()) - 2 * SELECTED,
							(int) (center.getY()
									+ (temp.getY() - center.getY() + temp
											.getHeight() / 2) * scale + offset
										.getY()) - SELECTED, 2 * SELECTED,
							2 * SELECTED);
					offGr.drawRect(
							(int) (center.getX()
									+ (temp.getX() - center.getX() + temp
											.getWidth()) * scale + offset
									.getX()),
							(int) (center.getY()
									+ (temp.getY() - center.getY() + temp
											.getHeight() / 2) * scale + offset
										.getY()) - SELECTED, 2 * SELECTED,
							2 * SELECTED);
					offGr.drawRect(
							(int) (center.getX()
									+ (temp.getX() - center.getX()) * scale + offset
										.getX()) - 2 * SELECTED,
							(int) (center.getY()
									+ (temp.getY() - center.getY() + temp
											.getHeight()) * scale + offset
									.getY()), 2 * SELECTED, 2 * SELECTED);
					offGr.drawRect(
							(int) (center.getX()
									+ (temp.getX() - center.getX() + temp
											.getWidth() / 2) * scale + offset
										.getX()) - SELECTED,
							(int) (center.getY()
									+ (temp.getY() - center.getY() + temp
											.getHeight()) * scale + offset
									.getY()), 2 * SELECTED, 2 * SELECTED);
					offGr.drawRect(
							(int) (center.getX()
									+ (temp.getX() - center.getX() + temp
											.getWidth()) * scale + offset
									.getX()),
							(int) (center.getY()
									+ (temp.getY() - center.getY() + temp
											.getHeight()) * scale + offset
									.getY()), 2 * SELECTED, 2 * SELECTED);
					tempColor = new Color(Math.max(0, temp.getBgColor()
							.getRed() - DARKER), Math.max(0, temp.getBgColor()
							.getGreen() - DARKER), Math.max(0, temp
							.getBgColor().getBlue() - DARKER));
					offGr.setColor(tempColor);
				} else {
					// offGr.setColor(new Color(Math.max(0,
					// Color.ORANGE.getRed() - DARK), Math.max(0,
					// Color.ORANGE.getGreen() - DARK), Math.max(0,
					// Color.ORANGE.getBlue() - DARK)));
					offGr.setColor(new Color(255, 159, 9, 255));
				}
			} else {
				tempColor = temp.getColor();
				offGr.setColor(tempColor);
			}

			doWhite(offGr);
			offGr.fillOval(
					(int) (center.getX() + (temp.getX() - center.getX())
							* scale + offset.getX())
							+ temp.getBorderWidth(),
					(int) (center.getY() + (temp.getY() - center.getY())
							* scale + offset.getY())
							+ temp.getBorderWidth(),
					(int) (temp.getWidth() * scale) - 2 * temp.getBorderWidth(),
					(int) (temp.getHeight() * scale) - 2
							* temp.getBorderWidth());

			if (temp.getIsStart()) {
				drawStartLabel(offGr, temp);
			}

			offGr.setColor(temp.getFontColor());
			Font font = temp.getFont();
			font = font.deriveFont((float) (font.getSize() * scale));
			offGr.setFont(font);
			FontRenderContext context = offGr.getFontRenderContext();
			Rectangle2D bounds = font.getStringBounds(temp.getLabel(), context);
			doWhite(offGr);
			offGr.drawString(
					temp.getLabel(),
					(int) (center.getX() + (temp.getX() - center.getX())
							* scale + offset.getX() + (temp.getWidth() * scale)
							/ 2 - (bounds.getWidth() / 2)),
					(int) (center.getY() + (temp.getY() - center.getY())
							* scale + (temp.getHeight() * scale) / 2
							+ (bounds.getHeight() / 3) + offset.getY()));
		}
		for (InternalNode temp : graph.keySet()) {
			for (InternalNode neighbour : graph.getNeighbours(temp)) {
				// for (AutomataGraphEditor.Edge edge :
				// graph.get(temp).get(neighbour)) {
				if (graph.get(temp).get(neighbour).size() > 0) {
					Edge edge = graph.get(temp).get(neighbour).get(0);
					if (edge.getClass().equals(Loop.class)) {
						if (selectedEdges.containsKey(temp)
								&& selectedEdges.get(temp).containsKey(
										neighbour)
								&& selectedEdges.get(temp).get(neighbour)
										.contains(edge)) {
							offGr.setColor(temp.getBgColor());
							doWhite(offGr);
							BasicStroke pen = new BasicStroke(
									edge.getWidth() + 4,
									BasicStroke.CAP_SQUARE,
									BasicStroke.JOIN_BEVEL);
							(offGr).setStroke(pen);
							drawLoop(offGr, neighbour, -20, 221);
							offGr.setColor(edge.getColor());
							doWhite(offGr);
							pen = new BasicStroke(edge.getWidth(),
									BasicStroke.CAP_SQUARE,
									BasicStroke.JOIN_BEVEL);
							(offGr).setStroke(pen);
							drawLoop(offGr, neighbour, -26, 232);
						} else
							drawLoop(offGr, neighbour, -26, 232);
						g.setColor(edge.getColor());
						doWhite(g);
						drawLoopArrow(offGr, neighbour);
						drawLoopLabel(offGr, edge, temp);
					} else if (graph.checkIfDoubleSideEdge(temp, neighbour)) {
						if (temp.getId() < neighbour.getId())
							drawDoubleSideEdge(offGr, temp, neighbour);
					} else
						DrawEdge(edge, temp, neighbour, offGr);
				}
			}
		}
		if (needRect) {
			offGr.setColor(Color.BLACK);
			doWhite(offGr);
			float[] dash = { 5, 4 };
			BasicStroke pen;
			if (mode == PLAIN_MODE) {
				pen = new BasicStroke(1, BasicStroke.CAP_SQUARE,
						BasicStroke.JOIN_BEVEL, 10, dash, 0);
			} else {
				pen = new BasicStroke(1, BasicStroke.CAP_SQUARE,
						BasicStroke.JOIN_BEVEL);
			}
			(offGr).setStroke(pen);
			int x = (int) Math.min(edgeStart.getX(), edgeEnd.getX());
			int y = (int) Math.min(edgeStart.getY(), edgeEnd.getY());
			int width = (int) Math.max(edgeStart.getX(), edgeEnd.getX()) - x;
			int height = (int) Math.max(edgeStart.getY(), edgeEnd.getY()) - y;
			offGr.drawRect(
					(int) (center.getX() + (x - center.getX()) * scale + offset
							.getX()), (int) (center.getY()
							+ (y - center.getY()) * scale + offset.getY()),
					(int) (width * scale), (int) (height * scale));
		} else if (needLoop) {
			drawLoop(offGr, startInternalNode, -26, 232);
			offGr.setColor(new Loop("").getColor());
			drawLoopArrow(offGr, startInternalNode);
		} else if (needEdge) {
			drawArrow(
					offGr,
					(int) (center.getX() + (edgeEnd.getX() - center.getX())
							* scale + offset.getX()),
					(int) (center.getY() + (edgeEnd.getY() - center.getY())
							* scale + offset.getY()),
					(int) (center.getX()
							+ (startInternalNode.getX()
									+ startInternalNode.getWidth() / 2 - center
										.getX()) * scale + offset.getX()),
					(int) (center.getY()
							+ (startInternalNode.getY()
									+ startInternalNode.getHeight() / 2 - center
										.getY()) * scale + offset.getY()), 0,
					(int) (startInternalNode.getWidth() / 2 * scale), 0);
		}

		offGr.setColor(Color.BLACK);
		doWhite(offGr);
		BasicStroke pen = new BasicStroke(1, BasicStroke.CAP_SQUARE,
				BasicStroke.JOIN_BEVEL);
		(offGr).setStroke(pen);

		// offGr.setColor(Color.GREEN);
		// for (int i = 0; i < 800; i++) {
		// for (int j = 0; j < 600; j++) {
		// if (graph.edgeContains(new Point(i, j), EPS, EDGE_EPS) != null)
		// offGr.fillRect(i, j, 1, 1);
		// }
		// }

		paintChildren(offGr);

		g.drawImage(offImg, 0, 0, this);
	}

	private void DrawEdge(Edge edge, InternalNode temp, InternalNode neighbour,
			Graphics2D offGr) {
		ExtractEdgeStrings(edge, temp, neighbour, offGr, 0, 0);

		if (selectedEdges.containsKey(temp)
				&& selectedEdges.get(temp).containsKey(neighbour)
				&& selectedEdges.get(temp).get(neighbour).contains(edge)) {
			offGr.setColor(temp.getBgColor());
			doWhite(offGr);
			BasicStroke pen = new BasicStroke(edge.getWidth() + 4,
					BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL);
			(offGr).setStroke(pen);
			drawArrow(
					offGr,
					(int) (center.getX()
							+ (neighbour.getX() + neighbour.getWidth() / 2 - center
									.getX()) * scale + offset.getX()),
					(int) (center.getY()
							+ (neighbour.getY() + neighbour.getHeight() / 2 - center
									.getY()) * scale + offset.getY()),
					(int) (center.getX()
							+ (temp.getX() + temp.getWidth() / 2 - center
									.getX()) * scale + offset.getX()),
					(int) (center.getY()
							+ (temp.getY() + temp.getHeight() / 2 - center
									.getY()) * scale + offset.getY()),
					(int) (neighbour.getWidth() / 2 * scale),
					(int) (temp.getWidth() / 2 * scale), 2);
			offGr.setColor(edge.getColor());
			doWhite(offGr);
			pen = new BasicStroke(edge.getWidth(), BasicStroke.CAP_SQUARE,
					BasicStroke.JOIN_BEVEL);
			(offGr).setStroke(pen);
			drawArrow(
					offGr,
					(int) (center.getX()
							+ (neighbour.getX() + neighbour.getWidth() / 2 - center
									.getX()) * scale + offset.getX()),
					(int) (center.getY()
							+ (neighbour.getY() + neighbour.getHeight() / 2 - center
									.getY()) * scale + offset.getY()),
					(int) (center.getX()
							+ (temp.getX() + temp.getWidth() / 2 - center
									.getX()) * scale + offset.getX()),
					(int) (center.getY()
							+ (temp.getY() + temp.getHeight() / 2 - center
									.getY()) * scale + offset.getY()),
					(int) (neighbour.getWidth() / 2 * scale),
					(int) (temp.getWidth() / 2 * scale), 0);
		} else {
			offGr.setColor(edge.getColor());
			doWhite(offGr);
			BasicStroke pen = new BasicStroke(edge.getWidth(),
					BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL);
			(offGr).setStroke(pen);
			drawArrow(
					offGr,
					(int) (center.getX()
							+ (neighbour.getX() + neighbour.getWidth() / 2 - center
									.getX()) * scale + offset.getX()),
					(int) (center.getY()
							+ (neighbour.getY() + neighbour.getHeight() / 2 - center
									.getY()) * scale + offset.getY()),
					(int) (center.getX()
							+ (temp.getX() + temp.getWidth() / 2 - center
									.getX()) * scale + offset.getX()),
					(int) (center.getY()
							+ (temp.getY() + temp.getHeight() / 2 - center
									.getY()) * scale + offset.getY()),
					(int) (neighbour.getWidth() / 2 * scale),
					(int) (temp.getWidth() / 2 * scale), 0);
		}
	}

	private void ExtractEdgeStrings(Edge edge, InternalNode temp,
			InternalNode neighbour, Graphics2D offGr, int xOff, int yOff) {
		if (!edge.getLabel().equals("")) {
			String label = edge.getLabel();
			String[] strings = label.split("\n");
			drawEdgeLabel(offGr, edge, temp, neighbour, strings, xOff, yOff);
		}
	}

	private void drawLoopLabel(Graphics2D offGr, Edge edge, InternalNode temp) {
		String[] strings = edge.getLabel().split("\n");
		int height = (int) (center.getY() + (temp.getY() - center.getY())
				* scale + (temp.getHeight() * scale) / 2 + offset.getY() - 58);
		for (int i = strings.length - 1; i >= 0; i--) {

			offGr.setColor(edge.getLabelColor());
			Font font = edge.getLabelFont();
			font = font.deriveFont((float) (font.getSize() * scale));
			offGr.setFont(font);
			FontRenderContext context = offGr.getFontRenderContext();
			Rectangle2D bounds = font.getStringBounds(strings[i], context);
			doWhite(offGr);
			offGr.drawString(strings[i], (int) (center.getX()
					+ (temp.getX() - center.getX()) * scale + offset.getX()
					+ (temp.getWidth() * scale) / 2 - (bounds.getWidth() / 2)),
					height + (int) (bounds.getHeight() / 3));
			height -= (bounds.getHeight() / 3) + 7;
		}
	}

	public void paint(Graphics g) {
		update(g);
	}

	public DocumentPanel(boolean edit, Automaton a, AutomatonRunner m,
			int width, int height) {

		this.edit = edit;
		this.m = m;

		setBackground(Color.WHITE);

		if (edit) {
			addMouseMotionListener(this);
			addMouseListener(this);
			addKeyListener(new KeyHandler());
		}
		init(a, width, height);

		if (edit) {
			ActionListener al = new ActionListener() {

				public void actionPerformed(ActionEvent evt) {
					if (singleClick) {

						selectedInternalNodes = new HashSet<InternalNode>();
						for (InternalNode internalNode : selectedNodesCopies) {
							selectedInternalNodes.add(internalNode);
						}
						selectedEdges = new HashMap<InternalNode, Map<InternalNode, List<Edge>>>();
						for (InternalNode internalNode : selectedEdgesCopy
								.keySet()) {
							selectedEdges.put(internalNode,
									selectedEdgesCopy.get(internalNode));
						}

						processSingleClick();
					}
					singleClick = false;
				}
			};
			timer = new javax.swing.Timer(delay, al);
			timer.setRepeats(false);
			requestFocusInWindow();
		}
		setPreferredSize(new Dimension(width, height));
	}

	public void init(Automaton g, int width, int height) {
		graph = createGraph(g, width, height);
		selectedInternalNodes = new HashSet<InternalNode>();
		selectedEdges = new HashMap<InternalNode, Map<InternalNode, List<Edge>>>();
		scale = 1;
		center = new Point();
		offset = new Point(0, 0);
		lastPoint = new Point();
		mode = PLAIN_MODE;
	}

	private void doSelection() {
		selectedNodesCopies = new HashSet<InternalNode>();
		for (InternalNode internalNode : selectedInternalNodes) {
			selectedNodesCopies.add(internalNode);
		}
		selectedEdgesCopy = new HashMap<InternalNode, Map<InternalNode, List<Edge>>>();
		for (InternalNode internalNode : selectedEdges.keySet()) {
			selectedEdgesCopy
					.put(internalNode, selectedEdges.get(internalNode));
		}

		edgeEnd = new Point();
		edgeEnd.setLocation(
				(int) ((lastEvent.getX() - offset.getX()) / scale - center
						.getX() * (1 / scale - 1)),
				(int) ((lastEvent.getY() - offset.getY()) / scale - center
						.getY() * (1 / scale - 1)));
		if (!rightClick && (mode == PLAIN_MODE)) {
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			if (!moveOn) {
				endNode = graph.nodeContains(edgeEnd, DEPS);
				if ((edgeStart.getX() == edgeEnd.getX())
						&& (edgeStart.getY() == edgeEnd.getY())
						&& (startInternalNode == null)) {
					EdgePair edgePair = graph.edgeContains(edgeEnd, EPS,
							EDGE_EPS);
					if (edgePair == null) {
						edgePair = graph.loopContains(edgeEnd, LOOP_EPS);
					}
					if (edgePair != null) {
						if (selectedEdges.containsKey(edgePair.getSource())
								&& selectedEdges.get(edgePair.getSource())
										.containsKey(edgePair.getSink())
								&& !selectedEdges.get(edgePair.getSource())
										.get(edgePair.getSink()).isEmpty()) {
							if (!isShiftDown) {
								selectedInternalNodes.clear();
								selectedEdges.clear();
							} else {
								selectedEdges.get(edgePair.getSource())
										.get(edgePair.getSink()).clear();
							}
						} else {
							if (!isShiftDown) {
								selectedInternalNodes.clear();
								selectedEdges.clear();
							}
							if (!selectedEdges
									.containsKey(edgePair.getSource())) {
								selectedEdges
										.put(edgePair.getSource(),
												new HashMap<InternalNode, List<Edge>>());
							}
							if (!selectedEdges.get(edgePair.getSource())
									.containsKey(edgePair.getSink())) {
								selectedEdges.get(edgePair.getSource()).put(
										edgePair.getSink(),
										new LinkedList<Edge>());
							}
							for (Edge edge : graph.get(edgePair.getSource())
									.get(edgePair.getSink())) {
								selectedEdges.get(edgePair.getSource())
										.get(edgePair.getSink()).add(edge);
							}
						}
					} else {
						if (!(selectedInternalNodes.isEmpty()
								&& selectedEdges.isEmpty() || isShiftDown)) {
							selectedInternalNodes.clear();
							selectedEdges.clear();
						}
					}
				} else if ((startInternalNode != null)
						&& startInternalNode.equals(endNode)) {
					if (selectedInternalNodes.contains(startInternalNode)) {
						if (!isShiftDown) {
							selectedInternalNodes.clear();
							selectedEdges.clear();
						} else {
							selectedInternalNodes.remove(startInternalNode);
						}
					} else {
						if (!isShiftDown) {
							selectedInternalNodes.clear();
							selectedEdges.clear();
						}
						selectedInternalNodes.add(startInternalNode);
					}
				} else if ((startInternalNode != null) && (endNode != null)
						&& !startInternalNode.equals((endNode))) {
					if (!isShiftDown) {
						selectedInternalNodes.clear();
						selectedEdges.clear();
					}
				} else if (startInternalNode == null) {
					if (!isShiftDown) {
						selectedInternalNodes.clear();
						selectedEdges.clear();
					}
					int x1 = (int) Math.min(edgeStart.getX(), edgeEnd.getX());
					int y1 = (int) Math.min(edgeStart.getY(), edgeEnd.getY());
					int x2 = (int) Math.max(edgeStart.getX(), edgeEnd.getX());
					int y2 = (int) Math.max(edgeStart.getY(), edgeEnd.getY());
					for (InternalNode temp : graph.keySet()) {
						if ((x1 <= (temp.getX() + temp.getWidth() / 2))
								&& (x2 >= (temp.getX() + temp.getWidth() / 2))
								&& (y1 <= (temp.getY() + temp.getHeight() / 2))
								&& (y2 >= (temp.getY() + temp.getHeight() / 2))) {
							if (!selectedInternalNodes.contains(temp)) {
								selectedInternalNodes.add(temp);
							}
							for (InternalNode neighbour : graph
									.getNeighbours(temp)) {
								if ((x1 <= (neighbour.getX() + neighbour
										.getWidth() / 2))
										&& (x2 >= (neighbour.getX() + neighbour
												.getWidth() / 2))
										&& (y1 <= (neighbour.getY() + neighbour
												.getHeight() / 2))
										&& (y2 >= (neighbour.getY() + temp
												.getHeight() / 2))) {
									if (!selectedEdges.containsKey(temp)) {
										selectedEdges
												.put(temp,
														new HashMap<InternalNode, List<Edge>>());
									}
									if (!selectedEdges.get(temp).containsKey(
											neighbour)) {
										selectedEdges.get(temp).put(neighbour,
												new LinkedList<Edge>());
									}
									for (Edge edge : graph.get(temp).get(
											neighbour)) {
										selectedEdges.get(temp).get(neighbour)
												.add(edge);
									}
								}
							}
						}
					}
				} else {
					if (!isShiftDown) {
						selectedInternalNodes.clear();
						selectedEdges.clear();
					}
				}
			}
		}
		repaint();
	}

	private void processSingleClick() {

		requestFocusInWindow();
		requestFocus();

		if (rightClick)
			return;
		edgeEnd = new Point();
		edgeEnd.setLocation(
				(int) ((lastEvent.getX() - offset.getX()) / scale - center
						.getX() * (1 / scale - 1)),
				(int) ((lastEvent.getY() - offset.getY()) / scale - center
						.getY() * (1 / scale - 1)));
		if (!rightClick && (mode == PLAIN_MODE)) {
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			if (!moveOn) {
				endNode = graph.nodeContains(edgeEnd, DEPS);
				if ((Math.abs(edgeStart.getX() - edgeEnd.getX()) < NODE_EPS)
						&& (Math.abs(edgeStart.getY() - edgeEnd.getY()) < NODE_EPS)
						&& (startInternalNode == null)) {
					EdgePair edgePair = graph.edgeContains(edgeEnd, EPS,
							EDGE_EPS);
					if (edgePair == null) {
						edgePair = graph.loopContains(edgeEnd, LOOP_EPS);
					}
					if (edgePair == null) {
						if (!(selectedInternalNodes.isEmpty()
								&& selectedEdges.isEmpty() || isShiftDown)) {
							// ///1111////
							selectedInternalNodes.clear();
							selectedEdges.clear();
						}
					}
				} else if ((startInternalNode != null)
						&& startInternalNode.equals(endNode) && !wasNeedEdge) {
					if (!selectedInternalNodes.contains(startInternalNode)) {
						// //1111////
						if (!isShiftDown) {
							selectedInternalNodes.clear();
							selectedEdges.clear();
						}
						selectedInternalNodes.add(startInternalNode);
					}
				}
			} else {
				// //111/////
				moveOn = false;
			}
		}
		repaint();
	}

	public void mousePressed(MouseEvent event) {
		if (white)
			return;

		needRect = false;
		needEdge = false;
		needLoop = false;

		wasNeedEdge = false;
		lastEvent = event;
		if ((event.getModifiersEx() & InputEvent.BUTTON3_DOWN_MASK) == 0) {
			if (mode == PLAIN_MODE) {
				rightClick = false;
				edgeStart = event.getPoint();
				edgeStart.setLocation(
						(int) ((event.getX() - offset.getX()) / scale - center
								.getX() * (1 / scale - 1)),
						(int) ((event.getY() - offset.getY()) / scale - center
								.getY() * (1 / scale - 1)));
				edgeEnd = event.getPoint();
				// startInternalNode = graph.nodeContains(new
				// Point((int)(edgeStart.getX() / scale - center.getX() * (1 /
				// scale - 1) - offset.getX()), (int)(edgeStart.getY() / scale -
				// center.getY() * (1 / scale - 1) - offset.getY())), DEPS);
				if (mode == PLAIN_MODE) {
					startInternalNode = graph.nodeContains(edgeStart, DEPS);
				}
				edgeEnd.setLocation(edgeStart.getX(), edgeStart.getY());
			} else if (mode == MOVE_MODE) {
				lastPoint = event.getPoint();
			}
		} else if ((event.getModifiersEx() & InputEvent.BUTTON3_DOWN_MASK) != 0) {
			rightClick = true;
		}
	}

	public void mouseReleased(MouseEvent event) {
		if (white)
			return;
		lastEvent = event;

		needRect = false;
		needEdge = false;
		needLoop = false;

		if (startInternalNode == null) {
			if (isClickOnEdge(event.getPoint())) {
				if (singleClick) {
					timer.stop();
					if (selectedEdges.size() == 0) {
						doSelection();
					}
					singleClick = false;
					return;
				} else {
					doSelection();
					singleClick = true;
					timer.start();
					return;
				}

			} else {
				processSingleClick();
				singleClick = false;
				return;
			}
		}

		if ((Math.abs(edgeStart.getX() + offset.getX() - event.getX()) > EPS_DOUBLE_CLICK || (Math
				.abs(edgeStart.getY() + offset.getY() - event.getY()) > EPS_DOUBLE_CLICK))) {
			processSingleClick();
			singleClick = false;
			return;
		}

		if (singleClick)

		{
			timer.stop();
			if (selectedInternalNodes.size() == 0) {
				doSelection();
			}
			singleClick = false;
			return;
		}

		doSelection();

		if (singleClick && selectedInternalNodes.size() == 0)

		{
			selectedInternalNodes = new HashSet<InternalNode>();
			for (InternalNode internalNode : selectedNodesCopies) {
				selectedInternalNodes.add(internalNode);
			}
			selectedEdges = new HashMap<InternalNode, Map<InternalNode, List<Edge>>>();
			for (InternalNode internalNode : selectedEdgesCopy.keySet()) {
				selectedEdges.put(internalNode,
						selectedEdgesCopy.get(internalNode));
			}

			processSingleClick();
			singleClick = false;
			return;
		}

		singleClick = !singleClick;
		timer.start();
	}

	private boolean isClickOnEdge(Point point) {
		return graph.edgeContains(point, EPS, EDGE_EPS) != null
				|| graph.loopContains(point, LOOP_EPS) != null;
	}

	public void mouseClicked(MouseEvent me) {
	}

	public void mouseEntered(MouseEvent me) {
	}

	public void mouseExited(MouseEvent me) {
	}

	public void mouseMoved(MouseEvent me) {
	}

	public void mouseDragged(MouseEvent event) {
		if (white)
			return;
		if (rightClick) {
			return;
		}
		if (mode == PLAIN_MODE) {
			if ((startInternalNode != null)
					&& selectedInternalNodes.contains(startInternalNode)) {
				setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
				moveOn = true;
				for (InternalNode temp : selectedInternalNodes) {
					temp.setX((int) (temp.getX() + ((event.getX() - offset
							.getX()) / scale - center.getX() * (1 / scale - 1) - edgeEnd
							.getX())));
					temp.setY((int) (temp.getY() + ((event.getY() - offset
							.getY()) / scale - center.getY() * (1 / scale - 1) - edgeEnd
							.getY())));
				}
				edgeEnd.setLocation(
						(int) ((event.getX() - offset.getX()) / scale - center
								.getX() * (1 / scale - 1)),
						(int) ((event.getY() - offset.getY()) / scale - center
								.getY() * (1 / scale - 1)));
			}
		}
		repaint();
		wasNeedEdge = wasNeedEdge || needEdge;
	}

	public void selectEdge(String nodeName) {
		for (InternalNode node : graph.keySet()) {
			for (InternalNode neighbour : graph.getNeighbours(node)) {
				for (Edge edge : graph.get(node).get(neighbour)) {
					if (node.getLabel().equals(nodeName)
							|| neighbour.getLabel().equals(nodeName)) {
						if (!selectedEdges.containsKey(node)) {
							selectedEdges.put(node,
									new HashMap<InternalNode, List<Edge>>());
						}
						if (!selectedEdges.get(node).containsKey(neighbour)) {
							selectedEdges.get(node).put(neighbour,
									new LinkedList<Edge>());
						}
						selectedEdges.get(node).get(neighbour).add(edge);
					}
				}
			}
		}
		repaint();
	}

	public void selectEdgeBySource(String sourceNodeName) {
		for (InternalNode node : graph.keySet()) {
			if (node.getLabel().equals(sourceNodeName)) {
				for (InternalNode neighbour : graph.getNeighbours(node)) {
					for (Edge edge : graph.get(node).get(neighbour)) {
						if (!selectedEdges.containsKey(node)) {
							selectedEdges.put(node,
									new HashMap<InternalNode, List<Edge>>());
						}
						if (!selectedEdges.get(node).containsKey(neighbour)) {
							selectedEdges.get(node).put(neighbour,
									new LinkedList<Edge>());
						}
						selectedEdges.get(node).get(neighbour).add(edge);
					}
				}
			}
		}
		repaint();
	}

	public void selectEdgeByDestination(String destinationNodeName) {
		for (InternalNode node : graph.keySet()) {
			for (InternalNode neighbour : graph.getNeighbours(node)) {
				for (Edge edge : graph.get(node).get(neighbour)) {
					if (neighbour.getLabel().equals(destinationNodeName)) {
						if (!selectedEdges.containsKey(node)) {
							selectedEdges.put(node,
									new HashMap<InternalNode, List<Edge>>());
						}
						if (!selectedEdges.get(node).containsKey(neighbour)) {
							selectedEdges.get(node).put(neighbour,
									new LinkedList<Edge>());
						}
						selectedEdges.get(node).get(neighbour).add(edge);
					}
				}
			}
		}
		repaint();
	}

	public void DeselectAll() {
		selectedEdges.clear();
		selectedInternalNodes.clear();
		repaint();
	}

	public void selectNode(String nodeName) {
		for (InternalNode node : graph.keySet()) {
			if (node.getLabel().equals(nodeName)
					&& !selectedInternalNodes.contains(node)) {
				selectedInternalNodes.add(node);
			}
		}
		repaint();
	}

	public void selectEdge(String startNode, String endNode) {
		for (InternalNode node : graph.keySet()) {
			for (InternalNode neighbour : graph.getNeighbours(node)) {
				for (Edge edge : graph.get(node).get(neighbour)) {
					if (node.getLabel().equals(startNode)
							&& neighbour.getLabel().equals(endNode)) {
						if (!selectedEdges.containsKey(node)) {
							selectedEdges.put(node,
									new HashMap<InternalNode, List<Edge>>());
						}
						if (!selectedEdges.get(node).containsKey(neighbour)) {
							selectedEdges.get(node).put(neighbour,
									new LinkedList<Edge>());
						}
						selectedEdges.get(node).get(neighbour).add(edge);
					}
				}
			}
		}
		repaint();
	}

	private class KeyHandler extends KeyAdapter {

		public void keyPressed(KeyEvent event) {
			isShiftDown = event.getKeyCode() == KeyEvent.VK_SHIFT;
			if (event.getKeyCode() == KeyEvent.VK_DELETE) {
				if (!selectedEdges.isEmpty()) {
					for (InternalNode temp : selectedEdges.keySet()) {
						for (InternalNode neighbour : selectedEdges.get(temp)
								.keySet()) {
							for (Iterator edge = graph.get(temp).get(neighbour)
									.iterator(); edge.hasNext();) {
								edge.next();
								edge.remove();
							}
						}
					}
					selectedEdges.clear();
				}
				if (!selectedInternalNodes.isEmpty()) {
					for (InternalNode temp : selectedInternalNodes) {
						graph.removeNode(temp);
					}
					selectedInternalNodes.clear();
					int startNode = -1;
					boolean start = false;
					for (int i = 0; i < graph.size(); i++) {
						if (((InternalNode) graph.keySet().toArray()[i]).isStart) {
							start = true;
						}
						if (!start && startNode == -1) {
							startNode = ((InternalNode) graph.keySet()
									.toArray()[i]).getId();
						}
					}
					if (!start && startNode >= 0) {
						graph.getNode(startNode).setIsStart(true);
					}
				}
			}
			repaint();
		}

		public void keyReleased(KeyEvent event) {
			isShiftDown = false;
		}
	}

	public int getMode() {
		return this.mode;
	}

	public void setBestScaleAndOffset() {
		if (!graph.isEmpty()) {
			InternalNode leftInternalNode = null, rightNode = null, topNode = null, bottomNode = null;
			for (InternalNode internalNode : graph.keySet()) {
				if ((leftInternalNode == null)
						|| (internalNode.getX() < leftInternalNode.getX())) {
					leftInternalNode = internalNode;
				}
				if ((rightNode == null)
						|| ((internalNode.getX() + internalNode.getWidth()) > (rightNode
								.getX() + rightNode.getWidth()))) {
					rightNode = internalNode;
				}
				if ((topNode == null) || (internalNode.getY() < topNode.getY())) {
					topNode = internalNode;
				}
				if ((bottomNode == null)
						|| ((internalNode.getY() + internalNode.getHeight()) > (bottomNode
								.getY() + bottomNode.getHeight()))) {
					bottomNode = internalNode;
				}
			}
			if (!(((rightNode.getX() + rightNode.getWidth() - leftInternalNode
					.getX()) * scale < w) && ((bottomNode.getY()
					+ bottomNode.getHeight() - topNode.getY())
					* scale < h))) {
				scale = Math
						.min((float) (w - 8)
								/ (rightNode.getX() + rightNode.getWidth() - leftInternalNode
										.getX()),
								(float) (h - 8)
										/ (bottomNode.getY()
												+ bottomNode.getHeight() - topNode
													.getY()));
			}
			offset.setLocation(
					(int) ((w - (rightNode.getX() + rightNode.getWidth() - leftInternalNode
							.getX()) * scale) / 2 - (center.getX() + (leftInternalNode
							.getX() - center.getX()) * scale)),
					(int) ((h - (bottomNode.getY() + bottomNode.getHeight() - topNode
							.getY()) * scale) / 2 - (center.getY() + (topNode
							.getY() - center.getY()) * scale)));
			repaint();
		}
	}

	private Graph createGraph(Automaton a, int width, int height) {
		if (a == null) {
			return createNullGraph(width, height);
		}
		Graph g = new Graph();
		int n = a.getNumberStates();
		double alpha = 0;
		double dalpha = 2 * Math.PI / n;
		int R = 2 * Math.min(width, height) / 5;
		for (int i = 0; i < n; i++) {
			int x = (int) (width / 2 + R * Math.sin(alpha));
			int y = (int) (height / 2 - R * Math.cos(alpha));
			g.addNode(new EllipseNode(i, a.getStateString(i), x, y, i == a
					.getInitialState()));
			alpha += dalpha;
		}
		for (int i = 0; i < a.getNumberStates(); i++) {
			Automaton.Transition t = a.getTransition(i, 1);
			if ((t != null) && (t.getEndState() != -1)) {
				if (t.getEndState() == i) {
					g.addEdge(g.getNode(i), g.getNode(i),
							new Loop("T, " + t.toString()));
				} else {
					g.addEdge(g.getNode(i), g.getNode(t.getEndState()),
							new Edge("T, " + t.toString()));
				}
			}
			t = a.getTransition(i, 0);
			if ((t != null) && (t.getEndState() != -1)) {
				if (t.getEndState() == i) {
					g.addEdge(g.getNode(i), g.getNode(i),
							new Loop("F, " + t.toString()));
				} else {
					g.addEdge(g.getNode(i), g.getNode(t.getEndState()),
							new Edge("F, " + t.toString()));
				}
			}
		}
		return g;
	}

	private Graph createNullGraph(int width, int height) {
		Graph g = new Graph();
		g.addNode(new EllipseNode(-1, "X", width / 2, height / 2, false));
		return g;
	}
}

