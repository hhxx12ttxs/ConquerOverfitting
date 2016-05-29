public double getMinDifference(double[][] baselineWindow, double[][] comparisonWindow){

double minDiff = Double.MAX_VALUE;
double cumDiff=0;
double curDiff;
cumDiff = cumDiff/(squareSize*squareSize);

if(cumDiff < minDiff){

