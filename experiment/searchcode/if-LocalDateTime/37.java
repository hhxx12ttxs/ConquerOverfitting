public Date convertToDatabaseColumn(LocalDateTime localDateTime) {
if (localDateTime == null) {
return null;
@Override
public LocalDateTime convertToEntityAttribute(Date date) {
if (date == null){

