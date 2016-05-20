package coverage.schedules;

import java.util.Formatter;
import java.util.Locale;

import coverage.Globals;
import coverage.Parameters;

/**
 * BlockSchedule is currently the workhorse class for the scheduling program.  It defines the
 * majority of the methods that its extending classes use.  See the Schedule class for additional
 * rationale.
 * 
 * BlockSchedule schedules are of a form in which working hours form a "block" when viewed as a
 * matrix.  The start time and length of shifts are uniform throughout the week.  Because of this
 * uniformity, information for BlockSchedule is not stored in matrix form but rather as a few
 * parameters which define the work block (e.g., start time, etc.).  This saves tons on read and
 * write during computation, although I haven't thoroughly tested whether it's actually more
 * efficient than storage in matrix form.  Obviously, I suspect it is.
 * 
 * Notice that many of the matrix calculation methods look like a big f'ing mess.  This is for the
 * sake of optimization--I wanted to give the Java compiler as much help as possible.
 * Unfortunately, this makes reading the code very painful; however, these chunks of code were
 * tested more thoroughly than the rest, so they should perform as intended.
 * 
 * Also notice that the summed squares metric is hard coded into a couple of the calculations.  It should be
 * straight-forward enough to generalize--just replace the metric-specific calculation with calls
 * to a new metrics class's static method containing the necessary calculations for the desired metric.
 * Let me know if I can be of help.
 * 
 * @author - jeffrey
 */
public abstract class BlockSchedule extends Schedule {

	// typeID is a unique integer for easily identifying this class without needing to use Java's
	// introspection stuff
	final int typeID;					

	// the number of days worked per week.  This remains constant for all iterations
	final int numDays;
	
	// the number of half hours worked per day.  This also remains constant for all iterations
	final int numHalfHours;
	
	// a 2-d array for each of the combinations of weekdays to be used in the computation
	final boolean[][] workDaysCombos;
	
	// size of the outer nest of the workDaysCombo array
	final int numWorkDaysCombos;
	
	/*
	 * workDaysComboIndex is the number assigned to the current iteration of workDaysCombos.  Its
	 * range is {-1, 0, ... , numWorkDaysCombos}.  The initial value, -1, and final value,
	 * numWorkDaysCombos, do not correspond to meaningful combinations and are merely used to
	 * signal that iteration has not begun or that there are no more iterations, respectively
	 */
	int workDaysComboIndex;
	
	// indicates the half-hour period of the day at which the BlockSchedule shift begins.
	int shiftStart;
	
	// constructor
	public BlockSchedule(int numDays, int numHalfHours, boolean[][] workDaysCombos, int typeID) {
		super(Parameters.SIZE_OF_DAY * workDaysCombos.length);
		
		assert numDays > 0;
		assert numDays <= Parameters.SIZE_OF_WEEK;
		assert numHalfHours > 0;
		assert numHalfHours <= Parameters.SIZE_OF_DAY;
		
		this.typeID = typeID;
	    this.numDays = numDays;
		this.numHalfHours = numHalfHours;
		this.workDaysCombos = workDaysCombos;
		this.numWorkDaysCombos = workDaysCombos.length;
		this.workDaysComboIndex = 0;
		this.shiftStart = -1;
	}
	
	// constructor used to create BlockSchedule instance from another BlockSchedule instance.  It
	// copies the current state of iteration to the new instance.
	public BlockSchedule(BlockSchedule old) {
		super(old);
		
		this.typeID = old.typeID;
	    this.numDays = old.numDays;
		this.numHalfHours = old.numHalfHours;
		this.workDaysCombos = old.workDaysCombos;
		this.numWorkDaysCombos = old.numWorkDaysCombos;
		this.workDaysComboIndex = old.workDaysComboIndex;
		this.shiftStart = old.shiftStart;
	}
	
	// default constructor
	public BlockSchedule() {
		super();
		
		typeID = -1;
		numDays = 0;
		numHalfHours = 0;
		workDaysCombos = null;
		numWorkDaysCombos = 0;
	}
	
	/**
	 * next calls BlockSchedule's internal next(TargetMatrix, int, double) method which does the
	 * dirty work of iterating to the next schedule configuration.  The minCovVar(t) method is
	 * used in the third parameter to ensure that a minimum amount of (non-over) coverage is
	 * achieved by the resulting configuration.
	 *
	 * @see coverage.schedules.Schedule#next(coverage.schedules.TargetMatrix, int)
	 */
	@Override
	public void next(TargetMatrix t, int minIteration) {
		next(t, minIteration, minCovVar(t));
	}

	/**
	 * This nextFullCoverer method does the same thing as the above next method except for requiring
	 * that the resulting schedule configuration provides zero redundant coverage (i.e., the
	 * relevant cells in the target matrix remain non-negative after subtracting the resulting 
	 * schedule matrix).
	 * 
	 * @see coverage.schedules.Schedule#nextFullCoverer(coverage.schedules.TargetMatrix, int)
	 */
	@Override
	public void nextFullCoverer(TargetMatrix t, int minIteration) {
		next(t, minIteration, numDays * numHalfHours);
	}
	
   /**
    * @see coverage.schedules.Schedule#next(coverage.schedules.TargetMatrix, coverage.schedules.DiffMatrix, int)
    */
	@Override
	public void next(TargetMatrix t, DiffMatrix d, int minIteration) {
		next(t, d, minIteration, minCovVar(t));
	}
	
	/**
	 * This internal next method does some of the work of iterating the BlockSchedule to the next
	 * suitable configuration.  "Lower level" work is handled by the next() method.
	 * 
	 * First, the iteration variable is compared to minIteration.  If less than, iterate to
	 * minIteration; else, iterate to iteration+1.
	 * 
	 * Next, continue iterating by 1 until either finished iterating or a suitable configuration is
	 * found.  In this case, a suitable schedule configuration
	 * 
	 * 	(a) must not create too much over-coverage (i.e., the resulting target matrix's score must
	 * 		not be worse that the best known score thus far with respect to the metric being used)
	 * 		and 
	 * 
	 * 	(b) must provide a minimum amount of non-over-coverage for the target matrix.
	 * 
	 * @param t - the target matrix
	 * @param minIteration
	 * @param minCov
	 */
	private void next(TargetMatrix t, int minIteration, double minCov) {
		if (iteration < minIteration) {
			skipTo(java.lang.Math.min(minIteration, numIters));
		}
		else {
			next();
		}
		while (! isFinished() && (providesExcessiveOvercoverage(t) 
									|| ! providesSufficientCoverage(t, minCov) 
									)) {
			next();
		}
	}
	
	/**
	 * This internal next method does some of the work of iterating the BlockSchedule to the next
	 * suitable configuration.  "Lower level" work is handled by the next() method.
	 * 
	 * First, the iteration variable is compared to minIteration.  If less than, iterate to
	 * minIteration; else, iterate to iteration+1.
	 * 
	 * Next, continue iterating by 1 until either finished iterating or a suitable configuration is
	 * found.  In this case, a suitable schedule configuration
	 * 
	 * 	(a) must provide a minimum amount of non-over-coverage.for the difference matrix,
	 * 
	 * 	(b) must not create too much over-coverage (i.e., the resulting target matrix's score must
	 * 		not be worse that the best known score thus far with respect to the metric being used),
	 * 		and 
	 * 
	 * 	(c) must provide a minimum amount of non-over-coverage.for the target matrix.
	 * 
	 * @param t - the target matrix
	 * @param d - the difference matrix
	 * @param minIteration
	 * @param minCov
	 */
	private void next(TargetMatrix t, DiffMatrix d, int minIteration, double minCov) {
		if (iteration < minIteration) {
			skipTo(java.lang.Math.min(minIteration, numIters));
		}
		else {
			next();
		}
		while (! isFinished() && (! providesSufficientCoverage(d)
								  || providesExcessiveOvercoverage(t)
								  || ! providesSufficientCoverage(t, minCov)
								  )) {
			next();
		}
	}
	
	/**
	 * This internal next method handles a single non-skipping iteration.  It ensures that the
	 * BlockSchedule instance's parameters (e.g., shift start time, work day combinations, etc.)
	 * are properly incremented.  Depending on the case, it performs the following actions:
	 * 
	 * 1) The work days combination is NOT the final combination in the sequence of possible
	 *    combinations
	 *    
	 *    	a) The shift start time is not 11:30pm
	 *    
	 *    		-Increase shift start time by 30 minutes
	 *    		-Increment the iteration counter by 1
	 *    
	 *    	b) The shift start time is 11:30pm
	 *    
	 *    		-Set shift start time to 12:00am
	 *    		-Move to the next work days combination
	 *    		-Increment the iteration counter by 1.
	 *    
	 * 2) The work days combination IS the final combination in the sequence of possible
	 *    combinations
	 *    
	 *    	a) The shift start time is not 11:30pm
	 *    
	 *    		-Increase shift start time by 30 minutes
	 *    		-Increment the iteration counter by 1
	 *    
	 *    	b) The shift start time is 11:30pm
	 *    
	 *    		-Throw an error because next() should not have been called
	 *    
	 */
	private void next() {
		if (workDaysComboIndex < numWorkDaysCombos - 1) {
			if (shiftStart < Parameters.SIZE_OF_DAY - 1) {
				shiftStart++;
				iteration++;
			}
			else /* shiftStart is last half-hour of day */ {
				shiftStart = 0;
				workDaysComboIndex++;
				iteration++;
			}
		}
		else /* workDaysComboIndex is on the last combo index */ {
			if (shiftStart < Parameters.SIZE_OF_DAY) {
				shiftStart++;
				iteration++;
			}
			else /* shiftStart is last half-hour of day */ {
				throw new Error("There are no more schedule iterations.");
			}
		}
		assert shiftStart == iteration % Parameters.SIZE_OF_DAY || iteration == numIters;
		assert workDaysComboIndex == iteration / Parameters.SIZE_OF_DAY || iteration == numIters;
	}
	
	/**
	 * This internal method is called in order to skip over a number of iterations which do not
	 * need to be checked.  It obviously saves a lot of time when there's a good reason not to do
	 * all the usual number crunching.
	 * 
	 * @param newIteration - the number corresponding to the iteration to be skipped to
	 */
	private void skipTo(int newIteration) {
		assert iteration < newIteration;
		iteration = newIteration;
		shiftStart = iteration % Parameters.SIZE_OF_DAY;
		workDaysComboIndex = iteration / Parameters.SIZE_OF_DAY;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		Formatter formatter = new Formatter(sb, Locale.US);
		
		String result = formatter.format("%1$2dx%2$1d   Shift start: %4$2d   Works days: %5$2d~[",
				numDays, numHalfHours, iteration, shiftStart, workDaysComboIndex).toString();
		
		for (int i = 0; i < numDays; i++) {
			result += Globals.cs2.numeralStore[typeID][numDays][workDaysComboIndex][i] + " ";
		}
		
		result += "]";
		
		return result;
	}
	
	// this is shitty (i.e., don't bother)
//	private boolean providesExcessiveOvercoverage_native(TargetMatrix t) {
//		final double ACCEPTABLE_ERROR = .0000000001; // a small amount of error will accumulate due to rounding.  This must be anticipated.
//	    final double[] t_data = t.data;
//	    final int shiftEnd = shiftStart + numHalfHours;         
//	    final double limit = Globals.getWorstTopOvercoverage() - ACCEPTABLE_ERROR;
//		final boolean[] workDays = Globals.cs[numDays].getBinaryworkDaysCombos()[workDaysComboIndex];
//        double overcoverage = t.overcoverage;
//
//		return NativeMethods.providesExcessiveOvercoverage_C(t_data, shiftStart, shiftEnd, limit, workDays, overcoverage);
//	}
	
	/**
	 * Checks the BlockSchedule instance's current schedule configuration against the supplied
	 * TargetMatrix to see if too much over-coverage is created by throwing this [person's] 
	 * schedule in with the rest of the [persons'] schedules.
	 * 
	 * Since this method is specific to the summed squares metric, what it does is subtracts each
	 * schedule from the target matrix, including this BlockSchedule instance's schedule, and
	 * taking only the negative entries in the resulting matrix, sums the squares of these entries.
	 * If this sum is greater than a provided limit (e.g., the score of the 10th best set of
	 * schedule configurations found thus far), then it returns true because the additional
	 * schedule is crappy.
	 * 
	 * Of course, the algorithm used here doesn't actually have to do nearly as much work as was
	 * just described..
	 *
	 */
    private boolean providesExcessiveOvercoverage(TargetMatrix t) {
    	final double ACCEPTABLE_ERROR = .0000000001; // a small amount of error may (will?) accumulate due to rounding.  This must be anticipated.
	    final double[] t_data = t.data;
	    final int shiftEnd = shiftStart + numHalfHours;         
	    final double limit = Globals.getWorstTopOvercoverage() - ACCEPTABLE_ERROR;
    	final boolean[] workDays = workDaysCombos[workDaysComboIndex];
        double accumulatedOvercoverage = t.overcoverage;
        
        for (int i = 0; i < Parameters.SIZE_OF_WEEK - 1; i++) {
            if (workDays[i]) {
                final int indexStart = i * Parameters.SIZE_OF_DAY;
                for (int j = shiftStart; j < shiftEnd; j++) {
                    final double entry = t_data[indexStart + j];
                    if		(entry < 0) {accumulatedOvercoverage += (WORK - 2 * entry) * WORK;}
                    else if (entry < 1) {accumulatedOvercoverage += java.lang.Math.pow((entry - WORK), 2);}
                    //else (entry >= 1) no added overcoverage--do nothing   
                    if (accumulatedOvercoverage >= limit) {
                        return true;
                    }
                }
            }
        }
        // if work late enough on last day of week, work periods wrap over to start of week
        if (workDays[Parameters.SIZE_OF_WEEK - 1]) {
            final int indexStart = (Parameters.SIZE_OF_WEEK - 1) * Parameters.SIZE_OF_DAY;
            if (shiftEnd < Parameters.SIZE_OF_DAY) {
                for (int j = shiftStart; j < shiftEnd; j++) {
                    final double entry = t_data[indexStart + j];
                    if		(entry < 0) {accumulatedOvercoverage += (WORK - 2 * entry) * WORK;}
                    else if (entry < 1) {accumulatedOvercoverage += java.lang.Math.pow((entry - WORK), 2);}
                    //else (entry >= 1) no added overcoverage--do nothing  
                }
            }
            else /* wrap to start of week */ {
                for (int j = shiftStart; j < Parameters.SIZE_OF_DAY; j++) {
                    final double entry = t_data[indexStart + j];
                    if		(entry < 0) {accumulatedOvercoverage += (WORK - 2 * entry) * WORK;}
                    else if (entry < 1) {accumulatedOvercoverage += java.lang.Math.pow((entry - WORK), 2);}
                    //else (entry >= 1) no added overcoverage--do nothing   
                }
                int stop = shiftEnd - Parameters.SIZE_OF_DAY;
                for (int j = 0; j < stop; j++) {
                    final double entry = t_data[j];
                    if		(entry < 0) {accumulatedOvercoverage += (WORK - 2 * entry) * WORK;}
                    else if (entry < 1) {accumulatedOvercoverage += java.lang.Math.pow((entry - WORK), 2);}
                    //else (entry >= 1) no added overcoverage--do nothing  
                }
            }
            if (accumulatedOvercoverage >= limit) {                    	
                return true;
            }
        }                    	
        return false;
    }
        
    // uncomment to use with minCovVar method's commented experimentations
//    private double logistic(double x, double a, double b) {
//    	return 1 / (1 + java.lang.Math.exp(b * (a - x)));
//    }
    
    /**
     * Generates a value for a minimum amount of non-over-coverage that should be required of an 
     * additional schedule given a target matrix.  The currently uncommented code is somewhat
     * conservative in terms of pruning.  The commented-out code might be fun to experiment with.
     */
    private double minCovVar(TargetMatrix t) {
//    	final double propCovArea = t.coverableAreaCeiling / Parameters.ARRAY_SIZE;
//    	final double lo = logistic(propCovArea, .5, 15);  // .5, 15
//    	final double prop = lo + (1 - lo) * propCovArea;
//    	final double minCov = prop * numHalfHours * numDays;
    	
        final double propCovArea = t.coverableAreaCeiling / Parameters.ARRAY_SIZE;
        //final double minCov = s.totalPeriods() * (2 / (1 + java.lang.Math.exp(-3 * propCovArea)) - 1);
        final double minCov = numDays * numHalfHours * propCovArea;
        //final double minCov = 10;
    	//final double minCov = minCov(t);
        
        return minCov;
    }
	
	/**
	 * Checks the BlockSchedule instance's current schedule configuration against the supplied
	 * TargetMatrix to see how much non-over-coverage is provided by addition of this schedule to
	 * the set of schedules.  If this amount of non-over-coverage is greater than or equal to the
	 * minCov parameter, then it returns true, indicating that the added schedule provides a
	 * sufficient amount of non-over-coverage.
	 * 
	 * The use of this method provides a nice speed boost in some situations at the risk of
	 * excessive pruning.  The easiest way to disable this would probably be just to comment out
	 * this method's code and replace it with
	 * 
	 * 	return true;
	 * 
	 */
    private boolean providesSufficientCoverage(TargetMatrix t, double minCov) {
		final double t_data[] = t.data;
		final int shiftEnd = shiftStart + numHalfHours;
		double accumulatedCoverage = 0;

        for (int i = 0; i < Parameters.SIZE_OF_WEEK - 1; i++) {
            if (workDaysCombos[workDaysComboIndex][i]) {
                final int indexStart = i * Parameters.SIZE_OF_DAY;
                for (int j = shiftStart; j < shiftEnd; j++) {
                    if (t_data[indexStart + j] > 0) {
                        accumulatedCoverage += 1;
                    }
                }
            }
            if (accumulatedCoverage >= minCov) {
                return true;
            }
        }
        // if work late enough on last day of week, work periods wrap over to start of week
        if (workDaysCombos[workDaysComboIndex][Parameters.SIZE_OF_WEEK - 1]) {
            final int indexStart = (Parameters.SIZE_OF_WEEK - 1) * Parameters.SIZE_OF_DAY;
            if (shiftEnd < Parameters.SIZE_OF_DAY) {
                for (int j = shiftStart; j < shiftEnd; j++) {
                    if (t_data[indexStart + j] > 0) {
                        accumulatedCoverage += 1;
                    }
                }
            }
            else /* wrap to start of week */ {
                for (int j = shiftStart; j < Parameters.SIZE_OF_DAY; j++) {
                    if (t_data[indexStart + j] > 0) {
                        accumulatedCoverage += 1;
                    }
                }
                int stop = shiftEnd - Parameters.SIZE_OF_DAY;
                for (int j = 0; j < stop; j++) {
                    if (t_data[j] > 0) {
                        accumulatedCoverage += 1;
                    }
                }
            }
            if (accumulatedCoverage >= minCov) {
                return true;
            }
        }
        return false;
    }
    
	/**
	 * Checks the BlockSchedule instance's current schedule configuration against the supplied
	 * DiffMatrix to see how much non-over-coverage is provided by addition of this schedule to
	 * the set of schedules.  If this amount of non-over-coverage is greater than or equal to the
	 * minCov parameter, then it returns true, indicating that the added schedule provides a
	 * sufficient amount of non-over-coverage.
	 * 
	 * The use of this method DOES NOT introduce pruning.  In fact, this method is basically
	 * required in order for this program's initial routine to function properly.
	 * 
	 * The line
	 * 
	 * 		double minCov = numDays - 3;
	 * 
	 * should be adjusted to taste.
	 * 
	 */
    private boolean providesSufficientCoverage(DiffMatrix d) {
    	double[] d_forwardData = d.forwardData;
    	double[] d_backwardData = d.backwardData;
    	double minCov = 1 * numDays - 3;  // TODO start simple    	--------------------------------------------   TWEAK THIS   --------------------------------------------------------
    	double accumulatedCoverage = 0;
    	
    	for (int i = 0; i < Parameters.SIZE_OF_WEEK; i++) {
    		if (workDaysCombos[workDaysComboIndex][i]) {
    			final int start = (i * Parameters.SIZE_OF_DAY) + shiftStart;
    			final double startData = d_forwardData[start];
    			final double endData = d_backwardData[(start + numHalfHours - 1) % Parameters.ARRAY_SIZE]; // hopefully not too slow
    			if		(startData > 1) {accumulatedCoverage += 1;}
    			else if (startData > 0) {accumulatedCoverage += startData;}
    			if		(endData > 1)	{accumulatedCoverage += 1;}
    			else if (endData > 0)	{accumulatedCoverage += endData;}
    		}
    		if (accumulatedCoverage >= minCov) {
    			return true;
    		}
    	}
    	return false;
    }

    // Method to add the BlockSchedule instance "matrix" to a TargetMatrix instance *in place*.
    // t += this
    @Override
    public void addTo(TargetMatrix t) {
        assert numCols == t.numCols;
        assert numRows == t.numRows;
    
	    final double t_data[] = t.data;
	    final int shiftEnd = shiftStart + numHalfHours;

        for (int i = 0; i < Parameters.SIZE_OF_WEEK - 1; i++) {
            if (workDaysCombos[workDaysComboIndex][i]) {
                final int indexStart = i * Parameters.SIZE_OF_DAY;
                for (int j = shiftStart; j < shiftEnd; j++) {
                    t_data[indexStart + j] += WORK;
                }
            }
        }
        // if work late enough on last day of week, work periods wrap over to start of week
        if (workDaysCombos[workDaysComboIndex][Parameters.SIZE_OF_WEEK - 1]) {
            final int indexStart = (Parameters.SIZE_OF_WEEK - 1) * Parameters.SIZE_OF_DAY;
            if (shiftEnd < Parameters.SIZE_OF_DAY) {
                for (int j = shiftStart; j < shiftEnd; j++) {
                    t_data[indexStart + j] += WORK;
                }
            }
            else /* wrap to start of week */ {
                for (int j = shiftStart; j < Parameters.SIZE_OF_DAY; j++) {
                    t_data[indexStart + j] += WORK;
                }
                int stop = shiftEnd - Parameters.SIZE_OF_DAY;
                for (int j = 0; j < stop; j++) {
                    t_data[j] += WORK;
                }
            }
        }
    }

    // Method to subtract the BlockSchedule instance "matrix" from a TargetMatrix instance *in place*.
    // t -= this
    @Override
    public void subFrom(TargetMatrix t) {
        assert numCols == t.numCols;
        assert numRows == t.numRows;
    
	    final double t_data[] = t.data;
	    final int shiftEnd = shiftStart + numHalfHours;

        for (int i = 0; i < Parameters.SIZE_OF_WEEK - 1; i++) {
            if (workDaysCombos[workDaysComboIndex][i]) {
                final int indexStart = i * Parameters.SIZE_OF_DAY;
                for (int j = shiftStart; j < shiftEnd; j++) {
                    t_data[indexStart + j] -= WORK;
                }
            }
        }
        // if work late enough on last day of week, work periods wrap over to start of week
        if (workDaysCombos[workDaysComboIndex][Parameters.SIZE_OF_WEEK - 1]) {
            final int indexStart = (Parameters.SIZE_OF_WEEK - 1) * Parameters.SIZE_OF_DAY;
            if (shiftEnd < Parameters.SIZE_OF_DAY) {
                for (int j = shiftStart; j < shiftEnd; j++) {
                    t_data[indexStart + j] -= WORK;
                }
            }
            else /* wrap to start of week */ {
                for (int j = shiftStart; j < Parameters.SIZE_OF_DAY; j++) {
                    t_data[indexStart + j] -= WORK;
                }
                int stop = shiftEnd - Parameters.SIZE_OF_DAY;
                for (int j = 0; j < stop; j++) {
                    t_data[j] -= WORK;
                }
            }
        }
    }
    
    // Method to add the BlockSchedule instance "matrix" to a DiffMatrix instance *in place*.
    // d += this
    @Override
    public void addTo(DiffMatrix d) {
    	double[] d_forwardData = d.forwardData;
    	double[] d_backwardData = d.backwardData;
    	
    	for (int i = 0; i < Parameters.SIZE_OF_WEEK; i++) {
    		if (workDaysCombos[workDaysComboIndex][i]) {
    			final int start = (i * Parameters.SIZE_OF_DAY) + shiftStart;
    			d_forwardData[start] += WORK;
    			d_backwardData[(start + numHalfHours - 1) % Parameters.ARRAY_SIZE] += WORK; // hopefully not too slow
    		}
    	}
    }
    
    // Method to subtract the BlockSchedule instance "matrix" from a DiffMatrix instance *in place*.
    // d -= this
    @Override
    public void subFrom(DiffMatrix d) {
    	double[] d_forwardData = d.forwardData;
    	double[] d_backwardData = d.backwardData;
    	
    	for (int i = 0; i < Parameters.SIZE_OF_WEEK; i++) {
    		if (workDaysCombos[workDaysComboIndex][i]) {
    			final int start = (i * Parameters.SIZE_OF_DAY) + shiftStart;
    			d_forwardData[start] -= WORK;
    			d_backwardData[(start + numHalfHours - 1) % Parameters.ARRAY_SIZE] -= WORK; // hopefully not too slow
    		}
    	}
    }
    
}

