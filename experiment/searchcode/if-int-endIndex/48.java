if(a.length==0)
return 0;

int last=a[0];
int count=1;
int endIndex=0;

for(int i=1; i<a.length; i++){
if(a[i]!=last){
endIndex++;

