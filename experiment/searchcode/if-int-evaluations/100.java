package teammates;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import teammates.exception.CourseDoesNotExistException;
import teammates.jdo.Course;
import teammates.jdo.EnrollmentReport;
import teammates.jdo.Evaluation;
import teammates.jdo.Student;
import teammates.jdo.Submission;
import teammates.jdo.SubmissionDetailsForCoordinator;

import com.google.appengine.api.datastore.Text;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * The API Servlet.
 * 
 * This is a hidden (to end user) servlet. It receives REST requests and
 * directly alter the data.
 * 
 * Mainly there for some automated testing purposes
 * 
 * @author nvquanghuy
 * 
 */
@SuppressWarnings("serial")
public class APIServlet extends HttpServlet {
	private HttpServletRequest	req;
	private HttpServletResponse	resp;
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException {
		doPost(req, resp);
	}
	
	private PersistenceManager getPM() {
		return Datastore.getPersistenceManager();
	}
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException {
		
		this.req = req;
		this.resp = resp;
		
		// TODO: Change to JSON/XML
		resp.setContentType("text/plain");
		
		// Check for auth code(to prevent misuse)
		String auth = req.getParameter("tm_auth");
		if( ! auth.equals(Config.API_AUTH_CODE)) {
			resp.getWriter().write("Authentication fails.");
			resp.flushBuffer();
			return;
		}
		
		String action = req.getParameter("action");
		System.out.println(action);
		if(action.equals("evaluation_open")) {
			evaluationOpen();
		} else if(action.equals("evaluation_close")) {
			evaluationClose();
		} else if(action.equals("evaluation_add")) {
			evaluationAdd();
		} else if(action.equals("evaluation_publish")) {
			evaluationPublish();
		} else if(action.equals("evaluation_unpublish")) {
			evaluationUnpublish();
		} else if(action.equals("course_add")) {
			courseAdd();
		} else if(action.equals("cleanup")) {
			totalCleanup();
		} else if(action.equals("cleanup_course")) {
			cleanupCourse();
		} else if(action.equals("cleanup_by_coordinator")) {
			totalCleanupByCoordinator();
		} else if(action.equals("enroll_students")) {
			enrollStudents();
		} else if(action.equals("student_submit_feedbacks")) {
			studentSubmitFeedbacks();
		} else if(action.equals("students_join_course")) {
			studentsJoinCourse();
		} else if(action.equals("email_stress_testing")) {
			emailStressTesting();
		} else if(action.equals("extend_class_size")) {
			extendClassSize();
		} else if(action.equals("update_bump_ratio")){
			updateBumpRatio();
		}else {
			
			System.err.println("Unknown command: " + action);
		}
		
		resp.flushBuffer();
	}
	
	/**
	 * Open an evaluation to students
	 */
	protected void evaluationOpen() throws IOException {
		System.out.println("Opening evaluation.");
		String courseID = req.getParameter("course_id");
		String name = req.getParameter("evaluation_name");
		
		boolean edited = Evaluations.inst().openEvaluation(courseID, name);
		
		if(edited) {
			resp.getWriter().write("ok");
		} else {
			resp.getWriter().write("fail");
		}
	}
	
	/**
	 * Close an evaluation
	 */
	protected void evaluationClose() throws IOException {
		System.out.println("Closing evaluation.");
		String courseID = req.getParameter("course_id");
		String name = req.getParameter("evaluation_name");
		
		boolean edited = Evaluations.inst().closeEvaluation(courseID, name);
		
		if(edited) {
			resp.getWriter().write("ok");
		} else {
			resp.getWriter().write("fail");
		}
	}
	
	/**
	 * Publish an evaluation
	 */
	protected void evaluationPublish() throws IOException {
		String courseID = req.getParameter("course_id");
		String name = req.getParameter("evaluation_name");
		
		Courses courses = Courses.inst();
		List<Student> studentList = courses.getStudentList(courseID);
		
		boolean edited = Evaluations.inst().publishEvaluation(courseID, name,
				studentList);
		
		if(edited) {
			resp.getWriter().write("ok");
		} else {
			resp.getWriter().write("fail");
		}
	}
	
	/**
	 * Unpublish an evaluation
	 */
	protected void evaluationUnpublish() throws IOException {
		String courseID = req.getParameter("course_id");
		
		String name = req.getParameter("evaluation_name");
		boolean edited = Evaluations.inst().unpublishEvaluation(courseID, name);
		if(edited) {
			resp.getWriter().write("ok");
		} else {
			resp.getWriter().write("fail");
		}
	}
	
	protected void evaluationAdd() throws IOException {
		String json = req.getParameter("evaluation");
		
		Gson gson = new Gson();
		Evaluation e = gson.fromJson(json, Evaluation.class);
		
		boolean edited = Evaluations.inst().addEvaluation(e);
		
		// TODO take a snapshot of submissions
		
		if(edited) {
			resp.getWriter().write("ok");
		} else {
			resp.getWriter().write("fail");
		}
	}
	
	/**
	 * Enroll students to course. Copied directly from TeammatesServlet.
	 * 
	 * TODO: take a look into the logic again.
	 * 
	 * @param studentList
	 * @param courseId
	 * @throws
	 */
	protected void enrollStudents() throws IOException {
		System.out.println("Enrolling students.");
		String courseId = req.getParameter("course_id");
		String str_json = req.getParameter("students");
		
		Gson gson = new Gson();
		Type listType = new TypeToken<List<Student>>() {
		}.getType();
		List<Student> studentList = gson.fromJson(str_json, listType);
		
		// Remove ID (Google ID) from studentList because if it's present, the
		// student will already be joined the course.
		for(Student s : studentList) {
			s.setID("");
		}
		
		List<EnrollmentReport> enrollmentReportList = new ArrayList<EnrollmentReport>();
		
		// Check to see if there is an ongoing evaluation. If there is, do not
		// edit
		// students' teams.
		Courses courses = Courses.inst();
		List<Student> currentStudentList = courses.getStudentList(courseId);
		Evaluations evaluations = Evaluations.inst();
		
		if(evaluations.isEvaluationOngoing(courseId)) {
			for(Student s : studentList) {
				for(Student cs : currentStudentList) {
					if(s.getEmail().equals(cs.getEmail())
							&& ! s.getTeamName().equals(cs.getTeamName())) {
						s.setTeamName(cs.getTeamName());
					}
				}
			}
		}
		// Add and edit Student objects in the datastore
		boolean edited = enrollmentReportList.addAll(courses.enrolStudents(
				studentList, courseId));
		
		if(edited) {
			resp.getWriter().write("ok");
		} else {
			resp.getWriter().write("fail");
		}
	}
	
	/**
	 * Delete all courses, evaluations, students, submissions. Except
	 * Coordinator
	 */
	@SuppressWarnings("unchecked")
	protected void totalCleanup() throws IOException {
		if(Config.development_mode) {
			System.out.println("Cleaning up.");
			
			// Delete all courses
			getPM().deletePersistentAll(Courses.inst().getAllCourses());
			
			// Delete all evaluations
			List<Evaluation> evals = Evaluations.inst().getAllEvaluations();
			getPM().deletePersistentAll(evals);
			
			// Delete all submissions
			List<Submission> submissions = (List<Submission>) getPM().newQuery(
					Submission.class).execute();
			getPM().deletePersistentAll(submissions);
			
			// Delete all students
			List<Student> students = (List<Student>) getPM().newQuery(
					Student.class).execute();
			getPM().deletePersistentAll(students);
			
			resp.getWriter().write("ok");
		} else {
			System.out.println("Production mode, disable cleaning data");
			resp.getWriter().write(
					"total clean up disabled for live site deploying");
		}
		
	}
	
	/**
	 * Clean up course, evaluation, submission related to the coordinator
	 * 
	 * @author wangsha
	 * @date Sep 8, 2011
	 */
	protected void totalCleanupByCoordinator() {
		String coordID = req.getParameter("coordinator_id");
		Courses.inst().cleanUpCoordinatorCourse(coordID);
		
	}
	
	/**
	 * Clean up everything about a particular course
	 */
	protected void cleanupCourse() {
		String courseID = req.getParameter("course_id");
		System.out.println("Cleaning everything about course " + courseID);
		// Delete course and enrolled students
		try {
			Courses.inst().cleanUpCourse(courseID);
			Evaluations.inst().deleteEvaluations(courseID);
		} catch(CourseDoesNotExistException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	protected void courseAdd() throws IOException {
		System.out.println("Adding new course");
		// String courseID = req.getParameter("course_id");
		// String courseName = req.getParameter("course_name");
		String googleID = req.getParameter("google_id");
		String json = req.getParameter("course");
		
		Gson gson = new Gson();
		Course c = gson.fromJson(json, Course.class);
		c.setCoordinatorID(googleID);
		
		getPM().makePersistent(c);
		
		resp.getWriter().write("ok");
	}
	
	protected void studentSubmitFeedbacks() throws IOException {
		
		String course_id = req.getParameter("course_id");
		String evaluation_name = req.getParameter("evaluation_name");
		String student_email = req.getParameter("student_email");
		System.out.println("Submitting feedback for student." + student_email);
		
		/*
		 * huy- Unable to use Transaction here. It says transaction batch
		 * operation must be on the same entity group (and must not be root
		 * entity). However it works for studentsJoinCourse below. ??? Aug 17 -
		 * It doesn't work for Join Course below either.
		 * http://code.google.com/appengine
		 * /docs/java/datastore/transactions.html
		 * #What_Can_Be_Done_In_a_Transaction
		 */
		
		Query query = getPM().newQuery(Submission.class);
		query.setFilter("courseID == course_id");
		query.setFilter("evaluationName == evaluation_name");
		query.setFilter("fromStudent == student_email");
		query.declareParameters("String course_id, String evaluation_name, String student_email");
		@SuppressWarnings("unchecked")
		List<Submission> submissions = (List<Submission>) query.execute(
				course_id, evaluation_name, student_email);
		
		for(Submission submission : submissions) {
			submission.setPoints((int) (Math.random() * 200));
			System.out.println("set " + submission.getFromStudentName() + "|"
					+ submission.getToStudentName() + ": "
					+ submission.getPoints());
			
			submission.setCommentsToStudent(new Text(String.format(
					"This is a public comment from %s to %s.", student_email,
					submission.getToStudent())));
			submission.setJustification(new Text(String.format(
					"This is a justification from %s to %s", student_email,
					submission.getToStudent())));
		}
		
	}
	
	protected void updateBumpRatio() {
		
		List<Evaluation> es = Evaluations.inst().getAllEvaluations();
		List<Submission> submissionList = null;
		for(Evaluation e : es) {
			
			submissionList = Evaluations.inst().getSubmissionList(
					e.getCourseID(), e.getName());
			for(Submission s : submissionList) {
				float pointsBumpRatio = 0;
				List<Submission> fromList = new LinkedList<Submission>();
				
				// filter submisstion list by fromStudent
				for(Submission fs : submissionList) {
					if(fs.getFromStudent().equals(s.getFromStudent()))
						fromList.add(fs);
				}
				
				pointsBumpRatio = Evaluations.inst().calculatePointsBumpRatio(
						e.getCourseID(), e.getName(), s.getFromStudent(), fromList);
				System.out.println("set bump ratio:" + pointsBumpRatio);
				s.setBumpRation(pointsBumpRatio);
			}
		}
		
		Datastore.getPersistenceManager().makePersistentAll(submissionList);
	}
	
	protected void studentsJoinCourse() throws IOException {
		System.out.println("Joining course for students.");
		
		// Set the Student.ID to emails.
		String course_id = req.getParameter("course_id");
		String str_json_students = req.getParameter("students");
		Type listType = new TypeToken<List<Student>>() {
		}.getType();
		Gson gson = new Gson();
		List<Student> students = gson.fromJson(str_json_students, listType);
		
		// Construct a Map< Email --> Student>
		HashMap<String, Student> mapStudents = new HashMap<String, Student>();
		for(Student s : students) {
			mapStudents.put(s.getEmail(), s);
		}
		
		// Query all Datastore's Student objects with CourseID received
		
		Query query = getPM().newQuery(Student.class);
		query.setFilter("courseID == course_id");
		query.declareParameters("String course_id");
		@SuppressWarnings("unchecked")
		List<Student> datastoreStudents = (List<Student>) query
				.execute(course_id);
		
		for(Student dsStudent : datastoreStudents) {
			Student jsStudent = mapStudents.get(dsStudent.getEmail());
			if(jsStudent != null) {
				dsStudent.setID(jsStudent.getID());
			}
		}
		// Store back to datastore
		getPM().makePersistentAll(datastoreStudents);
		
		resp.getWriter().write("Fail: something wrong");
	}
	
	/**
	 * Increase class size to 150, for testing evaluation report
	 * 
	 * @throws IOException
	 * @author wangsha
	 * @date Sep 8, 2011
	 */
	protected void extendClassSize() throws IOException {
		String course_id = req.getParameter("course_id");
		List<Student> newStudents = new LinkedList<Student>();
		System.out.println("extend class size for " + course_id);
		String evaluation_name = "Large Class Size";
		// Enroll Student
		for(int i = 0; i < 140; i ++ ) {
			newStudents.add(new Student("student" + i + "@comp.nus.edu.sg",
					"Name " + i, "googleID" + i, "comments to Name" + i,
					course_id, "Team " + i % 40));
		}
		getPM().makePersistentAll(newStudents);
		
		// Create Evaluation & Submission
		Evaluations.inst().addEvaluation(
				new Evaluation(course_id, evaluation_name, "", true, new Date(
						System.currentTimeMillis() - 24 * 60 * 60 * 1000),
						new Date(System.currentTimeMillis() + 24 * 60 * 60
								* 1000), 8.0, 10));
		
		// Fill in Submission
		Query query = getPM().newQuery(Submission.class);
		query.setFilter("courseID == course_id");
		query.setFilter("evaluationName == evaluation_name");
		query.declareParameters("String course_id, String evaluation_name");
		@SuppressWarnings("unchecked")
		List<Submission> submissions = (List<Submission>) query.execute(
				course_id, evaluation_name);
		
		for(Submission submission : submissions) {
			submission.setPoints((int) (Math.random() * 200));
			submission.setCommentsToStudent(new Text(String.format(
					"This is a public comment from %s to %s.",
					submission.getFromStudent(), submission.getToStudent())));
			submission.setJustification(new Text(String.format(
					"This is a justification from %s to %s",
					submission.getFromStudent(), submission.getToStudent())));
		}
		
		// Store back to datastore
		getPM().makePersistentAll(submissions);
		
	}
	
	protected void emailStressTesting() throws IOException {
		Emails emails = new Emails();
		String account = req.getParameter("account");
		int size = Integer.parseInt(req.getParameter("size"));
		
		emails.mailStressTesting(account, size);
		resp.getWriter().write("ok");
		
	}
}

