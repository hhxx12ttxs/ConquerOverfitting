public class Solution {
public int maxArea(int[] height) {
int size = height.length;
int result=(size-1)*((int)(height[0]<height[size-1]?height[0]:height[size-1]));
for(int p=0,q=size-1;p!=q;){

