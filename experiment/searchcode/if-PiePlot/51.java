import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
public static JFreeChart createChart(boolean is3D,DefaultPieDataset dataSet) {
if(is3D){
chart = ChartFactory.createPieChart3D(
&quot;图书借阅统计&quot;,    // 标题

