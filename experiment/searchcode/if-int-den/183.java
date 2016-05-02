/**
 * @author Manuel Guentzel
 */
package user;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Vector;

import javax.servlet.http.HttpSession;

import logger.Log;
import servlet.Helper;
import userManagement.LoggedInUsers;
import database.DatabaseController;
import database.account.Account;
import database.document.Document;
import database.institute.Institute;

/**
 * Verwaltet alle Aufgaben und Daten eines Admins.
 */
public class Admin extends User {

	/**
	 * Konstruktor. Erstellte Objekte werden automatisch in der LoggedInUsers
	 * klasse aufgenommen.
	 * 
	 * @param username
	 *            Benutzername im System.
	 * @param email
	 *            E-Mail Adresse.
	 * @param name
	 *            Realer Name.
	 * @param session
	 *            Session des Benutzers.
	 */
	public Admin(String username, String email, String name, HttpSession session) {
		super(username, email, name, null, session);
		userManagement.LoggedInUsers.addUser(this);
		this.log = Log.getInstance();
	}

	/**
	 * Loescht einen neuen Account.
	 * 
	 * @param username
	 *            Username des zu loeschenden Accounts.
	 * @return Gibt an, ob die Operation erfolgreich war.
	 */

	public boolean deleteAccount(String username) {
		Account account = acccon.getAccountByUsername(username);
		if (account == null) {
			log.write("Admin", "Can not delete <" + username
					+ ">. Does not exist!");
			// response.setContentType("text/error");
			// response.getWriter().write("This user doesn't seem to exist!");
			return false;
		}
		// If user is currently logged in, we do not allow deletion:
		if (LoggedInUsers.getUserByUsername(username) != null) {
			log.write("Admin", "Can not delete <" + username
					+ "> as currently logged in!");
			return false;
		}
		// check if it's a provider account
		if (account.getAccounttype() == 1) {
			acccon.deleteProviderAccount(account);
		}// check if it's a clerk account
		else if (account.getAccounttype() == 2) {
			acccon.deleteClerkAccount(account);
		}// check if it's an applicant account
		else if (account.getAccounttype() == 3) {
			acccon.deleteApplicantAccount(account);
		}// check if it's an admin account
		else if (account.getAccounttype() == 0) {
			acccon.deleteAccount(account);
		}
		log.write("Admin", "<" + getUserData().getUsername()
				+ "> deleted account with username <" + username + ">");

		return true;
	}

	/**
	 * Gibt alle existierenden Accounts zurueck.
	 * @return Vektor mit Accounts
	 */
	public Vector<Account> getAccounts(){
		return acccon.getAllAccounts();
	}
	/**
	 * Gibt alle DokumentArchetypen des Systems zurueck.
	 * @return Vektor mit Dokumenten
	 */
	public Vector<Document> getDocuments(){
		return doccon.getAllDocuments();
	}
	/**
	 * Gibt alle Institute zurueck.
	 * @return Vektor mit Instituten
	 */
	public Vector<Institute> getInstitutes(){
		return instcon.getAllInstitutes();
	}
	
	/**
	 * Gibt ein spezifisches Dokument zurueck.
	 * @param uid 
	 * 			ID des gesuchten Dokuments.
	 * @return Das Dokument.
	 */
	public Document getSpecificDocument(int uid){
		return doccon.getDocumentByUID(uid);
	}
	/**
	 * Gibt ein spezifisches Institut zurueck.
	 * @param id 
	 * 			ID des gesuchten Instituts.
	 * @return Das Institut.
	 */
	public Institute getSpecificInstitute(int id){
		return instcon.getInstituteByIID(id);
	}
	
	/**
	 * Gibt den Account eines bestimmten Users zurueck.
	 * @param username 
	 * 			Username des zu suchenden Accounts.
	 * @return Account des Users.
	 */
	public Account getUserAccount(String username){
		return acccon.getAccountByUsername(username);
	}
	/**
	 * Gibt die Systeminformationen als Json String zurueck
	 * @return Jsonstring mit Systeminformationen
	 */
	public String getSysteminfo(){
		Runtime r = Runtime.getRuntime();
		String[] names = new String[] { "loggedInUsers", "allUsers",
				"totalRAM", "maxRAM" };
		Object[] objects = new Object[] { LoggedInUsers.getUsers().size(),
				acccon.accountCount(),
				"~" + r.totalMemory() / (1024 * 1024) + " MB",
				"~" + r.maxMemory() / (1024 * 1024) + " MB" };
		return Helper.jsonAtor(names, objects);
	}
	

	/**
	 * Erstellt einen Account.
	 * 
	 * @param account
	 *            zu erstellender Account.
	 * @return Gibt an, ob die Operation erfolgreich war.
	 */
	public boolean createAccount(Account account) {
		if (!acccon.createAccount(account)) {
			// This can happen if the institute doesn't exist:
			log.write("Admin",
					"Error creating account! Is the institute valid?");
			return false;
		}
		log.write("Admin", "<" + getUserData().getUsername()
				+ "> created account for <" + account.getUsername() + ">.");
		return true;
	}

	/**
	 * Editiert einen Account. Der Benutzername ist dabei nicht aenderbar und
	 * identifiziert den zu aendernden Account in der Datenbank.
	 * 
	 * @param account
	 *            geaenderter Account.
	 * @return Gibt an, ob die Operation erfolgreich war.
	 */
	public boolean editAccount(Account account) {
		// For debugging wrong character set:
		// System.out.println(account.getName());
		if (!acccon.updateAccount(account)) {
			log.write("Admin", "Error modifying account!");
			return false;
		}
		log.write("Admin", "<" + getUserData().getUsername()
				+ "> modified account of <" + account.getUsername() + ">.");
		return true;
	}

	/**
	 * Loescht ein Administrator Dokument aus der Datenbank.
	 * 
	 * @param uid
	 *            ID des zu loeschenden Dokuments.
	 * @return Gibt an, ob die Operation erfolgreich war.
	 */
	public boolean deleteDoc(int uid) {
		Document doc = doccon.getDocumentByUID(uid);
		if(doc==null){
			log.write("Admin", "Error deleting document!");
			return false;
		}
		if (!doccon.deleteDocument(doc)) {
			log.write("Admin", "Error deleting document!");
			return false;
		} else {
			log.write("Admin", "<" + getUserData().getUsername()
					+ "> deleted document <" + doc.getName() + ">.");
			return true;
		}
	}

	/**
	 * Fuegt ein Administrator Dokument hinzu.
	 * 
	 * @param doc
	 *            Das hinzuzufuegende Dokument.
	 * @return Gibt an, ob das Dokument hinzugefuegt worden konnte.
	 */
	public boolean addDoc(Document doc) {
		if (!doccon.createDocument(doc)) {
			log.write("Admin", "Error adding a document!");
			return false;
		} else {
			log.write("Admin", "<" + getUserData().getUsername()
					+ "> added document <" + doc.getName() + ">.");
			return true;
		}
	}

	/**
	 * Editiert ein Administrator Dokument.
	 * 
	 * @param doc
	 *            geaendertes Dokument
	 * @return Gibt an, ob das Editieren erfolgreich war.
	 */
	public boolean editDoc(Document doc) {
		if (!doccon.updateDocument(doc)) {
			log.write("Admin", "Error updating document!");
			return false;
		} else {
			log.write("Admin", "<" + getUserData().getUsername()
					+ "> modified document <" + doc.getName() + ">.");
			return true;
		}
	}

	/**
	 * Fuegt ein neues Institut dem System hinzu.
	 * 
	 * @param institute 
	 * 				Institut das hinzugefuegt werden soll.
	 * @return Wahrheitswert, ob der Vorgang erfolgreich war.
	 */
	public boolean addInstitute(Institute institute) {
		if (!instcon.addInstitute(institute)) {
			log.write("Admin", "Error creating institute!");
			return false;
		} else {
			log.write("Admin", "<" + getUserData().getUsername()
					+ "> added institute <" + institute.getName() + ">.");
			return true;
		}
	}

	/**
	 * Loescht ein Institut aus dem System. 
	 * 
	 * @param institute 
	 * 				zu loeschendes Institut.
	 * @return Wahrheitswert, ob der Vorgang erfolgreich war.
	 */
	public boolean deleteInstitute(Institute institute) {
		if (!instcon.deleteInstitute(institute)) {
			log.write("Admin", "Error deleting institute!");
			return false;
		} else {
			log.write("Admin", "<" + getUserData().getUsername()
					+ "> deleted institute <" + institute.getName() + ">.");
			return true;
		}
	}

	/**
	 * Liest die Standardwerte eines Angebots aus der Datenbank und gibt sie als
	 * JSON-Objekt zurueck.
	 * 
	 * @return Das JSON-Objekt mit den Werten.
	 */
	public String readDefValues() {
		String ret = new String();
		ResultSet rs = DatabaseController.getInstance().select(
				new String[] { "*" }, new String[] { "Standardangebot" }, null);
		try {
			if (rs.next()) {
				ret = Helper.jsonAtor(
						new String[] { "hoursMonth", "wage", "startDate",
								"endDate" },
						new Object[] { rs.getInt("StdProMonat"),
								rs.getFloat("Lohn"),
								rs.getDate("StartDatum"),
								rs.getDate("EndDatum") });
			}
		} catch (SQLException e) {
			// e.printStackTrace();
		}
		if (ret.isEmpty()) {
			log.write("Admin", "Error reading default offer values!");
			return null;
		}
		return ret;
	}

	/**
	 * Speichert die neuen Werte fuer das Standard-Angebot in der Datenbank.
	 * 
	 * @param hoursMonth
	 *            Die neue Stundenanzahl.
	 * @param wage
	 *            Der standard Lohn.
	 * @param startDate
	 *            Das neue start Datum.
	 * @param endDate
	 *            Das neue end Datum.
	 * @return Flag zur kontrolle ob alles geklappt hat.
	 */
	public boolean writeDefValues(int hoursMonth, float wage, Date startDate,
			Date endDate) {
		if (!DatabaseController.getInstance()
				.update("Standardangebot",
						new String[] { "StdProMonat", "StartDatum", "EndDatum",
								"Lohn" },
						new Object[] { hoursMonth, startDate, endDate, wage },
						"true")) {
			log.write("Admin", "Error updating default offer!");
			return false;
		} else {
			// log.write("Admin", "<" + getUserData().getUsername()+
			// "> modified default offer.");
			return true;
		}
	}

}
