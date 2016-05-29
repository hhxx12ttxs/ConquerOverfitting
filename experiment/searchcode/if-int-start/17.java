package findKTH;

public class Solution {
public int findKth(int[] A, int A_start, int[] B, int B_start, int k) {
if (A_start >= A.length) {
return B[B_start + k - 1];
}
if (B_start >= B.length) {

