public static double pointToAngle(double x, double y, double mCenterX,
double mCenterY) {
double deltaY = y - mCenterY;
double deltaX = x - mCenterX;
if (deltaX < 0) {
return 180d + Math.atan(deltaY / deltaX) / Math.PI * 180;

