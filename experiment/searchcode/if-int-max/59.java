public class Solution {
public int trap(int[] A) {
int n = A.length;
if(n < 3) {
for(int i = 1; i < (n - 1); ++i) {
int height = Math.min(max_left[i], max_right[i]);
if(height > A[i]) {

