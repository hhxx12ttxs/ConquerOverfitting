while(!kue.isEmpty()){
int temp=kue.poll();
visited[temp]=true;

for(Neighbour nh=ver[temp].nh;nh!=null;nh=nh.next){
if(!visited[nh.ver_no] &amp;&amp; nh.wt>0){
//	visited[nh.ver_no]=true;
kue.add(nh.ver_no);

