public int removeElement(int[] A, int elem) {
int length=A.length;
int j=length-1;

for(int i=0;i<length;i++){
if(A[i]==elem){
length--;
while(A[j]==elem){
if(i==j){
return length;

