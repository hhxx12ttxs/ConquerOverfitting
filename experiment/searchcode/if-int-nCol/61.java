nRowCount = 0;
for (int nRow=range.getFirstRow(); nRow<=range.getLastRow(); nRow++) {
if (isVisibleRow(nRow)) { nRowCount++; }
}
nColCount = 0;
for (int nCol=range.getFirstCol(); nCol<=range.getLastCol(); nCol++) {

