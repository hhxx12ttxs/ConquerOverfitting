public class PolarPoly {

private Vector<PointDouble> points;

public PolarPoly() {
points = new Vector<PointDouble>();
}

public void addPoint(PointDouble p) {
addPoint(p.x, p.y);
}

public void addPoint(double x, double y) {

