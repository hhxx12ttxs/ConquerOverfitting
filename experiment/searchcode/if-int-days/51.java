package assign3;

import java.util.*;
import java.io.*;

/* Author:    David Currie (3336927)
 * This program is for Assignment 3 in Programming 1 at RMIT.
 * This class "drives" the application.
 */
public class RequestApp {

	// Declare arrays:
	private static ArrayList<User> users = new ArrayList<User>();
	private static ArrayList<Request> requests = new ArrayList<Request>();

	/*
	 * Calls the object readers and handles the main menu:
	 */
	public static void main(String args[]) {
		readFile("logins.txt");
		readFile("requests.txt");

		do {
			switch (listMainMenu()) {
			case '1': login();
			break;
			case '2': printRequestDetails(requests);
			break;
			case '3': printUserDetails(users);
			break;
			default:  System.out.println("\nPlease enter a valid choice.\n");			
			}
		} while (true);
	}

	/* 
	 * Output the main menu and get choice from user:
	 */
	public static char listMainMenu() {		
		System.out.println();
		System.out.println("1.     User login");
		System.out.println("2.     List requests");
		System.out.println("3.     List logins");

		// Authenticate the entered choice:
		return Authentication.validMenuChoice();
	}

	/*
	 * Authenticate the login:
	 */
	public static void login() {
		User currentUser = Authentication.validUser(users);

		// If the login is valid, run the appropriate action for the user type:
		if (currentUser != null) {
			if (currentUser.getRole().matches("admin")) {				
				adminMenu();
			} else {			
				printCourses(currentUser);
			}
		}
	}

	/* 
	 * Sub Menu. Repeat until user exits with 'X':
	 */
	public static void adminMenu() {
		do {
			switch (listAdminMenu()) {
			case '1': addRequest();
			break;
			case '2': addInstructor();
			break;
			case '3': addCourse();
			break;
			case '4': searchByInstructor(true);
			break;
			case 'X': System.out.println("Goodbye!");
			writeFile("logins.txt");
			writeFile("requests.txt");
			System.exit(0);
			default:  System.out.println("\nPlease enter a valid choice.\n");
			}
		} while (true);
	}

	/*
	 * Output the sub menu and get choice from user:
	 */
	public static char listAdminMenu() {
		System.out.println();
		System.out.println("1.     Add request");
		System.out.println("2.     Add a new instructor");
		System.out.println("3.     Add course to existing instructor");
		System.out.println("4.     List requests by instructor");
		System.out.println("X.     Exit the application");

		// Authenticate the entered choice:
		return Authentication.validMenuChoice();
	}	

	/*
	 * Read file and build the appropriate objects (requests or logins):
	 * Assumption is made that the file contains correct data.
	 */
	public static void readFile(String file) {
		String inputLine;
		int numTokens;
		FileReader fileReader;
		BufferedReader fileInput = null;
		File fileName = new File(file);

		// Attempt to read file and set up Objects to read:
		try {
			if (fileName.canRead()) {
				fileReader = new FileReader(fileName.getCanonicalPath());
				fileInput = new BufferedReader(fileReader);
			} else {
				throw new IOException("Unable to read " + file + ".");
			}
		} catch (FileNotFoundException e) {
			System.err.println(file + " not found.");
			System.exit(1);
		} catch (IOException e) {
			System.err.println("Unable to read " + file + ".");
			System.exit(1);
		}

		// Parse file to create User/Request object:
		try {
			while ((inputLine = fileInput.readLine()) != null) {
				if (file == "requests.txt") {
					StringTokenizer parseLine =
						new StringTokenizer(inputLine, ",");

					// Validate the file is valid:
					if ((numTokens = parseLine.countTokens()) >= 5) {
						String requestID = parseLine.nextToken();
						String studentID = parseLine.nextToken();
						String courseCode = parseLine.nextToken();
						String assessment = parseLine.nextToken();
						String reason = parseLine.nextToken();					

						// These are optional, hence second check:
						if (numTokens >= 8) {
							String convDays = parseLine.nextToken();
							int days = Integer.parseInt(convDays);

							String outcome = parseLine.nextToken();							

							String convDate = parseLine.nextToken();
							GregorianCalendar dueDate = 
								Authentication.convertStringDate(convDate);

							// Add a full request:
							requests.add(new Request(requestID, studentID,
									courseCode, assessment, reason, days,
									outcome, dueDate));							
						} else {
							// Add a partial request:
							requests.add(new Request(requestID, studentID,
									courseCode, assessment, reason));
						}
					} else {
						throw new IOException(file + " is not valid.");
					}					
				} else if (file == "logins.txt") {
					StringTokenizer parseLine =
						new StringTokenizer(inputLine, "|");

					// Validate the file is valid:
					if ((numTokens = parseLine.countTokens()) >= 3) {
						String login = parseLine.nextToken();
						String password = parseLine.nextToken();
						String role = parseLine.nextToken();

						// Only instructors can have a 4th field (courses):
						if (numTokens == 4 && role.matches("instructor")) {
							String convCourses = parseLine.nextToken();
							ArrayList<String> courses = new ArrayList<String>();

							// Courses uses a different delimiter:
							StringTokenizer parseCourses =
								new StringTokenizer(convCourses, ":");

							// Populate the array:
							for (int i=0; i<=parseCourses.countTokens(); i++) {
								courses.add(parseCourses.nextToken());
							}

							// Add a new instructor:
							users.add(new Instructor(login, password, role, 
									courses));
						} else {							
							// Add a new user:
							if (role.matches("admin")) {
								users.add(new Administrator(login, password, 
										role));
							} else {
								users.add(new Instructor(login, password, 
										role));
							}
						}

					} else {
						throw new IOException(file + " is not valid.");
					}
				}
				else {
					throw new IOException("Invalid filename (" + file + ").");
				}
			}
		} catch (IOException e) {
			System.err.println("Unable to read " + file + ".");
			System.err.println("Program terminated.");
			System.exit(1);
		}
	}

	/*
	 * Convert Objects to Strings and write to the file parameter:
	 */
	public static void writeFile(String file) {		
		ArrayList<String> writeList = new ArrayList<String>();
		PrintWriter writeFile = null;
		File fileName = new File(file);		


		// Attempt to read file and set up Objects to write:	
		try {
			if (fileName.canWrite()) {
				writeFile = new PrintWriter(new BufferedWriter
						(new FileWriter(fileName.getCanonicalPath())));
			}
		} catch (IOException e) {
			System.err.println("Unable to write to " + file + ".");
			System.err.println("Program terminated.");
			System.exit(1);
		}		

		// Build the list of Requests or Users to write to file.
		// Done by adding all elements with a delimiter to a String:
		try {
			if (file.matches("requests.txt")) {
				for (int i=0; i<requests.size(); i++) {
					writeList.add(requests.get(i).getRequestID() + "," +
							requests.get(i).getStudentID() + "," +
							requests.get(i).getCourseCode() + "," +
							requests.get(i).getAssessment() + "," +
							requests.get(i).getReason() + "," +
							requests.get(i).getDays() + "," +
							requests.get(i).getOutcome() + "," +
							requests.get(i).getDueDateString());
				}
			} else if (file.matches("logins.txt")) {
				String tempLine;

				for (int i=0; i<users.size(); i++) {				
					tempLine = users.get(i).getLogin() + "|" +
					users.get(i).getPassword() + "|" +
					users.get(i).getRole();

					// Instructor must add courses:
					if (users.get(i).getRole().matches("instructor")) {
						String courses = "";
						ArrayList<String> courseList = 
							((Instructor)users.get(i)).getCourses();

						// Convert the courses ArrayList to a string if loaded:
						if (courseList != null && courseList.size() != 0) {
							for (int j=0; j<courseList.size(); j++) {
								courses += courseList.get(j);

								// Do not add the delimiter at the end:
								if (j < courseList.size()-1) {
									courses += ":";
								}
							}
						}
						if (courses != "") {
							tempLine += "|" + courses;
						}
					}
					writeList.add(tempLine);
				}			
			} else {
				throw new IOException(file + " is not valid.");
			}
		} catch (Exception e) {
			System.err.println("Unable to write to " + file + ".");
			System.err.println("Program terminated.");
			System.exit(1);
		}

		// Write the list to file:
		for (int i=0; i<writeList.size(); i++) {
			writeFile.println(writeList.get(i));
		}
		writeFile.close();
	}

	/*
	 * Add a new Request object:
	 */
	public static void addRequest() {
		String outcome, requestID;
		GregorianCalendar dueDate;
		ArrayList<Request> resultsRequestID;
		ArrayList<String> courseList = buildCourseList();

		// Can only load a max of 10:
		if (requests.size() == 10) {
			System.out.println("\nThe maximum number of requests (10) have " +
			"been loaded.");
		}
		else {
			System.out.println("\nAdd a new request:");

			// Ensure requestID has not already been entered:
			do {
				requestID = Authentication.validRequestID();
				resultsRequestID = searchRequestID(requestID);

				if (resultsRequestID.size() > 0) {
					System.out.println("\nThat request ID already exists.");
				}
			} while (resultsRequestID.size() > 0);

			// Get rest of details:
			String studentID = Authentication.validStudentID();			
			String courseCode = Authentication.validCourseList(courseList);
			String assessment = Authentication.validAssessment();			
			String reason = Authentication.validReason();

			// Allow for authentication to be ignored as per specification:
			int days = Authentication.validDays();

			// Only get Outcome and Due Date if Days or Reason are valid:
			if (reason == null || days == 0) {
				outcome = "Pending";
				dueDate = new GregorianCalendar();               
			}
			else {
				outcome = Authentication.validOutcome();
				dueDate = Authentication.validDueDate();
			}

			// Add the request:
			requests.add(new Request(requestID,
					studentID, courseCode, assessment, reason,
					days, outcome, dueDate));

			System.out.println("\nRequest has been added.");
		}

		writeFile("requests.txt");
	}

	/*
	 * Add a new Instructor object:
	 */
	public static void addInstructor() {
		String login, password;
		ArrayList<String> courseList = new ArrayList<String>();

		System.out.print("\nAdd a new Instructor:");

		// Check login does not already exist:
		do {
			login = Authentication.validLogin();

			if (searchUser(login) != null) {
				System.out.println("\nThat user login already exists.");
			}
		} while (searchUser(login) != null);

		// Get rest of details:
		password = Authentication.validPassword();
		courseList = Authentication.validCourseCode(true);

		// Add the Instructor:
		users.add(new Instructor(login, password, "instructor", courseList));

		System.out.println("\nInstructor has been added.");

		writeFile("logins.txt");
	}

	/*
	 * Modify an existing Instructor:
	 */
	public static void addCourse() {			
		ArrayList<String> courseList = new ArrayList<String>();

		System.out.println("\nAdd a new course to an instructor:");

		// Get user to modify:
		User instructor = searchByInstructor(false);

		// Check Instructor is not already teaching course:
		do {
			courseList = Authentication.validCourseCode(false);

			if (searchCourses(instructor,courseList).size() != 0) {
				System.out.println("\n" + instructor.getLogin() + 
				" is already teaching that course.");
			}
		} while (searchCourses(instructor,courseList).size() != 0);

		// Add the course to instructor:
		((Instructor)instructor).addCourses(courseList);

		writeFile("logins.txt");
	}

	/*
	 * Search and output requests by Instructor.
	 * Parameter to handle whether or not to print:
	 */
	public static User searchByInstructor(boolean print) {
		printUsers(users);
		User instructor = null;

		// List Instructors and get an appropriate login:
		do {
			instructor = searchInstructor(Authentication.validLogin());

			if (instructor == null) {
				System.out.println("Invalid login.");
			}
		} while (instructor == null); 

		// Print the list of courses?
		if (print) {
			printCourses(instructor);
		}
		return instructor;
	}

	/*
	 * Build a list of all Instructor courses:
	 */
	public static ArrayList<String> buildCourseList() {
		ArrayList<String> arrayCourses = new ArrayList<String>();
		ArrayList<String> tempCourses = new ArrayList<String>();

		// Build list of courses:
		for (int i=0; i<users.size(); i++) {
			if (users.get(i).getRole().matches("instructor")) {
				tempCourses = ((Instructor)users.get(i)).getCourses();

				if (tempCourses != null) {
					arrayCourses.addAll(tempCourses);
				}
			}
		}

		return arrayCourses;
	}

	/*
	 * Search Requests by requestID:
	 */
	public static ArrayList<Request> searchRequestID(String requestID) {
		ArrayList<Request> resultsRequestID = 
			new ArrayList<Request>(requests.size());

		// Build an ArrayList of search results:
		for (int i=0; i<requests.size(); i++) {
			if (requests.get(i).getRequestID().matches(requestID)) {
				resultsRequestID.add(requests.get(i));
			}
		}

		return resultsRequestID;
	}

	/*
	 * Search Requests by courseCode:
	 */
	public static ArrayList<Request> searchCourseCode(String courseCode) {
		ArrayList<Request> resultsCourseCode =
			new ArrayList<Request>(requests.size());

		// Build an ArrayList of search results:
		for (int i=0; i<requests.size(); i++) {
			if (requests.get(i).getCourseCode().matches(courseCode)) {
				resultsCourseCode.add(requests.get(i));
			}
		}

		return resultsCourseCode;	
	}

	/* 
	 * Search Instructors only by login:
	 */
	public static User searchInstructor(String login) {
		for (int i=0; i<users.size(); i++) {
			if (users.get(i).getLogin().matches(login) && 
					users.get(i).getRole().matches("instructor")) {
				return users.get(i);
			}
		}

		return null;
	}

	/*
	 * Search Instructor's course codes:
	 */
	public static ArrayList<String> searchCourses(User instructor, 
			ArrayList<String> courseCode) {		
		ArrayList<String> courseList = ((Instructor)instructor).getCourses();
		ArrayList<String> resultsCourseList = new ArrayList<String>();

		// courseList may be a null if no courses are loaded:
		if (courseList == null) {
			return resultsCourseList;
		}

		// Build an ArrayList of search results:
		for (int i=0; i<courseList.size(); i++) {
			for (int j=0; j<courseCode.size(); j++) {
				if (courseList.get(i).matches(courseCode.get(j))) {
					resultsCourseList.add(courseCode.get(j));
				}	
			}
		}

		return resultsCourseList;	
	}

	/* 
	 * Search all Users by login:
	 */
	public static User searchUser(String login) {
		for (int i=0; i<users.size(); i++) {
			if (users.get(i).getLogin().matches(login)) {
				return users.get(i);
			}
		}

		return null;
	}	

	/*
	 * Print the requests for courses that the Instructor has access to: 
	 */
	public static void printCourses(User instructor) {
		ArrayList<String> coursesList = ((Instructor)instructor).getCourses();
		ArrayList<Request> resultsCourseCode;

		// Provide error if no courses available, otherwise output them:
		if (coursesList == null) {
			System.out.println();
			System.out.println(instructor.getLogin() + " is not teaching any" +
			" courses.");
		} else {
			for (int i=0; i<coursesList.size(); i++) {
				System.out.println("\nCourse " + coursesList.get(i) + ":");

				// Fetch course requests:
				resultsCourseCode = searchCourseCode(coursesList.get(i));

				// If requests are available output them, otherwise error:
				if (resultsCourseCode.size() == 0) {
					System.out.println("\nNo current requests.");
				} else {
					printRequestDetails(resultsCourseCode);
				}
			}			
		}

		Authentication.pause();
	}	

	/*
	 * Print requests:
	 */
	public static void printRequestDetails(ArrayList<Request> array) {
		System.out.printf("\n%10s  %10s  %11s  %13s  %4s  %7s  %10s  %s\n", 
				"Request ID", "Student ID", "Course Code", "Assessment", 
				"Days", "Outcome", "Due Date", "Reason");
		System.out.printf("%10s  %10s  %11s  %11s  %4s  %7s  %10s  %s\n", 
				"----------", "----------", "-----------", "-------------", 
				"----", "-------", "----------", "------");

		for (int i=0; i<array.size(); i++) {
			array.get(i).printDetails();
		}		
	}

	/*
	 * Print full User details:
	 */
	public static void printUserDetails(ArrayList<User> array) {
		System.out.printf("\n%8s  %8s  %10s  %8s\n", "Login", "Password",
				"Role", "Courses");
		System.out.printf("%8s  %8s  %10s  %8s\n",
				"--------", "--------", "----------", "--------");

		for (int i=0; i<array.size(); i++) {
			array.get(i).printDetails();
		}
	}

	/*
	 * Print Instructor logins only:
	 */
	public static void printUsers(ArrayList<User> array) {
		System.out.printf("\n%8s\n", "Login");
		System.out.printf("%8s\n", "--------");

		for (int i=0; i<array.size(); i++) {
			if (array.get(i).getRole().matches("instructor")) {
				System.out.printf("%8s\n", array.get(i).getLogin());	
			}			
		}
	}
}

