package teammates;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import teammates.exception.AccountExistsException;
import teammates.exception.CourseDoesNotExistException;
import teammates.exception.CourseExistsException;
import teammates.exception.EvaluationExistsException;
import teammates.exception.TeammatesException;
import teammates.object.Course;
import teammates.object.CourseDetailsForStudent;
import teammates.object.CourseSummaryForCoordinator;
import teammates.object.CourseSummaryForStudent;
import teammates.object.EnrollmentReport;
import teammates.object.Evaluation;
import teammates.object.EvaluationDetailsForCoordinator;
import teammates.object.Student;
import teammates.object.Submission;
import teammates.object.SubmissionDetailsForCoordinator;
import teammates.object.SubmissionDetailsForStudent;

import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

/**
 * Servlet to handle every request
 * 
 * TODO: split into StudentServlet, CoordinatorServlet, AdminServlet it should
 * have a base servlet class.
 * 
 * @author nvquanghuy
 * 
 */
@SuppressWarnings("serial")
public class MainServlet extends ServletBase {

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException,
			ServletException {

		this.resp = resp;
		this.req = req;

		// Decide what the action will be, so that we call appropriate method
		String page = getServletConfig().getInitParameter("page");
		if (page == null)
			page = "home";
		page = page.toLowerCase();
		// Call the appropriate method for each page.
		// TODO huy - Maybe we can find some module to auto-dispatch this.
		if (page.equals("home")) {
			pageHome();
		}

		// Render the View
		renderTemplate();

		// Flush the buffer just in case.
		this.resp.flushBuffer();
	}

	protected void pageHome() {
		templatePath = "home";
	}

	// --------------------------------------------------------------------------

	// OPERATIONS
	private static final String	OPERATION_ADMINISTRATOR_ADDCOORDINATOR									= "administrator_addcoordinator";
	private static final String	OPERATION_ADMINISTRATOR_CLEANUP													= "administrator_cleanup";
	private static final String	OPERATION_ADMINISTRATOR_LOGINATD												= "administrator_loginatd";
	private static final String	OPERATION_ADMINISTRATOR_LOGINMANAGESYSTEM								= "administrator_loginmanagesystem";
	private static final String	OPERATION_ADMINISTRATOR_LOGOUT													= "administrator_logout";
	private static final String	OPERATION_ADMINISTRATOR_SENDREGISTRATIONKEYS						= "administrator_sendregistrationkeys";

	private static final String	OPERATION_COORDINATOR_ADDCOURSE													= "coordinator_addcourse";
	private static final String	OPERATION_COORDINATOR_ADDEVALUATION											= "coordinator_addevaluation";
	private static final String	OPERATION_COORDINATOR_ARCHIVECOURSE											= "coordinator_archivecourse";
	private static final String	OPERATION_COORDINATOR_DELETEALLSTUDENTS									= "coordinator_deleteallstudents";
	private static final String	OPERATION_COORDINATOR_DELETECOURSE											= "coordinator_deletecourse";
	private static final String	OPERATION_COORDINATOR_DELETEEVALUATION									= "coordinator_deleteevaluation";
	private static final String	OPERATION_COORDINATOR_DELETESTUDENT											= "coordinator_deletestudent";
	private static final String	OPERATION_COORDINATOR_EDITEVALUATION										= "coordinator_editevaluation";
	private static final String	OPERATION_COORDINATOR_EDITEVALUATIONRESULTS							= "coordinator_editevaluationresults";
	private static final String	OPERATION_COORDINATOR_EDITSTUDENT												= "coordinator_editstudent";
	private static final String	OPERATION_COORDINATOR_ENROLSTUDENTS											= "coordinator_enrolstudents";
	private static final String	OPERATION_COORDINATOR_GETCOURSE													= "coordinator_getcourse";
	private static final String	OPERATION_COORDINATOR_GETCOURSELIST											= "coordinator_getcourselist";
	private static final String	OPERATION_COORDINATOR_GETEVALUATIONLIST									= "coordinator_getevaluationlist";
	private static final String	OPERATION_COORDINATOR_GETSTUDENTLIST										= "coordinator_getstudentlist";
	private static final String	OPERATION_COORDINATOR_GETSUBMISSIONLIST									= "coordinator_getsubmissionlist";
	private static final String	OPERATION_COORDINATOR_INFORMSTUDENTSOFEVALUATIONCHANGES	= "coordinator_informstudentsofevaluationchanges";
	private static final String	OPERATION_COORDINATOR_LOGIN															= "coordinator_login";
	private static final String	OPERATION_COORDINATOR_LOGOUT														= "coordinator_logout";
	private static final String	OPERATION_COORDINATOR_PUBLISHEVALUATION									= "coordinator_publishevaluation";
	private static final String	OPERATION_COORDINATOR_REMINDSTUDENTS										= "coordinator_remindstudents";
	private static final String	OPERATION_COORDINATOR_SENDREGISTRATIONKEY								= "coordinator_sendregistrationkey";
	private static final String	OPERATION_COORDINATOR_SENDREGISTRATIONKEYS							= "coordinator_sendregistrationkeys";
	private static final String	OPERATION_COORDINATOR_UNARCHIVECOURSE										= "coordinator_unarchivecourse";

	private static final String	OPERATION_STUDENT_ARCHIVECOURSE													= "student_archivecourse";
	private static final String	OPERATION_STUDENT_DELETECOURSE													= "student_deletecourse";
	private static final String	OPERATION_STUDENT_GETCOURSE															= "student_getcourse";
	private static final String	OPERATION_STUDENT_GETCOURSELIST													= "student_getcourselist";
	private static final String	OPERATION_STUDENT_GETPENDINGEVALUATIONLIST							= "student_getpendingevaluationlist";
	private static final String	OPERATION_STUDENT_GETPASTEVALUATIONLIST									= "student_getpastevaluationlist";
	private static final String	OPERATION_STUDENT_GETSUBMISSIONLIST											= "student_getsubmissionlist";
	private static final String	OPERATION_STUDENT_GETSUBMISSIONRESULTSLIST							= "student_getsubmissionresultslist";
	private static final String	OPERATION_STUDENT_JOINCOURSE														= "student_joincourse";
	private static final String	OPERATION_STUDENT_LOGIN																	= "student_login";
	private static final String	OPERATION_STUDENT_LOGOUT																= "student_logout";
	private static final String	OPERATION_STUDENT_SUBMITEVALUATION											= "student_submitevaluation";
	private static final String	OPERATION_STUDENT_UNARCHIVECOURSE												= "student_unarchivecourse";

	// PARAMETERS
	
	private static final String	COURSE_COORDINATORNAME																	= "coordinatorname";
	private static final String	COURSE_ID																								= "courseid";
	private static final String	COURSE_NAME																							= "coursename";
	private static final String	COURSE_NUMBEROFTEAMS																		= "coursenumberofteams";
	private static final String	COURSE_STATUS																						= "coursestatus";

	private static final String	EVALUATION_ACTIVATED																		= "activated";
	private static final String	EVALUATION_COMMENTSENABLED															= "commentsstatus";
	private static final String	EVALUATION_DEADLINE																			= "deadline";
	private static final String	EVALUATION_DEADLINETIME																	= "deadlinetime";
	private static final String	EVALUATION_GRACEPERIOD																	= "graceperiod";
	private static final String	EVALUATION_INSTRUCTIONS																	= "instr";
	private static final String	EVALUATION_NAME																					= "evaluationname";
	private static final String	EVALUATION_NUMBEROFCOMPLETEDEVALUATIONS									= "numberofevaluations";
	private static final String	EVALUATION_NUMBEROFEVALUATIONS													= "numberofcompletedevaluations";
	private static final String	EVALUATION_PUBLISHED																		= "published";
	private static final String	EVALUATION_START																				= "start";
	private static final String	EVALUATION_STARTTIME																		= "starttime";

	private static final String	STUDENT_COMMENTS																				= "comments";
	private static final String	STUDENT_COMMENTSEDITED																	= "commentsedited";
	private static final String	STUDENT_COMMENTSTOSTUDENT																= "commentstostudent";
	private static final String	STUDENT_COURSEID																				= "courseid";
	private static final String	STUDENT_EDITCOMMENTS																		= "editcomments";
	private static final String	STUDENT_EDITEMAIL																				= "editemail";
	private static final String	STUDENT_EDITGOOGLEID																		= "editgoogleid";
	private static final String	STUDENT_EDITNAME																				= "editname";
	private static final String	STUDENT_EDITTEAMNAME																		= "editteamname";
	private static final String	STUDENT_EMAIL																						= "email";
	private static final String	STUDENT_FROMSTUDENT																			= "fromemail";
	private static final String	STUDENT_FROMSTUDENTCOMMENTS															= "fromstudentcomments";
	private static final String	STUDENT_FROMSTUDENTNAME																	= "fromname";
	private static final String	STUDENT_ID																							= "id";
	private static final String	STUDENT_INFORMATION																			= "information";
	private static final String	STUDENT_JUSTIFICATION																		= "justification";
	private static final String	STUDENT_NAME																						= "name";
	private static final String	STUDENT_NAMEEDITED																			= "nameedited";
	private static final String	STUDENT_NUMBEROFSUBMISSIONS															= "numberofsubmissions";
	private static final String	STUDENT_POINTS																					= "points";
	private static final String	STUDENT_POINTSBUMPRATIO																	= "pointsbumpratio";
	private static final String	STUDENT_REGKEY																					= "regkey";
	private static final String	STUDENT_STATUS																					= "status";
	private static final String	STUDENT_TEAMMATE																				= "teammate";
	private static final String	STUDENT_TEAMMATES																				= "teammates";
	private static final String	STUDENT_TEAMNAME																				= "teamname";
	private static final String	STUDENT_TEAMNAMEEDITED																	= "teamnameedited";
	private static final String	STUDENT_TOSTUDENT																				= "toemail";
	private static final String	STUDENT_TOSTUDENTCOMMENTS																= "tostudentcomments";
	private static final String	STUDENT_TOSTUDENTNAME																		= "toname";

	// MESSAGES
	private static final String	MSG_COURSE_ADDED																				= "course added";
	private static final String	MSG_COURSE_EXISTS																				= "course exists";
	private static final String	MSG_COURSE_NOTEAMS																			= "course has no teams";
	private static final String	MSG_EVALUATION_ADDED																		= "evaluation added";
	private static final String	MSG_EVALUATION_DEADLINEPASSED														= "evaluation deadline passed";
	private static final String	MSG_EVALUATION_EXISTS																		= "evaluation exists";
	private static final String	MSG_EVALUATION_UNABLETOCHANGETEAMS											= "evaluation ongoing unable to change teams";
	private static final String	MSG_STATUS_OPENING																			= "<status>";
	private static final String	MSG_STATUS_CLOSING																			= "</status>";

	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException,
			ServletException {
		// Initialization
		this.req = req;
		this.resp = resp;

		this.resp.setContentType("text/xml");
		this.resp.setHeader("Cache-Control", "no-cache");

		// Processing
		String operation = this.req.getParameter("operation");

		if (operation.equals(OPERATION_ADMINISTRATOR_ADDCOORDINATOR)) {
			//administratorAddCoordinator();
		}

		else if (operation.equals(OPERATION_ADMINISTRATOR_CLEANUP)) {
			administratorCleanUp();
		}

		else if (operation.equals(OPERATION_ADMINISTRATOR_LOGINATD)) {
			administratorLoginATD();
		}

		else if (operation.equals(OPERATION_ADMINISTRATOR_LOGINMANAGESYSTEM)) {
			administratorLoginManageSystem();
		}

		else if (operation.equals(OPERATION_ADMINISTRATOR_LOGOUT)) {
			administratorLogout();
		}

		else if (operation.equals(OPERATION_ADMINISTRATOR_SENDREGISTRATIONKEYS)) {
			administratorSendRegistrationKeys();
		}

		else if (operation.equals(OPERATION_COORDINATOR_ADDCOURSE)) {
			// coordinatorAddCourse();
		}

		else if (operation.equals(OPERATION_COORDINATOR_ADDEVALUATION)) {
			// coordinatorAddEvaluation();
		}

		else if (operation.equals(OPERATION_COORDINATOR_ARCHIVECOURSE)) {
			coordinatorArchiveCourse();
		}

		else if (operation.equals(OPERATION_COORDINATOR_DELETEALLSTUDENTS)) {
			coordinatorDeleteAllStudents();
		}

		else if (operation.equals(OPERATION_COORDINATOR_DELETECOURSE)) {
			coordinatorDeleteCourse();
		}

		else if (operation.equals(OPERATION_COORDINATOR_DELETEEVALUATION)) {
			coordinatorDeleteEvaluation();
		}

		else if (operation.equals(OPERATION_COORDINATOR_DELETESTUDENT)) {
			coordinatorDeleteStudent();
		}

		else if (operation.equals(OPERATION_COORDINATOR_EDITEVALUATION)) {
			coordinatorEditEvaluation();
		}

		else if (operation.equals(OPERATION_COORDINATOR_EDITEVALUATIONRESULTS)) {
			coordinatorEditEvaluationResults();
		}

		else if (operation.equals(OPERATION_COORDINATOR_EDITSTUDENT)) {
			coordinatorEditStudent();
		}

		else if (operation.equals(OPERATION_COORDINATOR_ENROLSTUDENTS)) {
			coordinatorEnrolStudents();
		}

		else if (operation.equals(OPERATION_COORDINATOR_GETCOURSE)) {
			coordinatorGetCourse();
		}

		else if (operation.equals(OPERATION_COORDINATOR_GETCOURSELIST)) {
			coordinatorGetCourseList();
		}

		else if (operation.equals(OPERATION_COORDINATOR_GETEVALUATIONLIST)) {
			coordinatorGetEvaluationList();
		}

		else if (operation.equals(OPERATION_COORDINATOR_GETSTUDENTLIST)) {
			coordinatorGetStudentList();
		}

		else if (operation.equals(OPERATION_COORDINATOR_GETSUBMISSIONLIST)) {
			coordinatorGetSubmissionList();
		}

		else if (operation.equals(OPERATION_COORDINATOR_INFORMSTUDENTSOFEVALUATIONCHANGES)) {
			coordinatorInformStudentsOfEvaluationChanges();
		}

		else if (operation.equals(OPERATION_COORDINATOR_LOGIN)) {
			coordinatorLogin();
		}

		else if (operation.equals(OPERATION_COORDINATOR_LOGOUT)) {
			coordinatorLogout();
		}

		else if (operation.equals(OPERATION_COORDINATOR_PUBLISHEVALUATION)) {
			coordinatorPublishEvaluation();
		}

		else if (operation.equals(OPERATION_COORDINATOR_REMINDSTUDENTS)) {
			coordinatorRemindStudents();
		}

		else if (operation.equals(OPERATION_COORDINATOR_SENDREGISTRATIONKEY)) {
			coordinatorSendRegistrationKey();
		}

		else if (operation.equals(OPERATION_COORDINATOR_SENDREGISTRATIONKEYS)) {
			coordinatorSendRegistrationKeys();
		}

		else if (operation.equals(OPERATION_COORDINATOR_UNARCHIVECOURSE)) {
			coordinatorUnarchiveCourse();
		}

		else if (operation.equals(OPERATION_STUDENT_ARCHIVECOURSE)) {
			studentArchiveCourse();
		}

		else if (operation.equals(OPERATION_STUDENT_DELETECOURSE)) {
			studentDeleteCourse();
		}

		else if (operation.equals(OPERATION_STUDENT_GETCOURSE)) {
			studentGetCourse();
		}

		else if (operation.equals(OPERATION_STUDENT_GETCOURSELIST)) {
			studentGetCourseList();
		}

		else if (operation.equals(OPERATION_STUDENT_GETPASTEVALUATIONLIST)) {
			studentGetPastEvaluationList();
		}

		else if (operation.equals(OPERATION_STUDENT_GETPENDINGEVALUATIONLIST)) {
			studentGetPendingEvaluationList();
		}

		else if (operation.equals(OPERATION_STUDENT_GETSUBMISSIONLIST)) {
			studentGetSubmissionList();
		}

		else if (operation.equals(OPERATION_STUDENT_GETSUBMISSIONRESULTSLIST)) {
			studentGetSubmissionResultsList();
		}

		else if (operation.equals(OPERATION_STUDENT_JOINCOURSE)) {
			// studentJoinCourse();
		}

		else if (operation.equals(OPERATION_STUDENT_LOGIN)) {
			studentLogin();
		}

		else if (operation.equals(OPERATION_STUDENT_LOGOUT)) {
			studentLogout();
		}

		else if (operation.equals(OPERATION_STUDENT_SUBMITEVALUATION)) {
			studentSubmitEvaluation();
		}

		else if (operation.equals(OPERATION_STUDENT_UNARCHIVECOURSE)) {
			studentUnarchiveCourse();
		}

		// Clean-up
		this.resp.flushBuffer();
	}

	private void administratorCleanUp() {

		String courseID = req.getParameter(COURSE_ID);

		try {
			courseFactory.deleteCoordinatorCourse(courseID);
		}

		catch (CourseDoesNotExistException e) {

		}

	}

	private void administratorLoginATD() throws IOException {

		resp.getWriter()
				.write("<url><![CDATA[" + accountFactory.getLoginPage("/atd.jsp") + "]]></url>");
	}

	private void administratorLoginManageSystem() throws IOException {

		resp.getWriter().write(
				"<url><![CDATA[" + accountFactory.getLoginPage("/administrator.jsp") + "]]></url>");
	}

	private void administratorLogout() throws IOException {

		resp.getWriter().write(
				"<url><![CDATA[" + accountFactory.getLogoutPage("/admin.html") + "]]></url>");
	}

	private void administratorSendRegistrationKeys() {
		String email = req.getParameter(STUDENT_EMAIL);

		// Create dud Student objects with e-mail provided by tester
		List<Student> studentList = new ArrayList<Student>();

		for (int x = 0; x < 40; x++) {
			Student s = new Student(email, "Admin", new Text("Comments"), "Test Course", "Test TeamName");
			s.setRegistrationKey((long) 1111);
			studentList.add(s);
		}

		// Send the keys to the dud Student objects with e-mail provided by tester

		courseFactory.sendRegistrationKeys(studentList, "Test CourseID", "Test Course", "ADMIN");
	}

	/**
	 * Coordinator adds new courses URL: AJAX POST /coordinator/course/add
	 * 
	 * TODO: return XML about the course adding status
	 * 
	 * to /coordinator/courses page afterwards
	 * 
	 * @throws IOException
	 * @throws ServletException
	 */
	protected void postAddCourse() {
		String googleID = accountFactory.getUser().getNickname().toLowerCase();
		try {
			courseFactory.addCourse(req.getParameter("courseID"), req.getParameter("courseName"),
					googleID);

			respWrite(MSG_STATUS_OPENING + MSG_COURSE_ADDED + MSG_STATUS_CLOSING);
		} catch (CourseExistsException e) {
			respWrite(MSG_STATUS_OPENING + MSG_COURSE_EXISTS + MSG_STATUS_CLOSING);
		}
	}

	/**
	 * Action to add new evaluation URL: POST /coordinator/evaluation/add
	 * 
	 * Redirect back to evaluations page
	 */
	protected void postAddEvaluation() {
		String courseID = req.getParameter("courseid");

		// Check if the number of teams is zero
		int numberOfTeams = courseFactory.getNumberOfTeams(courseID);
		if (numberOfTeams == 0) {
			respWrite(MSG_STATUS_OPENING + MSG_COURSE_NOTEAMS + MSG_STATUS_CLOSING);
			return;
		}

		String name = req.getParameter(EVALUATION_NAME);
		String instructions = req.getParameter(EVALUATION_INSTRUCTIONS);

		boolean commentsEnabled = Boolean.parseBoolean(req.getParameter(EVALUATION_COMMENTSENABLED));

		String startDate = req.getParameter(EVALUATION_START);
		int startTime = Integer.parseInt(req.getParameter(EVALUATION_STARTTIME));

		String deadlineDate = req.getParameter(EVALUATION_DEADLINE);
		int deadlineTime = Integer.parseInt(req.getParameter(EVALUATION_DEADLINETIME));

		int gracePeriod = Integer.parseInt(req.getParameter(EVALUATION_GRACEPERIOD));

		Date start = convertToDate(startDate, startTime);
		Date deadline = convertToDate(deadlineDate, deadlineTime);

		// Add the evaluation
		EvaluationFactory evaluations = EvaluationFactory.getInstance();

		try {
			evaluations.addEvaluation(courseID, name, new Text(instructions), commentsEnabled, start,
					deadline, gracePeriod);
			respWrite(MSG_STATUS_OPENING + MSG_EVALUATION_ADDED + MSG_STATUS_CLOSING);
		}

		catch (EvaluationExistsException e) {
			respWrite(MSG_STATUS_OPENING + MSG_EVALUATION_EXISTS + MSG_STATUS_CLOSING);
		}
	}

	private void coordinatorArchiveCourse() throws IOException, ServletException {
		courseFactory.archiveCoordinatorCourse(req.getParameter(COURSE_ID));
	}

	private void coordinatorDeleteAllStudents() {

		courseFactory.deleteAllStudents(req.getParameter(COURSE_ID));

	}

	protected void coordinatorDeleteCourse() {
		String courseID = req.getParameter(COURSE_ID);

		try {
			courseFactory.deleteCoordinatorCourse(courseID);
		} catch (CourseDoesNotExistException e) {
			respWrite(e.getMessage());
		}

		EvaluationFactory evaluations = EvaluationFactory.getInstance();
		evaluations.deleteEvaluations(courseID);
	}

	private void coordinatorDeleteEvaluation() {
		String courseID = req.getParameter(COURSE_ID);
		String name = req.getParameter(EVALUATION_NAME);

		EvaluationFactory evaluations = EvaluationFactory.getInstance();
		evaluations.deleteEvaluation(courseID, name);
	}

	private void coordinatorDeleteStudent() {

		courseFactory.deleteStudent(req.getParameter(COURSE_ID), req.getParameter(STUDENT_EMAIL));

	}

	private void coordinatorEditEvaluationResults() {
		List<Submission> submissionList = new ArrayList<Submission>();

		int numberOfSubmissions = Integer.parseInt(req.getParameter(STUDENT_NUMBEROFSUBMISSIONS));

		String fromStudent = "";
		String toStudent = "";
		int points = 0;
		String justification = "";
		String commentsToStudent = "";

		String courseID = req.getParameter(COURSE_ID);
		String evaluationName = req.getParameter(EVALUATION_NAME);
		String teamName = req.getParameter(STUDENT_TEAMNAME);

		for (int x = 0; x < numberOfSubmissions; x++) {
			fromStudent = req.getParameter(STUDENT_FROMSTUDENT + x);
			toStudent = req.getParameter(STUDENT_TOSTUDENT + x);
			points = Integer.parseInt(req.getParameter(STUDENT_POINTS + x));
			justification = req.getParameter(STUDENT_JUSTIFICATION + x);
			commentsToStudent = req.getParameter(STUDENT_COMMENTSTOSTUDENT + x);

			submissionList.add(new Submission(fromStudent, toStudent, courseID, evaluationName, teamName,
					points, new Text(justification), new Text(commentsToStudent)));
		}

		EvaluationFactory evaluations = EvaluationFactory.getInstance();

		evaluations.editSubmissions(submissionList);
	}

	private void coordinatorEditStudent() throws IOException {
		String courseID = req.getParameter(COURSE_ID);
		String email = req.getParameter(STUDENT_EMAIL);
		String newName = req.getParameter(STUDENT_EDITNAME);
		String newTeamName = req.getParameter(STUDENT_EDITTEAMNAME);
		String newEmail = req.getParameter(STUDENT_EDITEMAIL);
		String newGoogleID = req.getParameter(STUDENT_EDITGOOGLEID);
		String newComments = req.getParameter(STUDENT_EDITCOMMENTS);

		if (newGoogleID == null) {
			newGoogleID = "";
		}

		if (newComments == null) {
			newComments = "";
		}

		EvaluationFactory evaluations = EvaluationFactory.getInstance();

		if (!evaluations.isEvaluationOngoing(courseID)) {
			courseFactory.editStudent(courseID, email, newName, newTeamName, newEmail, newGoogleID,
					newComments);

		}

		else {
			if (!courseFactory.getStudentWithEmail(courseID, email).getTeamName().equals(newTeamName)) {
				resp.getWriter().write(
						MSG_STATUS_OPENING + MSG_EVALUATION_UNABLETOCHANGETEAMS + MSG_STATUS_CLOSING);
			}

			courseFactory.editStudent(courseID, email, newName, newEmail, newGoogleID, newComments);
		}

		evaluations.editSubmissions(courseID, email, newEmail);

		resp.getWriter().write(MSG_STATUS_OPENING + "nil" + MSG_STATUS_CLOSING);
	}

	private void coordinatorEnrolStudents() throws IOException {
		String information = req.getParameter(STUDENT_INFORMATION);
		String courseID = req.getParameter(COURSE_ID);

		// Break down the input into Student objects
		List<Student> studentList = new ArrayList<Student>();

		String entries[] = information.split("\n");
		String fields[];
		String name;
		String email;
		String teamName;
		Text comments;

		for (int x = 0; x < entries.length; x++) {
			fields = entries[x].split("\t");
			name = fields[1];
			email = fields[2];
			teamName = fields[0];

			// Comments for student are optional
			if (fields.length == 4) {
				comments = new Text(fields[3]);
			}

			else {
				comments = new Text("");
			}

			studentList.add(new Student(email, name, comments, courseID, teamName));

		}

		List<EnrollmentReport> enrollmentReportList = new ArrayList<EnrollmentReport>();

		// Check to see if there is an ongoing evaluation. If there is, do not edit
		// students' teams.

		List<Student> currentStudentList = courseFactory.getStudentsByCourse(courseID);

		EvaluationFactory evaluations = EvaluationFactory.getInstance();

		if (evaluations.isEvaluationOngoing(courseID)) {
			for (Student s : studentList) {
				for (Student cs : currentStudentList) {
					if (s.getEmail().equals(cs.getEmail()) && !s.getTeamName().equals(cs.getTeamName())) {
						s.setTeamName(cs.getTeamName());
					}
				}
			}
		}

		// Add and edit Student objects in the datastore
		enrollmentReportList.addAll(courseFactory.enrolStudents(studentList, courseID));

		resp.getWriter().write(
				"<enrollmentreports>" + parseEnrollmentReportListToXML(enrollmentReportList).toString()
						+ "</enrollmentreports>");
	}

	private void coordinatorGetCourse() throws IOException, ServletException {

		Course course = courseFactory.getCourse(req.getParameter(COURSE_ID));

		CourseSummaryForCoordinator courseSummary = new CourseSummaryForCoordinator(course.getID(),
				course.getName(), course.isArchived(), courseFactory.getNumberOfTeams(course.getID()));

		ArrayList<CourseSummaryForCoordinator> courseSummaryList = new ArrayList<CourseSummaryForCoordinator>();
		courseSummaryList.add(courseSummary);

		resp.getWriter().write(
				"<courses>" + parseCourseSummaryForCoordinatorListToXML(courseSummaryList).toString()
						+ "</courses>");

	}

	private void coordinatorGetCourseList() throws IOException, ServletException {

		String googleID = accountFactory.getUser().getNickname().toLowerCase();

		List<Course> courseList = courseFactory.getCoordinatorCourseList(googleID);
		ArrayList<CourseSummaryForCoordinator> courseSummaryList = new ArrayList<CourseSummaryForCoordinator>();

		for (Course c : courseList) {
			CourseSummaryForCoordinator cs = new CourseSummaryForCoordinator(c.getID(), c.getName(),
					c.isArchived(), courseFactory.getNumberOfTeams(c.getID()));
			courseSummaryList.add(cs);
		}

		resp.getWriter().write(
				"<courses>" + parseCourseSummaryForCoordinatorListToXML(courseSummaryList).toString()
						+ "</courses>");
	}

	private void coordinatorGetEvaluationList() throws IOException {

		String googleID = accountFactory.getUser().getNickname().toLowerCase();

		List<Course> courseList = courseFactory.getCoordinatorCourseList(googleID);

		EvaluationFactory evaluations = EvaluationFactory.getInstance();
		List<Evaluation> evaluationList = evaluations.getEvaluationList(courseList);

		List<EvaluationDetailsForCoordinator> evaluationDetailsList = new ArrayList<EvaluationDetailsForCoordinator>();

		int numberOfCompletedEvaluations = 0;
		int numberOfEvaluations = 0;

		for (Evaluation e : evaluationList) {
			if (courseFactory.getCourse(e.getCourseID()).isArchived() != true) {
				numberOfCompletedEvaluations = evaluations.getNumberOfCompletedEvaluations(e.getCourseID(),
						e.getName());
				numberOfEvaluations = evaluations.getNumberOfEvaluations(e.getCourseID(), e.getName());

				evaluationDetailsList.add(new EvaluationDetailsForCoordinator(e.getCourseID(), e.getName(),
						e.getInstructions(), e.isCommentsEnabled(), e.getStart(), e.getDeadline(), e
								.getGracePeriod(), e.isPublished(), e.isActivated(), numberOfCompletedEvaluations,
						numberOfEvaluations));
			}
		}

		resp.getWriter().write(
				"<evaluations>"
						+ parseEvaluationDetailsForCoordinatorListToXML(evaluationDetailsList).toString()
						+ "</evaluations>");
	}

	private void coordinatorGetStudentList() throws IOException, ServletException {

		List<Student> studentList = courseFactory.getStudentsByCourse(req.getParameter(COURSE_ID));

		resp.getWriter().write(
				"<students>" + parseStudentListToXML(studentList).toString() + "</students>");

	}

	private void coordinatorGetSubmissionList() throws IOException {
		String courseID = req.getParameter(COURSE_ID);
		String evaluationName = req.getParameter(EVALUATION_NAME);

		EvaluationFactory evaluations = EvaluationFactory.getInstance();
		List<Submission> submissionList = evaluations.getSubmissionList(courseID, evaluationName);

		List<SubmissionDetailsForCoordinator> submissionDetailsList = new ArrayList<SubmissionDetailsForCoordinator>();

		String fromStudentName = "";
		String toStudentName = "";

		Text fromStudentComments = null;
		Text toStudentComments = null;
		Student student = null;

		float pointsBumpRatio = 0;

		for (Submission s : submissionList) {
			student = courseFactory.getStudentWithEmail(courseID, s.getFromStudent());
			fromStudentName = student.getName();
			fromStudentComments = student.getComments();

			student = courseFactory.getStudentWithEmail(courseID, s.getToStudent());
			toStudentName = student.getName();
			toStudentComments = student.getComments();

			pointsBumpRatio = evaluations.calculatePointsBumpRatio(courseID, evaluationName,
					s.getFromStudent());

			submissionDetailsList.add(new SubmissionDetailsForCoordinator(courseID, evaluationName,
					fromStudentName, toStudentName, s.getFromStudent(), s.getToStudent(),
					fromStudentComments, toStudentComments, s.getTeamName(), s.getPoints(), pointsBumpRatio,
					s.getJustification(), s.getCommentsToStudent()));
		}

		resp.getWriter().write(
				"<submissions>"
						+ parseSubmissionDetailsForCoordinatorListToXML(submissionDetailsList).toString()
						+ "</submissions>");

	}

	private void coordinatorInformStudentsOfEvaluationChanges() {
		String courseID = req.getParameter(COURSE_ID);
		String evaluationName = req.getParameter(EVALUATION_NAME);

		List<Student> studentList = courseFactory.getStudentsByCourse(courseID);

		EvaluationFactory evaluations = EvaluationFactory.getInstance();
		evaluations.informStudentsOfChanges(studentList, courseID, evaluationName);

	}

	private void coordinatorLogin() throws IOException, ServletException {

		resp.getWriter().write(
				"<url><![CDATA[" + accountFactory.getLoginPage("/coordinator.jsp") + "]]></url>");

	}

	private void coordinatorLogout() throws IOException, ServletException {

		resp.getWriter().write("<url><![CDATA[" + accountFactory.getLogoutPage("") + "]]></url>");

	}

	private void coordinatorPublishEvaluation() {
		String courseID = req.getParameter(COURSE_ID);
		String name = req.getParameter(EVALUATION_NAME);

		List<Student> studentList = courseFactory.getStudentsByCourse(courseID);

		EvaluationFactory evaluations = EvaluationFactory.getInstance();
		evaluations.publishEvaluation(courseID, name, studentList);

	}

	private void coordinatorRemindStudents() {
		String courseID = req.getParameter(COURSE_ID);
		String evaluationName = req.getParameter(EVALUATION_NAME);

		List<Student> studentList = courseFactory.getStudentsByCourse(courseID);

		// Filter out students who have submitted the evaluation
		EvaluationFactory evaluations = EvaluationFactory.getInstance();
		Evaluation evaluation = evaluations.getEvaluation(courseID, evaluationName);

		List<Student> studentsToRemindList = new ArrayList<Student>();

		for (Student s : studentList) {
			if (!evaluations.isEvaluationSubmitted(evaluation, s.getEmail())) {
				studentsToRemindList.add(s);
			}
		}

		Date deadline = evaluations.getEvaluation(courseID, evaluationName).getDeadline();

		evaluations.remindStudents(studentsToRemindList, courseID, evaluationName, deadline);

	}

	private void coordinatorSendRegistrationKey() {

		String courseID = req.getParameter(COURSE_ID);
		String email = req.getParameter(STUDENT_EMAIL);

		Course course = courseFactory.getCourse(courseID);
		Student student = courseFactory.getStudentWithEmail(courseID, email);

		List<Student> studentList = new ArrayList<Student>();
		studentList.add(student);

		String courseName = course.getName();
		String coordinatorName = accountFactory.getCoordinatorName(course.getCoordinatorID());

		courseFactory.sendRegistrationKeys(studentList, courseID, courseName, coordinatorName);
	}

	private void coordinatorSendRegistrationKeys() {

		// Get unregistered students
		List<Student> studentList = courseFactory.getUnregisteredStudentList(req
				.getParameter(COURSE_ID));

		if (!studentList.isEmpty()) {
			Course course = courseFactory.getCourse(studentList.get(0).getCourseID());

			String courseID = course.getID();
			String courseName = course.getName();
			String coordinatorName = accountFactory.getCoordinatorName(course.getCoordinatorID());

			courseFactory.sendRegistrationKeys(studentList, courseID, courseName, coordinatorName);
		}
	}

	private void coordinatorUnarchiveCourse() throws IOException, ServletException {

		courseFactory.unarchiveCoordinatorCourse(req.getParameter(COURSE_ID));
	}

	private Date convertToDate(String date, int time) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		Calendar calendar = Calendar.getInstance();

		Date newDate = new Date();

		// Perform date manipulation
		try {
			newDate = sdf.parse(date);
			calendar.setTime(newDate);

			if (time == 24) {
				calendar.set(Calendar.HOUR, 23);
				calendar.set(Calendar.MINUTE, 59);
			}

			else {
				calendar.set(Calendar.HOUR, time);
			}

			return calendar.getTime();
		}

		catch (Exception e) {
			return null;
		}

	}

	private StringBuffer parseCourseDetailsForStudentToXML(CourseDetailsForStudent courseDetails) {
		StringBuffer sb = new StringBuffer();

		sb.append("<coursedetails>");
		sb.append("<" + COURSE_ID + "><![CDATA[" + courseDetails.getCourseID() + "]]></" + COURSE_ID
				+ ">");
		sb.append("<" + COURSE_NAME + "><![CDATA[" + courseDetails.getCourseName() + "]]></"
				+ COURSE_NAME + ">");
		sb.append("<" + COURSE_COORDINATORNAME + "><![CDATA[" + courseDetails.getCoordinatorName()
				+ "]]></" + COURSE_COORDINATORNAME + ">");
		sb.append("<" + STUDENT_TEAMNAME + "><![CDATA[" + courseDetails.getTeamName() + "]]></"
				+ STUDENT_TEAMNAME + ">");
		sb.append("<" + STUDENT_NAME + "><![CDATA[" + courseDetails.getStudentName() + "]]></"
				+ STUDENT_NAME + ">");
		sb.append("<" + STUDENT_EMAIL + "><![CDATA[" + courseDetails.getStudentEmail() + "]]></"
				+ STUDENT_EMAIL + ">");
		sb.append("<" + STUDENT_TEAMMATES + ">");

		for (String s : courseDetails.getTeammateList()) {
			sb.append("<" + STUDENT_TEAMMATE + "><![CDATA[" + s + "]]></" + STUDENT_TEAMMATE + ">");
		}

		sb.append("</" + STUDENT_TEAMMATES + ">");
		sb.append("</coursedetails>");

		return sb;
	}

	@Deprecated
	private StringBuffer parseCourseSummaryForCoordinatorListToXML(
			ArrayList<CourseSummaryForCoordinator> courseSummaryList) {
		StringBuffer sb = new StringBuffer();

		for (CourseSummaryForCoordinator cs : courseSummaryList) {
			sb.append("<coursesummary>");
			sb.append("<" + COURSE_ID + "><![CDATA[" + cs.getID() + "]]></" + COURSE_ID + ">");
			sb.append("<" + COURSE_NAME + "><![CDATA[" + cs.getName() + "]]></" + COURSE_NAME + ">");
			sb.append("<" + COURSE_STATUS + ">" + cs.isArchived() + "</" + COURSE_STATUS + ">");
			sb.append("<" + COURSE_NUMBEROFTEAMS + ">" + cs.getNumberOfTeams() + "</"
					+ COURSE_NUMBEROFTEAMS + ">");
			sb.append("</coursesummary>");
		}

		return sb;
	}

	@Deprecated
	private StringBuffer parseCourseSummaryForStudentListToXML(
			ArrayList<CourseSummaryForStudent> courseSummaryList) {
		StringBuffer sb = new StringBuffer();

		for (CourseSummaryForStudent cs : courseSummaryList) {
			sb.append("<coursesummary>");
			sb.append("<" + COURSE_ID + "><![CDATA[" + cs.getID() + "]]></" + COURSE_ID + ">");
			sb.append("<" + COURSE_NAME + "><![CDATA[" + cs.getName() + "]]></" + COURSE_NAME + ">");
			sb.append("<" + COURSE_STATUS + ">" + cs.isArchived() + "</" + COURSE_STATUS + ">");
			sb.append("<" + STUDENT_TEAMNAME + "><![CDATA[" + cs.getTeamName() + "]]></"
					+ STUDENT_TEAMNAME + ">");
			sb.append("</coursesummary>");
		}

		return sb;
	}

	private StringBuffer parseEnrollmentReportListToXML(List<EnrollmentReport> enrollmentReportList) {
		StringBuffer sb = new StringBuffer();

		for (EnrollmentReport er : enrollmentReportList) {
			sb.append("<enrollmentreport>");
			sb.append("<" + STUDENT_NAME + "><![CDATA[" + er.getName() + "]]></" + STUDENT_NAME + ">");
			sb.append("<" + STUDENT_EMAIL + "><![CDATA[" + er.getEmail() + "]]></" + STUDENT_EMAIL + ">");
			sb.append("<" + STUDENT_STATUS + "><![CDATA[" + er.getStatus() + "]]></" + STUDENT_STATUS
					+ ">");
			sb.append("<" + STUDENT_NAMEEDITED + ">" + er.isNameEdited() + "</" + STUDENT_NAMEEDITED
					+ ">");
			sb.append("<" + STUDENT_TEAMNAMEEDITED + ">" + er.isTeamNameEdited() + "</"
					+ STUDENT_TEAMNAMEEDITED + ">");
			sb.append("<" + STUDENT_COMMENTSEDITED + ">" + er.isCommentsEdited() + "</"
					+ STUDENT_COMMENTSEDITED + ">");
			sb.append("</enrollmentreport>");
		}

		return sb;
	}

	private StringBuffer parseEvaluationDetailsForCoordinatorListToXML(
			List<EvaluationDetailsForCoordinator> evaluationDetailsList) {
		StringBuffer sb = new StringBuffer();

		for (EvaluationDetailsForCoordinator e : evaluationDetailsList) {
			sb.append("<evaluation>");

			sb.append("<" + COURSE_ID + "><![CDATA[" + e.getCourseID() + "]]></" + COURSE_ID + ">");
			sb.append("<" + EVALUATION_NAME + "><![CDATA[" + e.getName() + "]]></" + EVALUATION_NAME
					+ ">");
			sb.append("<" + EVALUATION_COMMENTSENABLED + "><![CDATA[" + e.isCommentsEnabled() + "]]></"
					+ EVALUATION_COMMENTSENABLED + ">");
			sb.append("<" + EVALUATION_INSTRUCTIONS + "><![CDATA[" + e.getInstructions().getValue()
					+ "]]></" + EVALUATION_INSTRUCTIONS + ">");
			sb.append("<" + EVALUATION_START + "><![CDATA["
					+ DateFormat.getDateTimeInstance().format(e.getStart()) + "]]></" + EVALUATION_START
					+ ">");
			sb.append("<" + EVALUATION_DEADLINE + "><![CDATA["
					+ DateFormat.getDateTimeInstance().format(e.getDeadline()) + "]]></"
					+ EVALUATION_DEADLINE + ">");
			sb.append("<" + EVALUATION_GRACEPERIOD + "><![CDATA[" + e.getGracePeriod() + "]]></"
					+ EVALUATION_GRACEPERIOD + ">");
			sb.append("<" + EVALUATION_PUBLISHED + "><![CDATA[" + e.isPublished() + "]]></"
					+ EVALUATION_PUBLISHED + ">");
			sb.append("<" + EVALUATION_ACTIVATED + "><![CDATA[" + e.isActivated() + "]]></"
					+ EVALUATION_ACTIVATED + ">");
			sb.append("<" + EVALUATION_NUMBEROFCOMPLETEDEVALUATIONS + "><![CDATA["
					+ e.getNumberOfCompletedEvaluations() + "]]></" + EVALUATION_NUMBEROFCOMPLETEDEVALUATIONS
					+ ">");
			sb.append("<" + EVALUATION_NUMBEROFEVALUATIONS + "><![CDATA[" + e.getNumberOfEvaluations()
					+ "]]></" + EVALUATION_NUMBEROFEVALUATIONS + ">");

			sb.append("</evaluation>");
		}

		return sb;
	}

	private StringBuffer parseEvaluationListToXML(List<Evaluation> evaluationList) {
		StringBuffer sb = new StringBuffer();

		for (Evaluation e : evaluationList) {
			sb.append("<evaluation>");
			sb.append("<" + COURSE_ID + "><![CDATA[" + e.getCourseID() + "]]></" + COURSE_ID + ">");
			sb.append("<" + EVALUATION_NAME + "><![CDATA[" + e.getName() + "]]></" + EVALUATION_NAME
					+ ">");
			sb.append("<" + EVALUATION_START + ">"
					+ DateFormat.getDateTimeInstance().format(e.getStart()) + "</" + EVALUATION_START + ">");
			sb.append("<" + EVALUATION_DEADLINE + ">"
					+ DateFormat.getDateTimeInstance().format(e.getDeadline()) + "</" + EVALUATION_DEADLINE
					+ ">");
			sb.append("<" + EVALUATION_GRACEPERIOD + ">" + e.getGracePeriod() + "</"
					+ EVALUATION_GRACEPERIOD + ">");
			sb.append("<" + EVALUATION_INSTRUCTIONS + "><![CDATA[" + e.getInstructions().getValue()
					+ "]]></" + EVALUATION_INSTRUCTIONS + ">");
			sb.append("<" + EVALUATION_COMMENTSENABLED + "><![CDATA[" + e.isCommentsEnabled() + "]]></"
					+ EVALUATION_COMMENTSENABLED + ">");
			sb.append("<" + EVALUATION_PUBLISHED + ">" + e.isPublished() + "</" + EVALUATION_PUBLISHED
					+ ">");
			sb.append("</evaluation>");
		}

		return sb;
	}

	private StringBuffer parseStudentListToXML(List<Student> studentList) {
		StringBuffer sb = new StringBuffer();

		for (Student s : studentList) {
			sb.append("<student>");
			sb.append("<" + STUDENT_NAME + "><![CDATA[" + s.getName() + "]]></" + STUDENT_NAME + ">");
			sb.append("<" + STUDENT_EMAIL + "><![CDATA[" + s.getEmail() + "]]></" + STUDENT_EMAIL + ">");
			sb.append("<" + STUDENT_ID + "><![CDATA[" + s.getID() + "]]></" + STUDENT_ID + ">");
			sb.append("<" + STUDENT_COMMENTS + "><![CDATA[" + s.getComments().getValue() + "]]></"
					+ STUDENT_COMMENTS + ">");
			sb.append("<" + STUDENT_REGKEY + "><![CDATA["
					+ KeyFactory.createKeyString(Student.class.getSimpleName(), s.getRegistrationKey())
					+ "]]></" + STUDENT_REGKEY + ">");
			sb.append("<" + STUDENT_COURSEID + "><![CDATA[" + s.getCourseID() + "]]></"
					+ STUDENT_COURSEID + ">");
			sb.append("<" + STUDENT_TEAMNAME + "><![CDATA[" + s.getTeamName() + "]]></"
					+ STUDENT_TEAMNAME + ">");
			sb.append("</student>");
		}

		return sb;
	}

	private StringBuffer parseSubmissionDetailsForCoordinatorListToXML(
			List<SubmissionDetailsForCoordinator> submissionDetailsList) {
		StringBuffer sb = new StringBuffer();

		for (SubmissionDetailsForCoordinator s : submissionDetailsList) {
			sb.append("<submission>");
			sb.append("<" + STUDENT_TEAMNAME + "><![CDATA[" + s.getTeamName() + "]]></"
					+ STUDENT_TEAMNAME + ">");
			sb.append("<" + STUDENT_FROMSTUDENTNAME + "><![CDATA[" + s.getFromStudentName() + "]]></"
					+ STUDENT_FROMSTUDENTNAME + ">");
			sb.append("<" + STUDENT_TOSTUDENTNAME + "><![CDATA[" + s.getToStudentName() + "]]></"
					+ STUDENT_TOSTUDENTNAME + ">");
			sb.append("<" + STUDENT_FROMSTUDENT + "><![CDATA[" + s.getFromStudent() + "]]></"
					+ STUDENT_FROMSTUDENT + ">");
			sb.append("<" + STUDENT_TOSTUDENT + "><![CDATA[" + s.getToStudent() + "]]></"
					+ STUDENT_TOSTUDENT + ">");
			sb.append("<" + STUDENT_FROMSTUDENTCOMMENTS + "><![CDATA["
					+ s.getFromStudentComments().getValue() + "]]></" + STUDENT_FROMSTUDENTCOMMENTS + ">");
			sb.append("<" + STUDENT_TOSTUDENTCOMMENTS + "><![CDATA["
					+ s.getToStudentComments().getValue() + "]]></" + STUDENT_TOSTUDENTCOMMENTS + ">");
			sb.append("<" + COURSE_ID + "><![CDATA[" + s.getCourseID() + "]]></" + COURSE_ID + ">");
			sb.append("<" + EVALUATION_NAME + "><![CDATA[" + s.getEvaluationName() + "]]></"
					+ EVALUATION_NAME + ">");
			sb.append("<" + STUDENT_POINTS + ">" + s.getPoints() + "</" + STUDENT_POINTS + ">");
			sb.append("<" + STUDENT_POINTSBUMPRATIO + ">" + s.getPointsBumpRatio() + "</"
					+ STUDENT_POINTSBUMPRATIO + ">");
			sb.append("<" + STUDENT_JUSTIFICATION + "><![CDATA[" + s.getJustification().getValue()
					+ "]]></" + STUDENT_JUSTIFICATION + ">");
			sb.append("<" + STUDENT_COMMENTSTOSTUDENT + "><![CDATA["
					+ s.getCommentsToStudent().getValue() + "]]></" + STUDENT_COMMENTSTOSTUDENT + ">");
			sb.append("</submission>");
		}

		return sb;
	}

	private StringBuffer parseSubmissionDetailsForStudentListToXML(
			List<SubmissionDetailsForStudent> submissionDetailsList) {
		StringBuffer sb = new StringBuffer();

		for (SubmissionDetailsForStudent s : submissionDetailsList) {
			sb.append("<submission>");
			sb.append("<" + STUDENT_TEAMNAME + "><![CDATA[" + s.getTeamName() + "]]></"
					+ STUDENT_TEAMNAME + ">");
			sb.append("<" + STUDENT_FROMSTUDENTNAME + "><![CDATA[" + s.getFromStudentName() + "]]></"
					+ STUDENT_FROMSTUDENTNAME + ">");
			sb.append("<" + STUDENT_TOSTUDENTNAME + "><![CDATA[" + s.getToStudentName() + "]]></"
					+ STUDENT_TOSTUDENTNAME + ">");
			sb.append("<" + STUDENT_FROMSTUDENT + "><![CDATA[" + s.getFromStudent() + "]]></"
					+ STUDENT_FROMSTUDENT + ">");
			sb.append("<" + STUDENT_TOSTUDENT + "><![CDATA[" + s.getToStudent() + "]]></"
					+ STUDENT_TOSTUDENT + ">");
			sb.append("<" + COURSE_ID + "><![CDATA[" + s.getCourseID() + "]]></" + COURSE_ID + ">");
			sb.append("<" + EVALUATION_NAME + "><![CDATA[" + s.getEvaluationName() + "]]></"
					+ EVALUATION_NAME + ">");
			sb.append("<" + STUDENT_POINTS + ">" + s.getPoints() + "</" + STUDENT_POINTS + ">");
			sb.append("<" + STUDENT_JUSTIFICATION + "><![CDATA[" + s.getJustification().getValue()
					+ "]]></" + STUDENT_JUSTIFICATION + ">");
			sb.append("<" + STUDENT_COMMENTSTOSTUDENT + "><![CDATA["
					+ s.getCommentsToStudent().getValue() + "]]></" + STUDENT_COMMENTSTOSTUDENT + ">");
			sb.append("</submission>");
		}

		return sb;
	}

	private void studentArchiveCourse() {

		String googleID = accountFactory.getUser().getNickname();

		String courseID = req.getParameter(COURSE_ID);

		courseFactory.archiveStudentCourse(courseID, googleID);
	}

	private void studentDeleteCourse() {

		String googleID = accountFactory.getUser().getNickname();

		String courseID = req.getParameter(COURSE_ID);

		courseFactory.deleteStudentCourse(courseID, googleID);

	}

	private void studentGetCourse() throws IOException {

		String googleID = accountFactory.getUser().getNickname();
		String courseID = req.getParameter(COURSE_ID);

		Course course = courseFactory.getCourse(courseID);
		String courseName = course.getName();
		String coordinatorName = accountFactory.getCoordinatorName(course.getCoordinatorID());

		Student student = courseFactory.getStudentWithID(courseID, googleID);

		String studentEmail = student.getEmail();
		String studentName = student.getName();
		String teamName = courseFactory.getTeamName(courseID, studentEmail);

		ArrayList<String> teammateList = new ArrayList<String>();
		List<Student> studentList = courseFactory.getStudentsByCourse(courseID);

		for (Student s : studentList) {
			if (!s.getID().equals(googleID) && s.getTeamName().equals(teamName)) {
				teammateList.add(s.getName());
			}
		}

		CourseDetailsForStudent courseDetails = new CourseDetailsForStudent(courseID, courseName,
				coordinatorName, teamName, studentName, studentEmail, teammateList);

		resp.getWriter().write(parseCourseDetailsForStudentToXML(courseDetails).toString());
	}

	/**
	 * Retrieve a list of courses
	 * 
	 * @throws IOException
	 * @deprecated
	 */
	private void studentGetCourseList() throws IOException {

		String googleID = accountFactory.getUser().getNickname();

		List<Student> studentList = courseFactory.getStudent(googleID);

		ArrayList<CourseSummaryForStudent> courseSummaryList = new ArrayList<CourseSummaryForStudent>();

		String courseID = "";
		String courseName = "";
		String teamName = "";

		boolean archived = false;

		Course course = null;

		for (Student s : studentList) {
			courseID = s.getCourseID();
			course = courseFactory.getCourse(courseID);
			courseName = course.getName();
			archived = s.isCourseArchived();
			teamName = courseFactory.getTeamName(courseID, s.getEmail());

			courseSummaryList.add(new CourseSummaryForStudent(courseID, courseName, teamName, archived));
		}

		resp.getWriter().write(
				"<courses>" + parseCourseSummaryForStudentListToXML(courseSummaryList).toString()
						+ "</courses>");
	}

	private void studentGetPastEvaluationList() throws IOException {

		String googleID = accountFactory.getUser().getNickname();

		List<Student> studentList = courseFactory.getStudent(googleID);

		List<Course> courseList = new ArrayList<Course>();

		for (Student s : studentList) {
			courseList.add(courseFactory.getCourse(s.getCourseID()));
		}

		EvaluationFactory evaluations = EvaluationFactory.getInstance();
		List<Evaluation> evaluationList = evaluations.getEvaluationList(courseList);

		// Filter evaluationList - make sure archived and unsubmitted(unless
		// evaluation is published and open evaluations are not
		// returned to student
		List<Evaluation> filteredEvaluationList = new ArrayList<Evaluation>();

		String email = "";
		Student student = null;

		Calendar now = Calendar.getInstance();
		Calendar start = Calendar.getInstance();
		Calendar deadline = Calendar.getInstance();

		// SGP timezone
		now.set(Calendar.HOUR_OF_DAY, now.get(Calendar.HOUR_OF_DAY) + 8);

		for (Evaluation e : evaluationList) {
			student = courseFactory.getStudentWithID(e.getCourseID(), googleID);

			if (student != null) {
				email = student.getEmail();

				start.setTime(e.getStart());
				deadline.setTime(e.getDeadline());

				if (e.isPublished()) {
					filteredEvaluationList.add(e);
				}

				else if (now.after(deadline) && !student.isCourseArchived()) {
					filteredEvaluationList.add(e);
				}

				else {
					if (evaluations.isEvaluationSubmitted(e, email) && (now.after(start))
							&& !student.isCourseArchived()) {
						filteredEvaluationList.add(e);
					}
				}
			}

			else {
				continue;
			}
		}

		resp.getWriter().write(
				"<evaluations>" + parseEvaluationListToXML(filteredEvaluationList).toString()
						+ "</evaluations>");
	}

	private void studentGetPendingEvaluationList() throws IOException {

		String googleID = accountFactory.getUser().getNickname();

		List<Student> studentList = courseFactory.getStudent(googleID);

		List<Course> courseList = new ArrayList<Course>();

		for (Student s : studentList) {
			courseList.add(courseFactory.getCourse(s.getCourseID()));
		}

		EvaluationFactory evaluations = EvaluationFactory.getInstance();
		List<Evaluation> evaluationList = evaluations.getEvaluationList(courseList);

		// Filter evaluationList - make sure archived, submitted and closed
		// evaluations are not returned to student
		List<Evaluation> filteredEvaluationList = new ArrayList<Evaluation>();

		String email = "";
		Student student = null;

		Calendar now = Calendar.getInstance();
		Calendar start = Calendar.getInstance();
		Calendar deadline = Calendar.getInstance();

		// SGP time zone
		now.set(Calendar.HOUR_OF_DAY, now.get(Calendar.HOUR_OF_DAY) + 8);

		for (Evaluation e : evaluationList) {
			student = courseFactory.getStudentWithID(e.getCourseID(), googleID);

			if (student != null) {
				email = student.getEmail();

				start.setTime(e.getStart());
				deadline.setTime(e.getDeadline());

				if (!evaluations.isEvaluationSubmitted(e, email)
						&& (now.after(start) && now.before(deadline)) && !student.isCourseArchived()) {
					filteredEvaluationList.add(e);
				}
			}

			else {
				continue;
			}
		}

		resp.getWriter().write(
				"<evaluations>" + parseEvaluationListToXML(filteredEvaluationList).toString()
						+ "</evaluations>");

	}

	private void studentGetSubmissionList() throws IOException {

		String googleID = accountFactory.getUser().getNickname();

		String courseID = req.getParameter(COURSE_ID);
		String evaluationName = req.getParameter(EVALUATION_NAME);

		// fromStudent is the Student's email

		Student student = courseFactory.getStudentWithID(courseID, googleID);

		String fromStudent = student.getEmail();
		String fromStudentName = student.getName();
		String teamName = student.getTeamName();

		EvaluationFactory evaluations = EvaluationFactory.getInstance();
		List<Submission> submissionList = evaluations.getSubmissionFromStudentList(courseID,
				evaluationName, fromStudent);

		List<SubmissionDetailsForStudent> submissionDetailsList = new ArrayList<SubmissionDetailsForStudent>();

		for (Submission s : submissionList) {
			student = courseFactory.getStudentWithEmail(courseID, s.getToStudent());

			// Always return the student's own submission first
			if (s.getToStudent().equals(fromStudent)) {
				submissionDetailsList.add(
						0,
						new SubmissionDetailsForStudent(courseID, evaluationName, fromStudentName, student
								.getName(), fromStudent, student.getEmail(), teamName, s.getPoints(), s
								.getJustification(), s.getCommentsToStudent()));
			}

			else {
				submissionDetailsList.add(new SubmissionDetailsForStudent(courseID, evaluationName,
						fromStudentName, student.getName(), fromStudent, student.getEmail(), teamName, s
								.getPoints(), s.getJustification(), s.getCommentsToStudent()));
			}
		}

		resp.getWriter().write(
				"<submissions>"
						+ parseSubmissionDetailsForStudentListToXML(submissionDetailsList).toString()
						+ "</submissions>");
	}

	private void studentGetSubmissionResultsList() throws IOException {

		String googleID = accountFactory.getUser().getNickname();

		String courseID = req.getParameter(COURSE_ID);
		String evaluationName = req.getParameter(EVALUATION_NAME);

		// fromStudent is the Student's email

		Student student = courseFactory.getStudentWithID(courseID, googleID);

		String toStudent = student.getEmail();
		String toStudentName = student.getName();
		String teamName = student.getTeamName();

		EvaluationFactory evaluations = EvaluationFactory.getInstance();
		List<Submission> submissionList = evaluations.getSubmissionToStudentList(courseID,
				evaluationName, toStudent);

		List<SubmissionDetailsForStudent> submissionDetailsList = new ArrayList<SubmissionDetailsForStudent>();
		float pointsBumpRatio = 1;

		for (Submission s : submissionList) {
			student = courseFactory.getStudentWithEmail(courseID, s.getFromStudent());

			// Always return the student's own submission first
			if (s.getFromStudent().equals(toStudent)) {
				submissionDetailsList.add(
						0,
						new SubmissionDetailsForStudent(courseID, evaluationName, student.getName(),
								toStudentName, student.getEmail(), toStudent, teamName, s.getPoints(), s
										.getJustification(), s.getCommentsToStudent()));
			}

			else {
				// Need to aggregate points given to each student, except the target
				// student
				pointsBumpRatio = evaluations.calculatePointsBumpRatio(courseID, evaluationName,
						s.getFromStudent());

				submissionDetailsList.add(new SubmissionDetailsForStudent(courseID, evaluationName, student
						.getName(), toStudentName, student.getEmail(), toStudent, teamName, s.getPoints()
						* pointsBumpRatio, s.getJustification(), s.getCommentsToStudent()));
			}

		}

		resp.getWriter().write(
				"<submissions>"
						+ parseSubmissionDetailsForStudentListToXML(submissionDetailsList).toString()
						+ "</submissions>");

	}

	@Deprecated
	private void studentLogin() throws IOException {

		resp.getWriter().write(
				"<url><![CDATA[" + accountFactory.getLoginPage("/student.jsp") + "]]></url>");
	}

	private void studentLogout() throws IOException {

		resp.getWriter().write("<url><![CDATA[" + accountFactory.getLogoutPage("") + "]]></url>");
	}

	private void studentSubmitEvaluation() throws IOException {
		List<Submission> submissionList = new ArrayList<Submission>();

		int numberOfSubmissions = Integer.parseInt(req.getParameter(STUDENT_NUMBEROFSUBMISSIONS));

		String fromStudent = "";
		String toStudent = "";
		int points = 0;
		String justification = "";
		String commentsToStudent = "";

		String courseID = req.getParameter(COURSE_ID);
		String evaluationName = req.getParameter(EVALUATION_NAME);
		String teamName = req.getParameter(STUDENT_TEAMNAME);

		// Make sure the deadline is not up yet, including grace period
		EvaluationFactory evaluations = EvaluationFactory.getInstance();

		Calendar now = Calendar.getInstance();
		Calendar deadline = Calendar.getInstance();

		Evaluation evaluation = evaluations.getEvaluation(courseID, evaluationName);
		deadline.setTime(evaluation.getDeadline());
		deadline.set(Calendar.MINUTE, deadline.get(Calendar.MINUTE) + evaluation.getGracePeriod());

		if (now.after(deadline)) {
			resp.getWriter().write(
					MSG_STATUS_OPENING + MSG_EVALUATION_DEADLINEPASSED + MSG_STATUS_CLOSING);
		}

		else {

			for (int x = 0; x < numberOfSubmissions; x++) {
				fromStudent = req.getParameter(STUDENT_FROMSTUDENT + x);
				toStudent = req.getParameter(STUDENT_TOSTUDENT + x);
				points = Integer.parseInt(req.getParameter(STUDENT_POINTS + x));
				justification = req.getParameter(STUDENT_JUSTIFICATION + x);
				commentsToStudent = req.getParameter(STUDENT_COMMENTSTOSTUDENT + x);

				submissionList.add(new Submission(fromStudent, toStudent, courseID, evaluationName,
						teamName, points, new Text(justification), new Text(commentsToStudent)));
			}

			evaluations.editSubmissions(submissionList);

		}

		resp.getWriter().write(MSG_STATUS_OPENING + "nil" + MSG_STATUS_CLOSING);
	}

	private void studentUnarchiveCourse() {

		String googleID = accountFactory.getUser().getNickname();

		String courseID = req.getParameter(COURSE_ID);

		List<Student> studentList = courseFactory.getStudentsByCourse(courseID);

		for (Student s : studentList) {
			if (s.getID().equals(googleID)) {
				courseFactory.unarchiveStudentCourse(courseID, s.getEmail());
				break;
			}
		}

	}

}

