public double getDistance(double x1, double y1, double x2, double y2) {
return Math.sqrt(Math.pow(x1-x2, 2) + Math.pow(y1-y2, 2) );
public Vector2D getDirectionD(double x1, double y1, double x2, double y2, double length) {
double distance = getDistance(x1, y1, x2, y2);
if (distance == 0) return new Vector2D(0.0, 0.0);

