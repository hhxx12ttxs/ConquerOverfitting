public class Solution {
public int removeDuplicates(int[] A) {
int len = A.length;

for (int i = 2; i < len; i++) {
if (A[i] == A[i - 1] &amp;&amp; A[i] == A[i - 2]) {

