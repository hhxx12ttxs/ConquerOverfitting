public void addValue(double value) {
min.increment(value);
max.increment(value);
sumlog.increment(value);
* Returns the sum of the values that have been added
* @return The sum or <code>Double.NaN</code> if no values have been added
*/
public double getSum() {

