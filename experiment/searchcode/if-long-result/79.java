long result = 1;
long divide = 1;
m-=1;
n-=1;
long small = Math.min(m, n);
for(int i=1;i<=small;i++){
divide*=(small-i+1);
result*=(m+n-i+1);
if(result%divide==0){

