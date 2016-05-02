package routing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import network.Address;
import network.INode;
import network.Node;

/**
 * An implementation of Dijkstra's shortest path algorithm. It computes the 
 * shortest path (in distance) to nodes in a RoutesMap.  The output of the 
 * algorithm is the shortest distance from the start T to every other T, and the
 * shortest path from the start T to every other.
 * <p>
 * Upon calling
 * {@link #execute(T, T)}, 
 * the results of the algorithm are made available by calling
 * {@link #getPredecessor(T)}
 * and 
 * {@link #getShortestDistance(T)}
 * and
 * {@link #getShortestPath(T)}
 * and
 * {@link #getNextHop(source, dest)}
 * 
 * @see #execute(T source, T dest)
 * 
 * @author Alex Maskovyak modified from Renaud Waldura
 */
public class DijkstraEngine<T extends Comparable<T>> {
    
	/** Infinity value for distances. */
    public static final int INFINITE_DISTANCE = Integer.MAX_VALUE;

    /** Some value to initialize the priority queue with. */
    private static final int INITIAL_CAPAT = 8;
    
    /**
     * Comparator which orders items according to shortest link cost in 
     * ascending order. If two items have equivalent cost, the comparables 
     * themselves are compared.
     */
    private final Comparator<T> shortestDistanceComparator = 
    	new Comparator<T>() {
            public int compare(T left, T right) {
                // note that this trick doesn't work for huge distances, close to Integer.MAX_VALUE
                int result = getShortestDistance(left) - getShortestDistance(right);
                
                return (result == 0) ? left.compareTo(right) : result;
            }
        };
    
    /** graph. */
    private final IRoutesMap<T> map;
    
    /** working set of items, kept ordered by shortest distance. */
    private final PriorityQueue<T> unsettledNodes = 
    	new PriorityQueue<T>(INITIAL_CAPAT, shortestDistanceComparator);
    
    /** set of cities for which the shortest distance to the source has been 
     * found. */
    private final Set<T> settledNodes = new HashSet<T>();
    
    /** currently known shortest distance for all cities. */
    private final Map<T, Integer> shortestDistances = new HashMap<T, Integer>();

    /** predecessors list: maps a T to its predecessor in the spanning tree of
     * shortest paths. */
    private final Map<T, T> predecessors = new HashMap<T, T>();
    
    /** Default constructor. */
    public DijkstraEngine(IRoutesMap<T> map) {
        this.map = map;
    }

    /**
     * Initialize all data structures used by the algorithm.
     * 
     * @param start the source node
     */
    private void init(T start) {
        settledNodes.clear();
        unsettledNodes.clear();
        
        shortestDistances.clear();
        predecessors.clear();
        
        // add source
        setShortestDistance(start, 0);
        unsettledNodes.add(start);
    }
    
    /**
     * Run Dijkstra's shortest path algorithm on the map.
     * The results of the algorithm are available through
     * {@link #getPredecessor(T)}
     * and 
     * {@link #getShortestDistance(T)}
     * upon completion of this method.
     * 
     * @param start the starting T
     * @param destination the destination T. If this argument is <code>null</code>, the algorithm is
     * run on the entire graph, instead of being stopped as soon as the destination is reached.
     */
    public void execute(T start, T destination) {
        init(start);
        
        // the current node
        T u;
        
        // extract the node with the shortest distance
        while ((u = unsettledNodes.poll()) != null) {
            assert !isSettled(u);
            
            // destination reached, stop
            if (u.equals(destination)) break;
            
            settledNodes.add(u);
            
            relaxNeighbors(u);
        }
    }

    /**
	 * Compute new shortest distance for neighboring nodes and update if a shorter
	 * distance is found.
	 * 
	 * @param u the node
	 */
    private void relaxNeighbors(T u) {
    	for (T v : map.getDestinations(u)) {
            // skip node already settled
            if (isSettled(v)) continue;
            
            int shortDist = getShortestDistance(u) + map.getCost(u, v);
            
            if (shortDist < getShortestDistance(v)) {
            	// assign new shortest distance and mark unsettled
                setShortestDistance(v, shortDist);
                                
                // assign predecessor in shortest path
                setPredecessor(v, u);
            }  
        }        
    }

	/**
	 * Test a node.
	 * 
     * @param v the node to consider
     * 
     * @return whether the node is settled, ie. its shortest distance
     * has been found.
     */
    private boolean isSettled(T v) {
        return settledNodes.contains(v);
    }

    /**
     * @return the shortest distance from the source to the given node, or
     * {@link DijkstraEngine#INFINITE_DISTANCE} if there is no route to the destination.
     */    
    public int getShortestDistance(T node) {
        Integer d = shortestDistances.get(node);
        return (d == null) ? INFINITE_DISTANCE : d;
    }
    
	/**
	 * Set the new shortest distance for the given node,
	 * and re-balance the queue according to new shortest distances.
	 * 
	 * @param T the node to set
	 * @param distance new shortest distance value
	 */        
    private void setShortestDistance(T node, int distance) {
        /*
         * This crucial step ensures no duplicates are created in the queue
         * when an existing unsettled node is updated with a new shortest 
         * distance.
         * 
         * Note: this operation takes linear time. If performance is a concern,
         * consider using a TreeSet instead instead of a PriorityQueue. 
         * TreeSet.remove() performs in logarithmic time, but the PriorityQueue
         * is simpler. (An earlier version of this class used a TreeSet.)
         */
        unsettledNodes.remove(node);

        /*
         * Update the shortest distance.
         */
        shortestDistances.put(node, distance);
        
		/*
		 * Re-balance the queue according to the new shortest distance found
		 * (see the comparator the queue was initialized with).
		 */
        unsettledNodes.add(node);
    }
    
    /**
     * Get the shortest path to a destination.
     * @param source from which to begin.
     * @param destination to which to go.
     * @return shortest path to destination.
     */
    public List<T> getShortestPath(T source, T destination) {
    	 List<T> path = new ArrayList<T>();
    	 
    	 for(T node = destination; node != null; node = getPredecessor(node)) {
    		 path.add( node );
    	 }

    	 if( !path.contains( source )) { path.clear(); }	// not viable
    	 
    	 Collections.reverse( path);
    	 return path;
    }
    
    /**
     * @return the node leading to the given node on the shortest path, or
     * <code>null</code> if there is no route to the destination.
     */
    public T getPredecessor(T node) {
        return predecessors.get(node);
    }
    
    /**
     * Obtains the next hop from the source node to the destination node.
     * @param dest node to get to.
     * @return next hop.
     */
    public T getNextHop(T source, T dest) {
    	if( source == dest ) { return dest; }	// shortcut
    	
    	List<T> path = getShortestPath( source, dest );
    	
    	int pathSize = path.size();
    	switch( pathSize ) {
    		case 0 : return null;
    		case 1 : return path.get( 0 );
    		default : return path.get( path.indexOf( source ) + 1 );
    	}
    }
    
    /**
     * Sets a predecessor.
     * @param a node who has a predecessor.
     * @param b node a's predecessor.
     */
    private void setPredecessor(T a, T b) {
        predecessors.put(a, b);
    }

    public static void main(String... args) {
    	Node a1 = new Node(new Address(1));
    	Node b2 = new Node(new Address(2));
    	Node c3 = new Node(new Address(3));
    	Node d4 = new Node(new Address(4));
    	Node e5 = new Node(new Address(5));
    	
    	
    	IRoutesMap<INode> map = new SparseRoutesMap<INode>();
    	DijkstraEngine<INode> engine = new DijkstraEngine<INode>(map);
    	
    	map.addDirectRoute(a1, b2, 5);
    	map.addDirectRoute(b2, c3, 4);
    	map.addDirectRoute(c3, b2, 2);
    	map.addDirectRoute(c3, d4, 7);
    	map.addDirectRoute(d4, e5, 5);
    	map.addDirectRoute(d4, a1, 3);
    	map.addDirectRoute(e5, c3, 5);
    	///map.addDirectRoute(start, end, cost)
    	
    	Node start = d4;
    	Node end = b2;
    	engine.execute(start, end);
    	Node current;
    	
    	List<INode> nodes = engine.getShortestPath(start, end);
    	for( INode node : nodes ) {
    		System.out.printf( "%s-", node.getAddress() );
    	}
    	
    	
    	System.out.printf( "\nnext: %s\n", engine.getNextHop(start, end).getAddress() );
    	
    	map.removeDirectRoute(d4, a1);
    	
    	engine.execute(start, end);
    	
    	nodes = engine.getShortestPath(start, end);
    	for( INode node : nodes ) {
    		System.out.printf( "%s-", node.getAddress() );
    	}
    	System.out.printf( "\nnext: %s\n", engine.getNextHop(start, end).getAddress() );
    	
    	map.remove( c3 );
    	
    	engine.execute(start, end);
    	
    	nodes = engine.getShortestPath(start, end);
    	for( INode node : nodes ) {
    		System.out.printf( "%s-", node.getAddress() );
    	}
    	
    	System.out.printf( "\nnext: %s\n", engine.getNextHop(start, end).getAddress() );
    	
    	/*System.out.println( a.getAddress() );
    	System.out.println( b.getAddress() );
    	System.out.println( c.getAddress() );

    	System.out.println( d.getAddress() );
    	System.out.println( engine.getPredecessor(d).getAddress() );
    	System.out.println( engine.getShortestDistance(d));*/
    }
}

