import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PiePlot3D;
ChartUtils.setChartTheme(chartModel);
// 用工厂类创建饼图
JFreeChart localJFreeChart = null;
if (StringUtils.equals(chartModel.getEnable3DFlag(), &quot;1&quot;)) {

