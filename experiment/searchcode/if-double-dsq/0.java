double c = Double.parseDouble(args[2]);

double dsq = b * b - 4 * a * c;
if (dsq >= 0) {
double d = Math.sqrt(dsq);
if (a != 0) {
} else {
dsq = -dsq;
double d = Math.sqrt(dsq);
// if (a != 0) { <= always true, as dsq would not be < 0 if a = 0.

