public SinLFO(double startValue, double endValue, double periodMs) {
this(new FixedParameter(startValue), new FixedParameter(endValue),
public SinLFO(LXParameter startValue, double endValue, double periodMs) {
this(startValue, new FixedParameter(endValue), new FixedParameter(periodMs));

