import org.jfree.chart.plot.XYPlot;

public class XYPlotTest {

public static void main(String[] args) {
new XYPlotTest().run();
}

private void run() {
final XYPlot plot = new XYPlot();

Thread t1 = new Thread(new Runnable() {

