public static void assertRelativelyEquals(double expected, double actual, double relativeError) {
assertRelativelyEquals(null, expected, actual, relativeError);
public static void assertRelativelyEquals(String msg, double expected, double actual, double relativeError) {
if (Double.isNaN(expected)) {

