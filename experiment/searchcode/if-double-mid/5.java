mid = (low + high) / 2;
if (mid * mid <= x &amp;&amp; (mid + 1) * (mid + 1) > x) {
break;
}
if (mid * mid > x) {
high = x;
}
double mid = 0;
while (low <= high) {
mid = (low + high) / 2;
if (mid * mid - x <= e &amp;&amp; (mid + 1) * (mid + 1) - x > e) {

