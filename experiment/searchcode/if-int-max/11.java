public int maxSubArray(int[] a){
int length = a.length;
int iMax=0,jMax=0,max=0;
int i=0,j=0;
for(i=0;i<length;i++){
int tempMax,temp;
if(a[i]>0){

