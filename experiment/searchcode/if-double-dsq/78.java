if (specularColor == null) specularColor = defaultSpecularColor;
if (ch == null) ch = defaultChannels;
double[] reflected = new double[3];
for (int j = 0; j< ts; ++j)	{
x = 2*(j+.5)/ts - 1.0;
double dsq = x*x+y*y;
if (dsq <= 1.0)	{
z = Math.sqrt(1.0-dsq);

