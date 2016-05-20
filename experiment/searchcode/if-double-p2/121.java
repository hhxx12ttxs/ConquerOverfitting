package trussoptimizater.Gui;


import trussoptimizater.Truss.TrussModel;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.util.Observable;
import javax.swing.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.MemoryImageSource;
import java.text.DecimalFormat;
import trussoptimizater.Gui.Actions.MyActionMap;
import trussoptimizater.Gui.Actions.ZoomAllAction;
import trussoptimizater.Gui.Element2D.*;
import trussoptimizater.Gui.GUIModels.GUIModeModel;
import trussoptimizater.Gui.GUIModels.ViewModel;
import trussoptimizater.Gui.Listeners.PopupListener;
import trussoptimizater.Gui.Listeners.SelectListener;
import trussoptimizater.Gui.Listeners.ZoomAndPanListener;
import trussoptimizater.Truss.Elements.*;

/**
 * This class is used to graphically represent a truss model.
 * @author Chris
 */
public class View extends JPanel implements java.util.Observer , MouseMotionListener{ 

    private TrussModel truss;
    private GUI gui;
    private ViewModel viewModel;
    private Graphics2D g2d;
    public static final Dimension viewDimension = new Dimension(1000, 600);    
    /*Listeners*/
    private ZoomAndPanListener zoomAndPanListener;
    private SelectListener selectListener;
    private PopupListener popupListener;
    private boolean init = true;
    private static final DecimalFormat DF = new DecimalFormat("#.##");
    /*Colors*/
    public static final Color DRAG_RIGHT_SELECT_COLOR = new Color(0.1f, 0.1f, 1.0f, 0.4f);;
    public static final Color DRAG_LEFT_SELECT_COLOR = Color.BLUE;
    public static final Color BACKGROUND_COLOR = new Color(204, 255, 255);
    public static final Color CROSS_AXIS_COLOR = Color.black;
    public static final BasicStroke CROSS_AXIS_STROKE = new BasicStroke(1);


    private Point mouselocation = new Point();

    public View(GUI gui, ViewModel viewModel) {
        super();
        this.viewModel = viewModel;
        this.viewModel.addObserver(this);
        this.gui = gui;
        this.zoomAndPanListener = new ZoomAndPanListener(this);
        this.selectListener = new SelectListener(gui);
        this.popupListener = new PopupListener(gui);
        this.setPreferredSize(viewDimension);
    }

    public void createGui() {
        this.addMouseMotionListener(selectListener);
        this.addMouseListener(selectListener);
        this.addMouseListener(zoomAndPanListener);
        this.addMouseMotionListener(zoomAndPanListener);
        this.addMouseWheelListener(zoomAndPanListener); 
        this.addMouseMotionListener(this);
    }//end of createGui

    public void setTrussModel(TrussModel truss) {
        this.truss = truss;
        repaint();
    }




    @Override
    public void paintComponent(Graphics g) {

        g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setFont(Element2D.font);

        //Painting background
        GradientPaint gp = new GradientPaint(0, 0, Color.white, this.getWidth(), this.getHeight(), Color.gray, true);
        g2d.setColor(View.BACKGROUND_COLOR);
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, this.getWidth(), this.getHeight());


        //paintAxis(g2d);
        paintAxis(g2d);
        paintStressBoxes(g2d);
        paintDragRectangle(g2d);
        if(gui.getGuiModeModel().getMode() == GUIModeModel.BAR_MODE || gui.getGuiModeModel().getMode() == GUIModeModel.NODE_MODE){
            paintCrossHair(g2d);
        }
        
        //zoom and pan
        if (init) {
            init = false;
            Dimension d = getSize();
            int xc = d.width / 2;
            int yc = d.height / 2;
            g2d.translate(xc, yc);
            zoomAndPanListener.setCoordTransform(g2d.getTransform());
        } else {
            //Restore the viewport after it was updated by the ZoomAndPanListener
            g2d.transform(zoomAndPanListener.getCoordTransform());
        }

        if (truss == null) {
            return;
        }
        //paintGrid(g2d);
        
        paintBars(g2d);
        paintNodes(g2d);
        paintSupports(g2d);
        paintLoads(g2d); 
        paintDeflectedBars(g2d);
        paintDeflectedNodes(g2d);
        paintAxialForces(g2d);
        paintReactions(g2d);
        paintMoments(g2d);
        paintShear(g2d);
        //paintTest(g2d);
    }

    public void paintGrid(Graphics2D g2d){
        ZoomAllAction zoomtemp = (ZoomAllAction)MyActionMap.ACTION_MAP.get(MyActionMap.ZOOM_ALL_ACTION_KEY);
        Rectangle2D nodebounds = zoomtemp.getNodeBounds();

        int overflow = 500;
        int xpos = (int)nodebounds.getMinX()-overflow;
        int ypos = (int)nodebounds.getMinY()-overflow;
        int spacing = gui.getTruss().getOptimizeMethods().getGAOptimizer().getGAModel().getNodalGrid();

        g2d.draw(nodebounds);

        //painting vertical line
        while(true){
            
            if(truss== null || truss.getNodeModel().size()<5 ||  xpos>nodebounds.getMaxX()+overflow){ //|| xpos>nodebounds.getMaxX()
                break;
            }else{
                g2d.drawLine(xpos, (int)nodebounds.getMinY()-overflow, xpos, (int)nodebounds.getMaxY()+overflow);
                xpos += spacing;
            }
        }

        //painting horizontal lines
        while(true){
            
            if( truss== null || truss.getNodeModel().size()<5 ||  ypos>nodebounds.getMaxY()+overflow ){
                break;
            }else{
                g2d.drawLine((int)nodebounds.getMinX()-overflow, ypos, (int)nodebounds.getMaxX()+overflow, ypos);
                ypos += spacing;
            }
        }

    }

    public void paintCrossHair(Graphics2D g2d) {
        g2d.setStroke(View.CROSS_AXIS_STROKE);
        g2d.setColor(View.CROSS_AXIS_COLOR);
        g2d.drawLine(-getWidth(), (int) mouselocation.getY(), getWidth(), (int) mouselocation.getY());
        g2d.drawLine((int) mouselocation.getX(), -getHeight(), (int) mouselocation.getX(), getHeight());
        g2d.setFont(new Font(Font.DIALOG, Font.PLAIN, 10));
        g2d.drawString(DF.format(gui.getView().getZoomAndPanListener().getMouseLocation().x * 10) + ", " + DF.format(gui.getView().getZoomAndPanListener().getMouseLocation().y * 10) + " (mm,mm)", 20, this.getHeight() - 20);
    }

    public void paintAxis(Graphics2D g2d) {
        g2d.setColor(Color.DARK_GRAY);
        int arrowHead = 5;
        int arrowLength = 30;
        int xpos = 20;
        int ypos = 20;
        //int xpos = 0;
        //int ypos = 0;
        g2d.setStroke(LocalBarAxis2D.stroke);
        //Draw x arrow
        g2d.setColor(LocalBarAxis2D.X_AXIS_ARROW_COLOR);
        g2d.drawLine(xpos, ypos, xpos+arrowLength, ypos);
        g2d.drawLine(xpos+arrowLength, ypos, xpos+arrowLength - arrowHead, ypos - arrowHead);
        g2d.drawLine(xpos+arrowLength, ypos, xpos+arrowLength - arrowHead, ypos + arrowHead);
        g2d.drawString("X", xpos+arrowLength + 5, ypos+ 5);

        //Draw y arorw
        g2d.setColor(LocalBarAxis2D.Z_AXIS_ARROW_COLOR);
        g2d.drawLine(xpos, ypos, xpos, ypos+arrowLength);
        g2d.drawLine(xpos, ypos+arrowLength, xpos - arrowHead, ypos+arrowLength - arrowHead);
        g2d.drawLine(xpos, ypos+arrowLength, xpos + arrowHead, ypos+arrowLength - arrowHead);
        g2d.drawString("Z", xpos - 3, ypos+arrowLength + 13);

    }

    /**
     * For debug use only
     * @param g2d
     */
    public void paintTest(Graphics2D g2d) {
        /*Testing for view all method*/
        g2d.setColor(Color.BLACK);
        g2d.drawOval(g2d.getClipBounds().x, g2d.getClipBounds().y, g2d.getClipBounds().width, g2d.getClipBounds().height);
        ZoomAllAction zoomAllAction = (ZoomAllAction)MyActionMap.ACTION_MAP.get(MyActionMap.ZOOM_ALL_ACTION_KEY);
        g2d.draw(zoomAllAction.getNodeBounds());

        g2d.setStroke(new BasicStroke(1));

        /*Node2D node2D;
        for (int i = 0; i < truss.getNodeModel().size(); i++) {
            node2D = new Node2D(truss.getNodeModel().get(i));
            g2d.draw(node2D.getHighLightShape());
        }*/


        /*Bar2D bar2D;
        for (int i = 0; i < truss.getBarModel().size(); i++) {
            bar2D = new Bar2D(truss.getBarModel().get(i));
            g2d.draw(bar2D.getHighLightShape());
        }*/

        /*Support2D support2D;
        for (int i = 0; i < truss.getSupportModel().size(); i++) {
            support2D = new Support2D(truss.getSupportModel().get(i));
            g2d.draw(support2D.getHighLightShape());
        }*/

        /*Load2D load2D;
        double maxLoad = truss.getMaxABSLoad();
        for (int i = 0; i < truss.getLoadModel().size(); i++) {
            load2D = new Load2D(truss.getLoadModel().get(i), maxLoad);
            g2d.draw(load2D.getHighLightShape());
        }*/
    }

    public void paintNodes(Graphics2D g2d) {
        //drawing nodes
        g2d.setColor(Node2D.COLOR);
        g2d.setStroke(Node2D.stroke);
        Node2D node2D;
        for (int i = 0; i < truss.getNodeModel().size(); i++) {
            if (truss.getNodeModel().get(i).isSelected() || truss.getNodeModel().get(i).isHighLighted()) {
                g2d.setColor(Node2D.HIGHLIGHT_COLOR);
            } else {
                g2d.setColor(Node2D.COLOR);
            }

            node2D = new Node2D(truss.getNodeModel().get(i));
            if (viewModel.isNodesVisible()) {
                g2d.fill(node2D.getShape());
            }

            if (viewModel.isNodeNumbersVisible()) {
                g2d.drawString(Integer.toString(truss.getNodeModel().get(i).getNumber()), (int) truss.getNodeModel().get(i).x, (int) truss.getNodeModel().get(i).z - Node2D.NODE_DIAMETER);
                //g2d.drawString(Integer.toString(truss.getNodeModel().get(i).getNumber()), (int) truss.getNodeModel().get(i).x*scaleFactor , (int) truss.getNodeModel().get(i).z*scaleFactor  - NODE_DIAMETER);
            }

        }
    }

    public void paintBars(Graphics2D g2d) {
        //drawing bars
        double maxStress = truss.getMaxBarStress();
        g2d.setStroke(Bar2D.CENTERLINE_STROKE);
        Bar2D bar2d =null;
        for (int i = 0; i < truss.getBarModel().size(); i++) {
            bar2d = new Bar2D(truss.getBarModel().get(i));
            if (viewModel.isColorTensionCompression() && truss.isAnalysed() && truss.getBarModel().get(i).getAxialForce() > 0.001) {
                g2d.setColor(Bar2D.COMPRESSION_COLOR);
            } else if (viewModel.isColorTensionCompression() && truss.isAnalysed() && truss.getBarModel().get(i).getAxialForce() < -0.001) {
                g2d.setColor(Bar2D.TENSION_COLOR);
            } else if (viewModel.isColorAxialForces() && truss.isAnalysed()) {
                g2d.setColor(getColor(truss.getBarModel().get(i).getStress(), maxStress));
            } else {
                g2d.setColor(Bar2D.COLOR);
            }

            if (truss.getBarModel().get(i).isSelected() || truss.getBarModel().get(i).isHighLighted()) {
                g2d.setColor(Bar2D.HIGHLIGHT_COLOR);
            } else if (!viewModel.isColorTensionCompression() && !truss.isAnalysed()) {
                g2d.setColor(Bar2D.COLOR);
            }

            if (viewModel.isBarsVisible()) {
                //g2d.
                //g2d.drawLine((int) truss.getBarModel().get(i).getNode1().x, (int) truss.getBarModel().get(i).getNode1().z, (int) truss.getBarModel().get(i).getNode2().x, (int) truss.getBarModel().get(i).getNode2().z);
                g2d.setStroke(Bar2D.BOUNDS_STROKE);
                g2d.draw(bar2d.getHighLightShape());
                g2d.setStroke(Bar2D.CENTERLINE_STROKE);
                g2d.draw(bar2d.getShape());
            }

            if (viewModel.isBarNumbersVisible()) {
                //g2d.setColor(black);
                g2d.drawString(Integer.toString(truss.getBarModel().get(i).getNumber()), (int) truss.getBarModel().get(i).getMidPoint().x, (int) truss.getBarModel().get(i).getMidPoint().y);
            }

            if (viewModel.isBarAxisVisible()) {
                LocalBarAxis2D axis = new LocalBarAxis2D(truss.getBarModel().get(i));
                g2d.setStroke(LocalBarAxis2D.stroke);
                g2d.setColor(LocalBarAxis2D.X_AXIS_ARROW_COLOR);
                g2d.draw(axis.getLocalXAxis2D());
                g2d.setColor(LocalBarAxis2D.Z_AXIS_ARROW_COLOR);
                g2d.draw(axis.getLocalZAxis2D());

            }
        }

        //If currently drawing bar, bar should follow mouse
        if (this.viewModel.isCurrentlyDrawingBar()) {
            g2d.setColor(Bar2D.COLOR);
            g2d.drawLine((int) viewModel.getBarStartNode().x, (int) viewModel.getBarStartNode().z, (int) zoomAndPanListener.getMouseLocation().x, (int) zoomAndPanListener.getMouseLocation().y);
        }
    }

    public void paintStressBoxes(Graphics2D g2d) {
        double maxStress = gui.getTruss().getMaxBarStress();
        int borderInset = 50;
        int increment = 10;
        int boxWidth = 30;
        int boxCount = 5;

        int x1 = gui.getView().getWidth() - borderInset - boxWidth;
        int y1 = gui.getView().getHeight() - borderInset - boxCount * boxWidth - (boxCount - 1) * increment;

        double stressIncrement = (1.0 / (boxCount - 1));
        if (gui.getView().getViewModel().isColorAxialForces() && gui.getTruss().isAnalysed()) {
            for (int i = 0; i < boxCount; i++) {


                g2d.setColor(getColor(stressIncrement * i));
                Rectangle rect = new Rectangle(x1, y1 + increment * i + boxWidth * i, boxWidth, boxWidth);
                g2d.fill(rect);
                g2d.setColor(Color.BLACK);
                double stress = (stressIncrement * i - 0.5) * 2 * maxStress;
                g2d.drawString(DF.format(stress), x1 - 50, y1 + increment * i + boxWidth * i + boxWidth / 2);
            }
            g2d.drawString(" KN/mm^2", x1, y1 + increment * boxCount + boxWidth * boxCount + 10);
        }
    }

    public void paintLoads(Graphics2D g2d) {
        //drawing load arrows
        g2d.setStroke(Load2D.stroke);
        double maxLoad = truss.getMaxABSLoad();

        for (int i = 0; i < truss.getLoadModel().size(); i++) {

            if (truss.getLoadModel().get(i).isSelected() || truss.getLoadModel().get(i).isHighLighted()) {
                g2d.setColor(Load2D.HIGHLIGHT_COLOR);
            } else {
                g2d.setColor(Load2D.COLOR);
            }
            Load2D load2D = new Load2D(truss.getLoadModel().get(i), maxLoad);
            
            if (viewModel.isLoadsVisible()) {
                g2d.draw(load2D.getShape());
            }

            //vertical loads
            if (viewModel.isLoadNumbersVisible() && truss.getLoadModel().get(i).getOrientation().equals(Load.VERTICAL_LOAD)) {
                //draw at top of line
                g2d.drawString(Integer.toString(truss.getLoadModel().get(i).getNumber()), (int) load2D.getArrowTailPoint().x, (int) load2D.getArrowTailPoint().y);
            }

            if (viewModel.isLoadValuesVisible() && truss.getLoadModel().get(i).getOrientation().equals(Load.VERTICAL_LOAD)) {
                //draw this in middle of line
                Point2D.Double midPoint = this.getLineMidPoint(truss.getLoadModel().get(i).getNode().getPoint2D(), load2D.getArrowTailPoint());
                g2d.drawString(DF.format(truss.getLoadModel().get(i).getLoad()) + " KN", (int) midPoint.x, (int) midPoint.y);
            }


            //Horizontal loads
            if (viewModel.isLoadNumbersVisible() && truss.getLoadModel().get(i).getOrientation().equals(Load.HORIZOANTAL_LOAD)) {
                //draw at top of line
                g2d.drawString(Integer.toString(truss.getLoadModel().get(i).getNumber()), (int) load2D.getArrowTailPoint().x, (int) load2D.getArrowTailPoint().y);
            }

            if (viewModel.isLoadValuesVisible() && truss.getLoadModel().get(i).getOrientation().equals(Load.HORIZOANTAL_LOAD)) {
                //draw this in middle of line
                Point2D.Double midPoint = this.getLineMidPoint(truss.getLoadModel().get(i).getNode().getPoint2D(), load2D.getArrowTailPoint());
                g2d.drawString(DF.format(truss.getLoadModel().get(i).getLoad()) + " KN", (int) midPoint.x, (int) midPoint.y);
            }


        }
    }

    public void paintSupports(Graphics2D g2d) {
        //drawing supports
        g2d.setStroke(Support2D.stroke);
        Support2D support2D;
        for (int i = 0; i < truss.getSupportModel().size(); i++) {
            if (truss.getSupportModel().get(i).isSelected() || truss.getSupportModel().get(i).isHighLighted()) {
                g2d.setColor(Support2D.HIGHLIGHT_COLOR);
            } else {
                g2d.setColor(Support2D.BORDER_COLOR);
            }
            support2D = new Support2D(truss.getSupportModel().get(i));
            if (viewModel.isSupportsVisible()) {
                g2d.fill(support2D.getShape());
                g2d.setColor(Support2D.FILL_COLOR);
                g2d.draw(support2D.getShape());
            }


            if (viewModel.isSupportNumbersVisible()) {
                g2d.drawString(Integer.toString(truss.getSupportModel().get(i).getNumber()), truss.getSupportModel().get(i).getNode().getPoint().x + Support2D.SUPPORT_WIDTH, truss.getSupportModel().get(i).getNode().getPoint().y + Node2D.NODE_DIAMETER / 2 + Support2D.SUPPORT_WIDTH);
            }
        }
    }

    public void paintDeflectedNodes(Graphics2D g2d) {
        //drawing Deflected Nodes
        g2d.setColor(Node2D.DEFLECTED_COLOR);
        g2d.setStroke(Node2D.stroke);
        Node2D node2D;
        for (int i = 0; i < truss.getNodeModel().size(); i++) {
            if (viewModel.isDeflectionsVisible() && truss.isAnalysed()) {
                node2D = new Node2D(truss.getNodeModel().get(i));
                g2d.fill(node2D.getDeflectedShape(viewModel.getDeflectionZoomScale()));
            }
        }
    }

    public void paintDeflectedBars(Graphics2D g2d) {
        //drawing deflected bars if BEAM ANALYSIS
        g2d.setStroke(Bar2D.CENTERLINE_STROKE);
        g2d.setColor(Bar2D.DEFLECTED_COLOR);
        Bar2D bar2D;
        for (int i = 0; i < truss.getBarModel().size(); i++) {

            if (viewModel.isDeflectionsVisible() && truss.isAnalysed()) {
                bar2D = new Bar2D(truss.getBarModel().get(i));
                g2d.draw(bar2D.getDeflectedShape(viewModel.getDeflectionZoomScale()));
            }
        }
    }

    public void paintAxialForces(Graphics2D g2d) {
        //drawing bar axial forces        

        if (truss.isAnalysed() && viewModel.isAxialForcesVisible()) {
            for (int i = 0; i < truss.getBarModel().size(); i++) {

                if (viewModel.isColorTensionCompression() && truss.isAnalysed() && truss.getBarModel().get(i).getAxialForce() > 0.001) {
                    g2d.setColor(Bar2D.COMPRESSION_COLOR);
                } else if (viewModel.isColorTensionCompression() && truss.isAnalysed() && truss.getBarModel().get(i).getAxialForce() < -0.001) {
                    g2d.setColor(Bar2D.TENSION_COLOR);
                } else {
                    g2d.setColor(Bar2D.COLOR);
                }
                g2d.drawString(DF.format(truss.getBarModel().get(i).getAxialForce()) + " KN", (int) truss.getBarModel().get(i).getMidPoint().x, (int) truss.getBarModel().get(i).getMidPoint().y);
            }
        }
    }

    public void paintReactions(Graphics2D g2d) {
        //drawing reactions
        g2d.setStroke(Reaction2D.stroke);
        g2d.setColor(Reaction2D.COLOR);
        if (truss.isAnalysed() && viewModel.isReactionsVisible()) {
            g2d.setColor(Color.RED);
            Reaction2D reaction2D;
            for (int i = 0; i < truss.getSupportModel().size(); i++) {
                reaction2D = new Reaction2D(truss.getSupportModel().get(i));

                if (truss.getSupportModel().get(i).getReactionZ() < -0.0001 || truss.getSupportModel().get(i).getReactionZ() > 0.0001) {

                    g2d.draw(reaction2D.getReactionZShape());
                    g2d.drawString(DF.format(truss.getSupportModel().get(i).getReactionZ()) + " KN", (int) reaction2D.getArrowTailPoint().x, (int) reaction2D.getArrowTailPoint().y);
                }
                if (truss.getSupportModel().get(i).getReactionX() < -0.0001 || truss.getSupportModel().get(i).getReactionX() > 0.0001) {
                    g2d.draw(reaction2D.getReactionXShape());
                    g2d.drawString(DF.format(truss.getSupportModel().get(i).getReactionX()) + " KN", (int) reaction2D.getArrowTailPoint().x - 10, (int) reaction2D.getArrowTailPoint().y - Node2D.NODE_DIAMETER);
                }
            }
        }
    }

    public void paintMoments(Graphics2D g2d) {
        /*Drawing Moment*/
        g2d.setStroke(new BasicStroke(2));
        if (truss.isAnalysed() && viewModel.isMomentsVisible()) {
            for (int i = 0; i < truss.getBarModel().size(); i++) {
                double rot1 = truss.getBarModel().get(i).getMomentForce()[0];
                double rot2 = truss.getBarModel().get(i).getMomentForce()[1];

                rot1 = rot1 * viewModel.getMomentZoomScale();
                rot2 = rot2 * viewModel.getMomentZoomScale();
                double bmp1 = truss.getBarModel().get(i).getNode1().z - rot1;
                double bmp2 = truss.getBarModel().get(i).getNode1().z - rot2;


                AffineTransform at = new AffineTransform();
                at.rotate(truss.getBarModel().get(i).getAngle(), truss.getBarModel().get(i).getNode1().getPoint().x, truss.getBarModel().get(i).getNode1().getPoint().y);

                Path2D path = new Path2D.Double();
                path.moveTo(truss.getBarModel().get(i).getNode1().x, truss.getBarModel().get(i).getNode1().z);
                path.lineTo(truss.getBarModel().get(i).getNode1().x, bmp1);
                path.lineTo(truss.getBarModel().get(i).getNode1().x + truss.getBarModel().get(i).getLength(), bmp2);
                path.lineTo(truss.getBarModel().get(i).getNode1().x + truss.getBarModel().get(i).getLength(), truss.getBarModel().get(i).getNode1().z);

                g2d.setColor(Bar2D.BENDING_MOMENT_COLOR);
                g2d.draw(at.createTransformedShape(path));
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
                g2d.fill(at.createTransformedShape(path));
            }
        }
    }

    public void paintShear(Graphics2D g2d) {
        /*Drawing Shear*/
        g2d.setStroke(new BasicStroke(2));
        if (truss.isAnalysed() && viewModel.isShearVisible()) {
            for (int i = 0; i < truss.getBarModel().size(); i++) {
                double shearValue1 = truss.getBarModel().get(i).getShearForce()[0];
                double shearValue2 = truss.getBarModel().get(i).getShearForce()[1];
                shearValue1 = shearValue1 * viewModel.getShearZoomScale();
                shearValue2 = shearValue2 * viewModel.getShearZoomScale();
                double sfp1 = truss.getBarModel().get(i).getNode1().z + shearValue1;
                double sfp2 = truss.getBarModel().get(i).getNode1().z + shearValue2;


                AffineTransform at = new AffineTransform();
                at.rotate(truss.getBarModel().get(i).getAngle(), truss.getBarModel().get(i).getNode1().getPoint().x, truss.getBarModel().get(i).getNode1().getPoint().y);
                Path2D path = new Path2D.Double();
                path.moveTo(truss.getBarModel().get(i).getNode1().x, truss.getBarModel().get(i).getNode1().z);
                path.lineTo(truss.getBarModel().get(i).getNode1().x, sfp1);
                path.lineTo(truss.getBarModel().get(i).getNode1().x + truss.getBarModel().get(i).getLength(), sfp2);
                path.lineTo(truss.getBarModel().get(i).getNode1().x + truss.getBarModel().get(i).getLength(), truss.getBarModel().get(i).getNode1().z);

                g2d.setColor(Bar2D.SHEAR_FORCE_COLOR);
                g2d.draw(at.createTransformedShape(path));
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
                g2d.fill(at.createTransformedShape(path));
            }
        }
    }



    public void paintDragRectangle(Graphics2D g2d) {
        //draw selection square
        g2d.setStroke(new BasicStroke(1));
        if (gui.getGuiModeModel().getMode() == GUIModeModel.SELECT_MODE && viewModel.isCurrentlyDragSelecting()) {
            
            float[] dash = {9.0f};
            Stroke stroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 4.0f, dash, 0.0f);
            g2d.setStroke(stroke);
            double width = mouselocation.x - viewModel.getDragStartScreen().x;
            double height = mouselocation.y - viewModel.getDragStartScreen().y;
            Rectangle2D rect;

            if (width > 0 && height > 0) {
                viewModel.setDragDirection(ViewModel.RIGHT_DRAG);
                rect = new Rectangle2D.Double(viewModel.getDragStartScreen().x, viewModel.getDragStartScreen().y, width, height);
                g2d.setColor(View.DRAG_RIGHT_SELECT_COLOR);
                g2d.fill(rect);
                //g2d.setColor(View.DRAG_LEFT_SELECT_COLOR);
                //g2d.draw(rect);
            } else if (width < 0 && height > 0) {
                viewModel.setDragDirection(ViewModel.LEFT_DRAG);
                rect = new Rectangle2D.Double(viewModel.getDragStartScreen().x + width, viewModel.getDragStartScreen().y, -width, height);
                g2d.setColor(View.DRAG_LEFT_SELECT_COLOR);
                g2d.draw(rect);
            } else if (width > 0 && height < 0) {
                viewModel.setDragDirection(ViewModel.RIGHT_DRAG);
                rect = new Rectangle2D.Double(viewModel.getDragStartScreen().x, viewModel.getDragStartScreen().y + height, width, -height);
                g2d.setColor(View.DRAG_RIGHT_SELECT_COLOR);
                g2d.fill(rect);
                //g2d.setColor(View.DRAG_LEFT_SELECT_COLOR);
                //g2d.draw(rect);
            } else {
                viewModel.setDragDirection(ViewModel.LEFT_DRAG);
                rect = new Rectangle2D.Double(viewModel.getDragStartScreen().x + width, viewModel.getDragStartScreen().y + height, -width, -height);
                g2d.setColor(View.DRAG_LEFT_SELECT_COLOR);
                g2d.draw(rect);
            }
            viewModel.setSelectionRectangle(rect);
        }
    }

    public static Color getColor(double stress, double maxStress) {

        double value = (stress / maxStress) / 2 + 0.5;
        return getColor(value);
    }

    public static Color getColor(double value) {

        double H = value * 0.7; // Hue (note 0.2 = Green, see huge chart below)
        double S = 0.9; // Saturation
        double B = 0.9; // Brightness
        return Color.getHSBColor((float) H, (float) S, (float) B);
    }

    public Point2D.Double getLineMidPoint(Point2D.Double p1, Point2D.Double p2) {
        return new Point2D.Double(p1.x + (p2.x - p1.x) / 2, p2.y - (p2.y - p1.y) / 2);
    }

    public void setCursor() {

        if (gui.getGuiModeModel().getMode() == GUIModeModel.SELECT_MODE && viewModel.isCurrentlyDragSelecting()) {
            gui.getFrame().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        } else if (gui.getGuiModeModel().getMode() == GUIModeModel.SELECT_MODE && !viewModel.isCurrentlyDragSelecting()) {
            gui.getFrame().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        } else if (gui.getGuiModeModel().getMode() == GUIModeModel.NODE_MODE || gui.getGuiModeModel().getMode() == GUIModeModel.BAR_MODE) {
            int[] pixels = new int[16 * 16];
            Image image = Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(16, 16, pixels, 0, 16));
            Cursor transparentCursor = Toolkit.getDefaultToolkit().createCustomCursor(image, new Point(0, 0), "invisibleCursor");
            gui.getFrame().setCursor(transparentCursor);
            //gui.getFrame().setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        }

    }

    public void update(Observable o, Object arg) {
        repaint();
    }
    
    public ZoomAndPanListener getZoomAndPanListener() {
        return zoomAndPanListener;
    }

    public SelectListener getSelectListener() {
        return selectListener;
    }

    public Point getMouselocation() {
        return mouselocation;
    }


    public PopupListener getPopupListener() {
        return popupListener;
    }

    public Graphics2D getG2d() {
        return g2d;
    }

    public ViewModel getViewModel() {
        return viewModel;
    }

    public void mouseDragged(MouseEvent e) {
        mouselocation = e.getPoint();
    }

    public void mouseMoved(MouseEvent e) {
        mouselocation = e.getPoint();
    }
}//end of class

