if (zone != null) {
return zone;
}
if (id.startsWith(&quot;+&quot;) || id.startsWith(&quot;-&quot;)) {
int offset = parseOffset(id);
public static DateTimeZone forOffsetHoursMinutes(int hoursOffset, int minutesOffset) throws IllegalArgumentException {
if (hoursOffset == 0 &amp;&amp; minutesOffset == 0) {

