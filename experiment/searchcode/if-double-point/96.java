package uk.ac.lkl.migen.mockup.polydials.ui;

import java.awt.*;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import uk.ac.lkl.common.util.event.UpdateEvent;
import uk.ac.lkl.common.util.event.UpdateListener;

import uk.ac.lkl.migen.mockup.polydials.model.ModuloCounter;
import uk.ac.lkl.migen.mockup.polydials.model.StepwiseAdjustable;

/**
 * A view of a counter. Shows a single hand.
 * 
 * <p>
 * <b>Improvement:</b> Allow a dial to have multiple hands. This information
 * would have to be encoded in the model rather than just at the model level
 * here.
 * </p>
 * 
 * @author $Author: toontalk@gmail.com $
 * @version $Revision: 2830 $
 * @version $Date: 2009-06-16 08:36:24 +0200 (Tue, 16 Jun 2009) $
 * 
 */
public class Dial extends SelectableComponent {

    // hack: coincides with grey used in color chooser
    private static final Color DEFAULT_FILL_COLOR = new Color(204, 204, 204);

    private ModuloCounter counter;

    private PolyDialsCanvas dialCanvas;

    private Color fillColor;

    // the size of this dial
    private int size;

    public Dial(PolyDialsCanvas dialCanvas, ModuloCounter counter) {
	setLayout(new BorderLayout());
	this.dialCanvas = dialCanvas;
	this.counter = counter;

	setDialSize(150);
	setSelected(false);
	setOpaque(false);
	setFillColor(DEFAULT_FILL_COLOR);

	this.counter
		.addUpdateListener(new UpdateListener<StepwiseAdjustable>() {

		    public void objectUpdated(UpdateEvent<StepwiseAdjustable> e) {
			repaint();
		    }
		});

	// JPanel flowPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
	// flowPanel.setOpaque(false);
	// JButton button = new JButton("e");
	// button.addActionListener(new ActionListener() {
	// public void actionPerformed(ActionEvent e) {
	// setSize(100, 100);
	// }
	// });
	// button.setOpaque(false);
	// flowPanel.add(button);
	// add(flowPanel, BorderLayout.CENTER);

	// addListeners();
    }

    @SuppressWarnings("unused")
    private void addListeners() {
	addMouseMotionListener();
    }

    private int[] cursorIds =
	    new int[] { Cursor.E_RESIZE_CURSOR, Cursor.SE_RESIZE_CURSOR,
		    Cursor.S_RESIZE_CURSOR, Cursor.SW_RESIZE_CURSOR,
		    Cursor.W_RESIZE_CURSOR, Cursor.NW_RESIZE_CURSOR,
		    Cursor.N_RESIZE_CURSOR, Cursor.NE_RESIZE_CURSOR };

    private Cursor[] cursors;
    private Cursor defaultCursor;
    private Cursor moveCursor;

    private void prepareCursors() {
	cursors = new Cursor[cursorIds.length];
	for (int i = 0; i < cursors.length; i++)
	    cursors[i] = new Cursor(cursorIds[i]);
	defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);
	moveCursor = new Cursor(Cursor.MOVE_CURSOR);
    }

    public int getDialSize() {
	return size;
    }

    public void setDialSize(int size) {
	if (size < 80)
	    size = 80;
	this.size = size;
	setSize(size, size);
	setPreferredSize(new Dimension(size, size));
//	validate();
//	Component parent = getParent();
//	if (parent != null)
//	    parent.validate();
//	repaint();
    }

    private void addMouseMotionListener() {
	prepareCursors();

	addMouseListener(new MouseAdapter() {
	    public void mousePressed(MouseEvent e) {

	    }
	});

	addMouseMotionListener(new MouseMotionAdapter() {

	    public void mouseMoved(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		Dimension size = getSize();
		int centerX = size.width / 2;
		int centerY = size.height / 2;
		int xOffset = x - centerX;
		int yOffset = y - centerY;
		double distance = Point.distance(0, 0, xOffset, yOffset);

		// repeated code
		int outerDiameter = Math.min(size.width, size.height) - 10;
		int outerRadius = outerDiameter / 2;

		if (Math.abs(outerRadius - distance) > 20) {
		    Cursor cursor =
			    distance > outerRadius ? defaultCursor : moveCursor;
		    setCursor(cursor);
		    return;
		}

		double rads = Math.atan2(yOffset, xOffset);
		double degs = Math.toDegrees(rads);
		degs += 22.5;
		if (degs < 0)
		    degs += 360;
		degs /= 45;
		int cursorIndex = (int) degs;
		setCursor(cursors[cursorIndex]);
	    }
	});
    }

    public void setFillColor(Color fillColor) {
	this.fillColor = fillColor;
	repaint();
    }

    public Color getFillColor() {
	return fillColor;
    }

    public void delete() {
	dialCanvas.getModel().removeCounter(counter);
    }

    public ModuloCounter getCounter() {
	return counter;
    }

    // convenience method
    public void setModulus(int modulus) {
	counter.setModulus(modulus);
    }

    // convenience method
    public int getModulus() {
	return counter.getModulus();
    }

    public void paintComponent(Graphics g) {
	((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
		RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		RenderingHints.VALUE_ANTIALIAS_ON);
	super.paintComponent(g);
	paintDial((Graphics2D) g.create());
	paintHand((Graphics2D) g.create());
    }

    // repeated code with paintDial -- some of this should be cached
    public boolean encloses(Point point) {
	if (!contains(point))
	    return false;

	Dimension size = getSize();
	int centerX = size.width / 2;
	int centerY = size.height / 2;

	// todo: cache this
	Point centerPoint = new Point(centerX, centerY);

	double distance = point.distance(centerPoint);

	int outerDiameter = Math.min(size.width, size.height) - 10;
	int outerRadius = outerDiameter / 2;

	return distance <= outerRadius;
    }

    private void paintDial(Graphics2D g2) {
	Dimension size = getSize();
	int outerDiameter = Math.min(size.width, size.height) - 10;
	int outerRadius = outerDiameter / 2;
	if (isSelected()) {
	    Stroke originalStroke = g2.getStroke();
	    g2.setStroke(new BasicStroke(10.0f));
	    g2.setColor(Color.YELLOW);
	    g2.drawOval(size.width / 2 - outerRadius, size.height / 2
		    - outerRadius, outerDiameter, outerDiameter);
	    g2.setStroke(originalStroke);
	}

	g2.setColor(fillColor);
	g2.fillOval(size.width / 2 - outerRadius,
		size.height / 2 - outerRadius, outerDiameter, outerDiameter);

	g2.setColor(Color.BLACK);

	g2.drawOval(size.width / 2 - outerRadius,
		size.height / 2 - outerRadius, outerDiameter, outerDiameter);

	// hack for digit radius
	int digitRadius = outerRadius - 10;

	double angleStep = 2 * Math.PI / counter.getModulus();
	double angle = Math.toRadians(270);
	FontMetrics metrics = g2.getFontMetrics();
	int height = metrics.getAscent();
	for (int i = 0; i < counter.getModulus(); i++) {
	    angle = Math.toRadians(270) + i * angleStep; // don't do
	    // incrementally
	    // (avoids fp
	    // errors)
	    int x = size.width / 2 + (int) (digitRadius * Math.cos(angle));
	    int y = size.height / 2 + (int) (digitRadius * Math.sin(angle));

	    String label = Integer.toString(i);

	    int width = metrics.stringWidth(label);

	    g2.drawString(label, x - width / 2, y + height / 2);
	}
	g2.fillOval(size.width / 2 - 2, size.height / 2 - 2, 4, 4);
    }

    private void paintHand(Graphics2D g2) {
	Dimension size = getSize();
	int outerDiameter = Math.min(size.width, size.height) - 10;
	int outerRadius = outerDiameter / 2;

	// hack for handle radius
	int handleRadius = outerRadius - 20;

	double angleStep = 2 * Math.PI / counter.getModulus();
	double angle = Math.toRadians(270);
	angle += angleStep * counter.getValue();

	// subtract i * 30 so can see multiple hands at same position
	int x = size.width / 2 + (int) ((handleRadius) * Math.cos(angle));
	int y = size.height / 2 + (int) ((handleRadius) * Math.sin(angle));

	int midX1 =
		size.width
			/ 2
			+ (int) (handleRadius / 2 * Math.cos(angle
				- Math.toRadians(10)));
	int midY1 =
		size.height
			/ 2
			+ (int) (handleRadius / 2 * Math.sin(angle
				- Math.toRadians(10)));
	g2.drawLine(size.width / 2, size.height / 2, midX1, midY1);
	g2.drawLine(midX1, midY1, x, y);

	int midX2 =
		size.width
			/ 2
			+ (int) (handleRadius / 2 * Math.cos(angle
				+ Math.toRadians(10)));
	int midY2 =
		size.height
			/ 2
			+ (int) (handleRadius / 2 * Math.sin(angle
				+ Math.toRadians(10)));

	g2.drawLine(x, y, midX2, midY2);
	g2.drawLine(midX2, midY2, size.width / 2, size.height / 2);

    }

}

