import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;

import java.awt.*;
import java.io.IOException;
PiePlot piePlot = new PiePlot(getDataset());
piePlot.setNoDataMessage(&quot;No data available&quot;);
if (paints != null &amp;&amp; paints.length > 0) {

