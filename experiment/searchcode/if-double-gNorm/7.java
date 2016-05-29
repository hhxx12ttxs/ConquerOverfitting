// predicted reduction.  Is computed efficiently depending on the case
private double predicted;

// if the step is at the region&#39;s border
private boolean maxStep;
// distance of the Gauss-Newton step
private double distanceGN;

double gnorm;

// Jacobian times the gradient
DenseMatrix64F Jg = new DenseMatrix64F(1,1);

