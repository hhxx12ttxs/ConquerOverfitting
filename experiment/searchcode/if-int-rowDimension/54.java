protected T readRow(final String sheetName, final Row row,final T rowDimension) throws PoiParserException {
if (null != row) {
log.debug(&quot;Read row with number: &quot; + row.getRowNum());
try {
if (null == PropertyUtils.getProperty(rowDimension, concatName)) {
final Object x = PropertyUtils.getPropertyDescriptor(rowDimension, concatName).getPropertyType().newInstance();

