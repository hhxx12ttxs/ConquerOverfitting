import static java.lang.Math.abs;

public class Solution {
public int maxArea(int[] height) {
int area = 0, lower = 0, higher = height.length-1;
while(higher != lower){
if(height[lower] > height[higher]){

