for(int i = 0; i < intervals; i++) {
double a = vals[i * 2];
double b = vals[i * 2 + 1];
if(!Double.isInfinite(a) &amp;&amp; a < ymin) {
ymin = a;
}
if(!Double.isInfinite(b) &amp;&amp; b > ymax) {
ymax = b;

