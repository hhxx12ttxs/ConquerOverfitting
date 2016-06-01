private final double GOLDEN_RATIO = (Math.sqrt(5) + 1) / 2;

@Override
public double minimize(DoubleFunction<Double> f, double lowerBound, double higherBound, double precision) {
while (!(Math.abs(higherBound - lowerBound) < precision)) {
double left = higherBound - ((higherBound - lowerBound) / GOLDEN_RATIO);
double right = lowerBound + ((higherBound - lowerBound) / GOLDEN_RATIO);

