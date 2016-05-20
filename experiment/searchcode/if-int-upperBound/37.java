package edu.psu.chemxseer.structure.setcover.featureGenerator;

import edu.psu.chemxseer.structure.setcover.featureGenerator.IFeatureSetConverter.FeatureSetType;
import edu.psu.chemxseer.structure.setcover.interfaces.IBranchBoundCalculator;
import edu.psu.chemxseer.structure.setcover.maxCoverStatus.ICoverStatusStream;
import edu.psu.chemxseer.structure.setcover.sets.ICoverSet_FeatureWrapper;


/**
 * Simple Calculation of the Branch Upper Bound
 * @author dayuyuan
 *
 */
public class BranchBoundCalculator implements IBranchBoundCalculator{
	private ICoverStatusStream setCoverStatus;
	private int classOneSize;
	private int classTwoSize;
	private FeatureSetType type;
	
	/**
	 * Constructor
	 * @param setCoverStatus
	 * @param type
	 * @param classOneSize
	 * @param classTwoSize
	 */
	public BranchBoundCalculator(ICoverStatusStream setCoverStatus, FeatureSetType type, int classOneSize, int classTwoSize){
		this.setCoverStatus = setCoverStatus;
		this.classOneSize = classOneSize;
		this.classTwoSize = classTwoSize;
		this.type = type;
	}
	

	private int getUpperBoundSubSearch(ICoverSet_FeatureWrapper oneFeature, short minFeatureID){
		long start = System.currentTimeMillis();
		int[] containedQueries = oneFeature.containedQueryGraphs();
		int upperBound = 0;
		int maxDirect = 0;
		int[] coveredCount = setCoverStatus.getCoveredCountExceptMin(minFeatureID, containedQueries);
		for(int i=0; i< containedQueries.length; i++){
			int localDirect = classOneSize-coveredCount[i];
			if(localDirect > maxDirect)
				maxDirect = localDirect; 
			// TODO: this is not correct, since I did not multiply with the frequency
			upperBound += localDirect;
		}
		System.out.println("UpperBound: " + (System.currentTimeMillis()-start));
		return upperBound + maxDirect;
	}

	private int getUpperBoundSupSearch(ICoverSet_FeatureWrapper oneFeature, short minFeatureID){
		int[] containedGraphs = oneFeature.containedDatabaseGraphs();
		int upperBound = 0;
		int[] coveredCount = setCoverStatus.getCoveredCountExceptMin(minFeatureID, containedGraphs);
		for(int i = 0; i< containedGraphs.length; i++){
			upperBound += classTwoSize-coveredCount[i];
		}
		
		return upperBound;	
	}
	
	/**
	 * This is a little bit complicated:
	 * (1) First divide all item pairs to different segments
	 * (1.1) Given a query "qID", find all the "gID" not covered by "qID". 
	 * (1.2) Then for each of the "gID", find the "qID" not covered by "gID"
	 * (1.3) Then we get a partition of "gID" * "qID". 
	 * (2) For each of the equal class, calculate the max(ad, bc), and return
	 * @param oneFeature
	 * @return
	 */
	private int getUpperBoundClassification(ICoverSet_FeatureWrapper oneFeature, int minFeatureID){
		//TODO: Little Bit Challenging, leave for later
		return 0;
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
			return this.getUpperBoundClassification(oneFeature, minFeatureID);
		default:
			System.out.println("Wrong Input");
		}
		return 0;
	}

}

