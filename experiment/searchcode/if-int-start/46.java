public class Solution {
public int searchInsert(int[] A, int target) {
int start = 0;
int end = A.length - 1;
while(start<=end){
int mid = (end + start)/2;

