public class Solution {
public static int mySqrt(int x) {
if(x < 2) return x;
double left = 0;
double mid = x/2;
double right = x;
while(true){
if(mid * mid <= x &amp;&amp; (mid + 1) * (mid + 1) > x) return (int)mid;

