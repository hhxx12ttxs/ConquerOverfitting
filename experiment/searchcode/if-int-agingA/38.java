if (Precision.equals(y[0], 0.0, 1)) {
// return the first endpoint if it is a perfect root.
return x[0];
}

int nbPoints;
int signChangeIndex;
if (y[0] * y[1] < 0) {

// reduce interval if it brackets the root

