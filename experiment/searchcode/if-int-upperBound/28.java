package edu.psu.chemxseer.structure.setcover.featureGenerator;

import de.parmol.graph.Graph;
import edu.psu.chemxseer.structure.iso.FastSU;
import edu.psu.chemxseer.structure.setcover.featureGenerator.IFeatureSetConverter.FeatureSetType;
import edu.psu.chemxseer.structure.setcover.interfaces.IBranchBoundCalculator;
import edu.psu.chemxseer.structure.setcover.maxCoverStatus.ICoverStatusStream;
import edu.psu.chemxseer.structure.setcover.sets.ICoverSet_FeatureWrapper;
import edu.psu.chemxseer.structure.subsearch.Impl.indexfeature.NoPostingFeatures;

/**
 * Pre-processing based Branch&Bound calculator. 
 * 
 * @author dayuyuan
 *
 */
public class BranchBoundCalculator2 implements IBranchBoundCalculator{

	private int[] containmentCounter; // for subgraph search, A[qID] = # of graphs containing qID
									// for supergraph search, A[gID] = # of queries containing gID
	private ICoverStatusStream setCoverStatus;
	private int classOneSize;
	private int classTwoSize;
	private FeatureSetType type;
	
	
	/**
	 * No pre-processing is needed, since the smaller graphs [either queries for subgraph search, or database graphs for
	 * supergraph search], are all mined as frequent subgraph of the bigger graph. 
	 * So the frequency is already known.
	 * @param setCoverStatus
	 * @param type
	 * @param classOneSize
	 * @param classTwoSize
	 * @param smallGraphs
	 */
	public BranchBoundCalculator2(ICoverStatusStream setCoverStatus,
			FeatureSetType type, int classOneSize, int classTwoSize, NoPostingFeatures smallGraphs){
		this.setCoverStatus = setCoverStatus;
		this.classOneSize = classOneSize;
		this.classTwoSize = classTwoSize;
		this.type = type;
		this.preProcessing(smallGraphs);
	}
	
	private void preProcessing(NoPostingFeatures smallGraphs){
		this.containmentCounter = new int[smallGraphs.getfeatureNum()];
		for(int i = 0; i< containmentCounter.length; i++)
			this.containmentCounter[i] = smallGraphs.getFeature(i).getFrequency();
	}
	
	/**
	 * Pre-processing is needed, to find for each small graphs, the number of big graphs containing it. 
	 * @param setCoverStatus
	 * @param type
	 * @param classOneSize
	 * @param classTwoSize
	 * @param graphs
	 * @param queries
	 */
	public BranchBoundCalculator2(ICoverStatusStream setCoverStatus, 
			FeatureSetType type, int classOneSize, int classTwoSize, Graph[] graphs, Graph[] queries){
		this.setCoverStatus = setCoverStatus;
		this.classOneSize = classOneSize;
		this.classTwoSize = classTwoSize;
		this.type = type;
		this.preProcessing(graphs, queries);
	}
	
	private void preProcessing (Graph[] graphs, Graph[] queries) {
		FastSU fastSu = new FastSU();
		switch(this.type){
			case subSearch:
				// In this case: upperBoundCounter[q_i] = D_1(q_i)
				this.containmentCounter = new int[this.classTwoSize];
				for(int i = 0; i< queries.length; i++){
					this.containmentCounter[i] =0;
					for(int j = 0; j< graphs.length; j++){
						if(graphs[j].getEdgeCount() <= queries[i].getEdgeCount() || graphs[j].getNodeCount() < queries[i].getNodeCount())
							continue;
						else if(fastSu.isIsomorphic(queries[i], graphs[j])) // query is subgraph of database graphs
							containmentCounter[i]++;
					}
				}
				break;
			case supSearch:
				// In this case: upperBoundCounter[g_i] = Q_2(g_i) 
				this.containmentCounter = new int[this.classOneSize];
				for(int i = 0; i< graphs.length; i++){
					this.containmentCounter[i] = 0;
					for(int j = 0; j< queries.length; j++)
						if(queries[j].getEdgeCount() <= graphs[i].getEdgeCount() || queries[j].getNodeCount() < graphs[i].getNodeCount())
							containmentCounter[i]++;
						else if(!fastSu.isIsomorphic(graphs[i], queries[j]))
							containmentCounter[i]++;
				}
				break;
			case classification:
				this.containmentCounter = null;
				break;
			default: 
				break;
		}
	}
	
	/**
	 * Given the Feature, calculate the upperbound for the number of items oneFeature's children
	 * can cover
	 * @param oneFeature
	 * @return
	 */
	private int getUpperBoundSubSearch(ICoverSet_FeatureWrapper oneFeature, short minFeatureID){

		int[] containedQueries = oneFeature.containedQueryGraphs();
		int upperBound = 0;
		int maxDirect = 0;
		int[] coveredCount = setCoverStatus.getCoveredCountExceptMin(minFeatureID, containedQueries);
		for(int i=0; i< containedQueries.length; i++){
			int localDirect = this.classOneSize-coveredCount[i];
			if(localDirect > maxDirect)
				maxDirect = localDirect; // TODO: this is not correct, since I did not multiply with the frequency
			upperBound += localDirect-this.containmentCounter[containedQueries[i]];
		}
		return upperBound + maxDirect;
	}
	
	/**
	 * Given the Feature, calculate the upperbound for the number of items oneFeature's children
	 * can cover
	 * @param oneFeature
	 * @return
	 */
	private int getUpperBoundSupSearch(ICoverSet_FeatureWrapper oneFeature, short minFeatureID){
		int[] containedGraphs = oneFeature.containedDatabaseGraphs();
		int upperBound = 0;
		int[] coveredCount = setCoverStatus.getCoveredCountExceptMin(minFeatureID, containedGraphs);
		for(int i = 0; i< containedGraphs.length; i++){
			int gID = containedGraphs[i];
			upperBound += this.classTwoSize - this.containmentCounter[gID]-
				coveredCount[i];
		}
		return upperBound;	
	}
	
	/**
	 * Return the UpperBound of the branch starting from the feature "oneFeature"
	 * @param oneFeature
	 * @return
	 */
	public int getUpperBound(ICoverSet_FeatureWrapper oneFeature, short minFeatureID){
		switch (this.type){
		case subSearch:
			return this.getUpperBoundSubSearch(oneFeature, minFeatureID);
		case supSearch:
			return this.getUpperBoundSupSearch(oneFeature, minFeatureID);
		case classification:
			System.out.println("Do not supply classification");
			break;
		default:
			System.out.println("Wrong Input");
		}
		return 0;
	}

}

