public boolean checkMove(Piece[] p,int startingRow, int startingCol, int endRow, int endCol) {

if ((endRow > startingRow || endRow < startingRow) &amp;&amp; endCol == startingCol)
public boolean noOneThere(Piece p[], int startingRow, int startingCol,
int endRow, int endCol) {

if (startingRow == endRow || (endCol == startingCol))

