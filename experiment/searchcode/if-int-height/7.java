
public class LargestRectangle {
public int largestRectangleArea(int[] height) {
if(height == null || height.length == 0) return 0;
int n = height.length;
if(n == 1) return height[0];

