final class GrayscaleFilter
extends RGBImageFilter
{
private double Yr, Yg, Yb;

public GrayscaleFilter()
{
this(0.2126d, 0.7152d, 0.0722d);
}

public GrayscaleFilter(double Yr, double Yg, double Yb)

