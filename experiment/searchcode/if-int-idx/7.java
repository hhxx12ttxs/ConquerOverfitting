A[i] <= A.length ) {
if( A[i] == A[ A[i] - 1 ] )
break;
int tmp = A[i];
A[i] = A[A[i] - 1];
A[tmp - 1] = tmp;
}
}
int idx;
for( idx = 0; idx < A.length; idx++ ) {

