List<Row> rows = pivotTable.getRowSection().getRowList();
List<ColumnField> cs = pivotTable.getColumnSection().getColumnFieldList();
for (Row row : rows) {
if (row instanceof Derived) {

