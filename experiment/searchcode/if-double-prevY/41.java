public static double interLagrang(double x, double[] yValue, int size){
double lagranPol = 0;
double currentX = point1 + diff/10 * i;
double prevY = fun(prevX);
double currentY = fun(currentX);

double interpol = prevY + (currentX - prevX) * (currentY - prevY)/(currentX - prevX);

