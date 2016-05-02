package de.deterministicarts.lib.functional.algo;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;

import de.deterministicarts.lib.functional.Chain;
import de.deterministicarts.lib.functional.Cursor;

public class ShortestPath {

	public static final class EdgeBuffer<A> {
		
		private Object[] nodes;
		private double[] distances;
		private int size;
		
		EdgeBuffer() {
			size = 0;
			nodes = new Object[16];
			distances = new double[16];
		}
		
		/**
		 * Adds a new egde. This method records, that the node 
		 * represented by {@code node} is reachable from the current
		 * position, and its distance is {@code distance}. The
		 * distance must not be negative. A value of <code>null</code>
		 * for {@code node} may be acceptable, but that depends on
		 * the tracking strategy used in the computation.
		 * 
		 * @param node		target node
		 * @param distance	distance
		 */
		
		public void add(A node, double distance) {
			
			if( distance < 0.0 ) throw new IllegalArgumentException();
			else {
				
				if( size == nodes.length ) {
					
					final int nsize = size + size / 2;
					final Object[] nnodes = new Object[nsize];
					final double[] ndistances = new double[nsize];
					
					System.arraycopy(nodes, 0, nnodes, 0, size);
					System.arraycopy(distances, 0, ndistances, 0, size);
					
					nodes = nnodes;
					distances = ndistances;
				}
				
				nodes[size] = node;
				distances[size] = distance;
				size += 1;
			}
		}
		
		void clear() {
			while( size > 0 ) {
				final int n = --size;
				nodes[n] = null;
				distances[n] = 0.0;
			}
		}
	}
	
	public static interface Scout<A> {
		
		/**
		 * Called to enumerate the edges leading out of a node. This
		 * method is called for a given node {@code node} in order to
		 * determine the edges, which lead out of that node. The method
		 * should fill the given {@code buffer}.
		 * 
		 * <p>Note, that the method must not store a reference to the
		 * buffer, as it is likely to be re-used by the caller.
		 * 
		 * @param node		the node to inspect
		 * @param buffer	buffer to be filled
		 */
		
		void listEdges(A node, EdgeBuffer<? super A> buffer);
	}
	
	public static class Path<A> {

		// This serial number is used internally in order to ensure,
		// that we yield nodes seen first before nodes seen later (as
		// long as they have the same distance). This allows us to
		// honour the ordering of the nodes as they were added by the
		// scout into the edge buffer in cases, where we have no more
		// important ordering criterion.
		
		private int serial;
		
		// Current length of the path from the start node to this object's
		// "segments" list's head
		
		private double distance;
		
		// The nodes, this path consists of, in reverse order, i.e., the
		// start node is the last node of this list, and the path's end
		// is at the front.
		
		private Chain<A> steps;
		
		// The node list as we hand it out to the client. Simply the reversed
		// chain of "steps". Is computed on-demand.
		
		private Chain<A> segments;
		
		Path(int serial, A head) {
			this.serial = serial;
			this.distance = 0;
			this.steps = Chain.of(head);
			this.segments = null;
		}
		
		Path(int serial, A head, double delta, Path<A> tail) {
			this.serial = serial;
			this.distance = tail.distance + delta;
			this.steps = tail.steps.prepend(head);
			this.segments = null;
		}
		
		boolean update(double delta, Path<A> tail) {
			final double ndist = tail.distance + delta;
			if( ndist < delta ) {
				distance = ndist;
				steps = tail.steps.prepend(steps.head());
				segments = null;
				return true;
			}
			return false;
		}
		
		public double distance() {
			return distance;
		}
		
		/**
		 * Reverse version of the chain returned by {@link #segments},
		 * i.e., this chain has the path's end node at the head, and
		 * the traversal start node at the end.
		 * 
		 * @return
		 */
		
		public Chain<A> mirroredSegments() {
			return segments;
		}
		
		/**
		 * Chain, containing the nodes in this path, with the traversal
		 * start node as first, and the path's end node as last element.
		 * 
		 * @return
		 */
		
		public Chain<A> segments() {
			if( segments == null ) segments = steps.reverse();
			return segments;
		}
	}
	
	private static final Comparator<Path<?>> pathComparator = new Comparator<Path<?>> () {
		public int compare(Path<?> o1, Path<?> o2) {
			final int d = (o1.distance < o2.distance? -1 : (o1.distance > o2.distance? 1 : 0));
			return d != 0? d : (o1.serial < o2.serial? -1 : (o1.serial > o2.serial? 1 : 0));
		}
	};
	
	/**
	 * Compute the shortest path between two nodes. This function returns a 
	 * {@link Path} instance, which represents the shortest path between the
	 * nodes {@code start} and {@code end}. The function uses Dijkstra's 
	 * algorithm.
	 * 
	 * <p>The {@code scout} is used to enumerate the edges, which lead out
	 * of some node {@code n} encountered during the computation. For each
	 * node in the graph reachable from {@code start}, the {@link Scout#listEdges(Object, EdgeBuffer) listEdges}
	 * is called at most once.
	 * 
	 * <p>This function uses hashing as tracking strategy.
	 * 
	 * @param <A>		node type
	 * @param start		start of the traversal
	 * @param end		desired destination node
	 * @param scout		provides outgoing edges
	 * 
	 * @return	a {@link Path} instance representing the shortest path from
	 * 			the given start node to the given end node, or <code>null</code>,
	 * 			if the end node cannot be reached from the start node.
	 */
	
	public static <A> Path<A> between(A start, A end, Scout<A> scout) {
		return between(start, end, scout, TrackingStrategies.<A>hashing());
	}

	/**
	 * Compute the shortest path between two nodes. This function returns a 
	 * {@link Path} instance, which represents the shortest path between the
	 * nodes {@code start} and {@code end}. The function uses Dijkstra's 
	 * algorithm.
	 * 
	 * <p>The {@code scout} is used to enumerate the edges, which lead out
	 * of some node {@code n} encountered during the computation. For each
	 * node in the graph reachable from {@code start}, the {@link Scout#listEdges(Object, EdgeBuffer) listEdges}
	 * is called at most once.
	 * 
	 * <p>The {@code tracking} strategy is used to create a map internally
	 * used to associate individual nodes with data needed for the computation.
	 * 
	 * @param <A>		node type
	 * @param start		start of the traversal
	 * @param end		desired destination node
	 * @param scout		provides outgoing edges
	 * @param tracking	used to allocate maps
	 * 
	 * @return	a {@link Path} instance representing the shortest path from
	 * 			the given start node to the given end node, or <code>null</code>,
	 * 			if the end node cannot be reached from the start node.
	 */
	
	public static <A> Path<A> between(A start, A end, Scout<A> scout, TrackingStrategy<A> tracking) {
		
		@SuppressWarnings({"rawtypes","unchecked"}) final Map<A,Path<A>> obarray = tracking.newMap((Class)Path.class);
		final HashSet<Path<A>> obsolete = new HashSet<Path<A>>();
		final PriorityQueue<Path<A>> queue = new PriorityQueue<Path<A>>(16, pathComparator);
		final EdgeBuffer<A> eb = new EdgeBuffer<A>();
		int counter = 0;
		
		{
			final Path<A> node = new Path<A>(++counter, start);
			queue.add(node);
			obarray.put(start, node);
		}
		
		while( !queue.isEmpty() ) {
			
			final Path<A> step = queue.remove();
			
			if( obsolete.add(step) ) {
				
				final A head = step.steps.head();
				
				if( tracking.areEqual(end, head) ) return step;
				else {
					
					scout.listEdges(head, eb);
					
					for( int k = 0; k < eb.size; ++k ) {
						
						@SuppressWarnings("unchecked") final A node = (A)eb.nodes[k];
						final double distance = eb.distances[k];
						final Path<A> present = obarray.remove(node);
						
						if( present == null ) {
							
							final Path<A> next = new Path<A>(++counter, node, distance, step);
							obarray.put(node, next);
							queue.add(next);
							
						} else {
							
							present.update(distance, step);
							queue.add(present);
						}
					}
					
					eb.clear();
				}
			}
		}
		
		return null;
	}
	
	public static <A> Cursor<Path<A>> fromNode(A start, Scout<A> scout) {
		return fromNode(start, scout, TrackingStrategies.<A>hashing());
	}
	
	/**
	 * Creates a cursor iterating over all nodes reachable from a given start
	 * node. This function returns a cursor, which yields all nodes reachable
	 * from {@code start} in the order defined by their distance from the starting
	 * point, closer nodes first.
	 * 
	 * <p>In each {@link Cursor#next() advance} step, the cursor will 
	 * traverse just enough of the object graph to determine the next path
	 * segment to yield, and what it needs to proceed after returning from
	 * {@code advance}.
	 * 
	 * <p>The {@code scout} is used to enumerate the edges, which lead out
	 * of some node {@code n} encountered during the computation. For each
	 * node in the graph reachable from {@code start}, the {@link Scout#listEdges(Object, EdgeBuffer) listEdges}
	 * is called at most once.
	 * 
	 * <p>The {@code tracking} strategy is used to create a map internally
	 * used to associate individual nodes with data needed for the computation.
	 * 
	 * @param <A>
	 * @param start		start node
	 * @param scout		determines the outgoing edges of a node 
	 * @param tracking	determines node equality
	 * 
	 * @return	a cursor iterating over the reachable nodes in a graph
	 * 			with a given starting point.
	 */
	
	public static <A> Cursor<Path<A>> fromNode(A start, Scout<A> scout, TrackingStrategy<A> tracking) {
		return new PathCursor<A>(start, scout, tracking);
	}
	
	private static final class PathCursor<A>
	implements Cursor<Path<A>> {
		
		private final Scout<A> scout;
		private final HashSet<Path<A>> obsolete;
		private final Map<A,Path<A>> obarray;
		private final EdgeBuffer<A> edges;
		private final PriorityQueue<Path<A>> queue;
		private Path<A> next;
		private int counter;
		
		@SuppressWarnings({"rawtypes","unchecked"})
		PathCursor(A start, Scout<A> scout, TrackingStrategy<A> tracking) {
			this.counter = 0;
			this.obarray = tracking.newMap((Class)Path.class);
			this.scout = scout;
			this.obsolete = new HashSet<Path<A>>();
			this.edges = new EdgeBuffer<A>();
			this.queue = new PriorityQueue<Path<A>>(16, pathComparator);
			final Path<A> node = new Path<A>(++counter, start);
			this.obarray.put(start, node);
			this.queue.add(node);
		}
		
		public boolean next() {
			
			while( !queue.isEmpty() ) {
				
				final Path<A> node = queue.remove();
				
				if( obsolete.add(node) ) {
					
					final A head = node.steps.head();
					
					scout.listEdges(head, edges);
					
					for( int k = 0; k < edges.size; ++k ) {

						@SuppressWarnings("unchecked") final A cont = (A)edges.nodes[k];
						final double distance = edges.distances[k];
						final Path<A> present = obarray.remove(cont);
						
						if( present == null ) {
							
							final Path<A> next = new Path<A>(++counter, cont, distance, node);
							obarray.put(cont, next);
							queue.add(next);
							
						} else {
							
							present.update(distance, node);
							queue.add(present);
						}
					}
					
					edges.clear();
					this.next = node;
					return true;
				}
			}
			
			this.next = null;
			return false;
		}
		
		public Path<A> get() {
			if( next == null ) throw new IllegalStateException();
			return next;
		}
	}
	
	private ShortestPath() {
		
	}
}

