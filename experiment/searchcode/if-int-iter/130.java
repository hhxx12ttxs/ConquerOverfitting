package edu.psu.chemxseer.structure.setcover.maxCoverStatus;

import edu.psu.chemxseer.structure.setcover.featureGenerator.IFeatureSetConverter;
import edu.psu.chemxseer.structure.setcover.sets.ICoverSet_FeatureWrapper;
import edu.psu.chemxseer.structure.util.IntersectionSet;

public class Stream_LinkedList_PMatrix implements ICoverStatusStream_Set{
	//sparse representation of the matrix status[i][item] = j, if [i][j] is an candidate entry
	private int[][] status; 
	private short[][][] invertedIndex; // the inverted index of entry-> sets containing the entry
	private short[][] invertedIndexSize; // the size of the invertedIdnex

	protected IFeatureSetConverter converter;
	private ICoverSet_FeatureWrapper[] selectedFeatures;
	private int[] gain;
	private int numOfSets; // the total number of selected sets
	
	// For the Upp bound calculation
	private int[] yCovered;
	private int[] yCoveredMinSet;
	
	
	public Stream_LinkedList_PMatrix(int K, IFeatureSetConverter converter, 
			Status_BooleanMatrix alreadyCovered){
		this.converter = converter;
		this.selectedFeatures = new ICoverSet_FeatureWrapper[K];
		this.gain  = new int[K];
		this.numOfSets = 0;

		int classOneNumber = converter.getCountOneNumber();
		int classTwoNumber= converter.getCountTwoNumber();
		//1. Construct the status matrix
		this.status = new int[classTwoNumber][];
		//2. Construct the invertedIndex
		this.invertedIndex = new short[classTwoNumber][][];
		this.invertedIndexSize = new short[classTwoNumber][];
		for(int i = 0; i< classTwoNumber; i++){
			int size = 0;
			for(int j = 0; j< classOneNumber; j++)
				// ignore the already covered items
				if(!alreadyCovered.coverStatus[i][j])
					size++;
			this.status[i] = new int[size];
			this.invertedIndex[i] = new short[size][];
			this.invertedIndexSize[i] = new short[size];
			int iter = 0;
			for(int j = 0; j< classOneNumber; j++)
				if(!alreadyCovered.coverStatus[i][j])
					status[i][iter++] = j;			
		}
		//3. Construct the Upperbound calculator
		yCovered = new int[converter.getCountTwoNumber()];
		yCoveredMinSet = new int[converter.getCountTwoNumber()];
		for(int i = 0; i< yCoveredMinSet.length; i++)
			yCoveredMinSet[i] = -2;
	}
	@Override
	public int getGain(ICoverSet_FeatureWrapper oneSet, short exceptSetID) {
		int gain = 0;
		int[][] matrix = this.converter.featureToSet_Matrix(oneSet);
		for(int i = 0; i< matrix.length; i++){
			int qID = matrix[i][0];
			//1. Get the intersection position
			int[] pos = IntersectionSet.getInterSectionPosition(this.status[qID], 0, status[qID].length, 
					matrix[i], 1, matrix[i].length);
			for(int onePos : pos){
				if(this.invertedIndexSize[qID][onePos] == 0)
					gain ++;
				else if(this.invertedIndexSize[qID][onePos] == 1 
						&& invertedIndex[qID][onePos][0] == exceptSetID)
					gain++;
			}
		}
		return gain;
	}

	@Override
	public boolean swap(short oldSetID, ICoverSet_FeatureWrapper newSet) {
		ICoverSet_FeatureWrapper oldSet = this.selectedFeatures[oldSetID];
		if(oldSet == null)
			return false;
		// Do the swap
		this.selectedFeatures[oldSetID] = newSet;
		// Update the structure
		int[][] newMatrix = this.converter.featureToSet_Matrix(newSet);
		int[][] oldMatrix = this.converter.featureToSet_Matrix(oldSet);
		IntersectionSet set = new IntersectionSet();
		
		int newIter = 0, oldIter = 0;
		while(newIter < newMatrix.length && oldIter < oldMatrix.length){
			int newQID = newMatrix[newIter][0];
			int oldQID = oldMatrix[oldIter][0];
			if(newQID < oldQID){
				//add the items covered by newQID
				int[] pos = IntersectionSet.getInterSectionPosition(
						this.status[newQID], 0, status[newQID].length, 
						newMatrix[newIter], 1, newMatrix[newIter].length);
				for(int onePos : pos)
					this.insertValue(newQID, onePos, oldSetID);
			}
			else if(newQID == oldQID){
				//2.1. Add the items covered by new set
				set.clear();
				set.addAll(newMatrix[newIter], 1, newMatrix[newIter].length);
				set.removeAll(oldMatrix[oldIter], 1, oldMatrix[oldIter].length);
				int[] newItems = set.getItems();
				
				int[] pos = IntersectionSet.getInterSectionPosition(
						this.status[newQID], 0, status[newQID].length, 
						newItems, 0, newItems.length);
				for(int onePos : pos)
					this.insertValue(newQID, onePos, oldSetID);
				//2.2. Remove the items covered by old set
				set.clear();
				set.addAll(oldMatrix[oldIter], 1, oldMatrix[oldIter].length);
				set.removeAll(newMatrix[newIter], 1, newMatrix[newIter].length);
				int[] oldItems = set.getItems();
				pos = IntersectionSet.getInterSectionPosition
					(status[oldQID], 0, status[oldQID].length,  oldItems, 0, oldItems.length);
				for(int onePos:pos)
					this.removeValue(oldQID, onePos, oldSetID);
			}
			else{
				// newQID > oldQID
				// remove the items covered by the oldQID
				int[] pos = IntersectionSet.getInterSectionPosition
					(status[oldQID], 0, status[oldQID].length, oldMatrix[oldIter], 1, oldMatrix[oldIter].length);
				for(int onePos:pos)
					this.removeValue(oldQID, onePos, oldSetID);
			}
		}
		for(; newIter < newMatrix.length; newIter++){
			int newQID = newMatrix[newIter][0];
			int[] pos = IntersectionSet.getInterSectionPosition(
					this.status[newQID], 0, status[newQID].length, 
					newMatrix[newIter], 1, newMatrix[newIter].length);
			for(int onePos : pos)
				this.insertValue(newQID, onePos, oldSetID);
		}
		for(; oldIter< oldMatrix.length;oldIter++){
			int oldQID = oldMatrix[oldIter][0];
			int[] pos = IntersectionSet.getInterSectionPosition
				(status[oldQID], 0, status[oldQID].length, oldMatrix[oldIter], 1, oldMatrix[oldIter].length);
			for(int onePos:pos)
				this.removeValue(oldQID, onePos, oldSetID);
			
		}	
		return true;
	}

	@Override
	public boolean removeSet(short sID) {
		ICoverSet_FeatureWrapper oldSet = this.selectedFeatures[sID];
		if(oldSet == null){
			return false;
		}
		else{
			int[][] matrix = this.converter.featureToSet_Matrix(oldSet);
			for(int i = 0; i< matrix.length; i++){
				int qId = matrix[i][0];
				int[] pos = IntersectionSet.getInterSectionPosition
					(status[qId], 0, status[qId].length, matrix[i], 1, matrix[i].length);
				for(int onePos:pos){
					this.removeValue(qId, onePos, sID);
				}
			}
			this.gain[sID] =0;
			return true;
		}
	}

	@Override
	public boolean addSet(ICoverSet_FeatureWrapper oneSet, short sID) {
		if(this.selectedFeatures[sID] == null){
			int[][] matrix = this.converter.featureToSet_Matrix(oneSet);
			for(int i = 0; i< matrix.length; i++){
				int qID = matrix[i][0];
				//1. Get the intersection position
				int[] pos = IntersectionSet.getInterSectionPosition(
						this.status[qID], 0, status[qID].length, 
						matrix[i], 1, matrix[i].length);
				for(int onePos : pos){
					this.insertValue(qID, onePos, sID);
				}
			}
			return true;
		}
		else return false;
	}

	@Override
	public short leastCoverSet(int[] minSize) {
		minSize[0] = Integer.MAX_VALUE;
		short result = (short)-1;
		for(short i = 0; i< gain.length; i++)
			if(this.selectedFeatures[i]!=null && gain[i] < minSize[0]){
				minSize[0] = gain[i];
				result = i;
			}
		return result;
	}
	
	@Override
	public int[] getCoveredCountExceptMin(short minSetID, int[] yIDs) {
		int[] result = new int[yIDs.length];
		int[][] minCovered = converter.featureToSet_Matrix(this.selectedFeatures[minSetID]);
		for(int minIndex = 0, yIDsIndex = 0; yIDsIndex < yIDs.length; yIDsIndex++){
			int yID = yIDs[yIDsIndex];
			if(this.yCoveredMinSet[yID] == this.selectedFeatures[minSetID].getFetureID())
				result[yIDsIndex] = yCoveredMinSet[yID]; //  no need to re-calculated
			else{
				// Need to recalculated:
				// 2.1 Calculate the total score
				int totalScore = 0;
				for(int w = 0; w< this.invertedIndexSize[yID].length; w++)
					if(invertedIndexSize[yID][w] > 0)
						totalScore ++;
				totalScore += this.converter.getCountOneNumber()-status[yID].length;
				
				// 2.2 get from the converter the minSet covers in yID
				while(minIndex < minCovered.length && minCovered[minIndex][0] < yID)
					minIndex++;
				
				if(minIndex >= minCovered.length || yID < minCovered[minIndex][0]){
					yCovered[yID] = totalScore; // use the total score directly
				}
				else if (yID == minCovered[minIndex][0]){
					int[] pos = IntersectionSet.getInterSectionPosition(
							status[yID], 0, status[yID].length, 
							minCovered[yID], 1, minCovered[yID].length);
					for(int w = 1; w < pos.length; w++){
						if(invertedIndexSize[yID][w] == 1)
							totalScore--;
					}
					yCovered[yID] = totalScore;
				}
				yCoveredMinSet[yID] = this.selectedFeatures[minSetID].getFetureID();
				result[yIDsIndex] = yCoveredMinSet[yID]; 
			}
			
		}
		return yCovered;
	}

	@Override
	public ICoverSet_FeatureWrapper[] getSelectedSets() {
		return this.selectedFeatures;
	}

	@Override
	public ICoverSet_FeatureWrapper getSelectedSet(short minSetID) {
		return this.selectedFeatures[minSetID];
	}

	@Override
	public int getGain(ICoverSet_FeatureWrapper newSet) {
		return this.getGain(newSet, (short)-1);
	}

	@Override
	public boolean addNewSet(ICoverSet_FeatureWrapper newSet) {
		if(this.numOfSets < this.selectedFeatures.length){
			this.selectedFeatures[numOfSets] = newSet;
			this.gain[numOfSets] = 0;
			
			int[][] matrix = this.converter.featureToSet_Matrix(newSet);
			for(int i = 0; i< matrix.length; i++){
				int qID = matrix[i][0];
				//1. Get the intersection position
				int[] pos = IntersectionSet.getInterSectionPosition(
						this.status[qID], 0, status[qID].length, 
						matrix[i], 1, matrix[i].length);
				for(int onePos : pos){
					this.insertValue(qID, onePos, numOfSets);
				}
			}
			this.numOfSets ++;
			return true;
		}
		else {
			System.out.println("Error: not enough sapce: use swap instead");
			return false;
		}
	}
	
	@Override
	public int[][] addNewSetWithReturn(ICoverSet_FeatureWrapper newSet) {
		if(this.numOfSets < this.selectedFeatures.length){
			this.selectedFeatures[numOfSets] = newSet;
			this.gain[numOfSets] = 0;
			
			int[][] matrix = this.converter.featureToSet_Matrix(newSet);
			int[][] result = new int[matrix.length][];
			int iter = 0;
			for(int i = 0; i< matrix.length; i++){
				int qID = matrix[i][0];
				//1. Get the intersection position
				int[] pos = IntersectionSet.getInterSectionPosition(
						this.status[qID], 0, status[qID].length, 
						matrix[i], 1, matrix[i].length);
				//2. Return the newly covered items:
				result[i] = new int[pos.length+1];
				result[i][0] = qID;
				iter = 1;
				
				for(int onePos : pos){
					if(this.invertedIndexSize[qID][onePos] == 0)
						result[i][iter++] = this.status[qID][onePos];
					this.insertValue(qID, onePos, numOfSets);
				}
				//3. Save space
				if(iter < result[i].length){
					int[] temp = new int[iter];
					for(int w = 0; w < iter; w++)
						temp[w] = result[i][w];
					result[i] = temp;
				}
			}
			this.numOfSets ++;
			return result;
		}
		else {
			System.out.println("Error: not enough sapce: use swap instead");
			return null;
		}
	}

	@Override
	public int getCoveredCount() {
		int counter = 0;
		for(int i = 0; i < this.invertedIndexSize.length; i++)
			for(int j = 0; j< this.invertedIndexSize[i].length; j++)
				if(invertedIndexSize[i][j] >0)
					counter++;
		return counter;
	}

	@Override
	public IFeatureSetConverter getConverter() {
		return this.converter;
	}
	
	
	/**********************Private Member*****************************/
	/**
	 * add the fID to the itemID entry 
	 * update the inverted index & gain function
	 * @param itemID
	 * @param fID
	 */
	private void insertValue(int yId, int xId, int fID){
		if(this.invertedIndex[yId][xId] == null){
			invertedIndex[yId][xId] = new short[2];
			invertedIndexSize[yId][xId] = 0;
		}
		else if(this.invertedIndex[yId][xId].length == invertedIndexSize[yId][xId]){
			short[] temp = new short[invertedIndexSize[yId][xId]*2];
			for(int i = 0; i< invertedIndexSize[yId][xId]; i++){
				temp[i] = invertedIndex[yId][xId][i];
			}
			invertedIndex[yId][xId] = temp;
		}
		// update the score
		if(invertedIndexSize[yId][xId] == 1){
			// currently covered by one feature f, after adding fID, f's score decrease
			this.gain[this.invertedIndex[yId][xId][0]] --;
		}
		else if(invertedIndexSize[yId][xId] == 0)
			this.gain[fID]++;
		// update the inverted index by appending the fID
		invertedIndex[yId][xId][invertedIndexSize[yId][xId]++] = (short) fID;
	}

	/**
	 * remove the fID from the itemID entry 
	 * update the inverted index & gain function
	 * since it is not required that the invered index is ordered, there fore an linear
	 * search is needed.
	 * @param itemID
	 * @param fID
	 */
	private void removeValue(int xId, int yId, int fID){
		// Shrink the size of the array
		if(2 * invertedIndexSize[xId][yId] == invertedIndex[xId][yId].length 
				&& invertedIndex[xId][yId].length > 2){
			short[] temp = new short[invertedIndexSize[xId][yId]];
			int iter = 0;
			for(int i = 0; i< invertedIndexSize[xId][yId]; i++)
				if(invertedIndex[xId][yId][i]!=fID)
					temp[iter++] = invertedIndex[xId][yId][i];
			this.invertedIndex[xId][yId] = temp;
			invertedIndexSize[xId][yId]--;
		}
		else{
			// do the deletion
			int pos = linearSearch(this.invertedIndex[xId][yId], this.invertedIndexSize[xId][yId], fID);
			if(pos == 0)
				System.out.println("error in removevalue, not such value");
			if(pos != invertedIndexSize[xId][yId]-1){
				// fID is not the last: swap the fID with the last value
				this.invertedIndex[xId][yId][pos] = 
					invertedIndex[xId][yId][invertedIndexSize[xId][yId]-1];
			}
			invertedIndexSize[xId][yId]--;;
		}
		// update the score
		if(invertedIndexSize[xId][yId] == 1){
			gain[invertedIndex[xId][yId][0]]--;
		}
		else if(invertedIndexSize[xId][yId] == 0)
			gain[fID]--;
		
	}
	
	private int linearSearch(short[] array, int boundary, int value){
		if(array == null)
			return -1;
		else{
			for(int i = 0; i< boundary; i++)
				if(array[i] == value)
					return i;
			return -1;
		}
	}

}

