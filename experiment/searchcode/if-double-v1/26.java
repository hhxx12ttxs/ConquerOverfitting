public static double[] calcNormal(double v[][])
{
double[] v1 = new double[3];
double[] v2 = new double[3];
double[] out = new double[3];

int x = 0;
int y = 1;
int z = 2;

v1[x] = v[0][x] - v[1][x];

