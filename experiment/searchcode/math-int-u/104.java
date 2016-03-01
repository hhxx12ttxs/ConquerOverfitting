package gui;

import java.io.IOException;
import java.net.UnknownHostException;
import javax.swing.JOptionPane;
import klient.Client;
import obsluga.*;
import pomoce.Pomoc;
import stale.*;

import java.util.*;

public class Events {
	private static volatile Events INSTANCE;
	private Client k = new Client();
	private Parishioner p = new Parishioner();
	private Priest priest;
	private User u = new User();
	private String adr;
	private int portt;
	private boolean logged = false;
	private CardLayoutExp adminForm;
	private String lastErr = "";
	private String lastErrData = "";

	/**
	 * CONSTRUCTOR - prywatny do SINGLETON
	 */

	private Events() {
		try {
			adr = Pomoc.loadFromFile("client.ini", "SERWERADRES");
		} catch (IOException e) {
			e.printStackTrace();
		} // pobranie adresu
		try {
			portt = Integer.parseInt(Pomoc.loadFromFile("client.ini", "PORT"));
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} // pobranie portu

		try {
			k.setIsConnected(k.connect(adr, portt));

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (k.getIsConnected() == false)
			System.out.println("Klient - niepolaczony");
		else
			System.out.println("Klient - polaczony");
		// if(!k.getIsConnected()){ JOptionPane.showMessageDialog(null,
		// "Blad polaczenia z serverem", "Error connect",
		// JOptionPane.ERROR_MESSAGE); }
	}

	// drugi konstruktor
	private Events(CardLayoutExp val) {
		this();
		adminForm = val;
	}

	public User getUser() {
		return u;
	}

	/**
	 * @return <b>Events</b> - SINGLETON<br />
	 *         Zwraca instancje siebie lub tworzy nawe jezeli nie istnieje
	 */
	public static Events getInstance(CardLayoutExp val) {
		if (INSTANCE == null)
			synchronized (Events.class) {
				if (INSTANCE == null)
					INSTANCE = new Events(val);
			}
		return INSTANCE;
	}

	public static Events getInstance() {
		if (INSTANCE == null)
			synchronized (Events.class) {
				if (INSTANCE == null)
					INSTANCE = new Events();
			}
		return INSTANCE;
	}

	private void connectionError() {
		if (adminForm != null) {
			adminForm.connectionError();
		}
	}

	/**
	 * @return <b>NewsList</b> - liste Aktualnosci
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	// ############################## WYSWIETLANIE AKTULNOSCI
	// ################################3
	LinkedList<Actuals> listAct;

	public LinkedList<Actuals> getActualsList() {
		return listAct;
	}

	public NewsList getNewsList() throws ClassNotFoundException, IOException {
		NewsList newsList = new NewsList();
		Actuals act = new Actuals();
		// newsList.generateNewsList(5); // pobranie przyk³adowej listy
		// aktualnoœci

		// Starsznie przekombionowane tutaj jest poniewaz nie wiem w ktorym
		// momencie Malysz laczy sie z serwerem
		// wiec polaczysem sie tutaj
		int i = 0;
		act.setKindQuery(KindQuery.SEL_DBASE);
		act.setQuery("Select * from actuals order by add_date desc");
		if(!k.sendObject(act)){
			this.connectionError();
			
		}
		k.reciveObject();

		LinkedList<Actuals> actL = new LinkedList<Actuals>();
		actL = (LinkedList<Actuals>) k.getPackage();
		listAct = actL;
		if (!actL.getFirst().getQuery().equals("ERR")) {
			Iterator<Actuals> itA = actL.iterator();
			while (itA.hasNext()) {
				i++;
				Actuals ae = itA.next();
				System.out.println(ae.getSubject());
				System.out.println(ae.getDescribe());
				System.out.println(ae.getAddDate());
				System.out.println(ae.getPriestPesel());
				System.out.println(ae.getName());
				System.out.println(ae.getSurName());
				newsList.addNews(new News(ae.getId(), ae.getSubject(), ae.getAddDate(), ae
						.getName() + " " + ae.getSurName(), 100*(ae.getDescribe().length()/200),ae.getDescribe()));
			}
		}
		return newsList;
	}


	// ####################### DODAWANIE AKTUALNOSCI
	// ##############################
	public boolean dodajAktualnosc(Actuals akt) throws IOException {
		akt.setKindQuery(KindQuery.ADD_DBASE);
		akt.setQuery("INSERT INTO Actuals VALUES (" + "seq_actuals.nextval,"
				+ akt.getPriestPesel() + ",'" + akt.getSubject() + "','"
				+ akt.getDescribe() + "'," + "to_date('"
				+ akt.getAddDate().toLocaleString().substring(0, 16)
				+ "','yyyy-MM-dd HH24:MI'))");
		System.out.println("^^^^^^^^^^^^" + akt.getQuery());
		if (!k.sendObject(akt)) {
			this.connectionError();
			return false;
		}

		try {
			if (!k.reciveObject())
				return false;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		akt = (Actuals) k.getPackage();
		this.setLastErr(akt.getQuery());

		return true;

	}

	// ################ USUWANIE AKTUALNOSCI ###################
	public void usunAktualnosc(Actuals a) throws ClassNotFoundException,
			IOException {
		a.setKindQuery(KindQuery.DEL_DBASE);
		a.setQuery("DELETE FROM actuals where id_actuals=" + a.getId());

		if (!k.sendObject(a)) {
			return;
		}

		k.reciveObject();
		a = (Actuals) k.getPackage();

		this.setLastErr(a.getQuery());
		this.setLastErrData(a.getData());
	}

	/**
	 * @param login
	 *            :String
	 * @param haslo
	 *            :String
	 * @return <b>true</b> - jezeli logowanie sie powiodlo<br />
	 *         <b>false</b> - jezeli logowanie sie nie powiodlo<br />
	 * <br />
	 *         uzupelnia tekrze odpowiednie obiekty parafianina i ksiedza w
	 *         zaleznoci od uprawniej urzytkownika
	 */

	// #################### LOGOWANIE/WYLOGOWANIE ##############################
	public boolean zaloguj(String login, String haslo) {
		boolean bigErr;
		u.setKindQuery(KindQuery.TRY_LOGIN); // zapytanie = logowanie
		u.setLogin(login);
		u.setPassword(haslo);

		u.setQuery("SELECT * FROM userr WHERE login = '" + u.getLogin()
				+ "' AND password = '" + u.getPassword() + "'");

		System.out.println("Zapytanie: \n" + u.getQuery());

		try {
			if (!k.sendObject(u)) {
				this.connectionError();
				// return false;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} // wysyla sie bo wszsytkie obiekty dziedzicza po Object
		try {
			if (!k.reciveObject())
				return false;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		u = (User) k.getPackage();

		if (u.getQuery().equals("ERR")) {
			/* Gdy login/haslo bledne */
			System.out.println("Nie mozna sie zalogowac (zly login/haslo)");
			u.setRestriction(KindRestriction.GUEST_R);
			// p.setRestriction(KindRestriction.GUEST_R);
			return false;
		}

		System.out.println("Prawa dostepu: " + u.getRestriction() + " Ranaga: "
				+ u.getRange());

		try {
			if (!k.reciveObject())
				return false;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("%%%%%%%%%%%%%%%  " + u.getRestriction());

		if (u.getRestriction() == KindRestriction.LOGED_R) {
			p = (Parishioner) k.getPackage();
			System.out.println("Parishioner Zalogowano jako: " + p.getName()
					+ " " + p.getSurName() + "\n" + " Pesel: " + p.getPesel());
		} else if (u.getRestriction() == KindRestriction.WORKS_R
				|| u.getRestriction() == KindRestriction.GOD_R) {
			priest = (Priest) k.getPackage();
			System.out.println("Priest Zalogowano jako: " + priest.getName()
					+ " " + priest.getSurName() + "\n" + " Adres: "
					+ " Pesel: " + priest.getPesel());
		} else {
			System.out.println("Blad logowania");
			return false;
		}

		logged = true;

		return true;
	}

	// ---------------------------------------------------------------------
	/**
	 * @return <b>true</b> - jezeli urzytkownik zostal wylogowany<br />
	 *         <b>false</b> - jezeli wylogowanie sie nie powiodlo<br />
	 * <br />
	 *         ustawia takrze obiekty klasy do stanu poczatkowego wlasciwego dla
	 *         urzytkownika bez uprawniem
	 */

	

	public boolean wyloguj() {
		boolean bigErr;
		int restriction = getRestriction();

		u.setKindQuery(KindQuery.TRY_LOGOUT);
		try {
			if (!k.sendObject(u))
				// return false;
				this.connectionError();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			if (!k.reciveObject())
				return false;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		u = (User) k.getPackage();
		// p = new Parishioner();
		u.setRestriction(KindRestriction.GUEST_R);
		logged = false;
		return true;
	}

	public LinkedList<Person> wyszukajPesel(String pesel) throws IOException,
			ClassNotFoundException {
		// najpierw wyszukaj w ksiedzu

		LinkedList<Person> personList = new LinkedList<Person>();

		Priest pr = new Priest();
		pr.setKindQuery(KindQuery.SEL_DBASE);
		pr.setPesel(pesel);
		pr.setQuery("Select * from priest where pesel=" + pr.getPesel());

		if (!k.sendObject(pr)) {
			this.connectionError();
		}
		k.reciveObject();
		pr = (Priest) k.getPackage();
		if (pr.getQuery().equals("OK+")) {
			personList.add((Person) pr);
			return personList;
		}

		Parishioner par = new Parishioner();
		par.setPesel(pesel);
		par.setKindQuery(KindQuery.SEL_DBASE);
		par.setQuery("Select * from parishioner where pesel=" + par.getPesel());
		System.out.println(par.getQuery());
		if (!k.sendObject(par)) {
			this.connectionError();
		}
		k.reciveObject();
		par = (Parishioner) k.getPackage();
		if (par.getQuery().equals("OK+")) {
			personList.add((Person) par);
			return personList;
		}

		return personList;
	}
	
	public LinkedList<Person> pobierzWszystkich()throws IOException,
	ClassNotFoundException{
		
		LinkedList<Person> personList = new LinkedList<Person>();
		Priest pr = new Priest();
		pr.setKindQuery(KindQuery.SEL_DBASE);
		
		pr.setQuery("Select * from priest");

		if (!k.sendObject(pr)) {
			this.connectionError();
		}
		k.setNullPackage();
		k.reciveObject();
		
		LinkedList<Person> p = (LinkedList<Person>)k.getPackage();
		personList.addAll(p);
		
		Parishioner par = new Parishioner();
		
		par.setKindQuery(KindQuery.SEL_DBASE);
		par.setQuery("Select * from parishioner");
		System.out.println(par.getQuery());
		if (!k.sendObject(par)) {
			this.connectionError();
		}
		k.setNullPackage();
		k.reciveObject();
		
		LinkedList<Person> pa = (LinkedList<Person>)k.getPackage();
		Iterator<Person> it = pa.iterator();
		while(it.hasNext()){
			Iterator<Person> pl = personList.iterator();
			Person para = it.next();
			boolean is=false;	
			while(pl.hasNext()){
			
				if(pl.next().getPesel().equals(para.getPesel())){
					is=true;
				}
			}
			if(!is) personList.add(para);
		}
		
	return personList;
	}

	// ####################### OPERACJE NA DANYCH
	// ################################
	public void pobierzDane() throws IOException, ClassNotFoundException {
		boolean bigErr;
		int restriction = getRestriction();
		if (restriction == KindRestriction.LOGED_R) {
			p.setKindQuery(KindQuery.SEL_DBASE);
			p.setQuery("Select * from parishioner where pesel=" + p.getPesel());

			if (!k.sendObject(p)) {
				this.connectionError();
			}

			k.reciveObject();
			p = (Parishioner) k.getPackage();

			// System.out.println("ZAMIESZKALY: "+p.getAdress().getCity()+"    "+p.getAdress().getPostcode());
			// System.out.println("URODZONY : "+p.getCourse().getBirthDay().toLocaleString());
		}

		if (restriction >= KindRestriction.WORKS_R) {
			priest.setKindQuery(KindQuery.SEL_DBASE);
			priest.setQuery("Select * from priest where pesel="
					+ priest.getPesel());
			/*JOptionPane.showMessageDialog(null,
					"Select * from priest where pesel=\"" + priest.getPesel()
							+ "\"");*/
			if (!k.sendObject(priest)) {
				this.connectionError();
			}
			k.reciveObject();
			priest = (Priest) k.getPackage();
		}
	}

	public void pobierzListeKsiezy() throws IOException, ClassNotFoundException {
		LinkedList<Priest> lp = new LinkedList<Priest>();
		Priest np = new Priest();
		np.setKindQuery(KindQuery.SEL_DBASE);
		np.setQuery("Select * from Priest");

		// System.out.println("TEST TEST TEST "+np.getQuery().contains("where"));
		if(!k.sendObject(np)){
			this.connectionError();
			return;
		}
		k.reciveObject();
		lp = (LinkedList<Priest>) k.getPackage();
		k.setPriestList(lp);
	}

	/**
	 * @param editedParishioner
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * <br />
	 * <br />
	 *             aktualizuje dane podanego uzytkownika w bazie danych
	 */
	public void edytujUzytkownika(Parishioner editedParishioner)
			throws IOException, ClassNotFoundException {

	}

	/**
	 * @param newU
	 *            :User - dane logowania urzytkownika
	 * @param newP
	 *            :Parishioner - dane nowego urzytkownika
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * <br />
	 * <br />
	 *             dodaje nowego urzytkownika<br />
	 *             dodalem nowa funkcje poniewarz bez sensy jest wrzucanie w
	 *             parametry obiekty Adress i Course poniewarz sa w klasie
	 *             Parishioner
	 */

	public void dodajUzytkownika(User newU, Adress newA, Priest newP)
			throws IOException, ClassNotFoundException {

		// User newU = new User();
		// newU.setLogin("xxxx");
		// newU.setPassword("P");
		// newU.setRestriction(KindRestriction.WORKS_R);
		// newU.setRange(KindRange.PRIEST_RANG);

		newU.setKindQuery(KindQuery.ADD_DBASE);
		newU.setQuery("INSERT INTO Userr VALUES (" + "seq_userr.nextval,'"
				+ newU.getLogin() + "','" + newU.getPassword() + "',"
				+ newU.getRestriction() + "," + newU.getRange() + ")");
		System.out.println("^^^^^^^^^^^^" + newU.getQuery());
		if (!k.sendObject(newU)) {
			this.connectionError();
		}
		k.reciveObject();
		newU = (User) k.getPackage();
		System.out.println("Wynik dodania uzytkownika  " + newU.getQuery());
		this.setLastErrData(newU.getData());
		this.setLastErr(newU.getQuery());

		// Adress newA = new Adress();
		// newA.setCity("Kielce");
		// newA.setHouseNumb("12A");
		// newA.setPostcode("14-111");
		// newA.setStreet("Wieczorna");

		newA.setKindQuery(KindQuery.ADD_DBASE);
		newA.setQuery("INSERT INTO Adress VALUES (" + "seq_adress.nextval,'"
				+ newA.getCity() + "','" + newA.getStreet() + "','"
				+ newA.getHouse() + "','" + newA.getPostcode() + "')");
		if (!k.sendObject(newA)) {
			this.connectionError();
		}
		k.reciveObject();
		newA = (Adress) k.getPackage();
		System.out.println("Wynik dodania adresu  " + newA.getQuery() + " "
				+ newA.getId());

		/*
		 * try{ System.out.println("!!!!!!!!!!!!Oczekuje"); Thread.sleep(0);
		 * }catch(InterruptedException e){
		 * 
		 * }
		 */

		if (newU.getRestriction() > KindRestriction.LOGED_R) {
			// Priest newP = new Priest();
			// newP.setPesel("900122");
			// newP.setName("AdamKSIADZ");
			// newP.setSurName("Milk");
			// newP.setPossition("Ksiadz");
			// newP.setSecularityDate(Pomoc.podajDate("1990-12-20"));
			// newP.setArrivalDate(Pomoc.podajDate("2002-08-10"));

			newP.setKindQuery(KindQuery.ADD_DBASE);
			newP.setQuery("INSERT INTO Priest VALUES ("
					+ newP.getPesel()
					+ ","
					+ newU.getId()
					+ ","
					+ newA.getId()
					+ ",'"
					+ newP.getName()
					+ "','"
					+ newP.getSurName()
					+ "','"
					+ newP.getPosition()
					+ "',"
					+ "to_date('"
					+ newP.getArrivalDate().toLocaleString().substring(0, 10)
					+ "','yyyy-MM-dd'),"
					+ "to_date('"
					+ newP.getSecularityDate().toLocaleString()
							.substring(0, 10) + "','yyyy-MM-dd')" + ")");
			System.out.println(newP.getQuery());

			if (!k.sendObject(newP)) {
				this.connectionError();
			}
			k.setNullPackage();
			k.reciveObject();

			newP = (Priest) k.getPackage();
			System.out.println("Wynik dodania ksiedza" + newP.getQuery());

			Parishioner newPa = new Parishioner();
			newPa.setKindQuery(KindQuery.ADD_DBASE);
			newPa.setQuery("INSERT INTO Parishioner VALUES (" + newP.getPesel()
					+ ",1" + "," + newU.getId() + "," + newA.getId() + ",'"
					+ newP.getName() + "','" + newP.getSurName() + "'" + ")");
			System.out.println(newPa.getQuery());

			if (!k.sendObject(newPa)) {
				this.connectionError();
			}
			k.setNullPackage();
			k.reciveObject();

			newPa = (Parishioner) k.getPackage();
			System.out.println("Wynik dodania parafianina" + newPa.getQuery());
			this.setLastErrData(newPa.getData());
			this.setLastErr(newPa.getQuery());

			this.setLastErrData(newP.getData());
			this.setLastErr(newP.getQuery());

		}
	}

	public void dodajUzytkownika(User newU, Parishioner newP)
			throws IOException, ClassNotFoundException {
		dodajUzytkownika(newU, newP.getAdress(), newP.getCourse(), newP);
	}

	// ####################################DODAWANIE
	// PARAFIANINA#############################
	public void dodajUzytkownika(User newU, Adress newA, Course newC,
			Parishioner newP) throws IOException, ClassNotFoundException {
		boolean bigErr;
		/*
		 * Jako arametry chyba najlepeij przekazac bedzie gotowe obiekty bo
		 * inaczej to parametrow w ciul
		 */
		/*
		 * User newU = new User(); newU.setLogin("Iipii");
		 * newU.setPassword("P"); newU.setRestriction(KindRestriction.LOGED_R);
		 * newU.setRange(KindRange.LOGG_RANG);
		 */
		newU.setKindQuery(KindQuery.ADD_DBASE);
		newU.setQuery("INSERT INTO Userr VALUES (" + "seq_userr.nextval,'"
				+ newU.getLogin() + "','" + newU.getPassword() + "',"
				+ newU.getRestriction() + "," + newU.getRange() + ")");
		System.out.println("^^^^^^^^^^^^" + newU.getQuery());
		if (!k.sendObject(newU)) {
			this.connectionError();
		}
		k.reciveObject();
		newU = (User) k.getPackage();
		System.out.println("Wynik dodania uzytkownika  " + newU.getQuery());
		this.setLastErrData(newU.getData());
		this.setLastErr(newU.getQuery());

		/*
		 * Adress newA = new Adress(); newA.setCity("Kielce");
		 * newA.setHouseNumb("12A"); newA.setPostcode("14-111");
		 * newA.setStreet("Wieczorna");
		 */
		newA.setKindQuery(KindQuery.ADD_DBASE);
		newA.setQuery("INSERT INTO Adress VALUES (" + "seq_adress.nextval,'"
				+ newA.getCity() + "','" + newA.getStreet() + "','"
				+ newA.getHouse() + "','" + newA.getPostcode() + "')");
		if (!k.sendObject(newA)) {
			this.connectionError();
		}
		k.reciveObject();
		newA = (Adress) k.getPackage();
		System.out.println("Wynik dodania adresu  " + newA.getQuery() + " "
				+ newA.getId());

		/*
		 * Course newC = new Course();
		 * newC.setBirthday(Pomoc.podajDate("1990-12-20"));
		 * newC.setBaptism(Pomoc.podajDate("1991-01-11"));
		 */
		newC.setKindQuery(KindQuery.ADD_DBASE);
		// UWAGA zastanawiamsie nad sposobem sprwdzenia nullow w datach

		String query;

		newC.setQuery("INSERT INTO Course VALUES (" + "seq_course.nextval,"
				+ "to_date('"
				+ newC.getBirthDay().toLocaleString().substring(0, 10)
				+ "','yyyy-MM-dd')" + ",to_date('"
				+ newC.getBaptism().toLocaleString().substring(0, 10)
				+ "','yyyy-MM-dd')," + "null,null,null,null)");

		query = ("INSERT INTO Course VALUES (" + "seq_course.nextval,"
				+ "to_date('"
				+ newC.getBirthDay().toLocaleString().substring(0, 10)
				+ "','yyyy-MM-dd')" + ",to_date('"
				+ newC.getBaptism().toLocaleString().substring(0, 10) + "','yyyy-MM-dd')");

		if (newC.getCommunion() == null) {
			query = query + ",to_date(null)";
		} else {
			query = query + ",to_date('"
					+ newC.getCommunion().toLocaleString().substring(0, 10)
					+ "','yyyy-MM-dd')";
		}

		if (newC.getConfirmation() == null) {
			query = query + ",to_date(null)";
		} else {
			query = query + ",to_date('"
					+ newC.getConfirmation().toLocaleString().substring(0, 10)
					+ "','yyyy-MM-dd')";
		}

		if (newC.getMarriage() == null) {
			query = query + ",to_date(null)";
		} else {
			query = query + ",to_date('"
					+ newC.getMarriage().toLocaleString().substring(0, 10)
					+ "','yyyy-MM-dd')";
		}

		if (newC.getDeath() == null) {
			query = query + ",to_date(null)";
		} else {
			query = query + ",to_date('"
					+ newC.getDeath().toLocaleString().substring(0, 10)
					+ "','yyyy-MM-dd')";
		}

		query = query + ")";
		System.out.println("moje zapytanie \n" + query);

		newC.setQuery(query);
		System.out.println(newC.getQuery());

		if (!k.sendObject(newC)) {
			this.connectionError();
		}
		k.setNullPackage();
		k.reciveObject();
		newC = (Course) k.getPackage();
		System.out.println("Wynik dodania przebiegu  " + newC.getQuery() + " "
				+ newC.getId());

		if (newU.getRestriction() == KindRestriction.LOGED_R) {
			/*
			 * Parishioner newP = new Parishioner(); newP.setPesel("900");
			 * newP.setName("Adam"); newP.setSurName("Milk");
			 */
			newP.setKindQuery(KindQuery.ADD_DBASE);
			newP.setQuery("INSERT INTO Parishioner VALUES (" + newP.getPesel()
					+ "," + newC.getId() + "," + newU.getId() + ","
					+ newA.getId() + ",'" + newP.getName() + "','"
					+ newP.getSurName() + "'" + ")");
			System.out.println(newP.getQuery());

			if (!k.sendObject(newP)) {
				this.connectionError();
			}
			k.setNullPackage();
			k.reciveObject();

			newP = (Parishioner) k.getPackage();
			System.out.println("Wynik dodania parafianina" + newP.getQuery());
			this.setLastErrData(newP.getData());
			this.setLastErr(newP.getQuery());
		}
	}

	public void updateUzytkownik(String login, String pass) throws IOException,
			ClassNotFoundException {

		u.setKindQuery(KindQuery.UPD_DBASE);
		String newPass = pass;
		String newLogin = login;
		u.setData("");
		
		if(login==null) u.setQuery("UPDATE Userr SET " + "password='"
				+ newPass + "' WHERE id_userr=" + u.getId()); 
		
		else{
		u.setQuery("UPDATE Userr SET " + "login='" + newLogin + "',password='"
				+ newPass + "' WHERE id_userr=" + u.getId());
		u.setData(login);
		}
		
		System.out.println("events   --  "+u.getQuery());

		k.setNullPackage();
		if (!k.sendObject(u)) {
			this.connectionError();
		}
		k.reciveObject();
		u = (User) k.getPackage();

		if (u.getQuery().equals("OK+")) {
			this.setLastErr(u.getQuery());
			this.setLastErrData("Aktualizacja zatwierdzona");
			System.out.println("Update uzytkownika poprawny");
		} else {
			this.setLastErr(u.getQuery());
			this.setLastErrData(u.getData());
			System.out.println("Blad updatu uzytkownika");
		}
	}

	public void updatePriest(Priest p) throws IOException,
			ClassNotFoundException {
		boolean cut = true;

		p.setKindQuery(KindQuery.UPD_DBASE);
		String query = "";
		query = "UPDATE Priest SET ";
		if (p.getName() != null)
			query = query + "name='" + p.getName() + "', ";
		if (p.getSurName() != null)
			query = query + "surname='" + p.getSurName() + "', ";
		else {
			query = query.substring(0, query.length() - 2);
			cut = false;
		}
		if (p.getPosition() != null) {
			query = query + " position='" + p.getPosition() + "', ";
			cut = true;
		} else {
			if (cut)
				query = query.substring(0, query.length() - 2);
			cut = false;
		}
		if (p.getArrivalDate() != null) {
			query = query + "beginWork=to_date('"
					+ p.getArrivalDate().toLocaleString().substring(0, 10)
					+ "','yyyy-MM-dd'), ";
			cut = true;
		} else {
			if (cut)
				query = query.substring(0, query.length() - 2);
			cut = false;
		}
		if (p.getSecularityDate() != null) {
			query = query + "holyOrders=to_date('"
					+ p.getSecularityDate().toLocaleString().substring(0, 10)
					+ "','yyyy-MM-dd') ";

		}

		query = query + " WHERE pesel=" + p.getPesel();
		p.setQuery(query);
		System.out.println(p.getQuery());

		if (!k.sendObject(p)) {
			this.connectionError();
		}
		k.reciveObject();
		p = (Priest) k.getPackage();

		if (p.getQuery().equals("OK+")) {
			this.setLastErr(p.getQuery());
			this.setLastErrData("Akyualizacja zatwierdzona");
			System.out.println("Update uzytkownika poprawny");
		} else {
			this.setLastErr(p.getQuery());
			this.setLastErrData("Blad aktualizacji");
			System.out.println("Blad updatu uzytkownika");
		}

	}

	public void updateParishioner(Parishioner p) throws IOException,
			ClassNotFoundException {

		p.setKindQuery(KindQuery.UPD_DBASE);
		String query = "";
		query = "UPDATE Parishioner SET ";
		if (p.getName() != null)
			query = query + "name='" + p.getName() + "', ";
		if (p.getSurName() != null)
			query = query + "surname='" + p.getSurName() + "' ";
		else {
			query = query.substring(0, query.length() - 2);
		}
		query = query + " where pesel=" + p.getPesel();
		p.setQuery(query);
		System.out.println(p.getQuery());

		if (!k.sendObject(p)) {
			this.connectionError();
		}
		k.reciveObject();
		p = (Parishioner) k.getPackage();

		if (p.getQuery().equals("OK+")) {
			this.setLastErr(p.getQuery());
			this.setLastErrData("Akyualizacja zatwierdzona");
			System.out.println("Update uzytkownika poprawny");
		} else {
			this.setLastErr(p.getQuery());
			this.setLastErrData("Blad aktualizacji");
			System.out.println("Blad updatu uzytkownika");
		}

	}

	public void updateCourse(Course c) throws IOException,
			ClassNotFoundException {

		c.setKindQuery(KindQuery.UPD_DBASE);

		String query = "";

		query = "UPDATE Course SET ";

		if (c.getBirthDay() != null)
			query = query + "birthday = to_date('"
					+ c.getBirthDay().toLocaleString().substring(0, 10)
					+ "','yyyy-MM-dd')"; else {
						JOptionPane.showMessageDialog(null, "Data urodzin musi być wypełniona");
						return;
					}

		if (c.getBaptism() != null)
			query = query + " ,baptism = to_date('"
					+ c.getBaptism().toLocaleString().substring(0, 10)
					+ "','yyyy-MM-dd')";

		if (c.getCommunion() != null)
			query = query + " ,communion = to_date('"
					+ c.getCommunion().toLocaleString().substring(0, 10)
					+ "','yyyy-MM-dd')";

		if (c.getConfirmation() != null)
			query = query + " ,confirmation = to_date('"
					+ c.getConfirmation().toLocaleString().substring(0, 10)
					+ "','yyyy-MM-dd')";

		if (c.getMarriage() != null)
			query = query + " ,marriage = to_date('"
					+ c.getMarriage().toLocaleString().substring(0, 10)
					+ "','yyyy-MM-dd')";
		if (c.getDeath() != null)
			query = query + " ,death = to_date('"
					+ c.getDeath().toLocaleString().substring(0, 10)
					+ "','yyyy-MM-dd')";

		query = query + " WHERE id_course=" + c.getId();

		c.setQuery(query);

		System.out.println(c.getQuery());

		k.setNullPackage();

		if (!k.sendObject(c)) {
			this.connectionError();
		}

		k.reciveObject();
		c = (Course) k.getPackage();

		if (c.getQuery().equals("OK+")) {
			this.setLastErrData("Update dat przebiegl z powodzeniem");
			//System.out.println("Update hasla ok");
		} else {
			this.setLastErr(c.getQuery());
			this.setLastErrData("Blad aktualizacji dat");
			//System.out.println("Blad updatu hasla");
		}

	}

	public void updateAdress(Adress a) throws IOException,
			ClassNotFoundException {

		a.setKindQuery(KindQuery.UPD_DBASE);

		String query = "";

		query = "UPDATE Adress SET ";

		if (a.getCity() != null)
			query = query + "city='" + a.getCity();
		if (a.getStreet() != null)
			query = query + "',street='" + a.getStreet();
		if (a.getHouse() != null)
			query = query + "',house_numb='" + a.getHouse();
		if (a.getPostcode() != null)
			query = query + "',postcode='" + a.getPostcode();
		query = query + "' WHERE id_adress=" + a.getId();

		a.setQuery(query);

		System.out.println(a.getQuery());

		k.setNullPackage();

		if (!k.sendObject(a)) {
			this.connectionError();
		}

		k.reciveObject();
		a = (Adress) k.getPackage();

		if (a.getQuery().equals("OK+")) {
			this.setLastErrData("Update adresu przebiegl z powodzeniem");
			System.out.println("Update hasla ok");
		} else {
			this.setLastErr(a.getQuery());
			this.setLastErrData("Blad aktualizacji adresu");
			System.out.println("Blad updatu hasla");
		}
	}

	// ############################ OPERACJE NA ZDARZENIACH
	// #######################
	public void pobierzZdarzenia() throws IOException, ClassNotFoundException {
		Event e = new Event();
		e.setKindQuery(KindQuery.SEL_DBASE);
		e.setQuery("Select * from event");

		if (!k.sendObject(e)) {
			this.connectionError();
		}
		k.reciveObject();
		LinkedList<Event> le = new LinkedList<Event>();
		le = (LinkedList<Event>) k.getPackage();
		k.setEventKindList(le);
	}

	// ############################## OPERACJE NA ZAMOWIENIACH
	// ####################
	public LinkedList<Order> pobierzZamowieniaParafianina() throws IOException,
			ClassNotFoundException {
		boolean bigErr;
		Order o = new Order();
		o.setKindQuery(KindQuery.SEL_DBASE);
		// *przykladowe zapytanie(POBIERA WSZYSTKIE ZAMOWIENIA ZLOZONE PRZEZ
		// PARAFIANINA)
		o.setQuery("Select id_orderr,id_event,"
				+ "odprawiajacy_pesel,zamawiajacy_pesel," + "describe,status ,"
				+ "to_char(beginD,'yyyy-MM-dd HH24:MI'),"
				+ "to_char(endD, 'yyyy-MM-dd HH24:MI') "
				+ "from orderr where zamawiajacy_pesel=" + p.getPesel());
		System.out.println(o.getQuery());
		if (!k.sendObject(o)) {
			this.connectionError();
		}
		k.reciveObject();

		LinkedList<Order> orderList = new LinkedList<Order>();

		orderList = (LinkedList<Order>) k.getPackage();

		/*
		 * Przyklad przegladania, jakbys chcal ladowac dozmiennej to zrob tak
		 * jak zrobilem dla Event (poieranie zdarzen:))
		 * 
		 * Iterator<Order> iterator = orderList.iterator();
		 * 
		 * while(iterator.hasNext()){ Order tmp =iterator.next();
		 * System.out.println
		 * (tmp.getDescribe()+"    "+tmp.getBeginDate().toLocaleString()); }
		 * System.out.println(orderList.size());
		 */
		return orderList;

	}

	// -----------------------------------------------------------------

	public LinkedList<Order> pobierzZamowieniaParafianina(String stat,
			Date dataBeg, Date dataEnd, int idEvent) throws IOException,
			ClassNotFoundException {
		boolean bigErr;
		Order o = new Order();
		o.setKindQuery(KindQuery.SEL_DBASE);
		// *przykladowe zapytanie(POBIERA WSZYSTKIE ZAMOWIENIA ZLOZONE PRZEZ
		// PARAFIANINA)
		String query;

		query = "Select id_orderr,id_event,"
				+ "odprawiajacy_pesel,zamawiajacy_pesel," + "describe,status ,"
				+ "to_char(beginD,'yyyy-MM-dd HH24:MI'),"
				+ "to_char(endD, 'yyyy-MM-dd HH24:MI') "
				+ "from orderr where zamawiajacy_pesel=" + p.getPesel();

		if (dataBeg != null && dataEnd != null) {
			query = query + " and beginD between '"
					+ dataBeg.toLocaleString().substring(0, 10) + "' and '"
					+ dataEnd.toLocaleString().substring(0, 10) + "'";
		}
		if (stat != null) {
			query = query + " and status='" + stat + "'";
		}

		if (idEvent != 0) {
			query = query + " and id_event='" + idEvent + "'";
		}

		o.setQuery(query);
		System.out.println(o.getQuery());
		if (!k.sendObject(o)) {
			this.connectionError();
		}
		k.reciveObject();

		LinkedList<Order> orderList = new LinkedList<Order>();

		orderList = (LinkedList<Order>) k.getPackage();

		return orderList;

	}

	// -----------------------------------------------------------------

	public LinkedList<Order> pobierzZamowieniaKsiedza(int role,
			String execPesel, String stat, Date dataBeg, Date dataEnd,
			int idEvent) throws IOException, ClassNotFoundException {
		boolean bigErr;
		/*
		 * Paramtr role okresla czy ponieramy zamowienia dla ksiedza jako a)
		 * ksiadz jako zamawiajacy (wtedy stawiamy go na rowni ze zwyklym
		 * uzytkownikem (LOGG_RANG)) b) ksiadz jako odprawiajacy
		 */
		Order o = new Order();
		o.setKindQuery(KindQuery.SEL_DBASE);
		// *przykladowe zapytanie(POBIERA WSZYSTKIE ZAMOWIENIA ZLOZONE PRZEZ
		// PARAFIANINA)

		String query;
		if (role == KindRange.LOGG_RANG) {
			query = ("Select id_orderr,id_event,"
					+ "odprawiajacy_pesel,zamawiajacy_pesel,"
					+ "describe,status ,"
					+ "to_char(beginD,'yyyy-MM-dd HH24:MI'),"
					+ "to_char(endD, 'yyyy-MM-dd HH24:MI') "
					+ "from orderr where zamawiajacy_pesel=" + priest
					.getPesel());
		} else {
			query = ("Select id_orderr,id_event,"
					+ "odprawiajacy_pesel,zamawiajacy_pesel,"
					+ "describe,status ,"
					+ "to_char(beginD,'yyyy-MM-dd HH24:MI'),"
					+ "to_char(endD, 'yyyy-MM-dd HH24:MI') "
					+ "from orderr where odprawiajacy_pesel=" + execPesel);
		}

		if (dataBeg != null && dataEnd != null) {
			query = query + " and beginD between '"
					+ dataBeg.toLocaleString().substring(0, 10) + "' and '"
					+ dataEnd.toLocaleString().substring(0, 10) + "'";
		}
		if (stat != null) {
			query = query + " and status='" + stat + "'";
		}

		if (idEvent != 0) {
			query = query + " and id_event='" + idEvent + "'";
		}

		o.setQuery(query);

		System.out.println(o.getQuery());
		if (!k.sendObject(o)) {
			this.connectionError();
		}
		k.reciveObject();

		LinkedList<Order> orderList = new LinkedList<Order>();

		orderList = (LinkedList<Order>) k.getPackage();

		/*
		 * Przyklad przegladania, jakbys chcal ladowac dozmiennej to zrob tak
		 * jak zrobilem dla Event (poieranie zdarzen:))
		 * 
		 * Iterator<Order> iterator = orderList.iterator();
		 * 
		 * while(iterator.hasNext()){ Order tmp =iterator.next();
		 * System.out.println
		 * (tmp.getDescribe()+"    "+tmp.getBeginDate().toLocaleString()); }
		 * System.out.println(orderList.size());
		 */
		return orderList;

	}

	// ----------------------------------------------------------------
	public LinkedList<Order> pobierzZamowieniaKsiedza(int role)
			throws IOException, ClassNotFoundException {
		boolean bigErr;
		/*
		 * Paramtr role okresla czy ponieramy zamowienia dla ksiedza jako a)
		 * ksiadz jako zamawiajacy (wtedy stawiamy go na rowni ze zwyklym
		 * uzytkownikem (LOGG_RANG)) b) ksiadz jako odprawiajacy
		 */
		Order o = new Order();
		o.setKindQuery(KindQuery.SEL_DBASE);
		// *przykladowe zapytanie(POBIERA WSZYSTKIE ZAMOWIENIA ZLOZONE PRZEZ
		// PARAFIANINA)

		if (role == KindRange.LOGG_RANG) {
			o.setQuery("Select id_orderr,id_event,"
					+ "odprawiajacy_pesel,zamawiajacy_pesel,"
					+ "describe,status ,"
					+ "to_char(beginD,'yyyy-MM-dd HH24:MI'),"
					+ "to_char(endD, 'yyyy-MM-dd HH24:MI') "
					+ "from orderr where zamawiajacy_pesel="
					+ priest.getPesel());
		} else {
			o.setQuery("Select id_orderr,id_event,"
					+ "odprawiajacy_pesel,zamawiajacy_pesel,"
					+ "describe,status ,"
					+ "to_char(beginD,'yyyy-MM-dd HH24:MI'),"
					+ "to_char(endD, 'yyyy-MM-dd HH24:MI') "
					+ "from orderr where odprawiajacy_pesel="
					+ priest.getPesel());
		}

		System.out.println(o.getQuery());
		if (!k.sendObject(o)) {
			this.connectionError();
		}
		k.reciveObject();

		LinkedList<Order> orderList = new LinkedList<Order>();

		orderList = (LinkedList<Order>) k.getPackage();

		/*
		 * Przyklad przegladania, jakbys chcal ladowac dozmiennej to zrob tak
		 * jak zrobilem dla Event (poieranie zdarzen:))
		 * 
		 * Iterator<Order> iterator = orderList.iterator();
		 * 
		 * while(iterator.hasNext()){ Order tmp =iterator.next();
		 * System.out.println
		 * (tmp.getDescribe()+"    "+tmp.getBeginDate().toLocaleString()); }
		 * System.out.println(orderList.size());
		 */
		return orderList;

	}

	// --------------------------------------------------------------------

	public LinkedList<Order> pobierzZamowienia(String q) throws IOException,
			ClassNotFoundException {
		boolean bigErr;
		/* Przykladowe zapytania, wariantow moze byc kupa i troche */

		/*
		 * Wszystkie zamowienia parafianina do usuniecia (TODELETE)
		 * "Select id_orderr,id_event," +
		 * "odprawiajacy_pesel,zamawiajacy_pesel," + "describe,status ,"+
		 * "to_char(beginD,'yyyy-MM-dd HH24:MI')," +
		 * "to_char(endD, 'yyyy-MM-dd HH24:MI') " +
		 * "from orderr where zamawiajacy_pesel="+p.getPesel()+
		 * " AND status='"+KindQuery.TODEL+"'";
		 */

		/*
		 * Wszystkie zamowienia ksiedza (odprawia) do usuniecia (TODELETE)
		 * "Select id_orderr,id_event," +
		 * "odprawiajacy_pesel,zamawiajacy_pesel," + "describe,status ,"+
		 * "to_char(beginD,'yyyy-MM-dd HH24:MI')," +
		 * "to_char(endD, 'yyyy-MM-dd HH24:MI') " +
		 * "from orderr where odprawiajacy_pesel="+priest.getPesel()+
		 * " AND status='"+KindQuery.TODEL+"'";
		 */

		Order o = new Order();
		o.setKindQuery(KindQuery.SEL_DBASE);
		o.setQuery(q);
		System.out.println(o.getQuery());
		if (!k.sendObject(o)) {
			this.connectionError();
		}
		k.reciveObject();

		LinkedList<Order> orderList = new LinkedList<Order>();
		orderList = (LinkedList<Order>) k.getPackage();

		return orderList;

	}

	// --------------------------------------------------------------------

	public Order usunZamowienie(Order o) throws IOException,
			ClassNotFoundException {
		boolean bigErr;

		o.setKindQuery(KindQuery.DEL_DBASE);
		o.setQuery("DELETE FROM Orderr where id_orderr=" + o.getId());

		if (!k.sendObject(o)) {
			this.connectionError();
		}
		k.reciveObject();
		Order check = (Order) k.getPackage();
		this.setLastErr(check.getQuery());
		this.setLastErrData(check.getData());
		System.out.println("Wynik usuwania " + check.getQuery());
		return check;

	}

	public Order akceptujZamowienie(Order o) throws IOException,
			ClassNotFoundException {

		o.setKindQuery(KindQuery.UPD_DBASE);
		o.setQuery("UPDATE Orderr SET status='" + KindQuery.ACK + "' , "
				+ "describe='" + o.getDescribe() + "' where" + " id_orderr="
				+ o.getId());

		if (!k.sendObject(o)) {
			this.connectionError();
		}

		k.reciveObject();
		Order check = (Order) k.getPackage();
		this.setLastErr(check.getQuery());
		this.setLastErrData(check.getData());
		System.out.println("Wynik updatu " + check.getQuery());
		check.setStatus(KindQuery.ACK);
		return check;
	}

	public Order odrzucZamowienie(Order o) throws IOException,
			ClassNotFoundException {

		o.setKindQuery(KindQuery.UPD_DBASE);
		o.setQuery("UPDATE Orderr SET status='" + KindQuery.DEN + "' , "
				+ "describe='" + o.getDescribe() + "' where" + " id_orderr="
				+ o.getId());

		if (!k.sendObject(o)) {
			this.connectionError();
		}

		k.reciveObject();
		Order check = (Order) k.getPackage();
		this.setLastErr(check.getQuery());
		this.setLastErrData(check.getData());
		check.setStatus(KindQuery.DEN);
		System.out.println("Wynik updatu " + check.getQuery());
		return check;
	}

	// ---------------------------------------------------------------------
	public void zlozZamowienie(String prPesel, String date, String idEvent,
			String desc) throws IOException, ClassNotFoundException {
		int restriction = getRestriction();
		Order o = new Order();
		o.setKindQuery(KindQuery.ADD_DBASE); // dodanie do bazy
		if (restriction == KindRestriction.LOGED_R)
			o.setSenderPesel(p.getPesel());
		if (restriction > KindRestriction.LOGED_R)
			o.setSenderPesel(priest.getPesel());

		o.setExecutorPesel(prPesel);
		o.setBeginDate(Pomoc.podajDate(date));
		o.setEndDate(Pomoc.podajDate(date));
		o.setEvent(idEvent);// 3 to msza wg Pawla bazy
		o.setDescribe(desc);
		o.setStatus(KindQuery.NEW);
		String q;

		q = "INSERT INTO Orderr VALUES (seq_orderr.nextval,3,'"
				+ o.getExecutroPesel() + "','" + o.getSenderPesel() + "','"
				+ o.getDescribe() + "','" + o.getStatus() + "'," + "to_date('"
				+ o.getBeginDate().toLocaleString().substring(0, 16)
				+ "','yyyy-MM-dd HH24:MI')," + "to_date('"
				+ o.getEndDate().toLocaleString().substring(0, 10)
				+ "','yyyy-MM-dd HH24:MI'))";
		o.setQuery(q);

		if (!k.sendObject(o)) {
			this.connectionError();
		}
		k.reciveObject();
		o = (Order) k.getPackage();

		System.out.println("KLIENT:  (otrzymana odpowiedz)" + o.getData());
		System.out.println(q);

	}

	public void zlozZamowienie(Order o) throws IOException,
			ClassNotFoundException {
		int restriction = getRestriction();
		// Order o = new Order();
		o.setKindQuery(KindQuery.ADD_DBASE); // dodanie do bazy

		if (restriction == KindRestriction.LOGED_R)
			o.setSenderPesel(p.getPesel());
		if (restriction > KindRestriction.LOGED_R)
			o.setSenderPesel(priest.getPesel());

		// o.setExecutorPesel(prPesel);
		// o.setBeginDate(Pomoc.podajDate(date));
		// o.setEndDate(Pomoc.podajDate(date));
		// o.setEvent(idEvent);// 3 to msza wg Pawla bazy
		// o.setDescribe(desc);
		o.setStatus(KindQuery.NEW);
		String q;

		q = "INSERT INTO Orderr VALUES (seq_orderr.nextval," + o.getEvent()
				+ ",'" + o.getExecutor().getPesel() + "','"
				+ o.getSenderPesel() + "','" + o.getDescribe() + "','"
				+ o.getStatus() + "'," + "to_date('"
				+ o.getBeginDate().toLocaleString().substring(0, 16)
				+ "','yyyy-MM-dd HH24:MI')," + "to_date('"
				+ o.getEndDate().toLocaleString().substring(0, 10)
				+ "','yyyy-MM-dd HH24:MI'))";

		o.setQuery(q);

		if (!k.sendObject(o)) {
			this.connectionError();
		}
		k.reciveObject();
		o = (Order) k.getPackage();

		this.setLastErr(o.getQuery());
		this.setLastErrData(o.getData());

		System.out.println("KLIENT:  (otrzymana odpowiedz)" + o.getData());
		System.out.println(q);

	}

	public Client getClient() {
		return k;
	}

	/**
	 * @return <b>Parishioner</b> - zwraca aktualnego uzytkownika
	 */
	public Parishioner getParishioner() {
		return p;
	}

	/**
	 * @return <b>Priest</b> - zwraca aktualnego ksiedza
	 */
	public Priest getPriest() {
		return priest;
	}

	/**
	 * @return <b>true</b> - jezeli uzytkownik jest zalogowany<br />
	 *         <b>false</b> - jezeli uzytkownik jest zalogowany
	 */
	public boolean getLogged() {
		return logged;
	}

	/**
	 * @return zwraca poziom uprawnien aktualnego uzytkownika<br />
	 *         KindRestriction.GUEST_R <br \>
	 *         KindRestriction.LOGED_R <br \>
	 *         KindRestriction.WORKS_R <br \>
	 *         KindRestriction.GOD_R <br \>
	 */
	public int getRestriction() {
		return u.getRestriction();
	}

	public void setLastErr(String val) {
		lastErr = val;
	}

	public String getLastErr() {
		return lastErr;
	}

	public void setLastErrData(String val) {
		if (val == null)
			lastErrData = "";
		else
			lastErrData = val;
	}

	public String getLastErrData() {
		return lastErrData;
	}

}

