
public class Pow{
public double pow(double x, int n){
if(n == 0){
return (double)1;
return x;
}
if(n < 0){
return pow(1/x, -n);
}
int n1 = n/2;
double r1 = pow(x, n1);

