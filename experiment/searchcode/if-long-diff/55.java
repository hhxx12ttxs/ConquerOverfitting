public class Solution {
public int sqrt(int x) {
long ret = 1;
long min_diff = Integer.MAX_VALUE;
long curr =  (start + end) /2;
while (curr != start) {
if(curr * curr == (long)x) {
return (int)curr;

