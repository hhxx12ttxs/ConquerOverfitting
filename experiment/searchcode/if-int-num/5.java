public class Solution {
public void nextPermutation(int[] num) {
for (int i=num.length-1; i > 0; i--) {
continue;
}
for (int j=num.length-1; j >= i; j--) {
if (num[j] > num[i-1]) {

