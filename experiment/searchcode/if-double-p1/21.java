public class LineFunction {

private Point2D p1, p2;

private double k, b;

public LineFunction(Point2D p1, Point2D p2) {
this.p1 = p1;
this.p2 = p2;
if(p1.getX() != p2.getX()){
k = ( p2.getY() - p1.getY()) / (p2.getX() - p1.getX());

