double getT(double cur, int V) {
cur *= 2; V *= 2;
if (V == 0)
return 1e100;
if (V > 0) {
double want = Math.ceil(cur + EPS);
double nextX = curX + t * dx;
double nextY = curY + t * dy;

double piece = Math.sqrt((nextX - curX) * (nextX - curX) + (nextY - curY) * (nextY - curY));

