import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.renderer.category.BarRenderer;
public class CustomizedBarChart implements JRChartCustomizer {

public void customize(JFreeChart chart, JRChart jasperChart) {
if(jasperChart.getChartType() == JRChart.CHART_TYPE_BAR) {

