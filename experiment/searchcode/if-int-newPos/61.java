for(int k = colNo; k<=7; k++) {
Position newPos = new Position(board, i, k);
if(!board.getSquare(newPos).isOccupied())
else {
if(board.getSquare(newPos).getOccupyingPieceColor() != this.color)

