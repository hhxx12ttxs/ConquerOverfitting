private ExactPoint center;
private final int radius;
private double deltaX, deltaY;


public Ball(Point center, int radius) {
public Ball(Point center, int radius, double deltaX, double deltaY){
if(center == null || radius < 0)

