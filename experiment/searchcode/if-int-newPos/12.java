char col;
int row;
List<Position> positions = new ArrayList<Position>();
Position newPos;
newPos = new Position(col, ++row);
while(isMoveValid(newPos, state)) {
positions.add(newPos);
if( state.getPieceAt(newPos) != null)

