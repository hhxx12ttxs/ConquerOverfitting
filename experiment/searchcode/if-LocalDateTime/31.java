public Timestamp convertToDatabaseColumn(LocalDateTime localDateTime) {
if(localDateTime == null) {
return null;
public LocalDateTime convertToEntityAttribute(Timestamp timestamp) {
if(timestamp == null) {

