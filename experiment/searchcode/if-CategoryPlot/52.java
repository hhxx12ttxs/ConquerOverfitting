import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
JFreeChart jfreechart = null;
if(!list.isEmpty()){
logger.info(&quot;list is not empty&quot;);
jfreechart = createChart(getDataSet(list),title,name);

