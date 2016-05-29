boolean isBipertile = true;
boolean [] isSeen;
boolean [] color;
void processGraph(){

for(int v=0; v<graph.length; v++){
if(!isSeen[v]) DFS(v);
}
}

void DFS(int v){
isSeen[v] = true;
for(int u : graph[v]){

