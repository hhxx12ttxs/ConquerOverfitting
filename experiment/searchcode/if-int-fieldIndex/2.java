public void removeFigure(final int fieldIndex) {
checkLegalFieldAccess(fieldIndex);
fields[fieldIndex].setCurrentFieldIndex(-1);
return fields.length;
}

private void checkLegalFieldAccess(final int fieldIndex) {
if (fieldIndex < 0 || fieldIndex > fields.length) {

