private void makeEleNeg(int[] A, int pos)
{
if((pos < 0) || (pos >= A.length)) return;
A[pos] = -1;
int posPos = -1;
int posNeg = -1;
for(posPos = 0; posPos < A.length; posPos ++){
if(A[posPos] > 0){

