pivotTable = f.getPivotTable();
}



@Test
public void testRun() {

int numberOfColumns = pivotTable.getColumnSection().getColumnFieldList().size();
int numberOfRows = pivotTable.getRowSection().getRowList().size() ;

