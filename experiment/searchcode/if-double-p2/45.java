public static double distance(Point p1, Point p2){
double dist =  Math.sqrt((p1.x - p2.x) * (p1.x - p2.x)
+ (p1.y - p2.y) * (p1.y - p2.y));
return new Point((p2.x+p1.x)/2 , (p2.y+p1.y)/2);
}

public static double slope(Point p1, Point p2){
return ((double)(p2.y-p1.y))/((double) (p2.x-p1.x));

