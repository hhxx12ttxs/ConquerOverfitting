public int jump(int[] A) {
if(A.length==0)
return 0;
if(A.length==1){
return 0;
int cur =0;
Arrays.fill(step, 0);
for(int i=0;i<A.length;i++){
int temp = i+A[i];
if(temp>=A.length-1){

