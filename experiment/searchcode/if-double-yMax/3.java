public class BoundingBox {
boolean inited = false;
double xmin, xmax, ymin, ymax;
public void addPoint(Point p) {
if(p == null) { return; }
xmax = p.x;
}
if(p.y > ymax) {
ymax = p.y;
}
}
if(Double.isNaN(xmin)) {
xmin = Double.MIN_VALUE;

