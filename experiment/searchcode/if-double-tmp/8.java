
public class Pow {
public double pow(double x, int n) {
long tmp = n;
if( tmp >= 0 )
return power(x, tmp);
else
return 1.0 / power(x, -tmp);
}
private double power(double x, long n) {

