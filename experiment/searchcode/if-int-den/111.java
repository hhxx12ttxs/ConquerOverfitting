/**
 * 
 */
package model;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import view.Chart;

/**
 * @author alexander
 * 
 * Class - StocksModel
 * 
 * Diese Klasse definiert das Singletone-Pattern.
 * Ausserdem sind in dieser Klasse die CSV-Import/-Export sowie Serialisierungs-Methoden definiert.
 */
public class StocksModel {
	/*
	 * Begin Singleton Pattern
	 */
	private String[] header = null;
	private ArrayList<Stock> MyStocks = null;
	private static StocksModel instance = null;
	
	private StocksModel() {
		MyStocks = new ArrayList<Stock>();
	}
	
	public static StocksModel getInstance() {
		if(instance==null)
			instance = new StocksModel();
		
		return instance;
	}

	public ArrayList<Stock> getStocksList() {
		 return this.MyStocks;
	}
	/*
	 * End Singleton Pattern
	 */
	
	
	// Getter fuer header  
	public String[] getHeader() {
		return this.header;
	}
	
	/*
	 * CSV-Import (Aktien)
	 */
	public void csvImport(String path) {
		// Initialisierung des BufferedReaders
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(path));
			String current_line = null;
			String[] current_row = null;
			// Header-Zeile aus dem CSV als Erstes abspeichern
			this.header = br.readLine().split(",");
			// Debug
			System.out.println("\tCSV-Import:");
			
			// Lese eine Zeile ein, solange das Seitenende nicht erreicht wurde
			while ((current_line = br.readLine()) != null) {
				// Debug
				System.out.print("\t"); 
				// Aktuelle Zeile beim "," aufteilen
				current_row = current_line.split(",");
				// Aufgeteilte Reihe in die MyStocks-ArrayList abspeichern
				this.MyStocks.add(new Stock(current_row));
				for (int i = 0; i < 9; i++) {
					System.out.print(current_row[i] + ", ");
				}
				System.out.println("");
			}
			// Debug
			System.out.println();
			br.close();
			
			// HashMaps aktualisieren
			for (int i = 0; i < MyStocks.size(); i++)
				Chart.updateStockValueHistory(i);
			Chart.updateDepotValueHistory();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * CSV-ImportFull (Aktien + History)
	 */
	public void csvImportFull(String path) {
		// Initialisierung des BufferedReaders
		BufferedReader br = null;
		
		try {
			br = new BufferedReader(new FileReader(path));
			String current_line = null;
			String current_row[] = null;
			// TRUE = Stocks, FALSE = History
			boolean type = false;
			
			// Header-Zeile aus dem CSV als Erstes abspeichern
			//this.header = br.readLine().split(",");
			// Debug
			System.out.println("CSV-Import:");
			
			// Lese CSV-Datei, solange das Seitenende nicht erreicht wurde
			while ((current_line = br.readLine()) != null) {
				// Debug
				System.out.println("\t");
				
				// Prüfen, ob Typ der Zeilen Aktien-Daten sind, falls ja, wird type auf true gesetzt
				if (current_line.equals("Aktien=WKN,Name,Anzahl,aktueller Preis,Kauf-Datum,Kauf-Preis,Verkauft,Verkaufs-Datum,Verkaufs-Preis")) {
					// Falls der String vorkommt, wird dieser übersprungen
					current_line = br.readLine();
					
					type = true;
				}
				
				// Prüfen, ob Typ der Zeilen History-Daten sind, falls ja, wird type auf false gesetzt
				if (current_line.equals("History=StockID,Date,Rate")) {
					// Falls der String vorkommt, wird dieser übersprungen
					current_line = br.readLine();
					
					type = false;
				}
				
				// Type = Aktie (true)
				if (type == true) {
					// Zeile beim "," splitten
					current_row = current_line.split(",");
					
					// Aufgeteilte Reihe in die MyStocks-ArrayList abspeichern
					this.MyStocks.add(new Stock(current_row));
					
					// Debug
					for (int i = 0; i < 9; i++) {
						System.out.print(current_row[i] + ", ");
					}
				}
				
				// Type = History
				if (type == false) {
					// Zeile beim "," splitten
					current_row = current_line.split(",");
					
					// Debug
					System.out.print(current_line);
					
					// wenn kein "DEPOT" in der StockID-Nummer steht, werden Daten in die Aktien-Daten gespeichert 
					if (!(current_row[0].equals("DEPOT"))) {
						int id = Integer.parseInt(current_row[0]);
						// Aufruf der Insert-Methode mit den zu übergebenden Daten
						MyStocks.get(id).insertStockHistory(current_row[1], Double.parseDouble(current_row[2]));
					// Falls jedoch "DEPOT" im StockID-Feld steht, werden die Daten in die Depot-History gespeichert
					} else {
						Stock.insertDepotHistory(current_row[1], Double.parseDouble(current_row[2]));
					}
				}
			}
			// Debug
			System.out.println();
			br.close();
			
			// HashMaps aktualisieren
			for (int i = 0; i < MyStocks.size(); i++) {
				Chart.updateStockValueHistory(i);
			}
			Chart.updateDepotValueHistory();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * CSV-Import (History)
	 */
	public void csvImportHistory(String path) {
		// Initialisierung des BufferedReaders
		BufferedReader br = null;
		
		try {
			br = new BufferedReader(new FileReader(path));
			String current_line = null;
			String current_row[] = null;
			// Header-Zeile aus der CSV-Datei als Erstes abspeichern
			this.header = br.readLine().split(",");
			// Debug
			System.out.println("\tCSV-Import:");
			
			// Lese eine Zeile ein, solange das Seitenende nicht erreicht wurde
			while ((current_line = br.readLine()) != null) {
				// Debug
				System.out.println("\t");
				// aktuelle Zeile beim "," aufteilen
				current_row = current_line.split(",");
				System.out.print(current_line);

				if (!(current_row[0].equals("DEPOT"))) {
					int id = Integer.parseInt(current_row[0]);
					// Aufruf der Insert-Methode mit den zu übergebenden Daten
					MyStocks.get(id).insertStockHistory(current_row[1], Double.parseDouble(current_row[2]));
				} else {
					Stock.insertDepotHistory(current_row[1], Double.parseDouble(current_row[2]));
				}
			}
		} catch (IOException e) {
			System.err.println(e);
		}
	}
	
	/*
	 * CSV-Export (Aktien)
	 */
	public void csvExport(String path) {
		// Header
		String export = "WKN,Name,Anzahl,aktueller Preis,Kauf-Datum,Kauf-Preis,Verkauft,Verkaufs-Datum,Verkaufs-Preis\n";
		
		// Zähler für jeden Satz in der ArrayList
		for (Stock current: this.MyStocks) {
			// Ruft die Methode getArrayListToString auf und speichert den 
			// zurückgegeben String in export ab
			export += current.getArrayListToString();
		}
		
		try {
			FileWriter fw = new FileWriter(path);
			
			// FileWriter schreibt den String in die Datei
			fw.write(export);
			
			// Flushen des Puffers
			fw.flush();
			// Datei schließen
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Debug
		System.out.println("csvExport: \n" + export);
	}
	
	/*
	 * CSV-ExportFull (Aktien + History)
	 */
	public void csvExportFull(String path) {
		// Header - Aktien
		String export = "Aktien=WKN,Name,Anzahl,aktueller Preis,Kauf-Datum,Kauf-Preis,Verkauft,Verkaufs-Datum,Verkaufs-Preis\n";
		// Zähler für jeden Satz in der ArrayList
		for (Stock current: this.MyStocks) {
			// Ruft die Methode getArrayListToString auf und speichert den 
			// zurückgegeben String in export ab
			export += current.getArrayListToString();
		}
		
		// Header - History
		export += "History=StockID,Date,Rate\n";
		// Zähler durch die Aktienliste
		for (int i = 0; i < this.getStocksList().size(); i++) {
			// Speichern der Inhalte in export
			export += MyStocks.get(i).getStockHashMapToString(i);
		}
		// Depotwert-Verlauf in export abspeichern
		export += Stock.getDepotHashMapToString();

		// String export in eine Datei abspeichern
		try {
			FileWriter fw = new FileWriter(path);
			
			// FileWriter schreibt den String in die Datei
			fw.write(export);
			
			// Flushen des Puffers
			fw.flush();
			// Datei schließen
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Debug
		System.out.println("csvExportFull" + export);
	}
	
	/*
	 * CSV-Export (History)
	 */
	public void csvExportHistory(String path) {
		// Header der CSV-Datei
		String export = "StockID, Date, Rate\n";
		
		// Zähler durch die Aktienliste
		for (int i = 0; i < this.getStocksList().size(); i++) {
			// Speichern der Inhalte in export
			export += MyStocks.get(i).getStockHashMapToString(i);
		}
		
		// Depotwert-Verlauf abspeichern
		export += Stock.getDepotHashMapToString();
		
		try {
			FileWriter fw = new FileWriter(path);
			
			// FileWriter schreibt den String in die Datei
			fw.write(export);
			
			// Flushen des Puffers
			fw.flush();
			// Datei schließen
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Debug
		System.out.println("csvExportHistory" + export);
	}
	
	/*
	 * Ausgabe aller Aktien
	 * 
	 * Zum Debuggen
	 */
	public void printToConsole() {
		System.out.println("\tcurrent stocks in arraylist:");
		
		// Zähler für jeden Satz in der ArrayList
		for (Stock current: this.MyStocks) {
			System.out.print(current.getArrayListToString());
		}
	}

	/*
	 * Serialisieren von Objekten
	 * 
	 * ArrayList MyStock = alMyStocks
	 * HashMap DepotHistory = hmDepotHistory
	 */	
	public static void writeSer(String path) {
		ArrayList alMyStocks = StocksModel.getInstance().MyStocks;
		HashMap hmDepotHistory = Stock.getDepotValueHistory();
		
		System.out.println("writeSer-Method\n----------");

		OutputStream fos = null;
		
		try {
			fos = new FileOutputStream(path);
			ObjectOutputStream o = new ObjectOutputStream(fos);
			
			o.writeObject(alMyStocks);
			o.writeObject(hmDepotHistory);
			
		} catch (IOException e) {
			System.err.println(e);
		} finally {
			try {
				fos.close();
			} catch (Exception e) {
				
			}
		}
	}
	
	/*
	 * Laden von serialisierten Objekten
	 */
	public static void readSer(String path) {
		HashMap hmNew = null;
		ArrayList alNew = null;
		
		System.out.println("readSer-Method\n----------");
		
		InputStream fis = null;
		
		try {
			fis = new FileInputStream(path);
			ObjectInputStream o = new ObjectInputStream(fis);
			
			alNew = (ArrayList) o.readObject();
			hmNew = (HashMap) o.readObject();
			
			
		} catch (IOException e) {
			System.err.println(e);
		} catch (ClassNotFoundException e) {
			System.err.println(e);
		} finally {
			try {
				fis.close();
			} catch (Exception e) {
				
			}
		}
		
		StocksModel.getInstance().MyStocks = alNew;
		Stock.setDepotValueHistory(hmNew);
	}
}

