if (cursor == null) {
return &quot;&quot;;
}
int columnIndex = cursor.getColumnIndex(columnName);
if (columnIndex < 0) {
if (cursor == null) {
return 0;
}
int columnIndex = cursor.getColumnIndex(columnName);
if (columnIndex < 0) {

