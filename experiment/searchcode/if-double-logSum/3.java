public double getLogOfSum(double[] logElements) {
double logSum = 0;
double max = Double.NEGATIVE_INFINITY;
for (int i = 0; i < logElements.length; i++)
if (logElements[i] > max)
max = logElements[i];
double sum = 0;
for (int i = 0; i < logElements.length; i++)

