double deltaX = p.getX() - q.getX();
double deltaY = p.getY() - q.getY();
return Math.sqrt(deltaX * deltaX + deltaY * deltaY);
}

class Point {
private double x, y;

public double getX() {
return x;

