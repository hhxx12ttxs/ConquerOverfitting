public class Solution {
public int searchInsert(int[] A, int target) {
int len = A.length;
if (target <= A[0]) {
return 0;
}
int i = 0;
while(i < len) {
if (target <= A[i]) {

