public HolidaysList(String name, DayOfWeek[] weekend, LocalDate[] list) {
holidays = new HashSet<>(300, 0.9f);
if (list != null) {
int c = daysCount;
LocalDate result = start;
if (c > 0) {
while (c > 0) {
result = result.plusDays(1);

