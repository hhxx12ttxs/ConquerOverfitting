for(int k = colNo; k<=7; k++) {
Position newPos = new Position(board, i, k);
if(!board.getSquare(newPos).isOccupied())
possibleMoves.add(board.getSquare(newPos));
else {
if(board.getSquare(newPos).getOccupyingPieceColor() != this.color)

