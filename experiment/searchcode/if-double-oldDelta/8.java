return collectingHigherMoments;
}

public double getSkewness() {
if (!collectingHigherMoments) {
throw new IllegalStateException(&quot;advanced stats not available&quot;);
final double oldDelta = sampleValue - runningMean;
if (collectingHigherMoments) {
final double weightedDelta = oldDelta / samples;

