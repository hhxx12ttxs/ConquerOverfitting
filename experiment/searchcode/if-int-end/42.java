public class Solution {
public int searchInsert(int[] A, int target) {
if(A.length == 0){
if(target > A[0]){
return 1;
}else return 0;
}
int start = 0;
int end = A.length - 1;

