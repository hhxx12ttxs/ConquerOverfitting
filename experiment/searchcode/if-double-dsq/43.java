// find two closest matches
for (final T target : features) {
final double dsq = getManhattanDistance(query, target);

if (dsq < distsq1) {
distsq2 = distsq1;

