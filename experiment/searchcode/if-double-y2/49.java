public static double distancia_1(double Y1, double Y2, double _1X1, double X2, double F, double P){
double dist =(Y2-Y1)*((F/2)+P) + (_1X1-X2)*(F*((3^(1/2))/2)) + (X2*Y1-Y2*_1X1);
return dist;
}
public static double distancia_2(double Y1, double Y2, double _1X2, double X2, double F, double P){

