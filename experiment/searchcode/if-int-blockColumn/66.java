log.info(&quot;Time needed to compute the path: &quot;+elapsedTime);

}

public void display(){
if(path.size() == 1){
log.info(&quot;Path of actions to go from the initial state to the final state&quot;);
for(int i=0;i<path.size();i++){
State state = path.get(i);
if(i==0){//Initial state

