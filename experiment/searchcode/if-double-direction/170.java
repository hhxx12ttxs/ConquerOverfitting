package beast.graphics;

import beast.app.shell.Plot;
import beast.evolution.tree.Node;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JComponent;

/**
 * @author Alexei Drummond
 */
public class UnrootedTreeDrawing extends AbstractTreeDrawing {

    Rectangle2D bounds = new Rectangle2D.Double(0, 0, 1, 1);
    double minX = Double.MAX_VALUE;
    double minY = Double.MAX_VALUE;
    double maxX = Double.MIN_VALUE;
    double maxY = Double.MIN_VALUE;

    
    
    public void initAndValidate() {
    	if (Plot.studio != null) {
	    	final UnrootedTreeDrawing treeDrawing = this;
	    	JComponent p = new JComponent() {
	    		@Override
	    		public void paintComponent(Graphics g) {
		        	setSize(500, 500);
		    		g.setClip(0,  0,  getWidth(), getHeight());
		    		if (treeDrawing.bgColorInput.get() != null) {
		    			Color oldBackground = ((Graphics2D) g).getBackground();
		    			((Graphics2D) g).setBackground(treeDrawing.bgColorInput.get());
		    			Rectangle r = g.getClipBounds();
		    			g.clearRect(r.x, r.y, r.width, r.height);
		    		}
		    		
	    			super.paintComponents(g);

	    			SmartGraphics2D g2d = new SmartGraphics2D((Graphics2D) g);
	    			treeDrawing.paintTree(g2d);
	    		}
			};
			Plot.studio.plotPane.addChart(p);
    	}
    	setBounds(new Rectangle(50, 50, 400, 400));
    }

    void setTipValues(Node node) {

        if (node.isRoot()) {
            node.setMetaData("p", new Point2D.Double(0, 0));
            node.setMetaData("arc", 2 * Math.PI);
            node.setMetaData("direction", -Math.PI / 2.0);
        } else {

            Node parent = node.getParent();

            double direction = (Double) node.getMetaData("direction");

            double length = node.getLength();

            Point2D parentPoint2D = (Point2D) parent.getMetaData("p");

            double x = parentPoint2D.getX() + length * Math.cos(direction);
            double y = parentPoint2D.getY() + length * Math.sin(direction);

            if (x < minX) minX = x;
            if (x > maxX) maxX = x;
            if (y < minY) minY = y;
            if (y > maxY) maxY = y;

            node.setMetaData("p", new Point2D.Double(x, y));
        }

        double arc = (Double) node.getMetaData("arc");

        int leafNodeCount = node.getLeafNodeCount();

        double childDirection = (Double) node.getMetaData("direction");
        for (Node child : node.getChildren()) {
            int childLeafNodeCount = child.getLeafNodeCount();

            double childArc = arc * (double) childLeafNodeCount / (double) leafNodeCount;

            child.setMetaData("arc", childArc);
            child.setMetaData("direction", childDirection - childArc / 2);

            childDirection += childArc;
        }

        for (Node childNode : node.getChildren()) {
            setTipValues(childNode);
        }
    }

    @Override
    public void setBounds(Rectangle2D bounds) {
        this.bounds = bounds;
    }

    @Override
    public void paintTree(Graphics2D g) {
        paintNode(getTree().getRoot(), g);
    }

    private void paintNode(Node node, Graphics2D g) {
        if (node.isRoot()) {
            setTipValues(getTree().getRoot());

            double scaleX = bounds.getWidth() / (maxX - minX);
            double scaleY = bounds.getHeight() / (maxY - minY);

            double scale = Math.min(scaleX, scaleY);

            double tx = (bounds.getWidth() - (maxX - minX) * scale) / 2.0;
            double ty = (bounds.getHeight() - (maxY - minY) * scale) / 2.0;

            tx += bounds.getMinX() - (minX * scale);
            ty += bounds.getMinY() - (minY * scale);

            System.out.println("minX = " + minX);
            System.out.println("minY = " + minY);
            System.out.println("maxX = " + maxX);
            System.out.println("maxY = " + maxY);
            System.out.println("scaleX = " + scaleX);
            System.out.println("scaleY = " + scaleY);

            scaleAndTranslatePositions(getTree().getRoot(), scale, tx, ty);
            g.setStroke(new BasicStroke((float) getLineThickness()));
            g.setColor(Color.black);
        }

        if (node.isLeaf()) {
            drawLabel(node, g);
        } else {

            for (Node childNode : node.getChildren()) {
                paintNode(childNode, g);
            }
            for (Node childNode : node.getChildren()) {
                drawBranch(node, childNode, g);
            }
        }
    }

    private void scaleAndTranslatePositions(Node node, double scale, double tx, double ty) {
        Point2D p = (Point2D) node.getMetaData("p");

        node.setMetaData("p", new Point2D.Double(p.getX() * scale + tx, p.getY() * scale + ty));

        for (Node child : node.getChildren()) {
            scaleAndTranslatePositions(child, scale, tx, ty);
        }
    }

    void drawBranch(Node node, Node childNode, Graphics2D g) {

        Point2D pp = (Point2D) node.getMetaData("p");
        Point2D cp = (Point2D) childNode.getMetaData("p");

        g.draw(new Line2D.Double(pp, cp));
    }

    void drawLabel(Node node, Graphics2D g) {

        Point2D p = (Point2D) node.getMetaData("p");

        double theta = (Double) node.getMetaData("direction");

        double x = p.getX() + getLeafLabelOffset() * Math.cos(theta);
        double y = p.getY() + getLeafLabelOffset() * Math.sin(theta);

        drawString(node.getID(), (int) x, (int) y, TikzRenderingHints.VALUE_CENTER, TikzRenderingHints.VALUE_normalsize, g);
    }


    @Override
    public void setRootHeightForCanonicalScaling(double maxRootHeight) {
        //TODO not sure what exactly to do here
    }

}

