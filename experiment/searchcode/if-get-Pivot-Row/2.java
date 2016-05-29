for (int i = 1; i < pivotTable.getRowSection().getRowList().size(); i++) {
int newIndex = i - 1;
Row row = new Row();
row.setRowFieldList(pivotTable.getRowSection().getRowList().get(i).getRowFieldList());

