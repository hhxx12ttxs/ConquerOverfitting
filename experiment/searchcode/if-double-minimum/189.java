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

import s3.ai.AStar;
import s3.ai.basic.modules.PathFinder;
import s3.base.S3;
import s3.entities.S3PhysicalEntity;
import s3.util.Pair;

/**
 * @author Walter Gress
 *
 */
public class ADStar implements PathFinder {

	//****borrowed data structures from AStar

	//Priority queue for candidates to be explored 
	protected PriorityQueue<Node> OPEN;

	//Queue for nodes already expanded in search
	protected PriorityQueue<Node> CLOSED;

	//Queue for nodes already visited
	protected PriorityQueue<Node> VISITED;

	//contains nodes that have alrady been expanded but are inconsistent
	protected ArrayList<Node> INCONS;

	// protected List <Pair<Node,Node>> cameFromList;
	// useful for recording search and returning
	protected HashMap<Node, Node> cameFromHashMap;


	//initial node (state)
	protected Node startNode;

	//goal node (state)
	protected Node goalNode;

	//inflation factor epsilon (e)

	protected double e;

	//inflation factor epsilon initial

	protected final double e0 = 25;

	//--

	//reference to the game S3
	protected S3 s2;

	//reference to the physical entity for which calculations are being done
	protected S3PhysicalEntity entity;

	/******************************************************************************/
	/*******************************MAIN METHODS BLOCK********************************/
	/******************************************************************************/



	/**
	 * Calculates the path distance from the start to the finish, for the
	 * specified entity
	 * 
	 * @param start_x
	 *            The starting x coordinate to start
	 * @param start_y
	 *            The starting y coordinate to start
	 * @param goal_x
	 *            the goal x coordinate
	 * @param goal_y
	 *            the goal y coordinate
	 * @param i_entity
	 *            the S3 entity for which, this instance is planning a path
	 * @param the_game
	 *            instance of the game
	 * @return the number of points in the path (i.e. the size of the path)
	 */
	@Override
	public int pathDistance(double start_x, double start_y, double goal_x, double goal_y,
			S3PhysicalEntity i_entity, S3 the_game) {
		ADStar a = new ADStar(start_x,start_y,goal_x,goal_y,i_entity,the_game);
		List<Pair<Double, Double>> path = a.computePath(); //call on compute path, i'm assuming
		//the pair data struction represent coordinates
		if (path!=null) return path.size();
		return -1;
	}

	//`initializes ADStar (creates object) and performs search (calls computepath)
	//custom constructor
	public ADStar(double start_x, double start_y, double goal_x, double goal_y,
			S3PhysicalEntity i_entity, S3 the_game) {

		NodeComparator genericComparator = new NodeComparator();
		
		OPEN = new PriorityQueue<Node>(1, genericComparator);
		CLOSED = new PriorityQueue<Node>(1, genericComparator);
		VISITED = new PriorityQueue<Node>(1, genericComparator);
		INCONS = new ArrayList<Node>();
		startNode = new Node(start_x, start_y);
		goalNode = new Node(goal_x, goal_y);

		// Add the startNode to the OPEN set with appropriate key
		startNode.priority_key_one = startNode.key(startNode)[0];
		startNode.priority_key_two = startNode.key(startNode)[1];
		OPEN.add(startNode);  //&&&&&&&&????
		// Make the returnList
		cameFromHashMap = new HashMap<Node, Node>();
		entity = (S3PhysicalEntity) (i_entity.clone());
		s2 = the_game;

	}


	/**
	 * Calculates the path from start to goal.
	 * 
	 * @return Returns the List of coordinates along the computed path from
	 *         start to goal
	 */
	//****** main algorithm */
	@Override
	public List<Pair<Double, Double>> computePath() {

		// TODO Auto-generated method stub

		//init scores and data structure
		startNode.set_g_score(Double.POSITIVE_INFINITY);
		startNode.set_rhs_score(Double.POSITIVE_INFINITY);
		goalNode.set_g_score(Double.POSITIVE_INFINITY);
		goalNode.set_rhs_score(0);
		e = e0;;
		OPEN.clear(); CLOSED.clear(); INCONS.clear();
		List<Pair<Double, Double>> publishingList = new LinkedList<Pair<Double, Double>>();
		//insert sGoal into OPEN with key(s_goal)
		//start with goal  node and work backwards (dynamic algo style)
		goalNode.priority_key_one = goalNode.key(goalNode)[0];
		goalNode.priority_key_two = goalNode.key(goalNode)[1];
		OPEN.add(goalNode);


		ComputeOrImprovePath();
		publishingList = publish();
		//

		//NOTE: For inclusion in S3, i don't want to publish quite yet
		//**publish current suboptimal solution with e inflation factor


		//while time allotted hasn't run out
		//while (OPEN.size() > 0) {
			//System.err.println("Inside OPEN.size() loop:: " + OPEN.size());
			//*if changes in edge costs are detected
				//*for all directed edges (u,v) with changed edge costs
					//*Update the edge cost c(u,v)
					//*UpdateState(u);

			//*if significant edge cost changes were observed
			//*increase epsilon or replan from scratch

			//else if epsilon > 1
			//decrease episilon

			if (e > 1) {
				e -= 1;
				if (e <= 1) {
					e = 1;
				}
				
			}
		

			//move states from INCONS back to OPEN
			Node inconState = null;
			do {
				//System.err.println("moving states from INCONS to OPEN");
				
				if(!INCONS.isEmpty()) {
					inconState = INCONS.remove(0);
				}
				if (inconState != null) {
					OPEN.add(inconState);
				}
				
			} while (inconState != null);

			//move through queue and 
			//update priorities of states in open queue according to
			//key(s) function (COMPLETE)
			
			
			Comparator genericComparator = new NodeComparator();
			
			PriorityQueue<Node> tempQueue = new PriorityQueue<Node>(1, genericComparator);
			
			for (Node n : OPEN) {
				//System.err.println("Old key n:" + n.priority_key_one);
				Node j = n;
				j.priority_key_one = j.key(n)[0];
				j.priority_key_two = j.key(n)[1];
				//System.err.println("New key n: " + n.priority_key_one);
				//System.err.println("New key j: " + j.priority_key_one);
			}
			
			//CLOSED = 0 (empty set)
			CLOSED.clear(); 

			ComputeOrImprovePath(); //generate a new path again
			
			//publish current epsilon suboptimal solution
			//and continue working until e==1 (completely deflated)

			publishingList = publish();
			
			if (e == 1) {         //optimal solution
				//wait for change in edge costs to update path finder
			}

		//} //end while


		 
		//temporary for debugging

		List<Pair<Double, Double>> paths = new LinkedList<Pair<Double, Double>>();

		Pair pair1 = new Pair<Double,Double>(10.0,10.0);

		Pair pair2 = new Pair<Double,Double>(20.0,10.0);

		Pair pair3 = new Pair<Double,Double>(30.0,10.0);

		Pair pair4 = new Pair<Double,Double>(40.0,10.0);

		Pair pair5 = new Pair<Double,Double>(50.0,10.0);

		paths.add(pair1);
		paths.add(pair2);
		paths.add(pair3);
		paths.add(pair4);
		paths.add(pair5);

		//System.err.println("Main Path Algo():: Ending path finding");
		//return paths;
		
		
		List<Pair<Double, Double>> path = buildPath();
		
		
		return publishingList;

	}

	
	
	public List<Pair<Double, Double>> publish() {
		List <Pair<Double,Double>> publishingList = buildPath();

		return publishingList;
	}
	
	
	//builds the path to return to the caller
	
	public List<Pair<Double, Double>> buildPath() {
		
		List<Pair<Double, Double>> pathway = new LinkedList<Pair<Double, Double>>();
		
		System.err.println("Coords for Start Node are x: " + startNode.x + " y: " + startNode.y);
		if (!OPEN.isEmpty()) {
			for (Node node : OPEN) {
			
				if (node != null) {
					Pair<Double, Double> coords = new Pair<Double, Double>(node.x, node.y);
					pathway.add(coords);
					System.err.println("Adding coords x: " + node.x + " y: " + node.y + " to path");
				} 
			}
		} else {
			System.err.println("OPEN Queue is empty!");
		}
		
		System.err.println("Coords for Goal Node are x: " + goalNode.x + " y: " + goalNode.y);
		
		
		return pathway;
	}
	

	/******************************************************************************/
	/*******************************SEARCH HELPERS********************************/
	/******************************************************************************/

	protected void UpdateState(Node node) {		//MOST LIKELY COMPLETE

		
		
		//*if s was not visited before
		if (!VISITED.contains(node)) {
			node.set_g_score(Double.POSITIVE_INFINITY);
			VISITED.add(node);
		}

		if (node != goalNode) {
			node.rhs_calculate(node);
		} 
		
		///if already on the open list then remove it
		//remove node from open list
		if (OPEN.contains(node)) {
			OPEN.remove(node);
			CLOSED.add(node);
			VISITED.add(node);
		} 
		
		if (node.g(node) != node.rhs(node)) {  //state is not consistent. these are the states that need
													//need to be updated and made consistent. INCONS holds exactly inconsistent states
													//which are inserted into OPEN in the main loop
			//If all nodes are consistent, finding the optimal path is just moving to the next neighbor
			//g(n) = rhs(n) is considered consistent
			//search is conducted only on inconsistent neighbors
			
			//consistent? add node to open list
			if (!CLOSED.contains(node)) {     //if not a member of the CLOSED queue
				//System.err.("Add to open queue");
				//insert s into OPEN with key(s) 
				if (node != null) {
					//System.err.println("UpdateState():: node is not null");
					node.priority_key_one = node.key(node)[0];
					node.priority_key_two = node.key(node)[1];
					OPEN.add(node);
				}
				 
			} else {	 			// inconsistent?					
				INCONS.add(node);
			}
		}
		

	}


	//expand node?
	protected void ComputeOrImprovePath() {
		//remove state s with minimum key from OPEN

		Node removedNode;
		double minimum = 100000000.00;


		boolean result = false;
		
		do {
			//calculate key vs rhs for all open states
			//find minimum of above calculation
			//remove the state with that minimum


			//Remove state with minimum key and thus highest priority
			Node node = OPEN.remove();
			
			//System.err.("ComputerOrImprovePath():: Removing node from OPEN list size:: " + OPEN.size());
			
			if (node.g(node) > node.rhs(node)) {
				node.set_g_score(node.rhs(node));
				//add to closed list
				CLOSED.add(node);


				// For all s' that are an element of Pred(s), Update(s')

				for (Node predNode : node.getPredecessorNodes(node)) {
					UpdateState(predNode);
				}

			 }   else {

				node.set_g_score(Double.POSITIVE_INFINITY);

				// For all s' that are an element of Pred(s), UNION {S} and Update(S')

				UpdateState(node);

				for (Node predNode : node.getPredecessorNodes(node)) {
					UpdateState(predNode);
				}
			}
			
	
			//
			Comparator comparator = new NodeComparator();
			
			
			boolean keys = false;
			
			//true if we are done looping
			
			if (comparator.compare(node, startNode) == -1) {
				//less than
				keys = true; //continue looping
			} else if (comparator.compare(node, startNode) == 1) {
				keys = true;  //no more less thans, stop loop
			} else {
				keys = true; //no more less thans, stop loop
			}
			
			boolean rhses = node.rhs(startNode) != node.g(startNode);
			result = keys || rhses;
			//System.err.println("ComputeInLoop():: rhses: " + rhses);
			//System.err.println("ComputeInLoop():: keys" + keys);
			//System.err.println("ComputeInLoop():: result is " + result);
			
			//supposed to run until there is no value in the queue with
			//a key value less than that of the start state
			
		} while (!result);


		//System.err.println("Exiting compute");

		
		// Add the startNode to the OPEN set
		OPEN.add(startNode);
	}




	/******************************************************************************/
	/*******************************NODE INNER CLASS********************************/
	/******************************************************************************/



	//////////////	

	//Node class borrowed from AStar
	protected class Node {

		private Node prevNode; //previous node reference

		private double x;

		private double y;

		// distance to this node
		// g cost g(s)
		private double g_score = 10000;

		// heuristic distance to goal node
		// heuristic value h(s)
		private double h_score = 10000;


		private double rhs_score = 100000;

		private double priority_key_one = 10000.0;
		private double priority_key_two = 10000.0;


		public double g(Node node) {
			return node.g_score;
		}

		public double h(Node node) {
			node.compute_heuristic_distance_to_goal();
			return node.h_score;
		}

		//Constructor, initialize node
		public Node(double node_x, double node_y) {
			x = node_x;
			y = node_y;
		}

		//calculates heuristic using pythagorean distance as metric
		public void compute_heuristic_distance_to_goal() {
			h_score = computeDistance(this, goalNode);
		}

		//calculate g cost g(s)
		public void set_g_score(double i_g_score) {
			g_score = i_g_score;
		}


		//calculate using pythagorean distance as metric
		private double computeDistance(Node node1, Node node2) {
			return Math.sqrt((node1.x - node2.x) * (node1.x - node2.x) + (node1.y - node2.y)
					* (node1.y - node2.y));
		}

		//Maintains an estimated distance to goal
		//	
		//	min of all s' where s' is a set of the successors of s 
		//  					s is an element of Succ(s)
		//
		// rhs is the min of the cost function below for all values of s'
		// c(s,s') + (g(s'))
		//
		public double rhs(Node node) {
			return rhs_score ; //??= rhs_calculate(node);
		}


		protected double rhs_calculate(Node node) {

			double minimum = 10000000;

			if (node == goalNode) { return 0; }
			else {


				List<Node> successor_nodes = getSuccessorNodes(node);

				for (Node snode: successor_nodes) {
					minimum = Math.min(minimum, getTotalNodeCost(node, snode));
				}


				return minimum;
			}
		}

		//calculate g cost g(s)
		public void set_rhs_score(double i_rhs_score) {
			rhs_score = i_rhs_score;
		}



		//return successor node for current node
		//**!!!!!INCOMPLETE
		List<Node> getSuccessorNodes(Node node) {
			return getNeighbors(s2.getMap().getWidth(), s2.getMap().getHeight());
		}


		List<Node> getPredecessorNodes(Node node) {
			/*
			List<Node> successorNodes = new LinkedList<Node>();

			Node thisNode = node;
			Node prevNode;


			while ((prevNode = thisNode.prevNode) != null) {

				successorNodes.add(prevNode);

				thisNode = prevNode; //going backwards
			}*/

			return getNeighbors(s2.getMap().getWidth(), s2.getMap().getHeight());
		}




		//return c(s,s') + g(s)

		public double getTotalNodeCost(Node startNode, Node endNode) {
			return (arc_cost_c(startNode, endNode) + endNode.g_score); 
		}



		//MUST BE TESTED
		//for judging priorities of queue state nodes
		public double [] key(Node node) {

			double [] return_key = new double[2];

			if (g(node) > rhs(node)) {
				return_key[0] = (rhs(node) + e*h(node));
				return_key[1] = rhs(node);
				return return_key;
			} else {
				return_key[0] = g(node) + h(node);
				return_key[1] = g(node);
			}

			return return_key;
		}

		// IMPLEMENT: ARC COST
		//the cost of moving from s to s'

		public double c(Node node1, Node node2) {
			return arc_cost_c(node1, node2);
		}

		public double arc_cost_c(Node node1, Node node2) {
			return 1.0;
		}




		//return g(s) + h(s)
		public double getTotalNodeScore() {
			return h_score + g_score;
		}


		public List<Node> getNeighbors(int width, int height) {
			// Add all the 9 neighbors of the node after checking to see if they
			// can be reached or not
			List<Node> neighborList = new ArrayList<Node>();
			for (int i = -1; i <= 1; i++)
				for (int j = -1; j <= 1; j++) {
					if (!((i == 0 && j == 0) || (x + i) < 0 || (y + j) < 0 || (x + i) >= width || (y + j) >= height)) {
						entity.setX((int) x + i);
						entity.setY((int) y + j);
						// perform check to see if the location is OK or not
						if (s2.anyLevelCollision(entity)==null) {
							Node newNode = new Node(x + i, y + j);
							neighborList.add(newNode);
						}
					}
				}

			return neighborList;
		}


		public boolean equals(Object incoming) {
			Node n = (Node) incoming;
			if (this.x == n.x && this.y == n.y)
				return true;
			return false;
		}

		public int hashCode() {
			return (int)(x+(y*128));
		}



		//!!!CHANGE BELOW, ADSTAR IS DYNAMIC

		/**
		 * Query the algorithm to determine if an update is required. Some dynamic
		 * algorithms might need to check if there is a collision
		 * 
		 * @param current_x
		 *            The current x coordinate
		 * @param current_y
		 *            The current y coordinate
		 * @param goal_x
		 *            the goal x coordinate
		 * @param goal_y
		 *            the goal y coordinate
		 * @param i_entity
		 *            the S3 entity for which, this instance is planning a path
		 * @param the_game
		 *            instance of the game
		 * @return True if an update is required (i.e. if it is necessary to
		 *         replan), otherwise false
		 */
		//called for replanning
		public boolean updatedRequired(double current_x, double current_y,
				double goal_x, double goal_y, S3PhysicalEntity i_entity, S3 the_game) {
			return false;
		}


	}
	
	public class NodeComparator implements Comparator<Node> {

		//IMPLEMENTS COMPARATOR: currently only for priority key one

		public int compare(Node o1, Node o2) {


			//priority key one = a
			//priority key two = b

			double a1 = o1.priority_key_one;
			double a2 = o2.priority_key_one;
			double b1 = o1.priority_key_two;
			double b2 = o2.priority_key_two;



			//(a1,b1) < (a2,b2)
			//a1 < a2
			//a1 == a2 && b1 < b2

			if (a1 < a2) {
				return -1;
			} else if ((a1 == a2) && (b1 < b2)) {
				return -1;
			} else if ((a1 == a2) && (b1 == b2)) {
				return 0;
			} else if (a1 > a2) {
				return 1;
			} else {
				return 0;
			}

		}

		
		//currently performs shallow copu
		public boolean equals(Object incoming) {
			NodeComparator n = (NodeComparator) incoming;
			if (this == n)
				return true;
			return false;
		}

		
	}

	@Override
	public List<Pair<Double, Double>> replan(double current_x, double current_y) {
		// TODO Auto-generated method stub
		return null;
	}
}
