calendar.set(Calendar.DAY_OF_YEAR, dayOfYear);
return calendar.DAY_OF_MONTH;
}

public static int convert(int month, int dayOfMonth) {
if(inMonth(month, dayOfMonth))
{
int result = 0;

// Converts month and day integers into a single integer representing the day of the year.

