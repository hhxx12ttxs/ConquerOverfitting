import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.SeriesRenderingOrder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.DeviationRenderer;
// Render forward
XYPlot xyplot = (XYPlot)jfreechart.getPlot();
xyplot.setSeriesRenderingOrder(SeriesRenderingOrder.FORWARD);

