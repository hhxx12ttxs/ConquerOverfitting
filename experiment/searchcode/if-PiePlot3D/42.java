import org.jfree.chart.labels.PieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.data.general.DefaultPieDataset;
public String generateSectionLabel(PieDataset piedataset, Comparable comparable)
{
String s = null;
if(piedataset != null &amp;&amp; !comparable.equals(&quot;PHP&quot;))

