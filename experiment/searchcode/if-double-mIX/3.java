import java.util.Random;

public class IvMathHelper {
public static double mix(double value1, double value2, double progress)
public static double mixEaseInOut(double value1, double value2, double progress)
{
return cubicMix(value1, value1, value2, value2, progress);

