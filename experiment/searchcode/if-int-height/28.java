public class Solution {
public int maxArea(int[] height) {
if(height.length < 2)
return 0;

int i = 0;
int j = height.length - 1;
int max = Integer.MIN_VALUE;

