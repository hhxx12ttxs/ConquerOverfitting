package agents.ramirez.das;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.PriorityQueue;

import agents.ramirez.das.utils.DoubleSlidingWindow;
import au.rmit.ract.planning.pathplanning.entity.ComputedPlan;
import au.rmit.ract.planning.pathplanning.entity.State;
import pplanning.interfaces.PlanningAgent;
import pplanning.simviewer.model.GridCell;
import pplanning.simviewer.model.GridDomain;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

public class PathPlanner implements PlanningAgent {
	
	final ThreadMXBean threadMX = ManagementFactory.getThreadMXBean();
	
	final private double 				MS_TO_NS_CONV_FACT = 1E6;
	
	private PriorityQueue<Node> 		OPEN = new PriorityQueue<Node>();
	private PriorityQueue<Node> 		PRUNED = new PriorityQueue<Node>();
	private Node[][] 					CLOSED;
	private Node[][]					OPEN_HASH;
	private Node[][]					PRUNED_HASH;
	private GridDomain 					problem = null;
	private	 ComputedPlan				solution = null;
	private int						planStep = 0;
	private GridCell 					goal = null;
	private Node 						root = null;
	private float 						h0;
	private long  						expanded = 0;
	private long						generated = 0;
	private boolean					done = false;
    private GridCell lastGoal = null;
    	
	// These values needs tuning!
	// This is the size of the sliding window, in entries.
	final private int EXPANSION_DELAY_WINDOW_LENGTH = 2000;

	// r_default. Used before conExpansionIntervals has settled.
	// This is the number of expansions to perform before the sliding window is deemed 'settled'
	final private int SETTLING_EXPANSION_COUNT = 1500;

	// Delta e value
	private DoubleSlidingWindow conExpansionDelays = new DoubleSlidingWindow(EXPANSION_DELAY_WINDOW_LENGTH,
			SETTLING_EXPANSION_COUNT);

	// r value
	private DoubleSlidingWindow conExpansionIntervals = new DoubleSlidingWindow(EXPANSION_DELAY_WINDOW_LENGTH,
			SETTLING_EXPANSION_COUNT);

	private double lastExpansionTime;
	
	@Override
	public ArrayList<GridCell> expandedNodes() {
		ArrayList<GridCell> nodes = new ArrayList<GridCell>();
		for ( int i = 0; i < problem.getWidth(); i++ ) {
			for ( int j = 0; j < problem.getHeight(); j++ )
				if ( CLOSED[i][j] != null )
					nodes.add(CLOSED[i][j].state);
		}
		return nodes;
	}
	
	@Override
	public ArrayList<GridCell> unexpandedNodes() {
		ArrayList<GridCell> nodes = new ArrayList<GridCell>();
		while (!OPEN.isEmpty()) {
			Node n = OPEN.poll();
			if ( !isClosed(n.state) ) {
				nodes.add( n.state );
			}
		}
		
		while (!PRUNED.isEmpty()) {
			Node n = PRUNED.poll();
			if ( !isClosed( n.state ))
				nodes.add( n.state );
		}
		return nodes;
	}
	
	private double getElapsed() {
		//return System.nanoTime() / MS_TO_NS_CONV_FACT;
		//return System.nanoTime();
		return threadMX.getCurrentThreadCpuTime();
	}
	
	@Override
	public GridCell getNextMove(GridDomain map, GridCell start, GridCell goal,
			int stepLeft, long stepTime, long timeLeft) {
		
		
		// do we need to replan?
		boolean replan = 
				solution == null ||			// no last path stored, have yet notr planned before?
				map.getChangedEdges().size() > 0 ||	// map has had changes 
				!lastGoal.equals(goal) || // Goal has changed (equals not implemented?)
				!solution.contains(start); // sNode is not in the path (sNode out of track)

	
				
				if (replan)	{ // Only do this if there is need to re-plan 
			planStep = 0;
			System.out.println("Initializing search engine");
			initialize( map, start, goal ); 
			
			double searchTime = (double)timeLeft * MS_TO_NS_CONV_FACT * 0.9; //- ((double)stepTime * h0);
			//double searchTime = ((double) (timeLeft-40L)) * MS_TO_NS_CONV_FACT; //- ((double)stepTime * h0);
			//double searchTime = (double)timeLeft * MS_TO_NS_CONV_FACT; //- ((double)stepTime * h0);

			System.out.println( "Search time set to " + ( searchTime/ MS_TO_NS_CONV_FACT ) + " ms" );
			double deadline = getElapsed() + searchTime;
			
			Node p = doSearch(deadline);
			if ( p == null) {
				System.out.println("No solution was found!");
				return start;
			}
			extractSolution(p);
			System.out.println("Plan length: " + solution.getLength() + " Cost: " + solution.getCost());
		
			
			lastGoal = goal;
		} 

		// execute next step is available
		if (solution != null && solution.getCurrentStepNo() < solution.getLength()) {
			return (GridCell) solution.getNextStep();
		} else
				return null;
	}

	private void extractSolution(Node n) {
		solution = new ComputedPlan();
		Node current = n;
		while ( current != null ) {
			solution.prependStep(current.state);
			current = current.parent;
		}
		solution.setCost( n.gn );
	}

	private Node doSearch(double deadline) {
		System.out.println("Starting search");
		long d_max = Long.MAX_VALUE;
		Node incumbent = null;
		OPEN.offer(root);
		
		while ( getElapsed() < deadline ) {
			
			if ( !OPEN.isEmpty() ) { 

				Node n = OPEN.poll();
				if ( incumbent != null && n.gn >= incumbent.gn )
					continue;
				/*
				if ( incumbent != null && n.gn > incumbent.gn ) {
					close(n);
				}
				*/
				//System.out.println("Expansion: " + expanded + ", h(n) = " + n.hn + ", e(n) = " + n.avg_error + " d(n) = " + n.corrected_dist_est + ", d_max = " + d_max);
				d_max = computeMaxDepth( deadline );
				if ( isGoal( n )  ) {
					if ( incumbent == null || n.gn < incumbent.gn ) {
						System.out.println( "g(n) = " + n.gn );
						incumbent = n;
					}
				}
				else if ( isClosed( n.state ) ) {
					if ( n.gn < getClosed( n.state ).gn ) {
						resetClosed(n.state);
						if ( n.corrected_dist_est < d_max  ) {
							expand(n);
							measureExpansionTime();
						}
						else
							//PRUNED.offer(n);
							insertPruned(n);
					}
				}
				else if ( n.corrected_dist_est < d_max ) {
					expand(n);
					measureExpansionTime();
				}
				else
					//PRUNED.offer(n);
					insertPruned(n);
				
			}
			else
				recoverNodesFromPruned(deadline);
		}
		
		System.out.println("Nodes expanded: " + expanded + " generated: " + generated );
		System.out.println( "Time left: " + (getElapsed() - deadline) + " ns"  );
		done = true;
		return incumbent;
	}

	private void resetClosed(GridCell s) {
		CLOSED[s.getCoord().getX()][s.getCoord().getY()] = null;
	}

	private Node	getClosed( GridCell s ) {
		return CLOSED[s.getCoord().getX()][s.getCoord().getY()];
	}
	
	private boolean isClosed(GridCell s) {
		return CLOSED[s.getCoord().getX()][s.getCoord().getY()] != null;
	}

	private int recoverNodesFromPruned(double deadline ) {
		double exp = (double)estimateExpansionsRemaining(deadline);
		int count = 0;
		while ( exp > 0 && !PRUNED.isEmpty() ) {
			Node n = null;
			// MRJ: Make sure the nodes we recover
			// from the PRUNED list aren't already in CLOSED
			while ( ! PRUNED.isEmpty() ) {
				n = PRUNED.poll();
				if ( !isClosed(n.state) )
					break;
				if ( n.gn < getClosed(n.state).gn ) {
					resetClosed( n.state );
					break;
				}
				System.out.println("Found node in PRUNED which is in CLOSED");
			}
			if ( n == null )
				break;
			//OPEN.offer(n);
			insertOpen(n);
			exp -= n.corrected_dist_est;
			count++;
		}
		conExpansionIntervals.reset();
		conExpansionDelays.reset();
		//System.out.println("Nodes recovered from PRUNED: " + count );
		return count;
	}

	private void expand(Node n) {
		expanded++;
		long nDelay = expanded - n.exp;
		conExpansionDelays.Push(nDelay);
		for (State absSucc : problem.getSuccessors(n.state) ) {
			if ( problem.isBlocked(absSucc)) continue;
			
			GridCell succ = (GridCell)absSucc;
			
			float curr_g = n.gn + problem.cost(n.state, succ);
			// MRJ: Check whether the successor s' is in CLOSED
			if ( isClosed( succ ) ) { 
				// MRJ: If so, check whether the path we have recorded
				// in CLOSED is worse than the one we found
				if ( curr_g >= getClosed(succ).gn )
					continue;
				// MRJ: If we found a better path, we clear the entry we
				// had, and add it into OPEN
				resetClosed( succ );
			}
			
			float h = manhattan(succ);
			
			Node n2 = new Node( succ, n, problem.cost(n.state, succ) , h, h, expanded );
			//OPEN.offer( n2 );
			insertOpen(n2);
			generated++;
		}
		close(n);

		//measureExpansionTime();

	}
	
	private void measureExpansionTime() {
		double currentTime = getElapsed();
		double expansionDelta = currentTime - lastExpansionTime;
		assert  expansionDelta > 0;
		lastExpansionTime = currentTime;
		conExpansionIntervals.Push(expansionDelta);	
	}

	private void close(Node n) {
		if ( CLOSED[n.state.getCoord().getX()][n.state.getCoord().getY()] != null ) {
			Node n2 = CLOSED[n.state.getCoord().getX()][n.state.getCoord().getY()];
			System.out.println( "g(n) = " + n.gn + ", g(n') = " + n2.gn);
		}
			
		CLOSED[n.state.getCoord().getX()][n.state.getCoord().getY()] = n;
	}

	private boolean isGoal(Node n) {
		return n.state.getCoord().getX() == goal.getCoord().getX() 
				&& n.state.getCoord().getY() == goal.getCoord().getY();
	}

	private long computeMaxDepth( double deadline ) {
		long d_max = Integer.MAX_VALUE;

		if ( !statsSettled()) return d_max;
		
		double avgExpansionDelay = conExpansionDelays.getAvg();

		double exp = (double)estimateExpansionsRemaining(deadline);
		d_max = (long) ( exp / avgExpansionDelay);
		//System.out.println("exp = " + exp + ", \\Delta e=" + avgExpansionDelay);
		//System.out.println("d_max = " + d_max);
		
		return d_max;		
	}

	private double estimateExpansionsRemaining(double deadline) {
		double remainingTime = deadline - getElapsed();
		double avgExpansionInterval = conExpansionIntervals.getAvg();
		double exp = remainingTime / avgExpansionInterval;
		return exp;
	}

	@Override
	public ComputedPlan getPath() {
		return solution;
	}

	@Override
	public Boolean showInfo() {
		return done;
	}

	private	 void	initialize( GridDomain prob, GridCell s0, GridCell g ) {
		problem = prob;
		System.out.println( "Map dimensions: " + problem.getWidth() + "x" + problem.getHeight() );
		goal = g;
		h0 = manhattan(s0);
		expanded = 0;
		CLOSED = new Node[problem.getWidth()][problem.getHeight()];
		OPEN_HASH = new Node[problem.getWidth()][problem.getHeight()];
		PRUNED_HASH = new Node[problem.getWidth()][problem.getHeight()];
		System.out.println("Closed has been initialized");
		root = new Node( s0, null, 0.0f, h0, h0, expanded );
		
		lastExpansionTime = getElapsed();
	}

	private Node getOpen( Node n ) {
		return OPEN_HASH[n.state.getCoord().getX()][n.state.getCoord().getY()];
	}
	
	private Node getPruned( Node n ) {
		return PRUNED_HASH[n.state.getCoord().getX()][n.state.getCoord().getY()];
	}
	
	private void insertOpen( Node n ) {
		Node n2 = getOpen(n);
		if ( n2 != null ) return;
		OPEN.offer(n);			
	}

	private void insertPruned( Node n ) {
		Node n2 = getPruned(n);
		if ( n2 != null ) return;
		PRUNED.offer(n);			
	}
	
	private float manhattan(GridCell s) {
		return Math.abs(s.getCoord().getX() - goal.getCoord().getX()) +
			       Math.abs(s.getCoord().getY() - goal.getCoord().getY());
	}

	private float euclidean(GridCell s) {
		float dx = s.getCoord().getX() - goal.getCoord().getX();
		dx *= dx;
		float dy = s.getCoord().getY() - goal.getCoord().getY();
		dy *= dy;
		return (float)Math.sqrt( dx + dy );
	}
	
	private boolean statsSettled()
	{
		// TODO: need to change sliding window class so that it has both windows as one.
		boolean isSettled = (conExpansionDelays.getSettled() && conExpansionIntervals.getSettled());
		return(isSettled);
	}
}

