import com.badlogic.gdx.math.Vector2;
import com.tictactower.gameboard.Gameboard;

public class FieldIndex{
private int x;
private int y;
return ( x>=0 &amp;&amp; x<Gameboard.COLUMNS_AND_ROWS ) &amp;&amp; ( y>=0 &amp;&amp; y<Gameboard.COLUMNS_AND_ROWS );
}

FieldIndex(int _x, int _y){
x = _x;

