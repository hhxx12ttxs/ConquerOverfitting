* @return the human-readable long name in the specified locale
*/
public String getName(long instant, Locale locale) {
if (locale == null) {
locale = Locale.getDefault();
long nextLocal = nextTransition(instantAdjusted);
if (nextLocal == (instantLocal - offsetLocal)) {
nextLocal = Long.MAX_VALUE;

