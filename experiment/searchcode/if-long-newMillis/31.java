* @param millis The time in millis since UNIX Epoch.
*/
public void setTime(long millis) {
long oldTime = mDate.getTime();
if (millis != oldTime) {
private void onDateChanged(long oldMillis, long newMillis) {
for (int i = 0; i < mObservers.size(); i++) {
mObservers.get(i).onDateChanged(this, oldMillis, newMillis);

