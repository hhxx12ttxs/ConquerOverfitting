private void printNumbersOfDays(int lengthOfMonth, int highlightedDay, DayOfWeek firstDayOfMonth) {
for (int dayOfMonth = 1; dayOfMonth <= lengthOfMonth; dayOfMonth++) {
int alignedDayOfMonth = dayOfMonth + firstDayOfMonth.getValue();

if (dayOfMonth == highlightedDay) {

