for(int i = 0 ; i < 1000 ; i++){
if(i %2 == 0){
arr[i] = 0;
}else{
arr[i] = 1;
}
}
int N = 1000;
int sum = 0;
int idx;
for (idx=0; idx<N; idx++) sum += arr[idx];
for (idx=0; idx<N-sum; idx++) arr[idx] = 0;

