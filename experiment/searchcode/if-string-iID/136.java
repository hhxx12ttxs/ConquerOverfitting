/**
 * @author Patryk Boczon
 * @author Oemer Sahin
 * @author Manuel Guentzel
 * @author Tamino Hartmann
 * @author Laura Irlinger
 */
package database.institute;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import logger.Log;
import mail.Mailer;
import servlet.Helper;
import database.DatabaseController;
import database.account.Account;
import database.account.AccountController;
import database.offer.Offer;
import database.offer.OfferController;

public class InstituteController {

	/**
	 * Variable fuer die Instanz der Klasse.
	 */
	private static InstituteController instController;
	/**
	 * AccountController Instanz um beim Loeschen die Institute der Account
	 * Tabelle zu updaten
	 */
	private static AccountController acccon;
	/**
	 * Instanz des Loggers.
	 */
	private Log log;

	/**
	 * Gibt eine Instanz des InstituteContoller zurueck.
	 * 
	 * @return Es wird die Instanz zurueckgegeben.
	 */
	public static InstituteController getInstance() {
		if (instController == null)
			instController = new InstituteController();
		return instController;

	}

	/**
	 * Konstruktor
	 */	
	private InstituteController() {
		db = DatabaseController.getInstance();
		acccon = AccountController.getInstance();
		log= Log.getInstance();
		log.write("InstituteController",
				"Instance created.");
	}

	/**
	 * Diese Instanz dient zum Zugang in die Datenbank.
	 */
	public DatabaseController db;

	/**
	 * Gibt den Naen der Tabelle an.
	 */
	final static String tableName = "Institute";// tabellenname

	/**
	 * Gibt alle Institute der Datenbank zurueck.
	 * 
	 * @return Ein Vektor mit allen Instituten.
	 */
	public Vector<Institute> getAllInstitutes() {
		ResultSet rs = db.select(new String[] { "*" },
				new String[] { tableName }, null);
		Vector<Institute> inst = new Vector<Institute>();
		if(rs==null){
			log.write("InstituteController", "No connection: couldn't get institutes");
			return inst;
		}
		try {
			while (rs.next()) {
				inst.add(new Institute(rs.getInt("IID"), rs.getString("Name")));
			}
			return inst;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Fuegt ein Institut in die Datenbank ein.
	 * 
	 * @param institute
	 *            das einzufuegende Institut
	 * @return Gibt an, ob die Operation erfolgreich war.
	 */
	public boolean addInstitute(Institute institute) {
		return db.insert(tableName, new Object[] { institute.getIID(),
				institute.getName() });
	}

	/**
	 * Loescht ein Institut aus der Datenbank. Alle Accounts mit diesem Institut werden 
	 * dem Institut default zugewiesen.
	 * 
	 * @param institute
	 *            Das zu loeschende Institut.
	 * @return  Gibt an, ob die Operation erfolgreich war.
	 */
	public boolean deleteInstitute(Institute institute) {
		// Update all accounts:
		Vector<Account>accs = acccon.getAccountsByInstitute(institute.getIID());
		for(Account a : accs){
			a.setInstitute(0);
			acccon.updateAccount(a);
			Mailer.getInstance().sendMail(a.getEmail(), "Lรถschung des Instituts", "Da ihr Institut gelรถscht wurde, wurden sie dem Standard Institut zugeordnet. Bei weiteren Fragen, wenden sie sich bitte an den Admin.");
			
		}
		// Update all offers:
		Vector<Offer>offers = OfferController.getInstance().getOffersByInstitute(institute.getIID());
		for(Offer offer: offers){
			offer.setInstitute(0);
			OfferController.getInstance().updateOffer(offer);
		}
		return db.delete(tableName, "IID=" + institute.getIID());
	}

	public Institute getInstituteByIID(int IID) {
		ResultSet rs = db.select(new String[] { "*" },
				new String[] { tableName }, "IID=" + IID);
		if(rs==null){
			log.write("InstituteController", "No connection: couldn't get institute");
			return null;
		}
		try {
			if (rs.next()) {
				return new Institute(rs.getInt("IID"), rs.getString("Name"));
			}
			return null;
		} catch (SQLException e) {
			// e.printStackTrace();
			return null;
		}
	}

	/**
	 * Gibt alle Institute zurueck die ein Clerk repraesentiert.
	 * 
	 * @param username
	 *            Username des Clerks
	 * @return Alle Institutsnummern in einem Vector.
	 */
	public Vector<Integer> getAllRepresentingInstitutes(String username) {
		Vector<Integer> ret = new Vector<Integer>();
		ResultSet rs = db.select(new String[] { "DISTINCT IID" }, new String[] {
				tableName, "Accounts" },
				"Institute.IID=Accounts.institut AND (Accounts.benutzername='"
						+ username + "' OR Accounts.stellvertreter='"
						+ username + "')");
		if(rs==null){
			log.write("InstituteController", "No connection: couldn't get institutes");
			return ret;
		}
		try {
			while (rs.next()) {
				ret.add(rs.getInt(1));
			}
		} catch (Exception e) {
			Helper.log.write("InstituteController",
					"Error getting representing Institutes");
		}
		return ret;
	}
}
