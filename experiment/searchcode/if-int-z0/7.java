public static int mandelbrot(Complex z0, int max) {
Complex z = z0;
for (int t = 0; t < max; t++) {
public static int colorMandelbrotFormula(Complex z0, int maxIter) {
if(maxIter - Mandelbrot.mandelbrot(z0, maxIter) == 0) {

