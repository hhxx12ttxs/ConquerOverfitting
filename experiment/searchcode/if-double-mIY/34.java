private int maX, maY, miX, miY; //Boundaries of the collision: Max x, Max y, Min x, Min y

public Collision(int top, int bottom, int left, int right){
col[1] = maX-size; //collide with right
col[4] = -1;
}
if(y<=miY){
col[2] = miY; //collide with top
col[4] = -1;

