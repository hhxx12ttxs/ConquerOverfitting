public int maxSubArray(int[] A) {
if(A.length==0)
return 0;
int max=A[0];
int sum = A[0];
for(int i=1;i<A.length;i++){
if(A[i]>=0){
if(sum<0){
sum=A[i];
if(max<sum){
max = sum;

