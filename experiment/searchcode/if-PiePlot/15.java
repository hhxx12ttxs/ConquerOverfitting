import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.data.general.DefaultPieDataset;
PiePlot3D piePlot = (PiePlot3D) pieChart.getPlot();
setSection(piePlot);//设置扇区颜色，可省略
setLabel(piePlot);

