super(row, col, iconName, color);
}

public void move(int endRow, int endCol, JPanel[][]boardOfImages, boardPieces[][] boardOfPieces) {
col = endCol;

}

public Boolean isValidMove(int endRow, int endCol, boardPieces[][] boardOfPieces) {
if(color == WHITE &amp;&amp; GameGUI.turnToMove == WHITE) {

