public Interval(double lowerBound, double upperBound) {
if (lowerBound < 0.0)
throw new IllegalArgumentException(
&quot;Illegal &#39;lo&#39; argument in Interval(double, double): &quot;
+ lowerBound);
if (upperBound > 1.0)

