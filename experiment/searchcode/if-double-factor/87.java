package com.googlecode.gchart.gcharttestapp.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.*;
import com.google.gwt.user.client.ui.*;

import com.googlecode.gchart.client.GChart;

/**
 * A sine curve with 500 points on it.<p>
 *
 * This example illustrates how you can use GWT's
 * <tt>DeferredCommand</tt> to bring up a large chart in several
 * stages so the user isn't stuck looking at a blank screen the
 * first time a large chart is displayed. Here we first display
 * the empty "graph paper" with a progress message on it ( which
 * comes up quite quickly) and later add the 500 point curve
 * to that chart.<p>
 *
 * As of version 2.1 the re-update of a chart's existing data
 * points with new data is usually around 5 times faster than the
 * very first update.  For example, on a 1.4GHz laptop running
 * Firefox 2 it took around 1250ms to render this chart the
 * first time, but only around 250ms to update the 500 points it
 * contains with new data values. Thus the deferred command
 * technique will often only be needed for the very first
 * "chart building" update.<p>
 * 
 */

public class GChartExample15c extends GChart {
   final int PERIOD = 100;   // of one complete sine wave cycle
   final int N_PERIODS = 30;  // number of complete sine waves shown
   final int DELTA_TIME = 1; // ==> 100 points per sine wave
   final int PIXELS_PER_UNITTIME = 2; // x-pixels per unit of time
   final int DELTA_PHASE = 10; // # of points scrolled per user click
   final int INITIAL_PHASE = 0; // phase shift of displayed sine wave
   int phase = INITIAL_PHASE;
   // size of the plot area, in pixels
   final int WIDTH = 1000;//PIXELS_PER_UNITTIME*PERIOD*N_PERIODS;
   final int HEIGHT = 50;                            
   
  final String SOURCE_CODE_LINK =
"<a href='GChartExample15.txt' target='_blank'>Source code</a>";
  final Button updateButton = new Button("<b>Update</b>");
  final Label updateTimeMsg = new Label(); 

  // Returns Grid with code link, update button, & timing message
  // shown at the bottom of Client-side GChart live-demo charts.
  private Grid getDemoFootnotes(String sourceCodeLink,
                                Widget updateWidget,
                                Label updateTimeMsg) {
        HTML sourceCode = new HTML(sourceCodeLink);
        Widget[] w = {sourceCode, updateWidget, updateTimeMsg};
        String[] wWidth = {"40%", "20%", "40%"};
        Grid result = new Grid(1, w.length);
        for (int i = 0; i < w.length; i++) {
          result.setWidget(0, i, w[i]);
          result.getCellFormatter().setWidth(0,i, wWidth[i]);
          result.getCellFormatter().setHorizontalAlignment(0,i, 
             HasHorizontalAlignment.ALIGN_CENTER);
        }
        result.setWidth("100%");
        return result;
  }
  
   void updateChart(int phase) {
      int nExisting = getCurve(0).getNPoints();
      final double FACTOR = 2*Math.PI/PERIOD;
      for (int i = 0; i*DELTA_TIME < PERIOD*N_PERIODS; i++) {
         double y = Math.sin(FACTOR*(i*DELTA_TIME+phase));
         if (i < nExisting)
            getCurve(0).getPoint(i).setY(y);
         else
            getCurve(0).addPoint(i, y);
      }
   }

   GChartExample15c() {
      super();
      long t0 = System.currentTimeMillis();
      setChartSize(WIDTH,HEIGHT);
      setPadding("5px 0px 0px 0px");
      setBorderWidth("1px");
      setBorderStyle("solid");
      setBorderColor("black");

      setChartTitle("<b><i>A 3000 Data Point Client-side GChart</i></b>");
      Button left = new Button("<big>&lt;</big>");
      left.setTitle("Left");
       left.addClickHandler(new ClickHandler() {
         public void onClick(ClickEvent event) {
            long t0 = System.currentTimeMillis();
            phase -= DELTA_PHASE;
            updateChart(phase);
            update(); // fastest update, because only y-values change
            long t1 = System.currentTimeMillis();
            updateTimeMsg.setText((t1-t0)+"ms");        
         }
      });
      Button right = new Button("<big>&gt;</big>");
      right.setTitle("Right");
      right.addClickHandler(new ClickHandler() {
         public void onClick(ClickEvent event) {
            long t0 = System.currentTimeMillis();
            phase += DELTA_PHASE;
            updateChart(phase);
            update();
            long t1 = System.currentTimeMillis();
            updateTimeMsg.setText((t1-t0)+"ms");
         }
      });
      FlowPanel scrollButtons = new FlowPanel();
      scrollButtons.add(left);
      scrollButtons.add(right);
      // GChart leaves a little space for even an empty x-axis
      // label. To remove this space, we put footnotes in the
      // x-axis label (instead of using setChartFootnotes):
      getXAxis().setAxisLabel(getDemoFootnotes(
         SOURCE_CODE_LINK, scrollButtons, updateTimeMsg));
      getXAxis().getAxisLabel().setWidth(
         WIDTH+"px");
      getXAxis().setHasGridlines(true);
      getXAxis().setTickCount(N_PERIODS + 1);
      getXAxis().setTickLabelFormat("#");
      getXAxis().setAxisMin(0);
      getXAxis().setAxisMax(PERIOD*N_PERIODS);
      getXAxis().setTickLabelFontSize(10);
      getXAxis().setTickLabelThickness(0);

      getYAxis().setHasGridlines(true);
      getYAxis().setTickCount(5); // ticks at: -1, -0.5, 0, 0.5, 1
      getYAxis().setAxisMin(-1);
      getYAxis().setAxisMax(1);
      getYAxis().setTickLabelThickness(10);
      getYAxis().setTicksPerLabel(2); // labels at: -1, 0, 1
      getYAxis().setTickLabelFontSize(10);
      getYAxis().setTickLabelThickness(0);

      addCurve();
      getCurve().getSymbol().setSymbolType(
        SymbolType.VBAR_BASELINE_CENTER);
      getCurve().getSymbol().setBorderWidth(1); 
      getCurve().getSymbol().setBorderColor("white");
      getCurve().getSymbol().setBackgroundColor("blue");
      getCurve().getSymbol().setWidth(3); // height defined by y

      // Create a center-of-chart "loading" message
      addCurve();
      // we only need the annotation, so don't display a symbol
      getCurve().getSymbol().setSymbolType(SymbolType.NONE);  
      getCurve().addPoint(PERIOD*N_PERIODS/2, 0); // plot-centered
// Without the <html> prefix, it renders as plain-text.
// Setting width, height upperbounds based on plot area size
// guarantees proper centering as long as message fits in plot area.
      getCurve().getPoint().setAnnotationText(
        "<html><b style='color: red; " + 
        "background: white; font-size:15; " +
        "border: solid black 1px; padding: 5px;'>" + 
        "Loading a 500 point curve, please wait...</b>",
        getXChartSize(), getYChartSize());
      getCurve().getPoint().setAnnotationLocation(
         AnnotationLocation.CENTER);
      // update now builds empty chart with loading msg quickly:
      update(); 
      long t1 = System.currentTimeMillis();
      updateTimeMsg.setText((t1-t0)+"ms");     
      // add 500 point sine curve in a deferred command so the
      // user can see the empty chart and the loading message.
      DeferredCommand.addCommand(new Command() {
         public void execute() {
            long t0 = System.currentTimeMillis();
            removeCurve(1);     // zap the loading message   
            updateChart(INITIAL_PHASE); // adds 500 points
            long t1 = System.currentTimeMillis();
            updateTimeMsg.setText((t1-t0)+"ms");     
         }
      });

   }
}

