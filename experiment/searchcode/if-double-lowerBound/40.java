public static String getRandomValue(final Random random, final int lowerBound, final int upperBound, final int decimalPlaces)
{
if(lowerBound < 0 || upperBound <= lowerBound || decimalPlaces < 0)
final double dbl = ((random == null ? new Random() : random).nextDouble() * (upperBound - lowerBound)) + lowerBound;

return String.format(&quot;%.&quot; + decimalPlaces + &quot;f&quot;, dbl);
}
}

