/**
 * 
 */
package model;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * @author alexander
 * 
 * Class - Calculations
 * 
 * Diese Klasse beinhaltet sämtliche Berechnungen für die gesamte Anwendung
 */
public class Calculations {
	private static ArrayList<Stock> list = StocksModel.getInstance().getStocksList();
	
	/*
	 * Methode zur Format-Umwandlung
	 * 
	 * Es wird eine double-Zahl übergeben und die Rückgabe ist ein String mit zwei Nachkommastellen
	 */
	public static String getFormat(double unformated) {
		// Nachstellen der Double-Werte auf zwei begrenzen
		DecimalFormat df = new DecimalFormat("#0.00");
		
		return df.format(unformated);
	}
	
	/*
	 * Es folgen die Berechnungen für die einzelnen Aktien-Statistiken
	 */
	
	/*
	 * Berechnung des Spreads vom Kauf-Preis und aktuellem Preis
	 * 
	 * Berechnung: Spread = aktueller Preis - Kauf-Preis
	 */
	public static double getCurrentSpread_old(int selectedRow) {
		return (list.get(selectedRow).getCurRate() - list.get(selectedRow).getBuyRate());
	}
	
	/*
	 * Berechnung der aktuellen Differenz
	 * 
	 * Berechnung:
	 * 		Differenz = aktueller Preis - Kauf-Preis
	 */
	public static double getCurrentSpread(int selectedRow) {
		return (list.get(selectedRow).getCurRate() - list.get(selectedRow).getBuyRate());	
	}
	
	/*
	 * Berechnung der aktuellen Differenz in Prozent
	 * 
	 * Berechnung: 
	 * 		DifferenzProzent = (Differenz * 100) / Kauf-Preis
	 */
	public static double getCurrentSpreadPercent(int selectedRow) {
		Double spread = list.get(selectedRow).getCurRate() - list.get(selectedRow).getBuyRate();
		Double spread_percent = (spread * 100) / list.get(selectedRow).getBuyRate();
		
		return spread_percent;		
	}
	
	/*
	 * Berechnet den Kauf-Wert (Gesamt)
	 * 
	 * Berechnung:
	 * 		Kauf-Wert = Kauf-Preis * Anzahl
	 */
	public static double getBuyValue(int selectedRow) {
		Double buyValue = list.get(selectedRow).getBuyRate() * list.get(selectedRow).getQuantity();
		
		return buyValue;
	}
	
	/*
	 * Berechnet den aktuellen-Wert (Gesamt)
	 * 
	 * Berechnung:
	 * 		aktueller Wert = Kauf-Preis * Anzahl
	 */
	public static double getCurrentValue(int selectedRow) {
		Double currentValue = list.get(selectedRow).getCurRate() * list.get(selectedRow).getQuantity();
		
		return currentValue;
	}
	
	/*
	 * Berechnet den Differenz vom aktuellem Wert zum Kauf-Wert (Gesamt)
	 * 
	 * Berechnung: 
	 * 		differenceValue = aktuellen Wert - Kauf-Wert
	 */
	public static double getCurrentValueDifference(int selectedRow) {
		return (getCurrentValue(selectedRow) - getBuyValue(selectedRow));
	}
	
	/*
	 * Berechnet den Verkaufswert
	 * 
	 * Berechnung:
	 * 		Verkaufswert = Verkaufs-Preis * Anzahl
	 */
	public static double getSellValue(int selectedRow) {
		return (list.get(selectedRow).getSellRate() * list.get(selectedRow).getQuantity());
	}
	
	/*
	 * Berechnet die Differenz vom Verkaufs- und Kaufwert
	 * 
	 * Berechnung:
	 * 		differenceValue = Verkaufs-Volumen - Kauf-Volumen
	 */
	public static double getSellValueDifference(int selectedRow) {
		return (getSellValue(selectedRow) - getBuyValue(selectedRow));
	}
	
	/*
	 * Berechnet die Differenz in Prozent vom Verkauf- und Kauf-Volumen
	 * 
	 * Berechnung:
	 * 		ChangePercent = Verkaufs-Volumen - Kauf-Volumen
	 */
	public static double getSellVolumeChangePercent(int selectedRow) {
		return ((getSellValueDifference(selectedRow) * 100) / getBuyValue(selectedRow));
	}
	
	// -------------------------------------------------------
	
	/*
	 * Es folgen die Berechnungen für die gesamten Statistiken
	 */

	/*
	 * Berechnet die Summe, die den aktuellen Wert aller Aktien wiederspiegelt
	 * 
	 * Berechnung:
	 * 		für jede Aktie/Stock:
	 * 		currentAssetValue = Anzahl * aktueller-Preis
	 */
	public static double getCurrentDepotValue() {
		double currentDepotValue = 0.0;
		for (int i=0; i < list.size(); i++) {
			if (list.get(i).isSold())
				currentDepotValue += list.get(i).getQuantity() * list.get(i).getSellRate();
			else
				currentDepotValue += list.get(i).getQuantity() * list.get(i).getCurRate();
		}
		
		return currentDepotValue;
	}
	
	/*
	 * Berechnet die Summe, für die alle Aktien gekauft wurden
	 * 
	 * Berechnung:
	 * 		für jede Aktie/Stock:
	 * 		startingAssetValue = Anzahl * Kauf-Preis 
	 */
	public static double getBuyDepotValue() {
		double depotValue = 0.0;
		for (int i=0; i < list.size(); i++)
			depotValue += list.get(i).getQuantity() * list.get(i).getBuyRate();
		
		return depotValue;
	}
	
	/*
	 * Berechnet die Performance des Investmentkapitals
	 * 
	 * Berechnung:
	 * 		performanceAssetValue = aktuelles Kapital - Start-/Kauf-Kapital
	 */
	public static double getDepotChange() {
		return (getCurrentDepotValue() - getBuyDepotValue());
	}
	
	/*
	 * Berechnet die Performance des Investmentkapitals in Prozent
	 * 
	 * Berechnung:
	 * 		performanceAssetValuePercent = aktuelles Kapital - Start-/Kauf-Kapital
	 */
	public static double getPerformanceAssetPercent() {
		if (list.isEmpty())
			return 0;
		else {
			return ((getDepotChange() * 100) / getBuyDepotValue());
		}
	}
	
	
	/*
	 * Berechnet die Summe der verkauften Aktien
	 * 
	 * Berechnung:
	 * 		für jede Aktie/Stock die verkauft wurde:
	 * 		WinLoss = Anzahl * Verkaufs-Preis
	 */
	public static double getCurrentWinLoss() {
		double currentWinLoss = 0.0;
		for (int i=0; i < list.size(); i++)
			// Es werden nur die nicht-verkauften Aktien miteinbezogen
			if (list.get(i).isSold())
				currentWinLoss += (list.get(i).getQuantity() * list.get(i).getSellRate()) - (list.get(i).getQuantity() * list.get(i).getBuyRate());
		
		return currentWinLoss;
	}
	
	/*
	 * Berechnet die Summe der verkauften Aktien in Prozent
	 * TODO Not correct yet
	 */
	public static double getCurrentWinLossPercent() {
		double currentWinLossPercent = 0.0;
		for (int i=0; i < list.size(); i++)
			// Es werden nur die nicht-verkauften Aktien miteinbezogen
			if (list.get(i).isSold())
				currentWinLossPercent += (getCurrentWinLoss() * 100) / getBuyDepotValue();
		
		return currentWinLossPercent;
	}
	
	/*
	 * Berechnet die Summe aller gespeicherten Aktien
	 */
	public static int getStockSum() {
		int stockSum = 0;
		for (int i=0; i < list.size(); i++)
			stockSum += list.get(i).getQuantity();
		
		return stockSum;
	}
	
	
	/*
	 * Berechnung für TableModel
	 */

	/*
	 * Methode berechnet die Performance (€ / %) einer übergebenen Aktie
	 * und gibt diese zurück
	 * 
	 * Berechnung: 
	 * 		change_percent = (Change * 100) / Kauf-Preis
	 */
	public static String getPerformance(int selectedRow) {
		// Nachstellen der Double-Werte auf zwei begrenzen
		DecimalFormat df = new DecimalFormat("#0.00");
		
		String performance = null;
		
		Double change = list.get(selectedRow).getSellRate() - list.get(selectedRow).getBuyRate();
		Double change_percent = (change * 100) / list.get(selectedRow).getBuyRate();
		
		performance = df.format(change) + " € (" + df.format(change_percent) + " %)";
		
		return performance;		
	}
}

