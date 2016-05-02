package edu.berkeley.cs169.server;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Set;

import com.google.gwt.core.client.GWT;

import edu.berkeley.cs169.client.helpers.StopoversSet;
import edu.berkeley.cs169.server.graph.Edge;
import edu.berkeley.cs169.server.graph.Location;
import edu.berkeley.cs169.server.graph.Node;
import edu.berkeley.cs169.server.graph.NodeType;
import edu.berkeley.cs169.server.graph.Path;

public class Algorithm {

    // We agreed on these booleans to match the UI
    private boolean bBikeOnly = false; // Bike-only route; use only edges that have the BIKE property set to true
    private boolean bMainPaths = true; // Use main paths only; use only edges that have the MAIN property set to true
    private boolean bNoStairs = false; // No outdoor stairs; use only edges that have STAIRS property set to false
    private boolean bScenic = true; // Prefer scenic routes even if it takes longer
    private boolean bNoCrowds = false; // Prefer non-crowded routes even if it takes longer
    private boolean bPassMCF = false; // Must pass by a Node that is of type Lab
    private boolean bPassLib = false; // Must pass by a Node that is of type Library
    private boolean bPassCafe = false; // Must pass by a Node that is of type Cafe

    private Location startloc; //holds the starting location
    private Location endloc;	// holds the destination location
    private byte slopePref;
    // Values of 1,2,3 corresponds to preference towards negative slope, flat slope, positive slope, respectively. 
    // Value of 0 means user has no preference.

    /**
     * Constructor for an algorithm. Tell this Algorithm what preferences you have.
     * @param options HashMap of options you're passing in
     * @param slope byte value of slope preference
     */
    public Algorithm (HashMap options, byte slope) {
        if (options.get("bike") != null)
            bBikeOnly = ((Boolean) options.get("bike")).booleanValue();
        if (options.get("main_only") != null)
            bMainPaths = ((Boolean) options.get("main_only")).booleanValue();
        if (options.get("no_stairs") != null)
            bNoStairs = ((Boolean) options.get("no_stairs")).booleanValue();
        if (options.get("scenic") != null)
            bScenic = ((Boolean) options.get("scenic")).booleanValue();
        if (options.get("no_crowd") != null)
            bNoCrowds = ((Boolean) options.get("no_crowd")).booleanValue();

        slopePref = slope;
    }

    public Algorithm() {} // Yes, you can have a default algorithm...

    /**
     * A way to set stopovers dynamically on this Algorithm
     * @param s A StopoversSet defining stopover booleans
     */
    
    public void setStopovers(StopoversSet s) {
        bPassMCF = s.passLab;
        bPassLib = s.passLib;
        bPassCafe = s.passCafe;
    }

    /**
     * The Algorithm that takes in two Locations.
     * 
     * @param loc1
     * @param loc2
     * @return Path Route of nodes plus distance
     */
    public Path find_path(final Location loc1, final Location loc2) {
        startloc = loc1;
        endloc = loc2;
        
        //System.out.println("slope pref: " + slopePref);
       // System.out.println("main path: " + bMainPaths);
        //System.out.println("No stairs: " + bNoStairs);
        if (bPassMCF || bPassLib || bPassCafe){
            // At least one stopover is selected; invoke special path calculator
            //System.out.println("Stopover selected");
        	
        	Path stopoverpath = path_stopover(loc1, loc2);
            

            return stopoverpath;
        }

        Node[] min_pair = shortest_Nodes(loc1.nodes,loc2.nodes);
        
        
             
        
        if (min_pair == null) {
            return null;
        }

        Path locpath = find_path(min_pair[0], min_pair[1]);
        return locpath;
    }

    /**
	 * The Algorithm that takes in two Nodes.
	 * Now implementing with sorted arrays (priority queues)
	 * @param start
	 * @param end
	 * @return Node[] route of nodes
	 */
	
	public Path find_path(final Node start, final Node end) {
	    Path toreturn = new Path();
	    if (start.id == end.id){
	        Node[] tmp = {start};
	        toreturn.nodes = tmp;
	        toreturn.distance = 0;
	        return toreturn;
	    }
	
	    ArrayList closedlist = new ArrayList(); // initialize closedlist
	    //ArrayList openlist = new ArrayList(); // initialize openlist
	    PriorityQueue openlist = new PriorityQueue(); 
	    Node[] path; // initialize path, which is to be returned
	
	    start.setG(0); // starting node's f,g,h values are set
	    start.setH(calcDistance3d(start.x, start.y,start.elevation, end.x, end.y,end.elevation));
	    start.setF();
	    openlist.add(start); // add starting node to openlist
	    boolean foundpath = false;
	    while (openlist.size() != 0) { // while openlist is not empty
	        
	        Node minF_Node = (Node)openlist.poll();
	        closedlist.add(minF_Node);
	        if (minF_Node.id == end.id) { 
	        	foundpath = true;
	        	break;
	        }
	        Node[] surroundingNodes = setToarray(minF_Node.getSurroundingNodes());
	        
	        // go through all of the surrounding nodes
	        for (int i = 0; i < surroundingNodes.length; i++) { 
	            Node thisNode = surroundingNodes[i];
	            // check option settings to see if node is usable and node is not inside closedlist
	            if (isUsable(minF_Node,thisNode,start,end)) { 
	                            	
	            	// calculate the cost of the node under investigation
	                double[] cost = calculate_cost(thisNode, minF_Node, end); 
	                
	
	                if (openlist.contains(thisNode)) { 
	                	if (cost[0] < thisNode.g) { // check whether or not to
	                        // modify f,g,h values, and
	                        // parent of this node
	                        thisNode.setParent(minF_Node);
	                        thisNode.setG(cost[0]);
	                        thisNode.setH(cost[1]);
	                        thisNode.setF();
	                        openlist.remove(thisNode);
	                        openlist.add(thisNode);
	                    }
	                	
	                    
	                }else if (closedlist.contains(thisNode)){ //case that nodes is already in the closedlist
	                	if (cost[0] < thisNode.g) { // check whether or not to
	                        // modify f,g,h values, and
	                        // parent of this node
	                        thisNode.setParent(minF_Node);
	                        thisNode.setG(cost[0]);
	                        thisNode.setH(cost[1]);
	                        thisNode.setF();
	                        closedlist.remove(thisNode);
	                        closedlist.add(thisNode);
	                    }
	                }else { // case that node is not already in the open list or closedlist
//	                	 if this node is
	                    // not already in
	                    // openlist or closedlist
	                    //add_Node(openlist, thisNode); // add node to openlist
	                	thisNode.setG(cost[0]); // set the f,g,h costs of the
	                    // node
	                    thisNode.setH(cost[1]);
	                    thisNode.setF();
	                    thisNode.setParent(minF_Node); // set minF_Node as
	                    // parent of this node
	                	openlist.add(thisNode);
	                }
	            }
	        }
	    }
	
	    if (!foundpath) { // no solution
	        return null;
	    } else {
	        ArrayList tmp_path = new ArrayList(); // tmp_path is a new
	        // temporary arraylist
	        tmp_path.add(end); // add the end node to tmp_path
	        while (((Node)tmp_path.get(0)).id != start.id) { // don't know if this comparison
	            // works (seems to work but
	            // potential bugs later)
	            Node first_Node = (Node) tmp_path.get(0);
	            tmp_path.add(0, DatParser.getNodeFromId(first_Node.parentId)); // add the parent of the
	            // node to the front of the
	            // arraylist
	        }
	        int size = tmp_path.size(); // create an array of node to return,
	        // and copies the elements in tmp_path
	        path = new Node[size];
	        for (int i = 0; i < size; i++) {
	            path[i] = (Node) tmp_path.get(i);
	
	        }
	        toreturn.nodes = path;
	        toreturn.distance = path_distance(path);
	        return toreturn;
	    }
	}

	/**
     * 
     * See notes in the case of all three stopover selection, as the notes apply to all three cases
     * @param loc1 - starting location
     * @param loc2 - ending location
     * @return node array representing path from starting node -> stopover(s) -> ending node
     */ 
    private Path path_stopover(Location loc1, Location loc2){
        Path thepath = null;
        /*	Node[] cafes = new Node[3]; //need to have real stopover node arrays
        Node[] libraries = new Node[3];
        Node[] labs = new Node[3];
         */
        Node[] cafes = DatParser.getStopovers(NodeType.CAFE);
        Node[] libraries = DatParser.getStopovers(NodeType.LIBRARY);
        Node[] labs = DatParser.getStopovers(NodeType.LAB);

        Node[] loc1nodes = setToarray(loc1.nodes);
        Node[] loc2nodes = setToarray(loc2.nodes);
        //case when user selects only one stopover (order does not apply here)

        if ((bPassCafe&&!bPassLib&&!bPassMCF) || (!bPassCafe&&bPassLib&&!bPassMCF) || (!bPassCafe&&!bPassLib&&bPassMCF)){
            Node[][] allNodes = new Node[3][];
            //set the first and end array of nodes
            allNodes[0] = loc1nodes;
            allNodes[2] = loc2nodes;
            Node[] onestopover;
            //set which stopover is selected
            if (bPassCafe){
                onestopover = cafes;
            }else if (bPassLib){
                onestopover = libraries;
            }else{ //only computer lab was selected
                onestopover = labs;
            }
            if (onestopover == null){
                //System.out.println("no stopover data");
                GWT.log("no stopover data", null);
                return null;
            }else{
                //System.out.println("there is stopover data");
            }
            //set allNodes so now it's starts -> stopovers -> ends
            allNodes[1] = onestopover; 
            thepath = best_nodes(allNodes); //return best paths
            
            //case when user selects all three stopovers(6 ordering possible)
        }else if (bPassCafe&&bPassLib&&bPassMCF) {
            Node[][] allNodes = new Node[5][];
            Path [] bestPaths = new Path[6]; //where we will keep the intermediate best nodes
            //populate allNodes
            allNodes[0] = loc1nodes;
            allNodes[1] = cafes;
            allNodes[2] = libraries;
            allNodes[3] = labs;
            allNodes[4] = loc2nodes;
            bestPaths[0] = best_nodes(allNodes); //first bestnodes
            allNodes[1] = cafes;
            allNodes[2] = labs;
            allNodes[3] = libraries;
            bestPaths[1] = best_nodes(allNodes); //second bestnodes
            allNodes[1] = libraries;
            allNodes[2] = labs;
            allNodes[3] = cafes;
            bestPaths[2] = best_nodes(allNodes); //third bestnodes
            allNodes[1] = libraries;
            allNodes[2] = cafes;
            allNodes[3] = labs;
            bestPaths[3] = best_nodes(allNodes); // fourth bestnodes
            allNodes[1] = labs;
            allNodes[2] = cafes;
            allNodes[3] = libraries;
            bestPaths[4] = best_nodes(allNodes); // fifth bestnodes
            allNodes[1] = labs;
            allNodes[2] = libraries;
            allNodes[3] = cafes;
            bestPaths[5] = best_nodes(allNodes); // sixth bestnodes
                    
            
            // DON'T FORGET TO CHECK FOR NULL RETURNS!!!
            double min_distance = Double.MAX_VALUE;
            for (int i=0;i<bestPaths.length;i++){
                if (bestPaths[i] != null){
                	if (bestPaths[i].distance < min_distance){
                		min_distance = bestPaths[i].distance; 
                    	thepath = bestPaths[i];
                	}
                }
            }

        }
        else { // case when user selects two stopovers(2 ordering possible)
            Node[] stopover1 = null;
            Node[] stopover2 = null;
            if (!bPassCafe){
                stopover1 = libraries;
                stopover2 = labs;
            }else if (!bPassLib){
                stopover1 = cafes;
                stopover2 = labs;
            }else{
                stopover1 = cafes;
                stopover2 = libraries;
            }
            Node[][] allNodes = new Node[4][];
            allNodes[0] = loc1nodes;
            allNodes[1] = stopover1;
            allNodes[2] = stopover2;
            allNodes[3] = loc2nodes;
            Path tmp1 = best_nodes(allNodes);
            allNodes[1] = stopover2;
            allNodes[2] = stopover1;
            Path tmp2 = best_nodes(allNodes);
            //check for null returns for path objects
            if (tmp1 == null && tmp2 == null){
            	return null;
            }
            if (tmp1 == null){
            	return tmp2;
            }
            if (tmp2 == null){
            	return tmp1;
            }
            
            if (tmp1.distance < tmp2.distance){
                thepath = tmp1;
            }else{
                thepath = tmp2;
            }
        }
        return thepath;
    }

    private Node[] setToarray(Set s){
        Iterator iter = s.iterator();
        Node[] toreturn = new Node[s.size()];
        int cnt = 0;
        while (iter.hasNext()){
            Node currNode = (Node)iter.next();
            toreturn[cnt] = currNode;
            cnt += 1;
        }
        return toreturn;
    }
    
    /**
     * 
     * @param allNodes - an array of array of nodes, it will be of the form [[starting nodes] [stopover1 nodes] ... [stopover3 nodes] [ending nodes]]
     * @return an array of best nodes, where each node is picked from each array of nodes. So one from starting location, one from stopover, ..etc.
     * 
     */
    private Path best_nodes(Node[][] allNodes){
        int path_length = allNodes.length;     //determines how many nodes we're picking
        Path[] bestpaths = new Path[path_length-1];
        Node[] bestnodes = null;
        double min_dist = Double.MAX_VALUE;
        int[] indexArray = new int[path_length];
        for (int i=0;i<indexArray.length;i++){  //initialize indexArray to all 0's
            indexArray[i] = 0;
        }
        int[] maxindexArray = new int[path_length];
        for (int i=0;i<maxindexArray.length;i++){ //set a maximum values index Array
            maxindexArray[i] = allNodes[i].length - 1;
        }
        int num_loops = 1;
        for (int i=0;i<path_length;i++){
            num_loops *= allNodes[i].length;  //calculate number of loops needed
        }

        int cnt = 0; 		//set count to 0
        Node[] tmp_bestnodes = new Node[path_length];

        while (cnt < num_loops){ 		//start looping over different index values

            for (int i=0;i<path_length;i++){
                tmp_bestnodes[i] = allNodes[i][indexArray[i]];
            }
            //I realized that running find_path on stopovers takes too long
            //Instead, i'm going to use Euclidean distance
            //Path[] tmp_paths = new Path[path_length -1];  //where we will hold temporary path values

            /*
        	for (int i=0; i <path_length - 1;i++){

        		tmp_paths[i] = find_path(tmp_bestnodes[i], tmp_bestnodes[i+1]);
                if (tmp_paths[i] == null){
                	nullpath = true;
                	break;
                }

            }
             */
            double distance = 0;
            //this is actually not being used anymore
            /*
            	for (int i=0;i<path_length-1;i++){
                	distance += tmp_paths[i].distance;
            	}
             */
            distance = path_distance(tmp_bestnodes);
            if (distance < min_dist){
                //also check that the routes are not null
                boolean nullpath = false;
                for (int i=0;i<tmp_bestnodes.length-1;i++){
                    if(find_path(tmp_bestnodes[i],tmp_bestnodes[i+1]) == null){
                        nullpath = true;
                        break;
                    }
                }
                if (!nullpath){
                    min_dist = distance;
                    //bestpaths = tmp_paths;
                    bestnodes = new Node[tmp_bestnodes.length];
                    for (int i=0;i<tmp_bestnodes.length;i++){  //save a copy of tmp_bestnodes
                        bestnodes[i] = tmp_bestnodes[i];
                    }
                }
            }

            indexArray = incrIndex(indexArray,maxindexArray);	
            cnt += 1;
        }
        //reconstructing the path object with the best stopover nodes
        if (bestnodes == null){
            return null;
        }
        
        for (int i=0;i<bestnodes.length;i++){
            if (bestnodes[i] == null){
                return null;
            }
        }
        Node[] firststopover = {bestnodes[1]}; 
        Node[] laststopover = {bestnodes[bestnodes.length-2]};
        bestnodes[0] = shortest_Nodes(setToarray(startloc.nodes), firststopover)[0];
        bestnodes[bestnodes.length-1] = shortest_Nodes(laststopover, setToarray(endloc.nodes))[1];

        for (int i=0;i<bestnodes.length-1;i++){
            bestpaths[i] = find_path(bestnodes[i],bestnodes[i+1]);
        }

        //combining the fragmented paths into one
        Path toreturn = path_combine(bestpaths);
        //saving stopover information
        for (int i=1; i< bestnodes.length -1;i++){
            if (bestnodes[i].type.i == 3){
                toreturn.cafe = bestnodes[i];
            }
            if (bestnodes[i].type.i == 4){
                toreturn.lab = bestnodes[i];
            }
            if (bestnodes[i].type.i == 5){
                toreturn.library = bestnodes[i];
            }
        }
        return toreturn;
    }

    private int[] incrIndex(int[] index, int[] maxindex){
        int[] newIndex = new int[index.length];
        for (int i=0;i<index.length;i++){ //increment last element of index by 1 and store that in newIndex
            if (i == index.length -1){
                newIndex[i] = index[i]+1; // increment last element by 1
            }else{
                newIndex[i] = index[i]; // leave rest as is
            }
        }
        for (int i=newIndex.length -1;i > 0; i--){ //check for overflow and adjust accordingly
            if (newIndex[i] > maxindex[i]){
                newIndex[i-1] +=1;
                newIndex[i] = 0;
            }
        }
        return newIndex;

    }

    /**
     * appends to path objects together but make sure path objects are connected,
     * or else it doesn't make sense
     * @param path1  
     * @param path2
     * @return the appended path object
     */    
    private Path path_append(Path path1, Path path2){
        if (path1 == null){
            return path2;
        }
        if (path2 == null){
            return path1;
        }
        Path toreturn = new Path();
        int length1 = path1.nodes.length;
        int length2 = path2.nodes.length;
        Node[] totalnodes = new Node[length1 + length2];
        for (int i=0;i<length1;i++){
            totalnodes[i] = path1.nodes[i];
        }
        for (int i=0;i<length2;i++){
            totalnodes[i+length1] = path2.nodes[i];
        }
        toreturn.nodes = totalnodes;
        toreturn.distance = path1.distance + path2.distance;
        return toreturn;
    }
    private Path path_combine (Path[] paths){
        Path toreturn = null;

        for (int i=0;i<paths.length;i++){
            toreturn = path_append(toreturn,paths[i]);
        }
        return toreturn;
    }
    
    
    
    /**
     * 
     * @param path
     * @return the total 3D euclidean distance of the input path
     */
    private double path_distance (Node[] path){
        double distance = 0;
        for (int i=0; i < path.length - 1; i++){
            distance += (calcDistance3d(path[i].x, 
            							path[i].y,
            							path[i].elevation, 
            							path[i+1].x, 
            							path[i+1].y,
            							path[i+1].elevation));
        }
        return distance;

    }

    private double[] calculate_cost(final Node thisNode, final Node minF_Node,
            final Node end) { 
        // calculate cost of this node, based on minF_node, and end node
        double[] results = new double[2];
        double slope_penalty,crowd_penalty,scenery_factor;
        
        
        //int[] originalArray =  {55, 66};
        int[] no_slope = {81,64,49,36,25,16,9,4,1,0,1,4,9,16,25,36,49,64,81}; //initialize arrays that will hold penalty values
        int[] pos_slope ={324,289,256,225,196,169,144,121,100,81,64,49,36,25,16,9,4,1,0};
        int[] neg_slope ={0,1,4,9,16,25,36,49,64,81,100,121,144,169,196,225,256,289,324};
        
        /* creating GLatLng objects does not seem to be working
        //get the distance (in meters) between thisNode and minF_Node       
        GLatLng thisPoint = new GLatLng(thisNode.y,thisNode.x);
        GLatLng minFPoint = new GLatLng(minF_Node.y, minF_Node.x);
        double distance = thisPoint.distanceFrom(minFPoint);
        */
        double kilometerperdegree = 100;
        double feetperkilometer = 3280;
        double diffcoord = Point2D.distance(	thisNode.x,thisNode.y,
        										minF_Node.x,minF_Node.y);
        diffcoord *= kilometerperdegree;
        diffcoord *= feetperkilometer;
        
        /* tried to do the calculation myself
        double latA = thisNode.y * (Math.PI/180);
        double lonA = thisNode.x * (Math.PI/180);
        double latB = minF_Node.y * (Math.PI/180);
        double lonB = minF_Node.x * (Math.PI/180);
        double cosAOB = Math.cos(latA)* Math.cos(latB) * Math.cos(lonB-lonA) + Math.sin(latA)*Math.sin(latB);
        double AOB = Math.acos(cosAOB);
        double distance = earthRadius * (AOB/(2*Math.PI));
        */
        
        //elevation is in feet
        double gradient = Math.atan((thisNode.elevation - minF_Node.elevation)/diffcoord) * (180/Math.PI);
        
               
        int index = 0;
        for (int i = 0 ; i < 18; i++){  //calculates which index of the array the gradient correspond to
            if (i*10 - 90 <= gradient && gradient < (i+1)*10 - 90){ // the gradient is in between index and index+1
                index = i;
                break;
            }		
        }
        Edge thisEdge = getEdge(thisNode, minF_Node);
        
        
        slope_penalty = 0;
        // calculate the slope penalty of thisNode        
        switch (slopePref) {
        case 0: // no preference   
        	slope_penalty = 0;
        	break;
        case 1: //negative slope preference
            if (!thisEdge.thruBuilding){
            	slope_penalty = linearInterpolation(neg_slope[index], neg_slope[index+1],gradient);
            }else{
            	slope_penalty = neg_slope[9];
            }
            break;
        case 2: //flat preference
            if (!thisEdge.thruBuilding){
            	slope_penalty = linearInterpolation(no_slope[index],no_slope[index+1], gradient);
            }else{
            	slope_penalty = no_slope[9];
            }
            break;
        case 3:  //positive slope preference
            if (!thisEdge.thruBuilding){
            	slope_penalty = linearInterpolation(pos_slope[index],pos_slope[index+1],gradient);
            }else{
            	slope_penalty = pos_slope[9];
            }
            break;
        }
        
        ///System.out.println(gradient);
        //System.out.println(index);
        //System.out.println(slope_penalty);
        scenery_factor = 0;
        if (bScenic){
            scenery_factor = thisEdge.scenicRating;
        }
        
        crowd_penalty = 0;
        if (bNoCrowds){
            crowd_penalty = thisEdge.crowdRating;
        }

        //standard cost is the 3d distance between two nodes
        double standard_cost = calcDistance3d(	thisNode.x,
												thisNode.y,
												thisNode.elevation,
												minF_Node.x,
												minF_Node.y,
												minF_Node.elevation);
        
        //multiply penalties and divide rewards from the standard cost  
        results[0] = minF_Node.g + standard_cost * (1 + slope_penalty + crowd_penalty)/(1+scenery_factor);
        
        //calculate the h value (heuristics)
        //must be a underestimate of the cost required to get to destination
        //we will be using the 3D distance directly from thisnode to destination
        results[1] = calcDistance3d(thisNode.x,
        							thisNode.y,
        							thisNode.elevation,
        							end.x,
        							end.y,
        							end.elevation);
        if (bScenic){
        	results[1] /= 6;
        }
        
        return results;
    }
    
    private double calcDistance3d(double x1,double y1,double z1,double x2,double y2,double z2){
        
        
        double feetperkilometer = 3280;
        double kilometerperdegree = 100; //approx. 100km in one degree of lat or lng
        // distance between the two coordinates in feet
        double diffcoord = Point2D.distance(x1,y1, x2,y2) * (kilometerperdegree * feetperkilometer);
        double distance3d = Math.sqrt(diffcoord*diffcoord + (z1-z2)*(z1-z2));
    	return distance3d;
    }
    
    private double linearInterpolation(int a, int b, double gradient){
    	double toreturn;
    	
    	//first determine the ratio
    	// Method: Divide the gradient by 10, then subtract the int version of gradient/10
    	// from gradient/10.  If gradient is negative, add 1.
    	//-65 -> -6.5 -> -6.5 + 6 -> -0.5 -> 1 + -0.5 -> 0.5
    	// 65 -> 6.5 -> 6.5 - 6 -> 0.5
    	double ratio;
    	ratio = gradient/10;
		int int_ratio = (int)ratio;
		ratio = ratio-int_ratio;
		
		if (gradient < 0){ //when gradient is negative
    		ratio = 1+ ratio;
    	}
    	
     	// determine the linear interpolation given a (start value) and b (end value) and the ratio
    	if (a < b){
    		toreturn = a+(b-a)*ratio;
    	}else{
    		toreturn = a-(a-b)*ratio;
    	}
    	    	
    	return toreturn;
    }
    
    
    private Edge getEdge (Node n1, Node n2){
        Edge toreturn = null;
        Set n2set = n2.surroundingEdges;
        Iterator edgeiter = n2set.iterator();
        Edge currEdge;
        while (edgeiter.hasNext()){
            currEdge = (Edge) edgeiter.next();
            if (currEdge.begin.id == n1.id || currEdge.end.id == n1.id){
                toreturn = currEdge;
                break;
            }
        }
        return toreturn;
    }

    /**
     * Takes in two nodes and checks that the edge connecting the two
     * fulfills the user specification.
     * @param minF - node with minimum F value
     * @param n - possible next node
     * @return true or false depending on usability of node n
     */
    private boolean isUsable(Node minF, Node n, Node beginNode, Node endNode) {

        Edge theEdge = getEdge(minF, n);

        if (n.type.i == NodeType.CONSTRUCTION) {
            return false;
        }

        // If user wants bike only routes and this edge doesn't allow bikes...
        if (bBikeOnly && !theEdge.bikesAllowed) {
            return false;
        }

        // If user only wants main paths, which disallows thru buildings...
        // If the edge is not a main path, but is directly coming out of
        // a begin node or an end node, then it's ok not being a main path.
        if (bMainPaths && !theEdge.isMainPath) {
            return false;
        }

        // If user doesn't want to confront outdoor stairs and this edge has outdoor stairs...
        if (bNoStairs && theEdge.hasStairs) {
            return false;
        }

        // Well then, this Edge is okay.
        return true; 
    }

    /**
     * Determine which index in the openlist corresponds to the node with lowest f value
     * @param openlist
     * @return

    private int minF_index(final ArrayList openlist) { 
        int min_index = -1; // this method simply goes through all the elements
        // in openlist and figures out which one has the
        // lowest f value
        double min_value = Double.POSITIVE_INFINITY; // we would change this if
        // we kept our openlist
        // sorted on f values
        for (int i = 0; i < openlist.size(); i++) {
            if (((Node) openlist.get(i)).f < min_value) {
                min_index = i;
                min_value = ((Node) openlist.get(i)).f;
            }
        }
        return min_index;
    }

    private int minF_index(final PriorityQueue openlist){
        return 0;
    }
     **/

    /**
     * Returns the best (shortest distance) pair of nodes given two arrays of nodes
     * @param Nodes1
     * @param Nodes2
     * @return
     */
    private Node[] shortest_Nodes(Node[] Nodes1, Node[] Nodes2) { 
        Node[] toReturn = new Node[2];
        toReturn[0] = null;
        toReturn[1] = null;
        // Start the minimum as high as possible
        double min_dist = Double.POSITIVE_INFINITY;

        Node currI,currJ;

        Path tmp_path = new Path();

        for (int i=0;i<Nodes1.length;i++){
            for (int j=0;j<Nodes2.length;j++){
                currI = Nodes1[i];
                currJ = Nodes2[j];
                tmp_path = find_path(currI,currJ);

                if (tmp_path != null){
                    // Change the minimum if necessary
                    if (tmp_path.distance < min_dist) {
                        min_dist = tmp_path.distance;
                        toReturn[0] = currI;
                        toReturn[1] = currJ;
                    }
                }
            }

        }
        if (toReturn[0] == null || toReturn[1] == null) {
            return null;
        }
        else {
            return toReturn;
        }
    }

    /**
     * Given two arrays of nodes, this would return an array of two nodes,
     * one from each of the input arrays, that have the shortest path
     * distance between them.
     * 
     * @param Nodes1
     * @param Nodes2
     * @return Node[] holding two nodes, first from first, second from second.
     */
    private Node[] shortest_Nodes(final Set Nodes1, final Set Nodes2) { 
        Node[] toReturn = new Node[2];
        toReturn[0] = null;
        toReturn[1] = null;
        // Start the minimum as high as possible
        double min_dist = Double.POSITIVE_INFINITY;

        Node currI,currJ;

        Iterator i = Nodes1.iterator();
        Iterator j = Nodes2.iterator();

        Path tmp_path = new Path();
        while (i.hasNext()) {
            currI = (Node) i.next();
            while (j.hasNext()) {
                currJ = (Node) j.next();

                // Calculate distance between the two points
                tmp_path = find_path(currI,currJ);

                if (tmp_path != null){
                    // Change the minimum if necessary
                    if (tmp_path.distance < min_dist) {
                        min_dist = tmp_path.distance;
                        toReturn[0] = currI;
                        toReturn[1] = currJ;
                    }
                }
            }
            j = Nodes2.iterator();
        }
        if (toReturn[0] == null || toReturn[1] == null) {
            return null;
        }
        else {
            return toReturn;
        }
    }
}
