public static void draw(int n, double sz, double x, double y)
{
if(n == 0) return ;
double x0 = x - sz / 2;
double x1 = x + sz / 2;
double y0 = y - sz / 2;
double y1 = y + sz / 2;

StdDraw.line(x0, y, x1, y);

