import java.io.Serializable;

public class Move implements Serializable {

ChessType type;
int orow, ocol, nrow, ncol;
* @param ncol new column
*/
public Move (int orow, int ocol, int nrow, int ncol) {
this.orow = orow;
this.ocol = ocol;

