//GeologyCalculator
package calculations;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

/**
 * Holds the methods that need to be called when performing calculations for
 * SiteSim.  As per the Paul Santi's outline, there is only one type of calculation
 * that is performed.
 *
 */

public class GeologyCalculator
{

	/**
	 * Generates a random number between -1 and 1 to be used
	 * in calculations at various depths
	 * @param z The seed to be used in the generator
	 * @return A decimal number out to the tenth's place
	 */
	/**
	 * returns a sufficiently random decimal between -1 and 1 with a precision of 0.1
	 */
	public static double getRandomValue(int x, int y)
	{
		//Creates new random number generator based on an integer seed
		Random randX = new Random(x);
		Random randY = new Random(randX.nextInt()%y);
		int xInt = randX.nextInt();
		int yInt = randY.nextInt();
		Random randGen = new Random(xInt%yInt);
		//Gets the next integer from randGen and sets it bewteen -1 and 1
		double randDecimal = ((randGen.nextInt()%10)/10.0);
		return randDecimal;
	}
	public static double getRandomValue(int x, int y, int z)
	{
		//Creates new random number generator based on an integer seed
		Random randX = new Random(x);
		Random randY = new Random(randX.nextInt()%y);
		if(z == 0) z = 1;
		Random randZ = new Random(randY.nextInt()%z);
		int xInt = randX.nextInt();
		int yInt = randY.nextInt();
		int zInt = randZ.nextInt();
//		System.out.println("yInt: " + yInt);
		Random randGen = new Random(xInt * yInt * zInt);
		//Gets the next integer from randGen and sets it bewteen -1 and 1
		double randDecimal = ((Math.abs(randGen.nextInt())%10)/10.0);
		return randDecimal;
	}
	/**
	 * Gets a random number that is seeded based on the time of the system
	 * that will be between -1 and 1
	 * @return A random decimal number between -1 and 1
	 */
	public static double getRandomValue()
	{
		//Creates new random number generator based on an integer seed
		Random randGen = new Random();
		//Gets the next integer from randGen and sets it bewteen -1 and 1
		double randDecimal = ((randGen.nextInt()%10)/10.0);
		return randDecimal;
	}
	
	/**
	 * Performs the calculation that is used throughout the application in finding
	 * different values of geological data.  A,B,C, and D are given in the descriptions
	 * for a site, while Z is derived during the via a random number generated.
	 * @param A The given A value for the current geological variable
	 * @param B The given B value for the current geological variable
	 * @param C The given C value for the current geological variable
	 * @param D The given D value for the current geological variable
	 * @param Z The random number generated
	 * @return (A + (B*Z)) + ((C + (D*Z))*R)
	 */
	public static double performCalc(double[] values, int Z, int x, int y)
	{
		
		
		//Seeds the random number based on location
		double A = values[0];
		double B = values[1];
		double C = values[2];
		double D = values[3];
		double R = getRandomValue(x, y, Z);
		
		//Calculation given by Dr. Santi
		double endValue = (A + (B*Z)) + ((C + (D*Z))*R);

		//Double eVal = endValue;
		//endValue = Math.round(endValue*100)/(100.0*endValue);
		//System.out.println("endValue before BD: " + endValue);
		BigDecimal bd = new BigDecimal(endValue).setScale(3, RoundingMode.HALF_EVEN);
		endValue = bd.doubleValue();
		//System.out.println("endValue after BD: " + endValue);

		
		return endValue;
	}
	public static double performKCalc(double[] values, int Z, int x, int y)
	{
		//Seeds the random number based on location
		
		double A = values[0];
		double B = values[1];
		double C = values[2];
		double D = values[3];
		double R = getRandomValue(x, y, Z);
		System.out.println("R: " + R);
		System.out.println("D: " + D);
		//Calculation given by Dr. Santi
		double endValue = (A + (B*Z)) + ((C + (D*Z))*R);
		//System.out.println("val before bigDecimal lines: " + endValue);
		//Double eVal = endValue;
		//endValue = Math.round(endValue*100)/(100.0*endValue);
		//System.out.println("END value" + endValue);
		
		return endValue;
	}

}

