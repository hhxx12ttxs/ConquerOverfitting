public class PathZigZag {
Dimension dim;
Point pos;
int posInicio;
int aux = 5;

public PathZigZag(Dimension dim, Point pos){
this.posInicio = pos.y;
}

public void move(){
pos.x += 5;
if(pos.x > dim.width){

