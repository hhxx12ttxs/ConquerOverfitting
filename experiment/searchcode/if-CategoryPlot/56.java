import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
public String generateLabel(CategoryDataset categorydataset, int i, int j)
{
String s = null;
Number number = categorydataset.getValue(i, j);
if(number != null)

