import euclid.PlotTypes.Line;
import euclid.PlotTypes.LinePlot;
import euclid.PlotTypes.ScatterPlot;
for (Plot2D item : plot){
whichToPlot(cr, item);
}
if (plot.size() ==0)
System.err.println(&quot;Plotter: nothing to plot&quot;);

