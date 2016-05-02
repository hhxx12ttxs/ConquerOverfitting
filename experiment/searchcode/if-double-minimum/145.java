//	Practicum IN1608WI	Opdracht 3
//	Auteur Arian Stolwijk,	Studienummer 4001079
//	Datum 28 9 2011

package opdracht3;

public class Opdracht3_3 {

	public static void main(String[] args) {

		System.out.println("Maximum:");
		System.out.println("    1: " + maximum(0, 5, 1));
		System.out.println("  0.1: " + maximum(0, 5, 0.1));
		System.out.println(" 0.01: " + maximum(0, 5, 0.01));
		System.out.println("0.001: " + maximum(0, 5, 0.001));

		System.out.println("Minium:");
		System.out.println("    1: " + minimum(0, 5, 1));
		System.out.println("  0.1: " + minimum(0, 5, 0.1));
		System.out.println(" 0.01: " + minimum(0, 5, 0.01));
		System.out.println("0.001: " + minimum(0, 5, 0.001));

		testOpdracht3_3();

	}

	/**
	 * A function
	 * @param x
	 * @return double
	 */
	public static double f(double x){
		return x * x - 2.57 * x + 4.67;
	}

	/**
	 * Calculates the maximum of the function "f"
	 * @param lower lower boundary
	 * @param upper upper boundary
	 * @param step step size
	 * @return the calculated maximum of "f"
	 */
	public static double maximum(double lower, double upper, double step){
		double i = lower;
		double result = f(i);
		i += step;
		for (; i <= upper; i += step){
			double value = f(i);
			if (value > result) result = value;
		}
		return result;
	}

	/**
	 * Calculates the minimum of the function "f"
	 * @param lower lower boundary
	 * @param upper upper boundary
	 * @param step step size
	 * @return the calculated minimum of "f"
	 */
	public static double minimum(double lower, double upper, double step){
		double i = lower;
		double result = f(i);
		i += step;
		for (; i <= upper; i += step){
			double value = f(i);
			if (value < result) result = value;
		}
		return result;
	}

	public static void testOpdracht3_3(){
		System.out.println("f(7.0)  == 35.68 is " + (Math.abs(f(7.0) - 35.68) <= 1E-10));
		System.out.println("f(-6.0) == 56.09 is " + (Math.abs(f(-6.0) - 56.09) <= 1E-10));
		System.out.println("maximum(0,50,1)     == 2376.1700000000 is " + (Math.abs(maximum(0,50,1) -     2376.1700000000) <= 1E-9));
		System.out.println("maximum(0,50,0.1)   == 2366.4370000000 is " + (Math.abs(maximum(0,50,0.1) -   2366.4370000000) <= 1E-9));
		System.out.println("maximum(0,50,0.01)  == 2376.1699999998 is " + (Math.abs(maximum(0,50,0.01) -  2376.16999999986) <= 1E-9));
		System.out.println("maximim(0,50,0.001) == 2376.1699999748 is " + (Math.abs(maximum(0,50,0.001) - 2376.16999999748) <=1E-9));
		System.out.println("minimum(0,50,1)     == 3.1             is " + (Math.abs(minimum(0,50,1) - 3.1) <= 1E-9));
		System.out.println("minimum(0,50,0.1)   == 3.019           is " + (Math.abs(minimum(0,50,0.1) - 3.019) <= 1E-9));
		System.out.println("minimum(0,50,0.01)  == 3.0187999999999 is " + (Math.abs(minimum(0,50,0.01) - 3.0187999999999) <= 1E-9));
		System.out.println("minimim(0,50,0.001) == 3.018775        is " + (Math.abs(minimum(0,50,0.001) - 3.018775) <= 1E-9));
	}

}

