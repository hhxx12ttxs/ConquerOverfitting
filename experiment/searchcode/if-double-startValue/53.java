public static final double characteristicDecayPerSample(double characteristicSamples) {
if (characteristicSamples < 1e-2) {
public static Integrator of(long sampleRate, double characteristicSeconds, double startValue) {
if (sampleRate < 1) {
throw new IllegalArgumentException(&quot;Samplerate must be at least 1&quot;);

