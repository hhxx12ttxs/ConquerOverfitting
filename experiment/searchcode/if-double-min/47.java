public static double InverseLerp(double value, double min, double max) {
double ret = (value - min) / (max - min);
return MathUtil.clamp01(ret);
public static double clamp(double val, double min, double max) {
if(val > max) {
return max;
}
if(val < min) {

