public Point[] intersects(Point p1,Point p0){
Point[] out = {null,null};
double delx = p1.posx-p0.posx;
double dely = p1.posy-p0.posy;
double delz = p1.posz-p0.posz;
double root1 = (B*-1+square)/A;
double root2 = (B*-1-square)/A;
if(root1==root2){
double xx = p0.posx+root1*delx;

