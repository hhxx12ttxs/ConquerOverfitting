int start = 0, end = values.length-1;
double minDiff = Double.MAX_VALUE;
double min = Double.MAX_VALUE;

while(start <= end) {
double diff = Math.abs(values[mid]-d);
if(diff < minDiff) {
min = values[mid];
minDiff = diff;

