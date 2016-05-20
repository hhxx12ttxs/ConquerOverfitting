package com.googlecode.gchart.gcharttestapp.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;

import com.googlecode.gchart.client.GChart;

/**
 * This example displays a sine and cosine curve at various
 * sizes and levels of detail to illustrate how you can control
 * the performance vs. chart-quality tradeoffs inherent in
 * using Client-side GChart's LINE Symbol type. 
 *
 * <p>
 *
 * Unlike bar charts, line charts are not ideally suited for
 * Client-side GChart's simple "just use rectangular widgets"
 * implementation strategy. The basic problem: many of the line
 * charts you might want to create can require adding thousands
 * HTML elements to the DOM, and this simply takes too long on a
 * typical client machine.
 * 
 * <p> A simple way to characterize these limitations is to say
 * that, with a Client-side GChart line chart, you get to pick
 * only two of the following three desirable attributes:
 *
 * <ol>
 *   <li>High resolution (1px) solid, connecting lines.
 *   <li>Wide (600px or more) plot area.
 *   <li>Fast (less than 300ms) update times.
 * </ol>
 * <p>
 *
 * To illustrate these tradeoffs, this example lets you see the
 * same sine/cosine chart, but configured so as to produce the
 * following three possible "you pick two" combinations:
 * 
 * <p>
 *
 * <ol>
 *   <li>A large sized, low resolution, fast updating chart
 *   <li>A large sized, high resolution, slow updating chart
 *   <li>A small sized, high resolution, fast updating chart
 * </ol>
 * <p>
 *
 * <p> As you click the button at the bottom of the chart, it
 * cycles through these three options. As you run the demo, note
 * how the displayed update times, chart size, and chart quality
 * vary on the client platforms of interest to you.  Of course,
 * everything's relative to the capabilities of your client
 * platform: you can always get big, fast, high resolution line
 * charts if your visitors all have client platforms a lot more
 * powerful than the ones I used to test this example.
 *
 */

public class GChartExample15d extends GChart {
   final int PERIOD = 200;    // of one complete sine wave cycle
   final int N_PERIODS = 4;   // number of complete sine waves shown
   final int DELTA_TIME = 10; // time between line-connected points
   final int DELTA_PHASE = 10;// time phase shift per user click
   // use large positive phase so we can safely ignore phase < 0.
   int phase = PERIOD*(Integer.MAX_VALUE/PERIOD)/2; 

   // size of the plot area, in pixels
   final int BIG_WIDTH = 1000; 
   final int SMALL_WIDTH = 200; 
   final int HEIGHT = 100;                            
   final String SOURCE_CODE_LINK =
	   "<a href='GChartExample15b.txt' target='_blank'>Source code</a>";
   final Label updateTimeMsg = new Label(); 
   final String CANVAS_LABEL = "<big>Big,HiRes via Canvas</big>";
   final String CANVAS_TITLE = 
"Click for a big, detailed, very fast canvas-rendered chart.";
   final String LESS_DETAILED_LABEL = "<big>Big,LoRes via HTML</big>";
   final String LESS_DETAILED_TITLE = 
"Click to use a big, low resolution, fast HTML-rendered chart.";
   final String MORE_DETAILED_LABEL = "<big>Big,HiRes via HTML</big>";
   final String MORE_DETAILED_TITLE =
"Click to use a big, high resolution, slow HTML-rendered chart.";
   final String SMALLER_LABEL = "<big>Small,HiRes via HTML</big>";
   final String SMALLER_TITLE = 
"Click to use a small, high resolution, fast HTML-rendered chart.";
   final Button modeSwitcherButton = new Button();
// start in the fastest mode, to speed up first-time display
   String currentMode = CANVAS_LABEL;

   // Returns Grid with code link, update button, & timing message
   // shown at the bottom of Client-side GChart live-demo charts.
   private Grid getDemoFootnotes(String sourceCodeLink,
                                 Widget updateWidget,
                                 Label updateTimeMsg) {
         HTML sourceCode = new HTML(sourceCodeLink);
         Widget[] w = {sourceCode, updateWidget, updateTimeMsg};
         String[] wWidth = {"35%", "30%", "35%"};
         Grid result = new Grid(1, w.length);
         for (int i = 0; i < w.length; i++) {
           result.setWidget(0, i, w[i]);
           result.getCellFormatter().setWidth(0,i, wWidth[i]);
           result.getCellFormatter().setHorizontalAlignment(0,i, 
              HasHorizontalAlignment.ALIGN_CENTER);
         }
         result.setWidth(getXChartSizeDecorated()+"px");
         return result;
   }
   // adds or updates sin and cos curves on the chart
   void updateCurves(int phase) {
      int nExisting = getCurve(0).getNPoints();
      final double FACTOR = 2*Math.PI/PERIOD;
      for (int i = 0; i*DELTA_TIME <= PERIOD*N_PERIODS; i++) {
         double ySin = Math.sin(FACTOR*(i*DELTA_TIME+phase));
         double yCos = Math.cos(FACTOR*(i*DELTA_TIME+phase));
         if (i < nExisting) {
// save time by not setting x (exploits regular x-spacing)
            getCurve(0).getPoint(i).setY(ySin);
            getCurve(1).getPoint(i).setY(yCos);
         }
         else {
            getCurve(0).addPoint(i*DELTA_TIME, ySin);
            getCurve(1).addPoint(i*DELTA_TIME, yCos);
         }
      }
   }

   // responds appropriately to a click on the "mode switcher" button
   void switchMode() {   
      if (currentMode==CANVAS_LABEL) {
         // flip into the larger, more detailed, slow updating mode
         setChartSize(BIG_WIDTH, HEIGHT);  
         for (int i=0; i < 2; i++) {               
            getCurve(i).getSymbol().setWidth(0); 
            getCurve(i).getSymbol().setHeight(0);
            getCurve(i).getSymbol().setFillSpacing(1);
            getCurve(i).getSymbol().setFillThickness(1);
            getCurve(i).getSymbol().setSymbolType(SymbolType.LINE);
         }
         
         modeSwitcherButton.setHTML(MORE_DETAILED_LABEL);
         modeSwitcherButton.setTitle(MORE_DETAILED_TITLE);
         currentMode = MORE_DETAILED_LABEL;
      }
      else if (currentMode==MORE_DETAILED_LABEL) {
         // flip into the larger, more detailed, slow updating mode
         setChartSize(BIG_WIDTH, HEIGHT);  
         for (int i=0; i < 2; i++) {               
            getCurve(i).getSymbol().setWidth(5); 
            getCurve(i).getSymbol().setHeight(5);
            getCurve(i).getSymbol().setFillSpacing(1);
            getCurve(i).getSymbol().setFillThickness(1);
            getCurve(i).getSymbol().setSymbolType(SymbolType.LINE);
         }
         modeSwitcherButton.setHTML(SMALLER_LABEL);
         modeSwitcherButton.setTitle(SMALLER_TITLE);
         currentMode = SMALLER_LABEL;
      }
      else if (currentMode==SMALLER_LABEL) {
         // flip into the smaller, more detailed, fast updating mode
         setChartSize(SMALL_WIDTH, HEIGHT);  
         for (int i=0; i < 2; i++) {               
            getCurve(i).getSymbol().setWidth(5); 
            getCurve(i).getSymbol().setHeight(5);
            getCurve(i).getSymbol().setFillSpacing(1);
            getCurve(i).getSymbol().setFillThickness(1);
            getCurve(i).getSymbol().setSymbolType(SymbolType.LINE);
         }
         modeSwitcherButton.setHTML(LESS_DETAILED_LABEL);
         modeSwitcherButton.setTitle(LESS_DETAILED_TITLE);
         currentMode = LESS_DETAILED_LABEL;
      }
      else if (currentMode==LESS_DETAILED_LABEL) {
         // flip into the larger, less detailed, fast updating mode
         setChartSize(BIG_WIDTH, HEIGHT);  
         for (int i=0; i < 2; i++) {
            getCurve(i).getSymbol().setWidth(0); 
            getCurve(i).getSymbol().setHeight(0);
            getCurve(i).getSymbol().setFillSpacing(10);
            getCurve(i).getSymbol().setFillThickness(10);
            getCurve(i).getSymbol().setSymbolType(SymbolType.LINE);
         }
         modeSwitcherButton.setHTML(CANVAS_LABEL);
         modeSwitcherButton.setTitle(CANVAS_TITLE);
         currentMode = CANVAS_LABEL;
      }
   }
   
   GChartExample15d() {
      long t0 = System.currentTimeMillis();
      setChartSize(BIG_WIDTH,HEIGHT);
      setPadding("5px 0px 0px 0px");
      setBorderWidth("1px");
      setBorderStyle("solid");
      setBorderColor("black");
      setChartTitle("<b><i>Sine and Cosine</i></b>");
      setChartTitleThickness(30);
      // add buttons for scrolling left or right
      Button leftScrollerButton = new Button("<big>&lt;</big>");
      leftScrollerButton.setTitle("Scroll left");
      leftScrollerButton.addClickHandler(new ClickHandler() {
         public void onClick(ClickEvent event) {
            long t0 = System.currentTimeMillis();
            phase -= DELTA_PHASE;
            updateCurves(phase);
            update(); 
            long t1 = System.currentTimeMillis();
            updateTimeMsg.setText((t1-t0)+"ms");        
         }
      });
      modeSwitcherButton.addClickHandler(new ClickHandler() {
         public void onClick(ClickEvent event) {
            long t0 = System.currentTimeMillis();
            switchMode();
            updateCurves(phase);
            update();
            long t1 = System.currentTimeMillis();
            updateTimeMsg.setText((t1-t0)+"ms");
         }
      });
      Button rightScrollerButton = new Button("<big>&gt;</big>");
      rightScrollerButton.setTitle("Scroll right");
      rightScrollerButton.addClickHandler(new ClickHandler() {
         public void onClick(ClickEvent event) {
            long t0 = System.currentTimeMillis();
            phase += DELTA_PHASE;
            updateCurves(phase);
            update();
            long t1 = System.currentTimeMillis();
            updateTimeMsg.setText((t1-t0)+"ms");        
         }
      });
      HorizontalPanel buttonPanel = new HorizontalPanel();
      buttonPanel.add(leftScrollerButton);
      buttonPanel.add(modeSwitcherButton);
      buttonPanel.add(rightScrollerButton);
      setChartFootnotes(getDemoFootnotes(
    	         SOURCE_CODE_LINK, buttonPanel, updateTimeMsg));
      setChartFootnotesThickness(30);

      // configure x-axis 
      getXAxis().setAxisMin(0);
      getXAxis().setAxisMax(PERIOD*N_PERIODS);
      getXAxis().setHasGridlines(true);
      getXAxis().setTickCount(2*N_PERIODS + 1); // half-period ticks
      getXAxis().setTicksPerGridline(2);        // full-period grid 
      getXAxis().setTickLabelFontSize(10);
      getXAxis().setAxisLabelThickness(20);

      // configure y-axis
      getYAxis().setAxisMin(-1);
      getYAxis().setAxisMax(1);
      getYAxis().setHasGridlines(true);
      getYAxis().setTickCount(5); // ticks at: -1, -0.5, 0, 0.5, 1
      getYAxis().setTicksPerLabel(2); // labels at: -1, 0, 1
      getYAxis().setTickLabelThickness(10);
      getYAxis().setTickLabelFontSize(10);
      getYAxis().setAxisLabelThickness(20);

      // add/configure sine curve
      addCurve();
      getCurve().getSymbol().setSymbolType(SymbolType.LINE);
      getCurve().getSymbol().setBorderWidth(0); 
      getCurve().getSymbol().setBackgroundColor("blue");
      getCurve().setLegendLabel("sin");

      // add/configure cosine curve
      addCurve();
      getCurve().getSymbol().setSymbolType(SymbolType.LINE);
      getCurve().getSymbol().setBorderWidth(0); 
      getCurve().getSymbol().setBackgroundColor("red");
      getCurve().setLegendLabel("cos");

      setCanvasFactory(new GWTCanvasBasedCanvasFactory());
      
      switchMode();
      updateCurves(phase); 
      update();
      long t1 = System.currentTimeMillis();
      updateTimeMsg.setText((t1-t0)+"ms"); 
   }
}

