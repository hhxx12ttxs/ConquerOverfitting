private static Double previousMean;
private static Double previousStdDev;


public static double random(Double mean, Double stddev) {
if (newRandom != null &amp;&amp; mean.compareTo(previousMean) == 0
&amp;&amp; stddev.compareTo(previousStdDev) == 0) {

