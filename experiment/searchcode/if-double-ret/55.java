public static double[] calculateRootsOfEquation( double a, double b, double c ){

double[] ret;

if(a == 0){
ret = new double[1];
ret[0] = -c/b;
return ret;
}

double delta = calculateDelta(a, b, c);

if(delta < 0){
ret = new double[0];

