public static String getPatternOfTriCandles(double[][] quadCandles) {
String pattern = &quot;&quot;;
double[] c1 = quadCandles[0];
double[] c2 = quadCandles[1];
&amp;&amp; Math.abs(c1[0] - c1[3]) >= c1[1] * AllConstants.CANDLE_BODY_MINIMUM) {
if (c1[0] > c1[3] &amp;&amp; c3[3] >= (c1[0] + c1[3]) / 2 &amp;&amp; c2[1] < c1[3] &amp;&amp; c2[1] < c3[0]) {

