package coder.slynk.Engine2D;

public final class Util {
	
	/**
	 *  Check whether a given variable is within the range. 
	 *  
	 *  @param Lower lower limit of range.
	 *  @param Upper upper limit of range.
	 *  @param variable the value to be tested within the range.
	 *  
	 *  @Return 1  if greater than range.
	 *  @Return 0  if within range.
	 *  @Return -1 if less than range.
	 */
	public static final int checkRange(int Lower, int Upper, int variable)
	{
		if(variable > Upper)
			return 1;
		else if (variable < Lower)
			return -1;
		else
			return 0;
	}
	
	/**
	 *  Check whether a given variable is within the range. 
	 *  
	 *  @param Lower lower limit of range.
	 *  @param Upper upper limit of range.
	 *  @param variable the value to be tested within the range.
	 *  
	 *  @Return 1  if greater than range.
	 *  @Return 0  if within range.
	 *  @Return -1 if less than range.
	 */
	public static final int checkRange(float Lower, float Upper, float variable)
	{
		if(variable > Upper)
			return 1;
		else if (variable < Lower)
			return -1;
		else
			return 0;
	}
	
	/**
	 *  Check whether a given variable is within the range. 
	 *  
	 *  @param Lower lower limit of range.
	 *  @param Upper upper limit of range.
	 *  @param variable the value to be tested within the range.
	 *  
	 *  @Return 1  if greater than range.
	 *  @Return 0  if within range.
	 *  @Return -1 if less than range.
	 */
	public static final int checkRange(double Lower, double Upper, double variable)
	{
		if(variable > Upper)
			return 1;
		else if (variable < Lower)
			return -1;
		else
			return 0;
	}
}

