package model;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

/**
 * @author alexander
 * 
 * Class - Stock
 * 
 * Diese Klasse bildet eine Aktie mit den entsprechenden Attributen ab.
 * Zudem befinden sich in dieser Klasse diverse Konstruktoren und Getter/Setter.
 */
public class Stock implements Serializable {
	
	////////////////////////////////////////////////////////////////
	/*
	 * Attribute
	 */
	////////////////////////////////////////////////////////////////
	
	private static StocksModel instance = StocksModel.getInstance();
	private static ArrayList<Stock> list = StocksModel.getInstance().getStocksList();
	
	private static int counter = 0;				// Klassenvariable
	private static HashMap<String, Double> depotValueHistory = new HashMap<String, Double>();
	
	private int id = 0;
	private String wkn;							// WKN/ISIN
	private String name;						// Name der Aktie
	private int quantity = 0;					// Anzahl / Menge der Aktien
	private Boolean sold = false;				// true wenn Aktie verkauft
	
	private String b_date;
	private String s_date;
	
	private double b_rate = 0.0;				// Buying-Rate , Kauf-Preis
	private double s_rate = 0.0;				// Selling-Rate , Verkauf-Preis
	private double cur_rate = 0.0;				// Current-Rate , aktueller Preis
	
	// HashMap zur Speicherung der Preise einer Aktie
	private HashMap<String, Double> stockValueHistory = new HashMap<String, Double>();

	
	////////////////////////////////////////////////////////////////
	/*
	 * Konstruktor
	 */
	////////////////////////////////////////////////////////////////
	
	public Stock() {
		this(null);
	}

	// BuyStock-Konstruktor
	public Stock(String wkn, String name, int quantity, double cur_rate, double b_rate, String b_date) {
		this.id = Stock.counter;
		Stock.counter++;
		
		this.wkn = wkn;
		this.name = name;
		this.quantity = quantity;
		this.cur_rate = cur_rate;
		this.b_date = b_date;
		this.b_rate = b_rate;
		this.sold = false;
		this.s_date = "";
		this.s_rate = 0.0;
	}
	
	public Stock(String wkn, String name, int quantity, double cur_rate, 
			String b_date, double b_rate, boolean sold, String s_date, double s_rate) {
		this.id = Stock.counter;
		Stock.counter++;
		
		this.wkn = wkn;
		this.name = name;
		this.quantity = quantity;
		this.cur_rate = cur_rate;
		this.b_date = b_date;
		this.b_rate = b_rate;
		this.sold = sold;
		this.s_date = s_date;
		this.s_rate = s_rate;
	}
	
	/*
	 * 
	 * @param values Call Stock(values[0],values[1],...,values[2])
	 */
	public Stock(String[] values) {
		this.id = Stock.counter;
		Stock.counter++;

		if(values != null) {
			try {
				this.wkn = values[0];
				this.name = values[1];
				this.quantity = Integer.valueOf(values[2]);
				this.cur_rate = Double.valueOf(values[3]);
				//this.b_date = new Date(values[4]);
				this.b_date = values[4];
				this.b_rate = Double.valueOf(values[5]);
				this.sold = Boolean.valueOf(values[6]);
				//this.s_date = new Date(values[7]);
				this.s_date = values[7];
				this.s_rate = Double.valueOf(values[8]);	
			} catch(IllegalArgumentException e ) {
				e.printStackTrace();
			}
		}
	}

	/*
	 * Setter- and Getter-Methoden
	 */
	public int getCurrentId() {
		if (list.size() == 0)
			return 0;
		else
			return counter-1;
	}
	
	public static int getNextId() {
		if (list.size() == 0)
			return 0;
		else
			return counter;
	}
	
	public static void resetCounter() {
		Stock.counter = 0;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getWkn() {
		return wkn;
	}

	public void setWkn(String wkn) {
		this.wkn = wkn;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public boolean isSold() {
		return sold;
	}

	public void setSold(boolean sold) {
		this.sold = sold;
	}

	public String getBuyDate() {
		return b_date;
	}

	public void setBuyDate(String b_date) {
		this.b_date = b_date;
	}

	public String getSellDate() {
		return s_date;
	}

	public void setSellDate(String s_date) {
		this.s_date = s_date;
	}

	public double getBuyRate() {
		return b_rate;
	}

	public void setBuyRate(double b_rate) {
		this.b_rate = b_rate;
	}

	public double getSellRate() {
		return s_rate;
	}

	public void setSellRate(double s_rate) {
		this.s_rate = s_rate;
	}

	public double getCurRate() {
		return cur_rate;
	}

	public void setCurRate(double cur_rate) {
		this.cur_rate = cur_rate;
	}

	/*
	 * Methode speichert alle Variablen von Stock in ein String ab und gibt diesen zurück
	 * 
	 * Notwending für den CSV-Export bzw. Debug-Ausgabe
	 */
	public String getArrayListToString() {
		String current_line = this.wkn + "," + this.name + "," + 
			this.quantity + "," + this.cur_rate + "," + this.b_date + "," + 
			this.b_rate + "," + this.sold + "," + this.s_date + "," + this.s_rate + "\n";
		
		return current_line;
	}
	
	/*
	 * Methode speichert alle PerformanceHistory-Daten von Aktien
	 * 
	 * Notwending für den CSV-ExportHistory
	 */
	public String getStockHashMapToString(int i) {
		String current_line = "";
		for (String key: this.stockValueHistory.keySet()) {
			current_line += i + "," + key + "," + this.stockValueHistory.get(key) + "\n";
		}
		
		return current_line;
	}
	
	/*
	 * Methode speichert alle PerformanceHistory-Daten von Depotwert
	 * 
	 * Notwending für den CSV-ExportHistory
	 */
	public static String getDepotHashMapToString() {
		String current_line = "";
		for (String key: Stock.depotValueHistory.keySet()) {
			current_line += "DEPOT" + "," + key + "," + Stock.depotValueHistory.get(key) + "\n";
		}
		
		return current_line;
	}
	
	////////////////////////////////////////////////////////////////
	/*
	 * Datumsfunktionen
	 */
	////////////////////////////////////////////////////////////////
		
	/*
	 * Methode zur Erzeugung des aktuellen Datum mit Uhrzeit im Format 
	 * Jahr-Monat-Tag Studen:Minuten
	 * 
	 * Rueckgabewert ist ein String mit dem Datum inkl. Uhrzeit
	 */
	public static String getCurrentDate() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
		Date date = new Date();
		
		return dateFormat.format(date);
	}
	
	/*
	 * Methode zur Erzeugung des aktuellen Datum mit Uhrzeit im Format 
	 * Jahr-Monat-Tag Studen:Minuten
	 * 
	 * Rueckgabewert ist ein String mit dem Datum inkl. Uhrzeit
	 */
	public static String getCurrentDateWithoutTime() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
		Date date = new Date();
		
		return dateFormat.format(date);
	}
	
	////////////////////////////////////////////////////////////////
	/*
	 * Berechnungen
	 */
	////////////////////////////////////////////////////////////////
	
	/*
	 * Methode berechnet die Performance (€ / %) einer übergebenen Aktie
	 * und gibt diese zurück
	 * 
	 * Berechnung: 
	 * 		change_percent = (Change * 100) / Kauf-Preis
	 */
	public static String getPerformance(Stock selectedRow) {
		// Nachstellen der Double-Werte auf zwei begrenzen
		DecimalFormat df = new DecimalFormat("#0.00");
		
		String performance = null;
		
		Double change = selectedRow.getSellRate() - selectedRow.getBuyRate();
		Double change_percent = (change * 100) / selectedRow.getBuyRate();
		
		performance = df.format(change) + " € (" + df.format(change_percent) + " %)";
		
		return performance;		
	}
	
	/*
	 * Berechnung der Change (Stock-Objekt wird übergeben, notwendig für das JTableModel)
	 * 
	 * Berechnung: 
	 * 		Change = Verkaufs-Preis - Kauf-Preis
	 */
	public static double getSellChange(Stock selectedRow) {
		return (selectedRow.getSellRate() - selectedRow.getBuyRate());
	}
	
	////////////////////////////////////////////////////////////////
	/*
	 * HashMaps
	 */
	////////////////////////////////////////////////////////////////
	
	// valueHistory
	
	/*
	 * Einfügen in die Stock HashMap
	 */
	public void insertStockHistory(String date, Double value) {
		this.stockValueHistory.put(date, value);
	}

	/*
	 * Rückgabe der HashMap
	 * 
	 * Benutzung in der view.LineChart.java
	 */
	public HashMap<String, Double> getValueHistory() {
		return this.stockValueHistory;
	}
	
	/*
	 * Debug-Output
	 */
	public void printValueHistoryContent() {
		for (String date: this.stockValueHistory.keySet())
			System.out.println(date + ": "+ this.stockValueHistory.get(date));
	}
	
	// depotValueHistory
	
	/*
	 * Einfügen in die HashMap
	 */
	public static void insertDepotHistory(String date, Double value) {
		Stock.depotValueHistory.put(date, value);
	}
	
	/*
	 * Rückgabe der HashMap
	 * 
	 * Benutzung in der view.LineChart.java
	 */
	public static HashMap<String, Double> getDepotValueHistory() {
		return Stock.depotValueHistory;
	}
	
	/*
	 * Debug-Output
	 */
	public void printDepotValueHistory() {
		for (String date: this.depotValueHistory.keySet())
			System.out.println(date + ": " + this.depotValueHistory.get(date));
	}
	
	/*
	 * Depotwert HashMap löschen
	 */
	public static void clearDepotValueHistory() {
		Stock.depotValueHistory.clear();
	}
	
	/*
	 * Depotwert HashMap setzen
	 */
	public static void setDepotValueHistory(HashMap hmNew) {
		Stock.depotValueHistory = hmNew;
	}
}
