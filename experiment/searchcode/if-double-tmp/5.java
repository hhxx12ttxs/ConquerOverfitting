public class Solution {
public double pow(double x, int n) {
if (x == 1) return x;
else {
double tmp = pow(x, n / 2);
if (n % 2 == 0) return tmp * tmp;

