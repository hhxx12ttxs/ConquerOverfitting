private final double xmin, ymin;   // minimum x- and y-coordinates
private final double xmax, ymax;   // maximum x- and y-coordinates
// construct the axis-aligned rectangle [xmin, xmax] x [ymin, ymax]
public RectHV(double xmin, double ymin, double xmax, double ymax) {
if (xmax < xmin || ymax < ymin) {

