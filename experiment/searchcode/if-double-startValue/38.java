public abstract class FunctionalModulator extends LXRangeModulator {

public FunctionalModulator(double startValue, double endValue, double periodMs) {
this(new FixedParameter(startValue), new FixedParameter(endValue),
new FixedParameter(periodMs));

