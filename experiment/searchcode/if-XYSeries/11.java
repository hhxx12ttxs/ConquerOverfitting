public static XYSeries getXYSeries(XYSeriesCollection dataset, Comparable seriesKey) {
if (hasXYSeries(dataset, seriesKey)) {
return dataset.getSeries(seriesKey);
public static void removeXYSeries(XYSeriesCollection seriesCollection, Comparable seriesKey) {
XYSeries series = seriesCollection.getSeries(seriesKey);
if (series != null) {

