import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PiePlot3D;

import org.jfree.chart.plot.RingPlot;
PieDataset dataset=createDataset();
switch(mode){
case pie:{
if(is3D){
jFreeChart=ChartFactory.createPieChart3D(title, dataset, legend, tooltips, urls);

