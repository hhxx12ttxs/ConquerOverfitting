package com.googlecode.gchart.gcharttestapp.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;

import com.googlecode.gchart.client.GChart;

/**
 * A sine and a cosine curve, drawn with GChart 2.11's new
 * LINE SymbolType.
 * <p>
 * 
 * The sin curve uses a fillSpacing of 1px, and is thus smoother
 * than the cos curve, which uses a fillSpacing of 2px (the two
 * curves also have different fillThickness settings, 1px vs.
 * 5px) But, the grainy cos curve uses half the HTML elements
 * and thus requires half the memory and updates in about half
 * the time.
 * <p>
 *
 * Also illustrates the use of a zero-width slice to anchor
 * annotations at a fixed location below the pie used
 * to display the phase angle.
 * 
 */

public class GChartExample15b extends GChart {
   final int PERIOD = 200;    // of one complete sine wave cycle
   final int N_PERIODS = 4;   // number of complete sine waves shown
   final int DELTA_TIME = 10; // time between line-connected points
   final int DELTA_PHASE = 10;// time phase shift per user click
   // start w. large positive phase so we can assume phase > 0.
   int phase = PERIOD*(Integer.MAX_VALUE/PERIOD)/2; 

   // size of the plot area, in pixels
   final int WIDTH = 1000;
   final int HEIGHT = 200;                            

   // adds or updates sin and cos curves on the chart
   void updateCurves(int phase) {
      int nExisting = getCurve(0).getNPoints();
      final double FACTOR = 2*Math.PI/PERIOD;
      for (int i = 0; i*DELTA_TIME <= PERIOD*N_PERIODS; i++) {
         double ySin = Math.sin(FACTOR*(i*DELTA_TIME+phase));
         double yCos = Math.cos(FACTOR*(i*DELTA_TIME+phase));
         if (i < nExisting) {
            getCurve(0).getPoint(i).setY(ySin);
            getCurve(1).getPoint(i).setY(yCos);
         }
         else {
            getCurve(0).addPoint(i*DELTA_TIME, ySin);
            getCurve(1).addPoint(i*DELTA_TIME, yCos);
         }
      }

      // update phase-displaying pie
      getCurve(2).getSymbol().setPieSliceSize(
           (phase % PERIOD)/(0. + PERIOD));
      getCurve(3).getSymbol().setPieSliceSize(
           1.0 - (phase % PERIOD)/(0. + PERIOD));
      getCurve(4).getPoint(0).setAnnotationText("phase="+
        Math.round(100*getCurve(2).getSymbol().getPieSliceSize())+"%");
      getCurve(2).getSymbol().setHovertextTemplate(
         GChart.formatAsHovertext(
         getCurve(4).getPoint(0).getAnnotationText()));
      getCurve(3).getSymbol().setHovertextTemplate(
         GChart.formatAsHovertext(
         getCurve(4).getPoint(0).getAnnotationText()));
      getCurve(4).getSymbol().setHovertextTemplate(
         getCurve(4).getPoint(0).getAnnotationText());
   }

   GChartExample15b() {
      setChartSize(WIDTH,HEIGHT);
      setBorderWidth("1px");
      setBorderStyle("solid");
      setBorderColor("black");
      setChartTitle("<b><i>Sine and Cosine</i></b>");
//      setShowOffChartPoints(true); // so we can see phase angle pie
      
      // add buttons for shifting the viewport left or right
      Button left = new Button("<big>&lt;</big>");
      left.setTitle("Left");
      left.addClickHandler(new ClickHandler() {
         public void onClick(ClickEvent event) {
            phase -= DELTA_PHASE;
            updateCurves(phase);
            update(); 
         }
      });
      final String MORE_DETAILED_LABEL = "<big>More Detailed</big>";
      final String LESS_DETAILED_LABEL = "<big>Less Detailed</big>";
      final String MORE_DETAILED_TITLE =
"Click to increase curve detail (updates will go slower)";
      final String LESS_DETAILED_TITLE = 
"Click to decrease curve detail (updates will go faster)";
      final Button resolution = new Button(MORE_DETAILED_LABEL);
      final int MIN_SPACING = 1;
      final int MAX_SPACING = 10;
      resolution.setTitle(MORE_DETAILED_TITLE);
      resolution.addClickHandler(new ClickHandler() {
         public void onClick(ClickEvent event) {
            if (getCurve(0).getSymbol().getFillSpacing()==MAX_SPACING) {
              // flip into more detailed mode   
              for (int i=0; i < 2; i++) {               
                getCurve(i).getSymbol().setWidth(5); 
                getCurve(i).getSymbol().setHeight(5);
                getCurve(i).getSymbol().setFillSpacing(MIN_SPACING);
                getCurve(i).getSymbol().setFillThickness(MIN_SPACING);
              }
              resolution.setHTML(LESS_DETAILED_LABEL);
              resolution.setTitle(LESS_DETAILED_TITLE);
            }
            else {   // flip into less detailed mode
              for (int i=0; i < 2; i++) {               
                getCurve(i).getSymbol().setWidth(0); 
                getCurve(i).getSymbol().setHeight(0);
                getCurve(i).getSymbol().setFillSpacing(MAX_SPACING);
                getCurve(i).getSymbol().setFillThickness(MAX_SPACING);
              }
              resolution.setHTML(MORE_DETAILED_LABEL);
              resolution.setTitle(MORE_DETAILED_TITLE);
            }
            updateCurves(phase);
            update();
         }
      });
      Button right = new Button("<big>&gt;</big>");
      right.setTitle("Right");
      right.addClickHandler(new ClickHandler() {
         public void onClick(ClickEvent event) {
            phase += DELTA_PHASE;
            updateCurves(phase);
            update();
         }
      });
      FlowPanel scrollButtons = new FlowPanel();
      scrollButtons.add(left);
      scrollButtons.add(resolution);
      scrollButtons.add(right);
      setChartFootnotes(scrollButtons);

      // configure x-axis 
      getXAxis().setAxisMin(0);
      getXAxis().setAxisMax(PERIOD*N_PERIODS);
      getXAxis().setHasGridlines(true);
      getXAxis().setTickCount(2*N_PERIODS + 1); // half-period ticks
      getXAxis().setTicksPerGridline(2);        // full-period grid 

      // configure y-axis
      getYAxis().setAxisMin(-1);
      getYAxis().setAxisMax(1);
      getYAxis().setHasGridlines(true);
      getYAxis().setTickCount(5); // ticks at: -1, -0.5, 0, 0.5, 1
      getYAxis().setTicksPerLabel(2); // labels at: -1, 0, 1

      // add/configure sine curve
      addCurve();
      getCurve().setLegendLabel("Sine");
      getCurve().getSymbol().setSymbolType(SymbolType.LINE);
      getCurve().getSymbol().setBorderWidth(0); 
      getCurve().getSymbol().setBackgroundColor("blue");
      getCurve().getSymbol().setWidth(0); 
      getCurve().getSymbol().setHeight(0);
      getCurve().getSymbol().setFillSpacing(10);
      getCurve().getSymbol().setFillThickness(10);

      // add/configure cosine curve
      addCurve();
      getCurve().setLegendLabel("Cosine");
      getCurve().getSymbol().setSymbolType(SymbolType.LINE);
      getCurve().getSymbol().setBorderWidth(0); 
      getCurve().getSymbol().setBackgroundColor("red");
      getCurve().getSymbol().setWidth(0);  // removes individual data 
      getCurve().getSymbol().setHeight(0); // point markers
      // it's grainy, but renders twice as fast with 2 instead of 1
      getCurve().getSymbol().setFillSpacing(10);
      // thicker curve costs nothing and hides stair-steps a bit
      getCurve().getSymbol().setFillThickness(10);
      
      // use a small pie chart to display phase angle

      final double DIAMETER = 30;
      final double PIE_CENTER_X = N_PERIODS*PERIOD+3*DIAMETER/2;
      final double PIE_CENTER_Y = 0.75;
      // phase angle slice
      addCurve();
      getCurve().getSymbol().setSymbolType(
        SymbolType.PIE_SLICE_OPTIMAL_SHADING);
      getCurve().addPoint(PIE_CENTER_X, PIE_CENTER_Y);
      getCurve().getSymbol().setBorderWidth(1);
      // between white and blue to soften pie edges
      getCurve().getSymbol().setBorderColor("#77F");
      getCurve().getSymbol().setBackgroundColor("#00F");
      // define pie diameter in x-axis model units
      getCurve().getSymbol().setModelWidth(DIAMETER);
      getCurve().getSymbol().setHeight(0);
      getCurve().getSymbol().setFillSpacing(3);
      getCurve().getSymbol().setFillThickness(3);

      // non-phase angle slice ("empty part" of pie)
      addCurve();
      getCurve().getSymbol().setSymbolType(
        SymbolType.PIE_SLICE_OPTIMAL_SHADING);
      getCurve().addPoint(PIE_CENTER_X, PIE_CENTER_Y);
      getCurve().getSymbol().setBorderWidth(1);
      // between white and gray to soften pie edges
      getCurve().getSymbol().setBorderColor("#DDD");
      getCurve().getSymbol().setBackgroundColor("#BBB");
      // define pie diameter in x-axis model units
      getCurve().getSymbol().setModelWidth(30);
      getCurve().getSymbol().setHeight(0);
      getCurve().getSymbol().setFillSpacing(3);
      getCurve().getSymbol().setFillThickness(3);

      // zero-sized slice to hold pie label
      addCurve();
      getCurve().getSymbol().setPieSliceSize(0);
      getCurve().getSymbol().setSymbolType(
        SymbolType.PIE_SLICE_OPTIMAL_SHADING);
      getCurve().addPoint(PIE_CENTER_X, PIE_CENTER_Y);
      getCurve().getPoint().setAnnotationLocation(
         AnnotationLocation.OUTSIDE_PIE_ARC);
      getCurve().getPoint().setAnnotationFontSize(10);
      getCurve().getPoint().setAnnotationXShift(3);
      // define pie diameter in x-axis model units
      getCurve().getSymbol().setModelWidth(30);
      getCurve().getSymbol().setHeight(0);
      
      updateCurves(phase); 

   }
}

