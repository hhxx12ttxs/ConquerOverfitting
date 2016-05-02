/**
 * @author Tamino Hartmann
 * @author Patryk Boczon 
 * @author Oemer Sahin
 * @author Manuel Guentzel
 * @author Laura Irlinger
 **/
package database.account;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import logger.Log;
import mail.Mailer;
import user.Applicant;
import user.Clerk;
import database.DatabaseController;
import database.application.Application;
import database.application.ApplicationController;
import database.offer.Offer;
import database.offer.OfferController;

/**
 * Verwaltet alle Datenbankzugriffe auf Account-bezogene Daten. Der
 * Accountcontroller behandelt die Verwaltungsmethoden von allen Accounts.
 * 
 */

public class AccountController {

	/**
	 * Klassenattribut "acccontr" beinhaltet eine AccountController-Instanz,
	 * falls keine vorhanden war und mit der Methode getInstance angelegt wird.
	 * Dies dient zur Gewaehrleistung, dass nur eine Instanz von
	 * AccountController existiert.
	 */
	private static AccountController acccontr;
	/**
	 * Instanz des Loggers.
	 */
	private Log log;

	/**
	 * Tabellenname
	 */
	final static String tableName = "Accounts";// tabellenname

	/**
	 * Diese Methode prueft ob ein AccountController-Objekt existiert. Falls
	 * nicht wird eine neue AccountController-Instanz angelegt, zurueckgegeben
	 * und in dem Klassenattribut "acccontr" abgespeichert. Dies dient zur
	 * Gewaehrleistung, dass nur eine Instanz von AccountController existiert.
	 * 
	 * @return Es wird die Instanz zurueckgegeben.
	 */
	public static AccountController getInstance() {
		if (acccontr == null)
			acccontr = new AccountController();
		return acccontr;

	}

	/**
	 * Privater Konstruktor, da die Klasse selbst ein Singleton ist.
	 */
	private AccountController() {
		dbc = DatabaseController.getInstance();
		log = Log.getInstance();
		log.write("AccountController", "Instance created.");
	}

	/**
	 * Diese Instanz dient zum Zugang in die Datenbank.
	 */
	public DatabaseController dbc;

	/**
	 * Diese Methode erstellt einen neuen Account mit uebergebener
	 * Account-Instanz als Parameter.
	 * 
	 * @param account
	 *            Parameter "account" ist ein Account-Objekt, welches alle
	 *            noetigen Attribute enthaelt.
	 * @return Gibt an, ob das erstellen des Accounts erfolgreich war.
	 */
	public boolean createAccount(Account account) { // checked
		Object[] values = { account.getUsername(), account.getPasswordhash(),
				account.getAccounttype(), account.getEmail(),
				account.getName(), account.getInstitute(),
				account.getRepresentative() };
		return dbc.insert(tableName, values);
	}

	/**
	 * Methode zum Loeschen eines ApplicantAccounts.
	 * 
	 * @param applicant
	 *            Applicant-Objekt
	 * @return Gibt an, ob die Operation erfolgreich war.
	 */
	public boolean deleteApplicantAccount(Applicant applicant) {
		String username = applicant.getUserData().getUsername();
		Account acc = getAccountByUsername(username);
		// deleting all applications from username
		Vector<Application> apps = ApplicationController.getInstance()
				.getApplicationsByApplicant(username);
		if (apps != null) {
			for (int i = 0; i < apps.size(); i++) {
				Application a = apps.get(i);
				if(a.isChosen()){
					Offer toincrement = OfferController.getInstance().getOfferById(a.getAid());
					toincrement.setSlots(toincrement.getSlots()+1);
					if(!OfferController.getInstance().updateOffer(toincrement))
						log.write("AccountController", "Could't reincrement slots of offer <"+toincrement.getName()+">");
				}
				ApplicationController.getInstance().deleteApplication(
						apps.elementAt(i));
			}
		}
		return deleteAccount(acc);
	}

	/**
	 * Methode zum Loeschen eines ApplicantAccounts.
	 * 
	 * @param applicant
	 *            Account-Objekt
	 * @return Gibt an, ob die Operation erfolgreich war.
	 */
	public boolean deleteApplicantAccount(Account applicant) {
		String username = applicant.getUsername();
		Account acc = getAccountByUsername(username);
		// deleting all applications from username
		Vector<Application> apps = ApplicationController.getInstance()
				.getApplicationsByApplicant(username);
		if (apps != null) {
			for (int i = 0; i < apps.size(); i++) {
				ApplicationController.getInstance().deleteApplication(
						apps.elementAt(i));
			}
		}
		return deleteAccount(acc);
	}

	/**
	 * Methode zum Loeschen eines ClerkAccounts
	 * 
	 * @param clerk
	 *            Clerk-Objekt
	 * @return Gibt an, ob die Operation erfolgreich war.
	 */

	public boolean deleteClerkAccount(Clerk clerk) {
		String username = clerk.getUserData().getUsername();
		Account acc = getAccountByUsername(username);

		String representative = acc.getRepresentative();

		// removes the clerk as a reprensentative from all the other clerks
		Object[] values = { null };
		String[] columns = { "stellvertreter" };
		String where = "stellvertreter = '" + username + "'";
		dbc.update(tableName, columns, values, where);

		// sets clerk - of all applications with current clerk as clerk - to
		// null or representative
		Vector<Application> apps = ApplicationController.getInstance()
				.getApprovedApplicationsByClerk(username);
		if (apps != null) {
			for (int i = 0; i < apps.size(); i++) {
				Application temp = apps.elementAt(i);
				if (representative == null) {
					temp.setClerk(null);
				} else {
					temp.setClerk(representative);
				}
				ApplicationController.getInstance().updateApplication(temp);
			}
		}
		return deleteAccount(acc);
	}

	/**
	 * Methode zum Loeschen eines ClerkAccounts
	 * 
	 * @param clerk
	 *            Account-Objekt
	 * @return Gibt an, ob die Operation erfolgreich war.
	 */
	public boolean deleteClerkAccount(Account clerk) {
		String username = clerk.getUsername();
		Account acc = getAccountByUsername(username);

		String representative = acc.getRepresentative();

		// removes the clerk as a reprensentative from all the other clerks
		Object[] values = { null };
		String[] columns = { "stellvertreter" };
		String where = "stellvertreter = '" + username + "'";
		dbc.update(tableName, columns, values, where);

		// sets clerk - of all applications with current clerk as clerk - to
		// null or representative
		Vector<Application> apps = ApplicationController.getInstance()
				.getApprovedApplicationsByClerk(username);
		if (apps != null) {
			for (int i = 0; i < apps.size(); i++) {
				Application temp = apps.elementAt(i);
				if (representative == null) {
					temp.setClerk(null);
				} else {
					temp.setClerk(representative);
				}
				ApplicationController.getInstance().updateApplication(temp);
			}
		}
		return deleteAccount(acc);
	}

	/**
	 * Methode zum Loeschen eines ProviderAccounts
	 * 
	 * @param provider
	 *            Provider-Objekt
	 * @return Gibt an, ob die Operation erfolgreich war.
	 */
	public boolean deleteProviderAccount(Account provider) {
		String username = provider.getUsername();

		// removes the provider as a reprensentative from all the other
		// providers
		Object[] values = { null };
		String[] columns = { "stellvertreter" };
		String where = "stellvertreter = '" + username + "'";
		dbc.update(tableName, columns, values, where);
		Vector<Offer> off = OfferController.getInstance().getOffersByProvider(
				username);
		String representative = provider.getRepresentative();
		if (representative == null) {
			for (int i = 0; i < off.size(); i++) {
				Offer temp = off.elementAt(i);
				OfferController.getInstance().deleteOffer(temp);
			}
		} else {
			for (int i = 0; i < off.size(); i++) {
				Offer temp = off.elementAt(i);
				temp.setAuthor(representative);
				OfferController.getInstance().updateOffer(temp);
			}
		}
		return deleteAccount(getAccountByUsername(username));
	}

	/**
	 * Diese Methode Loescht den gewuenschten Account mit uebergebenem
	 * Account-Objekt.
	 * 
	 * @param account
	 *            Parameter "account" ist ein Account-Objekt, welches alle
	 *            noetigen Attribute enthaelt.
	 * @return Gibt an, ob die Operation erfolgreich war.
	 */
	public boolean deleteAccount(Account account) { // checked
		String where = "benutzername = '" + account.getUsername() + "'";
		boolean ret = dbc.delete("Accounts", where);
		if(ret)Mailer.getInstance().sendMail(account.getEmail(), "Lรถschung ihres Accounts", "Hiermit teilen wir Ihnen mit, dass ihr Account gelรถscht wurde.");
		return ret;
	}

	/**
	 * Diese Methode nimmt Aenderungen an einem Account-Objekt vor. Dabei werden
	 * die geaenderten Attribute vom uebergebenem Parameter uebernommen.
	 * 
	 * @param account
	 *            Parameter "account" ist ein Account-Objekt, welches alle
	 *            noetigen Attribute enthaelt.
	 * @return Gibt an, ob die Operation erfolgreich war.
	 */
	public boolean updateAccount(Account account) { // checked
		String where = "benutzername = '" + account.getUsername() + "'";
		Object[] values = { account.getPasswordhash(),
				account.getAccounttype(), account.getEmail(),
				account.getName(), account.getInstitute(),
				account.getRepresentative() };
		String[] columns = { "passworthash", "accounttyp", "email", "name",
				"institut", "stellvertreter" };
		return dbc.update(tableName, columns, values, where);
	}

	/**
	 * Gibt die Anzahl der registrierten Accounts zurueck.
	 * 
	 * @return Anzahl der Accounts
	 */
	public int accountCount() {
		return dbc.count(new String[] { tableName }, null);
	}

	/**
	 * Diese Methode gibt alle in der Datenbank gespeicherten Accounts zurueck.
	 * 
	 * @return Account-Objekt das diesem Username entspricht. Falls nicht
	 *         existent <code>null</code>.
	 */
	public Vector<Account> getAllAccounts() { // checked
		ResultSet rs = dbc.select(new String[] { "*" },
				new String[] { tableName }, null);
		Vector<Account> accounts = new Vector<Account>();
		if (rs == null) {
			log.write("AccountController",
					"No Connection: couldn't fetch accounts");
			return accounts;
		}
		try {
			while (rs.next()) {
				accounts.add(new Account(rs.getString(1), rs.getString(2), rs
						.getInt(3), rs.getString(4), rs.getString(5), rs
						.getInt(6), rs.getString(7)));
			}
		} catch (SQLException e) {
			logger.Log.getInstance().write("AccountController",
					"Error while reading all accounts from Database.");
		}
		return accounts;
	}

	/**
	 * Diese Methode gibt den Account mit dem spezifizierten Username zurueck.
	 * Sollte dieser nicht existieren bekommt man einen <code>null</code>
	 * -Verweis zurueck.
	 * 
	 * @param username
	 *            zu suchender Username
	 * @return Account-Objekt das diesem Username entspricht. Falls nicht
	 *         existent <code>null</code>.
	 */
	public Account getAccountByUsername(String username) { // checked
		ResultSet rs = dbc.select(new String[] { "*" },
				new String[] { tableName }, "benutzername='" + username + "'");
		if (rs == null) {
			log.write("AccountController",
					"No connection: couldn't get Account");
			return null;
		}
		try {
			if (rs.next()) {
				return new Account(rs.getString(1), rs.getString(2),
						rs.getInt(3), rs.getString(4), rs.getString(5),
						rs.getInt(6), rs.getString(7));
			} else {
				return null;
			}
		} catch (SQLException e) {
			logger.Log.getInstance().write("AccountController",
					"Error while reading Account from Database");
		}
		return null;
	}

	/**
	 * Diese Methode selektiert alle Accounts mit uebergebenem Institut.
	 * 
	 * @param id
	 *            Id ist der Primaerschluessel in der Institute-DB.
	 * @return Es wird ein Vector zurueckgegeben, welcher alle Account-Objekte
	 *         enthaelt und zwar alle Account-Objekte mit uebergebenem Institut.
	 */
	public Vector<Account> getAccountsByInstitute(int id) { // checked
		Vector<Account> accountvec = new Vector<Account>(30, 10);

		String[] select = { "*" };
		String[] from = { tableName };
		String where = "institut = " + id;

		ResultSet rs = dbc.select(select, from, where);
		if (rs == null) {
			log.write("AccountController",
					"No connection: couldn't get accounts.");
			return accountvec;
		}
		try {
			while (rs.next()) {
				Account currentacc;
				currentacc = new Account(rs.getString(1), rs.getString(2),
						rs.getInt(3), rs.getString(4), rs.getString(5),
						rs.getInt(6), rs.getString(7));

				accountvec.add(currentacc);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return accountvec;
	}

	/**
	 * Gibt die Accounts der Stellvertretenden zurueck. Gegeben wird der EIGENE
	 * Bentuzername!
	 * 
	 * @param userName
	 *            Der Benutzername des Accounts, fuer den wir delegierte
	 *            Stellvertreter suchen.
	 * @return Alle accounts fuer welche die Person Stellvertreter ist.
	 */
	public Vector<Account> getRepresentativeAccounts(String userName) {
		ResultSet rs = dbc.select(new String[] { "*" },
				new String[] { "Accounts" }, "stellvertreter= '" + userName
						+ "'");
		if (rs == null) {
			log.write("AccountController",
					"No connection: couldn't get account");
			return null;
		}
		Vector<Account> accounts = new Vector<Account>();
		try {
			while (rs.next()) {
				accounts.add(new Account(rs.getString("benutzername"), rs
						.getString("passworthash"), rs.getInt("accounttyp"), rs
						.getString("email"), rs.getString("name"), rs
						.getInt("institut"), rs.getString("stellvertreter")));
			}
			return accounts;
		} catch (SQLException e) {
			logger.Log.getInstance().write(
					"AccountController",
					"Error while reading sccounts from Database for <"
							+ userName + ">.");
		}
		return null;
	}

	/**
	 * Gibt die potentiellen Stellvertreter eines Benutzers zurueck.
	 * 
	 * @param username
	 *            Nutzer mit username wird nicht im Resultat enthalten sein.
	 * @return Vector, der alle usernames beinhaltet, ausser dem uebergebenen.
	 */
	public Vector<String> getPotentialRepresentatives(String username) {
		int accounttype = 3;
		ResultSet rstype = dbc
				.select(new String[] { "accounttyp" },
						new String[] { tableName }, "benutzername = '"
								+ username + "'");
		if (rstype == null) {
			log.write("AccountController",
					"No connection: couldn't get potential representatives");
			return new Vector<String>();
		}
		try {
			if (rstype.next()) {
				accounttype = rstype.getInt("accounttyp");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Vector<String> representatives = new Vector<String>();

		ResultSet rs = dbc.select(new String[] { "benutzername" },
				new String[] { tableName }, "accounttyp = " + accounttype
						+ " AND benutzername != '" + username + "'");
		if (rs == null) {
			log.write("AccountController",
					"No connection: couldn't get potential representatives");
			return representatives;
		}
		try {
			while (rs.next()) {
				representatives.add(rs.getString("benutzername"));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return representatives;
	}

	/**
	 * Methode zum holen eines Accounts anhand der Emailaddresse. Wird verwendet
	 * um die "Passwort vergessen" Funktion zu implementieren. Gibt es mehre
	 * Accounts mit der gleichen Emailaddresse wird ein leerer Account
	 * zurueckgegeben.
	 * 
	 * @param email
	 *            Die Emailaddresse anhand welcher der Account ausgewaehlt wird.
	 * @return Bei einem Account wird dieser zurueckgegeben, bei mehreren ein
	 *         leerer Account, bei keinen <code>Null</code>.
	 */
	public Account getAccountByEmail(String email) {
		ResultSet rs = dbc.select(new String[] { "*" },
				new String[] { tableName }, "email LIKE '" + email + "'");
		if (rs == null) {
			log.write("AccountController", "No connection: couldn't get email!");
			return null;
		}
		try {
			if (rs.next()) {
				Account acc = new Account(rs.getString("benutzername"),
						rs.getString("passworthash"), rs.getInt("accounttyp"),
						rs.getString("email"), rs.getString("name"),
						rs.getInt("institut"), rs.getString("stellvertreter"));
				if (rs.next()) {
					// This is the case where more than one account has the same
					// email address:
					logger.Log.getInstance().write(
							"AccountController",
							"Resetting password failed because of multiple accounts! Email: <"
									+ email + ">");
					return new Account("", "", 0, "", "", 0, "");
				} else
					return acc;
			} else
				// This can happen when no account exists and is okay:
				return null;
		} catch (SQLException e) {
			return null;
		}
	}
}

