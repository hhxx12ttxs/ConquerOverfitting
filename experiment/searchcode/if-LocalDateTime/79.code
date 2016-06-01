public Object toJdbcType(Object value) {
if (value instanceof LocalDateTime) {
return new Timestamp(((LocalDateTime) value).toDateTime().getMillis());
return BasicTypeConverter.toTimestamp(value);
}

@Override
public LocalDateTime toBeanType(Object value) {
if (value instanceof java.util.Date) {

