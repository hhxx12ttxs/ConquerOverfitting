class Solution {
public int equi ( int[] A ) {
if(A.length<=1) return 0;
int sum = 0;
for(int x : A){
sum+=x;
}

int right_sum = 0;
int left_sum = sum;

for(int i = 0 ; i < A.length; i++){

