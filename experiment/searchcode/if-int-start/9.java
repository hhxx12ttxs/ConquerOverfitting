class Solution {
public int findMin(int[] num) {
int start = 0, end = num.length-1;
while (start < end &amp;&amp; num[start] > num[end]) {
int mid = start + (end - start) / 2;

