// delta is only > 0 when the time stamp for a given buck is within the range of tStart and tEnd
if (delta > 0) {
double value = values[i];

if (!Double.isNaN(value)) {
totalSeconds += delta;

