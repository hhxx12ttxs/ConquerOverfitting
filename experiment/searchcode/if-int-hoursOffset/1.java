return forTimeZone(TimeZone.getTimeZone(id));
}

public static DateTimeZone forOffsetHours(int hoursOffset) {
return forOffsetHoursMinutes(hoursOffset, 0);
public static DateTimeZone forOffsetHoursMinutes(int hoursOffset, int minutesOffset) {
if (hoursOffset == 0 &amp;&amp; minutesOffset == 0) {
return DateTimeZone.UTC;

