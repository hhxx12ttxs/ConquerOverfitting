public class MonthlyDate {
private int year;
private int month;
private int dayOfMonth;
private int dayOfWeekIndex;
private DateContent dateContent = null;
this.init(year, month, 1);
}

public MonthlyDate(int year, int month, int dayOfMonth) {
this.init(year, month, dayOfMonth);

