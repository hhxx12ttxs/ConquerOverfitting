//TODO check for cycle, if cycle exist topoligical sort is not possible
public class TopologicalSort {

int[][] graph;
boolean[] isSeen;
Stack reversePostOrder = new Stack();
void processGraph(){

for(int v=0; v<graph.length; v++){
if(!isSeen[v])DFS(v);

