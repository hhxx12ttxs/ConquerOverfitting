* Uses {@link java.lang.Math#log(double)} to compute the logs.  Therefore,
* <ul>
* <li>If any of values are < 0, the result is <code>NaN.</code></li>
public double evaluate(final double[] values, final int begin, final int length) {
double sumLog = Double.NaN;
if (test(values, begin, length)) {

