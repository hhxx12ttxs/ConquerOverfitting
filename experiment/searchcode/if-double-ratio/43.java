public static double calculateCLalfaPolhamus(double aspectRatio, double lambdaLE, double mach, double taperRatio){
// NOTE: angle in radian
double k=0;
if (aspectRatio < 4){

k = 1. + (aspectRatio*(1.87-0.000233*lambdaLE))/100;
}
else if (aspectRatio >=4) {

