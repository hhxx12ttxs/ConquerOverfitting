public class ContainerWithMostWater {
public int maxArea(int[] height) {
if (height == null || height.length <= 1) return 0;
int max = 0;
int w = end - start;
int h = height[start] < height[end] ? height[start] : height[end];
int area = w * h;
if (area > max) max = area;

