
public class ContainerWithMostWater {

public int maxArea(int[] height) {
if(height == null || height.length == 0){
int max = 0;
int maxArea = 0;
for(int i=0; i<height.length; i++){
if(height[i] > max){

