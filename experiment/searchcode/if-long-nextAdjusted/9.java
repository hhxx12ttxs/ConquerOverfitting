public String getShortName(long instant, Locale locale) {
if (locale == null) {
locale = Locale.getDefault();
if (nextLocal == (instantLocal - offsetLocal)) {
nextLocal = Long.MAX_VALUE;
}
long nextAdjusted = nextTransition(instantLocal - offsetAdjusted);

