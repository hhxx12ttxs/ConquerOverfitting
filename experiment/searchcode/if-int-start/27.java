public class Solution {
public int findMin(int[] num) {
int start = 0;
int end = num.length - 1;
while(start < end){
int mid = start + end;
mid /= 2;

