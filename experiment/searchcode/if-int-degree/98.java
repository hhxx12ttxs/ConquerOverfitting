package org.abc.swms.action;

import java.util.ArrayList;


import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.abc.swms.dao.ProjectDAO;
import org.abc.swms.dao.TaskDAO;
import org.abc.swms.dao.TimesheetDAO;
import org.abc.swms.dao.UserDAO;
import org.abc.swms.entity.Project;
import org.abc.swms.entity.Task;
import org.abc.swms.entity.Timesheet;
import org.abc.swms.entity.User;
import org.apache.struts2.ServletActionContext;
import org.abc.swms.action.MyGoogleMailSender;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;

public class LeadAction extends ActionSupport {
	private Task task = new Task();
	private Project p = new Project();
	private List<Task> tasks;
	private List<Project> allProjects;
	private List<Project> projects;
	private User consultant;
	private TaskDAO taskDAO = new TaskDAO();
	private UserDAO userDAO = new UserDAO();
	private ProjectDAO projectDAO = new ProjectDAO();
	private TimesheetDAO timesheetDAO = new TimesheetDAO();
	private Calendar firstDayOfWeek = Calendar.getInstance();
	private Calendar lastDayOfWeek = Calendar.getInstance();
	// Consultants
	
	private List<User> allUser = userDAO.list();
	private List<User> freeConsultants = new ArrayList<User>();
	private List<User> selectedConsultants = new ArrayList<User>();
	static private List<User> temp = new ArrayList<User>();
	private List<Timesheet> relatedTimesheets;
	private List<String> rightSide;
	private List<String> defaultRightSide;
	private boolean timesheetStatus[] = new boolean[100];
	
	//private List<String> consultantIds;// list consultants id to be assigned to
										// a task
	private List<Project> leadProjects = new ArrayList<Project>();
	private List<Task> leadProjectTasks = new ArrayList<Task>();
	private List<Timesheet> timesheets = new ArrayList<Timesheet>();// list all timesheets
															// of consultants
	private List<Timesheet> selectedTimesheets = new ArrayList<Timesheet>();// filter of
																	// timesheets
																	// for
																	// repeat
																	// avoidance
	
	private int currentConsultantId;
	private Date currentDate;
	private String approve;
	private String disapprove;
	private String unlock;

	public String viewConsultantList() throws Exception {
		Map session = ActionContext.getContext().getSession();
		Integer id = (Integer) session.get("user.id");
		setLeadProjects(new ProjectDAO().listByLead(id));
		for (int i = 0; i < leadProjects.size(); i++) {
			setLeadProjectTasks(taskDAO.listByProjectId(leadProjects.get(i)
					.getId()));
			for (int j = 0; j < leadProjectTasks.size(); j++) {
				timesheets = new TimesheetDAO().listByTaskId(leadProjectTasks
						.get(j).getId());
				for (int k = 0; k < timesheets.size(); k++) {
					int degree = 1;
					if (selectedTimesheets.isEmpty()) {
						selectedTimesheets.add(timesheets.get(k));
					} else {
						for (int m = 0; m < selectedTimesheets.size(); m++) {
							Timesheet temp = selectedTimesheets.get(m);
							if (temp.getUser()
									.getFullname()
									.equals(timesheets.get(k).getUser()
											.getFullname())) {
								if (!temp
										.getTask()
										.getProject()
										.getName()
										.equals(timesheets.get(k).getTask()
												.getProject().getName())) {
									degree = 2;
									selectedTimesheets.remove(m);
									String temp2 = timesheets.get(k).getTask()
											.getProject().getName();
									timesheets
											.get(k)
											.getTask()
											.getProject()
											.setName(
													temp.getTask().getProject()
															.getName()
															+ ", " + temp2);
								} else {
									degree = 3;
								}
							}
						}
						if (degree == 1 || degree == 2) {
							selectedTimesheets.add(timesheets.get(k));
						}
					}

				}

			}

		}
		return SUCCESS;
	}

	// Tasks
	public String viewTaskList() throws Exception {
		Map session = ActionContext.getContext().getSession();
		Integer id = (Integer) session.get("user.id");
		tasks = taskDAO.listByLead(id);
		return SUCCESS;
	}

	public String createTask_input() throws Exception {
		p = projectDAO.findById(Integer.parseInt(ServletActionContext
				.getRequest().getParameter("projectId")));
		task.setProject(p);
		return SUCCESS;
	}

	public String createTask() throws Exception {
		tasks = taskDAO.listByProjectId(task.getProject().getId());
		int expectedTotalHours = 0;
		for (int i = 0; i < tasks.size(); i++){
			expectedTotalHours += tasks.get(i).getExpectedTotalHours();
		}
		if (expectedTotalHours + task.getExpectedTotalHours() > projectDAO.findById(task.getProject().getId()).getExpectedTotalHours()){
			addActionError("Expected total man-hours of tasks exceeds the total of the project");
			return INPUT;
		}
		
		Project project = projectDAO.findById(task.getProject().getId());
		
		if (task.getCreatedDate().compareTo(project.getStartDate())<0){
			addActionError("Start date of task could not be earlier start date of project");
			return INPUT;
		}
		
		if (task.getDueDate().compareTo(project.getDueDate())>0){
			addActionError("Due date of task could not be later than due date of project");
			return INPUT;
		}
		
		taskDAO.create(task);
		Calendar cal = Calendar.getInstance();
		int offset = cal.get(Calendar.DAY_OF_WEEK) == 1 ? -6 : -(cal
				.get(Calendar.DAY_OF_WEEK) - 2);
		cal.add(Calendar.DATE, offset);
		/*
		if(this.defaultRightSide.size() == 0){
			addActionError(task.getDescription() + ": Please select consultants!");
		}*/
		// lead can create task first, then edit task and assign more consultants later
		String []sendTo= new String[defaultRightSide.size()];
		String emailFromAddress="swmssystem@gmail.com",emailSubjectTxt="SWMS notification",
			emailMsgTxt="You are assigned to a new Task in SWMS system!";
		
		for (int i = 0; i < defaultRightSide.size(); i++) {
			User user = userDAO
					.findById(Integer.parseInt(defaultRightSide.get(i)));
			Timesheet t = new Timesheet();
			t.setTask(task);
			t.setUser(user);
			t.setStartDate(cal.getTime());
			t.setStatus("unlocked");
			new TimesheetDAO().create(t);
			sendTo[i] = user.getEmail();
			timesheets.add(t);
		}
		if (defaultRightSide.size() > 0){
			try{
				new MyGoogleMailSender().sendSSLMessage(
						sendTo, emailSubjectTxt, emailMsgTxt, emailFromAddress);
			}
			catch (Exception e){
				
			}
		}
		task.setTimesheets(timesheets);
		taskDAO.update(task);
		
		return SUCCESS;
	}

	public String editTask_input() throws Exception {
		task = taskDAO.findById(Integer.parseInt(ServletActionContext
				.getRequest().getParameter("taskId")));
		setRelatedTimesheets(timesheetDAO.listByTaskId(task.getId()));
		
		selectedConsultants = userDAO.listConsultantByTask(task);
		freeConsultants = userDAO.list();
		for (int i = 0; i < this.getSelectedConsultants().size(); i ++){
			for (int j = 0; j < this.getFreeConsultants().size(); j ++){
				if(this.getFreeConsultants().get(j).getId() == this.getSelectedConsultants().get(i).getId()){
					freeConsultants.remove(j);
				}
			}
		}
		temp = selectedConsultants;
		selectedConsultants = new ArrayList<User>();
		return SUCCESS;
	}

	public String updateTask() throws Exception {
		List<Task> tempTasks = taskDAO.listByProjectId(task.getProject().getId());
		int expectedTotalHours = 0;
		for (int i = 0; i < tempTasks.size(); i++){
			if (tempTasks.get(i).getId()!= task.getId()) expectedTotalHours += tempTasks.get(i).getExpectedTotalHours();
		}
		if (expectedTotalHours + task.getExpectedTotalHours() > projectDAO.findById(task.getProject().getId()).getExpectedTotalHours()){
			addActionError("Expected total man-hours of tasks exceeds the total of the project");
			return INPUT;
		}
		
		Project project = projectDAO.findById(task.getProject().getId());
		timesheets = timesheetDAO.listByTaskId(task.getId());
		
		if (task.getCreatedDate().compareTo(project.getStartDate())<0){	
			addActionError("Start date of task could not be earlier start date of project");
			return INPUT;
		}
		
		if (task.getDueDate().compareTo(project.getDueDate())>0){
			addActionError("Due date of task could not be later than due date of project");
			return INPUT;
		}

		task.setTimesheets(timesheets);
		
		taskDAO.update(task);
		
		Calendar cal = Calendar.getInstance();
		int offset = cal.get(Calendar.DAY_OF_WEEK) == 1 ? -6 : -(cal
				.get(Calendar.DAY_OF_WEEK) - 2);
		cal.add(Calendar.DATE, offset);
		String []sendTo= new String[rightSide.size()];
		String emailFromAddress="swmssystem@gmail.com",emailSubjectTxt="SWMS notification",
			emailMsgTxt="You are assigned to a new Task in SWMS system!";
		
		for (int i = 0; i < rightSide.size(); i ++){
			User user = userDAO.findById(Integer.parseInt(rightSide.get(i)));
			boolean check = true;
			for (int j = 0; j < temp.size(); j ++){
				if (temp.get(j).getId() == user.getId()){
					check = false;
				}
			}
			if (check == true){
				//System.out.println("User added is : " + user.getFullname());
				Timesheet t = new Timesheet();
				t.setTask(task);
				t.setUser(user);
				t.setStartDate(cal.getTime());
				t.setStatus("unlocked");
				sendTo[i] = user.getEmail();
				new TimesheetDAO().create(t);
			}
		}
		temp = null;
		if (rightSide.size() > 0){
			try{
				new MyGoogleMailSender().sendSSLMessage(
						sendTo, emailSubjectTxt, emailMsgTxt, emailFromAddress);
			}
			catch (Exception e){}
		}
		return SUCCESS;
	}

	public String deleteTask() throws Exception {
		task = taskDAO.findById(Integer.parseInt(ServletActionContext
				.getRequest().getParameter("taskId")));
		if (task.getTimesheets().size() == 0) {
			taskDAO.delete(task);
			return SUCCESS;
		} else
			return ERROR;
	}

	// Timesheets
	public String viewConsultantTimesheet() throws Exception {
		Map session = ActionContext.getContext().getSession();
		Integer leadId = (Integer) session.get("user.id");
		Integer consultantId = Integer.parseInt(ServletActionContext
				.getRequest().getParameter("userId"));
		String dateText = ServletActionContext.getRequest()
				.getParameter("Date");
		if (dateText != null) {
			String[] param = dateText.split("/");
			firstDayOfWeek.clear();
			firstDayOfWeek.set(Integer.parseInt(param[2]),
					Integer.parseInt(param[0]) - 1, Integer.parseInt(param[1]));
		}
		int offset = firstDayOfWeek.get(Calendar.DAY_OF_WEEK) == 1 ? -6
				: -(firstDayOfWeek.get(Calendar.DAY_OF_WEEK) - 2);
		firstDayOfWeek.add(Calendar.DATE, offset);
		setConsultant(userDAO.findById(consultantId));
		timesheets = timesheetDAO.listByDateLeadConsultant(
				firstDayOfWeek.getTime(), leadId, consultantId);

		lastDayOfWeek.clear();
		lastDayOfWeek.setTime(firstDayOfWeek.getTime());
		lastDayOfWeek.add(Calendar.DATE, 6);
		currentConsultantId = consultantId;
		currentDate = firstDayOfWeek.getTime();
		
		return SUCCESS;
	}
	
	public String viewWeeklyTaskTimesheet() throws Exception {
		Map session = ActionContext.getContext().getSession();
		Integer id = (Integer) session.get("user.id");
		String dateText = ServletActionContext.getRequest().getParameter("Date");
		task = taskDAO.findById(Integer.parseInt(ServletActionContext.getRequest().getParameter("taskId")));
		if (dateText != null){		
			String[] param = dateText.split("/");
			firstDayOfWeek.clear();
			firstDayOfWeek.set(Integer.parseInt(param[2]),Integer.parseInt(param[0])-1,Integer.parseInt(param[1]));
			int offset = firstDayOfWeek.get(Calendar.DAY_OF_WEEK) == 1 ? -6 : -(firstDayOfWeek.get(Calendar.DAY_OF_WEEK) - 2);
			firstDayOfWeek.add(Calendar.DATE, offset);
			timesheets = timesheetDAO.listByDateTask(firstDayOfWeek.getTime(), task);
		}else{
			// what is the date of the beginning of the week?
			Calendar cal = Calendar.getInstance();
			int offset = cal.get(Calendar.DAY_OF_WEEK) == 1 ? -6 : -(cal.get(Calendar.DAY_OF_WEEK) - 2);
			cal.add(Calendar.DATE, offset);
			timesheets = timesheetDAO.listByDateTask(cal.getTime(), task);
		}
		System.out.println(timesheets.size());
		lastDayOfWeek.clear();
		lastDayOfWeek.setTime(firstDayOfWeek.getTime());
		lastDayOfWeek.add(Calendar.DATE,6);
		
		return SUCCESS;
	}

	public String approveConsultantTimesheet() throws Exception {
		Map session = ActionContext.getContext().getSession();
		Integer leadId = (Integer) session.get("user.id");
		Integer consultantId = Integer.parseInt(ServletActionContext
				.getRequest().getParameter("userId"));
		String dateText = ServletActionContext.getRequest()
				.getParameter("Date");
		if (dateText != null) {
			String[] param = dateText.split("/");
			firstDayOfWeek.clear();
			firstDayOfWeek.set(Integer.parseInt(param[2]),
					Integer.parseInt(param[0]) - 1, Integer.parseInt(param[1]));
		}
		int offset = firstDayOfWeek.get(Calendar.DAY_OF_WEEK) == 1 ? -6
				: -(firstDayOfWeek.get(Calendar.DAY_OF_WEEK) - 2);
		firstDayOfWeek.add(Calendar.DATE, offset);
		setConsultant(userDAO.findById(consultantId));
		timesheets = timesheetDAO.listByDateLeadConsultant(
				firstDayOfWeek.getTime(), leadId, consultantId);

		lastDayOfWeek.clear();
		lastDayOfWeek.setTime(firstDayOfWeek.getTime());
		lastDayOfWeek.add(Calendar.DATE, 6);
		
		int length=timesheets.size();
		//timesheetStatus = new boolean[length];
		int count = 0;
		for (int i = 0; i < length; i ++){
			if (timesheetStatus[i] == true)count ++;
		}
		String []sendTo= new String[count];
		String emailFromAddress="swmssystem@gmail.com",emailSubjectTxt="SWMS notification",
		emailMsgTxt="Lead disapproved your submitted timesheets!";
		
		if(approve!= null){
			
			for(int i=0; i<length;i++){
				
				if(timesheetStatus[i]==true){
					timesheets.get(i).setStatus("approved");
					timesheetDAO.update(timesheets.get(i));
				}
			}
		}
		else if (disapprove != null){
			int index = 0;
			for(int i=0; i<length;i++){
				System.out.println(i + " " + timesheetStatus[i]);
				if(timesheetStatus[i]==true){
					timesheets.get(i).setStatus("unlocked");
					sendTo[index] = timesheets.get(i).getUser().getEmail();
					index ++;
					timesheetDAO.update(timesheets.get(i));
				}
			}
			if(index > 0){
				try{
					new MyGoogleMailSender().sendSSLMessage(
							sendTo, emailSubjectTxt, emailMsgTxt, emailFromAddress);
				}
				catch (Exception e){};
			}
		}else {
			int index = 0;
			for(int i=0; i<length;i++){
				System.out.println(i + " " + timesheetStatus[i]);
				if(timesheetStatus[i]==true){
					timesheets.get(i).setStatus("unlocked");
					sendTo[index] = timesheets.get(i).getUser().getEmail();
					index ++;
					timesheetDAO.update(timesheets.get(i));
				}
			}
			if(index > 0){
				try{
					emailMsgTxt="Lead unlocked your submitted timesheets!";
					new MyGoogleMailSender().sendSSLMessage(
							sendTo, emailSubjectTxt, emailMsgTxt, emailFromAddress);
				}
				catch (Exception e){};
			}
		}
		currentConsultantId = consultantId;
		currentDate = firstDayOfWeek.getTime();
		return SUCCESS;
	}

	public String disapproveConsultantTimesheet() throws Exception {
		//System.out.println("bla bla");
		Map session = ActionContext.getContext().getSession();
		Integer leadId = (Integer) session.get("user.id");
		Integer consultantId = Integer.parseInt(ServletActionContext
				.getRequest().getParameter("userId"));
		String dateText = ServletActionContext.getRequest()
				.getParameter("Date");
		if (dateText != null) {
			String[] param = dateText.split("/");
			firstDayOfWeek.clear();
			firstDayOfWeek.set(Integer.parseInt(param[2]),
					Integer.parseInt(param[0]) - 1, Integer.parseInt(param[1]));
		}
		int offset = firstDayOfWeek.get(Calendar.DAY_OF_WEEK) == 1 ? -6
				: -(firstDayOfWeek.get(Calendar.DAY_OF_WEEK) - 2);
		firstDayOfWeek.add(Calendar.DATE, offset);
		setConsultant(userDAO.findById(consultantId));
		timesheets = timesheetDAO.listByDateLeadConsultant(
				firstDayOfWeek.getTime(), leadId, consultantId);

		lastDayOfWeek.clear();
		lastDayOfWeek.setTime(firstDayOfWeek.getTime());
		lastDayOfWeek.add(Calendar.DATE, 6);
		
		int length=timesheets.size();
		int count = 0;
		for (int i = 0; i < length; i ++){
			if (timesheetStatus[i] == true)count ++;
		}
		String []sendTo= new String[count];
		String emailFromAddress="swmssystem@gmail.com",emailSubjectTxt="SWMS notification",
		emailMsgTxt="Lead disapproved your submitted timesheets!";
		int index = 0;
		for(int i=0; i<length;i++){
			System.out.println(i + " " + timesheetStatus[i]);
			if(timesheetStatus[i]== true){
				timesheets.get(i).setStatus("unlocked");
				sendTo[index] = timesheets.get(i).getUser().getEmail();
				index ++;
				timesheetDAO.update(timesheets.get(i));
			}
		}
		if(index > 0){
			try{
				new MyGoogleMailSender().sendSSLMessage(
						sendTo, emailSubjectTxt, emailMsgTxt, emailFromAddress);
			}
			catch (Exception e){};
		}
		return SUCCESS;
	}

	public String unlockConsultantTimesheet() throws Exception {
		return SUCCESS;
	}

	// Projects
	public String viewProjectList() throws Exception {
		Map session = ActionContext.getContext().getSession();
		Integer id = (Integer) session.get("user.id");
		projects = new ProjectDAO().listByLead(id);

		return SUCCESS;
	}

	// Getters & Setters
	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}

	public List<Task> getTasks() {
		return tasks;
	}

	public void setTasks(List<Task> tasks) {
		this.tasks = tasks;
	}

	public List<Timesheet> getTimesheets() {
		return timesheets;
	}

	public void setTimesheets(List<Timesheet> timesheets) {
		this.timesheets = timesheets;
	}

	public List<User> getConsultants() {
		return userDAO.list();
	}

	public void setProjects(List<Project> projects) {
		this.projects = projects;
	}

	public List<Project> getProjects() {
		// get current user id
		Map session = ActionContext.getContext().getSession();
		Integer id = (Integer) session.get("user.id");
		return projectDAO.listByLead(id);
	}

	public void setAllProjects(List<Project> allProjects) {
		this.allProjects = allProjects;
	}

	public List<Project> getAllProjects() {
		return allProjects;
	}

	public void setLeadProjects(List<Project> leadProjects) {
		this.leadProjects = leadProjects;
	}

	public List<Project> getLeadProjects() {
		return leadProjects;
	}

	public void setLeadProjectTasks(List<Task> leadProjectTasks) {
		this.leadProjectTasks = leadProjectTasks;
	}

	public List<Task> getLeadProjectTasks() {
		return leadProjectTasks;
	}

	public void setSelectedTimesheets(List<Timesheet> selectedTimesheets) {
		this.selectedTimesheets = selectedTimesheets;
	}

	public List<Timesheet> getSelectedTimesheets() {
		return selectedTimesheets;
	}

	public Calendar getFirstDayOfWeek() {
		return firstDayOfWeek;
	}

	public void setFirstDayOfWeek(Calendar firstDayOfWeek) {
		this.firstDayOfWeek = firstDayOfWeek;
	}

	public Calendar getLastDayOfWeek() {
		return lastDayOfWeek;
	}

	public void setLastDayOfWeek(Calendar lastDayOfWeek) {
		this.lastDayOfWeek = lastDayOfWeek;
	}

	public void setConsultant(User consultant) {
		this.consultant = consultant;
	}

	public User getConsultant() {
		return consultant;
	}

	public void setSelectedConsultants(List<User> selectedConsultants) {
		this.selectedConsultants = selectedConsultants;
	}

	public List<User> getSelectedConsultants() {
		return selectedConsultants;
	}

	public void setRelatedTimesheets(List<Timesheet> relatedTimesheets) {
		this.relatedTimesheets = relatedTimesheets;
	}

	public List<Timesheet> getRelatedTimesheets() {
		return relatedTimesheets;
	}

	public void setFreeConsultants(List<User> freeConsultants) {
		this.freeConsultants = freeConsultants;
	}

	public List<User> getFreeConsultants() {
		return freeConsultants;
	}

	public void setRightSide(List<String> rightSide) {
		this.rightSide = rightSide;
	}

	public List<String> getRightSide() {
		if (rightSide == null){
			rightSide = new ArrayList<String>();
		}
		return rightSide;
	}

	public void setAllUser(List<User> allUser) {
		this.allUser = allUser;
	}

	public List<User> getAllUser() {
		return allUser;
	}

	public void setTemp(List<User> temp){
		this.temp = temp;
	}
	
	public List<User> getTemp() {
		return temp;
	}
	
	public void setDefaultRightSide(List<String> defaultRightSide) {
		this.defaultRightSide = defaultRightSide;
	}

	public List<String> getDefaultRightSide() {
		return defaultRightSide;
	}

	public int getCurrentConsultantId() {
		return currentConsultantId;
	}

	public void setCurrentConsultantId(int currentConsultantId) {
		this.currentConsultantId = currentConsultantId;
	}

	public Date getCurrentDate() {
		return currentDate;
	}

	public void setCurrentDate(Date currentDate) {
		this.currentDate = currentDate;
	}

	public boolean[] getTimesheetStatus() {
		return timesheetStatus;
	}

	public void setTimesheetStatus(boolean[] timesheetStatus) {
		this.timesheetStatus = timesheetStatus;
	}

	public String getApprove() {
		return approve;
	}

	public void setApprove(String approve) {
		this.approve = approve;
	}

	public void setDisapprove(String disapprove) {
		this.disapprove = disapprove;
	}

	public String getDisapprove() {
		return disapprove;
	}

	public void setUnlock(String unlock) {
		this.unlock = unlock;
	}

	public String getUnlock() {
		return unlock;
	}
}

