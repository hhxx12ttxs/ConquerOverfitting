public class InterpolateDouble implements Serializable {

private final double startValue;
private final long startTimeInMs;

private final double endValue;
public InterpolateDouble(double startValue, long startTimeInMs, double endValue, long endTimeInMs) {
this.startValue = startValue;
this.startTimeInMs = startTimeInMs;

