public static double stdev(double[] data) {
double size;
double mean;
double var;
double stdev;

size = data.length;
double temp = 0;
for (double a : data)
temp += (mean - a) * (mean - a);
var = temp / size;

stdev = Math.sqrt(var);

