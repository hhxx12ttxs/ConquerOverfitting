Comparable<?> obj = v;
if (v instanceof Integer) {
obj = Long.valueOf(((Integer) v).longValue());
public double getCumPct(Comparable<?> v) {
final long sumFreq = getSumFreq();
if (sumFreq == 0) {

