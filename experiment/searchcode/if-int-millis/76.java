public static String formatDuration(long durationMillis) {
String humanDuration = null;
// if (durationMillis >= DateUtils.MILLIS_PER_DAY) {
// humanDuration = String.format(&quot;%sJ %sh&quot;, days, hours);
// } else
if (durationMillis >= DateUtils.MILLIS_PER_HOUR) {
long hours = (durationMillis / DateUtils.MILLIS_PER_HOUR);

