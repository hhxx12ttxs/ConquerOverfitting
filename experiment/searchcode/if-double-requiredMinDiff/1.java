double biglsq = ZERO;
double distsq = ZERO;
// Update GOPT if necessary before the first iteration and after each
for(int j = 0; j < n; j++) {
double bdtest = bdtol;
if(newPoint.getEntry(j) == lowerDifference.getEntry(j)) {

