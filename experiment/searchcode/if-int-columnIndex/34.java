public static long getTripId(Cursor cursor) {
int columnIndex = cursor.getColumnIndex(TripColumns.ID);

if (columnIndex >= 0 &amp;&amp; !cursor.isNull(columnIndex)) {
cursor.moveToFirst();
int columnIndex = cursor.getColumnIndex(TripColumns.Date);

if (columnIndex >= 0 &amp;&amp; !cursor.isNull(columnIndex)) {

