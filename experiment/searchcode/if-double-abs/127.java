//	Practicum IN1608WI	Opdracht 3
//	Auteur Arian Stolwijk,	Studienummer 4001079
//	Datum 28 9 2011

package opdracht3;

public class Opdracht3_4 {

	public static void main(String[] args) {

		System.out.println("Grootste van 3 en 4 is " + max(3, 4));
		System.out.println("Absolute waarde van -5 is " + abs(-5));
		System.out.println("Kwadraat van -3 is " + kwadraat(-3));
		System.out.println("Wortel van 3 is " + vierkantswortel(3));
		System.out.println("Afstand van (2,3) tot (3,4) is " + afstand(2, 3, 3, 4));

		testOpdracht3_4();

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
		if (x < 0) x *= -1;
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

	public static void testOpdracht3_4(){
		System.out.println("max(3.0,-4.0)==3.0                        => " + (max(3.0,-4.0)==3.0));
		System.out.println("max(1.0,5.0)==5.0                         => " + (max(1.0,5.0)==5.0));
		System.out.println("max(5.0,1.0)==5.0                         => " + (max(5.0,1.0)==5.0));
		System.out.println("max(2.5,2.5)== 2.5                        => " + (max(2.5,2.5)== 2.5));
		System.out.println("abs(-3.0)==3.0                            => " + (abs(-3.0)==3.0));
		System.out.println("abs(0.3)==0.3                             => " + (abs(0.3)==0.3));
		System.out.println("abs(kwadraat(3.0)-9.000)<1e-3             => " + (abs(kwadraat(3.0)-9.000)<1e-3));
		System.out.println("abs(kwadraat(-3.2)-10.240)<1e-3           => " + (abs(kwadraat(-3.2)-10.240)<1e-3));
		System.out.println("abs(vierkantswortel(3.0)-1.732)<1e-3      => " + (abs(vierkantswortel(3.0)-1.732)<1e-3));
		System.out.println("abs(vierkantswortel(25.0)-5.000)<1e-3     => " + (abs(vierkantswortel(25.0)-5.000)<1e-3));
		System.out.println("abs(vierkantswortel(3.8)-1.949)<1e-3      => " + (abs(vierkantswortel(3.8)-1.949)<1e-3));
		System.out.println("abs(afstand(2.0,3.0,3.0,3.0)-1.000)<1e-3  => " + (abs(afstand(2.0,3.0,3.0,3.0)-1.000)<1e-3));
		System.out.println("abs(afstand(1.0,3.0,2.5,2.5)-1.581)<1e-3  => " + (abs(afstand(1.0,3.0,2.5,2.5)-1.581)<1e-3));
		System.out.println("abs(afstand(-2.0,3.0,4.0,1.0)-6.324)<1e-3 => " + (abs(afstand(-2.0,3.0,4.0,1.0)-6.324)<1e-3));
		System.out.println("abs(afstand(-2.0,3.0,1.0,4.0)-3.162)<1e-3 => " + (abs(afstand(-2.0,3.0,1.0,4.0)-3.162)<1e-3));
	}

}

