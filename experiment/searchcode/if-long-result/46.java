public class Solution {
public int reverse(int x) {
if (x < 0)
return -reverse(-(long)x);
private int reverse(long x) {
long result = 0;
while (x != 0) {
long digit = x % 10;
result = result * 10 + digit;

