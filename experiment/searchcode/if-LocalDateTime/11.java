public Date convertToDatabaseColumn(LocalDateTime localDateTime) {
if (localDateTime == null) {
return null;
}
return localDateTime.toDate();
}

@Override
public LocalDateTime convertToEntityAttribute(Date date) {
if (date == null) {

