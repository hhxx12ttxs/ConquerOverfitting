double mag = 2 * Math.sqrt(diffx * diffx + diffy * diffy);
double dx = diffx / mag;
double dy = diffy / mag;
if (Double.isNaN(dx) || Double.isNaN(dy)) return;
int prevx = -1, prevy = -1;
while (inBounds((int) curx, (int) cury)) {
if ((int) curx == prevx &amp;&amp; (int) cury == prevy) {

