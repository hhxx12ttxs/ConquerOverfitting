super.setNanos((int)nanos);
}
cal.add(GregorianCalendar.DATE, days);
if (months != 0)
cal.add(GregorianCalendar.MONTH, months);
if (nanoseconds > 0) {
int months, int days, int hours, int minutes, int seconds)  {
public NSTimestamp(long milliseconds, int nanoseconds) {
super(milliseconds);
public NSTimestamp timestampByAddingGregorianUnits(int years, 
long justMilliseconds = milliseconds % 1000;
long nanos = (justMilliseconds * 1000000) + nanoseconds;

