public class Knight extends Piece {

@Override
public boolean isValid(ChessBoard boardInstance, int startPos,
int startRow, int startCol, int finalPos, int finalRow, int finalCol) {
if(finalRow < 0 || finalRow > 7 || finalCol < 0 || finalCol > 7) {

