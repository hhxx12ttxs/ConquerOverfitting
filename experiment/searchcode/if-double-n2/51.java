public Number add(Number n1, Number n2) {
if (n1 != null &amp;&amp; n2 != null) {
double r = n1.doubleValue() + n2.doubleValue();
if (n1 instanceof Integer &amp;&amp; n2 instanceof Integer &amp;&amp; (r > Integer.MAX_VALUE || r < Integer.MIN_VALUE)) {

