layer, fieldName);

Interval[] intervals = getIntervals();
this.nIntervals = intervals.length;
if (intervals.length > 0) {
double stepG = (end.getGreen() - g) / (nIntervals - 1.0);
double stepB = (end.getBlue() - b) / (nIntervals - 1.0);

Map<Interval, Symbolizer> symbolsMap = new HashMap<Interval, Symbolizer>();

