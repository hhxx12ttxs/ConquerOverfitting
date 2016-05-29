for(int v=0; v< graph.length; v++){
if(!isSeen[v])DFS(v);
}
}
void DFS(int v){
isSeen[v] = true;
onCurrentStack[v] = true;
for(int u: graph[v]){
if(!isSeen[u])DFS(u);
else if(onCurrentStack[u]){

