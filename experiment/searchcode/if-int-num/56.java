public class Solution {
public boolean isUgly(int num) {
if (num < 1) {
return false;
}

while (num % 2 == 0) {
num = num >> 1;

