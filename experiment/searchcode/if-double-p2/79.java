set(p1.x, p1.y, p2.x, p2.y);
}

public VLine(Point2D.Double p1, Point2D.Double p2) {
set(p1.x, p1.y, p2.x, p2.y);
}

public Double getIntersection(VLine l) {
Point2D.Double q;
double temp = A * l.B - B * l.A;
if (temp != 0) {

