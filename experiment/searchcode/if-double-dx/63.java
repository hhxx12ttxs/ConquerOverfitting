public double dx,dy,length;
public Vector2D(){
dx=0.;
dy=0.;
length=0.f;
//the the closed ratio of integer dxy[0] and dxy[1]; the resolution is 1:3;
double ax=Math.abs(dx), ay=Math.abs(dy);
if(ax>ay){
if(ay/ax<0.1){

