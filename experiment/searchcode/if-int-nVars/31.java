for(int i=nvars; i<2*nvars;i++){
s+=names[i-nvars]+&quot; : &quot;;
for(int j=0; j<mynet.getParents(i).length;j++){
if(mynet.getParents(i)[j]<=nvars-1){

