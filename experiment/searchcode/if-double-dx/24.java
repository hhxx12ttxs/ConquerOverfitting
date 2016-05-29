import java.awt.Color;

public class Particle {
public double x, y, dx, dy;
public int size;
public void step(double gravity, int gX, int gY, boolean GForce, double GPower, int screenWidth, int screenHeight) {
x += dx;
y += dy;
dy += gravity;
if(GForce){
double xD = gX-x;

