private DayOfWeek dayOfWeek;
private Month month;
private int dayOfMonth;
private int year;

public CalendarDay() {
month = month.next();
dayOfMonth = 1;
}

private int getDaysInMonth(Month month) {
int daysInMonth;
if (month.has30Days()) {

