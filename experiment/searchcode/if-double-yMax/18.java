package KD_Trees;

public class RectHV {
private double xmin, ymin, xmax, ymax;

public RectHV(double xmin, double ymin, double xmax, double ymax) {
if (xmax < xmin || ymax < ymin)
throw new IllegalArgumentException(&quot;Invalid rectangle&quot;);

