public class Solution {
public int maxArea(int[] height) {
int N = height.length;

int x = 0;
int y = N - 1;
int result = Math.min(height[x], height[y]) * (y - x);

