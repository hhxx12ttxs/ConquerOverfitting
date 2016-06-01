public class Solution {
public double pow(double x, int n) {
if (n == 0) {
return 1;
double half = pow (x, n/2);
double ret = 0;
if ((n &amp; 1) == 0) {
ret = half * half;
} else {
ret = half * half * x;

