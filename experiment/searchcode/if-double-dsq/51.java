// find two closest matches
for (final T target : features) {
final double dsq = getEuclideanDistance(query, target);

if (dsq < distsq1) {

