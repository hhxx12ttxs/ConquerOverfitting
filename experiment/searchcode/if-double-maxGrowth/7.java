int finalx =-1, finaly=-1;
double maxGrowth = -1.0;

for (int x=(fx - sx)/bigBoxSize; --x>=0;) {
for (int y=(fy - sy)/bigBoxSize; --y>=0;) {
if (maxGrowth < finalLocations[x][y]) {

