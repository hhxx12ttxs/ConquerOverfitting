public static void   mergeSortedArray(int[] A, int m, int[] B, int n) {

int lenA=m-1;
int lenB=n-1;
int len=m+n-1;


for(int i=len; i>=0;i--){
if (lenB>=0&amp;&amp; lenA>=0){

