package Math;
/**
Implement pow(x, n).
*/

public class PowerXN {
public double pow(double x, int n) {
if(n == 1) {
return x;
}

double v = pow(x, Math.abs(n/2));

if(n%2 == 0) {

