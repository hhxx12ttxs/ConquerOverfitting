di=(end-index)/increment;
index+=(di*increment);

if(increment<0){
while(index>=end){
index+=increment;
done=NewPredict(vnf.current,end,vnf.increment,todo);

if(done==0){
/*			printf(&quot;predict stopped it. current %ld, end %ld\n&quot;,vnf.current,end);

