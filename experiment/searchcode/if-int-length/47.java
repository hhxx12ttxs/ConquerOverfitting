public int removeDuplicates(int[] A) {
if(A.length==0) return 0;
int length = 1;
for(int i =1;i<A.length;i++){
if(A[i]==A[length-1]) continue;
A[length++] = A[i];
}
return length;
}
}

