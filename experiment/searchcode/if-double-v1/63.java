package utils;

public class Point implements Comparable<Point>{

public double x;
public double y;
public int compareTo(Point p) {
double v1 = x*x+y*y;
double v2 = p.x*p.x+p.y*p.y;

if(v1 < v2) return -1;
if(v1 > v2) return 1;
return 0;
}
}

