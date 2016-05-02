/*$$
 * Copyright (c) 2007, Centre of Informatics and Systems of the University of Coimbra 
 * All rights reserved.
 *
 * Rui Lopes
 *$$*/
package mater.paths;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.PriorityQueue;
import java.util.Vector;

import mater.MaterModel;
import mater.Profiler;
import mater.agents.City;
import mater.agents.Patch;

public class PathFinder implements PathFinderI {
	
	/*
	private final String labelTotal = "Total";
	private final String labelsuc =  "Successors";
	private final String labelFilter = "Filter Successors";
	private final String labelMin = "Collections.min";
	*/
	//for benchmarking
	private static int lengthSum = 0;
	private static int iterationSum = 0;
	private static int numPaths = 0;
	
	//temps para o search
	private Hashtable<Patch,PathNode> closedSet = new Hashtable<Patch,PathNode>();
	private PathNode nodeCurrent = null;
	private PathNode tempPathNode = null;	
	private Hashtable<Patch,PathNode> openSet = new Hashtable<Patch,PathNode>();
	private City goal = null;
	private MaterModel model;

	ArrayList<PathNode> successors = new ArrayList<PathNode>();
	private PriorityQueue<PathNode> pQueue = new PriorityQueue<PathNode>( 20, new NodeComp() );
	Profiler profiler = null;
	private static PathFinder _instance=null;

	protected PathFinder( MaterModel model){
		this.model = model;
		this.profiler = new Profiler();
	}
	
	public static PathFinderI Instance( MaterModel m ){
		return _instance == null? new PathFinder( m ) : _instance;
	}

	public ArrayList<Patch> search( City goal, Point curPos ){
		//profiler.startProfiling( labelTotal );
		successors.clear();
		nodeCurrent = null;
		tempPathNode = null;
		closedSet.clear();
		openSet.clear();
		pQueue.clear();
		this.goal = goal;
		
		//MaterModel.pError(" to "+ goal.getId() );
		//MaterModel.pError("BEGGINING PATH SEARCH from "+ curPos.toString() +" to "+ goal.getCenter().toString());
		
		Patch pStart = model.getPatchAt( curPos );
		PathNode nodeStart = new PathNode( pStart, null );
		openSet.put( pStart , nodeStart );
		pQueue.offer( nodeStart );
		
		while( !openSet.isEmpty() ){
			//profiler.startProfiling( labelMin );
			nodeCurrent = pQueue.poll();
			//profiler.stopProfiling( labelMin );
			
			openSet.remove( nodeCurrent.node );
			
			if( nodeCurrent.node.getCity() != null && nodeCurrent.node.getXY().equals( goal.getCenter() ) ){
				//MaterModel.pError("RETURNING PATH !");
				/*ArrayList<Patch> a = getSearchResult( nodeCurrent );
				lengthSum += a.size();
				iterationSum += c;
				numPaths++;
				*/
				//profiler.stopProfiling( labelTotal );
				//profiler.printResults();
				return getSearchResult( nodeCurrent );
			}
			
			//profiler.startProfiling( labelsuc );
			getSuccessors( nodeCurrent, successors );
			//profiler.stopProfiling( labelsuc );
			
			tempPathNode = null;
			//profiler.startProfiling( labelFilter );
			//TODO: Aqui pode permitir reabrir closeSet, caminhos mais suaves, pior performance
//			for( PathNode nodeSuccessor : successors ){
//				tempPathNode = openSet.get( nodeSuccessor.node );
//				if( !closedSet.containsKey( nodeSuccessor.node ) &&
//					   (tempPathNode == null || 
//					   tempPathNode.getFn() > nodeSuccessor.getFn())){
//					
//					//tempPathNode = closedSet.get( nodeSuccessor.node );
//					//if(  tempPathNode == null || 
//						//	   tempPathNode.getFn() > nodeSuccessor.getFn() ){
//						//Remove occurences of node_successor from OPEN and CLOSED
//								   //nao e preciso remover pq vao ser recolocados outra vez e faz overwrite
//						//openSet.remove( nodeSuccessor.node );
//						//closedSet.remove( nodeSuccessor.node );
//						//nodeSuccessor.setParent( nodeCurrent );
//						
//						openSet.put( nodeSuccessor.node, nodeSuccessor );
//						//orderedV.add( nodeSuccessor );
//						pQueue.offer( nodeSuccessor );
//					//}
//				}
//			}
			for( PathNode nodeSuccessor : successors ){
				tempPathNode = openSet.get( nodeSuccessor.node );
				if( tempPathNode == null || 
					   tempPathNode.fn > nodeSuccessor.fn ){
					
					tempPathNode = closedSet.get( nodeSuccessor.node );
					if(  tempPathNode == null || 
							   tempPathNode.fn > nodeSuccessor.fn ){
						//Remove occurences of node_successor from OPEN and CLOSED
								   //nao e preciso remover pq vao ser recolocados outra vez e faz overwrite
						//openSet.remove( nodeSuccessor.node );
						closedSet.remove( nodeSuccessor.node );
						//nodeSuccessor.setParent( nodeCurrent );
						
						openSet.put( nodeSuccessor.node, nodeSuccessor );
						//orderedV.add( nodeSuccessor );
						pQueue.offer( nodeSuccessor );
					}
				}
			}
			//profiler.stopProfiling( labelFilter );
			closedSet.put( nodeCurrent.node, nodeCurrent );
		}		
		System.out.print("PF returning null!... ");
		return null;
	}

	private void getSuccessors( PathNode n1, ArrayList<PathNode> r ) {
		//Patch start = p.getNode2();
		r.clear();
		PathNode n2 = null;
		Patch pn1 = n1.node;
		Vector<Patch> neighbors = 
			//model.getSpace().getMooreNeighbors( pn1.getX(), pn1.getY() ,false);
			model.getSpace().getVonNeumannNeighbors( pn1.getX(), pn1.getY() ,false);
		
		for( Patch p : neighbors ){
			n2 = new PathNode( p, n1 );
			r.add( n2 );
		}	
	}
	
	/*private void makeStartQueue(Point curPos, City goal ) {
		openSet.clear();
		Patch n1 = model.getObs().getPatchAt( curPos );
		double hn = 0;
		double gn = 0;
		//Path path = null;
		for( int dy = -1;  dy < 2 ; dy++ )
			for( int dx = -1;  dx < 2 ; dx++ )
				if( !(dx==0 && dy==0)  &&
						curPos.x + dx < 2400 &&
						curPos.y + dy < 1200 &&
						curPos.y + dy >= 0 &&
						curPos.x + dx >= 0 ){
					Patch n2 = model.getObs().getPatchAt( curPos.x + dx, curPos.y + dy );
					//path = new Path(p,
					//				model.getObs().getPatchAt( curPos.x + dx, curPos.y + dy ),
					//				null);
					//path.setHn( getHeuristic( path.node2, goal ) );
					//path.setGn( getCost( path ) );
					//temp.add( path );
					hn = getHeuristic( path.node2, goal );
				}
		return temp;
	}
*/
	/*private ArrayList makeStartQueue(Point curPos, City goal ) {
		temp.clear();
		Patch p = model.getObs().getPatchAt( curPos );
		Path path = null;
		for( int dy = -1;  dy < 2 ; dy++ )
			for( int dx = -1;  dx < 2 ; dx++ )
				if( !(dx==0 && dy==0)  &&
						curPos.x + dx < 2400 &&
						curPos.y + dy < 1200 &&
						curPos.y + dy >= 0 &&
						curPos.x + dx >= 0 ){
					path = new Path(p,
									model.getObs().getPatchAt( curPos.x + dx, curPos.y + dy ),
									null);
					path.setHn( getHeuristic( path.node2, goal ) );
					path.setGn( getCost( path ) );
					temp.add( path );
				}
		return temp;
	}*/
	
	private ArrayList<Patch> getSearchResult(PathNode p ) {
		ArrayList<Patch> a = new ArrayList<Patch>();
		a.add( p.node );
		PathNode temp = p.parent;
		while( temp != null ){
			a.add( 0 , temp.node );
			temp = temp.parent;
		}
		return a;
	}

	/*private ArrayList getSearchResult(Path p ) {
		ArrayList a = new ArrayList();
		a.add( p.node2 );
		Path temp = p.parent;
		
		while( temp != null ){
			a.add( 0 , temp.node2 );
			temp = temp.parent;
		}
		return a;
	}*/
	
	class PathNode implements Comparable{
		
		private Patch node;
		private double gn = 0;
		private double hn = 0;
		private double fn = 0;
		private PathNode parent;
		
		PathNode( Patch node, PathNode parent ){
			this.node = node;
			this.parent = parent;
			this.gn = getCost();
			this.hn = getHeuristic( node );
			this.fn = gn + hn;
		}
		
		PathNode(){}
		
		private double getCost() {
			//para ja o custo e igual para todas ( 1 );
			double pcost = ( parent == null? 0: parent.gn );
			return pcost + node.getCost( parent==null? null:parent.node, false );
		}
		
		private double getHeuristic( Patch n ) {
			double h = 0;
			
			/*
			h = Math.sqrt( Math.pow( goal.getCenter().x - n.getX(), 2) +
							Math.pow( goal.getCenter().y - n.getY(), 2 ) );
			h *= model.getBaseCost();
			*/
			/*
			 * h_diagonal(n) = min(abs(n.x-goal.x), abs(n.y-goal.y))
			 * h_straight(n) = (abs(n.x-goal.x) + abs(n.y-goal.y))
			 * h(n) = D2 * h_diagonal(n) + D * (h_straight(n) - 2*h_diagonal(n)))
			 */
			/*
			int n_diagonal = Math.min( 
					Math.abs( n.getX() - goal.getCenter().x ) , 
					Math.abs( n.getY() - goal.getCenter().y ));
			int n_straight = Math.abs( n.getX() - goal.getCenter().x ) + 
							Math.abs( n.getY() - goal.getCenter().y );
				
			h = model.getBaseCost() * Math.sqrt( 2 ) * n_diagonal + 
				model.getBaseCost() * ( n_straight - 2 * n_diagonal );
			*/
			h = 
				/*model.getBaseCost()*/1 * 
				(Math.abs( n.getX() - goal.getCenter().x ) + 
				Math.abs( n.getY() - goal.getCenter().y ));
			
			h *= (1 + 0.001);
			return h;
		}

		public int compareTo(Object arg0) {
			int i = 0;
			
			if( this.fn >= ((PathNode)arg0).fn )
				i = 1;
			else
				i = -1;
			
			return new Integer(i);
		}
	}
	
	class NodeComp implements Comparator<PathNode>{

		public int compare(PathNode arg0, PathNode arg1) {
			int i = 0;
			
			if( arg0.fn > arg1.fn )
				i = 1;
			else if( arg0.fn < arg1.fn )
				i = -1;
			
			return i;
		}		
	}
	
	public static void printStats(){
		if( numPaths > 0 ){
			MaterModel.pError("Medium Path LEngth: "+ (lengthSum/numPaths));
			MaterModel.pError("Medium Iteration Number: "+ (iterationSum/numPaths));
		}			
	}
}

