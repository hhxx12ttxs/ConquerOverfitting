public class Solution {
public int removeDuplicates(int[] A) {
int len = A.length;

for (int i = 1; i < len; i++) {
if (A[i] == A[i - 1]) {

