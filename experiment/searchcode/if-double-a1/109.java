package com.bdcorps.triangleSource;

/**Purpose:
 * The solver class which contains the main method to solve the triangle. 
 * The menu screen to the user is also shown from this class.
 * Contains error checking methods to check if same option was input more than once.
 * 
 * Part Of: Text Based Triangle Solving Program
 * @author Sukhpal S. Saini and Vasu Kamra
 * Last Modified: 05-04-2014 at 2:20 PM
 */

import java.util.Scanner;

/**
 * @author Sukhpal S. Saini and Vasu Kamra
 * 
 */
public class SolveMatic {

	/**
	 * Rounds the parameter to one decimal place and returns the value
	 * 
	 * @param x
	 * @return Rounded value to one decimal place
	 */
	public static double round(double x) {
		double place = 10;
		x = Math.round(x * place) / place; // rounds number to 1 decimal place
		return x;
	}

	/**
	 * This method provides inerface when user wants to enter a side Error
	 * checking is done
	 * 
	 * @return The length of the valid side is returned
	 */
	public static double inputSide() {
		Scanner in = new Scanner(System.in);
		double side = -1; // holds the side length that the user enters for the
							// triangle
		do {
			System.out.println("Enter a side length (in units):"); // prompts
																	// for value
																	// of side
																	// length of
																	// the
																	// chosen
																	// side
																	// length
			if (in.hasNextDouble()) { // ensures that the value entered by the
										// user is a double
				side = in.nextDouble();
				if (side < 0) // checks to see if the side length the user
								// enters is negative
					System.out
							.println("Error! Side length cannot be negative.");
			} else {
				in.nextLine(); // skips the current line of invalid input
				System.out.println("Error! Input type must be a number.");
			}

		} while (side < 0); // keeps prompting and getting side length from user
							// until side length input is valid
		return side;
	}

	/**
	 * This method provides interface when user wants to enter an angle Error
	 * checking is done
	 * 
	 * @return The value of the valid angle is returned
	 */
	public static double inputAngle() {
		Scanner in = new Scanner(System.in);
		double angle = -1; // holds the angle that the user enters for the
							// triangle
		do {
			System.out.println("Enter an angle (in degrees):"); // prompts for
																// value of
																// angle of the
																// chosen angle
			if (in.hasNextDouble()) { // ensures that the value the user enters
										// is a double
				angle = in.nextDouble();
				if (angle <= 0 || angle >= 180) // checks if the angle is
												// between 0 and 180 degrees
					System.out
							.println("Error! Angle must be between 0 and 180 degrees.");
			} else {
				in.nextLine(); // skips the current line of invalid input
				System.out.println("Error! Input type must be a number.");
			}
		} while (angle <= 0 || angle >= 180); // keeps prompting and getting
												// angle from user until it is
												// valid
		return angle;
	}

	/**
	 * Prompts the user to select an option for the triangle. You can select to
	 * display specifications, perimeter or area of the triangle or reset the
	 * sides of the triangle or exit the program.
	 * 
	 * @return the option that the user selects
	 */
	public static int inputMenu() {
		Scanner in = new Scanner(System.in);
		int option = -1; // holds the option that the user enters for their menu
							// choice
		do {
			// outputs menu
			System.out
					.println("1 - Specifications\n2 - Perimeter\n3 - Area\n4 - Reset Sides\n5 - Exit the program\nSelect option:");
			if (in.hasNextInt()) { // checks if the value the user enters is an
									// integer
				option = in.nextInt();
				if (option < 1 || option > 5) // checks if the option entered is
												// between 1 and 5
					System.out
							.println("Error! Option must be 1, 2, 3, 4, or 5.");
			} else {
				in.nextLine(); // skips the current line of invalid input
				System.out.println("Error! Option must be 1, 2, 3, 4, or 5.");
			}

		} while (option < 1 || option > 5); // keeps looping prompt until a
											// valid option is selected
		return option;
	}

	/**
	 * Prompts for a option to enter an angle or side length for Includes Error
	 * checking
	 * 
	 * @return the option that the user enters
	 */
	public static String inputoption() {
		Scanner in = new Scanner(System.in);
		String x; // a temporary variable which will store the option that the
					// user enters
		do {
			System.out.println("Select a field (a, b, c, A, B, C): ");
			x = in.nextLine();
			if ((!x.equals("a") && !x.equals("b") && !x.equals("c")
					&& !x.equals("A") && !x.equals("B") && !x.equals("C"))) // checks
																			// if
																			// the
																			// value
																			// that
																			// the
																			// user
																			// enters
																			// is
																			// a
																			// valid
																			// choice
				System.out.println("Error! Input is not a valid field.");
		} while ((!x.equals("a") && !x.equals("b") && !x.equals("c")
				&& !x.equals("A") && !x.equals("B") && !x.equals("C"))); // keeps
																			// looping
																			// until
																			// a
																			// valid
																			// input
																			// is
																			// entered
		return x;
	}

	/**
	 * Prompts for a option to enter side length for Only used when
	 * "Reset Sides" is asked for Includes Error checking
	 * 
	 * @return the option that the user enters
	 */
	public static String resetSidesInput() {
		Scanner in = new Scanner(System.in);
		String x; // a temporary variable which will store the option that the
					// user enters
		do {
			System.out.println("Select a field (a, b, c): ");
			x = in.nextLine();
			if ((!x.equals("a") && !x.equals("b") && !x.equals("c"))) // checks
																		// if
																		// the
																		// value
																		// that
																		// the
																		// user
																		// enters
																		// is a
																		// valid
																		// choice
				System.out.println("Error! Input is not a valid field.");
		} while ((!x.equals("a") && !x.equals("b") && !x.equals("c"))); // keeps
																		// looping
																		// until
																		// a
																		// valid
																		// input
																		// is
																		// entered
		return x;
	}

	/**
	 * Checks whether the option selected already had been entered or not
	 * 
	 * @param String
	 *            x, String optionsUsed
	 * @return true or false depending on if the option has already been
	 *         entered.
	 */
	public static boolean optionUsed(String x, String[] optionsUsed) {
		boolean temp = false; // true if the option is already used
		for (int i = 0; i < optionsUsed.length; i++) {
			if (x.equals(optionsUsed[i])) { // checks to see if the value of
											// index i in the array is already
											// in the array
				temp = true;
				System.out.println("Error! This option is already used.");
			}
		}
		return temp;
	}

	public static void main(String[] args) {
		boolean reset = false; // to check if the user wants to rest the
								// specifications of the triangle or to exit the
								// prorgram
		String[] optionsArray = new String[6]; // array that holds the options
												// that are already have values
		int z; // index to keep track of optionsUsed array
		double A1, B1, C1; // the angles of the first solution of the triangle
		double a1, b1, c1; // the side lengths of the first solution of the
							// triangle
		int angleCount; // number of angles counter
		int sideCount; // number of side lengths counter
		String option; // holds the option that denotes the field the user wants
						// to enter values for
		double tempS; // a temporary variable that holds the side length that
						// the user enters
		double tempA; // a temporary variable that holds the angle that the user
						// enters
		int menuOption; // a variable that holds the menu option that the user
						// selects

		TriangleUnit t = new TriangleUnit(); // declares the TriangleUnit t,
												// which will be constructed
												// later in the program

		System.out
				.println("Welcome to the text version of the Vasu/Sukhpal Triangle Solver program\n");
		do {
			do {
				// sets values to a default triangle
				A1 = B1 = C1 = 60;
				a1 = b1 = c1 = 3;
				angleCount = 0;
				sideCount = 0;
				for (int i = 0; i < optionsArray.length; i++)
					// runs through the array and makes each value in the array
		
					optionsArray[i] = "";
				z = 0;

				do {
					do {
						option = inputoption(); // prompts for which field to
												// enter a value for
					} while (optionUsed(option, optionsArray) == true); // checks
																		// to
																		// see
																		// if
																		// option
																		// is
																		// already
																		// input
																		// into
																		// the
																		// program
																		// and
																		// loops
																		// back
																		// if so
					if (option.equals("a") || option.equals("b")
							|| option.equals("c")) { // checks to see if a side
														// length option was
														// inputed
						tempS = inputSide(); // stores user input in temporary
												// variable
						// the following checks to see if option is a, b, or c
						// and assigns value to appropriate variable
						if (option.equals("a")) {
							a1 = tempS;
							t.puta1(a1);
						} else if (option.equals("b")) {
							b1 = tempS;
							t.putb1(b1);
						} else {
							c1 = tempS;
							t.putc1(c1);
						}
						sideCount++;
						optionsArray[z] = option; // stores the option into
													// optionsUsed
						z++;
					} else if ((option.equals("A") || option.equals("B") || option
							.equals("C")) && angleCount < 3) { // checks to see
																// if option
																// inputed is
																// angle ltter
						tempA = inputAngle(); // stores user input in temporary
												// variable
						// the following checks to see if option is A, B or C
						// and assigns value to appropriate variable
						if (option.equals("A")) {
							A1 = tempA;
							t.putA1(A1);
						} else if (option.equals("B")) {
							B1 = tempA;
							t.putB1(B1);
						} else {
							C1 = tempA;
							t.putC1(C1);
						}
						angleCount++;
						optionsArray[z] = option; // stores the option into
													// optionsUsed
						z++;
					} else
						System.out
								.println("Error! Cannot enter more than 3 angles."); // prevents
																						// program
																						// from
																						// getting
																						// stuck
																						// in
																						// AAA
																						// case
				} while (!((angleCount + sideCount == 3 && sideCount > 0) || (angleCount == 3 && sideCount == 1))); // conditions
																													// met
				if (t.getSolutions() == 0) // checks to see if inputed values of
											// the triangle construct a valid
											// triangle
				{
					optionsArray = new String[6];
					System.out
							.println("A triangle cannot be constructed based on these specifications.\n\nRestarting program.\n");
				}
			} while (t.getSolutions() == 0); // loops back to beginning if a
												// triangle cannot be
												// constructed
			do {
				menuOption = inputMenu(); // prompts and gets the menu option

				if (menuOption == 1) { // display specifications is selected
					// outputs triangle specifications
					if (t.getSolutions() == 1) // different outputs depending on
												// whether there is 1 or 2
												// solutions
						System.out.println("Solution 1:");
					System.out.println("Side 'a' is equal to "
							+ round(t.geta1()) + " units.");
					System.out.println("Side 'b' is equal to "
							+ round(t.getb1()) + " units.");
					System.out.println("Side 'c' is equal to "
							+ round(t.getc1()) + " units.");
					System.out.println("Angle 'A' is equal to "
							+ round(t.getA1()) + " degrees.");
					System.out.println("Angle 'B' is equal to "
							+ round(t.getB1()) + " degrees.");
					System.out.println("Angle 'C' is equal to "
							+ round(t.getC1()) + " degrees.\n");
					if (t.getSolutions() == 2) { // checks to see if two
													// triangles can be created,
													// if so the program outputs
													// the second triangle's
													// specifications neatly
						System.out.println("\nSolution 2:");
						System.out.println("Side 'a' is equal to "
								+ round(t.geta2()) + " units.");
						System.out.println("Side 'b' is equal to "
								+ round(t.getb2()) + " units.");
						System.out.println("Side 'c' is equal to "
								+ round(t.getc2()) + " units.");
						System.out.println("Angle 'A' is equal to "
								+ round(t.getA2()) + " degrees.");
						System.out.println("Angle 'B' is equal to "
								+ round(t.getB2()) + " degrees.");
						System.out.println("Angle 'C' is equal to "
								+ round(t.getC2()) + " degrees.\n");
					}
				} else if (menuOption == 2) { // display perimeter was selected
					// outputs the perimeter of the triangle
					if (t.getSolutions() == 2) // different outputs depending on
												// whether there is 1 or 2
												// solutions
						System.out.println("Solution 1:");
					System.out.println("Perimeter = "
							+ round(t.getPerimeter1()) + " units\n");
					if (t.getSolutions() == 2) { // displays second perimeter if
													// applicable
						System.out.println("Solution 2:");
						System.out.println("Perimeter = "
								+ round(t.getPerimeter2()) + " units\n");
					}
				} else if (menuOption == 3) { // display area was selected
					// outputs the area of the triangle
					if (t.getSolutions() == 2) // different outputs depending on
												// whether there is 1 or 2
												// solutions
						System.out.println("Solution 1:");
					System.out.println("Area = " + round(t.getArea1())
							+ " units^2\n");
					if (t.getSolutions() == 2) { // displays the second area if
													// applicable
						System.out.println("Solution 2:");
						System.out.println("Area = " + round(t.getArea2())
								+ " units^2\n");
					}
				} else if (menuOption == 4) {
					option = resetSidesInput(); // prompts for which field to
												// enter a value for

					if (option.equals("a") || option.equals("b")
							|| option.equals("c")) { // checks to see if a side
														// length option was
														// inputed
						tempS = inputSide(); // stores user input in temporary
												// variable
						// the following checks to see if option is a, b, or c
						// and assigns value to appropriate variable
						if (option.equals("a")) {
							a1 = tempS;
							t.puta1(a1);
						} else if (option.equals("b")) {
							b1 = tempS;
							t.putb1(b1);
						} else {
							c1 = tempS;
							t.putc1(c1);
						}
						sideCount++;

						t = new TriangleUnit();
						t.puta1(a1);
						t.putb1(b1);
						t.putc1(c1);
					}

				} else if (menuOption == 5) // exit program was selected
					reset = false;
				else { // Reset Sides was selected
					System.out.println("Restarting program.\n");
					reset = true;
				}
			} while (menuOption >= 1 && menuOption <= 4); // loops back if
															// display
															// specifications,
															// perimeter, or
															// area selected
		} while (reset); // loops back to the beginning if rest specifications
							// was selected
		System.out.println("Safely Exited");
	}
}

