final boolean forward = (t > t0);

// initialize working arrays
if (y != y0) {
System.arraycopy(y0, 0, y, 0, y0.length);
start(previousF.length - 1, stepSize, manager, equations, stepStart, y);
if (Double.isNaN(previousT[0])) {
return stopTime;

