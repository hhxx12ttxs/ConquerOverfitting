public class Interpolate
{
public static double interpolate(double n, double Y1, double Y2, double Y3)
{
double a = Y2 - Y1;
double b = Y3 - Y2;
double c = Y1 + Y3 - 2 * Y2;

