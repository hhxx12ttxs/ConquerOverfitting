
public class Solution {
public int solution(int[] A) {
if(A.length==2){
return 0;
}

float minAverage=0.0f;
int minPos=0;

for(int i=0; i<A.length-1;++i){
float average = (A[i]+A[i+1])/(2.0f);

