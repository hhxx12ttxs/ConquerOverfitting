* Asks the user to enter a double value within a given set of constraints
*/
class ConstraintRead {
double minval;
try {
double v = Double.valueOf(readln());
if ( ((strict &amp;&amp; (v > minval)&amp;&amp;(v < maxval))) || (!strict &amp;&amp; (v >= minval)&amp;&amp;(v <= maxval)) ) return v;

