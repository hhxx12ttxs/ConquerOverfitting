public static final ThresholdNumberComparator DEFAULT = new ThresholdNumberComparator(.00001);
double threshold;

protected ThresholdNumberComparator(double threshold) {
this.threshold = threshold;
double absDiff = difference < 0 ? difference * -1 : difference;
if (absDiff < this.threshold) {
return 0;
}
else if (difference > 0){

