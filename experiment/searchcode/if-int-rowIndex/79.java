public class Position {
private int rowIndex;
private int colIndex;

public Position(int rowIndex, int colIndex){
public int getRowIndex() {
return rowIndex;
}

public void setRowIndex(int rowIndex) {
this.rowIndex = rowIndex;
if (Solver.isOkFlag)

