public static double standardDeviation(List<Double> array, int option) {
if (array.size() < 2)
return Double.NaN;

double sum = 0.0;
double sd = 0.0;
double diff;
double meanValue = mean(array);

for (int i = 0; i < array.size(); i++) {

