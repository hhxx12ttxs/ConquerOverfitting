public static double getAngle(int centerX,int centerY,int pointX,int pointY)
{
double a=(-((double)pointY-(double)(centerY+20)))/
Math.sqrt
(Math.pow((double)pointX-(double)(centerX+20),2)
+Math.pow(-((double)pointY-(double)(centerY+20)),2));

