for (int i = 0; i < 6; i++) {
double a = 2 * Math.PI * i / 6;
double nextX = curX + step * Math.cos(a);
double nextY = curY + step * Math.sin(a);
double nextF = f.apply(nextX, nextY);
if (bestF > nextF) {
bestF = nextF;

