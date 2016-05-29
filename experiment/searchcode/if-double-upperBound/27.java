public class ContinuousUniformGenerator implements RandomGenerator
{
private double lowerBound;
private double upperBound;

/**
* Constructor.
private static void checkBounds(final double lowerBound,
final double upperBound)
{
if (upperBound < lowerBound)

