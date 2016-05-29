public class LocalDateToDateConverter implements AttributeConverter<LocalDate, Date> {

@Override
public Date convertToDatabaseColumn(LocalDate localDate) {
if (localDate == null) {
public LocalDate convertToEntityAttribute(Date date) {
if (date == null) {
return null;
}
return new LocalDate(date);
}

}

