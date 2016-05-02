package core;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateful;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import appointment.IAppointmentManagementBean;
import user.IUserManagementBean;
import entities.Appointment;
import entities.UserAccount;
import enumerations.EAllocationType;
import enumerations.ERegisterNotifications;

/**
 * Implementation of ICoreManagementBean
 */
@Stateful
public class CoreManagementBean implements ICoreManagementBean {
	@EJB
	private IUserManagementBean userManager;

	@EJB
	private IAppointmentManagementBean appointmentManager;

	@PersistenceContext
	private EntityManager entityManager;

	
	/**
	 * Register an account with given data.
	 * @param userName Username of the account to register.
	 * @param firstName First name of user.
	 * @param lastName Last name of user.
	 * @param password Chosen password.
	 * @param passwordRepeat Password repetition. Has to match the parameter "password".
	 * @param email - Email address of user.
	 * @return A notification about the result of registration.
	 */
	public ERegisterNotifications register(String userName, String firstName, String lastName, String password, String passwordRepeat, String email) {
		return userManager.register(userName, firstName, lastName, password, passwordRepeat, email);
	}
	
	
	/**
	 * Tries to login the user with username/password combination.
	 * @param userName Username of user.
	 * @param password Password of user.
	 * @return <i>account of the logged in user</i> - if login was successful.<br>
	 * 		   <i>null</i> - else
	 */
	public UserAccount login(String userName, String password) {
		return userManager.login(userName, password);
	}

	
    /**
     * Current user will be logged out.
     */
	public void logout() {
		userManager.logout();
	}
	
	
	/**
	 * Returns the currently logged in user.
	 * @return <i>The user currently logged in</i> - if existent<br>
	 * 		   <i>null</i> - else
	 */
	public UserAccount getCurrentUser() {
		return userManager.getCurrentUser();
	}

	
	/**
	 * Creates an appointment with the given parameters.
	 * @param userName Owner of the appointment.
	 * @param partnerUserName Username of user, the appointment will be shared with. 
	 * <i>null</i> or empty String for no partner.
	 * @param title Title of the appointment.
	 * @param isPrivate <i>true</i> if the appointment should be private.
	 * @param startDate Start date of the appointment.
	 * @param endDate End date of the appointment.
	 * @param allocationType AllocationType of the appointment.
	 * @param notes Additional notes for the appointment.
	 * @return The appointment - if it can be created correctly<br>
	 * 		   <i>null</i> - else
	 */
	public Appointment addAppointment(String userName, String partnerUserName, String title, 
			boolean isPrivate, Date startDate, Date endDate, EAllocationType allocationType, String notes) {

		boolean conflict = false;
		boolean isPartner = false;

		// check title
		if (title.equals("")) {
			addNotification(userName, "Error: Please add a title for the appointment!");
			return null;
		}

		// check if partner is different from currentUser
		if (partnerUserName.equals(userName)) {
			addNotification(userName, "Error: You can not invite yourself!");
			return null;
		}

		// check if partner input is valid
		if (checkPartnerValid(partnerUserName)) {
			// check if partner exists
			if (checkUserExistent(partnerUserName)) {
				isPartner = true;
			} else {
				addNotification(userName, "Error: Partner does not exist!");
				return null;
			}
		}

		// check if startDate is before endDate
		if (startDate.getTime() > endDate.getTime()) {
			addNotification(userName, "Error: Start date can not be after end date!");
			return null;
		}

		// check for conflicts of current user
		conflict = appointmentManager.isAppointmentInConflict(userName, startDate, endDate, allocationType, userName);

		// check for conflicts of partner
		if (isPartner) {
			conflict = conflict || appointmentManager.isAppointmentInConflict(partnerUserName, startDate, endDate, allocationType, userName);
		}

		if (!conflict) {
			
			// initialize and save appointment for user
			Appointment appointment = initializeAppointment (title, isPrivate, startDate,
					endDate, allocationType, notes);
			appointmentManager.saveAppointment(appointment.getId(), userName);

			if (isPartner) {
				// initialize and save appointment for partner (if existent)
				Appointment partnerAppointment = initializeAppointment(title, 
						isPrivate, startDate, endDate, allocationType, notes);
				appointmentManager.saveAppointment(partnerAppointment.getId(), partnerUserName);
				
				// link both appointments via partnerAppointemntId
				partnerAppointment.setPartnerAppointmentId(appointment.getId());
				appointment.setPartnerAppointmentId(partnerAppointment.getId());
				
				// add notification for partner
				UserAccount partnerUserAccount = entityManager.find(UserAccount.class, partnerUserName);
				partnerUserAccount.getNotifications().add(userName + " created an appointment with you:  " + appointment.getTitle());
				
				// update partner appointment for persistence
				entityManager.merge(partnerAppointment);
			
			} else {
				// no partner added --> partnerAppointmentId = 0
				appointment.setPartnerAppointmentId(0);
			}
			
			// update user appointment for persistence
			entityManager.merge(appointment);
			
			return appointment;
		}
		return null;
	}
	

	/**
	 * Private method to initialize the appointment.
	 * @param title Title of the appointment.
	 * @param isPrivate <i>true</i> if the appointment should be private.
	 * @param startDate Start date of the appointment.
	 * @param endDate End date of the appointment.
	 * @param allocationType AllocationType of the appointment.
	 * @param notes Additional notes for the appointment.
	 * @return appointment - if initialization successful<br>
	 *         <i>null</i> - else
	 */
	private Appointment initializeAppointment(String title, boolean isPrivate, 
			Date startDate, Date endDate, EAllocationType allocationType, String notes) {
		
		Appointment appointment = new Appointment();
		appointment.setTitle(title);
		appointment.setPrivate(isPrivate);
		appointment.setStartDate(startDate);
		appointment.setEndDate(endDate);
		appointment.setType(allocationType);
		appointment.setNotes(notes);

		entityManager.persist(appointment);
		
		return appointment;
	}


	/**
	 * Deletes the appointment with given ID.
	 * @param appointmentId ID of the appointment you with to delete.
	 */
	public boolean deleteAppointment(int appointmentId) {

		UserAccount currentUser = userManager.getCurrentUser();
		if (currentUser == null) {
			return false;
		}

		Appointment appointment = entityManager.find(Appointment.class, appointmentId);
		if (appointment == null) {
			return false;
		}

		appointmentManager.deleteAppointment(appointmentId);

		String notification = "";
		if (currentUser.getUserName().equals(appointment.getUserAccount().getUserName())) {
			notification = "You removed the appointment: " + appointment.getTitle();
		} else {
			notification = currentUser.getUserName() + " removed the shared appointment: " + appointment.getTitle();
		}

		appointment.getUserAccount().getNotifications().add(notification);
		entityManager.persist(appointment.getUserAccount());

		return true;
	}	

	
	/**
	 * Method to get all appointments of given user within time from start date to end date.
	 * If isOwner is set to <i>false</i>, the list will only contain non-private appointments of the given user.
	 * @param userName Username of the user, the appointment list will be created for.
	 * @param isOwner <i>true</i> if the list should only contain appointments, the user is owner of.
	 * @param startDate Start date of the appointments.
	 * @param endDate End date of the appointments.
	 * @return A list of appointments - if given data is valid<br>
	 * 		   <i>null</i> - else
	 */
	public List<Appointment> getAppointments(String userName, boolean isOwner, Date startDate, Date endDate) {

		if (isCurrentUser(userName)) {
			return appointmentManager.getAppointments(userName, isOwner, startDate, endDate);
		} else {
			return appointmentManager.getAppointments(userName, false, startDate, endDate);
		}
	}

	
	/**
	 * Method to return all enum types of EAllocationType.
	 * @return List of allocation types.
	 */
	public List<EAllocationType> getAllocationTypes() {
		List<EAllocationType> appointmentTypes = new ArrayList<EAllocationType>();

		for (EAllocationType type : EAllocationType.values()) {
			appointmentTypes.add(type);
		}

		return appointmentTypes;
	}
	
	
	/**
	 * Adds a notification to the account of the given user.
	 * @param userName Notification will be added to this user.
	 * @param notification The notification to add.
	 * @return <i>true</i>  - if notification added successfully
	 *         <i>false</i> - else
	 */
	public boolean addNotification(String userName, String notification) {
		if (userName == null || notification == null) {
			return false;
		}
		
		UserAccount userAccount = entityManager.find(UserAccount.class, userName);
		userAccount.getNotifications().add(notification);
		//entityManager.persist(userAccount);
		entityManager.merge(userAccount);
		
		return true;
	}	
	
	
	/**
	 * Clears the list of notifications for given user.
	 * @param userName Notification list will be cleared for this user.
	 */
	public boolean clearNotifications(String userName) {
		if (userName == null) {
			return false;
		}
		
		UserAccount userAccount = entityManager.find(UserAccount.class, userName);
		userAccount.setNotifications(new ArrayList<String>());
		//entityManager.persist(userAccount);
		entityManager.merge(userAccount);
		
		return true;
	}
	
	
	/**
	 * Method returns a global access to PersistenceContext.
	 * @return The entity manger for persistence operations.
	 */
	public EntityManager getEntityManager() {
		return entityManager;
	}
	

	/**
	 * Private method to check if given user is current user.
	 * @param userNameToCheck Username of user to check
	 * @return <i>true</i>  - if given user is currently logged in
	 *         <i>false</i> - else
	 */
	private boolean isCurrentUser(String userNameToCheck) {

		UserAccount currentUser = null;
		if (userManager.getCurrentUser() == null) {
			return false;
		}

		currentUser = userManager.getCurrentUser();
		if (currentUser.getUserName().endsWith(userNameToCheck)) {
			return true;
		}

		return false;
	}

	
	/**
	 * Private method to check if given partner user name is valid string.
	 * @param partnerUserName Username to check.
	 * @return <i>true</i>  - if username is valid
	 *         <i>false</i> - else
	 */
	private boolean checkPartnerValid(String partnerUserName) {
		if (partnerUserName != null && !partnerUserName.equals("")) {
			return true;
		} else {
			return false;
		}
	}

	
	/**
	 * Private method to check if user with given username exists.
	 * @param userName Username of user to check.
	 * @return <i>true</i>  - if user is existent
	 *         <i>false</i> - else
	 */
	private boolean checkUserExistent(String userName) {
		if (userName != null) {
			if (entityManager.find(UserAccount.class, userName) != null) return true;
		}

		return false;
	}
}

