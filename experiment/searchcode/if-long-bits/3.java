public static boolean[] getBits(long l)
{
boolean[] bits = new boolean[64];
return getBits(l, bits);
}

public static boolean[] getBits(float f)
public static boolean[] getBits(double d)
{
return getBits(Double.doubleToRawLongBits(d));
}

public static boolean[] getBits(char c)

