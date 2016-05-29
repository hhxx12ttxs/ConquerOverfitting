public static double[] solveQuadraticEquation(double a, double b, double c){
double D = b*b-4*a*c;
if(D<0) return new double[]{};
if(D==0) return new double[]{-b/(2*a)};
else {
double aa = 2*a;
double sqD = Math.sqrt(D);
return new double[]{(-b-sqD)/aa, (-b+sqD)/aa};

