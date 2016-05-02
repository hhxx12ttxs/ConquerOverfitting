package com.bdcorps.triangleSource;

import java.util.ArrayList;

/**
 * Contains the side lengths and angles of all the solutions of a triangle (can
 * be 0, 1, or 2) Constructs the triangle by solving a triangle(SSS, SAS, ASA,
 * SAA, SSA) Contains error checking methods Contains perimeter and area
 * calculating methods
 * 
 * Part Of: Text Based and GUI Based Triangle Solving Program
 * @author Sukhpal S. Saini Vasu Kamra
 */

public class TriangleUnit {
	private double A1, B1, C1 = 60; // these fields represent the angles of
									// first
									// solution of the triangle
	private double A2, B2, C2 = 60; // these fields represent the angles of the
									// second solution of the triangle
	private double a1, b1, c1 = 3; // these fields represent the sides of first
									// solution of the triangle
	private double a2, b2, c2 = 3; // these fields represent the sides of second
									// solution of the triangle
	private ArrayList<String> lettersArray = new ArrayList<String>();
	private int angleCount = 0;// counter for angles
	private int sideCount = 0;// counter for sides
	private int solutions = 1;// default number of solutions

	// Accessors for the variables in the class
	public int getSolutions() {
		return solutions;
	}

	public double getA1() {
		return A1;
	}

	public double getB1() {
		return B1;
	}

	public double getC1() {
		return C1;
	}

	public double geta1() {
		return a1;
	}

	public double getb1() {
		return b1;
	}

	public double getc1() {
		return c1;
	}

	public double getA2() {
		return A2;
	}

	public double getB2() {
		return B2;
	}

	public double getC2() {
		return C2;
	}

	public double geta2() {
		return a2;
	}

	public double getb2() {
		return b2;
	}

	public double getc2() {
		return c2;
	}

	public void setSolutions(int solutions) {
		this.solutions = solutions;
	}

	public double getPerimeter1() {
		return a1 + b1 + c1;
	}

	public double getPerimeter2() {
		return a2 + b2 + c2;
	}

	public double getArea1() {
		double s = getPerimeter1() / 2;
		return Math.sqrt(s * (s - a1) * (s - b1) * (s - c1));
	}

	public double getArea2() {
		double s = getPerimeter2() / 2;
		return Math.sqrt(s * (s - a2) * (s - b2) * (s - c2));
	}

	// Mutators for the variables in the class
	public void putA1(double x) {
		A1 = x;
		lettersArray.add("A");
		classifySolve();
	}

	public void putB1(double x) {
		B1 = x;
		lettersArray.add("B");
		classifySolve();
	}

	public void putC1(double x) {
		C1 = x;
		lettersArray.add("C");
		classifySolve();
	}

	public void puta1(double x) {
		a1 = x;
		lettersArray.add("a");
		classifySolve();
	}

	public void putb1(double x) {
		b1 = x;
		lettersArray.add("b");
		classifySolve();
	}

	public void putc1(double x) {
		c1 = x;
		lettersArray.add("c");
		classifySolve();
	}

	public void putA2(double x) {
		A2 = x;
	}

	public void putB2(double x) {
		B2 = x;
	}

	public void putC2(double x) {
		C2 = x;
	}

	public void puta2(double x) {
		a2 = x;
	}

	public void putb2(double x) {
		b2 = x;
	}

	public void putc2(double x) {
		c2 = x;
	}

	/**
	 * The constructor for the TriangleUnit class Sets the solutions to default
	 * values
	 */
	public TriangleUnit() {
		// the instance fields of the first solution are set to their
		// corresponding parameter values
		A1 = 60;
		B1 = 60;
		C1 = 60;
		a1 = 3;
		b1 = 3;
		c1 = 3;

		A2 = 60;
		B2 = 60;
		C2 = 60;
		a2 = 3;
		b2 = 3;
		c2 = 3;
	}

	/**
	 * This method runs every time a triangle value is put in It checks if three
	 * values are received and are ready to be solved or not Classfies, if three
	 * values are recieved to one of the five cases
	 */
	private void classifySolve() {
		angleCount = 0;
		sideCount = 0;
		boolean contained = true;
		for (int i = 0; i < lettersArray.size(); i++) {
			String letter1 = lettersArray.get(i);
			if (Character.isUpperCase(letter1.charAt(0))) {
				angleCount++;
			} else if (!Character.isUpperCase(letter1.charAt(0))) {
				sideCount++;
			}

			for (int j = 0; j < lettersArray.size(); j++) {
				String letter2 = lettersArray.get(j);
				if (j != i) {

					// check containment
					// quits if contained becomes false
					if (contained) {
						if (letter2.toUpperCase().equals(letter1.toUpperCase())) {
							contained = false;
						} else {
							contained = true;
						}
					}
				}
			}
		}

		if (sideCount == 3) {
			SSS();
		} else if (sideCount == 2 && angleCount == 1) {
			if (contained) {
				SAS();
			} else {
				SSA();
			}
		} else if (sideCount == 1 && angleCount == 2) {
			AASorASA();
		}
		solutions = solutions();
	}

	public void SSS() {

		A1 = Math.toDegrees(Math
				.acos(((b1 * b1 + c1 * c1 - a1 * a1) / (2 * b1 * c1))));
		B1 = Math.toDegrees(Math
				.acos(((a1 * a1 + c1 * c1 - b1 * b1) / (2 * a1 * c1))));
		C1 = Math.toDegrees(Math
				.acos(((a1 * a1 + b1 * b1 - c1 * c1) / (2 * a1 * b1))));

	}

	public void AASorASA() {
		// calculates the remaining angle
		if (lettersArray.indexOf("A") == -1)
			A1 = 180 - B1 - C1;
		else if (lettersArray.indexOf("B") == -1)
			B1 = 180 - A1 - C1;
		else
			C1 = 180 - A1 - B1;
		// calculates the other two sides using the sine law twice
		if (lettersArray.indexOf("a") != -1) {
			b1 = Math.sin(Math.toRadians(B1))
					* (a1 / Math.sin(Math.toRadians(A1)));
			c1 = Math.sin(Math.toRadians(C1))
					* (a1 / Math.sin(Math.toRadians(A1)));
		} else if (lettersArray.indexOf("b") != -1) {
			a1 = Math.sin(Math.toRadians(A1))
					* (b1 / Math.sin(Math.toRadians(B1)));
			c1 = Math.sin(Math.toRadians(C1))
					* (b1 / Math.sin(Math.toRadians(B1)));
		} else if (lettersArray.indexOf("c") != -1) {
			b1 = Math.sin(Math.toRadians(B1))
					* (c1 / Math.sin(Math.toRadians(C1)));
			a1 = Math.sin(Math.toRadians(A1))
					* (c1 / Math.sin(Math.toRadians(C1)));
		}
	}

	public void SAS() {
		// finds out the remaining side of the triangle
		if (lettersArray.indexOf("a") == -1)
			a1 = Math.sqrt(b1 * b1 + c1 * c1 - 2 * b1 * c1
					* Math.cos(Math.toRadians(A1)));
		if (lettersArray.indexOf("b") == -1)
			b1 = Math.sqrt(a1 * a1 + c1 * c1 - 2 * a1 * c1
					* Math.cos(Math.toRadians(B1)));
		if (lettersArray.indexOf("c") == -1)
			c1 = Math.sqrt(a1 * a1 + b1 * b1 - 2 * a1 * b1
					* Math.cos(Math.toRadians(C1)));
		// calculates the remaining two angles of the triangle using cosine law
		if (lettersArray.indexOf("A") != -1) {
			B1 = Math.toDegrees(Math
					.acos(((a1 * a1 + c1 * c1 - b1 * b1) / (2 * a1 * c1))));
			C1 = 180 - A1 - B1;
		}
		if (lettersArray.indexOf("B") != -1) {
			C1 = Math.toDegrees(Math
					.acos(((a1 * a1 + b1 * b1 - c1 * c1) / (2 * a1 * b1))));
			A1 = 180 - B1 - C1;
		}
		if (lettersArray.indexOf("C") != -1) {
			A1 = Math.toDegrees(Math
					.acos(((b1 * b1 + c1 * c1 - a1 * a1) / (2 * b1 * c1))));
			B1 = 180 - A1 - C1;
		}
	}

	public void SSA() {

		double sideUnknown, sideKnown, sideKnown2; // variables that store the
													// unknown side, known side,
													// and second known side of
													// the triangle
		double angleKnown, angleUnknown, angleUnknown2; // variables that store
														// the known angle,
														// unknown angle, and
														// second unknown angle
														// of the triangle
		double angleUnknownA, angleUnknown2A, sideUnknownA; // variables that
															// store the unknown
															// angle, second
															// unknown angle,
															// and unknown side
															// of the ambiguous
															// case
		double ratio; // a variable that holds the sine ratio which helps when
						// using the sine law
		// the following selection structure initializes the fields of the
		// triangles to the variables declared in the beginning of the method
		if ((lettersArray.indexOf("a") != -1)
				&& (lettersArray.indexOf("A") != -1)) { // a and A are given
			sideKnown = a1;
			angleKnown = A1;
			a2 = sideKnown;
			A2 = angleKnown;
			if ((lettersArray.indexOf("b") != -1)) {
				sideKnown2 = b1;
				b2 = sideKnown2;
			} else {
				sideKnown2 = c1;
				c2 = sideKnown2;
			}
		} else if ((lettersArray.indexOf("b") != -1)
				&& (lettersArray.indexOf("B") != -1)) { // b and B are given
			sideKnown = b1;
			angleKnown = B1;
			b2 = sideKnown;
			B2 = angleKnown;
			if ((lettersArray.indexOf("a") != -1)) {
				sideKnown2 = a1;
				a2 = sideKnown2;
			} else {
				sideKnown2 = c1;
				c2 = sideKnown2;
			}
		} else { // c and C are given
			sideKnown = c1;
			angleKnown = C1;
			c2 = sideKnown;
			C2 = angleKnown;
			if ((lettersArray.indexOf("b") != -1)) {
				sideKnown2 = b1;
				b2 = sideKnown2;
			} else {
				sideKnown2 = a1;
				a2 = sideKnown2;
			}
		}
		// for first solution
		ratio = Math.sin(Math.toRadians(angleKnown)) / sideKnown;
		angleUnknown = Math.toDegrees(Math.asin(ratio * sideKnown2));
		angleUnknown2 = 180 - angleUnknown - angleKnown;
		sideUnknown = Math.sin(Math.toRadians(angleUnknown2)) / ratio;
		// for second solution
		angleUnknownA = 180 - Math.toDegrees(Math.asin(ratio * sideKnown2));
		angleUnknown2A = 180 - angleUnknownA - angleKnown;
		sideUnknownA = Math.sin(Math.toRadians(angleUnknown2A)) / ratio;
		if ((lettersArray.indexOf("a") != -1)
				&& (lettersArray.indexOf("A") != -1)) {
			if ((lettersArray.indexOf("b") == -1)) {
				b1 = sideUnknown;
				C1 = angleUnknown;
				B1 = angleUnknown2;
				b2 = sideUnknownA;
				C2 = angleUnknownA;
				B2 = angleUnknown2A;
			} else { // abA was initially given
				c1 = sideUnknown;
				B1 = angleUnknown;
				C1 = angleUnknown2;
				c2 = sideUnknownA;
				B2 = angleUnknownA;
				C2 = angleUnknown2A;
			}
		} else if ((lettersArray.indexOf("b") != -1)
				&& (lettersArray.indexOf("B") != -1)) {
			if ((lettersArray.indexOf("a") == -1)) {
				a1 = sideUnknown;
				C1 = angleUnknown;
				A1 = angleUnknown2;
				a2 = sideUnknownA;
				C2 = angleUnknownA;
				A2 = angleUnknown2A;
			} else {
				c1 = sideUnknown;
				A1 = angleUnknown;
				C1 = angleUnknown2;
				c2 = sideUnknownA;
				A2 = angleUnknownA;
				C2 = angleUnknown2A;
			}
		} else if ((lettersArray.indexOf("c") != -1)
				&& (lettersArray.indexOf("C") != -1)) {
			if ((lettersArray.indexOf("a") == -1)) {
				a1 = sideUnknown;
				B1 = angleUnknown;
				A1 = angleUnknown2;
				a2 = sideUnknownA;
				B2 = angleUnknownA;
				A2 = angleUnknown2A;
			} else {
				b1 = sideUnknown;
				A1 = angleUnknown;
				B1 = angleUnknown2;
				b2 = sideUnknownA;
				A2 = angleUnknownA;
				B2 = angleUnknown2A;
			}
		}
	}

	/**
	 * Calculates the number of solutions a triangle will have
	 * 
	 * @return The number of solutions
	 */
	public int solutions() {
		int tempSols = 1; // holds solution value to be returned
		double height; // variable which will hold the height of the triangle
		double s1 = 3, s2 = 3, angle = 3;

			
		if (angleCount == 2 && A1 + B1 + C1 > 180) // checks if there are no 2
													// obtuse angles, no 2 right
													// angles, and sum of 2
													// angles less than 180 when
													// 2 angles are given
			tempSols = 0;
		if (angleCount == 3 && A1 + B1 + C1 != 180) // checks whether the the
													// angles add up to 180 when
													// 3 are given
			tempSols = 0;
		else if (sideCount == 2) { // 2 side lengths are given
			// the following sets s1, s2, and angle to certain values to
			// determine if there are 0, 1, or 2 solutions in a SSA case
			if ((lettersArray.indexOf("a") != -1)
					&& (lettersArray.indexOf("b") != -1)
					&& (lettersArray.indexOf("A") != -1)) { // abA
				s1 = a1;
				s2 = b1;
				angle = A1;
			} else if ((lettersArray.indexOf("b") != -1)
					&& (lettersArray.indexOf("c") != -1)
					&& (lettersArray.indexOf("B") != -1)) { // bcB
				s1 = b1;
				s2 = c1;
				angle = B1;
			} else if ((lettersArray.indexOf("c") != -1)
					&& (lettersArray.indexOf("a") != -1)
					&& (lettersArray.indexOf("C") != -1)) { // caC
				s1 = c1;
				s2 = a1;
				angle = C1;
			} else if ((lettersArray.indexOf("b") != -1)
					&& (lettersArray.indexOf("a") != -1)
					&& (lettersArray.indexOf("B") != -1)) { // baB
				s1 = b1;
				s2 = a1;
				angle = B1;
			} else if ((lettersArray.indexOf("c") != -1)
					&& (lettersArray.indexOf("b") != -1)
					&& (lettersArray.indexOf("C") != -1)) { // cbC
				s1 = c1;
				s2 = b1;
				angle = C1;
			} else if ((lettersArray.indexOf("a") != -1)
					&& (lettersArray.indexOf("c") != -1)
					&& (lettersArray.indexOf("A") != -1)) { // acA
				s1 = a1;
				s2 = c1;
				angle = A1;
			}
			if (s1 != 3 && s2 != 3 && angle != 60) { // SSA
				height = s2 * Math.sin(Math.toRadians(angle));
				if (angle < 90 && s1 < height)
					tempSols = 0;
				else if (angle < 90 && height < s1 && s1 < s2)
					tempSols = 2;
				else if (angle > 90 && s1 <= s2)
					tempSols = 0;
			}
		}

		else if (sideCount == 3) { // if 3 sides are given, it ensures that no
									// side is longer than the sum of the other
									// 2
			if ((a1 + b1 <= c1) || (a1 + c1 <= b1) || (b1 + c1 <= a1)) {
				tempSols = 0;
			}
		}
		solutions = tempSols;

		if (solutions == 0) {
			lettersArray.clear();
		}

		return tempSols;
	}

}
