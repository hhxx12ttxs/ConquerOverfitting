public int getMinuteFromStartOfWeek() {
int daysInMinutes = dayOfWeek.ordinal() * MinutesInDay;
int hoursInMinutes = hours * MinutesInHour;
int dateTimeInMinutesFromStartOfWeek = daysInMinutes + hoursInMinutes
+ minutes;

