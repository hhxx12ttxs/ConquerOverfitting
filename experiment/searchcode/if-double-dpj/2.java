double range = Double.MIN_VALUE;
for(int j=0; j<x.getDimensionality(); j++) {

if (x.getRange(j) > range)
for(int j=0; j<x.getPoints().size(); j++) {
if (p!=j) {
//original distances
double dpjOld = x.getDistance(j,p);

