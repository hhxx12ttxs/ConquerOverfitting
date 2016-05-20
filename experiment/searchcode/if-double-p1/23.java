package trussoptimizater.Gui.Listeners;
import java.awt.event.*;
import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

public class ZoomAndPanListener implements MouseListener, MouseMotionListener, MouseWheelListener {

    public static final int DEFAULT_MIN_ZOOM_LEVEL = -20;
    public static final int DEFAULT_MAX_ZOOM_LEVEL = 10;
    public static final double DEFAULT_ZOOM_MULTIPLICATION_FACTOR = 1.2;
    //private View targetComponent;
    private int zoomLevel = 0;
    private int minZoomLevel = DEFAULT_MIN_ZOOM_LEVEL;
    private int maxZoomLevel = DEFAULT_MAX_ZOOM_LEVEL;
    private double zoomMultiplicationFactor = DEFAULT_ZOOM_MULTIPLICATION_FACTOR;  
    private Point dragStartScreen;
    private Point dragEndScreen;
    
    
    private AffineTransform coordTransform = new AffineTransform();
    private Point mouseLocation = new Point();

    //private GUI gui;
    private JComponent[] components;

    public ZoomAndPanListener(JComponent component) {
        this(new JComponent[]{component});

    }
    public ZoomAndPanListener(JComponent[] components) {
        this.components = components;

    }

    public Point2D.Double getMouseLocation() {
        try {
            return this.transformPoint(mouseLocation);
        } catch (NoninvertibleTransformException ex) {
            Logger.getLogger(ZoomAndPanListener.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public void mouseClicked(MouseEvent e) {
        mouseLocation = e.getPoint();
        repaintAll();
    }

    public void mousePressed(MouseEvent e) {
        dragStartScreen = e.getPoint();
        dragEndScreen = null;
    }

    public void mouseReleased(MouseEvent e) {
        mouseLocation = e.getPoint();
        repaintAll();
    }

    public void mouseEntered(MouseEvent e) {
        mouseLocation = e.getPoint();
        repaintAll();

    }

    public void mouseExited(MouseEvent e) {
        mouseLocation = e.getPoint();
        repaintAll();
    }

    /*Repaints to see if elements should be highlighted*/
    public void mouseMoved(MouseEvent e) {
        mouseLocation = e.getPoint();
        repaintAll();
    }

    public void mouseDragged(MouseEvent e) {
        mouseLocation = e.getPoint();
        if(SwingUtilities.isMiddleMouseButton(e)){          
            moveCamera(e);
        }
        repaintAll();
    }

    public void mouseWheelMoved(MouseWheelEvent e) {
        zoomCamera(e);
    }

    private void moveCamera(MouseEvent e) {
        try {
            dragEndScreen = e.getPoint();
            Point2D.Double dragStart = transformPoint(dragStartScreen);
            Point2D.Double dragEnd = transformPoint(dragEndScreen);
            double dx = dragEnd.getX() - dragStart.getX();
            double dy = dragEnd.getY() - dragStart.getY();
            coordTransform.translate(dx, dy);
            dragStartScreen = dragEndScreen;
            dragEndScreen = null;
            //targetComponent.repaint();
            repaintAll();
        } catch (NoninvertibleTransformException ex) {
            ex.printStackTrace();
        }
    }
    

    /**
     * Used to programmatically pan view.
     * Used by ZoomAll action
     * @param dragStart
     * @param dragEnd
     */
    public void moveCamera(Point2D.Double dragStart, Point2D.Double dragEnd) {

            double dx = dragEnd.getX() - dragStart.getX();
            double dy = dragEnd.getY() - dragStart.getY();
            coordTransform.translate(dx, dy);
            dragStartScreen = dragEndScreen;
            dragEndScreen = null;
    }

 

    private void zoomCamera(MouseWheelEvent e) {
        //System.out.println(e.getPoint().toString());
        try {
            int wheelRotation = e.getWheelRotation();
            Point p = e.getPoint();
            if (wheelRotation > 0) {
                if (zoomLevel < maxZoomLevel) {
                    zoomLevel++;
                    Point2D p1 = transformPoint(p);
                    coordTransform.scale(1 / zoomMultiplicationFactor, 1 / zoomMultiplicationFactor);
                    Point2D p2 = transformPoint(p);
                    coordTransform.translate(p2.getX() - p1.getX(), p2.getY() - p1.getY());
                    repaintAll();
                }
            } else {
                if (zoomLevel > minZoomLevel) {
                    zoomLevel--;
                    Point2D p1 = transformPoint(p);
                    coordTransform.scale(zoomMultiplicationFactor, zoomMultiplicationFactor);
                    Point2D p2 = transformPoint(p);
                    coordTransform.translate(p2.getX() - p1.getX(), p2.getY() - p1.getY());
                    repaintAll();

                }
            }
        } catch (NoninvertibleTransformException ex) {
            ex.printStackTrace();
        }
    }
    

    /**
     * Used to programmatically zoom the view.
     * Used by zoomIn, zoomOut and zoomAll actions
     * @param p
     * @param wheelRotation
     */
    public void zoomCamera(Point p, int wheelRotation) {
        //System.out.println(p.toString());
            try {
              if (wheelRotation > 0) {
            
                if (zoomLevel < maxZoomLevel) {
                    zoomLevel++;
                    Point2D p1 = transformPoint(p);
                    coordTransform.scale(1 / zoomMultiplicationFactor, 1 / zoomMultiplicationFactor);
                    Point2D p2 = transformPoint(p);
                    coordTransform.translate(p2.getX() - p1.getX(), p2.getY() - p1.getY());
                    repaintAll();
                }
            } else {
                if (zoomLevel > minZoomLevel) {
                    zoomLevel--;
                    
                    Point2D p1 = transformPoint(p);
                    coordTransform.scale(zoomMultiplicationFactor, zoomMultiplicationFactor);
                    Point2D p2 = transformPoint(p);
                    coordTransform.translate(p2.getX() - p1.getX(), p2.getY() - p1.getY());
                    repaintAll();

                }
            }
        } catch (NoninvertibleTransformException ex) {
            ex.printStackTrace();
        }

    }
    
    

    public Point2D.Double transformPoint(Point p1) throws NoninvertibleTransformException {
        AffineTransform inverse = coordTransform.createInverse();
        Point2D.Double p2 = new Point2D.Double();
        inverse.transform(p1, p2);
        return p2;
    }
    
    public Rectangle2D.Double transformRectangle(Rectangle2D rect){
        Point2D p1 = null;
        Point2D p2 = null;
        try {
            p1 = transformPoint(new Point((int) rect.getMinX(), (int) rect.getMinY()));
            p2 = transformPoint(new Point((int) rect.getMaxX(), (int) rect.getMaxY()));
        } catch (NoninvertibleTransformException ex) {
            Logger.getLogger(ZoomAndPanListener.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new Rectangle2D.Double(p1.getX(),p1.getY(),p2.getX() - p1.getX(),p2.getY() - p1.getY());
    }

    public AffineTransform getCoordTransform() {
        return coordTransform;
    }

    public void setCoordTransform(AffineTransform coordTransform) {
        this.coordTransform = coordTransform;
    }
    
    public void repaintAll(){
        for(int i = 0;i<components.length;i++){
            components[i].repaint();
        }
    }


}

