public LinearDoubleMapping(double startTime, double endTime, double startValue, double endValue) {
if(startTime <= endTime) {
this.startTime = startTime;
this.endTime = endTime;
* @see de.uni_trier.jane.basetypes.DoubleMapping#getValue(double)
*/
public double getValue(double time) {
if(time < startTime) {
return startValue;
}
else if(time > endTime) {

