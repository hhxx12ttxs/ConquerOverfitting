int i=0,j=0;
boolean flag=false;
for(int index=0;index<ts.length();index++){
if(ts.charAt(index)==&#39; &#39;){
if(flag){
ts.deleteCharAt(index);
index--;
}
if(i<j){
int m=i,n=(j-1+m)>>1;

