private Ease ease = Ease.IN;

public QuadraticEnvelope(double startValue, double endValue, double periodMs) {
this(new FixedParameter(startValue), new FixedParameter(endValue),
new FixedParameter(periodMs));
}

public QuadraticEnvelope(LXParameter startValue, double endValue,
double periodMs) {

