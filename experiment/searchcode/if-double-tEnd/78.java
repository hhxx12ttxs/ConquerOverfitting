for (int i = 0; i < count; i++) {
this.timestamps[i] = tStart + ((double) i / (double) (count - 1)) * (tEnd - tStart);
double rawValue = rawValues[rawSeg];
if (!Double.isNaN(rawValue)) {
long rawLeft = rawTimestamps[rawSeg] - rawStep;

