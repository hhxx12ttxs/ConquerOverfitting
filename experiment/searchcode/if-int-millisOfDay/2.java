public class TimeConverter {

public static long timeToEpoch(int year, byte month, byte day, long millisOfDay) {

int hours, minutes, seconds, milliseconds;
hours = (int) TimeUnit.MILLISECONDS.toHours(millisOfDay);
minutes = (int) (TimeUnit.MILLISECONDS.toMinutes(millisOfDay) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millisOfDay)));

