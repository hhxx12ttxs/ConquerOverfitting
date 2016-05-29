public static int doMandelbrot(final double re, final double im, int iterations) {
double real = re;
double imaginary = im;

for (int i = 0; i < iterations; i++) {
double tempR = (real * real) - (imaginary * imaginary);
double tempI = 2 * (real * imaginary);

