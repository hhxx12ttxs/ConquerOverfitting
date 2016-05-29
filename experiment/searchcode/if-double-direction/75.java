public class Direction {

double vx;
double vy;

double cosine;
double sine;


public Direction(Direction d) {
this(d.getXCpt(), d.getYCpt());
}



public Direction(double a, double b) {

