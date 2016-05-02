import java.util.*;
/**
 * Graph data structure
 * @author Ryan Fisher
 **/
public class Graph<E>
{
	TreeSet<E> vertices;
	HashMap<E, Vector<WeightedEdge>> list;

	/**
	 * Constructor for class Graph
	 **/
	public Graph() {
		vertices = new TreeSet<E>(); //May want to use different interface
		list = new HashMap<E, Vector<WeightedEdge>>(); //Need to choose different interface
	}

	/**
	 * Adds a vertex to the graph
	 *
	 * @param  v  object to be added as vertex
	 **/
	public void addVertex(E v) {
		//Add vertex v to Set of vertices
		if (!vertices.contains(v)) {
			vertices.add(v);
			list.put(v, new Vector<WeightedEdge>());
		}
	}
	
	/**
	 * Removes the specified vertex from the graph
	 *
	 * @param  v vertex to be removed from graph
	 **/
	public void removeVertex(E v) {
		//Remove vertex from set and edges that v connects to
		if (vertices.contains(v)) {
			vertices.remove(v); //remove vertex from TreeSet
			Vector<WeightedEdge> edges = list.get(v); //gets edges from that v connects to
			Iterator<WeightedEdge> iter = edges.iterator(); //instantiates iterator for edges
			Vector<E> verts = new Vector<E>(); //instantiates a vector to store vertices
			while (iter.hasNext()) verts.add(iter.next().getVertex()); //stores vertices that v connects to
			list.remove(v); //removes v from adjacency list
			boolean found;
			int count;
			while (!verts.isEmpty()) { 
				E vert = verts.remove(0); 
				edges = list.get(vert); //gets edges that vert connects to
				iter = edges.iterator();
				count = 0;
				found = false;
				while (!found) {
					if (iter.next().getVertex().equals(v)) {
						edges.remove(count);
						found = true;
					}
					++count;
				}
			}
		}
	}
	
	/**
	 * Adds an edge between two vertices with defined weight.
	 * 
	 * @param  v1  first vertex of edge
	 * @param  v2  second vertex of edge
	 * @param weight weight of edge
	 * @throws IllegalArgumentException  if vertices are not in graph
	 **/
	public void addEdge(E v1, E v2, double weight) throws IllegalArgumentException {
		if (list.containsKey(v1) && list.containsKey(v2)) {
			WeightedEdge edge1 = new WeightedEdge(v2, weight);
			WeightedEdge edge2 = new WeightedEdge(v1, weight);
			Vector<WeightedEdge> edges1 = list.get(v1);
			Vector<WeightedEdge> edges2 = list.get(v2);
			Iterator<WeightedEdge> iter = edges2.iterator();
			boolean edgesSet = false;
			WeightedEdge edge;
			int count = 0;
			while (iter.hasNext() && !edgesSet) {
				edge = iter.next();
				if (edge.getVertex().equals(v1)) {
					edge2 = edges2.get(count);
					Iterator<WeightedEdge> iter2 = edges1.iterator();
					count = 0;
					while (iter2.hasNext() && !edgesSet) {
						edge = iter2.next();
						if (edge.getVertex().equals(v2)) {
							edge1 = edges1.get(count);
							edge1.setWeight(weight);
							edge2.setWeight(weight);
							edgesSet = true;
						}
						count++;
					}
				}
				count++;
			}
			if (!edgesSet) {
				edges1.add(new WeightedEdge(v2, weight));
				edges2.add(new WeightedEdge(v1, weight));
			}
		}
		else throw new IllegalArgumentException();
	}

	/**
	 * Removes edge between two specified vertices
	 *
	 * @param v1 first vertex
	 * @param v2 second vertex
	 * @throws IllegalArgumentException if either of specified vertices are not in graph
	 **/
	public void removeEdge(E v1, E v2) throws IllegalArgumentException {
		if (list.containsKey(v1) && list.containsKey(v2)) {
			Vector<WeightedEdge> edges = list.get(v1);
			Iterator<WeightedEdge> iter = edges.iterator();
			boolean found = false;
			int count = 0;
			while (iter.hasNext() && !found) {
				if (iter.next().getVertex().equals(v2)) {
					edges.remove(count);
					found = true;
				}
				++count;
			}
			edges = list.get(v2);
			iter = edges.iterator();
			found = false;
			count = 0;
			while (iter.hasNext() && !found) {
				if (iter.next().getVertex().equals(v1)) {
					edges.remove(count);
					found = true;
				}
				++count;
			}
		}
		else throw new IllegalArgumentException();
	}
	
	/**
	 * Returns a collection of vertices in the graph
	 *
	 * @return the vertices in the graph
	 **/
	public Collection<E> getVertices() {
		Collection<E> newVertices = new TreeSet<E>();
		Iterator<E> iter = vertices.iterator();
		while (iter.hasNext()) newVertices.add(iter.next());
		return newVertices;
	}

	/**
	 * Returns all edges adjacent to a specific vertex
	 *
	 * @param  v  a vertex in the graph
	 * @return  the edges adjacent to specified vertex
	 **/
	public Collection<WeightedEdge> getEdges(E v) {
		Collection<WeightedEdge> edges = new Vector<WeightedEdge>();
		Iterator<WeightedEdge> iter = list.get(v).iterator();
		while (iter.hasNext()) edges.add(iter.next());
		return edges;
	}

	/**
	 * Returns the weight of the specified edge
	 *
	 * @param  v1  first vertex in edge
	 * @param  v2  second vertex in edge
	 * @return weight of edge, Double.POSITIVE_INFINITY if no edge
	 * @throws IllegalArgumentException if one of vertices is not in graph
	 **/
	public double getEdgeWeight(E v1, E v2) throws IllegalArgumentException {
		double weight = Double.POSITIVE_INFINITY;
		if (list.containsKey(v1) && list.containsKey(v2)) {
			Iterator<WeightedEdge> iter = list.get(v1).iterator();
			boolean found = false;
			WeightedEdge edge;
			while (iter.hasNext() && !found) {
				edge = iter.next();
				if (edge.getVertex().equals(v2)) {
					found = true;
					weight = edge.getWeight();
				}
			}
		}
		else throw new IllegalArgumentException();

		return weight;
	}

	/**
	 * Returns the shortest unweighted path from a start vertex to an ending vertex.
	 * An empty list returned would indicate that no such path exists.
	 *
	 * @param  s  the start vertex
	 * @param  t  the terminal vertex
	 * @return a LinkedList of the vertices in the path, list empty if no path exists
	 **/
	public LinkedList<E> shortestUnweightedPath(E s, E t) {
		return shortestPath(this.shortestUnweightedPathTree(s), s, t);
	}

	/**
	 * Returns the shortest weighted path from a start vertex to and ending vertex.
	 * An empty list returned would indicate that no such path exists.
	 *
	 * @param  s  the start vertex
	 * @param  t  the terminal vertex
	 * @return a LinkedList of the vertices in the path, list empty if no path exists
	 **/
	public LinkedList<E> shortestWeightedPath(E s, E t) {
		return shortestPath(this.shortestWeightedPathTree(s), s, t);
	}

	private LinkedList<E> shortestPath(Graph<E> tree, E s, E t) {
		HashMap<E, Boolean> visited = new HashMap<E, Boolean>();
		Iterator<E> iter = vertices.iterator();
		Stack<E> edges = new Stack<E>();

		while (iter.hasNext()) {
			E v = iter.next();
			visited.put(v, false);
		}

		visited.put(s, true);
		edges.push(s);
		E v;
		boolean found;
		Iterator<WeightedEdge> iterEdges;
		while (!edges.empty() && !edges.peek().equals(t)) {
			iterEdges = tree.getEdges(edges.peek()).iterator();
			found = false;
			while (iterEdges.hasNext() && !found) {
				v = iterEdges.next().getVertex();
				if (!visited.get(v)) {
					visited.put(v, true);
					edges.push(v);
					found = true;
				}
			}
			if (!found) edges.pop();
		}

		LinkedList<E> path = new LinkedList<E>();
		if (!edges.empty()) {
			while (!edges.empty()) path.add(edges.remove(0));
		}
			
		return path;

	}

	/**
	 * Returns the shortest unweighted path tree.
	 *
	 * @param  s  a vertex in the graph
	 * @return the resulting tree
	 **/
	public Graph<E> shortestUnweightedPathTree(E s) {
		Iterator<E> iter = vertices.iterator();
		HashMap<E, Boolean> visited = new HashMap<E, Boolean>();
		Graph<E> tree = new Graph<E>();
		LinkedList<WeightedEdge> edges = new LinkedList<WeightedEdge>();
		
		while (iter.hasNext()) {
			E v = iter.next();
			tree.addVertex(v);
			visited.put(v, false);
		}

		explore(s, visited, edges);
		WeightedEdge edge;
		E vertex;
		while (!edges.isEmpty()) {
			edge = edges.removeFirst();
			vertex = edge.getVertex();
			if (!visited.get(vertex)) {
				tree.addEdge(edge.getStart(), vertex, edge.getWeight());
				explore(vertex, visited, edges);
			}
		}

		return tree;
	}

	private void explore(E a, HashMap<E, Boolean> visited, Collection<WeightedEdge> edges) {
		visited.put(a, true);
		Iterator<WeightedEdge> iter = list.get(a).iterator();
		WeightedEdge edge;
		while (iter.hasNext()) {
			edge = iter.next();
			if (!visited.get(edge.getVertex())) {
				edge.setStart(a);
				edges.add(edge);
			}
		}
	}

	/**
	 * Returns the shortest weighted path tree.
	 *
	 * @param  s  a vertex in the graph
	 * @return the resulting tree
	 **/
	public Graph<E> shortestWeightedPathTree(E s) {
		Iterator<E> iter = vertices.iterator();
		HashMap<E, Boolean> visited = new HashMap<E, Boolean>();
		Graph<E> tree = new Graph<E>();
		PriorityQueue<DistanceEdge> edges = new PriorityQueue<DistanceEdge>();
		
		while (iter.hasNext()) {
			E v = iter.next();
			tree.addVertex(v);
			visited.put(v, false);
		}

		exploreDist(s, visited, edges, 0);
		DistanceEdge edge;
		E vertex;
		double distance;
		while (!edges.isEmpty()) {
			edge = edges.poll();
			vertex = edge.getVertex();
			if (!visited.get(vertex)) {
				tree.addEdge(edge.getStart(), vertex, edge.getWeight());
				exploreDist(vertex, visited, edges, edge.getDistance());
			}
		}

		return tree;
	}

	private void exploreDist(E a, HashMap<E, Boolean> visited, Collection<DistanceEdge> edges, double distance) {
		visited.put(a, true);
		Iterator<WeightedEdge> iter = list.get(a).iterator();
		DistanceEdge edge;
		WeightedEdge e;
		while (iter.hasNext()) {
			e = iter.next();
			edge = new DistanceEdge(e.getVertex(), e.getWeight(), distance + e.getWeight());
			if (!visited.get(edge.getVertex())) {
				edge.setStart(a);
				edges.add(edge);
			}
		}
	}

	/**
	 * Returns the minimum spanning tree
	 *
	 * @return the minimum spanning tree
	 **/
	public Graph<E> minimumSpanningTree() {
		Iterator<E> iter = vertices.iterator();
		HashMap<E, Boolean> visited = new HashMap<E, Boolean>();
		Graph<E> tree = new Graph<E>();
		PriorityQueue<WeightedEdge> edges = new PriorityQueue<WeightedEdge>();
		
		E v = null;
		while (iter.hasNext()) {
			v = iter.next();
			visited.put(v, false);
		}

		exploreMin(v, visited, edges);
		tree.addVertex(v);
		WeightedEdge edge;
		E vertex;
		while (!edges.isEmpty()) {
			edge = edges.poll();
			vertex = edge.getVertex();
			if (!visited.get(vertex)) {
				tree.addVertex(vertex);
				tree.addEdge(edge.getStart(), vertex, edge.getWeight());
				exploreMin(vertex, visited, edges);
			}
		}

		return tree;
	}

	private void exploreMin(E a, HashMap<E, Boolean> visited, Collection<WeightedEdge> edges) {
		visited.put(a, true);
		Iterator<WeightedEdge> iter = list.get(a).iterator();
		WeightedEdge edge;
		WeightedEdge e;
		while (iter.hasNext()) {
			e = iter.next();
			edge = new WeightedEdge(e.getVertex(), e.getWeight());
			if (!visited.get(edge.getVertex())) {
				edge.setStart(a);
				edges.add(edge);
			}
		}
	}	
	
	/**
	 * WeightedEdge class describes an edge in the graph.
	 **/
	public class WeightedEdge implements Comparable
	{
		E vertex;
		E start;  //Only used for non-basic graph algorithm purposes
		double weight;
		
		/**
		 * Constructor for weighted edge
		 **/
		public WeightedEdge() {
			weight = 0;
		}


		/**
		 * Constructor for weighted edge takes two parameters
		 *
		 * @param  v  a vertex
		 * @param  w  weight of edge
		 **/
		public WeightedEdge(E v, double w) {
			vertex = v;
			weight = w;
		}
		
		/**
		 * Sets weight of edge
		 *
		 * @param  w  weight of edge
		 **/
		public void setWeight(double w) {
			weight = w;
		}
	
		/**
		 * Gets weight of the edge
		 *
		 * @return  weight of the edge
		 **/
		public double getWeight() {
			return weight;
		}

		/**
		 * Sets the vertex of the edge
		 *
		 * @param  v  vertex of the edge
		 **/
		public void setVertex(E v) {
			vertex = v;
		}

		/**
		 * Gets the vertex of the edge
		 *
		 * @return vertex of the edge
		 **/
		public E getVertex() {
			return vertex;
		}

		/**
		 * Sets the start vertex of the edge
		 *
		 * @param  v  the start vertex
		 **/
		public void setStart(E v) {
			start = v;
		}

		/**
		 * Gets the start vertex of the edge
		 *
		 * @return the start vertex
		 **/
		public E getStart() {
			return start;
		}

		/**
		 * Compares the WeightedEdge with the specified object for order. 
		 * Returns a negative integer, zero, or a positive integer as 
		 * this object is less than, equal to, or greater than the 
		 * specified object.
		 *
		 * @param  o1  object to compare
		 * @return -1, 0, or 1 if object is less than, equal to, or greater than
		 **/
		public int compareTo(Object o1) {
			WeightedEdge v = (WeightedEdge)o1;
			int retVal;

			if (this.getWeight() < v.getWeight()) retVal = -1;
			else if (this.getWeight() > v.getWeight()) retVal = 1;
			else retVal = 0;
		
			return retVal;
		}

		/**
		 * Compares WeightedEdge with a specified WeightedEdge for equality.
		 *
		 * @param  e  edge to compare
		 * @return true if equal
		 **/
		public boolean equals(WeightedEdge e) {
			boolean retVal;
			if (e.getWeight() == weight && e.getVertex() == vertex) retVal = true;
			else retVal = false;
			return retVal;
		}
	} //End inner class WeightedEdge


	/**
	 * DistanceEdge is used with Graph algorithms that need to take into account
	 * weights over multiple edges
	 **/
	public class DistanceEdge extends WeightedEdge
	{
		double distance; //total of weight over edges from start

		public DistanceEdge(E v, double w, double d) {
			super(v, w);
			distance = d;
		}
		/**
		 * Sets distance from original vertex to current
		 *
		 * @param  d  the distance to set
		 **/
		public void setDistance(double d) {
			distance = d;
		}

		/**
		 * Gets the distance of the current edge
		 *
		 * @return  distance of the edge
		 **/
		public double getDistance() {
			return distance;
		}

		/**
		 * Compares the DistanceEdge with the specified object (another DistanceEdge) for order. 
		 * Returns a negative integer, zero, or a positive integer as 
		 * this object is less than, equal to, or greater than the 
		 * specified object.
		 *
		 * @param  o1  object to compare
		 * @return -1, 0, or 1 if object is less than, equal to, or greater than
		 **/
		public int compareTo(Object o1) {
			DistanceEdge v = (DistanceEdge)o1;
			int retVal;

			if (distance < v.getDistance()) retVal = -1;
			else if (distance > v.getDistance()) retVal = 1;
			else retVal = 0;
		
			return retVal;
		}
	}

} //End class Graph

