parts=firstsums(A,middle);
if (parts[0]==parts[1])
return middle;
else{
int counter=1;
long rightpos=parts[1]; long rightneg=parts[1]; long leftpos=parts[0]; long leftneg=parts[0];
while(middle+counter<A.length){
if(middle+counter==A.length) rightpos=0;
else

