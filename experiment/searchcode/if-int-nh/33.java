visited[temp]=true;

for(Neighbour nh=ver[temp].nh;nh!=null;nh=nh.next){
if(!visited[nh.vertno]){
ver[nh.vertno].data= ver[nh.vertno].data+nh.wh;
if(!visited[nh.vertno]){
kue.add(nh.vertno);
visited[nh.vertno]=true;

