private int id;

@Column(name = &quot;START_VALUE&quot;)
private double startValue;

@Column(name = &quot;END_VALUE&quot;)
private double endValue;
List<Double> scaleValues = new ArrayList<Double>();
double startValue = getStartValue(), endValue = getEndValue();

if (startValue > endValue) {
throw new ScaleException();

