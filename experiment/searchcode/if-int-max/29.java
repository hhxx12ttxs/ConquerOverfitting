package epic;

class MaxSubArray{
int[] solution(int[] n){
if(n.length <2)
return null;
if(n.length == 2)
return n;

int maxStart = 0;
int maxEnd = 1;
int maxHereStart = 0;

