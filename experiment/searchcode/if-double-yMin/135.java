package com.googlecode.gchart.gcharttestapp.client;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.dom.client.MouseWheelHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.googlecode.gchart.client.GChart;
import com.googlecode.gchart.client.HoverUpdateable;
/**
 * Example that adds an interactive pan and zoom capability to a
 * GChart. Basically, axis limits are adjusted appropriately in
 * response to mouse pan and zoom related activities. A zoom
 * selection cursor is implemented via a single-point box
 * curve configured appropriately. Hover widgets are used to
 * create +/- zoom control buttons that are auto-hidden when the
 * mouse is outside of the zoom selection cursor.
 * <p>
 *
 * Here's how the end user sees it:
 *
 * <ol>
 * 
 * <li> Panning: Users drag to pan in the standard manner.
 * 
 * <p>
 *
 * <li> The zooming features are best described in terms of new user
 * and experienced user zooming scenarios (other scenarios
 * exist).
 * <p>
 *
 * Typical new user zooming scenario:
 * <p>
 * 
 * <ol>
 *  <li>User clicks on chart, which creates default zoom cursor centered
 * on mouse half the size of the plot area
 *  <li>User uses + or - buttons at center of zoom-cursor to
 * zoom in or out. First zoom in or out auto-pans plot area so
 * that selection cursor is centered in plot area. Zoom in and
 * out are exact inverses of each other so they can always get
 * back to where they started from.
 *   <li>User moves mouse outside the selection cursor to make
 * +/- zoom controll buttons go away if they are occluding the view
 *   <li>User clicks anywhere on chart to dismiss selection cursor
 * or simply starts drag-panning which also auto-dismisses selection
 * cursor.
 * </ol>
 * 
 * <p>
 * Typical experienced user zooming scenario:
 * <p>
 * 
 * <ol>
 *  <li> User Drags while holding Ctrl key to create selection
 * rectangle (normally, no +/- buttons will appear because mouse
 * will be just outside selection rectangle after such a
 * drag-select).
 *
 *  <li> User uses mouse wheel to zoom in an out; behavior is exactly
 * the same as for clicking plus or minus zoom controller
 * buttons.
 *
 *  <li> Alt-Click immediately reverts to the initial plot area
 *  window, clearing any selection cursor (go back to initial
 *  state).
 * 
 * <li> User cancels zoom mode via click or dragging as described earlier.
 *
 * </ol>
 * </ol>
 * <p>
 *
 * This example was created in response to GChart issue #38. It
 * has the basic features of pan and zoom discussed in that
 * issue, but does not include every feature suggested by the
 * original author (no cross-hairs cursors, for example). This
 * example is intended to get you started. I expect that, with
 * some effort, all of the features discussed in issue #38, as
 * well as other features that some other GChart users may
 * require, can be added via appropriate code. If you come up with
 * useful variations on this pan and zoom theme, please share
 * your code or ideas via a comment to issue #38 or start a new
 * "how to do pan and zoom" related issue.
 * <p>
 *
 * Finally, note that the simple approach of this example
 * requires that all chart points be loaded into memory. Though
 * such limits are VERY client platform specific, on the slowest
 * client platforms (e.g. an old machine running IE6) you cannot
 * expect reasonable responsiveness with more than a thousand
 * points or so. Of course paging data in from a server, using
 * compressed data during zoom, and so on, could in principle
 * overcome such limits, but that's far beyond the scope of this
 * simple approach.
 *
 * 
 */
public class GChartExample24 extends GChart {
   final int N_POINTS = 100;
   final int SELECTION_CURVE; // curve index of selection cursor
   class Point {double x; double y;};
   Point p1 = new Point(); // first corner (@mousedown) of selection rect
   Point p2 = new Point(); // second corner (@mouseup) of selection rect
   boolean selecting = false;
   boolean moving = false;
   boolean ctrlPressed = false; // as evaluated at mouse down
   boolean altPressed = false;
   // (# zoom ins) - (# zoom outs) since selection rect created;
   // lets us know when to restore initial plot area limits/cursor
   int zoomIndex = 0;  
   final ZoomController zoomController = new ZoomController();
   // min plot area fraction zoom selection cursor must capture
   final double MIN_SELECTION_FRACTION_X = 0.1;
   final double MIN_SELECTION_FRACTION_Y = 0.1;

   class Region {double xMin; double xMax; double yMin; double yMax;}
   Region initialPlotRegion = new Region();
   Region initialSelectionRegion = new Region();
   
   class ZoomController extends HorizontalPanel
         implements HoverUpdateable {
	   final Button zoomIn = new Button("<big>+</big>",
				      new ClickHandler() {
			   public void onClick(ClickEvent event) {
			     zoomIn();}});
	   final Button zoomOut = new Button("<big>-</big>",
		       new ClickHandler() {
			   public void onClick(ClickEvent event) {
			     zoomOut();}});
      ZoomController() {
         zoomIn.setTitle(
           "Zoom in (expands selected region so it fills plot area)");
         zoomOut.setTitle(
           "Zoom out (shrinks plot area so it fits in selected region)");
		   add(zoomIn);
		   add(zoomOut);
	   }
      void zoomIn() {

        if (-1 == zoomIndex) { // return to starting (0 index) state
          getXAxis().setAxisMin(initialPlotRegion.xMin);
          getXAxis().setAxisMax(initialPlotRegion.xMax);
          getYAxis().setAxisMin(initialPlotRegion.yMin);
          getYAxis().setAxisMax(initialPlotRegion.yMax);
          p1.x = initialSelectionRegion.xMin;
          p2.x = initialSelectionRegion.xMax;
          p1.y = initialSelectionRegion.yMin;
          p2.y = initialSelectionRegion.yMax;
        }
        else {   
          double xMin = getXAxis().getAxisMin(); 
          double xMax = getXAxis().getAxisMax(); 
          double yMin = getYAxis().getAxisMin(); 
          double yMax = getYAxis().getAxisMax();
          if (0 == zoomIndex) { // moving away from starting state
              initialPlotRegion.xMin = xMin;
              initialPlotRegion.xMax = xMax;
              initialPlotRegion.yMin = yMin;
              initialPlotRegion.yMax = yMax;
              initialSelectionRegion.xMin = Math.min(p1.x, p2.x);
              initialSelectionRegion.xMax = Math.max(p1.x, p2.x);
              initialSelectionRegion.yMin = Math.min(p1.y, p2.y);
              initialSelectionRegion.yMax = Math.max(p1.y, p2.y);
              double dx = xMax - xMin;
              double dy = yMax - yMin;
              // center plot area on selection cursor
              xMin = (p1.x + p2.x - dx)/2;
              xMax = (p1.x + p2.x + dx)/2;
              yMin = (p1.y + p2.y - dy)/2;
              yMax = (p1.y + p2.y + dy)/2;                   
           }

           double pXMin = Math.min(p1.x, p2.x);
           double pXMax = Math.max(p1.x, p2.x);
           double pYMin = Math.min(p1.y, p2.y);
           double pYMax = Math.max(p1.y, p2.y);

           double newPXSize = (pXMax-pXMin)*(pXMax-pXMin)/(xMax - xMin);  
           double newPYSize = (pYMax-pYMin)*(pYMax-pYMin)/(yMax - yMin);  

           double xCenter = (p1.x + p2.x)/2;
           double yCenter = (p1.y + p2.y)/2;
           p1.x = xCenter - newPXSize/2;
           p2.x = xCenter + newPXSize/2;
           p1.y = yCenter - newPYSize/2;
           p2.y = yCenter + newPYSize/2;

           getXAxis().setAxisMin(pXMin);
           getXAxis().setAxisMax(pXMax);
           getYAxis().setAxisMin(pYMin);
           getYAxis().setAxisMax(pYMax);

        }
        updateCursor();
        zoomIndex++;
	   }
	   void zoomOut() {
        if (1 == zoomIndex) { // return to starting (0 index) state
          getXAxis().setAxisMin(initialPlotRegion.xMin);
          getXAxis().setAxisMax(initialPlotRegion.xMax);
          getYAxis().setAxisMin(initialPlotRegion.yMin);
          getYAxis().setAxisMax(initialPlotRegion.yMax);
          p1.x = initialSelectionRegion.xMin;
          p2.x = initialSelectionRegion.xMax;
          p1.y = initialSelectionRegion.yMin;
          p2.y = initialSelectionRegion.yMax;
        }
        else {   
           double xMin = getXAxis().getAxisMin(); 
           double xMax = getXAxis().getAxisMax(); 
           double yMin = getYAxis().getAxisMin(); 
           double yMax = getYAxis().getAxisMax();
           if (0 == zoomIndex) {
              initialPlotRegion.xMin = xMin;
              initialPlotRegion.xMax = xMax;
              initialPlotRegion.yMin = yMin;
              initialPlotRegion.yMax = yMax;
              initialSelectionRegion.xMin = Math.min(p1.x, p2.x);
              initialSelectionRegion.xMax = Math.max(p1.x, p2.x);
              initialSelectionRegion.yMin = Math.min(p1.y, p2.y);
              initialSelectionRegion.yMax = Math.max(p1.y, p2.y);
              double dx = xMax - xMin;
              double dy = yMax - yMin;
              double xCenter = (p1.x + p2.x)/2;
              double yCenter = (p1.y + p2.y)/2;
              // center plot area on selection cursor 
              xMin =  xCenter - dx/2;
              xMax =  xCenter + dx/2;
              yMin =  yCenter - dy/2;
              yMax =  yCenter + dy/2;                   
           }
           double pXMin = Math.min(p1.x, p2.x);
           double pXMax = Math.max(p1.x, p2.x);
           double pYMin = Math.min(p1.y, p2.y);
           double pYMax = Math.max(p1.y, p2.y);

           double newXSize = (xMax - xMin)*(xMax - xMin)/(pXMax-pXMin);  
           double newYSize = (yMax - yMin)*(yMax - yMin)/(pYMax-pYMin);  

           double dx = xMax - xMin;
           double dy = yMax - yMin;
           double xCenter = (p1.x + p2.x)/2;
           double yCenter = (p1.y + p2.y)/2;
           p1.x = xCenter - dx/2;
           p2.x = xCenter + dx/2;
           p1.y = yCenter - dy/2;
           p2.y = yCenter + dy/2;

           double newXMin = (xMin + xMax - newXSize)/2.0;
           double newXMax = (xMin + xMax + newXSize)/2.0;
           double newYMin = (yMin + yMax - newYSize)/2.0;
           double newYMax = (yMin + yMax + newYSize)/2.0;

           getXAxis().setAxisMin(newXMin);
           getXAxis().setAxisMax(newXMax);
           getYAxis().setAxisMin(newYMin);
           getYAxis().setAxisMax(newYMax);

           p1.x = xMin;
           p2.x = xMax;
           p1.y = yMin;
           p2.y = yMax;
        }
        zoomIndex--;
        updateCursor();
	   }
	public void hoverCleanup(
			com.googlecode.gchart.client.GChart.Curve.Point hoveredAwayFrom) {
	}
	public void hoverUpdate(
			com.googlecode.gchart.client.GChart.Curve.Point hoveredOver) {
		
	}
	  
   }
   
   void updateCursor() {
      double dx = p2.x - p1.x;
      double dy = p2.y - p1.y;
      if (!moving &&
               Math.abs(dx) >= MIN_SELECTION_FRACTION_X*
    		        (getXAxis().getAxisMax()-getXAxis().getAxisMin()) &&
               Math.abs(dy) >= MIN_SELECTION_FRACTION_Y*
                 (getYAxis().getAxisMax()-getYAxis().getAxisMin())) {
         getCurve(SELECTION_CURVE).setVisible(true);
         getCurve(SELECTION_CURVE).getSymbol().setSymbolType(
            SymbolType.BOX_CENTER);
         getCurve(SELECTION_CURVE).getSymbol().setBorderColor("gray");
         getCurve(SELECTION_CURVE).getSymbol().setBorderWidth(2);
         getCurve(SELECTION_CURVE).getSymbol().setModelWidth(Math.abs(dx));
         getCurve(SELECTION_CURVE).getSymbol().setModelHeight(Math.abs(dy));
         getCurve(SELECTION_CURVE).getPoint(0).setX((p1.x + p2.x)/2);
         getCurve(SELECTION_CURVE).getPoint(0).setY((p1.y + p2.y)/2);
      }
      else if (moving) {
        if (getCurve(SELECTION_CURVE).isVisible()) 
           getCurve(SELECTION_CURVE).setVisible(false);
        double xMin = getXAxis().getAxisMin() - dx;
        double xMax =  getXAxis().getAxisMax() - dx;
        double yMin =  getYAxis().getAxisMin() - dy;
        double yMax =  getYAxis().getAxisMax() - dy;
        getXAxis().setAxisMin(xMin); 
        getXAxis().setAxisMax(xMax); 
        getYAxis().setAxisMin(yMin); 
        getYAxis().setAxisMax(yMax);
      }
      
      
      update();
      
   }
   
   GChartExample24() {
      setChartTitle(
"Drag to pan; Press Ctrl while drag-selecting a rectangle to zoom"); 
     setChartSize(500, 150);
     Anchor a = new Anchor();
     a.setPixelSize(10, 500);
     getYAxis().setAxisLabel(a);
     // another option is to use clipToDecoratedChart(true) instead.
     setClipToPlotArea(true);
     addCurve();
     for (int i = 0; i < N_POINTS; i++) 
        getCurve().addPoint(i, Math.sin((2* Math.PI * i)/N_POINTS)*
                               Math.sin(10*(2* Math.PI * i)/N_POINTS));

     getCurve().getSymbol().setSymbolType(SymbolType.LINE);
     
     // will use this curve to create the selection cursor
     addCurve();
     getCurve().addPoint(-Double.MAX_VALUE, -Double.MAX_VALUE);
     getCurve().setVisible(false);
     // preferentially selects cursor over ordinary curves:
     getCurve().getSymbol().setDistanceMetric(0,0);
     getCurve().getSymbol().setHoverWidget(zoomController);
     getCurve().getSymbol().setHoverLocation(
       AnnotationLocation.CENTER);
     // hides hover-buttons when mouse is outside zoom-cursor
     getCurve().getSymbol().setBrushSize(0, 0);
     SELECTION_CURVE = getNCurves()-1;
     // give them some x-panning space
     getXAxis().setAxisMin(0.25*N_POINTS);
     getXAxis().setAxisMax(0.75*N_POINTS);
     
     getYAxis().setTickLabelThickness(50);
     getYAxis().setAxisMin(-0.5);
     getYAxis().setAxisMax(0.5);

/*     
     addClickHandler(new ClickHandler() {
       public void onClick(ClickEvent event) {
          double x = getXAxis().getMouseCoordinate();
          double y = getYAxis().getMouseCoordinate();
          if (altPressed) {
             getXAxis().setAxisMin(0.25*N_POINTS);
             getXAxis().setAxisMax(0.75*N_POINTS);
             getYAxis().setAxisMin(-0.5);
             getYAxis().setAxisMax(.5);
             getCurve(SELECTION_CURVE).setVisible(false);
             update();
          }
          else if (getCurve(SELECTION_CURVE).isVisible()) {
             p1.x = p2.x = x;
             p1.y = p2.y = y;
             getCurve(SELECTION_CURVE).setVisible(false);
             update();
          }
          else {
            double xMin = getXAxis().getAxisMin(); 
            double xMax = getXAxis().getAxisMax(); 
            double yMin = getYAxis().getAxisMin(); 
            double yMax = getYAxis().getAxisMax();
            p1.x = x - (xMax - xMin)/4; // half-size zoom cursor
            p2.x = x + (xMax - xMin)/4;
            p1.y = y - (yMax - yMin)/4;
            p2.y = y + (yMax - yMin)/4;
            moving = selecting = false;
            zoomIndex = 0;
            getCurve(SELECTION_CURVE).setVisible(true);
            updateCursor();
          }
          moving = selecting = false;
      }
    });
  */  
     addMouseDownHandler(new MouseDownHandler() {
        public void onMouseDown(MouseDownEvent event) {
           /*
            * Most browsers, by default, support the ability to
            * to "drag-copy" any web page image to the desktop.
            * But GChart's rendering makes extensive use of
            * images, so we need to override this default.
            * 
            */
           event.preventDefault();
           ctrlPressed = event.isControlKeyDown();
           altPressed = event.isAltKeyDown();
           double x = getXAxis().getMouseCoordinate();
           double y = getYAxis().getMouseCoordinate();
           if (Math.min(p1.x, p2.x) <= x &&
               x <= Math.max(p1.x, p2.x) &&
               Math.min(p1.y, p2.y) <= y &&
               y <= Math.max(p1.y, p2.y) )
              return; // ignore mouse down inside selection rectangle
           p1.x = p2.x = x;
           p1.y = p2.y = y;
          double xMin = getXAxis().getAxisMin(); 
          double xMax = getXAxis().getAxisMax(); 
          double yMin = getYAxis().getAxisMin(); 
          double yMax = getYAxis().getAxisMax();
          initialPlotRegion.xMin = xMin;
          initialPlotRegion.xMax = xMax;
          initialPlotRegion.yMin = yMin;
          initialPlotRegion.yMax = yMax;
          if (ctrlPressed) {
              selecting = true;
              moving = false;
           }
           else {
              selecting = false;
              moving = true;
           }
           updateCursor();
             
        }
     });
     addMouseMoveHandler(new MouseMoveHandler() {
        public void onMouseMove(MouseMoveEvent event) {
           event.preventDefault();
           if (selecting || moving) {
             p2.x = getXAxis().getMouseCoordinate();
             p2.y = getYAxis().getMouseCoordinate();
             updateCursor();
             if (moving) {
               p1.x = p2.x = getXAxis().getMouseCoordinate();
               p1.y = p2.y = getYAxis().getMouseCoordinate();
             }             
           }
        }
     });
     addMouseUpHandler(new MouseUpHandler() {
        public void onMouseUp(MouseUpEvent event) {
           event.preventDefault();
           if (selecting || moving) {
             p2.x = getXAxis().getMouseCoordinate();
             p2.y = getYAxis().getMouseCoordinate();
             updateCursor();
             selecting = false;
             moving = false;
           }  
        }
     });
     addMouseWheelHandler(new MouseWheelHandler() {
        public void onMouseWheel(MouseWheelEvent event) {
           event.preventDefault();
           if (getCurve(SELECTION_CURVE).isVisible()) {
              if (event.isNorth())
                 zoomController.zoomIn();
              else if (event.isSouth())
                 zoomController.zoomOut();
           }
        }
     });
     
  }
}

