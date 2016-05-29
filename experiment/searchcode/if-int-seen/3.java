public int trap(int[] height) {
int result = 0;
int maxSeenRight [] = new int[height.length];
int maxSeenSoFar = 0;
int maxSeenLeft = 0;
for(int i =height.length-1;i>=0;i--){
if(maxSeenSoFar<height[i]){

