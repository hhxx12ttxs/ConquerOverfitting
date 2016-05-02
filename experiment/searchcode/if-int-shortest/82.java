package model;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;

/**
 * A representation of a Multi-Directional Graph.
 * @author Daroczi Krisztian-Zoltan
 * @version 1.0
 */
public class MultiDirectedGraph{
	/**
	 * Holds the {@link Vertex}s
	 */
	protected Vector<Vertex> vertices;
	
	/**
	 * Stores the number of edges in the graph
	 */
	protected int edgeCount;
	
	/**
	 * Used when calculating shortest paths in the graph when it has negative edges.
	 * Bellman-Ford distance Map of {@link Vertex}s as keys and {@link Float}s as values.
	 * Each entry holds a vertex, and the distance to get from the starting vertex to this one.
	 */
	protected Map<Vertex, Float> bfdistance;
	
	/**
	 * Used when calculating shortest paths in the graph when it has negative cost edges.
	 * Bellman-Ford predecessor Map of {@link Vertex}s as keys and values.
	 * Each entry holds a vertex, and the parent of it, to get from the starting vertex to this one.
	 */
	protected Map<Vertex, Vertex> bfpredecessor;
	
	/**
	 * Used when calculating shortest paths in the graph when it has only positive cost edges.
	 * Dijkstra distance Map of {@link Vertex}s as keys and {@link Float}s as values.
	 * Each entry holds a vertex, and the distance to get from the starting vertex to this one.
	 */	
	protected Map<Vertex, Float> djdistance;

	/**
	 * Used when calculating shortest paths in the graph when it has negative cost edges.
	 * Dijkstra predecessor Map of {@link Vertex}s as keys and values.
	 * Each entry holds a vertex, and the parent of it, to get from the starting vertex to this one.
	 */
	protected Map<Vertex, Vertex> djpredecessor;
	
	protected int backgroundType;	// 0=>color, 1=>image
	protected String background;
	
	
	
	
	/**
	 * Creates an empty graph
	 * @since Version 1.0
	 */
	public MultiDirectedGraph(){
		this.edgeCount = 0;
		this.vertices = new Vector<Vertex>();
		this.background = "#D4F59F";
	}
	
	public MultiDirectedGraph(MultiDirectedGraph g){
		this.background = g.background;
		this.vertices = new Vector<Vertex>();
		for (int i = 0; i < g.getVertexCount(); i++){
			Vertex v = g.getVertexByIndex(i);
			if (v instanceof Circle){
				float rad = ((Circle) v).getRadius();
				Vertex nv = new Circle(v.id, v.text, v.color, v.highlightColor, v.left, v.top, v.priority, rad);
				try {
					this.addVertex(nv);
				} catch (MyException e) {
					e.printStackTrace();
				}
			}else if (v instanceof Ellipse){
				int w = ((Ellipse) v).getWidth();
				int h = ((Ellipse) v).getHeight();
				Vertex nv = new Ellipse(v.id, v.text, v.color, v.highlightColor, v.left, v.top, v.priority, w, h);
				try {
					this.addVertex(nv);
				} catch (MyException e) {
					e.printStackTrace();
				}
			}else if (v instanceof Rectangle){
				int w = ((Rectangle) v).getWidth();
				int h = ((Rectangle) v).getHeight();
				Vertex nv = new Rectangle(v.id, v.text, v.color, v.highlightColor, v.left, v.top, v.priority, w, h);
				try {
					this.addVertex(nv);
				} catch (MyException e) {
					e.printStackTrace();
				}
			}
		}
		
		for (int i = 0; i < this.getVertexCount(); i++){
			Vertex v1_g = g.getVertexByIndex(i);
			Vertex v1 = this.getVertexById(v1_g.getId());
			
			for (int j = 0; j < v1_g.getOutDegree(); j++){
				Edge e = v1_g.out.get(j);
				Vertex v2_g = e.getEnd();
				Vertex v2 = this.getVertexById(v2_g.getId());
				try {
					this.addEdge(v1, v2, e.color, e.highlightColor, e.cost, e.label, e.thickness, e.getPoints(), e.getRealDistances());
				} catch (MyException e1) {
					e1.printStackTrace();
				}
			}
		}
		
		this.edgeCount = g.edgeCount;
	}
	
	/**
	 * Used to create and add an Edge <b>without adding the two vertices</b> that it links.
	 * The vertices v1 and v2 are set not to be isolated
	 * @param v1 the starting vertex
	 * @param v2 the destination vertex
	 * @param color the edge color
	 * @param highLightColor the edge highlight color
	 * @param cost the edge cost
	 * @param thickness the edge thickness
	 * @param points the edge custom drawing points
	 * @throws MyException <br>
	 * - if v1 or v2 is <b>null</b> <br>
	 * - if v1 or v2 is not in the graph
	 * @since Version 1.0
	 */
	public void addEdge(Vertex v1, Vertex v2, Color color, Color highLightColor, float cost, String label, int thickness, Vector<Vector2D> points, 
			boolean useRealDistances) throws MyException{
		if ((v1 != null) && (v2 != null)){
			if ((this.vertexBelongs(v1)) || (this.vertexBelongs(v2))){
				Edge e = new Edge(cost, color, highLightColor, v1, v2,thickness, points, useRealDistances, 100);
				e.id = this.edgeCount;
				e.start.addOutNeighbor(e);
				e.end.addInNeighbor(e);
				e.label = label;
				v1.setIsolated(false);
				v2.setIsolated(false);
				this.edgeCount++;
			}else
				throw new MyException("Can't add edge to graph: Vertices are not in the graph! [use addEdgeWithVertices instead]!");
		}else
			throw new MyException("Can't add edge to graph: Vertices must be set [not null]!");
	}
	
	/**
	 * Used to create and add an Edge <b>and the two vertices</b> that it links.
	 * The vertices v1 and v2 are set not to be isolated
	 * @param v1 the starting vertex
	 * @param v2 the destination vertex
	 * @param color the edge color
	 * @param highLightColor the edge highlight color
	 * @param cost the edge cost
	 * @param thickness the edge thickness
	 * @param points the edge custom drawing points
	 * @throws MyException if v1 or v2 is <b>null</b>
	 * @since Version 1.0
	 */
	public void addEdgeWithVertices(Vertex v1, Vertex v2, Color color, Color highLightColor, float cost, int thickness, Vector<Vector2D> points, 
			boolean useRealDistances) throws MyException{
		if ((v1 != null) && (v2 != null)){
			Edge e = new Edge(cost, color, highLightColor, v1, v2,thickness, points, useRealDistances, 100);
			e.id = this.edgeCount;
			vertices.add(v1);
			vertices.add(v2);
			e.start.addOutNeighbor(e);
			e.end.addInNeighbor(e);
			this.edgeCount++;
		}else
			throw new MyException("Can't add edge to graph: Vertices must be set [not null]!");
	}
	
	/**
	 * Used to add an already created edge to link two vertices again.
	 * @param e an edge
	 * @throws MyException if e is <b>null</b>
	 * @since Version 1.0
	 */
	public void addEdge(Edge e) throws MyException{
		if (e.equals(null))
			throw new MyException("Can't add edge to graph: Edge must be set [not null]!");
		e.id = this.edgeCount;
		e.start.addOutNeighbor(e);
		e.end.addInNeighbor(e);
		this.edgeCount++;
	}
	
	/**
	 * Adds a new vertex to the graph, and sets its isolation level.
	 * @param v the vertex to add
	 * @throws MyException if the parameters' id is already an id of any vertex in the graph
	 * @since Version 1.0
	 */
	public void addVertex(Vertex v) throws MyException{
		if ((v.in.size() == 0) && (v.out.size() == 0))
			v.setIsolated(true);
		else
			v.setIsolated(false);
		
		//check for duplicate id
		for (int i = 0; i < this.vertices.size(); i++)
			if (vertices.get(i).id == v.id)
				throw new MyException("Can't add vertex with dublicate id!");
		this.vertices.add(v);
	}
	
	/**
	 * Adds a new vertex to the graph as a {@link Circle}
	 * @throws MyException if the parameters' id is already an id of any vertex in the graph
	 * @see Circle constructor
	 * @since Version 1.0
	 */
	public void addVertexCircle(int id, String text, Color color, Color highlightColor, int left, int top, int priority, float radius) throws MyException{
		Vertex c = new Circle(id, text, color, highlightColor, left, top, priority, radius);
		this.addVertex(c);
	}
	
	/**
	 * Adds a new vertex to the graph as a {@link Rectangle}
	 * @throws MyException if the parameters' id is already an id of any vertex in the graph 
	 * @see Rectangle constructor
	 * @since Version 1.0
	 */
	public void addVertexRectangle(int id, String text, Color color, Color highlightColor, int left, int top, int priority, int width, int height) throws MyException{
		Vertex r = new Rectangle(id, text, color, highlightColor, left, top, priority, width, height);
		this.addVertex(r);
	}
	
	/**
	 * Adds a new vertex to the graph as a {@link Ellipse}
	 * @throws MyException if the parameters' id is already an id of any vertex in the graph
	 * @see Ellipse constructor
	 * @since Version 1.0
	 */	
	public void addVertexEllipse(int id, String text, Color color, Color highlightColor, int left, int top, int priority, int width, int height) throws MyException{
		Vertex e = new Ellipse(id, text, color, highlightColor, left, top, priority, width, height);
		this.addVertex(e);
	}
	
	/**
	 * @return the number of edges in the graph
	 * @since Version 1.0
	 */
	public int getEdgeCount(){
		return edgeCount;
	}
	
	/**
	 * Removes an edge from the graph. Vertices are converted to isolated, if needed, after removing the edge.
	 * @param e the edge to remove
	 * @throws MyException if e doesn't belong to the graph, or e is <b>null</b>.
	 * @since Version 1.0
	 */
	public void removeEdge(Edge e) throws MyException{
		if (!(this.edgeBelongs(e)))
			throw new MyException("Can't remove edge from graph: Edge is not in the graph!");
		Vertex start = e.start;
		Vertex end = e.end;
		//start.removeOutEdge(e);
		//boolean firstOk = start.out.removeElement(end);
		//boolean secondOk = end.in.removeElement(start);
		start.removeOutEdge(e);
		end.removeInEdge(e);
		this.edgeCount--;
	}
	
	/**
	 * Checks if an edge is part of the graph.
	 * @param e the edge to check
	 * @return true if <i>e</i> is an edge of the graph, false otherwise
	 * @since Version 1.0
	 */
	public boolean edgeBelongs(Edge e){
		for (int i = 0; i < this.vertices.size(); i++)
			if ((this.vertices.get(i).in.contains(e)) || (this.vertices.get(i).out.contains(e)))
				return true;
		return false;
	}
	
	/**
	 * Returns an edge which has the id the parameter id
	 * @param id the id of the edge
	 * @return the edge, or null if it was not found
	 * @since Version 1.0
	 */
	public Edge getEdgeById(int id){
		if (id < 0)
			return null;
		for (int i = 0; i < this.vertices.size(); i++){
			Vertex v = this.vertices.get(i);
			for (int j = 0; j < v.out.size(); j++){
				Edge e = v.out.get(j);
				if (e.id == id)
					return e;
			}
		}
		return null;
	}
	
	/**
	 * Removes a vertex (and all its edges) from the graph.
	 * @param v the vertex to remove
	 * @throws MyException if v is <b>null</b> or is not a vertex of the graph
	 * @since Version 1.0
	 */
	public void removeVertex(Vertex v) throws MyException{
		if ((v == null) || (!(this.vertexBelongs(v))))
			throw new MyException("Can't remove vertex from graph: Vertex is not in the graph!");
		//ki kell szedni az osszes edge-et, ami a vertex in es out vektoraban van
		//atnezzuk az in-t es out-ot es mindegyik bejovo edge-et le kell torolni
		
		while (v.in.size() > 0){
			Edge incoming = v.in.get(0);
			this.removeEdge(incoming);
		}
		
		while (v.out.size() > 0){
			Edge outgoing = v.out.get(0);
			this.removeEdge(outgoing);
		}
		//vegul a vertexet is le kell torloni
		if (this.vertices.removeElement(v) == false)
			throw new MyException("Can't remove vertex from graph [unkown error]!");
		this.reOrderIndicesAndPriorities();
	}
	
	/**
	 * Checks if a vertex is in the graph
	 * @param v the vertex to check
	 * @return true, if the vertex is in the graph, false otherwise
	 * @since Version 1.0
	 */
	public boolean vertexBelongs(Vertex v){
		if (v == null)
			return false;
		for (int i = 0; i < this.vertices.size(); i++)
			if (v.equals(this.vertices.get(i)))
				return true;
		return false;
		//return (this.vertices.contains(v));
	}
	
	/**
	 * Returns the number of vertices in the graph
	 * @return the number of vertices in the graph
	 * @since Version 1.0
	 */
	public int getVertexCount(){
		return this.vertices.size();
	}
	
	/**
	 * Returns the first occurrence of any vertex that has as text the parameter
	 * @param text the text to look for
	 * @return <b>null</b> if no vertex was found, or the vertex otherwise
	 * @since Version 1.0
	 */
	public Vertex getVertexByText(String text){
		for (int i = 0; i < this.vertices.size(); i++)
			if (this.vertices.get(i).text.equals(text))
				return this.vertices.get(i);
		return null;
	}
	
	/**
	 * Returns the vertex with the id <i>id</i>
	 * @param id the id to look for
	 * @return <b>null</b> if no vertex was found, or the vertex otherwise
	 * @since Version 1.0
	 */	
	public Vertex getVertexById(int id){
		for (int i = 0; i < this.vertices.size(); i++)
			if (this.vertices.get(i).id == id)
				return this.vertices.get(i);
		return null;
	}
	
	/**
	 * Returns the vertex with the index <i>idx</i>
	 * @param idx the index to return
	 * @return <b>null</b> if the parameter is greater than the number of vertices
	 * @since Version 1.0
	 */		
	public Vertex getVertexByIndex(int idx){
		if ((idx < this.vertices.size()) && (idx >= 0))
			return this.vertices.get(idx);
		else
			return null;
	}
	
	public int getBackgroundType() {
		return backgroundType;
	}

	public void setBackgroundType(int backgroundType) {
		this.backgroundType = backgroundType;
	}

	public String getBackground() {
		return background;
	}

	public void setBackground(String background) {
		this.background = background;
	}

	/**
	 * A private function to calculate the shortest path from a vertex to every other vertex in the graph. It uses <i>bfpredecessor</i> and <i>bfdistance</i>
	 * to store its results. This is used the graph has negative cost edges.
	 * @param s the source vertex
	 * @throws ShortestPathException<br>
	 * - if <i>bfpredecessor</i> or <i>bfdistance</i> are not empty<br>
	 * - if there is a negative cost cycle in the graph
	 * @since Version 1.0
	 */
	private void useBellmanFord(Vertex s) throws ShortestPathException{
		
		if (bfpredecessor != null)
			throw new ShortestPathException("Can't calculate shortest path [predecessor map must be empty when calling useBellmanFord()]!");
		if (bfdistance != null)
			throw new ShortestPathException("Can't calculate shortest path [distance map must be empty when calling useBellmanFord()]!");
		
		this.bfpredecessor = new HashMap<Vertex,Vertex>();
		this.bfdistance = new HashMap<Vertex, Float>();
		
		//initialization
		for (int i = 0; i < this.vertices.size(); i++){
			Vertex v = this.vertices.get(i);
			if (v.equals(s))
				bfdistance.put(v, 0.0f);
			else
				bfdistance.put(v, Float.POSITIVE_INFINITY);
			bfpredecessor.put(v, null);
		}
		
		//relaxation
		for (int k = 0; k < this.vertices.size(); k++)
		for (int i = 0; i < this.vertices.size(); i++){
			//bejarni mindegyik edge-t (eleg mindegyik vertex-nek az out neighbor-jait megnezni)
			Vertex current = this.vertices.get(i);
			for (int j = 0; j < current.out.size(); j++){
				Edge uv = current.out.get(j);
				if (!uv.getLabel().equals(""))	//exclude the edges with labels
					continue;
				Vertex u = uv.start;
				Vertex v = uv.end;
				if (bfdistance.get(u) + uv.cost < bfdistance.get(v)){
					bfdistance.put(v, bfdistance.get(u) + uv.cost);
					/*Vector<Vertex> pathSoFar = bfpredecessor.get(v);
					pathSoFar.add(u);
					*/
					bfpredecessor.put(v, u);
				}
			}
		}
		
		//check for negative cycle
		for (int i = 0; i < this.vertices.size(); i++){
			Vertex current = this.vertices.get(i);
			for (int j = 0; j < current.out.size(); j++){
				Edge uv = current.out.get(j);
				Vertex u = uv.start;
				Vertex v = uv.end;
				if (bfdistance.get(u) + uv.cost < bfdistance.get(v))
					throw new ShortestPathException("Can't calculate shortest path [the graph contains negative cycle]!");
			}
		}
	}
	
	/**
	 * A private function to calculate the shortest path from a vertex to every other vertex in the graph. It uses <i>djpredecessor</i> and <i>djdistance</i>
	 * to store its results. This is used the graph has only positive cost edges.
	 * @param s the source vertex
	 * @param useMarked set it to true to use the marked vertices in the shortest path
	 * @throws ShortestPathException if <i>djpredecessor</i> or <i>djdistance</i> are not empty<br>
	 * @since Version 1.0
	 */
	@SuppressWarnings("unchecked")
	private void useDijkstra(Vertex s, boolean useMarked) throws ShortestPathException{
		if (djpredecessor != null)
			throw new ShortestPathException("Can't calculate shortest path [predecessor map must be empty when calling useDijkstra()]!");
		if (djdistance != null)
			throw new ShortestPathException("Can't calculate shortest path [distance map must be empty when calling useDijkstra()]!");
		
		this.djpredecessor = new HashMap<Vertex, Vertex>();
		this.djdistance = new HashMap<Vertex, Float>();
		
		//initialization
		for (int i = 0; i < this.vertices.size(); i++){
			Vertex v = this.vertices.get(i);
			if (v.equals(s))
				djdistance.put(v, 0.0f);
			else
				djdistance.put(v, Float.POSITIVE_INFINITY);
			djpredecessor.put(v, null);
		}
		
		if (!useMarked){
			int p = 0;
			while (p < this.vertices.size()){
			//for (int i = 0; i < this.vertices.size(); i++)
				if (vertices.get(p).getMarked())
					vertices.remove(p);
				else
					p++;
			}
		}
		
		
		Vector<Vertex> q = new Vector<Vertex>();
		q = (Vector<Vertex>) this.vertices.clone();
		
		for (int i = 0; i < q.size(); i++){
			if (s.id == q.get(i).id){
				s = q.get(i);
				break;
			}
		}
		
		
		while (!q.isEmpty()){
			//get the smallest vertex with the smallest distance so far
			Vertex u = null;
			
			float minDist = Float.POSITIVE_INFINITY;
			//int counter = 0, idx = 0, index = 0;
			//meg kell keresni azt az u-t a q-bol, aminek a legkissebb a dist[]-je
			for (int i = 0; i < q.size(); i++){
				Vertex currentVertexFromQ = q.get(i);
				//if ((currentVertexFromQ.getMarked()) && (!useMarked))
				//	continue;
				float dist = djdistance.get(currentVertexFromQ);
				if ((dist <= minDist) && (dist != Float.POSITIVE_INFINITY)){
					minDist = djdistance.get(currentVertexFromQ);
					u = currentVertexFromQ;
				}
			}
			
			/*
			//check each VALUE from the distance Map
			for (Object obj : djdistance.values()){
				float val = (Float) obj;
				if (val < minDist){
					minDist = val;
					index = idx;
				}
				idx++;
			}
			//get the vertex from the distance map with the index-th index
			for (Object obj : djdistance.keySet()){
				if (index == counter){
					u = (Vertex) obj;
					break;
				}
				counter++;
			}
			
			Vertex vU = this.getVertexById(u.getId());
			Vertex qU = null;
			for (int i = 0; i < q.size(); i++){
				if (u.id == q.get(i).id){
					qU = q.get(i);
					break;
				}
			}
			*/
			if ((u == null) || (djdistance.get(u) == Float.POSITIVE_INFINITY))
				break;
			
			q.removeElement(u);
			
			for (int i = 0; i < u.out.size(); i++){
				Edge uv = u.out.get(i);
				Vertex v = uv.end;
				if (!uv.getLabel().equals(""))	//exclude the edges with labels
					continue;
				float alt = djdistance.get(u) + uv.cost;
				if (alt < djdistance.get(v)){
					djdistance.put(v, alt);
					djpredecessor.put(v, u);
				}
			}
		}
	}
	
	/**
	 * Checks for negative cost edges in the graph
	 * @return true, if there is at least one edge with negative cost, false otherwise
	 * @since Version 1.0
	 */
	private boolean hasNegativeCostEdge(){
		for (int i = 0; i < this.vertices.size(); i++){
			Vertex current = this.vertices.get(i);
			for (int j = 0; j < current.out.size(); j++){
				Edge uv = current.out.get(j);
				if (uv.cost < 0)
					return true;
			}
		}
		return false;
	}
	
	/**
	 * Function to calculate the shortest path between a pair of vertices in the graph. It is allowed to have negative cost edges, but not negative cost cycles.
	 * @param v1 the source vertex
	 * @param v2 the destination vertex
	 * @param useMarked set it to true to use the marked vertices in the shortest path
	 * @return {@link ShortestPathException} object with the results
	 * @throws ShortestPathException if any of <i>useDijkstra</i> or <i>useBellmanFord</i> return an exception
	 * @since Version 1.0
	 */
	public ShortestPath getShortestPath(Vertex v1, Vertex v2, boolean useMarked) throws ShortestPathException{
		//check for negative costs
		ShortestPath result = new ShortestPath();
		Vector <Vertex> path = new Vector<Vertex>();
		if (this.hasNegativeCostEdge() || useMarked){
			//clean the `containers'
			if (this.bfdistance != null){
				this.bfdistance.clear();
				this.bfdistance = null;
			}

			if (this.bfpredecessor != null){
				this.bfpredecessor.clear();
				this.bfpredecessor = null;
			}
			//use the Bellman-Ford algorithm in this case
			this.useBellmanFord(v1);
			//get the distance and path from v1->v2
			//valamiert atmasolja mashova, es mar nem lesz ugyanaz a `vertices'-ben, mint az elejen =>
			//=> ujra lekerem a memoriabol a v2-t, mert maskepp nem talalja meg az eredmenyben
			v2 = this.getVertexById(v2.getId());
			result.resultCost = bfdistance.get(v2);
			if (result.resultCost == Float.POSITIVE_INFINITY)
				return result;
			Vertex current = bfpredecessor.get(v2);
			while (bfpredecessor.get(current) != null){
				path.add(current);
				current = bfpredecessor.get(current);
			}
			
		} else {
			//clean the `containers'
			if (this.djdistance != null){
				this.djdistance.clear();
				this.djdistance = null;
			}

			if (this.djpredecessor != null){
				this.djpredecessor.clear();
				this.djpredecessor = null;
			}
			//use Dijkstra's algorithm in this case
			this.useDijkstra(v1, useMarked);
			//valamiert atmasolja mashova, es mar nem lesz ugyanaz a `vertices'-ben, mint az elejen =>
			//=> ujra lekerem a memoriabol a v2-t, mert maskepp nem talalja meg az eredmenyben
			v2 = this.getVertexById(v2.getId());
			result.resultCost = djdistance.get(v2);
		
		
			if (result.resultCost == Float.POSITIVE_INFINITY)
				return result;
			Vertex current = djpredecessor.get(v2);
			while (djpredecessor.get(current) != null){
				path.add(current);
				current = djpredecessor.get(current);
			}
		}
		result.resultPath.add(v1);
		
		for (int i = path.size() - 1; i >= 0; i--)
			result.resultPath.add(path.get(i));
		
		result.resultPath.add(v2);
		
		
		return result;
			
	}
	
	/**
	 * Checks if a vertex is accessible from another vertex.
	 * @param x destination vertex
	 * @param s source vertex
	 * @return true, if there is a path from <i>s</i> to <i>x</i>, false otherwise
	 * @since Version 1.0
	 */
	public boolean isAccessible(Vertex x, Vertex s){
		if (x.equals(s))
			return true;
		Stack <Vertex> st = new Stack<Vertex>();
		for (int i = 0; i < this.vertices.size(); i++)
			this.vertices.get(i).marked = false;
		s.marked = true;
		st.push(s);
		Vertex z = null;
		while (!st.isEmpty()){
			Vertex y = st.pop();
			for (int i = 0; i < y.out.size(); i++){
				z = y.out.get(i).end;
				if (z.marked == false){
					z.marked = true;
					st.push(z);
				}
				if (z.equals(x))
					return true;
			}
		}
		return false;
	}
	
	//returns the strongly connected component in which `v' is present
	
	
	/**
	 * Returns the strongly connected component of a vertex.
	 * @param v the vertex
	 * @return a vector of vertices(including <i>v</i>) that are accessible from <i>v</i>
	 * @see isAccessible(Vertex x, Vertex s)
	 * @since Version 1.0
	 */
	public Vector<Vertex> getSCC(Vertex v){
		Vector<Vertex> result = new Vector<Vertex>();
		//for each vertex, check if it is accessible from v, and vice versa
		for (int i = 0; i < this.vertices.size(); i++){
			if ((this.isAccessible(this.vertices.get(i), v)) && 
					(this.isAccessible(v, this.vertices.get(i)))
				)
				result.add(this.vertices.get(i));
		}
		return result;
	}
	
	/**
	 * Called upon garbage collection. It deletes the graph by removing each vertex(therefore removing each edge).
	 * @since Version 1.0
	 */
	public void finalize(){
		for (int i = 0; i < this.vertices.size(); i++){
			try {
				this.removeVertex(this.vertices.get(i));
			} catch (MyException e) {
				System.err.println("Can't delete the graph");
				e.printStackTrace();
			}
		}
		this.vertices.clear();
		try {
			super.finalize();
		} catch (Throwable e) {
			System.err.println("Can't delete the graph");
			e.printStackTrace();
		}
	}
	
	/**
	 * Returns the lowest cost {@link Edge} between the {@link Vertex}<i>start</i> and the {@link Vertex}<i>destination</i>
	 * @param start the starting vertex
	 * @param destination the destination vertex
	 * @return the lowest cost edge between <i>start</i> and <i>destination</i>, or <b>null</b> if no edge was found
	 * @since Version 1.0
	 */
	public Edge getMinCostPath(Vertex start, Vertex destination){
		float minCost = Float.POSITIVE_INFINITY;
		int idx = -1;
		for (int i = 0; i < start.out.size(); i++){
			if (start.out.get(i).getEnd().equals(destination) == false)
				continue;
			if (start.out.get(i).getCost() < minCost){
				minCost = start.out.get(i).getCost();
				idx  = i;
			}
		}
		if ((idx == -1) || (idx > start.out.size() - 1))
			return null;
		else
			return start.out.get(idx);
	}
	
	/**
	 * Method that `de-Highlights' every vertex(sets their highlight bit to false)
	 * @since Version 1.0
	 */
	public void deHighlightVertices(){
		for (int i = 0; i < this.vertices.size(); i++){
			this.vertices.get(i).setHighLight(false);
			this.vertices.get(i).setPathStarter(false);
			this.vertices.get(i).setPathEnder(false);
		}
	}
	
	/**
	 * Method that `de-Highlights' every edge(sets their highlight bit to false)
	 * @since Version 1.0
	 */
	public void deHighlightEdges(){
		for (int i = 0; i < this.vertices.size(); i++){
			Vertex v = this.vertices.get(i);
			for (int j = 0; j < v.out.size(); j++)
				v.out.get(j).setHighLight(false);
		}

	}
	
	/**
	 * Method to reorder vertex indicies and priorities after a vertex was removed
	 * @since Version 1.0
	 */
	public void reOrderIndicesAndPriorities(){
		for (int i = 0; i < this.vertices.size(); i++){
			Vertex v = vertices.get(i);
			v.id = i;
			v.priority = i;
		}
	}
	
}

