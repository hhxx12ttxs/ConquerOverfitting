package ecf3.model;

import ecf3.ECFUtil;
import ecf3.XMLParser;
import ecf3.ui.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import javax.swing.*;
// import javax.swing.tree.DefaultMutableTreeNode;

/**
 * <p>A <code>Path</code> connects two nodes. Paths are associated with
 * resource flows, signals or other variables. </p>
 * <p>
 * Resources flow in the direction indicated by the arrow. Resources may
 * be monetary or real. Many flows may be associated to one path. The flows
 * describe the kind of resource and the quantity.</p>
 * <img src="..\images\Paths.gif" alt="Paths">
 * <p>
 * Each path belongs to one model layer. It has two end points with connectors
 * that can be dragged by the mouse. They can be connected to a node if dragged
 * over that node and then released. The connection is done from the node side
 * via <A href="NodeConnectors.html">NodeConnectors</A>.
 * <p>
 * The <code>Path</code> class has only metods for disconnection flows: <br>
 * - <code>disconnectThis()</code> disconnects both ends (before deleting the flow path) <br>
 * - <code>translate(dx,dy)</code> disconnects the translated end via the
 * <A href="PathConnector.html">PathConnector</A> class.
 *
 * @version 2011-07-22 
 * Flow replaced by Variable whih may be flow, signal or other.
 */
public class Path implements ECFStructureObject {

    /** Show flow connectors */
    public static void ShowFlowConnectors() {
        fFlowConnectorsShown = true;
    } // ShowFlowConnectors
    /** Hide flow connectors */
    public static void HideFlowConnectors() {
        fFlowConnectorsShown = false;
    } // HideFlowConnectors
    /** Indicate if flow connectors are shown. */
    public static boolean fFlowConnectorsShown = true;
    /** The head end (with arrow) of a flow path. */
    public static int END_HEAD = 1;
    /** The tail end (with no arrow) of a flow path. */
    public static int END_TAIL = 2;
    /** Connector at the head end of the flowpath. */
    protected PathConnector conn1 = null;
    /** Connector at the tail end of the flowpath. */
    protected PathConnector conn2 = null;
    /** Variable of the path. */
    protected Variable var = null;
    /** Selected connector, if found, else <code>null</code>.*/
    protected PathConnector connSelected = null;
    /** Indicate if label is found. */
    protected boolean fLabelFound = false;
    /** Indicate if label is shown. */
    protected boolean fLabelShown = true;
    /** Indicate if flow path is selected. */
    protected boolean fPathSelected = false;
    /** The layer to which the flowpath belongs. */
    protected Layer layer;
    /** The model to which the flow path belongs. */
    protected Model model = Model.getInstance();
    /** Type of side to which the flowpath is connected on node at the head end,
     * see <code>Node</code>. */
    protected int sideNode1 = Node.NONE;
    /** Type of side to which the flowpath is connected on node at the tail end,
     * see <code>Node</code>. */
    protected int sideNode2 = Node.NONE;
    /** The name of the flowpath. */
//	protected String szName = "Flow";
    /** X-coordinate of the head end.*/
    protected int x1 = 0;
    /** X-coordinate of the head end.*/
    protected int x2 = 0;
    /** X-coordinate of center of label. */
    protected int xLabel = 0;
    /** Offset in x direction of label, relative to middle point */
    protected int xOffsetLabel = 0;
    /** X-coordinate of label offset before translating. */
    protected int xStart;
    /** Y-coordinate of the tail end.*/
    protected int y1 = 0;
    /** Y-coordinate of the tail end.*/
    protected int y2 = 0;
    /** Y-coordinate of center of label. */
    protected int yLabel = 0;
    /** Offset in y direction of label, relative to middle point */
    protected int yOffsetLabel = 0;
    /** Y-coordinate of label offset before translating. */
    protected int yStart;
    /** Label of the this flowpath. */
    protected GraphLabel label = new GraphLabel("Flow");
    /** Number of pixels for max error vidth. */
    protected int cpixelMaxArrowWidth = 15;

    
    /** Create an empty path for use when decoding XML code. */
    public Path() {
        setSide1(Node.NONE);
        setSide2(Node.NONE);
        conn1 = new PathConnector(this, 1, new Point(x1, y1));
        conn2 = new PathConnector(this, 2, new Point(x2, y2));
    }
    
    /**
     * Creates a path in the <code>layer</code> from point
     * <code>(x2,y2)</code> to point <code>(x1,y1)</code>.
     * @param layer The layer to which the path belongs.
     * @param x1 X coordinate of the head end.
     * @param y1 Y coordinate of the head end.
     * @param x2 X coordinate of the tail end.
     * @param y2 Y coordinate of the tail end.
     * @param var  The variable of the path.
     */
//    public Path(Layer layer, int x1, int y1, int x2, int y2, Variable var) {
//        this.layer = layer;
//        this.x1 = x1;
//        this.y1 = y1;
//        this.x2 = x2;
//        this.y2 = y2;
//        this.var = var;
//        model.addVariable(var);
//        setSide1(Node.NONE);
//        setSide2(Node.NONE);
//        conn1 = new PathConnector(this, 1, new Point(x1, y1));
//        conn2 = new PathConnector(this, 2, new Point(x2, y2));
//    } // Path
//
    /**
     * Creates a path in layer <code>ilayer</code> from point
     * <code>(x2,y2)</code> to point <code>(x1,y1)</code>.
     * The default variable is a flow.
     * @param layer The layer to which the path belongs.
     * @param x1 X coordinate of the head end.
     * @param y1 Y coordinate of the head end.
     * @param x2 X coordinate of the tail end.
     * @param y2 Y coordinate of the tail end.
     */
    public Path(Layer layer, int x1, int y1, int x2, int y2) {
        this.layer = layer;
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        // The default variable is flow.
        //model.addFlow(new Flow(layer));
        setSide1(Node.NONE);
        setSide2(Node.NONE);
        conn1 = new PathConnector(this, 1, new Point(x1, y1));
        conn2 = new PathConnector(this, 2, new Point(x2, y2));
    } // Path

    /**
     * Creates a vertical path of length 100 in layer <code>ilayer</code>
     * from point <code>(x1,y1+100)</code> to point <code>(x1,y1)</code>.
     * @param layer The layer to which the path belongs.
     * @param x1 X coordinate of the head end.
     * @param y1 Y coordinate of the head end.
     */
    public Path(Layer layer, int x1, int y1) {
        this(layer, x1, y1, x1, y1 + 100);
    } // Path

    /**
     * Creates a vertical path of length 100 in layer <code>ilayer</code>
     * from point <code>(x1,y1+100)</code> to point <code>(x1,y1)</code>.
     * @param layer The layer to which the path belongs.
     * @param x1 X coordinate of the head end.
     * @param y1 Y coordinate of the head end.
     * @param var The variable to be put into the path.
     *
     */
//    public Path(Layer layer, int x1, int y1, Variable var) {
//        this(layer, x1, y1, x1, y1 + 100, var);
//    } // Path
//
    /**
     * Creates a vertical path of length 100 in layer 0
     * from point <code>(10,110)</code> to point <code>(10,10)</code>.
     * @param layer The layer of the path.
     * @param var The varoiable to be put into the path.
     */
//    public Path(Layer layer, Variable var) {
//        this(layer, 10, 10, 10, 110, var);
//    } // Path

    /**
     * Creates a vertical path of length 100 in the given layer
     * from point <code>(10,110)</code> to point <code>(10,10)</code>.
     * @param layer The layer of the path.
     */
    public Path(Layer layer) {
        this(layer, 10, 10, 10, 110);
    } // Path

    /** Remove units from variables etc. of layer ilayer
     * @param layer The layer from which units are removed.*/
    public void clearUnits(Layer layer) {
        if (this.layer == layer) {
            var.clearUnits();
        }
    } // clearUnits

    /** 
     * Makes a clone (copy) of this Path.
     * @return A clone of this Path.
     */
    @Override
    public Path clone() {
        Path flowpathClone = new Path(layer, x1, y1, x2, y2);
        flowpathClone.setVariable(var);
        return flowpathClone;
    }

    /**
     * Checks if the <code>Path</code> belongs to layer <code>ilayer</code>
     * and contains the coordinates (x,y) in either label or connectors.
     * Sets label as selected or one of the end connectors.
     * @param layer The layer of the path.
     * @param x X coordiante to be contained.
     * @param y Y coordiante to be contained.
     * @return true if contained, false otherwise.
     */
    @Override
    public boolean contains(Layer layer, int x, int y) {
        if (this.layer == layer) {
            if (fLabelShown) {
                fLabelFound = label.contains(x, y);
                if (fLabelFound) {
                    connSelected = null;
                    return true;
                } // if fLabelFound
            } // if fLabelShown
            if (conn1.contains(x, y)) {
                connSelected = conn1;
                return true;
            }
            if (conn2.contains(x, y)) {
                connSelected = conn2;
                return true;
            }
        }
        return false;
    } // contains

    /**
     * Decode flowpath from XML code.
     * @param szXMLCode The xml description of the flow path.
     */
    @Override
    public void decodeXML(String szXMLCode) {
        XMLParser xml = null;
        int ilayer = 0;
        // Decode XML code
        try {
            while (szXMLCode.length() > 0) {
                xml = new XMLParser(szXMLCode);
                String szTag = xml.tag();
                String szSub = xml.sub();
                if (szTag.equals("ilayer")) {
                    ilayer = Integer.parseInt(szSub);
                }
                if (szTag.equals("x1")) {
                    x1 = Integer.parseInt(szSub);
                }
                if (szTag.equals("y1")) {
                    y1 = Integer.parseInt(szSub);
                }
                if (szTag.equals("x2")) {
                    x2 = Integer.parseInt(szSub);
                }
                if (szTag.equals("y2")) {
                    y2 = Integer.parseInt(szSub);
                }
                if (szTag.equals("xOffsetLabel")) {
                    xOffsetLabel = Integer.parseInt(szSub);
                }
                if (szTag.equals("yOffsetLabel")) {
                    yOffsetLabel = Integer.parseInt(szSub);
                }
                if (szTag.equals("sideNode1")) {
                    sideNode1 = Integer.parseInt(szSub);
                }
                if (szTag.equals("sideNode2")) {
                    sideNode2 = Integer.parseInt(szSub);
                }
                if (szTag.equals("flowindex")) {
                    int iflow = Integer.parseInt(szSub);
                    Flow flowTemp = model.getFlow(iflow);
                    if (flowTemp != null) {
                        var = flowTemp;
//                        flow.add(this);
                    } else {
                        String szMsg = "Path.decodeXML could not find flow " +
                                "with index=" + iflow;
                        JOptionPane.showMessageDialog(MainFrame.getInstance(), szMsg);
                    } // if/else
                }// if flowindex
                if (szTag.equals("signalindex")) {
                    int isignal = Integer.parseInt(szSub);
                    Signal signalTemp = model.getSignal(isignal);
                    if (signalTemp != null) {
                        var = signalTemp;
//                        flow.add(this);
                    } else {
                        String szMsg = "Path.decodeXML could not find signal " +
                                "with index=" + isignal;
                        JOptionPane.showMessageDialog(MainFrame.getInstance(), szMsg);
                    } // if/else
                }// if flowindex
                szXMLCode = xml.remainder();
            } // while
            layer = model.getLayer(ilayer);
            conn1.setSideOnly(sideNode1);
            conn1.setPosOnly(x1, y1);
            conn2.setSideOnly(sideNode2);
            conn2.setPosOnly(x2, y2);
        } catch (Exception e) {
            System.out.println("Path.decodeXML: " + e);
        } // try/catch
    } // decodeXML

    /** Dummy method, should not be called. 
     * @param szXMLCode XML code for the flow path.
     * @param root The root of the node tree.
     */
//    @Override
//    public void decodeXML(String szXMLCode, ECFMutableTreeNode root) {
//        throw new UnsupportedOperationException("Path.decodeXML:" +
//                "Usage of this method " +
//                "indicates a program error");
//    } // decodeXML
//    
    /**
     * Decode only label position from flowpath from XML code.
     * @param szXMLCode The xml description of this model.
     */
    public void decodeXMLLabelPos(String szXMLCode) {
        XMLParser xml = null;
        // Decode XML code
        try {
            while (szXMLCode.length() > 0) {
                xml = new XMLParser(szXMLCode);
                String szTag = xml.tag();
                String szSub = xml.sub();
                if (szTag.equals("xOffsetLabel")) {
                    xOffsetLabel = Integer.parseInt(szSub);
                }
                if (szTag.equals("yOffsetLabel")) {
                    yOffsetLabel = Integer.parseInt(szSub);
                }
                szXMLCode = xml.remainder();
            } // while
        } catch (Exception e) {
            System.out.println("Path.decodeXMLLabelPos: " + e);
        } // try/catch
    } // decodeXMLLabelPos

    /** Disconect both ends from nodes if connected.*/
    @Override
    public void disconnectThis() {
        conn1.disconnectNode();
        conn2.disconnectNode();
    } // disconnectThis

    /** Export Path values to file *.dat
     * @return A string with rows of label and values on each line.
     */
    public String export() {
        return var.export();
    } // export

    /** Gets the layer to which the flowpath belongs.
     * @return The layer to which the flowpath belongs. */
    public Layer getLayer() {
        return layer;
    }

    /** Gets the name of the path.
     * @return The name of the path. */
    public String getName() {
        return var.getName();
    } // szName;}

    /** Get the flowconnector at end iend of this Path.
     * @param iend The end of this Path.
     * @return The PathConnector. */
    public PathConnector getPathConnector(int iend) {
        if (iend == 1) {
            return conn1;
        } else {
            return conn2;
        }
    } // getPathConnector

    /** Get the index of the path in the parent model. */
    public int getPathIndex() {
        if (model != null) {
            return model.getPathIndex(this);
        } else // ModelFrame flowpaths (TwinPathConnector).
        {
            return 88;
        }
    } // getPathIndex

    /** Gets the selected connector.
     * @return The connector at the end found by the contains() metod. */
    public PathConnector getSelectedConnector() {
        return connSelected;
    }

    /** Get the variable of the path.
     * @return The variable of the path.*/
    public Variable getVariable() {
        return var;
    }

    /** Hide path connector permanently at iend.
     * @param iend The end of the connector, END_HEAD or END_TAIL
     */
    public void hideConnectorPermanently(int iend) {
        if (iend == END_HEAD) {
            conn1.hidePermanently();
        } else if (iend == END_TAIL) {
            conn2.hidePermanently();
        }
    } // hideConnectorPermanently

    /** Hide flow label permanently. */
    public void hideLabelPermanently() {
        fLabelShown = false;
    } // hideConnectorPermanently

    /** Check if label was hit.
     * @param layer The layer of the flow path.
     * @param x X coordinate to be contained in the label symbol.
     * @param y Y coordinate to be contained in the label symbol.
     * @return true if label was hit, else false. 
     */
    public boolean hitLabel(Layer layer, int x, int y) {
        return label.contains(x, y);
    }

    /** List XML code for this flowpath.
     * @param szBlanks Blanks to indent XML code
     * @return XML code. 
     */
    @Override
    public String listXML(String szBlanks) {
        String szBlanks1 = szBlanks + "  ";
        int ilayer = model.getLayerIndex(layer);
        String szXML = "";
        szXML += szBlanks + "<Path>\n";
        szXML += szBlanks1 + "<ilayer>" + ilayer + "</ilayer>\n";
        szXML += szBlanks1 + "<x1>" + x1 + "</x1>\n";
        szXML += szBlanks1 + "<y1>" + y1 + "</y1>\n";
        szXML += szBlanks1 + "<x2>" + x2 + "</x2>\n";
        szXML += szBlanks1 + "<y2>" + y2 + "</y2>\n";
        szXML += szBlanks1 + "<xOffsetLabel>" + xOffsetLabel + "</xOffsetLabel>\n";
        szXML += szBlanks1 + "<yOffsetLabel>" + yOffsetLabel + "</yOffsetLabel>\n";
        szXML += szBlanks1 + "<sideNode1>" + sideNode1 + "</sideNode1>\n";
        szXML += szBlanks1 + "<sideNode2>" + sideNode2 + "</sideNode2>\n";
        if (var instanceof Flow) {
            szXML += szBlanks1 + "<flowindex>" +
                    model.getFlowIndex((Flow)var) + "</flowindex>\n";
        } // if Flow
        if (var instanceof Signal) {
            szXML += szBlanks1 + "<signalindex>" +
                    model.getSignalIndex((Signal)var) + "</signalindex>\n";
        } // if Signal
        szXML += szBlanks + "</Path>\n";
        return szXML;
    } // listXML

    /**
     * Draws a line from end (x2,y2) to end (x1,y1) in graphics context g, <br>
     * the line may be strait or broken into horizontal and vertical sections
     * that connect in strait angle to the sides of the connected nodes.
     * @param g the graphics context.
     */
    private void drawLine(Graphics g) {
        /** Dash pattern for regular uniform dashes */
        float[] dashes = new float[] { 5.0f, 5.0f };
        int xm, ym;
        int xm1, ym1;
        int xm2, ym2;
        // Offset of middle line of a U-shaped flowpath.
        int offsetULine = 40;

        if (sideNode1 == Node.NONE || sideNode2 == Node.NONE) {
            g.drawLine(x1, y1, x2, y2); // Strait line
            xLabel = (x1 + x2) / 2;
            yLabel = (y1 + y2) / 2;
        } else if ((sideNode1 == Node.EAST || sideNode1 == Node.WEST) &&
                (sideNode2 == Node.NORTH || sideNode2 == Node.SOUTH)) {
            xm = x2;
            ym = y1;        // L-line starting horizontal
            g.drawLine(x1, y1, xm, ym); // Horizontal line
            g.drawLine(xm, ym, x2, y2); // Vertical line
            xLabel = x2;
            yLabel = y1;
        } else if ((sideNode1 == Node.NORTH || sideNode1 == Node.SOUTH) &&
                (sideNode2 == Node.EAST || sideNode2 == Node.WEST)) {
            xm = x1;
            ym = y2;   // L-line starting vertical
            g.drawLine(x1, y1, xm, ym); // Vertical line
            g.drawLine(xm, ym, x2, y2); // Horizontal line
            xLabel = x1;
            yLabel = y2;
        } else if (sideNode1 == Node.EAST && sideNode2 == Node.EAST) {
            xm = Math.max(x1, x2) + offsetULine; // U-line east starting horizontal
            xm1 = xm + xOffsetLabel;
            ym1 = y1;
            xm2 = xm + xOffsetLabel;
            ym2 = y2;
            g.drawLine(x1, y1, xm1, ym1);   // Horizontal line
            g.drawLine(xm1, ym1, xm2, ym2); // Vertical line
            g.drawLine(xm2, ym2, x2, y2);   // Horizontal line
            xLabel = xm;
            yLabel = (y1 + y2) / 2;
        } else if (sideNode1 == Node.WEST && sideNode2 == Node.WEST) {
            xm = Math.min(x1, x2) - offsetULine;  // U-line west starting horizontal
            xm1 = xm + xOffsetLabel;
            ym1 = y1;
            xm2 = xm + xOffsetLabel;
            ym2 = y2;
            g.drawLine(x1, y1, xm1, ym1);   // Horizontal line
            g.drawLine(xm1, ym1, xm2, ym2); // Vertical line
            g.drawLine(xm2, ym2, x2, y2);   // Horizontal line
            xLabel = xm;
            yLabel = (y1 + y2) / 2;
        } else if (sideNode1 == Node.NORTH && sideNode2 == Node.NORTH) {
            ym = Math.min(y1, y2) - offsetULine;  // U-line north starting vertical
            xm1 = x1;
            ym1 = ym + yOffsetLabel;
            xm2 = x2;
            ym2 = ym + yOffsetLabel;
            g.drawLine(x1, y1, xm1, ym1);   // Vertical line
            g.drawLine(xm1, ym1, xm2, ym2); // Horizontal line
            g.drawLine(xm2, ym2, x2, y2);   // Vertical line
            xLabel = (x1 + x2) / 2;
            yLabel = ym;
        } else if (sideNode1 == Node.SOUTH && sideNode2 == Node.SOUTH) {
            ym = Math.max(y1, y2) + offsetULine;   // U-line south starting vertical
            xm1 = x1;
            ym1 = ym + yOffsetLabel;
            xm2 = x2;
            ym2 = ym + yOffsetLabel;
            g.drawLine(x1, y1, xm1, ym1);   // Vertical line
            g.drawLine(xm1, ym1, xm2, ym2); // Horizontal line
            g.drawLine(xm2, ym2, x2, y2);   // Vertical line
            xLabel = (x1 + x2) / 2;
            yLabel = ym;
        } else if ((sideNode1 == Node.EAST && sideNode2 == Node.WEST) ||
                (sideNode1 == Node.WEST && sideNode2 == Node.EAST)) {
            xm = (x1 + x2) / 2;                   // Z-line horizontal
            xm1 = xm + xOffsetLabel;
            ym1 = y1;
            xm2 = xm + xOffsetLabel;
            ym2 = y2;
            g.drawLine(x1, y1, xm1, ym1);   // Horizontal line
            g.drawLine(xm1, ym1, xm2, ym2); // Vertical line
            g.drawLine(xm2, ym2, x2, y2);   // Horizontal line
            xLabel = xm;
            yLabel = (y1 + y2) / 2;
        } else if ((sideNode1 == Node.NORTH && sideNode2 == Node.SOUTH) ||
                (sideNode1 == Node.SOUTH && sideNode2 == Node.NORTH)) {
            ym = (y1 + y2) / 2;                   // Z-line vertical
            xm1 = x1;
            ym1 = ym + yOffsetLabel;
            xm2 = x2;
            ym2 = ym + yOffsetLabel;
            g.drawLine(x1, y1, xm1, ym1);   // Vertical line
            g.drawLine(xm1, ym1, xm2, ym2); // Horizontal line
            g.drawLine(xm2, ym2, x2, y2);   // Vertical line
            xLabel = (x1 + x2) / 2;
            yLabel = ym;
        }
    } // drawLine

    /**
     * Remember the start position of the flow path.
     * @param e Event when mouse is pressed.
     */
    public void mousePressed(MouseEvent e) {
        if (e.getButton()==MouseEvent.BUTTON3) {
            openDialog();
        } else {
            setStartPos();
        } // if/else
    } // mousePressed

    /** Open a dialog for setting properties of the path. */
    public void openDialog() {
        if (var instanceof Flow) new FlowDialog((Flow)var);
        else
        new VariableDialog(MainFrame.getInstance(),"Variable properties",var);
        model.notifyChanges();
    } // openDialog

    /**
     * Paint the path of layer <em>layer</em> only if
     * <em>layer</em> is the same as the layer of the path.
     * @param g the graphics context.
     * @param layer The layer to which the path belongs.
     */
    public void paint(Graphics g, Layer layer) {
        Graphics2D g2 = (Graphics2D)g;

        if (this.layer == layer) {
            g2.setStroke(var.getStroke());
            drawLine(g);
            g2.setStroke(new BasicStroke());
            paintArrow(g);
            conn1.paint(g);
            conn2.paint(g);
            if (fLabelShown) {
                // Flow or signal name label
                label.setText(var.getNameForLabel());
                Color colorBg = ECFUtil.COLOR_DEFAULT;
                if (var.isPrescribed()) {
                    colorBg = ECFUtil.COLOR_PRESCRIBED;
                }
                label.setBackgroundDefault(colorBg);
                int x = xLabel - label.getWidth() / 2 + xOffsetLabel;
                int y = yLabel + yOffsetLabel;
                label.setPos(x, y);
                label.paint(g);
                // Flow or signal
                GraphLabel labelQuant = new GraphLabel(var.getStringQuant(), x, 
                        y + 14, Color.white);
                labelQuant.paint(g);
            } // label
        } // if
    } // paint

    /**
     * Paint the arrow of the flow path.
     * @param g The graphics context.
     */
    private void paintArrow(Graphics g) {
        // Empty arrow flow direction
        int dl = 20; //10; // Lenght of arrow head
        int dw = 8;  //4;  // Width of arrow head
        double pi = 3.14159;
        double angle = 0; // Clockwise from x-axis.
        if (sideNode1 == Node.NORTH) {
            g.drawLine(x1, y1, x1 - dw, y1 - dl);
            g.drawLine(x1, y1, x1 + dw, y1 - dl);
            angle = pi * 1 / 2;
        } else if (sideNode1 == Node.EAST) {
            g.drawLine(x1, y1, x1 + dl, y1 - dw);
            g.drawLine(x1, y1, x1 + dl, y1 + dw);
            angle = pi;
        } else if (sideNode1 == Node.WEST) {
            g.drawLine(x1, y1, x1 - dl, y1 - dw);
            g.drawLine(x1, y1, x1 - dl, y1 + dw);
            angle = 0.0;
        } else if (sideNode1 == Node.SOUTH) {
            g.drawLine(x1, y1, x1 - dw, y1 + dl);
            g.drawLine(x1, y1, x1 + dw, y1 + dl);
            angle = pi * 3 / 2;
        } else {
            try {
                angle = Math.atan2((y1 - y2), (x1 - x2)); // clockwise from x-axis to the right
                g.drawLine(x1, y1, (int) (x1 - dl * Math.cos(angle) + dw * Math.sin(angle)),
                        (int) (y1 - dl * Math.sin(angle) - dw * Math.cos(angle)));
                g.drawLine(x1, y1, (int) (x1 - dl * Math.cos(angle) - dw * Math.sin(angle)),
                        (int) (y1 - dl * Math.sin(angle) + dw * Math.cos(angle)));
            } catch (Exception e) {
                System.out.println("Path.paintArrow:" +e);
            }
        }
        // Draw flow arrow
        if (var.isPrescribed()) // flowArrow(g,angle,flow.getFlowPrescribed(),ECFUtil.COLOR_PRESCRIBED);
        {
            flowArrow(g, angle, var.getPixelValue(cpixelMaxArrowWidth), ECFUtil.COLOR_PRESCRIBED);
        } else // flowArrow(g,angle,flow.getFlowCalculated(),ECFUtil.COLOR_CALCULATED);
        {
            flowArrow(g, angle, var.getPixelValue(cpixelMaxArrowWidth), ECFUtil.COLOR_CALCULATED);
        // if
        }
    } // paintArrow

    /** Paint a filled arrow with width propotional to flow rate.
     * @param g Graphics context.
     * @param theta Angle of flow direction.
     * @param cpixelArrowWidth Number of pixels at actual arrow width.
     * @param color The color of the filling.*/
    private void flowArrow(Graphics g, double theta, double cpixelArrowWidth,
            Color color) {
        int dl = 10; // Lenght of arrow head
        int dw = 4; // Width of arrow head at dl length
        // Filled arrow for flow magnitude
        int widthArrow = (int) (Math.abs(cpixelArrowWidth) + 0.5);
        if (widthArrow >= cpixelMaxArrowWidth * 1.2) {
            color = ECFUtil.COLOR_WARNING;
            widthArrow = (int) (cpixelMaxArrowWidth * 1.2);
        } // if
        int lengthArrow = 40;
        int dla = (int) dl * widthArrow / dw;
        int dwa = widthArrow;
        // Draw flow arrow
        double xArrow[] = new double[5];
        double yArrow[] = new double[5];
        if (cpixelArrowWidth > 0) {
            // Arrow pointing in x-direction, points clockwise.
            xArrow[0] = x1;
            yArrow[0] = y1;  // Arrow point
            xArrow[1] = x1 - dla;
            yArrow[1] = y1 + dwa;
            xArrow[2] = x1 - lengthArrow;
            yArrow[2] = y1 + dwa;
            xArrow[3] = x1 - lengthArrow;
            yArrow[3] = y1 - dwa;
            xArrow[4] = x1 - dla;
            yArrow[4] = y1 - dwa;
        } else {
            // Arrow pointing in minus x-direction, points clockwise.
            xArrow[0] = x1 - lengthArrow;
            yArrow[0] = y1;  // Arrow point;
            xArrow[1] = x1 - lengthArrow + dla;
            yArrow[1] = y1 - dwa;
            xArrow[2] = x1;
            yArrow[2] = y1 - dwa;
            xArrow[3] = x1;
            yArrow[3] = y1 + dwa;
            xArrow[4] = x1 - lengthArrow + dla;
            yArrow[4] = y1 + dwa;
        } // if/else
        g.setColor(color);
        Polygon poly = new Polygon();
        double cos = Math.cos(theta);
        double sin = Math.sin(theta);
        for (int ipoint = 0; ipoint < 5; ipoint++) {
            // Make own transform because AffineTransform does not work with Applet
            int x = x1 + (int) (cos * (xArrow[ipoint] - x1) - sin * (yArrow[ipoint] - y1));
            int y = y1 + (int) (sin * (xArrow[ipoint] - x1) + cos * (yArrow[ipoint] - y1));
            poly.addPoint(x, y);
        }// while
        g.fillPolygon(poly);
    } // flowArrow

    /**
     * Set a flow as the associated variable.
     * @param flow The flow.
     */
    public void setFlow(Flow flow) {
        var = flow;
    } // setFlow

    /**
     * Set a signal as the associated variable.
     * @param signal The signal
     */
    public void setSignal(Signal signal) {
        var = signal;
    }
    /**
     * Set layer and position of node before adding with model.addNodeWaiting().
     * @param layerCurrent The current layer
     * @param x The X-coordinate for the upper left corner
     * @param y The Y-coordinate for the upper left corner
     */
    @Override
    public void setLayerAndPosition(Layer layerCurrent, int x, int y) {
        layer = layerCurrent;
        var.setLayer(layer);
        var.setDefaultUnits();
        x1 = x;
        y1 = y;
        x2 = x;
        y2 = y+100;
        conn1.setPosOnly(x1, y1);
        conn2.setPosOnly(x2, y2);
    } // setLayerAndPosition

    /** Set the name of the path. 
     * @param szName The name to be set.
     */
    public void setName(String szName) {
        var.setName(szName);
    }

    /**
     * Set weather the flow path is selected
     * @param fSelected True if selected, else false.
     */
    @Override
    public void setSelected(boolean fSelected) {
        fPathSelected = fSelected;
        label.setSelected(fSelected);
    }

    /** Set selected flow connector.
     * @param iend Index of selected end = Path.END_HEAD or Path.END_TAIL. */
    public void setSelectedConnector(int iend) {
        if (iend == Path.END_HEAD) {
            connSelected = conn1;
        }
        if (iend == Path.END_TAIL) {
            connSelected = conn2;
        }
    } // setSelectedConnector

    /** Set side of node to which the head end of the flowpath is connected.
     * @param sideNode side of node, see <A href="Node.html">Node</A>.
     */
    public void setSide1(int sideNode) {
        this.sideNode1 = sideNode;
    }

    /** Set side of node to which end 2 of the flowpath is connected.
     * @param sideNode side of node, see <A href="Node.html">Node</A>.
     */
    public void setSide2(int sideNode) {
        this.sideNode2 = sideNode;
    }

    /**
     * Set associated variable
     * @param var The variable to be set.
     */
    private void setVariable(Variable var) {
        this.var = var;
    }

    /**
     * Set start position (before translating) of the flowpath and
     * selected flow connector to current positions.
     */
    public void setStartPos() {
        xStart = xOffsetLabel;
        yStart = yOffsetLabel;
        if (connSelected != null) {
            connSelected.setStartPos();
        }
    } // setStartPos

    /** Set X-coordinate of the head end.
     * @param x1 X-coordinate. */
    public void setX1(int x1) {
        this.x1 = x1;
    }

    /** Set X-coordinate of tail end.
     * @param x2 X-coordinate. */
    public void setX2(int x2) {
        this.x2 = x2;
    }

    /** Set Y-coordinate of the head end.
     * @param y1 Y-coordinate. */
    public void setY1(int y1) {
        this.y1 = y1;
    }

    /** Set Y-coordinate of the tail end.
     * @param y2 Y-coordinate. */
    public void setY2(int y2) {
        this.y2 = y2;
    }

    /** Set the units of layer ilayer if not previously set.
     * @param layer The layer.
     * @param units The units to be set.*/
    public void setUnitsIfNotSet(Layer layer, Units units) {
        if (this.layer == layer) {
            var.setUnitsIfNotSet(units);
        }
    } // setUnitsIfNotSet

    /** Show properties of this Path
     * @return A string telling the properties.*/
    @Override
    public String toString() {
        if (var == null) {
            return "Path: var==null";
        }
        return "Path: " + var.getName();
    } // toString

    /** Translate label or selected end by (dx,dy),
     * see method <code>contains</code> above.
     * @param dx translation in x-direction.
     * @param dy translation in y-direction. */
    @Override
    public void translate(int dx, int dy) {
        // Translate label
        if (fLabelFound) {
            xOffsetLabel = xStart + dx;
            yOffsetLabel = yStart + dy;
        } // if
        // Translate connector
        if (connSelected != null) {
            Node nodeConnected = connSelected.getNode();
            if (nodeConnected!=null && nodeConnected instanceof Sector) 
                model.deleteSubModelConnector(this,nodeConnected);
            connSelected.translate(dx, dy);
            x1 = (int) conn1.getRefPoint().getX();
            y1 = (int) conn1.getRefPoint().getY();
            x2 = (int) conn2.getRefPoint().getX();
            y2 = (int) conn2.getRefPoint().getY();
        } // if
    } // translate

    /** Update the line of the flow path to current positions of ends
     * and current sides of connected nodes. */
    public void updateLine() {
        x1 = (int) conn1.getRefPoint().getX();
        y1 = (int) conn1.getRefPoint().getY();
        sideNode1 = conn1.getSide();
        x2 = (int) conn2.getRefPoint().getX();
        y2 = (int) conn2.getRefPoint().getY();
        sideNode2 = conn2.getSide();
    } // updateLine

} // Path
