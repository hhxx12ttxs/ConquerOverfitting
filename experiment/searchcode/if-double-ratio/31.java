public static boolean checkRatio(double d1, double d2, double limit) {
double error_ratio = (double)Math.abs(d1-d2) / (double)d1;
if (error_ratio < limit) {
public static boolean checkRatio(long d1, long d2, double limit) {
double error_ratio = (double)Math.abs(d1-d2) / (double)d1;
if (error_ratio < limit) {

