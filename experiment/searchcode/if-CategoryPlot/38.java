import org.jfree.chart.axis.*;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
JFreeChart jfreechart = ChartFactory.createBarChart(&quot;CategoryLabelPositionsDemo1&quot;, &quot;Category&quot;, &quot;Value&quot;, categorydataset, PlotOrientation.VERTICAL, false, false, false);
CategoryPlot categoryplot = (CategoryPlot)jfreechart.getPlot();
CategoryAxis categoryaxis = categoryplot.getDomainAxis();

