double mindiff = Double.POSITIVE_INFINITY;
double slope = 0;
for(int i = min; i <= max; i++) {
double s = bestSlope + i * dy;
double diff = fSlope(s);
if(diff < mindiff) {
slope = s;

