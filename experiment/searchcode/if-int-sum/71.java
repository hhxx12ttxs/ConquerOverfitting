public class Solution {
boolean isHappy(int n) {
if (n == 4) return false;

int sum = getSum(n);
if(sum == 1) {
return true;
}else {

