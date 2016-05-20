package processSimulation.statistic;

import java.util.ArrayList;
import java.util.Iterator;

//---------------------------------------------------------------------
//Änderungen Juli 2010 (Christoph Behrends):
//- zusätzlicher Konstruktor ohne Länge, da berechenbar
//- zusätzlicher Konstruktor mit ArrayList<Double>, 
//	da damit Zeiten besser zu sammeln sind
//- Anpassung der Methodennamen an Java Code Conventions
//- Methode getMedian(), um den Median einer Stichprobe zu bestimmen
//- neue Methoden: getMin(), getMax(), print(), blockData() und
//- removeFirstN(), removeFirstMinMax(), calcOSRTest(), calcKSTest(),
//- calcConfidenceIntervall(), calcInvMean()
//- calcChi2Test(), calcBlockingOSRTest() (auskommentiert)
//- sowie Überladungen
//---------------------------------------------------------------------

/**
 * Implementiert eine Stichprobe.
 * @author Andrea Baldas
 */

public class Sample {
	
	/**
	 * Stichprobenumfang
	 */
	int len;
	/**
	 * Datenfeld mit den Werten der Stichprobe
	 */
	public double arr[];
	
	
	/******************************************************************
	 * Erstellt eine Stichprobe mit dem Stichprobenumfang len und dem
	 * Datenfeld arr.
	 * @param len	Länge des Arrays
	 * @param arr	Array, das die Werte der Stichprobe enthält
	 *****************************************************************/
	public Sample(int len, double[] arr){
		this.len = len;
		this.arr = arr;
	}
	
	/**
	 * Erstellt eine Stichprobe mit dem Datenfeld arr.
	 * @param arr	Array mit den Werten der Stichprobe
	 */
	public Sample(double[] arr){
		this.len = arr.length;
		this.arr = arr;
	}
	
	public Sample(ArrayList<Double> list){
		len = list.size();
		arr = new double[len];
		
		Iterator<Double> it = list.iterator();
		int i = 0;
		
		while(it.hasNext()){
			arr[i] = it.next();
			i++;
		}
	}
	
	public Sample(){
	}

	/******************************************************************
	 * Berechnet die Summe aus allen Elementen.
	 * @return	Summe aus allen Elementen
	 *****************************************************************/
	public double getSum()
	{
		double result = 0;
		int i;
    
		for (i = 0; i < len; i++)
			result += arr[i];
		return result;
	}
	
	/******************************************************************
	 * Berechnet den arithmetischen Mittelwert.
	 * @return 	arithmetischer Mittelwert
	 *****************************************************************/
	public double getMean()
	{
		return getSum() / (double)len;
	}

	/******************************************************************
	 * Berechnet die Varianz der Stichprobe.
	 * @return	Varianz der Stichprobe
	 *****************************************************************/
	public double getVariance()
	{
		double m = getMean();
		double s = 0;
		double temp;
		int i;
    
		for (i = 0; i < len; i++)
		{   
			temp = arr[i] - m;
			s += temp * temp;
		}
    
		s /= (double)(len - 1);
    
		return s;
	}

	/******************************************************************
	 * Gibt den Umfang der Stichprobe zurück.
	 * @return	Umfang der Stichprobe (Länge des Arrays)
	 *****************************************************************/
	public int getLen(){
		return len;
	}
	
	/******************************************************************
	 * Setzt den Umfang der Stichprobe.
	 * @param n	Umfang der Stichprobe (Länge des Arrays) 
	 *****************************************************************/
	public void setLen(int n){
		len = n;
	}

	//Christoph Behrends:
	//in private geändert
	//Namen angepasst: ConfInterval() -> getConfIntervalDev
	/******************************************************************
	 * Berechnet das Konfidenzintervall:<br>
	 * Der Mittelwert einer Stichprobe liegt mit der Wahrscheinlichkeit
	 * 1-alpha in dem Intervall [Mittelwert - y, Mittelwert + y]
	 * @return y Konfidenzintervall: [Mittelwert - y, Mittelwert + y]
	 * @param alpha	Irrtumswahrscheinlichkeit
	 *****************************************************************/
	private double getConfIntervalDev(double alpha){
		double y,y1;
		TDtr t = new TDtr(len - 2); // len - 2, da zweiseitiger Test
		y = t.invDistFunction(alpha);
		y1 = Math.sqrt(getVariance() / len);
		y = y * y1;
		y = Math.abs(y);
		return y;
	}

	/**
	 * Gibt den Median einer Stichprobe zurück
	 * @return	Median der Stichprobe
	 */
	public double getMedian(){
		double med;
		double[] tmp = arr.clone();
		int len = tmp.length;

		java.util.Arrays.sort(tmp); // aufsteigend sortieren

		//wenn gerade Anzahl
		if(len % 2 == 0){
			double tmp1 = tmp[len / 2 -1];
			double tmp2 = tmp[len / 2];
			med = (tmp1 + tmp2) / 2;
		}
		//wenn ungerade Anzahl
		else{
			med = tmp[len / 2];
		}
		return med;
	}
	
	/**
	 * Eine beliebige Anzahl von Daten können zu Beginn der Datenliste
	 * entfernt werden.
	 * @param n	Anzahl der zu entfernenden Daten
	 * @return	Anzahl der in der Liste verbliebenden Daten
	 */
	public int removeFirstN(int n) throws IllegalArgumentException{

		if(n <= arr.length){
			arr = java.util.Arrays.copyOfRange(arr, n, arr.length);
			len = arr.length;
		}
		else{
			throw new IllegalArgumentException("Zu wenig Daten " +
					"zum Löschen vorhanden!");
		}
		return arr.length;
	}
	
	/**
	 * Eine Heuristik zur Bestimmung der transienten Phase, bei der die
	 * ersten Werte aus der Datenliste entfernt werden, die Minimum
	 * oder Maximum der Restfolge sind.
	 * @return Anzahl entfernter Werte
	 */
	public int removeFirstExtremes(){
		int removeVals = 0;
		
		double[] tmp = arr.clone();	//Kopie vom Array erstellen
		java.util.Arrays.sort(tmp); //Kopie aufsteigend sortieren

		int lvMin = 0;
		int lvMax = tmp.length-1;
		
		//Heuristik: zähle ab Anfang die Werte,
		//die Min/Max der Restfolge sind; dann enfernen
		for(int i = 0; i < arr.length; i++){
			//Wenn aktueller Wert Maximum ist
			if(arr[i] == tmp[lvMax]){
				lvMax--;
				removeVals++;
			}
			//Wenn aktueller Wert Minimum ist
			else if(arr[i] == tmp[lvMin]){
				lvMin++;
				removeVals++;
			}
			//Wenn kein Max/Min
			else{
				break;
			}
		}
		//ermittelte Anzahl entfernen
		removeFirstN(removeVals);
		return removeVals;
	}

	/**
	 * Es wird ein Konfidenzintervall berechnet, in dem der wahre 
	 * Mittelwert der Stichprobe mit einer Fehlerwahrscheinlichkeit
	 * von 5% liegt. Angenommene Verteilung für KS-Test ist eine
	 * Normalverteilung mit aus der Stichprobe errechneten
	 * Erwartungswert und Varianz.
	 * @return Konfidenzintervall
	 */
	public ConfInterval getConfidenceInterval(){
		return getConfidenceInterval(0.05);
	}
	
	/**
	 * Es wird ein Konfidenzintervall berechnet, in dem der wahre
	 * Mittelwert der Stichprobe mit der Fehlerwahrscheinlichkeit
	 * alpha liegt.
	 * Alle nötigen Voraussetzungen (unabhängige und normalverteilte
	 * Werte) werden, wenn möglich, mit der Fehlerwahrscheinlichkeit
	 * alpha geschaffen.
	 * @return Konfidenzintervall
	 * @param alpha	Fehlerwahrscheinlichkeit
	 * @param dtr angenommene Verteilung für KS-Test
	 */
	public ConfInterval getConfidenceInterval(double alpha){
		boolean end = false;
		double y;
		ConfInterval ci = new ConfInterval(Double.NaN, Double.NaN);

		//solange nicht erfolgreich oder Fehler
		while(!end){
			//wenn unabhängig
			if(calcOSRTest(alpha)){
				//wenn Stichprobe Verteilung entspricht
				if(calcKSTest(alpha)){
					y = getConfIntervalDev(alpha);
					double mean = getMean();
					ci = new ConfInterval(mean - y, mean + y);	
					end = true;
				}
				else if(!blockData()){
					end = true;
				}
			}
			else if(!blockData()){
				end = true;
			}
		}
		return ci;
	}
	
	/**
	 * Es wird das inverse Konfidenzintervall für den Mittelwert der
	 * Stichprobe mit einer Fehlerwahrscheinlichkeit von 5% bestimmt.
	 * @return inverses Konfidenzintervall
	 */
	public ConfInterval getCIInvMean(){
		return getCIInvMean(0.05);
	}

	/**
	 * Es wird das inverse Konfidenzintervall für den Mittelwert der
	 * Stichprobe bestimmt.
	 * @param alpha Fehlerwahrscheinlichkeit
	 * @param dtr angenommene Verteilung für KS-Test
	 * @return inverses Konfidenzintervall, oder [- | -] bei Fehler
	 */
	public ConfInterval getCIInvMean(double alpha){
		ConfInterval iv = getConfidenceInterval(alpha);
		iv = new ConfInterval(1 / iv.getHigh(), 1 / iv.getLow());
	
		return iv;
	}
	
	/**
	 * Die Werteliste wird geblockt.
	 * @return true wenn geblockt wurde und genug Daten für Auswertung
	 * 				vorhanden sind, sonst false
	 */
	public boolean blockData(){
		boolean succeeded;
		int lv = 0;				//Laufvariable für Array
		len = arr.length / 2;	//Länge des neuen Arrays nach Blockung
		boolean even;
		if(arr.length % 2 == 0){
			even = true;
		}
		else{
			even = false;
		}
		//für sinnvolle Auswertung werden noch >= 10 Werte verlangt 
		if(len >= 10){
			double[] tmp = new double[len];
			for(int i = 0; i < len; i++) {
				tmp[i] = (arr[lv++] + arr[lv++]) / 2;
			}
			//berücksichtigt den letzten Wert bei einer ungerade Länge
			if(!even){
				tmp[len-1] = (arr[arr.length-3] 
				                  + arr[arr.length-2] 
				                        + arr[arr.length-1]) / 3;
			}
			arr = tmp;	//übertrage Änderungen auf Array der Stichprobe
			succeeded = true;
		}
		else{
			System.out.println("\tBerechnung abgebrochen: " +
					"Bei Blockung zu wenig Werte vorhanden!");
			succeeded = false;
		}
		return succeeded;
	}
	
	/**
	 * Die Datenliste wird mit dem OSR-Test auf Unabhängigkeit geprüft
	 * und bei Bedarf so lange geblockt, bis Unabhängigkeit mit einer
	 * Fehlerwahrscheinlichkeit von 5% angenommen werden kann.
	 * @return true wenn Unabhängigkeit angenommen werden kann
	 */
//	public boolean calcBlockingOSRTest(){
//		boolean succeeded = false;
//		boolean end = false;
//		
//		while(!succeeded && !end){
//			if(calcOSRTest()){
//				succeeded = true;
//			}
//			else if(!blockData()){
//				end = true;
//			}
//		}
//		return succeeded;
//	}
	
	/**
	 * Der One-Sample-Runs-Test prüft, ob die Werte einer Datenreihe
	 * unabhängig sind und wird mit einer Fehlerwahrscheinlichkeit von
	 * 5% durchgeführt.
	 * @return true wenn Unabhängigkeit mit einer
	 * 				Fehlerwahrscheinlichkeit von 5%
	 * 				angenommen werden kann
	 */
	public boolean calcOSRTest(){
		return calcOSRTest(0.05);
	}
	
	/**
	 * Der One-Sample-Runs-Test prüft, ob die Werte einer Datenreihe
	 * unabhängig sind.
	 * @return true wenn Unabhängigkeit mit Fehlerwahrscheinlichkeit
	 * 				alpha angenommen werden kann
	 * @param alpha	Wahrscheinlichkeit für Fehler 1. Art
	 */
	public boolean calcOSRTest(double alpha){
		boolean zufaellig = false;

		OSRTest osr = new OSRTest(alpha);
		zufaellig = osr.calculateTest(this);

		return zufaellig;
	}
	
	/**
	 * Prüft, ob eine Stichprobe mit einer Fehlerwahrscheinlichkeit
	 * von 5% aus einer angenommenen Verteilung stammt.
	 * @return true wenn Stichprobe mit der Fehlerwahrscheinlichkeit
	 * 				aus der angenommenen Verteilung stammt
	 */
//	public boolean calcChi2Test(){
//		return calcChi2Test(0.05);
//	}
	
	/**
	 * Prüft, ob eine Stichprobe aus einer angenommenen Verteilung
	 * stammt.
	 * @return true wenn Stichprobe mit der Fehlerwahrscheinlichkeit
	 * 				aus der angenommenen Verteilung stammt
	 * @param alpha	Wahrscheinlichkeit für Fehler 1. Art
	 */
//	public boolean calcChi2Test(double alpha){
//		boolean sameDtr = false;
//		Chi2Test chi = new Chi2Test(alpha);
//		
//		if(chi.calculateTest(this)){
//			sameDtr = true;
//		}
//		else{
//			sameDtr = false;
//		}
//		return sameDtr;
//	}
	
	
//	public boolean calcFullChi2Test(){
//		boolean succeeded = false;
//		boolean ende = false;
//		
//		while(!succeeded && !ende){
//			if(calcOSRTest()){
//				if(calcChi2Test()){
//					succeeded = true;
//				}
//				else if(!blockData()){
//					ende = true;
//				}
//			}
//			else if(!blockData()){
//				ende = true;
//			}
//		}
//		return succeeded;
//	}
	
	/**
	 * Führt den Kolmogorov-Smirnov-Anpassungstest durch, um zu prüfen
	 * ob eine Stichprobe mit einer Fehlerwahrscheinlichkeit von 5% aus
	 * der angenommenen Normalverteilung mit aus der Stichprobe
	 * errechneten Erwartungswert und Varianz stammt.
	 * @return true wenn Stichprobe mit der Fehlerwahrscheinlichkeit
	 * 			von 5% aus der angenommenen Normalverteilung stammt,
	 * 			sonst false
	 */
	public boolean calcKSTest(){
		NormalDtr ndtr = new NormalDtr(getMean(), getVariance());
		return calcKSTest(0.05, ndtr);
	}
	
	/**
	 * Führt den Kolmogorov-Smirnov-Anpassungstest durch, um zu prüfen,
	 * ob eine Stichprobe aus der angenommenen Normalverteilung mit aus
	 * der Stichprobe errechneten Erwartungswert und Varianz stammt.
	 * @return true wenn Stichprobe mit Fehlerwahrscheinlichkeit alpha
	 * 		aus der angenommenen Normalverteilung stammt, sonst false
	 * @param alpha	Wahrscheinlichkeit für Fehler 1. Art
	 */
	public boolean calcKSTest(double alpha){
		NormalDtr ndtr = new NormalDtr(getMean(), getVariance());
		return calcKSTest(alpha, ndtr);
	}
	
	/**
	 * Führt den Kolmogorov-Smirnov-Anpassungstest durch, um zu prüfen,
	 * ob eine Stichprobe aus der angenommenen Verteilung stammt.
	 * @return	true wenn Stichprobe mit der Fehlerwahrscheinlichkeit
	 * von alpha aus der angenommenen Verteilung stammt, sonst false
	 * @param alpha	Wahrscheinlichkeit für Fehler 1. Art
	 * @param dtr	angenommene Verteilung
	 */
	public boolean calcKSTest(double alpha, Distribution dtr){

		boolean gleicheVert = false;

		KSTest ks =  new KSTest(alpha, dtr);
		gleicheVert = ks.calculateTest(this);
		return gleicheVert;
	}
	
	/**
	 * Liefert das Minimum der Stichprobe.
	 * @return Minimum
	 */
	public double getMin(){
		double min = arr[0];
		for(int i = 1; i < arr.length; i++){
			if(arr[i] < min){
				min = arr[i];
			}
		}
		return min;
	}
	
	/**
	 * Liefert das Maximum der Stichprobe.
	 * @return Maximum
	 */
	public double getMax(){
		double max = arr[0];
		for(int i = 1; i < arr.length; i++){
			if(arr[i] > max){
				max = arr[i];
			}
		}
		return max;
	}
	
	/**
	 * Gibt die Daten der Stichprobe in der Form "[index] = #.###" aus.
	 */
	public void print(){
		int tmp = String.valueOf(arr.length).length();
		for(int i = 0; i < arr.length; i++){
			System.out.printf("[%" + tmp + "d] = \t%.3f\n", i, arr[i]);
		}
	}
}

