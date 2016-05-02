package hw8;

import hw5.Multigraph;
import hw8.CampusParser.MalformedDataException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;



/**
 * This class's primary goal is to keep the main driver of the model from becoming more cluttered 
 * than it has to be, and to organize the graph manipulation and interpretation method 
 * for the purpose of easy reuse and modifications down the road. 
 * 
 */
public class CGT { // Campus Graph Tools
    
    
    /**
     * This method takes a Multigraph<String, String> object as well as the campus
     * data and uses it to create a graph of the campus.
     * 
     * @requires fileName and campusGraph cannot be null. fileName must contain the campus 
     * data.
     * @param filename : the name of the file. In this case, it should be the campus data.
     * @param campusGraph : the Multigraph that will store the paths and vertices of the 
     * campus.
     * @modifies campusGraph
     * @effects adds campus data to the graph
     * @throws MalformedDataException if file has formatting errors
     * 
     */
    public static void buildGraph(String fileName, Multigraph<String, Double> campusGraph) {
        Set<String> points = new TreeSet<String>();
        Map<String, List<Pair<String, Double>>> connections = new HashMap<String, List<Pair<String, Double>>>();
        try {
            CampusParser.parsePathData(fileName, points, connections);
        } catch (MalformedDataException e) {
            System.err.println("MalformedDataException encountered in buildGraph: " + e);
        }
        Iterator<String> pointItr = points.iterator();
        while(pointItr.hasNext()){
            String currentPoint = pointItr.next();
            campusGraph.addNode(currentPoint);
            Iterator<Pair<String, Double>> edgesItr = connections.get(currentPoint).iterator();
            while(edgesItr.hasNext()){
                //Get child node and weight
                Pair<String, Double> edgeData = edgesItr.next();
                campusGraph.addNode(edgeData.e1);
                campusGraph.addEdgeDirected(currentPoint, edgeData.e1, edgeData.e2);
            }
        }
    }
    
    
    /**
     * Takes in a graph as well as a desired start and ending destination, and modifies 
     * three other passed objects to provide data to the user about the best possible path.
     * Note that connectedNodes and connectedEdges are parallel stacks.
     * 
     * @requires graph, connectedNodes, connectedEdges, and totalCost cannot be null
     *              start and dest must be valid nodes
     * @param start : the desired starting location
     * @param dest : the desired ending location
     * @param graph : the campus path's representation
     * @param connectedNodes : A stack that will be used to print the path's vertices
     * @param connectedEdges : Another stack that will be used to print the path's edges
     * @param totalCost : Will be used to save the total cost of the path
     * @modifies connectedNodes 
     * @modifies ConnectedEdges 
     * @modifies totalCost
     * @effects adds path's vertices (if any)
     * @effects adds path's edges (if any)
     * @effects add's total cost to totalCost[0] (if a cost exists)
     * @throws IllegalArgumentException if there is a negative edge
     * 
     */
    public static void getPath(String start, String dest, Multigraph<String, Double> graph,
            Stack<Pair<String, String>> connectedNodes, Stack<Pair<Double, String>> connectedEdges, Double[] totalCost){
        
          //Return if either of the nodes cannot be found in the graph
          if(!graph.hasNode(start) || !graph.hasNode(dest))
             return;
        
          //Store nodes to be checked in order of priority
          PriorityQueue<Node> active = new PriorityQueue<Node>();
            
          //Represents known nodes
          Set<String> finished = new TreeSet<String>();

          //Path to itself
          Node beginningNode = new Node(start, 0.0, null, 0);
          beginningNode.path = beginningNode;
          active.add(beginningNode);

          while(!active.isEmpty()){
              
              //minDest = destination node          
              Node minDest = active.remove();          

              if(minDest.vertex.equals(dest)){
                  //The the min path is found and we're done with the search
                  makePathWithData(minDest, connectedNodes, connectedEdges, totalCost);
                  return;
              }
              
              //Check nodes that haven't been marked as finished
              if(!finished.contains(minDest.vertex)){
                  List<String> children = graph.nodeChildrenList(minDest.vertex);
                  for(int i = 0; i < children.size(); i++){
                      String child = children.get(i);
                      Double childCost = graph.findEdges(minDest.vertex, child).iterator().next();
                      if(childCost < 0.0)
                          throw new IllegalArgumentException("Edge from " + minDest.vertex + " to " + child + " cannot be negative!");
                      if(!finished.contains(child)){
                          Node newPath = new Node(child, minDest.cost + childCost, minDest, 
                                  minDest.numSteps + 1);
                          //add new potential path to active                  
                          active.add(newPath);
                      }
                  }
              }
              
              //Update nodes that have been checked
              finished.add(minDest.vertex);
            }
        }
        

        /**
         * The following is a helper method that is used with getPath(...) to make a representation
         * of the least expensive path.
         * 
         * @requires none of the passed objects are null. minPath is a linked compilation of nodes 
         * that represents the shortest path.
         * @param minPath : the node that has a tail of nodes that led to the least expensive path
         * @param connectedNodes : A stack that will be used to print the path's vertices
         * @param connectedEdges : Another stack that will be used to print the path's edges
         * @param totalCost : Will be used to save the total cost of the path 
         * @modifies connectedNodes
         * @modifies connectedEdges
         * @modifies totalCost
         * @effects adds path's vertices (if any)
         * @effects adds path's edges (if any)
         * @effects add's total cost to totalCost[0] (if a cost exists)
         *  
         */
        private static void makePathWithData(Node minPath, Stack<Pair<String, String>> connectedNodes,
                Stack<Pair<Double, String>> connectedEdges, Double[] totalCost) {

            Node nodeToAdd = minPath;
            boolean totalCostAdded = false;
            while(nodeToAdd.numSteps != 0){
                if(!totalCostAdded){
                    totalCost[0] = minPath.cost;
                    totalCostAdded = true;
                }
                //Must be read in reverse order
                connectedNodes.push(new Pair<String, String>(nodeToAdd.path.vertex, nodeToAdd.vertex));
                connectedEdges.push(new Pair<Double, String>(nodeToAdd.cost - nodeToAdd.path.cost, 
                        getDirection(nodeToAdd.path.vertex, nodeToAdd.vertex))); 

                nodeToAdd = nodeToAdd.path;
            }
            
        }


        /**
         * Takes two string of the form "pointX1,pointY1" and "pointX2,pointY2"
         * and uses them to derive an angle and print a direction.
         * 
         * @requires n1 and n2 are not null. both must represent doubles in the 
         * form listed above.
         * @param n1 : the String representing x1,y1
         * @param n2 : the String representing x2,y2
         * @returns a String of the form X, where X is N,S,E,or W
         *             or a String of the form XY, where X is N or S and Y is 
         *             E or W when the direction is diagonal.
         */
        //The class has been made public for purposes of testing
        public static String getDirection(String n1, String n2) {
            String[] pair1 = n1.split(",");
            String[] pair2 = n2.split(",");
            Double n1X = Double.parseDouble(pair1[0]);
            Double n1Y = Double.parseDouble(pair1[1]);
            Double n2X = Double.parseDouble(pair2[0]);
            Double n2Y = Double.parseDouble(pair2[1]);
            
            Double deltaX = n2X - n1X;
            Double deltaY = n2Y - n1Y;
            Double angle = Math.atan2(deltaY, deltaX);
                
            //Note that the angles aren't intuitive since 
            //0.0 is the point farthest to the northwest
            if(angle < Math.PI/8 && angle >= -Math.PI/8){
                return "E";
            }else if(angle <= 3*Math.PI/8 && angle >= Math.PI/8){
                return "SE";
            }else if(angle <= 5*Math.PI/8 && angle >= 3*Math.PI/8){
                return "S";
            }else if(angle <= 7*Math.PI/8 && angle >= 5*Math.PI/8){
                return "SW";
            }else if(angle <= -7*Math.PI/8 || angle >= 7*Math.PI/8){
                return "W";
            }else if(angle >= -7*Math.PI/8 && angle <= -5*Math.PI/8){
                return "NW";
            }else if(angle >= -5*Math.PI/8 && angle <= -3*Math.PI/8){
                return "N";
            }else{ //if(angle >= -3*Math.PI/8 && angle <= -1*Math.PI/8){
                return "NE"; 
            }
            
        }

}

/**
 * The following is an inner class that is used with dijkstra's algorithm to remember
 * the best possible path.
 * 
 */
class Node implements Comparable<Node>{
    String vertex;
    Double cost;
    Node path;
    int numSteps;

    //Abstraction Function: Every node N = <Vertex, Cost, Path, Place in Path>
    //Vertex = Name of node, Cost = total cost to reach this particular node, 
    //Path = the node that led to this node, numSteps = the number of jumps required
    //to reach this node on this path.

    //Representation Invariant: The vertex must be unique to this node for the purposes of
    //the graph. The cost should always be positive for the purposes of this graph.
    //The path must represent the actual node that led to the current node in the least 
    //cost path. numSteps must represent the actual number of steps for the dijkstra's algorithm to
    //function properly. If any of these conditions are broken, dijkstra's algorithm will not work 
    //as it should.

    //Constructor
    public Node(String vertex,    Double cost, Node path, int numSteps){
        this.vertex = vertex;
        this.cost = cost;
        this.path = path;
        this.numSteps = numSteps;
    }

    /**
     * Used to allow for comparisons when alphabetical ordering becomes important.
     * 
     * @return number representing priority first based on the cost, then based on 
     * the vertex. Negative numbers mean that this comes before other,
     * 0 means they share the same priority, and positive means that other comes before
     * this.
     * 
     */
    public int compareTo(Node other){
        if(this.cost == other.cost){
            return this.vertex.compareTo(other.vertex);
        }else{
            return this.cost.compareTo(other.cost);
        }
        
    }

    @Override
    /**
     * @returns a String representation of the vertex 
     */
    public String toString(){
        return vertex;
    }

    @Override
    /**
     * @returns true if one node is equal to the other. False otherwise. 
     * 
     */
    public boolean equals(Object other){
        if(! (other instanceof Node))
            return false;
        return this.vertex.equals(((Node)(other)).vertex);
    }
}


