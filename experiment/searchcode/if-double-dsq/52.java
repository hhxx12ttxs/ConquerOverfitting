for (final T target : features) {
final double dsq = getEuclideanDistance(query, target);

if (dsq < distsq1) {
distsq1 = dsq;
minkey = target;
} else if (dsq < distsq2) {

