protected static double downFluctuatingFactor(List<Double> lambdas) {
int length = lambdas.size();
if (length <= 1) return 1.0;

double minRatio = 1;
double localMax = lambdas.get(0);

