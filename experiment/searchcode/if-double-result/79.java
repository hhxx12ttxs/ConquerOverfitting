package th.c.Leetcode;

public class Pow {
public double pow(double x, int n) {
if(n < 0) {
x = 1/x; n = -n;
}
double result = 1;
while(n != 0) {
if((n&amp;1) != 0)
result *= x;
n >>= 1;
x *= x;
}
return result;
}
}

