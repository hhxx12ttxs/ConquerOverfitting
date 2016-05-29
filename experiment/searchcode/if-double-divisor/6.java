public double div(String dividentString, String divisorString) {
double divident;
double divisor;
if (null == dividentString || null == divisorString) {
divident = su.parseDouble(dividentString);
divisor = su.parseDouble(divisorString);
if (0 == divisor) {

