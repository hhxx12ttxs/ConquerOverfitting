* Code taken from http://stackoverflow.com/questions/1174899/java-joda-time-implement-a-date-range-iterator
*/


public class LocalDateRange implements Iterable<LocalDate>
{
private final LocalDate start;
private final LocalDate end;

public LocalDateRange(LocalDate start,

