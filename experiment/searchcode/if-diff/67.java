public int removeElement(int[] A, int elem) {
int diff = 0;
for(int i = 0; i < A.length; ++i){
if(A[i] == elem)
++diff;
else
A[i - diff] = A[i];
}
return A.length - diff;
}
}

