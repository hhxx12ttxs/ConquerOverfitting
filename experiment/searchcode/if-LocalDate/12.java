public static LocalDate[] getWeekday(LocalDate date) {

if (date == null) {
date = LocalDate.now();
}

LocalDate begin = null;
if (date.getDayOfWeek().equals(DayOfWeek.SUNDAY)) {

