public static void merge(int A[], int m, int B[], int n) {
int indexA = 0;
int indexB = 0;

for(int i = A.length - 1; i >=0; i--) {

if(indexA == m ) {
A[i] = B[n - 1 - indexB];

