import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.Plot;
getBarRenderer().setSeriesPaint(index, color);
}

private BarRenderer getBarRenderer() {
if(barRenderer == null) {
CategoryPlot plot = (CategoryPlot) super.getJFreeChart().getPlot();

