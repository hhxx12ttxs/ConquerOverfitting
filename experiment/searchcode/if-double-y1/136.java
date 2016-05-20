//	Practicum IN1608WI	Opdracht 3
//	Auteur Arian Stolwijk,	Studienummer 4001079
//	Datum 28 9 2011

package opdracht3;

public class Opdracht3_5 {

	public static void main(String[] args) {

		System.out.println(
			"De omtrek van de driehoek met hoekpunten\n"
			+ " (0,0),(1,0) en (0,1) is " + omtrek(0, 0, 1, 0, 0, 1)
		);
		System.out.println(
			"De oppervlakte van de driehoek met hoekpunten\n"
			+ " (0,0),(1,0) en (0,1) is " + oppervlakte(0, 0, 1, 0, 0, 1)
		);

		testOpdracht3_5();

	}

	/**
	 * Returns the highest value of x or y
	 * @param x
	 * @param y
	 * @return highest value of x or y
	 */
	public static double max(double x, double y){
		if (x > y) return x;
		return y;
	}

	/**
	 * Returns the absolute value of x
	 * @param x
	 * @return a positive number
	 */
	public static double abs(double x){
		if (x < 0) return x * -1;
		return x;
	}

	/**
	 * Calculates the square
	 * @param x
	 * @return square of x
	 */
	public static double kwadraat(double x){
		return x * x;
	}

	/**
	 * Calculates the square root of a number
	 * @param a
	 * @return double
	 */
	public static double vierkantswortel(double a){
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
	 * Calculates the shortest distance from point 1 to point 2
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return the distance
	 */
	public static double afstand(double x1, double y1, double x2, double y2){
		double x = x2 - x1;
		double y = y2 - y1;
		return vierkantswortel(x * x + y * y);
	}

	/**
	 * Calculates the circumference of a triangle with points 1, 2 and 3
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @param x3
	 * @param y3
	 * @return circumference
	 */
	public static double omtrek(double x1, double y1, double x2, double y2, double x3, double y3){
		return afstand(x1, y1, x2, y2)
		     + afstand(x2, y2, x3, y3)
		     + afstand(x3, y3, x1, y1);
	}

	/**
	 * Calculates the surface of a triangle
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @param x3
	 * @param y3
	 * @return surface
	 */
	public static double oppervlakte(double x1, double y1, double x2, double y2, double x3, double y3){
		double a = afstand(x1, y1, x2, y2);
		double b = afstand(x2, y2, x3, y3);
		double c = afstand(x3, y3, x1, y1);
		double s = (a + b + c) / 2;
		return vierkantswortel(s * (s - a) * (s - b) * (s -c));
	}

	public static void testOpdracht3_5(){
		System.out.println("abs(omtrek(0,0, 1,0, 1,1)-3.414) < 1e-3             => " + (abs(omtrek(0,0, 1,0, 1,1)-3.414) < 1e-3));
		System.out.println("abs(omtrek(-1,0, 3,0, 1,1)-8.472) < 1e-3            => " + (abs(omtrek(-1,0, 3,0, 1,1)-8.472) < 1e-3));
		System.out.println("abs(omtrek(1,2, -3,4, 5,6)-18.375) < 1e-3           => " + (abs(omtrek(1,2, -3,4, 5,6)-18.375) < 1e-3));
		System.out.println("abs(omtrek(0.1,0.2, 0.3,0.4, 0.5,0.6)-1.131) < 1e-3 => " + (abs(omtrek(0.1,0.2, 0.3,0.4, 0.5,0.6)-1.131) < 1e-3));
		System.out.println("abs(oppervlakte(0,0, 1,0, 1,1) - 0.50) < 1e-3       => " + (abs(oppervlakte(0,0, 1,0, 1,1) - 0.50) < 1e-3));
		System.out.println("abs(oppervlakte(-1,0, 3,0, 1,1) - 2.00) < 1e-3      => " + (abs(oppervlakte(-1,0, 3,0, 1,1) - 2.00) < 1e-3));
		System.out.println("abs(oppervlakte(1,2, -3,4, 5,6) - 12.00) < 1e-3     => " + (abs(oppervlakte(1,2, -3,4, 5,6) - 12.00) < 1e-3));
	}

}

