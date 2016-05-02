/**
 * 
 */
package s3.ai.basic.pathfinding;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

import s3.ai.basic.modules.PathFinder;
import s3.base.S3;
import s3.entities.S3PhysicalEntity;
import s3.util.Pair;

/**
 * @author Josh Datko
 * 
 */
public class DLite implements PathFinder, Comparator<Double> {

	/**
	 * @author jdatko
	 * 
	 *         This class implements the comparator to compare G values in the
	 *         D*Lite algorithm
	 */
	public class gCompare implements Comparator<DLiteNode> {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(DLiteNode lhs, DLiteNode rhs) {

			// Simple less than / greater than based on double value
			if (lhs.get_g() < rhs.get_g()) {
				return -1;
			} else if (lhs.get_g() == rhs.get_g())
				return 0;
			else
				return 1;
		}

	}

	protected DLiteNode goal;
	protected DLiteNode start;
	protected PriorityQueue<DLiteNode> queue;
	protected S3PhysicalEntity entity;
	protected S3 game;
	protected double km = 0;
	protected HashMap<MapPoint, DLiteNode> map;
	protected List<Pair<Double, Double>> path;

	public DLite() {
		this.queue = new PriorityQueue<DLiteNode>();
	}

	/**
	 * Main Constructor to be used when there are useful game entities to be
	 * passed in.
	 * 
	 * @param start_x
	 *            The starting x coordinate
	 * @param start_y
	 *            The starting y coordinate
	 * @param goal_x
	 *            The goal x coordinate
	 * @param goal_y
	 *            The goal y coordinate
	 * @param i_entity
	 *            The entity trying to move
	 * @param the_game
	 *            The game instance
	 */
	public DLite(double start_x, double start_y, double goal_x, double goal_y,
			S3PhysicalEntity i_entity, S3 the_game) {

		// Assign the start and the goal
		this.start = new DLiteNode(start_x, start_y);
		this.goal = new DLiteNode(goal_x, goal_y);

		// Setup the open queue, default to 100 nodes.
		this.queue = new PriorityQueue<DLiteNode>(100, this.goal);

		// the hashmap is the abstracted map
		this.map = new HashMap<MapPoint, DLiteNode>();

		// So, we know that the start and goal are real (assumption) so put them
		// into the map
		this.map.put(new MapPoint(start_x, start_y), this.start);
		this.map.put(new MapPoint(goal_x, goal_y), this.goal);

		// Per D*Lite set to 0
		this.goal.set_rhs(0);

		// Calculate the goal key
		this.goal.setKey(this.calculateKey(this.goal));

		// D*Lite works "backwards" from the gaol, so place the goal in the
		// queue first
		queue.add(this.goal);

		this.entity = (S3PhysicalEntity) (i_entity.clone());
		this.game = the_game;

		this.path = new LinkedList<Pair<Double, Double>>();
	}

	/**
	 * Calculates the Heuristic using straight-line "euclidian" estimates.
	 * 
	 * @param node1
	 *            From node
	 * @param node2
	 *            To Node
	 * @return Heuristic Value, always underestimate. In this case, it is the
	 *         exact distance (euclidian) from the two points
	 */
	protected double calculateHeuristic(DLiteNode node1, DLiteNode node2) {
		return DLiteNode.computeDistance(node1, node2);
	}

	/**
	 * Calculate key per D*Lite Algorithm
	 * 
	 * @param node
	 *            The node for which the key needs calculating
	 * @return Return the key's value
	 */
	protected MapPoint calculateKey(DLiteNode node) {

		double x = 0;
		double y = 0;

		double rhs_plus_h = DLiteNode.safeAdd(node.get_rhs(),
				this.calculateHeuristic(node, this.start));

		double rhs_plus_h_plus_k_m = DLiteNode.safeAdd(rhs_plus_h, this.km);

		x = Math.min(node.get_g(), rhs_plus_h_plus_k_m);

		y = Math.min(node.get_g(), node.get_rhs());

		return new MapPoint(x, y);

	}

	/**
	 * This method determines if there is a collision with the passed in entity
	 * in the game
	 * 
	 * @param entity
	 *            Entity for which we check if there is a collision
	 * @return null if there is a collision or the node of there is none
	 */
	protected DLiteNode checkCollisionWith(S3PhysicalEntity entity) {

		MapPoint loc = new MapPoint((double) entity.getX(),
				(double) entity.getY());

		DLiteNode cachedNode = this.map.get(loc);

		boolean collision = false;


		if ( this.game.anyLevelCollision(entity) != null) {
			// This entity is colliding with something
			collision = true;
		}

		if (null == cachedNode) {
			// Our abstract map doesn't yet know abou this node, so add it
			cachedNode = new DLiteNode(loc);

			this.map.put(loc, cachedNode);

		}

		// Don't return invalid nodes, but we wanted to add it to the map prior
		// to returning
		if (true == collision)
			return null;

		return cachedNode;
	}

	@Override
	public int compare(Double lhs, Double rhs) {
		// Standard compare, useful for priority queues
		if (lhs < rhs)
			return -1;
		else if (lhs == rhs)
			return 0;
		else
			return 1;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see s3.ai.basic.modules.PathFinder#computePath()
	 */
	@Override
	public List<Pair<Double, Double>> computePath() throws NullPointerException {

		//System.out.println("Starting node: " + this.start.toString());
		//System.out.println("Goal Node: " + this.goal.toString());

		if (this.start.get_x() < 0 || this.start.get_y() < 0
				|| this.goal.get_x() < 0 || this.goal.get_y() < 0)
			throw new NullPointerException("Invalid start or goal");

		DLiteNode current = this.start;

		//System.out.println(current.toString());

		try {

			// This is the real D*Lite algorithm
			this.computeShortestPath();

			// Create an instance of a comparator, for use in the priority queue
			gCompare compare = new gCompare();

			if (path.isEmpty()) {

				while (!current.isSame(this.goal)) {

					// Using 8 way navigation, the queue should have no more
					// than 9 entries per node
					PriorityQueue<DLiteNode> min = new PriorityQueue<DLiteNode>(
							9, compare);

					// Add all the neighbors, this can throw an exception if
					// there are no valid neighbors. This means that this unit
					// has no valid moves in any directions at the moment.
					min.addAll(this.getNeighbors(current));

					// Add the location to the path
					MapPoint next = min.peek().getLoc();
					
					if ( this.path.contains(next ))
						throw new NullPointerException("No Path");
					
					this.path.add(next);

					// Get a new current
					current = min.poll();

					if (current.get_g() == Double.MAX_VALUE)
						throw new NullPointerException("No Path");

					//System.out.println(current.toString());

				}
			}
		} catch (NullPointerException e) {
			// No path
			this.path.clear();
			String s = new String("No Path to goal: " + this.goal.toString());
			//System.out.println(s);
			throw new NullPointerException(s);
		}

		return this.path;
	}

	/**
	 * Compute shortest path using D*Lite
	 * 
	 * @throws NullPointerException
	 *             If there is no path
	 */
	protected void computeShortestPath() throws NullPointerException {

		if (queue.isEmpty()) {
			throw new NullPointerException("No Path");
		}
		while (!queue.isEmpty()
				&& (this.start.compare(queue.peek(),
						this.calculateKey(this.start)) == -1
				|| this.start.get_rhs() != this.start.get_g())) {

			DLiteNode k_old = queue.peek();
			DLiteNode u = queue.poll();

			if (k_old.compare(k_old, this.calculateKey(k_old)) == -1) {
				u.setKey(this.calculateKey(k_old));
				// add back to map
				this.map.put(u.getLoc(), u);
				queue.add(u);
			} else if (u.get_g() > u.get_rhs()) {
				// This node has improved

				u.set_g(u.get_rhs());
				// add back to map
				this.map.put(u.getLoc(), u);
				try {
					for (DLiteNode s : this.getPredecessors(u)) {
						this.updateVertex(s);
					}
				} catch (NullPointerException e) {
					// No valid neighbors
					throw new NullPointerException("No Path");
				}
			} else {
				// This path is now worse, set g back to max

				u.set_g(Double.MAX_VALUE);
				// add back to map
				this.map.put(u.getLoc(), u);
				try {
					for (DLiteNode s : this.getPredecessors(u)) {
						this.updateVertex(s);
					}
				} catch (NullPointerException e) {
					System.out.println(e.toString());
				}
				this.updateVertex(u);
			}
		}
	}

	/**
	 * Calculate the cost from going to node 1 to node 2, which is simply the
	 * distance between the two nodes. There is no special weighting.
	 * 
	 * @param lhs
	 *            node 1
	 * @param rhs
	 *            node 2
	 * @return the cost
	 */
	protected double cost(DLiteNode lhs, DLiteNode rhs) {
		return DLiteNode.computeDistance(lhs, rhs);
	}

	/**
	 * Get the list of nodes that have been updated dynamically.
	 * 
	 * @return the list of nodes that have changed since the last run of compute
	 *         shortest path
	 */
	protected List<DLiteNode> getModifiedNodes() {

		List<DLiteNode> modified = new LinkedList<DLiteNode>();

		for (MapPoint loc : this.map.keySet()) {
			DLiteNode node = this.map.get(loc);

			if (node.isModified())
				modified.add(node);

			// return the node
			this.map.put(loc, node);

		}

		return modified;

	}

	protected List<DLiteNode> getNeighbors(DLiteNode node) {
		// Add all the 9 neighbors of the node after checking to see if they
		// can be reached or not
		int height = this.game.getMap().getHeight();
		int width = this.game.getMap().getWidth();

		List<DLiteNode> neighborList = new ArrayList<DLiteNode>();
		for (int i = -1; i <= 1; i++)
			for (int j = -1; j <= 1; j++) {
				if (!((i == 0 && j == 0) || (node.get_x() + i) < 0
						|| (node.get_y() + j) < 0
						|| (node.get_x() + i) >= width || (node.get_y() + j) >= height)) {
					this.entity.setX((int) node.get_x() + i);
					this.entity.setY((int) node.get_y() + j);
					// perform check to see if the location is OK or not

					DLiteNode temp = this.checkCollisionWith(entity);

					if (temp != null)
						neighborList.add(temp);

				}
			}

		if (neighborList.isEmpty())
			throw new NullPointerException("No valid neighbors");

		return neighborList;
	}

	/**
	 * Gets the predecessors, in a 2D game, predecessors = successors =
	 * neighbors
	 * 
	 * @param node
	 *            node for which neighbors are found
	 * @return list of neighbors
	 */
	protected List<DLiteNode> getPredecessors(DLiteNode node) {

		return this.getNeighbors(node);

	}

	/**
	 * Gets the predecessors, in a 2D game, predecessors = successors =
	 * neighbors
	 * 
	 * @param node
	 *            node for which neighbors are found
	 * @return list of neighbors
	 */
	protected List<DLiteNode> getSuccessors(DLiteNode node) {

		return this.getNeighbors(node);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see s3.ai.basic.modules.PathFinder#pathDistance(double, double, double,
	 * double, s3.entities.S3PhysicalEntity, s3.base.S3)
	 */
	@Override
	public int pathDistance(double start_x, double start_y, double goal_x,
			double goal_y, S3PhysicalEntity i_entity, S3 the_game) {
		// TODO Auto-generated method stub

		List<Pair<Double, Double>> moves = null;
		try {
			moves = this.computePath();
		} catch (NullPointerException e) {
			// No Path
			return -1;
		}

		return moves.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see s3.ai.basic.modules.PathFinder#replan(double, double)
	 */
	public List<Pair<Double, Double>> replan(double current_x, double current_y) {
		// Ok, something changed so go and update our map
		
		//First ditch our current path, it obvously doesn't work
		this.path.clear();
		
		MapPoint current = new MapPoint(current_x, current_y);
		this.updateMap(current);
		// get the current node
		this.km = DLiteNode.safeAdd(this.km,
				this.calculateHeuristic(this.map.get(current), this.start));
		this.start = this.map.get(current);

		// Get the list of changed nodes
		List<DLiteNode> modified = this.getModifiedNodes();

		HashSet<DLiteNode> changed = new HashSet<DLiteNode>();
		
		//get all the neighbors (changed edge weights)
		for (DLiteNode u : modified){
			List<DLiteNode> l = this.getNeighbors(u);
			changed.addAll(l);
		}
		// Update the vertex on each
		for (DLiteNode u : changed) {
			this.updateVertex(u);
		}

		return this.computePath();

	}


	/**
	 * Scans the known map and compares to the game map, finding if there are
	 * inconsistencies, marks that node as changed and returns all changed
	 * nodes.
	 * 
	 * @param current
	 *            current location
	 */
	protected void updateMap(MapPoint current) {

		for (MapPoint loc : this.map.keySet()) {

			DLiteNode node = this.map.get(loc);
			

			this.entity.setX(loc.m_a.intValue());
			this.entity.setY(loc.m_b.intValue());

			if (current.equals(node.getLoc())) {
				// Don't change the current node
			} else if (node.getLoc().equals(this.start.getLoc())){
				//start is a special case
			} else if ( node.getLoc().equals(this.goal.getLoc())) {
				// Check is the goal is still reachable
				if (this.game.anyLevelCollision(this.entity) != null){
					//Goal has become unreachable
					throw new NullPointerException("Goal is now unreachable");
				}
			} else if (this.game.anyLevelCollision(this.entity) != null
					&& !(node.get_rhs() == node.get_g() && node.get_g() == Double.MAX_VALUE )) {
				// There is a new collision
				
				
				node.set_g(Double.MAX_VALUE);
				node.set_rhs(Double.MAX_VALUE);
				node.setModified();

				this.map.put(node.getLoc(), node);
			}

		}

	}

	/**
	 * Update the Vertex per the D*Lite algorithm
	 * 
	 * @param node
	 *            to update
	 */
	protected void updateVertex(DLiteNode node) {

		try {
			if (!node.isSame(this.goal)) {
				List<DLiteNode> succ = this.getSuccessors(node);

				PriorityQueue<Double> succQ = new PriorityQueue<Double>(10,
						this);
				for (DLiteNode s : succ) {
					Double value = new Double(this.cost(node, s) + s.get_g());

					succQ.offer(value);
				}

				Double bestValue = succQ.poll();

				if (bestValue == null)
					throw new NullPointerException("Update Vertex failed");

				node.set_rhs(bestValue);
				// add back to map
				this.map.put(node.getLoc(), node);

			}
		} catch (NullPointerException e) {
			// no successors
		}

		if (queue.contains(node)) {
			queue.remove(node);
		}

		if (node.get_g() != node.get_rhs()) {
			node.setKey(this.calculateKey(node));

			// add back to map
			this.map.put(node.getLoc(), node);

			queue.offer(node);

		}

	}

}

