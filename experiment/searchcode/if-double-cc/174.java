package org.omelogic.tome;
/*
 *      GraphStatsUtils.java
 *      
 *      Copyright 2007 Ajish George <ajish@hocuslocus.com>
 *      
 *      This program is free software; you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation; either version 2 of the License, or
 *      (at your option) any later version.
 *      
 *      This program is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *      
 *      You should have received a copy of the GNU General Public License
 *      along with this program; if not, write to the Free Software
 *      Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 *      MA 02110-1301, USA.
 */

import prefuse.data.column.Column;
import prefuse.data.Table;

import edu.uci.ics.jung.algorithms.cluster.*;
import edu.uci.ics.jung.graph.*;
import edu.uci.ics.jung.graph.decorators.*;
import edu.uci.ics.jung.graph.filters.*;
import edu.uci.ics.jung.graph.filters.impl.*;
import edu.uci.ics.jung.graph.impl.*;
import edu.uci.ics.jung.random.generators.*;
import edu.uci.ics.jung.statistics.*;
import edu.uci.ics.jung.exceptions.*;
import edu.uci.ics.jung.statistics.*;
import edu.uci.ics.jung.utils.*;
import java.util.*;

import javax.swing.ProgressMonitor;


public class TomeGraphUtilities {
	
	public static String CLUSTERING_COEFFICIENT = "ClusteringCoefficient";
	public static String AVERAGE_PATH_LENGTH = "AveragePathLength";
	public static String AVERAGE_DEGREE = "AverageDegree";
	public static String TRIM_CONNECTED = "TrimConnected";
	public static String REM_SELF_LOOPS = "RemSelfLoops";
	public static String TRIM_SPIKES = "TrimSpikes";
	public static String NUM_INITIAL_NODE_LIST = "InitialNodeList";
	public static String INITIAL_IMPORT_STRICT = "InitialImportStrict";
	public static String NUM_INITIAL_NODES = "InitialNodes";
	public static String NUM_INITIAL_EDGES = "InitialEdges";

	public static final class ControlTableCols
	{
		public static final int CLUSTERING_COEFFICIENT = 0;
		public static final int AVERAGE_PATH_LENGTH = 1;
		public static final int AVERAGE_DEGREE = 2;
	}
	
	public static Graph filterGraph( Graph g , TomeEdgeIDFilter vFilt)
	{
		if (vFilt == null){
			return g;
		}

		UnassembledGraph filteredUnGraph = vFilt.filter(g);
		Graph filteredGraph = (Graph)filteredUnGraph.assemble();
		filteredUnGraph = DropSoloNodesFilter.getInstance().filter(filteredGraph);
		filteredGraph = (Graph)filteredUnGraph.assemble();
		filteredGraph.importUserData(g);
		return filteredGraph;
	}
	

	
	public static ArchetypeGraph trimConnected( ArchetypeGraph g )
	{
		g.setUserDatum(TRIM_CONNECTED, new Boolean(true), UserData.SHARED);
		WeakComponentClusterer clustMaker = new WeakComponentClusterer();
		ClusterSet clusts = clustMaker.extract( g );
		clusts.sort();
		ArchetypeGraph filteredGraph = (ArchetypeGraph) clusts.getClusterAsNewSubGraph(0);
		filteredGraph.importUserData(g);
		return filteredGraph;
		//return removeSelfLoops((UndirectedSparseGraph) clusts.getClusterAsNewSubGraph(0));
		
	}
	
	public static Graph removeSelfLoops(Graph g)
	{
		g.setUserDatum(REM_SELF_LOOPS, new Boolean(true), UserData.SHARED);
		Filter selfLoopFilter = new GeneralEdgeAcceptFilter(){
			public boolean acceptEdge( Edge e ) {
				return (e.getIncidentVertices().size()>1);
			}
			public String getName(){
				return "Self Loop Removal";
			}
		};
		Graph filteredGraph = (Graph)selfLoopFilter.filter(g).assemble();
		filteredGraph.importUserData(g);
		return filteredGraph;
		//return trimSpikes((UndirectedSparseGraph)(selfLoopFilter.filter(g).assemble()));
	}		



	public static UndirectedSparseGraph trimSpikes( ArchetypeGraph g )
	{
		g.setUserDatum(TRIM_SPIKES, new Boolean(true), UserData.SHARED);
		UndirectedSparseGraph gClone = (UndirectedSparseGraph)g.copy();
		
		Iterator<Vertex> vIter = gClone.getVertices().iterator();
		Vertex v;
		int neighborCount;
		while( vIter.hasNext() ){
			v = vIter.next();
			neighborCount = v.degree();
			if(v.isNeighborOf(v)){
				//don't count self-loop
				neighborCount--;
			}
			if (neighborCount <= 1){
				//remove the vertex
				gClone.removeVertex(v);
				//reset the traversal
				vIter = gClone.getVertices().iterator();
			}	
		}
		//gClone.importUserData(g);		
		return gClone;
	}			
			
			
	public static void addGraphStatistics( UndirectedSparseGraph g )
	{
		HashMap<String, Double> graphStats = new HashMap<String, Double>();
		System.out.println("Calculating clustering coeffs... \n");
		Map clustCoeffs = GraphStatistics.clusteringCoefficients( g );
		System.out.println("Calculating average dists... \n");
		Map avgDists = GraphStatistics.averageDistances(g);
		System.out.println("Done.\n");
		int numVertices = g.numVertices();
		Iterator vIter = g.getVertices().iterator();
		double clustCoeffSum = 0.;
		double avgDistSum = 0.;
		double cc, ad, dd;
		TomeVertex v;
		//SelfLoopEdgePredicate selfLoopCheck = new SelfLoopEdgePredicate();
		while( vIter.hasNext() ){
			v = (TomeVertex)vIter.next();
			dd = new Double(v.numNeighbors());
			cc = ((Double)clustCoeffs.get(v)).doubleValue();
			ad = ((Double)avgDists.get(v)).doubleValue();
			v.setClusteringCoefficient(cc);
			v.setAverageDistance(ad);
			v.setDegree(dd);
			clustCoeffSum += cc;
			avgDistSum += ad;
		} 
		
		double avgCC = clustCoeffSum/numVertices;
		double avgAD = avgDistSum/numVertices;
		double avgDD = 2* g.numEdges() / g.numVertices();
		
		
		graphStats.put(CLUSTERING_COEFFICIENT, new Double(avgCC));
		graphStats.put(AVERAGE_PATH_LENGTH, new Double(avgAD));
		graphStats.put(AVERAGE_DEGREE, new Double(avgDD));
		
		g.setUserDatum("GraphStatistics", graphStats, UserData.SHARED);
		
	}
	
	

	public static void addConnectionControlStats( UndirectedSparseGraph g, int nControls) throws Exception
	{
		Table connCtrls = new Table();
		connCtrls.addColumn( CLUSTERING_COEFFICIENT, double.class );
		connCtrls.addColumn( AVERAGE_PATH_LENGTH, double.class );
		connCtrls.addColumn( AVERAGE_DEGREE, double.class );
		connCtrls.addRows(nControls);
		
		int numInitialEdges;
		try{
			numInitialEdges = ((Integer)g.getUserDatum(NUM_INITIAL_EDGES)).intValue();	
		}catch(Exception e){
			numInitialEdges = g.numEdges();
		}
		
		int numInitialVertices;
		try{
			numInitialVertices = ((Integer)g.getUserDatum(NUM_INITIAL_NODES)).intValue();
		}catch(Exception e){
			numInitialVertices = g.numVertices();
		}

		boolean shouldTrimConnected = true;
		boolean shouldRemSelfLoops = false;
		boolean shouldTrimSpikes = false;

		try{
			shouldTrimConnected = ((Boolean)g.getUserDatum(TRIM_CONNECTED)).booleanValue();
		}catch(Exception e){}
		try{	
			shouldRemSelfLoops = ((Boolean)g.getUserDatum(REM_SELF_LOOPS)).booleanValue();
		}catch(Exception e){}
		try{	
			shouldTrimSpikes = ((Boolean)g.getUserDatum(TRIM_SPIKES)).booleanValue();
		}catch(Exception e){}
		
		if (numInitialVertices == 0){
			throw new Exception("TomeGraphUtilities.addConnectionControlStats : found numIntialVertices of ZERO!");
		}
		if (numInitialEdges == 0){
			throw new Exception("TomeGraphUtilities.addConnectionControlStats : found numIntialEdges of ZERO!");
		}

		SimpleRandomGenerator randomGen = new SimpleRandomGenerator( numInitialVertices, numInitialEdges );
		ArchetypeGraph randGraph;
		int k;
		for (k = 0; k < nControls; k++)
		{
			if (k % 10 == 0) System.out.println(k+"% done");
			/*UndirectedSparseGraph randGraph = new UndirectedSparseGraph();
			UndirectedSparseVertex[] verts = new UndirectedSparseVertex[numInitialVertices];
			for (int i = 0; i < numInitialVertices; i++){
				UndirectedSparseVertex newVert = new UndirectedSparseVertex();
				randGraph.addVertex(newVert);
				verts[i] = newVert;
			}
				
			int currEdge =0;
			
			while (currEdge < numInitialEdges)
			{
				int rand1 = (int)(Math.random() * numInitialVertices);
				int rand2;
				do{
				 	rand2 = (int)(Math.random() * numInitialVertices);
				}while(rand2 == rand1);
				try
				{
					randGraph.addEdge( new UndirectedSparseEdge( verts[rand1], verts[rand2] ));
					currEdge++;
				}catch(ConstraintViolationException e ){
				}
			}
			*/
			randGraph = randomGen.generateGraph();
			
			if (shouldTrimConnected) randGraph = trimConnected(randGraph);
			if (shouldRemSelfLoops) randGraph = removeSelfLoops((Graph)randGraph);
			if (shouldTrimSpikes) randGraph = trimSpikes(randGraph);
			
			double avgCC = 0, avgAD = 0, avgDD = 0, clustCoeffSum =0, avgDistSum =0;
			Object[] clustCoeffs;
			Object[] avgDists;
			int coeffIndex;
			if (randGraph.numVertices() > 0 && randGraph.numEdges() > 0)
			{
				clustCoeffs = GraphStatistics.clusteringCoefficients( randGraph ).values().toArray();
				avgDists = GraphStatistics.averageDistances(randGraph).values().toArray();
				clustCoeffSum = 0.;
				avgDistSum = 0.;

				for(coeffIndex =0; coeffIndex < clustCoeffs.length; coeffIndex++){
					clustCoeffSum += ((Double)clustCoeffs[coeffIndex]).doubleValue();
					avgDistSum += ((Double)avgDists[coeffIndex]).doubleValue();
				} 

				avgCC = clustCoeffSum/randGraph.numVertices();
				avgAD = avgDistSum/randGraph.numVertices();
				avgDD = 2* randGraph.numEdges() / randGraph.numVertices();
			}	
			connCtrls.setDouble(k , ControlTableCols.CLUSTERING_COEFFICIENT , avgCC);
			connCtrls.setDouble(k , ControlTableCols.AVERAGE_PATH_LENGTH , avgAD);
			connCtrls.setDouble(k , ControlTableCols.AVERAGE_DEGREE , avgDD);
			//System.out.println("cc: "+avgCC+"\tad:"+avgAD);
			
		}

		g.setUserDatum("ConnectionControls", connCtrls, UserData.SHARED);
				
	}
	
	
		

	public static void addSelectionControlStats( UndirectedSparseGraph g, UndirectedSparseGraph backingGraph, int nControls) throws Exception
	{
		
		
		Table selCtrls = new Table();
		selCtrls.addColumn( CLUSTERING_COEFFICIENT, double.class );
		selCtrls.addColumn( AVERAGE_PATH_LENGTH, double.class );
		selCtrls.addColumn( AVERAGE_DEGREE, double.class );
		selCtrls.addRows(nControls);
		
		int numInitialVertices;
		boolean importStrict = true;
		try{
			numInitialVertices = ((Integer)g.getUserDatum(NUM_INITIAL_NODE_LIST)).intValue();
		}catch(Exception e){
			numInitialVertices = g.numVertices();
		}
		try{
			importStrict = ((Boolean)g.getUserDatum(INITIAL_IMPORT_STRICT)).booleanValue();
		}catch(Exception e){}

		boolean shouldTrimConnected = true;
		boolean shouldRemSelfLoops = false;
		boolean shouldTrimSpikes = false;

		try{
			shouldTrimConnected = ((Boolean)g.getUserDatum(TRIM_CONNECTED)).booleanValue();
		}catch(Exception e){}
		try{
			shouldRemSelfLoops = ((Boolean)g.getUserDatum(REM_SELF_LOOPS)).booleanValue();
		}catch(Exception e){}
		try{
			shouldTrimSpikes = ((Boolean)g.getUserDatum(TRIM_SPIKES)).booleanValue();
		}catch(Exception e){}

		
		if (numInitialVertices == 0){
			throw new Exception("TomeGraphUtilities.addConnectionControlStats : found numIntialVertices of ZERO!");
		}
		if (g.numEdges() == 0){
			throw new Exception("TomeGraphUtilities.addConnectionControlStats : found numIntialEdges of ZERO!");
		}
		
		//int numVsToRemove = backingGraph.numVertices() - numInitialVertices;
		
		ArrayList<String> allIDs = new ArrayList<String>();
		Iterator backingGraphIter = backingGraph.getVertices().iterator();;
		while ( backingGraphIter.hasNext() )
		{
			allIDs.add(     ( (TomeVertex)backingGraphIter.next() ).getID()     );
		}
		
		Set<String> randomSet;
		ArchetypeGraph randGraph;
		for (int k = 0; k < nControls; k++)
		{

			if (k % 10 == 0) System.out.println(k+"% done");
			randomSet = new HashSet<String>();
			int rand;
			while( randomSet.size() < numInitialVertices )
			{
				rand = (int)(Math.random() * backingGraph.numVertices());
				randomSet.add( allIDs.get(rand) );
			}

			randGraph = filterGraph( backingGraph, new TomeEdgeIDFilter(randomSet, importStrict) );
			
			if (shouldTrimConnected) randGraph = trimConnected(randGraph);
			if (shouldRemSelfLoops) randGraph = removeSelfLoops((Graph)randGraph);
			if (shouldTrimSpikes) randGraph = trimSpikes(randGraph);
			
			double avgCC = 0, avgAD = 0, avgDD = 0, clustCoeffSum =0, avgDistSum =0;
			Object[] clustCoeffs;
			Object[] avgDists;
			int coeffIndex;
			if (randGraph.numVertices() > 0 && randGraph.numEdges() > 0)
			{
				clustCoeffs = GraphStatistics.clusteringCoefficients( randGraph ).values().toArray();
				avgDists = GraphStatistics.averageDistances(randGraph).values().toArray();
				clustCoeffSum = 0.;
				avgDistSum = 0.;

				for(coeffIndex =0; coeffIndex < clustCoeffs.length; coeffIndex++){
					clustCoeffSum += ((Double)clustCoeffs[coeffIndex]).doubleValue();
					avgDistSum += ((Double)avgDists[coeffIndex]).doubleValue();
				} 

				avgCC = clustCoeffSum/randGraph.numVertices();
				avgAD = avgDistSum/randGraph.numVertices();
				avgDD = 2* randGraph.numEdges() / randGraph.numVertices();
			}	
			selCtrls.setDouble(k , ControlTableCols.CLUSTERING_COEFFICIENT , avgCC);
			selCtrls.setDouble(k , ControlTableCols.AVERAGE_PATH_LENGTH , avgAD);
			selCtrls.setDouble(k , ControlTableCols.AVERAGE_DEGREE , avgDD);
			//System.out.println("cc: "+avgCC+"\tad:"+avgAD);
			
		}

		g.setUserDatum("SelectionControls", selCtrls, UserData.SHARED);
				
	}

}

