import Jama.*;

public class Tile {

public double xmin, ymin, xmax, ymax;

public Tile(double x, double y, double width, double height) {
xmin = x;
ymin = y;
xmax = x+width;
ymax = y+height;

