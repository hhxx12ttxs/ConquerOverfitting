values.add(null);
}
}

public void setColumnAt(int columnIndex, Object value) throws ColumnFormatException, IndexOutOfBoundsException {
checkColumnIndex(columnIndex);
if (value != null) {

