for(int i = 0; i < A.length;){
if(A[i] > 0 &amp;&amp; A[i] <= A.length &amp;&amp; A[i] != i + 1 &amp;&amp; A[i] != A[A[i] - 1]){
for(int i = 0; i < A.length; ++i){
if(A[i] != i + 1){
return i + 1;
}
}
return (A.length == 0) ? 1 : A.length + 1;
}
}

