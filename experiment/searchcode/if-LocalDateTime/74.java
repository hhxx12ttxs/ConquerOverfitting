public Date convertToDatabaseColumn(LocalDateTime local) {
if (local == null) {
return null;
@Override
public LocalDateTime convertToEntityAttribute(Date date) {
if (date == null) {

