package optimization;

public class Bounds {

private final double[] lower;
private final double[] upper;

public Bounds(double[] lower, double[] upper) {
if(lower.length != upper.length)

