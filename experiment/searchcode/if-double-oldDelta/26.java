double y2 = y0;
double oldDelta = x2 - x1;
int i = 0;
while (i < maximalIterationCount) {
setResult(x1, i);
return result;
}
if (Math.abs(oldDelta) <
Math.max(relativeAccuracy * Math.abs(x1), absoluteAccuracy)) {

