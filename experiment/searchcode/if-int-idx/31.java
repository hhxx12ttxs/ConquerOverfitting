int zIdx = -1, oIdx=-1, tIdx=-1;
for(int i = 0; i<A.length; i++){
if(A[i] == 0){
if(zIdx == -1){
zIdx = oIdx == -1? (tIdx == -1? i : tIdx) : oIdx;
A[oIdx] = 0;
oIdx++;
}
if(tIdx != -1){
int temp = A[i];
A[i] = A[tIdx];
A[tIdx] = temp;

