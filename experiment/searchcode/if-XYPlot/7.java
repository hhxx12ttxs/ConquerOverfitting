import org.jfree.chart.plot.XYPlot;
import org.jfree.data.Range;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleEdge;
xyPlot.setOutlinePaint(null);

xyPlot.getRenderer().setSeriesShape(0, shape);

if (legend) {
getChart().getLegend().setPosition(RectangleEdge.TOP);

