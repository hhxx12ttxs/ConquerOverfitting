package lab3;

import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
public Series(String name) {
xySeries = new XYSeries(name);
}

public void addPoint(Point point) {
if (point instanceof Point2D) {

