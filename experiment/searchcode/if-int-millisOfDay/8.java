return new DateTime(calendar);
}

public DateTime withMillisOfDay(int millisOfDay) {
if (millisOfDay > MAX_MILLIS_PER_DAY || millisOfDay < 0) {
throw new RuntimeException(&quot;Illegal millis of day: &quot; + millisOfDay);
}
int hours = millisOfDay / MILLIS_PER_HOUR;

