public boolean checkMove(Piece[] p,int startingRow, int startingCol, int endRow, int endCol) {

if (col == startingCol &amp;&amp; noOneThere(p, startingRow, startingCol, endRow, endCol))
public boolean noOneThere(Piece p[], int startingRow, int startingCol,
int endRow, int endCol) {

if (endCol == startingCol)

