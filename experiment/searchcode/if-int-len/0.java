if(A.length < 2){
return A.length;
}
int len = 2;
for(int i = 2;i < A.length;i++){
if(A[i] != A[len - 2]){
A[len] = A[i];
len++;
}
}
return len;

