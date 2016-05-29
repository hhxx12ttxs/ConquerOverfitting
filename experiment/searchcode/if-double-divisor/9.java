public void setDividend(int dividend) {
this.dividend = dividend;
if (dividend != 0 &amp;&amp; divisor != 0)
simplify();
}

public void setDivisor(int divisor) {
if (0 == divisor) {

