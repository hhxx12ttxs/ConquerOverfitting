public double getResult() {
if ((n) > 0) {
return value;
} else {
return java.lang.Double.NaN;
}
}

public long getN() {
public double evaluate(final double[] values, final int begin, final int length) {
double sumLog = java.lang.Double.NaN;
if (test(values, begin, length)) {
sumLog = 0.0;
for (int i = begin ; i < (begin + length) ; i++) {

