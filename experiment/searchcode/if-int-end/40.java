int start = 0;
int end = A.length-1;
for(int i = 0; i <= end; i++){
if(A[i] == 0){
end--;
}
if(end < i){
break;
}
swap(A,end, i);

