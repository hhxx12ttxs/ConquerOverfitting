private double green;
private double blue;

public ColorPixel(double r, double g, double b) {
if (r < 0.0 || r > 1.0 || g < 0.0 || g > 1.0 || b < 0.0 || b > 1.0) {
private static boolean check_factor(double factor) {
if (factor < 0) {
throw new RuntimeException(&quot;Factor can not be less than 0.0&quot;);

