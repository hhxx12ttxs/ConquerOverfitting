public class RectHV {
private final double xmin, ymin;
private final double xmax, ymax;

public RectHV(double xmin, double ymin, double xmax, double ymax) {
if (xmax < xmin || ymax < ymin) {

