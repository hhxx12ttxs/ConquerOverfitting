public class Problem231 {

public boolean isPowerOfTwo(int n) {

long base = 1;
long powerOfTwo = 1;

while (n >= powerOfTwo) {
if (n == powerOfTwo) {
return true;

