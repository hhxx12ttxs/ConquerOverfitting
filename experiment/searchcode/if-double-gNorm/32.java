// intermediate values when computing cauchy step
private double gBg;
private double gnorm;

/**
* Specify configuration
protected void cauchyStep(double regionRadius, DenseMatrix64F step) {
double normRadius = regionRadius/gnorm;

double dist = distanceCauchy;
if( dist >= normRadius ) {

