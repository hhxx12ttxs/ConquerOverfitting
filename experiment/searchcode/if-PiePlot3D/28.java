import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.data.general.DefaultPieDataset;
JFreeChart jfreechart = ChartFactory.createPieChart3D(&quot;Statitic&quot;, piedataset, true, false, false);
PiePlot3D pieplot3d = (PiePlot3D) jfreechart.getPlot();

