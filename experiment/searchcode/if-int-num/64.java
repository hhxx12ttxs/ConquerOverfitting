public class Solution {
void reverse(int[] num, int start, int end){
for(int i = start, j = end; i < j; i++, j--){
for(int j = i + 1; j < num.length; j++)
if(num[i] < num[j]){
int tmp = num[i];
num[i] = num[j];

