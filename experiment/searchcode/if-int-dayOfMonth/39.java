private final int month;

private final int dayOfMonth;

private transient int hashCode;

private transient String toString;
dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
}

public MagmaDate(int year, int month, int dayOfMonth) {
this(asCalendar(year, month, dayOfMonth));

