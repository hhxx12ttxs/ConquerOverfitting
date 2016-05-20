package beast.graphics;

import beast.evolution.alignment.Alignment;
import beast.evolution.alignment.Sequence;
import beast.evolution.tree.Node;
import beast.evolution.tree.Tree;
import beast.evolution.tree.coalescent.IntervalType;
import beast.evolution.tree.coalescent.TreeIntervals;
import beast.util.TreeParser;
//import org.jtikz.TikzGraphics2D;

import javax.swing.*;


import java.awt.*;
import java.awt.geom.*;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;

/**
 * @author Alexei Drummond
 */
public class TreeComponent extends JComponent {

    RootedTreeDrawing treeDrawing;
    TreeDrawingOrientation orientation = TreeDrawingOrientation.UP;
    BranchStyle branchStyle = BranchStyle.SQUARE;
    NodeDecorator leafDecorator, internalNodeDecorator;
    NodePositioningRule positioningRule = NodePositioningRule.AVERAGE_OF_CHILDREN;

    NodeTimesDecorator leafTimesDecorator;
    NodeTimesDecorator internalNodeTimesDecorator;

    String caption = null;
    String colorTraitName = null;

    Tree tree;

    boolean positioned = false;

    // the position of the "current" leaf node
    private double p = 0;

    NumberFormat format = NumberFormat.getInstance();

    double rootHeightForScale;

    private Rectangle2D bounds = new Rectangle2D.Double(0, 0, 1, 1);

    static final Color[] traitColors = {Color.red, Color.blue, Color.green, Color.yellow, Color.orange, Color.magenta,
            Color.cyan, Color.gray, Color.darkGray, Color.lightGray, Color.black};
    private ColorTable traitColorTable = new ColorTable(Arrays.asList(traitColors));


    private class Location {
        int[] loc;

        public Location(int[] loc) {
            this.loc = new int[loc.length];

            for (int i = 0; i < loc.length; i++)
                this.loc[i] = loc[i];
        }

        @Override
        public boolean equals(Object object) {

            if (!(object instanceof Location))
                return false;

            Location otherLocation = (Location) object;

            if (loc.length != otherLocation.loc.length)
                return false;

            for (int i = 0; i < loc.length; i++)
                if (loc[i] != otherLocation.loc[i])
                    return false;

            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 83 * hash + Arrays.hashCode(this.loc);
            return hash;
        }

    }

    Map<Location, Integer> locationColours;
    int nextLocationColour;

    /**
     * @param treeDrawing the  tree drawing
     */
    public TreeComponent(RootedTreeDrawing treeDrawing) {

        format.setMaximumFractionDigits(5);

        //this.scalebar = scalebar;
        //this.isTriangle = isTriangle;

        this.treeDrawing = treeDrawing;
        this.tree = treeDrawing.getTree();

        rootHeightForScale = treeDrawing.getTree().getRoot().getHeight();

        locationColours = new HashMap<Location, Integer>();
        nextLocationColour = 0;
    }

    public void setBounds(Rectangle2D bounds) {
        this.bounds = bounds;
        setSize((int) bounds.getWidth(), (int) bounds.getHeight());
    }

    void setTipValues(Node node) {
        if (node.isLeaf()) {
            node.setMetaData("p", p);
            node.setMetaData("p_min", p);
            node.setMetaData("p_max", p);
            p += getCanonicalNodeSpacing(node.getTree());
        } else {
            double pmin = Double.MAX_VALUE;
            double pmax = Double.MIN_VALUE;
            for (Node childNode : node.getChildren()) {
                setTipValues(childNode);

                double cpmin = (Double) childNode.getMetaData("p_min");
                double cpmax = (Double) childNode.getMetaData("p_max");

                if (cpmin < pmin) pmin = cpmin;
                if (cpmax > pmax) pmax = cpmax;
            }
            node.setMetaData("p_min", pmin);
            node.setMetaData("p_max", pmax);
        }
    }

    
    void positionInternalNodes(Node node) {
        if (!node.isLeaf()) {
            if (positioningRule.getTraversalOrder() == NodePositioningRule.TraversalOrder.PRE_ORDER) {
                positioningRule.setPosition(node, "p");
            }
            for (Node child : node.getChildren()) {
                positionInternalNodes(child);
            }
            if (positioningRule.getTraversalOrder() == NodePositioningRule.TraversalOrder.POST_ORDER) {
                positioningRule.setPosition(node, "p");
            }

        }
    }

    void drawNode(Point2D p, Graphics2D g, NodeDecorator decorator) {

        double nodeSize = decorator.getNodeSize();

        Shape shape = null;
        double halfSize = nodeSize / 2.0;

        switch (decorator.getNodeShape()) {
            case circle:
                shape = new Ellipse2D.Double(p.getX() - halfSize, p.getY() - halfSize, nodeSize, nodeSize);
                break;
            case square:
                shape = new Rectangle2D.Double(p.getX() - halfSize, p.getY() - halfSize, nodeSize, nodeSize);
                break;
            case triangle:
                Path2D path = new Path2D.Double();
                path.moveTo(p.getX(), p.getY() - halfSize);
                path.lineTo(p.getX() + halfSize, p.getY() + halfSize);
                path.lineTo(p.getX() - halfSize, p.getY() + halfSize);
                path.closePath();
                shape = path;
            default:
        }
        Color oldColor = g.getColor();
        g.setColor(decorator.getNodeColor());
        g.fill(shape);
        g.setColor(oldColor);
        if (decorator.drawNodeShape()) {
            g.draw(shape);
        }

        //g.setFont(oldFont);
    }

    void drawCanonicalString(String string, double x, double y, Object anchor, Object fontSize, SmartGraphics2D g) {

        Point2D p = getTransformedPoint2D(new Point2D.Double(x, y));

        treeDrawing.drawString(string, p.getX(), p.getY(), anchor, fontSize, g);
    }

    private boolean isDrawingBranchLabels(RootedTreeDrawing treeDrawing) {
        return treeDrawing.getBranchLabels() != null && !treeDrawing.getBranchLabels().equals("");
    }

    private Point2D getCanonicalNodePoint2D(Node node) {
        return new Point2D.Double(getCanonicalNodeX(node), getCanonicalNodeY(node.getHeight()));
    }

    private double getCanonicalNodeX(Node node) {
        return (Double) node.getMetaData("p");
    }

    private double getCanonicalNodeY(double height) {

        double h = height / rootHeightForScale;

        if (treeDrawing.isRootAligned()) {
            h = h + (1.0 - (tree.getRoot().getHeight() / rootHeightForScale));
        }

        return h;
    }

    private double getCanonicalNodeSpacing(Tree tree) {
        return 1.0 / (tree.getLeafNodeCount() - 1);
    }

    private Point2D getTransformedNodePoint2D(Node node) {
        return getTransformedPoint2D(getCanonicalNodePoint2D(node));
    }

    private Point2D getTransformedPoint2D(Point2D canonicalPoint2D) {
        return orientation.getTransform(bounds).transform(canonicalPoint2D, null);
    }

    final void drawBranch(Node node, Node childNode, Graphics2D g) {

        if (colorTraitName != null) {
            int childColorIndex = getIntegerTrait(childNode, colorTraitName);

            int parentColorIndex = getIntegerTrait(node, colorTraitName);

            if (childColorIndex == parentColorIndex && node.getChildCount() == 1) {
                System.out.println("Parent and single child have same state!!");
                drawNode(getTransformedNodePoint2D(node), g, NodeDecorator.BLACK_DOT);
            }

            g.setColor(traitColorTable.getColor(childColorIndex));
        }

        Shape shape = branchStyle.getBranchShape(getCanonicalNodePoint2D(childNode), getCanonicalNodePoint2D(node));
        Shape transformed = orientation.getTransform(bounds).createTransformedShape(shape);

        g.draw(transformed);
    }

    private int getIntegerTrait(Node childNode, String traitName) {
        Object trait = childNode.getMetaData(traitName);
        if (trait instanceof Integer) return (Integer) trait;
        if (trait instanceof Double) return (int) Math.round((Double) trait);
        if (trait instanceof String) return (int) Math.round(Double.parseDouble((String) trait));

        if (trait instanceof int[]) {
            Location location = new Location((int[]) trait);
            if (locationColours.containsKey(location))
                return locationColours.get(location);
            else {
                locationColours.put(location, nextLocationColour);
                return nextLocationColour++;
            }
        }

        return -1;
    }


    /**
     * Draws the label of a particular branch along the branch
     *
     * @param label     the label text
     * @param tree      the tree
     * @param node      the parent node
     * @param childNode the child node
     * @param fontSize  a hint about font size
     * @param g         the graphics object to draw to
     */
    final void drawBranchLabel(String label, Tree tree, Node node, Node childNode, Object fontSize, Graphics2D g) {

        Point2D p = getTransformedPoint2D(branchStyle.getCanonicalBranchLabelPoint2D(getCanonicalNodePoint2D(childNode), getCanonicalNodePoint2D(node)));

        treeDrawing.drawString(label, p.getX(), p.getY(), orientation.getBranchLabelAnchor(), fontSize, g);
    }

    final void drawLeafLabel(Node node, Graphics2D g) {

        Point2D nodePoint = getTransformedNodePoint2D(node);

        treeDrawing.drawString(node.getID(), nodePoint.getX(), nodePoint.getY(), orientation.getLeafLabelAnchor(), TikzRenderingHints.VALUE_normalsize, g);
    }

    /**
     * Draws the tree
     *
     * @param treeDrawing
     * @param node
     * @param g
     */
    void draw(RootedTreeDrawing treeDrawing, Node node, Graphics2D g) {

        Tree tree = treeDrawing.getTree();

        if ((internalNodeTimesDecorator != null || leafTimesDecorator != null) && node.isRoot()) {
            drawNodeTimes(treeDrawing.getTreeIntervals(), g);
        }

        g.setStroke(new BasicStroke((float) treeDrawing.getLineThickness()));

        p = 0.0; // canonical positioning goes from 0 to 1.

        if (node.isRoot() && !positioned) {
            setTipValues(node);
            positionInternalNodes(node);
            positioned = true;
        }

        if (node.isLeaf()) {
            if (treeDrawing.showLeafLabels()) {
                drawLeafLabel(node, g);
            }
        } else {

            for (Node childNode : node.getChildren()) {
                draw(treeDrawing, childNode, g);
            }

            for (Node childNode : node.getChildren()) {

                drawBranch(node, childNode, g);
                if (isDrawingBranchLabels(treeDrawing)) {
                    Object metaData = childNode.getMetaData(treeDrawing.getBranchLabels());
                    String branchLabel;
                    if (metaData instanceof Number) {
                        branchLabel = format.format(metaData);
                    } else {
                        branchLabel = metaData.toString();
                    }
                    drawBranchLabel(branchLabel, tree, node, childNode, TikzRenderingHints.VALUE_scriptsize, g);
                }
            }
        }

        // finally draw all the node decorations
        if (node.isRoot()) {
            if (leafDecorator != null) {
                List<Node> nodes = tree.getExternalNodes();
                for (Node leaf : nodes) {
                    drawNode(getTransformedPoint2D(getCanonicalNodePoint2D(leaf)), g, leafDecorator);
                }
            }
            if (internalNodeDecorator != null) {
                List<Node> internalNodes = tree.getInternalNodes();
                for (Node internalNode : internalNodes) {
                    drawNode(getTransformedPoint2D(getCanonicalNodePoint2D(internalNode)), g, internalNodeDecorator);
                }
            }
        }
    }

    private void drawNodeTimes(TreeIntervals treeIntervals, Graphics2D g) {

        Tree tree = treeIntervals.treeInput.get();
        Stroke s = g.getStroke();
        g.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10, new float[]{1.0f, 1.0f}, 0));

        double unscaledHeight = 0.0;

        double p1 = -0.1;
        double p2 = 1.1;

        String label = format.format(unscaledHeight);
        IntervalType oldIntervalType = IntervalType.SAMPLE;
        IntervalType newIntervalType;
        if (internalNodeTimesDecorator != null) internalNodeTimesDecorator.resetCurrent();
        if (leafTimesDecorator != null) leafTimesDecorator.resetCurrent();
        NodeTimesDecorator decorator = leafTimesDecorator;

        drawNodeTime(label, decorator, getCanonicalNodeY(unscaledHeight), p1, p2, g);
        for (int i = 0; i < treeIntervals.getIntervalCount(); i++) {

            double interval = treeIntervals.getInterval(i);
            newIntervalType = treeIntervals.getIntervalType(i);

            unscaledHeight += interval;

            if (newIntervalType == IntervalType.SAMPLE) {
                decorator = leafTimesDecorator;
            } else if (newIntervalType == IntervalType.COALESCENT) {
                decorator = internalNodeTimesDecorator;
            }

            if (interval > 0.0 || (newIntervalType != oldIntervalType)) {
                label = format.format(unscaledHeight);
                drawNodeTime(label, decorator, getCanonicalNodeY(unscaledHeight), p1, p2, g);
            }
            oldIntervalType = newIntervalType;
        }
        g.setStroke(s);
    }

    void drawNodeTime(String label, NodeTimesDecorator decorator, double canonicalHeight, double pos1, double pos2, Graphics2D g) {
        if (decorator != null) {
            Point2D p1 = getTransformedPoint2D(new Point2D.Double(pos1, canonicalHeight));
            Point2D p2 = getTransformedPoint2D(new Point2D.Double(pos2, canonicalHeight));

            if (decorator.showNodeTimeLines()) {
                g.draw(new Line2D.Double(p1, p2));
            }
            if (decorator.showNodeTimeLabels()) {
                String nodeTimeLabel = decorator.getCurrentLabel(label);
                treeDrawing.drawString(nodeTimeLabel, p2.getX(), p2.getY(), orientation.getNodeHeightLabelAnchor(),
                        decorator.getNodeTimeLabelFontSize().getTikzRenderingHint(), g);
                decorator.incrementCurrent();
            }
        }
    }

    public void paintComponent(Graphics g) {
    	super.paintComponents(g);
    	setSize(500, 500);
		g.setClip(0,  0,  getWidth(), getHeight());
		if (treeDrawing.bgColorInput.get() != null) {
			Color oldBackground = ((Graphics2D) g).getBackground();
			((Graphics2D) g).setBackground(treeDrawing.bgColorInput.get());
			Rectangle r = g.getClipBounds();
			g.clearRect(r.x, r.y, r.width, r.height);
		}


        Graphics2D g2d;
        g2d = new SmartGraphics2D((Graphics2D) g);

        g.setColor(Color.black);
        Tree tree = treeDrawing.getTree();

        draw(treeDrawing, tree.getRoot(), g2d);
    }

    public static void main(String[] args) throws Exception {

        String newickTree = "((((1:0.1,2:0.1):0.1,3:0.2):0.1,4:0.3):0.1,5:0.4);";

        List<Sequence> sequences = new ArrayList<Sequence>();
        sequences.add(new Sequence("A", "A"));
        sequences.add(new Sequence("B", "A"));
        sequences.add(new Sequence("C", "A"));
        sequences.add(new Sequence("D", "A"));
        sequences.add(new Sequence("E", "A"));

        Alignment alignment = new Alignment(sequences, 4, "nucleotide");

        RootedTreeDrawing treeDrawing = new RootedTreeDrawing(new TreeParser(alignment, newickTree));
        treeDrawing.leafLabelOffsetInput.setValue(10.0, treeDrawing);
        treeDrawing.treeOrientationInput.setValue(RootedTreeDrawing.TreeOrientation.left, treeDrawing);
        treeDrawing.showLeafLabelsInput.setValue(true, treeDrawing);
        treeDrawing.initAndValidate();
        TreeComponent treeComponent = treeDrawing.treeComponent;


//        TikzGraphics2D tikzGraphics2D = new TikzGraphics2D();
//        treeComponent.paintComponent(tikzGraphics2D);
//        tikzGraphics2D.flush();

        JFrame frame = new JFrame();
        frame.getContentPane().add(treeComponent, BorderLayout.CENTER);
        frame.setSize(500, 500);

        treeComponent.setBounds(new Rectangle2D.Double(50, 50, 400, 400));

        frame.setVisible(true);
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public void setColorTraitName(String colorTraitName) {
        this.colorTraitName = colorTraitName;
    }

    public void setTraitColorTable(ColorTable colorTable) {
        this.traitColorTable = colorTable;
    }
}


