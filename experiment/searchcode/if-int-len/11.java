public int removeDuplicates(int[] A) {
int len = 0;
if(A.length == 0)
return 0;
for(int i = 0; i<A.length; ) {
A[len] = A[i];
int times = 1;
do {
if(times == 2) {

