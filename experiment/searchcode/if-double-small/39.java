paramDouble = 0.0D;
i = 1;
}
}
else
{
d = atanSmallArg(paramDouble);
if (i != 0)
d1 = -(1.570796326794897D + d1);
}
}

private static final double atanSmallArg(double paramDouble)
{
double d;
if ((paramDouble <= 0.4142135623730952D) &amp;&amp; (paramDouble >= -0.4142135623730952D))

