private double prevYscale = Double.NaN;

private double prevY = Double.NaN;

/**
* Only used by toString()
*/
private final String name;
public void layout(final double y, final double yScale) {
if (yScale != prevYscale || y != prevY) {
prevYscale = yScale;

