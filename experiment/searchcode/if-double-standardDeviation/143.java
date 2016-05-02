package aipackage;

/**
 * Taken from the interwebs
 * 
 * Basic standard function
 */
public class StandardDeviation 
{ 
	public static double standardDeviationCalculate ( double[] data ) 
	{ 
		final int n = data.length; 
		if ( n < 2 ) 
		{ 
			return Double.NaN; 
		} 
		double avg = data[0]; 
		double sum = 0; 
		for ( int i = 1; i < data.length; i++ ) 
		{ 
			double newavg = avg + ( data[i] - avg ) / ( i + 1 ); 
			sum += ( data[i] - avg ) * ( data [i] -newavg ) ; 
			avg = newavg; 
		} 
		// Change to ( n - 1 ) to n if you have complete data instead of a sample. 
		return Math.sqrt( sum / ( n - 1 ) ); 
	} 

} 
