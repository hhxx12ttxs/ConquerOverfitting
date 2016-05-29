private static double doubleHighPart(double d) {
if (d > -SAFE_MIN &amp;&amp; d < SAFE_MIN){
return d; // These are un-normalised - don&#39;t try to convert
double invx = 1.0/x;
double invy = 1.0/y;

if (invx == 0.0) { // X is infinite
if (x > 0) {

