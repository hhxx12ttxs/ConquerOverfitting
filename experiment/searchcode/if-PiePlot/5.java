import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;

public class PieServicePersonCommision {
public void setLabel(PiePlot pieplot) {
//设置扇区标签显示格式：关键字：值(百分比)
if (this.flag==1) {
pieplot.setLabelGenerator(new StandardPieSectionLabelGenerator(

