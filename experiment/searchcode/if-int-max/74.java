public class Solution {
public int trap(int[] height) {
int max = 0;
int maxHeight = 0;
for (int i = 0; i < height.length; i++) {
if (height[i] > height[maxHeight]) {

