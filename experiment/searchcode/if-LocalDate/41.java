final LocalDate today = LocalDate.now();
for (LocalDate date : dates) {
if (today.equals(date)) {
final List<LocalDate> datesInInterval = new ArrayList<>();

for (LocalDate date : dates) {
if (start.equals(date) || end.equals(date) || (date.isAfter(start) &amp;&amp; date.isBefore(end))) {

