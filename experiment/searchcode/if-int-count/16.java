public class Solution {
public int removeDuplicates(int[] A) {
if (A.length == 0) return 0;
int j = 0;
int val = A[0];
int count = 1;
for (int i = 1; i < A.length; i++) {

