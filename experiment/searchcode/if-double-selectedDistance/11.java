Segment       selectedSegment  = null;
double        selectedDistance = Double.POSITIVE_INFINITY;
final Segment lowerLeft        = new Segment(end, -1.0e-10, -1.0e-10);
final double distance = end.distance(segment.getStart());
if (distance < selectedDistance) {

