public static void assertEquals(String msg, double expected, double actual, double delta) {
// check for NaN
if(Double.isNaN(expected)){
Assert.assertTrue(&quot;&quot; + actual + &quot; is not NaN.&quot;,
public static void assertRelativelyEquals(String msg, double expected, double actual, double relativeError) {
if (Double.isNaN(expected)) {

