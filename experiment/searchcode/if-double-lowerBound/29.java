import static com.google.common.base.Preconditions.checkArgument;

/**
*
*/
public final class Bounds {
private final double lowerBound;
private final double upperBound;
public boolean isInsideBounds(double value) {
if (Double.compare(value, upperBound) <= 0 &amp;&amp; Double.compare(value, lowerBound) >= 0) {

