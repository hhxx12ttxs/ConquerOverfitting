public class Solution {
public void nextPermutation(int[] num) {
int i;
for(i = num.length-1; i > 0; i--) {
reverse(num, i, num.length-1);

if(i == 0) return;
int j = i;
while(num[i-1] >= num[j]) j++;

