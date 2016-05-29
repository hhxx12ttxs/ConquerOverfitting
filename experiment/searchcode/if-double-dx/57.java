angle = Math.acos(x / Math.sqrt(x*x + y*y));
if(y>0) angle = -angle;
return angle;
}

public static double getAngle(MyPoint p, MyPoint q)
{
double angle = 0.0f;
double dx = q.x - p.x;
double dy = q.y - p.y;

