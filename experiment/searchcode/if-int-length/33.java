public class Solution {
public int removeDuplicates(int[] A) {
if (A.length == 0)
return 0;
int cur = A[0];
int i = 0;
int length = 1;

while (i < A.length - 1) {

