package edu.psu.chemxseer.structure.setcover.impl;

import edu.psu.chemxseer.structure.setcover.IO.IInputStream;
import edu.psu.chemxseer.structure.setcover.IO.Input_DFSStream;
import edu.psu.chemxseer.structure.setcover.interfaces.IBranchBoundCalculator;
import edu.psu.chemxseer.structure.setcover.interfaces.IMaxCoverSolver;
import edu.psu.chemxseer.structure.setcover.maxCoverStatus.ICoverStatusStream;
import edu.psu.chemxseer.structure.setcover.sets.ICoverSet_FeatureWrapper;
import edu.psu.chemxseer.structure.util.MemoryConsumptionCal;

/**
 * The Implementation of the Streaming-Set-Cover Algorithms
 * Which use the streaming model, assuming # of items are already known, butthe sets comes one after another
 * The memory is big enough to hold "K" sets, but not more. 
 * When a new CoverSet comes from the stream, the algorithm measures the contribution of the new CoverSet
 * Decide whether to select it or not. 
 * @author dayuyuan
 *
 */
public class MaxCoverSolver_Stream implements IMaxCoverSolver{
	private IInputStream input;
	private ICoverStatusStream status;
	private IBranchBoundCalculator bbCalculator;
	private double delta; // swap parameter
	private int swapType; // [type = 0, B_1 Swap, type =1 T_current swap]
	
	/**
	 * Construct a Stream Max-Coverage Solver
	 * @param input: Input_DFSStream, the stream input
	 * @param delta: control the swap criterion
	 * @param branchBound: can be null if no branch& bound is needed
	 * @param type: type = 0 [B_1 swap], type = 1 [T_current swap]
	 */
	public MaxCoverSolver_Stream(IInputStream input, ICoverStatusStream status, 
			double delta, IBranchBoundCalculator branchBound, int swapType){
		this.input = input;
		this.delta = delta;
		this.bbCalculator = branchBound;
		this.swapType = swapType;
		this.status = status;
	}
	@Override
	public int[] runGreedy(int K) {
		long start = System.currentTimeMillis();
		double[] avgMem = new double[1];
		avgMem[0] = 0;
		
		//1. Select the First K sets
		ICoverSet_FeatureWrapper oneSet = null;
		for(int i = 0; i< K; i++){
			oneSet = input.nextSet();
			if(oneSet == null){
				System.out.println("Error in Stream_SetCover:run, insufficient sets");
				return null;
			}
			this.status.addNewSet(oneSet);
		}
		
		//2. Start Swapping The Sets:
		this.doSwap(K, avgMem);
		
		
		System.out.println("Average Space Complexity (MB): " + avgMem[0]);
		System.out.println("Total Time for Streamining Mining: " + (System.currentTimeMillis()-start));
		
		// return results
		ICoverSet_FeatureWrapper[] selectedSets = this.status.getSelectedSets();
		int[] results= new int[K];
		for(int i = 0; i< K; i++)
			results[i] = selectedSets[i].getFetureID();
		return results;
	}
	
	/**
	 * This is optimized for the status implementation of 
	 * Do swap if and only if the difference is huge. 
	 * @param K
	 */
	private void doSwap(int K, double[] avgMem){
		//STATISTICS ONLY
		int totalIteration = 0;
		int swapIteration = 0;
		//END OF STATISTICS
		double typeScore = 0;
		int[] minDecrease = new int[1];
		short minSetID = this.status.leastCoverSet(minDecrease);
		
		int totalCoveredCount = 0;
		if(this.swapType == 1){
			totalCoveredCount = this.status.getCoveredCount();
			typeScore = (delta*totalCoveredCount)/K;
		}
		else if(this.swapType == 0)
			typeScore = delta * minDecrease[0];

		ICoverSet_FeatureWrapper oneSet = null;
		int i = 0;
		System.out.println(totalIteration);
		while((oneSet = input.nextSet())!=null){
			totalIteration ++;
			// Test whether we should do the swap
			int increase = this.status.getGain(oneSet, minSetID);
			int difference = increase-minDecrease[0];
			boolean swap = false;
			if(difference > typeScore)
				swap = true;
			// Do the swap
			if(swap){
				swapIteration++;
				this.status.swap(minSetID, oneSet);
				// update the minDecrease, minSet, totalCoveredCount
				minSetID = status.leastCoverSet(minDecrease);
				if(this.swapType == 1){
					totalCoveredCount = totalCoveredCount + difference;
					typeScore = (delta*totalCoveredCount)/K;
				}
				else if(this.swapType == 0)
					typeScore = delta * minDecrease[0];
			}
			// Branch & Bound
			else if(this.bbCalculator!=null){
				double threshold = minDecrease[0] + typeScore;
				if(this.bbCalculator.getUpperBound(oneSet, minSetID) < threshold){
					input.pruneBranch();
				}
			}
			System.out.println(totalIteration);
//			double mem =MemoryConsumptionCal.usedMemoryinMB();
//			if(i == 0)
//				avgMem[0] = mem;
//			else{
//				avgMem[0] = (i*avgMem[0] + mem)/i+1;
//			}
//			i++;
		}
		System.out.println("TotalIteration: " + totalIteration + " SwapIteration: " + swapIteration);
	}
//	/**
//	 * This is optimized for the SetCoverStatus_EWAH_Count & SetCoverStatus_Short
//	 * We first remove the least used feature, 
//	 * then try to add the new set in, if they meet the criterion. 
//	 * Since getGain(newSet, exceptForID) is not implemented
//	 * @param K
//	 */
//	private void doSwap2(int K, double[] avgMem){
//		//STATISTICS ONLY
//		int totalIteration = 0;
//		int swapIteration = 0;
//		//END OF STATISTICS
//		
//		double typeScore = 0;
//		int[] minDecrease = new int[1];
//		int minSetID = this.status.leastCoverSet(minDecrease);
//		ICoverSet_FeatureWrapper minSet = this.status.getSelectedSet(minSetID);
//	
//		int totalCoveredCount = 0;
//		if(this.swapType == 1){
//			totalCoveredCount = this.status.getCoveredCount();
//			typeScore = (delta*totalCoveredCount)/K;
//		}
//		else if(this.swapType == 0)
//			typeScore = delta * minDecrease[0];
//		//1. First Remove the minSet
//		this.status.removeSet(minSetID);
//		//2. Iterative find a succefully swap
//		ICoverSet_FeatureWrapper oneSet = null;
//		int i = 0;
//		while((oneSet = input.nextSet())!=null){
//			totalIteration++;
//			// Test whether we should do the swap
//			int increase = this.status.getGain(oneSet);
//			int difference = increase-minDecrease[0];
//			boolean swap = false;
//			if(difference > typeScore)
//				swap = true;
//			// Do the swap
//			if(swap){
//				swapIteration++;
//				this.status.addSet(oneSet, minSetID);
//				// update the minDecrease, minSet, totalCoveredCount
//				minSetID = status.leastCoverSet(minDecrease);
//				minSet = this.status.getSelectedSet(minSetID);
//				if(this.swapType == 1){
//					totalCoveredCount = totalCoveredCount + difference;
//					typeScore = (delta*totalCoveredCount)/K;
//				}
//				else if(this.swapType == 0)
//					typeScore = delta * minDecrease[0];
//				//2.1 Remove the least again
//				this.status.removeSet(minSetID);
//			}
//			// Branch & Bound
//			else if(this.bbCalculator!=null){
//				double threshold = minDecrease[0] + typeScore;
//				if(this.bbCalculator.getUpperBound(oneSet, -1) < threshold)
//					input.prunBranches();
//			}
//			double mem =MemoryConsumptionCal.usedMemoryinMB();
//			if(i == 0)
//				avgMem[0] = mem;
//			else{
//				avgMem[0] = (i*avgMem[0] + mem)/i+1;
//			}
//		}
//		//add the wrongly delete min
//		this.status.addSet(minSet, minSetID);
//		System.out.println("TotalIteration: " + totalIteration + " SwapIteration: " + swapIteration);
//	}
	
//	private void doSwap2Test(int K){
//		//STATISTICS ONLY
//		int totalIteration = 0;
//		int swapIteration = 0;
//		//END OF STATISTICS
//		
//		double typeScore = 0;
//		int[] minDecrease = new int[1];
//		int minSetID = this.status.leastCoverSet(minDecrease);
//		int testMinSetID = this.testingStatus.leastCoverSet(minDecrease);
//		if(minSetID!=testMinSetID)
//			System.out.println("Lala");
//		
//		CoverSet_FeatureWrapper minSet = this.status.getSelectedSet(minSetID);
//	
//		int totalCoveredCount = 0;
//		if(this.swapType == 1){
//			totalCoveredCount = this.status.getCoveredCount();
//			typeScore = (delta*totalCoveredCount)/K;
//		}
//		else if(this.swapType == 0)
//			typeScore = delta * minDecrease[0];
//		//1. First Remove the minSet
//		this.status.removeFeature(minSetID);
//		this.testingStatus.removeFeature(minSetID);
//		
//		//2. Iterative find a succefully swap
//		CoverSet_FeatureWrapper oneSet = null;
//		while((oneSet = input.nextSet())!=null){
//			totalIteration++;
//			// Test whether we should do the swap
//			int increase = this.status.getGain(oneSet);
//			int testIncrease = this.testingStatus.getGain(oneSet);
//			if(increase!=testIncrease)
//				System.out.println("lala2");
//			int difference = increase-minDecrease[0];
//			boolean swap = false;
//			if(difference > typeScore)
//				swap = true;
//			// Do the swap
//			if(swap){
//				swapIteration++;
//				this.status.addFeature(oneSet, minSetID);
//				this.testingStatus.addFeature(oneSet, minSetID);
//				// update the minDecrease, minSet, totalCoveredCount
//				minSetID = status.leastCoverSet(minDecrease);
//				testMinSetID = this.testingStatus.leastCoverSet(minDecrease);
//				if(minSetID!=testMinSetID)
//					System.out.println("Lala");
//				
//				minSet = this.status.getSelectedSet(minSetID);
//				if(this.swapType == 1){
//					totalCoveredCount = totalCoveredCount + difference;
//					typeScore = (delta*totalCoveredCount)/K;
//				}
//				else if(this.swapType == 0)
//					typeScore = delta * minDecrease[0];
//				//2.1 Remove the least again
//				this.status.removeFeature(minSetID);
//				this.testingStatus.removeFeature(minSetID);
//			}
//			// Branch & Bound
//			else if(this.bbCalculator!=null){
//				double threshold = minDecrease[0] + typeScore;
//				int bUp = this.bbCalculator.getUpperBound(oneSet, -1);
//				int testBUP = this.testingBBCalculator.getUpperBound(oneSet, -1);
//				if(bUp!=testBUP){
//					bUp = this.bbCalculator.getUpperBound(oneSet, -1);
//					System.out.println("lala3");
//				}
////				if(this.bbCalculator.getUpperBound(oneSet, -1) < threshold)
////					input.prunBranches();
//			}
//		}
//		//add the wrongly delete min
//		this.status.addFeature(minSet, minSetID);
//		System.out.println("TotalIteration: " + totalIteration + " SwapIteration: " + swapIteration);
//	}


	public ICoverSet_FeatureWrapper[] getSelectedSets(){
		return this.status.getSelectedSets();
	}

	public int totalCoveredItems() {
		return this.status.getCoveredCount();
	}

}

