public class Solution {
public int maxArea(int[] height) {
if (height == null || height.length == 0)
int y = height.length - 1;
int maxVal = y * Math.min(height[x], height[y]);

while (y > x) {
if (height[x] < height[y]) {

