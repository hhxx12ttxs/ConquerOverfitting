double pairwiseRepulsion (Cell c1, Cell c2) {
double r = Cell.sumOfRadii (c1, c2);
double k = preferredEdgeLength + r;
double dSquared = Cell.getDistanceSquared (c1, c2);
if (dSquared < k * k) return k * k / dSquared;

