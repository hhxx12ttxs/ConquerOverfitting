public class Solution {
public void nextPermutation(int[] num) {
if(num == null || num.length == 0) return;
for(int i = num.length - 1; i > 0; i--) {
if(num[i] > num[i-1]) {

