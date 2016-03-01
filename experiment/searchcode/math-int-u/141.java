    import java.util.LinkedList;
    import java.util.Queue;
    import java.util.Scanner;

    public class Ford_Fulkerson

    {

        private int[] parent;
        private Queue<Integer> queue;
        private int numberOfVertices;
        private boolean[] visited;

        
        public Ford_Fulkerson(int numberOfVertices)
        {
            this.numberOfVertices = numberOfVertices;
            this.queue = new LinkedList<Integer>();
            parent = new int[numberOfVertices + 1];
            visited = new boolean[numberOfVertices + 1];		
        }
        
        public Ford_Fulkerson()
        {
        
        }

        public boolean bfs(int source, int goal,  Graph G) //int graph[][],)
        {
            boolean pathFound = false;
            int destination, element;

            for(int vertex = 1; vertex <= numberOfVertices; vertex++)
            {
                parent[vertex] = -1;
                visited[vertex] = false;
            }

            queue.add(source);
            parent[source] = -1;
            visited[source] = true;

            while (!queue.isEmpty())
            { 
                element = queue.remove();
                destination = 1;
                System.out.println("passou0000");
                while (destination <= numberOfVertices)
                {
                	System.out.println(element);
                	System.out.println(destination);
                	//if( (G.get_arc(element, destination) != null) && !visited[destination])
                	// descomentar acima	
                		
                    //if (graph[element][destination] > 0 &&  !visited[destination])
                    {
                        parent[destination] = element;
                        queue.add(destination);
                        visited[destination] = true;
                    }
                    destination++;
                }
            }

            if(visited[goal])
            {
                pathFound = true;
            }
            return pathFound;
        }

     

        public int ford_Fulkerson(int graph[][], int source, int destination, Graph G, Graph residual_graph)
        {
            int u, v;
            int maxFlow = 0;
            int pathFlow;

            //int[][] residualGraph = new int[numberOfVertices + 1][numberOfVertices + 1];
            //copiar grafo antigo!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            //Graph residual_graph = new Graph() ;
            //residual_graph = G ;
            
            /*for (int sourceVertex = 1; sourceVertex <= numberOfVertices; sourceVertex++)
            {
                for (int destinationVertex = 1; destinationVertex <= numberOfVertices; destinationVertex++)
                {
                    residualGraph[sourceVertex][destinationVertex] = graph[sourceVertex][destinationVertex];
                }
            }*/
            System.out.println("passou22");
            while (bfs(source, destination, residual_graph))
            {
                pathFlow = Integer.MAX_VALUE;
                
                // gets the minimum flow that can be passed
                for (v = destination; v != source; v = parent[v])
                {
                	System.out.println("passou2222");
                    u = parent[v];
                    //pathFlow = Math.min(pathFlow, residualGraph[u][v]);
                    System.out.println(u) ;
                    System.out.println(v) ;
                    //int new_capacity = residual_graph.get_arc(u, v).get_maximum_capacity() - residual_graph.get_arc(u, v).get_flow() ;
                    // descomentar acima
                   // pathFlow = Math.min(pathFlow, new_capacity) ;
                    //descomentar acima
                }
                System.out.println("passou2");
                // take this flow from the arcs
                for (v = destination; v != source; v = parent[v])
                {
                    u = parent[v];
                    /*residual_graph.get_arc(u, v).add_flow(pathFlow) ;
                    residual_graph.get_arc(u, v).set_residual_capacity(residual_graph.get_arc(u, v).get_residual_capacity() - pathFlow) ;                  
                    residual_graph.get_arc(v, u).set_residual_capacity(residual_graph.get_arc(v, u).get_residual_capacity() + pathFlow) ;*/
                    // descomentar os tres acima
                    //residualGraph[u][v] -= pathFlow;
                    //residualGraph[v][u] += pathFlow;
                }

                maxFlow += pathFlow;	
            }
            
            
            return maxFlow;

        }

     

        public int main_ford(Graph G, int source, int sink, Graph residual_graph)

        {
            int[][] graph;
            int numberOfNodes;
            //int source;
            //int sink;
            int maxFlow;

            //Scanner scanner = new Scanner(System.in);
            System.out.println("Enter the number of nodes");
            numberOfNodes = G.get_number_nodes();

            graph = new int[numberOfNodes + 1][numberOfNodes + 1];

            System.out.println("Enter the graph matrix");
            for (int sourceVertex = 1; sourceVertex <= numberOfNodes; sourceVertex++)
            {
               for (int destinationVertex = 1; destinationVertex <= numberOfNodes; destinationVertex++)
               {
                  // graph[sourceVertex][destinationVertex] = scanner.nextInt();
               }
            }

            /*System.out.println("Enter the source of the graph");
            source = scanner.nextInt();

            System.out.println("Enter the sink of the graph");
            sink = scanner.nextInt();*/

            Ford_Fulkerson ford_Fulkerson_object = new Ford_Fulkerson(numberOfNodes);
            System.out.println("passou11111111111");
            //maxFlow = ford_Fulkerson_object.ford_Fulkerson(graph, source, sink);
            maxFlow = ford_Fulkerson_object.ford_Fulkerson(graph, source, sink, G, residual_graph);

            //System.out.println("The Max Flow is " + maxFlow);

            //scanner.close();
            return maxFlow ;

        }

    }

