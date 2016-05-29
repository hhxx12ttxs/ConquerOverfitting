package pack1;

public class Pow_x_n {


public double pow(double x, int n) {
int sign = (x>0)?1:-1;
if(n%2==0){
sign=1;
}
x=Math.abs(x);
double pow=1;
for(int i=0; i<n; i++){

