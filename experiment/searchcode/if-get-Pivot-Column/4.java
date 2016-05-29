CalculatedCollection aggregatedColumnFound = findAverageColumn(pivotTable);
if (aggregatedColumnFound == null) {
List<ColumnField> colList = pivotTable.getColumnSection().getColumnFieldList();
for (ColumnField columnField : colList) {

