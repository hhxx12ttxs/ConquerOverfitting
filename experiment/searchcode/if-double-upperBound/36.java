private final double upperBound;

/**
* @param lowerBound
* @param upperBound
*/
public Bounds(double lowerBound, double upperBound) {
* @return <code>true</code> if value is inside bounds
*/
public boolean isInsideBounds(double value) {
if (Double.compare(value, upperBound) <= 0 &amp;&amp; Double.compare(value, lowerBound) >= 0) {

