
public class Line {
boolean isVertical;
double slope;
double intercept;

public Line(Point p1, Point p2) {
if (p1.x == p2.x) {
isVertical = true;
slope = Double.POSITIVE_INFINITY;

