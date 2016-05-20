//	Practicum IN1608WI	Opdracht 3
//	Auteur Arian Stolwijk,	Studienummer 4001079
//	Datum 28 9 2011

package opdracht3;

public class Opdracht3_2 {

	public static void main(String[] args) {

		System.out.println("    1: " + integraal(0, 5, 1));
		System.out.println("  0.1: " + integraal(0, 5, 0.1));
		System.out.println(" 0.01: " + integraal(0, 5, 0.01));
		System.out.println("0.001: " + integraal(0, 5, 0.001));

		testOpdracht3_2();

	}

	/**
	 * Calculates the square root of a number
	 * @param a
	 * @return double
	 */
	public static double f(double a){
		double epsilon = 1E-10;
		double x = 1, b;

		do {
			b = a - x * x;
			if (b > -epsilon && b < epsilon) break;
			x = (a + x * x) / (2 * x);
		} while (true);

		return x;
	}

	/**
	 * Integrates the function "f" from lower to upper
	 * @param lower lower boundary
	 * @param upper upper boundary
	 * @param step step size
	 * @return the calculated integral of "f"
	 */
	public static double integraal(double lower, double upper, double step){
		double result = 0;
		for (double i = lower; i <= upper; i += step){
			result += f(i) * step;
		}
		return result;
	}

	public static void testOpdracht3_2(){
		System.out.println("f(3.0) == 1.7320508076 is " + (Math.abs(f(3.0) - 1.7320508076) <= 1E-9));
		System.out.println("f(9.0) == 3.0          is " + (Math.abs(f(9.0) - 3.0) <= 1E-9));
		System.out.println("integraal(0,50,1)     == 239.0358082329 is " + (Math.abs(integraal(0,50,1) - 239.0358082329) <= 1E-9));
		System.out.println("integraal(0,50,0.1)   == 235.3421927538 is " + (Math.abs(integraal(0,50,0.1) - 235.3421927538) <= 1E-9));
		System.out.println("integraal(0,50,0.01)  == 235.7374085139 is " + (Math.abs(integraal(0,50,0.01) - 235.7374085139) <= 1E-9));
		System.out.println("integraal(0,50,0.001) == 235.7057893690 is " + (Math.abs(integraal(0,50,0.001) - 235.7057893690) <= 1E-9));
	}

}

