public class Sqrt {
public int mySqrt(int x) {
long start = 0;
long end = x;
while (start + 1 < end){
long mid = (end - start) / 2 + start;
if (mid * mid == x){

