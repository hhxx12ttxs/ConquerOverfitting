int length=A.length;
int end=length;

if(A==null || A.length==0){
return 0;
}

for(int i=0;i<end;i++){
if(A[i]==elem){
while(end>i){
//note: it is note-1

