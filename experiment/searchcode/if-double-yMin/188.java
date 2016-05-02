/** ChartActivity.java
 * 
 * Author:      James Browning, m140726@usna.edu
 * Date:        1 May 2014
 * Description: Implementation of Android library AChartEngine
 *              to render charts showing MPG vs. Odometer.
 */

package com.jamboix.autoassist;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

public class ChartActivity extends Activity {

GraphicalView mChart;
String FILENAME = "vehicle_list";
ArrayList<Vehicle> vehicles;
FileInputStream fis;
ObjectInputStream ois;
int position;
double xMin, xMax, yMin, yMax;
int scale;
GraphicalView chartView;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);
        
        getList();
        Intent intent = getIntent();
        position = intent.getIntExtra("position", 1);
       
        openChart();
    }
 
    /** 
     * This method gets the list of vehicles from the saved file.
     */
    @SuppressWarnings("unchecked")
    private void getList() {
       File file = getFileStreamPath(FILENAME);
   
       if (!file.exists()) {
          try {
             file.createNewFile();
             vehicles = new ArrayList<Vehicle>();
          } catch (Exception e) {
             e.printStackTrace();
          }
       }
       else {
          try {
             fis = openFileInput(FILENAME);
             ois = new ObjectInputStream(fis);
             vehicles = (ArrayList<Vehicle>) ois.readObject();
             return;
          }
          catch (Exception e) {
             e.printStackTrace();
          }
       }
       vehicles = new ArrayList<Vehicle>();
    }
    
    /** 
     * This opens the chart. Some code taken from AChartEngine's reference pages.
     */
    private void openChart() {
       LinearLayout layout = (LinearLayout) findViewById(R.id.chart);
       chartView = ChartFactory.getLineChartView(this,
             getDataset(), getRenderer());
       layout.addView(chartView);
    }
    
    /** 
     * This method defines the data set which will be rendered using the
     * AChartEngine library. Some code borrowed from their reference pages.
     */
    private XYMultipleSeriesDataset getDataset() {
       String[] parts;
       double x, y, lastX = 0;
       XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
       XYSeries series = new XYSeries("Fueling Entry");
       if(vehicles.get(position).getFuelEntries().size() < 2) {
          Toast.makeText(getBaseContext(), 
                "You need at least two fuel entries to display average MPG.", 
                Toast.LENGTH_LONG).show();
       }
       for (int i = 0; i < vehicles.get(position).getFuelEntries().size(); i++) {
          parts = vehicles.get(position).getFuelEntries().get(i).split("\n|:");
          y = Double.valueOf(parts[4]);
          x = Double.valueOf(parts[6]);
          
          // This is the math to calculate average MPG.
          if(i != 0) {
             y = ((x - lastX) / y);
             series.add(x, y);
          }
          lastX = x;
          
          // This bit of code is used to make sure the chart wil be centered on the data points.
          if(i == 1) {
             xMin = xMax = x;
             yMin = yMax = y;
          }
          else {
             if(x > xMax) {
                xMax = x;
             }
             if(x < xMin) {
                xMin = x;
             }
             if(y > yMax) {
                yMax = y;
             }
             if(y < yMin) {
                yMin = y;
             }
          }
          if(x > 100000) {
             scale = 10000;
          }
          else if(x > 10000) {
             scale = 1000;
          }
          else if(x > 1000) {
             scale = 100;
          }
          else if(x > 100) {
             scale = 10;
          }
          else {
             scale = 5;
          }
       }
       dataset.addSeries(series);
       return dataset;
    }
    
    /** 
     * This method creates the AChartEngine renderer object used to draw the chart.
     */
    private XYMultipleSeriesRenderer getRenderer() {
       XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
       renderer.setAxisTitleTextSize(50);
       renderer.setChartTitleTextSize(70);
       renderer.setLabelsTextSize(30);
       renderer.setLegendTextSize(50);
       renderer.setPointSize(10f);
       renderer.setMargins(new int[] { 100, 175, 100, 150 });
       XYSeriesRenderer r = new XYSeriesRenderer();
       r.setColor(Color.CYAN);
       r.setPointStyle(PointStyle.CIRCLE);
       r.setFillPoints(true);
       renderer.addSeriesRenderer(r);
       setChartSettings(renderer);
       return renderer;
    }
    
    /** 
     * This method defines a renderer's settings, or how we want the chart to look.
     *
     * @param  XYMultipleSeriesRenderer   Renderer created by getRenderer()
     */
    private void setChartSettings(XYMultipleSeriesRenderer renderer) {
       renderer.setChartTitle("Gallons vs Odometer");
       renderer.setXTitle("Odometer Value");
       renderer.setYTitle("Average Miles Per Gallon");
       renderer.setRange(new double[] {0,6,-70,40});
       renderer.setFitLegend(false);
       renderer.setAxesColor(Color.WHITE);
       renderer.setBackgroundColor(Color.BLACK);
       renderer.setApplyBackgroundColor(true);
       renderer.setGridColor(Color.WHITE);
       renderer.setShowGrid(true);
       renderer.setYLabelsPadding(50);
       renderer.setXAxisMin(xMin - scale);
       renderer.setXAxisMax(xMax + scale);
       renderer.setYAxisMin(yMin - 5);
       renderer.setYAxisMax(yMax + 5);
       renderer.setZoomEnabled(false);
     }
}

