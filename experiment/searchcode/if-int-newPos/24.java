char col;
int row;
List<Position> positions = new ArrayList<Position>();
Position newPos;
col = initPos.getColumn();
row = initPos.getRow();
newPos = new Position(col, ++row);
if(isMoveValid(newPos, state) &amp;&amp; state.getPieceAt(newPos) == null) {

