public class Solution {
public int trap(int[] height) {
if (height==null || height.length<=2) return 0;
int maxNow = height[0];

for (int i=0;i<maxIndex;i++) {
int cur = height[i];
if (cur>maxNow) {

