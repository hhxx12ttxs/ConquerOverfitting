public class Solution {
public int removeDuplicates(int[] A) {
if (A.length == 0) {
return 0;
}
int length = 1;
int prev = A[0];
for (int i = 0; i < A.length; i++) {

