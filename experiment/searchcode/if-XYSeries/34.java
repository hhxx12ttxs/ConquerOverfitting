import org.jfree.data.xy.XYSeries;

public class ChartToolBase {

protected <T extends Number> XYSeries createSeries(List<T> dataset, String seriesTitle, int sampleIntervalSeconds) {
int iteration = 0;
int samplesCounter = 0;

if (dataset.size() > 0) {
series = new XYSeries(seriesTitle);

