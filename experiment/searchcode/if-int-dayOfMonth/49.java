public static final MonthAndDay FEB_29 = new MonthAndDay(2, 29);

private final int monthOfYear;
private final int dayOfMonth;

public MonthAndDay(int monthOfYear, int dayOfMonth) {
if (monthOfYear <= 0) throw new IllegalArgumentException(&quot;month must be greater than zero&quot;);

