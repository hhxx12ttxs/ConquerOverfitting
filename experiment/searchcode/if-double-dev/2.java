double minDem = -1;
double minDev = 100000;
for (double i = 1; i <= 1000000 * goal; i++) {
for (double j = (int)((i-1)/goal); j <= (i+1)/goal; j++) {
double dev = goal - i/j;
if (dev < minDev &amp;&amp; dev > 0) {

