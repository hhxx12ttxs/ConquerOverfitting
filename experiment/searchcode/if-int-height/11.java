public class Solution {
public int trap(int[] height) {
return trap1(height);
}

public int trap1(int[] height) {
int area = 0;

int left = 0;
int right = height.length - 1;

