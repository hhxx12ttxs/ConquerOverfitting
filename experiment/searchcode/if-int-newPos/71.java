int colNo = pos.getColumn();

outerloop:
for(int i = rowNo + 1; i<=7; i++) {
Position newPos = new Position(board, i, colNo);
if(!board.getSquare(newPos).isOccupied())

