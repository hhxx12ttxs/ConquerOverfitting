public class DoublePoint implements Clusterable{

/** the point */
private final double[] point;
public boolean equals(final Object other) {
if (!(other instanceof DoublePoint)) {
return false;
}
return Arrays.equals(point, ((DoublePoint) other).point);

