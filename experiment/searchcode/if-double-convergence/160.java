package plugins.adufour.activemeshes.util;

import icy.math.ArrayMath;

/**
 * Utility class defining a fixed-size window where a user may store values and check convergence
 * against various criteria
 * 
 * @author Alexandre Dufour
 * 
 */
public class SlidingWindowConvergence
{
	/**
	 * The list of operations that can be applied on the window
	 * 
	 * @author Alexandre Dufour
	 * 
	 */
	public enum Operation
	{
		MIN, MAX, MEAN, SUM, VARIANCE,
		/**
		 * Coefficient of variation (standard deviation over the mean)
		 */
		VAR_COEFF
	};
	
	public enum LimitType
	{
		/**
		 * Convergence is reached if the value computed over the window becomes lower than the given
		 * criterion
		 */
		LOWER_BOUND,
		/**
		 * Convergence is reached if the value computed over the window becomes higher the given
		 * criterion
		 */
		UPPER_BOUND
	}
	
	private double[]		window;
	
	private final Operation	operation;
	
	private int				count	= 0;
	
	/**
	 * Creates a new convergence window with given size, operation and convergence test sorting
	 * method
	 * 
	 * @param size
	 *            the window size
	 */
	public SlidingWindowConvergence(int size, Operation operation)
	{
		this.window = new double[size];
		this.operation = operation;
	}
	
	/**
	 * Adds the given value to the queue
	 * 
	 * @param value
	 */
	public final void add(double value)
	{
		window[count % window.length] = value;
		count++;
	}
	
	/**
	 * Erase all values from the convergence window. Makes the window reusable without destruction
	 */
	public void clear()
	{
		java.util.Arrays.fill(window, 0);
		count = 0;
	}
	
	public boolean checkConvergence(double epsilon)
	{
		return (count < window.length) ? false : (computeCriterion() < epsilon);
	}
	
	public double computeCriterion()
	{
		switch (operation)
		{
			case MIN:
				return ArrayMath.min(window);
			case MAX:
				return ArrayMath.max(window);
				
			case MEAN:
				return ArrayMath.mean(window);
				
			case SUM:
				return ArrayMath.sum(window);
				
			case VARIANCE:
				return ArrayMath.var(window, true);
				
			case VAR_COEFF:
				double mean = ArrayMath.mean(window);
				return mean == 0 ? 0 : ArrayMath.std(window, true) / mean;
				
			default:
				throw new UnsupportedOperationException("operation " + operation.toString() + " not supported yet");
		}
	}
}

