public class Solution {
public int trap(int[] A) {
int result = 0;
int max = 0;
int maxIndex = -1;
for(int i = 0; i < A.length; ++i) {
if(A[i] > max) {

