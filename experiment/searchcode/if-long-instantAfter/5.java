* @return the human-readable short name in the specified locale
*/
public String getShortName(long instant, Locale locale) {
if (locale == null) {
long nextLocal = nextTransition(instantAdjusted);
long nextAdjusted = nextTransition(instantLocal - offsetAdjusted);
if (nextLocal != nextAdjusted) {

