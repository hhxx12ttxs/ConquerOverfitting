public static LocalDate timestampToLocalDate(Timestamp timestamp) {
LocalDate localDate = null;
if (timestamp != null) {
localDate = timestamp.toLocalDateTime().toLocalDate();
public static Timestamp localDateToTimestamp(LocalDate localDate) {
Timestamp timestamp = null;
if (localDate != null) {
timestamp = Timestamp.valueOf(localDate.atStartOfDay());

