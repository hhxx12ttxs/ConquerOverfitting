public boolean is_inside(Point p){
double distance = distance(center, p);
if(distance < radius){
return true;
}
return false;
}


public double distance(Point one, Point two){
double delta_x = one.getX() - two.getX();

