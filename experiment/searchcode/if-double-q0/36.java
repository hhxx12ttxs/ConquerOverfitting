public static Rational reduce(final long numerator, final long denominator)
{
if (denominator == 0L)
throw new IllegalArgumentException();
public static boolean equals(final Rational q0, final Rational q1)
{
return q0.numerator() == q1.numerator() &amp;&amp; q0.denominator() == q1.denominator();

