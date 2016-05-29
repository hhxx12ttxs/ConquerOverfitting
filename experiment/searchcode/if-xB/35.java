public Object getRegion(int xa, int ya, int xb, int yb) {
Object aux;

if ((xb<xa) &amp;&amp; (yb<ya)) {
ya = yb;

xb = za;
yb = wa;

} else if(xb<xa) {

