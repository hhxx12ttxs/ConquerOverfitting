public LocalDateTime convertTo(Date date, LocalDateTime localDateTime) {
if(date==null){
return null;
}
return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
public Date convertFrom(LocalDateTime localDateTime, Date date) {
if(localDateTime==null){
return null;

