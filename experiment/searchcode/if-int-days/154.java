package assign3;

import java.text.*;
import java.util.*;

/* Author:    David Currie (3336927)
 * This program is for Assignment 3 in Programming 1 at RMIT.
 * This class handles all authentication (input validation and logins).
 */
public class Authentication {
	private static final Scanner INPUT = new Scanner(System.in);
	private static final SimpleDateFormat DF = 
		new SimpleDateFormat("dd/MM/yyyy");

	final static String exceptionError = "\nInvalid entry.";

	/* 
	 * Check login/password is correct, if so return the User object:
	 */
	public static User validUser(ArrayList<User> arrayLogins) {
		String login = validLogin();
		String password = validPassword();

		// Check list of logins if login/pass are valid:
		for (int i=0; i<arrayLogins.size(); i++) {
			if (arrayLogins.get(i).getLogin().matches(login) &&
					arrayLogins.get(i).getPassword().matches(password)) {
				System.out.println("\n" + arrayLogins.get(i).getLogin() +
				" has successfully logged in.");
				return arrayLogins.get(i);
			}
		}

		System.out.println("\nInvalid login/password.");
		return null;
	}

	/*
	 * Input methods. Used to retrieve input from console and apply validation.
	 * They will then return an appropriately validated value.
	 */

	// Prompts the user to hit enter to continue. Will allow anything.
	// No need to validate the entry or advise on error if exception:
	public static void pause() {
		try {
			System.out.print("\nPress <ENTER> to continue. ");
			INPUT.nextLine();
		}
		catch (Exception e) {}
	}

	public static String validLogin() {
		String login = null;
		boolean continueLoop = true;

		do {
			try {               
				do {
					System.out.print("\nEnter login (4-8 characters): ");
					login = INPUT.nextLine();
				} while (login.length() < 4 || login.length() > 8);		

				continueLoop = false;
			} 
			catch (Exception e) {
				System.err.println(exceptionError);
			}
		} while (continueLoop);        

		return login;
	}

	public static String validPassword() {
		String password = null;
		boolean continueLoop = true;

		do {
			try {               
				do {
					System.out.print("\nEnter password (4-8 characters): ");
					password = INPUT.nextLine();
				} while (password.length() < 4 || password.length() > 8);		

				continueLoop = false;
			} 
			catch (Exception e) {
				System.err.println(exceptionError);
			}
		} while (continueLoop);        

		return password;	
	}

	public static String validRequestID() {
		String requestID = null;
		boolean continueLoop = true;
		final String REGEX = "[0-9]*";

		do {
			try {               
				do {            
					System.out.print("Enter request ID " +
					"(between 3-6 digits, numbers only): ");
					requestID = INPUT.nextLine();
				} while (requestID.length() < 3 || requestID.length() > 6 ||
						!requestID.matches(REGEX));

				continueLoop = false;
			} 
			catch (Exception e) {
				System.err.println(exceptionError);
			}
		} while (continueLoop);

		return requestID;
	}

	public static String validStudentID() {
		String studentID = null;
		boolean continueLoop = true;

		do {
			try {               
				do {
					System.out.print("\nEnter student ID " +
					"(7-8 characters, numbers and letters): ");
					studentID = INPUT.nextLine();
				} while (studentID.length() < 7 || studentID.length() > 8);

				continueLoop = false;
			} 
			catch (Exception e) {
				System.err.println(exceptionError);
			}
		} while (continueLoop);        

		return studentID;
	}

	/* 
	 * allowBlank parameter allows for the course code to be left blank.
	 * An ArrayList is used as that is how the values are stored in the
	 * object. It's easier to do it this way instead of converting
	 * from a String to an ArrayList and vice versa.
	 */
	public static ArrayList<String> validCourseCode(boolean allowBlank) {
		ArrayList<String> courseList = new ArrayList<String>();
		String courseCode, prompt;
		boolean continueLoop = true;
		final String REGEX = "COSC[0-9]{4}";

		// Modify prompt based on whether or not it can be blank:
		if (allowBlank) {			
			prompt = "\nEnter course code " +
			"(8 characters, eg. COSC2135 or N/A for none): ";
		} else {
			prompt = "\nEnter course code " +
			"(8 characters, eg. COSC2135): ";
		}

		do {
			try {               
				do {
					System.out.print(prompt);
					courseCode = INPUT.nextLine();

					// Only allow N/A if allowBlank:
					if (courseCode.matches("[Nn]/[Aa]") && allowBlank) {
						return null;
					}
				} while (!courseCode.matches(REGEX));

				courseList.add(courseCode);
				continueLoop = false;
			} 
			catch (Exception e) {
				System.err.println(exceptionError);
			}
		} while (continueLoop);        

		return courseList;
	}

	// Provide a list of courses to select from (must select course in list):
	public static String validCourseList(ArrayList<String> courseList) {
		String courseCode = null;
		int courseNum, courseTotal = courseList.size()-1;
		boolean continueLoop = true;

		do {
			try {               
				do {
					System.out.println("\nValid course codes:");
					for (int i=0; i<courseList.size(); i++) {
						System.out.println(i + ".     " + courseList.get(i)); 
					}
					System.out.print("\nEnter number (0-" + courseTotal
							+ "): ");
					courseNum = INPUT.nextInt();

				} while (courseNum > courseTotal || courseNum < 0);

				clearCarriage();
				courseCode = courseList.get(courseNum);
				continueLoop = false;
			} 
			catch (Exception e) {
				System.err.println(exceptionError);
				clearCarriage();
			}
		} while (continueLoop);        

		return courseCode;
	}	

	public static String validAssessment() {
		String assessment = null;
		boolean continueLoop = true;
		final String REGEX = "Assignment[1-3]|Weblearn|DeferredExam";

		do {
			try {               
				do {
					System.out.print("\nEnter assessment type " +
					"(Assignment1,2,3, Weblearn or DeferredExam): ");
					assessment = INPUT.nextLine();

				} while (!assessment.matches(REGEX));

				continueLoop = false;
			} 
			catch (Exception e) {
				System.err.println(exceptionError);
			}
		} while (continueLoop);

		return assessment;
	}

	public static String validReason() {
		String reason = null;
		boolean continueLoop = true;

		do {
			try {               
				do {
					System.out.print("\nEnter reason for extension " +
					"(maximum 50 characters): ");
					reason = INPUT.nextLine();
				} while (reason.length() > 50 || reason.length() == 0);

				continueLoop = false;
			} 
			catch (Exception e) {
				System.err.println(exceptionError);
			}
		} while (continueLoop);

		return reason;	
	}

	public static int validDays() {
		int days = -1;
		boolean continueLoop = true;

		do {
			try {               
				do {
					System.out.print("\nEnter days requested " +
							"(eg. 7 for a 7-day extension or 0 " +
					"if not provided): ");
					days = INPUT.nextInt();					
				} while (days < 0);

				clearCarriage();
				continueLoop = false;
			} 
			catch (Exception e) {
				System.err.println(exceptionError);				
				clearCarriage(); 
			}
		} while (continueLoop);

		return days;
	}

	public static String validOutcome() {
		String outcome = null;
		boolean continueLoop = true;
		final String REGEX = "Granted|Denied|Pending";

		do {
			try {               
				do {
					System.out.print("\nEnter outcome " +
					"(Granted/Denied/Pending): ");
					outcome = INPUT.nextLine();
				} while (!outcome.matches(REGEX));

				continueLoop = false;
			} 
			catch (Exception e) {
				System.err.println(exceptionError);
			}
		} while (continueLoop);

		return outcome;
	}

	public static GregorianCalendar validDueDate() {
		String validateDueDate;        
		GregorianCalendar dueDate = null;
		boolean continueLoop = true;
		final String REGEX = "[0-9]{2}/[0-9]{2}/[0-9]{4}";

		do {
			try {
				do {
					System.out.print("\nEnter due date (DD/MM/YYYY): ");
					validateDueDate = INPUT.nextLine();
				} while (!validateDueDate.matches(REGEX));

				// Convert String to GregorianCalendar:
				dueDate = convertStringDate(validateDueDate);
				continueLoop = false;
			} 
			catch (Exception e) {
				System.err.println(exceptionError);
			}
		} while (continueLoop);

		return dueDate;
	}

	public static char validMenuChoice() {
		String validateChar;
		boolean continueLoop = true;
		char menuChoice = ' ';

		do {
			try {
				do {
					System.out.print("\nSelect one of the options above: ");		
					validateChar = INPUT.nextLine();    				
				} while (validateChar.length() != 1);

				// Convert String to upper case character:
				menuChoice = Character.toUpperCase(validateChar.charAt(0));
				continueLoop = false;
			}
			catch (Exception e) {
				System.err.println(exceptionError);
			}
		} while (continueLoop);

		return menuChoice;
	}

	/*
	 * Convert String to GregorianCalendar:
	 */
	public static GregorianCalendar convertStringDate (String stringDate) {
		Date date;
		GregorianCalendar dueDate = null;

		try {
			date = DF.parse(stringDate);
			dueDate = new GregorianCalendar();
			dueDate.setTime(date);
		} catch (ParseException e) {
			System.err.println("Unable to parse " + stringDate + ".");
			System.err.println("Program terminated.");
			System.exit(1);
		}

		return dueDate;
	}

	/* 
	 * Clear a carriage return. Used after nextInt();
	 */
	public static void clearCarriage() {
		if (INPUT.hasNextLine()) {
			INPUT.nextLine();
		} 
	}
}

