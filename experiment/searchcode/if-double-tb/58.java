public static CatmullRomSpline regularInterpolate(double dt, double... values) {
if (values.length < 2)
return null;
public static Spline2D regularInterpolate(double dt, PointD... values) {
if (values.length < 2)
return null;

