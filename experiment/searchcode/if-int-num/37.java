public class Solution {
public void nextPermutation(int[] num) {
int len = num.length;
for (int i = len - 1; i > 0; --i) {
if (num[i] > num[i - 1]) {

