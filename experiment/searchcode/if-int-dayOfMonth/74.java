public Date(int year, int month, int dayOfMonth, int hours, int minutes) {
this.year = year;
this.month = month;
localTime = LocalTime.of(this.hours, this.minutes);
}

public Date(int year, int month, int dayOfMonth) {
this.year = year;

