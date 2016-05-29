public void addValue(Comparable<?> v){
Comparable<?> obj = v;
if (v instanceof Integer) {
obj = Long.valueOf(((Integer) v).longValue());
* @return the proportion of values less than or equal to v
*/
public double getCumPct(Comparable<?> v) {
final long sumFreq = getSumFreq();
if (sumFreq == 0) {

