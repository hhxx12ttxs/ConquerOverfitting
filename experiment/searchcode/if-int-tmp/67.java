public class Solution {
public boolean isPalindrome(int x) {
if (x < 0) return false;

int power = 1, tmp = x;
while (tmp >= 10) {
power *= 10;

