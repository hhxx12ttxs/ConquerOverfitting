package coverage.schedules;

import coverage.Parameters;

/**
 * TargetMatrix is the class which stores information for the number of people needed to cover a
 * each half-hour work period as a rational matrix.
 * 
 * TargetMatrix inherits DenseMatrix methods.  It has methods for adding and subtracting Schedule
 * classes to/from itself but actually calls the respective Schedule class's add/subtract methods,
 * since different Schedule classes (could) have different algorithms for interacting with a
 * matrix.  It has methods for calculating information about itself (e.g, the amount of
 * overcoverage with respect to the metric).  It also has methods for computing difference matrices
 * from itself.
 * 
 * Notice my horrible abuse of terminology throughout the documentation.  "Target matrix" is used
 * interchangeably as both the actual target numbers of people covering each period as well as the
 * actual target numbers minus the current number of people covering each period during any given
 * stage of the algorithm.  "TargetMatrix" obviously refers to this class.
 * 
 * @author - jeffrey
 */
public class TargetMatrix extends DenseMatrix {
	
	// Apply metric to negative target matrix entries to get this.  RECALCULATE UPON CHANGE OF
	// MATRIX STATE!
	double overcoverage;
	
	// This is the sum of the entries of matrix M, where M is the target matrix with positive
	// entries replaced by 1 and negative entries replaced by 0.  RECALCULATE UPON CHANGE OF MATRIX
	// STATE!
	double coverableAreaCeiling;
	
	/*
	 *  Constructs a TargetMatrix instance by copying the state of a DenseMatrix instance and
	 *  calculates fields.
	 */
	public TargetMatrix(DenseMatrix m) {
		super(m);
		computeOvercoverage();
		computeCoverableAreaCeiling();
	}
	
	/*
	 * Constructs a TargetMatrix instance given number of rows and columns and a 1-d array of data.
	 * Calculates fields.
	 */
	public TargetMatrix(int numRows, int numCols, double[] data) {
		super(numRows, numCols, data);
		computeOvercoverage();
		computeCoverableAreaCeiling();
	}
	
	@Override
	public TargetMatrix copy() {
		return new TargetMatrix(this);
	}

	/**
	 * Calls a Schedule class's addTo method with the TargetMatrix instance as the parameter.  This
	 * adds the Schedule instance data to the TargetMatrix instance data *in place* and then
	 * recalculates its fields.
	 * 
	 * @see Schedule.addTo(TargetMatrix)
	 * 
	 * @param s - the Schedule subclass instance to be added to the TargetMatrix instance
	 */
	// this += s 
	public void addEquals(Schedule s) {
		assert s.numRows == numRows;
		assert s.numCols == numCols;
    	
		s.addTo(this);
		
    	computeOvercoverage();
    	computeCoverableAreaCeiling();
    }
	
	/**
	 * Calls a Schedule class's subtractFrom method with the TargetMatrix instance as the parameter.  This
	 * subtracts the Schedule instance data from the TargetMatrix data *in place* and then
	 * recalculates its fields.
	 * 
	 * @see Schedule.subtractFrom(TargetMatrix)
	 * 
	 * @param s - the Schedule subclass instance to be subtracted from the TargetMatrix instance
	 */
	// this -= s
	public void subEquals(Schedule s) {
		assert s.numRows == numRows;
		assert s.numCols == numCols;

		s.subFrom(this);
		
    	computeOvercoverage();
    	computeCoverableAreaCeiling();
    }

	/**
	 * Looks at the TargetMatrix instance data.  If a positive entry is found, then there is still
	 * need for additional coverage.
	 * 
	 * @return	boolean true if a positive entry is found in TargetMatrix instance data; else false
	 */
	public boolean hasCoverableArea() {
    	for (int i = 0; i < Parameters.ARRAY_SIZE; i++) {
    		if (data[i] > 0) {
    			return true;
    		}
    	}		
		return false;
	}
	
	/**
	 * Internal method for calculating the coverableAreaCeiling field.  For each entry in the
	 * target matrix, if that entry is positive, add 1 to the result.  I suppose this is just the 
	 * number of positive entries in the target matrix--and a way of measuring how much of the
	 * target has yet to be fully covered).
	 */
	private void computeCoverableAreaCeiling() {
    	double result = 0;
    	
    	for (int i = 0; i < Parameters.ARRAY_SIZE; i++) { // need to run this method any time data change!
    		if 		(data[i] > 0)	{result += 1;}
    		//else	(data[i] <= 0)	{result += 0;}
    	}
    	coverableAreaCeiling = result;
	}
	
	/**
	 * Internal method for calculating the overcoverage field.  This is not as of yet metric
	 * agnostic.  For each negative entry in the target matrix, its square is added to the result.
	 * The result is then assigned to the overcoverage field.
	 */
	private void computeOvercoverage(/* metric? */) { // need to run this method any time data change!
    	double result = 0;
		
		for (int i = 0; i < Parameters.ARRAY_SIZE; i++) {
			if (data[i] < 0) {
				result += data[i] * data[i];
			}
		}
		overcoverage = result;
	}
	
	// stupid getter
	public double getOvercoverage() {
		return overcoverage;
	}

	// stupid getter
	public double getCoverableAreaCeiling() {
		return coverableAreaCeiling;
	}
	
	/**
	 * creates a DiffMatrix instance based on the TargetMatrix instance's data.
	 * 
	 * @see TargetMatrix.createForwardDiffMatrix() and
	 * 		TargetMatrix.createBackwardDiffMatrix()
	 */
	public DiffMatrix createDiffMatrix() {
		DiffMatrix result = new DiffMatrix(numRows, numCols);
		result.forwardData = createForwardDiffMatrix();
		result.backwardData = createBackwardDiffMatrix();
		return result;
	}
	
	/**
	 * @return a forward difference matrix--gah...ask me if you really want to know how
	 */
	// slow algorithm, but doesn't get used often
	private double[] createForwardDiffMatrix() {
		final double[] wrappedData = new double[Parameters.ARRAY_SIZE + Parameters.MIN_SHIFT_LENGTH];
		System.arraycopy(data, Parameters.ARRAY_SIZE - Parameters.MIN_SHIFT_LENGTH, wrappedData, 0, Parameters.MIN_SHIFT_LENGTH);
		System.arraycopy(data, 0, wrappedData, Parameters.MIN_SHIFT_LENGTH, Parameters.ARRAY_SIZE);
		
		double[] result = new double[numRows * numCols];
		
		double lastSpot;
		double sameSpot;
		
		for (int i = 0; i < Parameters.ARRAY_SIZE; i++) {	//HERE
			lastSpot = wrappedData[i];
			sameSpot = wrappedData[i + 1];
			for (int j = 1; j < Parameters.MIN_SHIFT_LENGTH; j++) {
				if (lastSpot < wrappedData[i + j]) {
					lastSpot = wrappedData[i + j];
				}
				if (sameSpot < wrappedData[i + 1 + j]) {
					sameSpot = wrappedData[i + 1 + j];
				}
			}												//TO HERE identical to below
			result[i] = java.lang.Math.max(0, sameSpot - lastSpot);
		}
		
		return result;
	}
		
	/**
	 * @return a backward difference matrix--gah...ask me if you really want to know how
	 */
	// slow algorithm, but doesn't get used often
	private double[] createBackwardDiffMatrix() {
		final double[] wrappedData = new double[Parameters.ARRAY_SIZE + Parameters.MIN_SHIFT_LENGTH];
		wrappedData[0] = data[Parameters.ARRAY_SIZE - 1];
		System.arraycopy(data, 0, wrappedData, 1, Parameters.ARRAY_SIZE);
		System.arraycopy(data, 0, wrappedData, 1 + Parameters.ARRAY_SIZE, Parameters.MIN_SHIFT_LENGTH - 1);
		
		double[] result = new double[numRows * numCols];
		
		double lastSpot;
		double sameSpot;
		
		for (int i = 0; i < Parameters.ARRAY_SIZE; i++) {	//HERE
			lastSpot = wrappedData[i];
			sameSpot = wrappedData[i + 1];
			for (int j = 1; j < Parameters.MIN_SHIFT_LENGTH; j++) {
				if (lastSpot < wrappedData[i + j]) {
					lastSpot = wrappedData[i + j];
				}
				if (sameSpot < wrappedData[i + 1 + j]) {
					sameSpot = wrappedData[i + 1 + j];
				}
			}												//TO HERE identical to above
			result[i] = java.lang.Math.max(0, lastSpot - sameSpot);
		}
		
		return result;
	}
	
    public String toString() {
    	final double[] data = this.data;
    	final int numRows = this.numRows;
    	final int numCols = this.numCols;
    	
    	String result = "";
    	for (int i = 0; i < numRows; i++) {
    		String subresult = "";
    		for (int j = 0; j < numCols; j++) {
    			if (data[i * numCols + j] > 0) {
    			subresult += String.valueOf(data[i * numCols + j]).substring(0, 3) + " ";
    			}
    			else if (data[i * numCols + j] == 0) {
    				subresult += " o  ";
    			}
    			else {
    				subresult += " .  ";
//    				subresult += String.valueOf(data[i * numCols + j]).substring(0, 3) + " ";
    			}
    		}
    		result += subresult + "\n";
    	}
    	return result;
    }
	
}

