public Date convertToDatabaseColumn(LocalDate localDate) {

if(null == localDate){
return null;
@Override
public LocalDate convertToEntityAttribute(Date date) {

if(null == date) {
return null;

