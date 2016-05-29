public static Boolean getBooleanFromCursor(Cursor c, int columnIndex) {
return c.isNull(columnIndex) ? null : (c.getInt(columnIndex) == 1);
}

public static Short getShortFromCursor(Cursor c, int columnIndex) {
return c.isNull(columnIndex) ? null : c.getShort(columnIndex);

