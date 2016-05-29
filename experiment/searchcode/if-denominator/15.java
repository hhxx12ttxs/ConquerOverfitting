public Rational(int numerator, int denominator) {
if (denominator == 0) {
System.err.println(&quot;That is not a rational  number&quot;);
}
if (denominator < 0) {
denominator = denominator * -1;

