Arrays.sort(array);
double cumPct = 0;
final long sumFreq = getSumFreq();
for (i = 0; i < array.length; i++) {
public double getPct(Comparable<?> v) {
final long sumFreq = getSumFreq();
if (sumFreq == 0) {
return Double.NaN;

