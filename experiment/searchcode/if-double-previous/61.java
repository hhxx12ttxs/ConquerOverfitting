public static double angleBetween(Point2D.Double center, Point2D.Double next, Point2D.Double  previous) {
double angle = (Math.atan2(next.getX() - center.getX(),next.getY() - center.getY())-
Math.atan2(previous.getX() - center.getX(),previous.getY() - center.getY()));
if(angle<0){
angle+= 2*Math.PI;
}

return angle;
}

public static Point2D.Double calcPoint(Point2D.Double p, double dist, double ang) {

