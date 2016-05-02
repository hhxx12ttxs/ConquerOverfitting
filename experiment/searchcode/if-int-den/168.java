/**
 *@author Tamino Hartmann
 *@author Patryk Boczon
 *@author Manuel Guentzel
 *@author Laura Irlinger
 *@author Oemer Sahin
 *@author Anatoli Brill
 */
package user;

import java.util.Vector;

import javax.servlet.http.HttpSession;

import servlet.Helper;

import database.account.Account;
import database.application.Application;
import database.document.AppDocument;
import database.document.Document;
import database.offer.Offer;

/**
 * Verwaltet alle Aufgaben und Daten eines Bewerbers.
 */
public class Applicant extends User {

	/**
	 * Konstruktor. Erstellte Objekte werden automatisch in der LoggedInUsers
	 * klasse aufgenommen.
	 * 
	 * @param username
	 *            Benutzername im System
	 * @param email
	 *            E-Mail Adresse
	 * @param name
	 *            Realer Name
	 * @param session
	 *            Session des Benutzers
	 */
	public Applicant(String username, String email, String name,
			HttpSession session) {
		super(username, email, name, null, session);
		userManagement.LoggedInUsers.addUser(this);
	}

	/**
	 * Editiert den eigenen Account. Der Benutzername ist dabei nicht aenderbar
	 * und identifiziert den zu aendernden Account in der Datenbank.
	 * 
	 * @param acc
	 *            geaenderter Account
	 * @return Gibt an, ob die Operation erfolgreich war.
	 */
	public boolean editAccount(Account acc) {

		if (!acccon.updateAccount(acc)) {
			log.write("Appllicant", "Error modifying account!");
			return false;
		}
		log.write("Applicant", "<" + getUserData().getUsername()
				+ "> modified account of <" + acc.getUsername() + ">.");
		return true;
	}

	/**
	 * Methode zum Loeschen seines Accounts
	 * 
	 * @return Beim erfolgreichen Entfernen wird ein TRUE zurueckgegeben. Falls
	 *         irgendwo ein Fehler aufgetreten ist wird ein FALSE
	 *         zurueckgegeben.
	 */
	public boolean deleteOwnAccount() {
		invalidate();
		return acccon.deleteApplicantAccount(this);
	}

	/**
	 * Bewerben auf ein Angebot
	 * 
	 * @param offerID
	 *            ID des Angebots
	 * @return Wahrheitswert ob erfolgreich.
	 */
	public boolean apply(int offerID) {
		Application app = new Application(getUserData().getUsername(), offerID,
				false, null, false);
		return appcon.createApplication(app);

	}

	/**
	 * Loescht die Bewerbung.
	 * 
	 * @param appToDelete
	 *            zu loeschende Bewerbung
	 * @return Wahrheitswert ob erfolgreich.
	 */
	public boolean deleteApplication(Application appToDelete) {
		return appcon.deleteApplication(appToDelete);

	}

	/**
	 * Gibt eine Bewerbung zurueck
	 * 
	 * @param id
	 *            ID der Bewerbung
	 * @return Bewerbung.
	 */
	public Application getApplication(int id) {
		return appcon.getApplicationByOfferAndUser(id, getUserData()
				.getUsername());
	}

	/**
	 * Liefert alle Informationen zu einer Bewerbung als JSON-Objekt zurueck.
	 * 
	 * @param aid
	 *            ID der Bewerbung
	 * @return JSON-Objekt mit den Informationen.
	 */
	public String getApplicationInfo(int aid) {
		Application app = appcon.getApplicationByOfferAndUser(aid,
				getUserData().getUsername());
		if (app == null)
			return null;
		Offer off = getOffer(aid);
		return Helper.jsonAtor(new String[] { "offerName", "author", "status",
				"descr" }, new Object[] { off.getName(), off.getAuthor(),
				app.isChosen(), off.getDescription() });
	}

	/**
	 * Gibt Angebote zurueck auf die der Benutzer sich beworben hat.
	 * 
	 * @return Vektor mit Angeboten
	 */
	public Vector<Offer> myOffers() {
		return offcon.getOffersByApplicant(getUserData().getUsername());
	}

	/**
	 * Gibt Angebote zurueck auf die der Benutzer sich bewerben kann.
	 * 
	 * @return Vektor mit Angeboten
	 */
	public Vector<Offer> possibleOffers() {
		return offcon.getPossibleOffers(getUserData().getUsername());
	}

	/**
	 * Gibt ein Angebot zurueck
	 * 
	 * @param id
	 *            ID des Angebots
	 * @return Das Angebot
	 */
	public Offer getOffer(int id) {
		return offcon.getOfferById(id);
	}

	/**
	 * Gibt Dokumentdaten zurueck
	 * 
	 * @param aid
	 *            Angebots ID
	 * @return Daten der Dokumente zu diesem Angebot.
	 */
	public Vector<String> getDocuments(int aid) {
		// Create JSON version of custom data:
		Vector<String> docDataObject = new Vector<String>();
		for (AppDocument appDoc : doccon.getAppDocument(aid, getUserData()
				.getUsername())) {
			int UID = appDoc.getdID();
			Document doc = doccon.getDocumentByUID(UID);
			String dataObject = "{name:\"";
			// Write name to dataObject:
			dataObject += doc.getName();
			dataObject += "\",isChecked:";
			// Write if it has been checked:
			dataObject += (appDoc.getPresent()) ? 1 : 0;
			dataObject += "}";
			// Do nice things to wrap it up:
			docDataObject.add(dataObject);
		}
		return docDataObject;
	}

	/**
	 * Holt die Emailaddresse eines Benutzers aus der Datenbank.
	 * 
	 * @param user
	 *            Der Benutzer dessen Email gefragt ist.
	 * @return Die Emailaddresse.
	 */
	public String getEmail(String user) {
		try {
			return acccon.getAccountByUsername(user).getEmail();
		} catch (NullPointerException e) {
			log.write("Applicant", "Error getting e-mail adress of user <"
					+ user + ">");
			return null;
		}
	}
}

