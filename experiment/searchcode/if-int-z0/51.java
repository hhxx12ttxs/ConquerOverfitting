public void buildGraph(int z0[],int z1[]){

numVertices = z0.length;
edges = new LinkedList[numVertices];
Visited = new boolean[numVertices];

LinkedList<edge>list;
for(int i=0;i<z0.length;i++){

