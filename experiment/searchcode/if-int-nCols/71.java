public class SpreadSheet{

private int nRows;
private int nCols;
private Cell[][] cells;

public SpreadSheet(int nRows, int nCols) {
this.nRows = nRows;
this.nCols = nCols;
cells = new Cell[nRows][nCols];

