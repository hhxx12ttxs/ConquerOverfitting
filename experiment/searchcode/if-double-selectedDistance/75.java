double                  selectedDistance = Double.POSITIVE_INFINITY;
final ComparableSegment lowerLeft        = new ComparableSegment(end, -1.0e-10, -1.0e-10);
segment = n.getElement();
final double distance = end.distance(segment.getStart());
if (distance < selectedDistance) {

