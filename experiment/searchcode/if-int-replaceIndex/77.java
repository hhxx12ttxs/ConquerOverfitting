int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
for(int i=0; i<A.length; i++){
if(A[i] > max)
max = A[i];
if(A[i] < min)
min = A[i];
}
//Change all values to Positive
for(int i=0; i<A.length; i++)

