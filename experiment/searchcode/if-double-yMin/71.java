double xmax, double ymax) {

if (ymin > ymax || xmin > xmax) throw new java.lang.IllegalArgumentException(&quot;Illegal rectangle&quot;);
}     // construct the rectangle [xmin, xmax] x [ymin, ymax]
// throw a java.lang.IllegalArgumentException if (xmin > xmax) or (ymin > ymax)

public  double xmin() {

