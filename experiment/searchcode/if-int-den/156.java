import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import java.text.*;
/**
 * Die Klasse Einkaufsliste kann aus den Kantinenplan-Objekten eine Einkaufsliste mit einzelnen 
 * Einkaufslistenpositionen erzeugen. Dazu erzeugt sie als Zwischenschritt BedarfPos-Objekte 
 * fόr den Gesamtbedarf einer Zutat. Die Bedarfe werden dann in BestellPos-Objekte όberfόhrt.
 * 
 * Eine Einkaufsliste steht in einer Kompositionsbeziehung zur Bestellpositionen, weil eine Einkaufsliste nie 
 * ohne eine Bestellpsotion existieren kann. Sie verwaltet zudem die BedarfsPos und aggregeiert
 * diese. Sie aggregiert auch die Kantinenplδne um den Gesamtbedarf an Artikeln zu ermitteln, die als 
 * BedarfPos gefόhrt werden. Die Assoziation zur Lieferantenverwaltung ermφglicht der Einkaufsliste die 
 * gόnstigsten Artikel zu ermitteln und aus der BedarfsPos die BestellPos zu generieren.
 * Fόr den Planungslauf steht sie in Assoziation zur Kantinenplanung.
 * Damit Einkaufslisten erzeugt werden kφnnen, ist auch eine Assoziation zur Serviceklasse Datei vorhanden.
 * 
 * @author Rene Wiederhold 
 * @version 1.00
 */
public class Einkaufsliste
{
	/** Enthδlt die Objekte der Klasse BestellPos */
	private ArrayList<BestellPos> bestellPosList;
	/** Enthδlt die Objekte der Klasse BedarfPos */
	private ArrayList<BedarfPos> bedarfPosList;
	/** Enthδlt die Gesamtkosten der Bestellung inklusive der Transportkosten */
	private double gesamtkosten;
	/** Enthδlt die zu verwendende Lieferantenverwaltung */
	private Lieferantenverwaltung lieferantenverw;

	/**
	 * Der Konstruktor
	 */
	public Einkaufsliste()
	{

		bedarfPosList=new ArrayList<BedarfPos>();
		bestellPosList=new ArrayList<BestellPos>();
	}

	/**
	 * Die Methode fόgt die benφtigten Zutaten fόr einen Kantinenplan den Bedarfsposition-Objekten hinzu bzw. erstellt diese.
	 *
	 * @param  kantinenplan   Ein Kantinenplanobjekt
	 */
	public void addKantinenplan(Kantinenplan kantinenplan)
	{ 
		//Tagesgericht-Schleife
		for (int i=0;i<kantinenplan.getTagesgerichte().size();i++){
			Tagesgericht tg=kantinenplan.getTagesgerichte().get(i);
			int m=tg.getMenge();
			//Zutatenschleife
			for (int j=0;j<tg.getRezept().getZutaten().size();j++){
				Zutat z=tg.getRezept().getZutaten().get(j);
				String zname=z.getName();
				String zeinh=z.getEinheit();
				float zmenge=z.getMenge();
				
				if (bedarfPosList.isEmpty()){
					//Wenn noch keine Bedarfposition angelegt wurde, kann einfach hinzugefόgt werden
					BedarfPos bPos=new BedarfPos();
					bPos.setName(zname);
					bPos.setEinheit(zeinh);
					bPos.setMenge(m*zmenge);
					bedarfPosList.add(bPos);
				}
				else {
					//Es sind bereits Objekte in der bedarfPosList
					//Es muss geprόft werden ob ein Objekt gleichen Namens vorhanden ist
					//Hier wird eine "Iterator-Schleife verwendet, wie bei addLebensmittel in der Lieferantenverwaltung
					Iterator<BedarfPos> it=bedarfPosList.iterator();
					//Variable fόr die "Vorhanden"-Prόfung
					Boolean vorh=false;
					//Variable fόr die bereits in der List vorhandene Bedarfposition
					BedarfPos vorhBP=new BedarfPos();
					//Solang die List noch ein weiteres Element hat, lδuft die Schleife
					while (it.hasNext()){
						//next() gibt das nδchste BedarfPos-Objekt
						BedarfPos bedarfPosIt=it.next();
						//Stimmt der Name der BedarfPos mit der zu prόfenden Zutat όberein?
						//Wenn ja, dass Objekt temporδr gespeichert und die Schleife gebrochen.
						if(bedarfPosIt.getName().equals(zname)){
							vorh=true;
							vorhBP=bedarfPosIt;
							break;
						}
						//Stimmt der όbergebene Name der BedarfPos nicht mit dem iterierten όberein, bleibt vorh auf false
						else{
							vorh=false;
						}	
					} //while-Ende - Jetzt steht entweder vorh auf true und vorhBP enthδlt das BedarfPos-Objekt oder vorh steht auf false
					// Lebensmittel-Objekt schon vorhanden
					if(vorh==true){
						vorhBP.setMenge(vorhBP.getMenge()+(m*zmenge));
						//Debug-Print
						//MainWin.StringOutln(zname+"-Menge von "+vorhBP.getMenge()+" auf "+(vorhBP.getMenge()+(m*zmenge))+" um "+(m*zmenge)+" angepasst");
					}
					// Lebensmittel-Objekt noch nicht vorhanden
					else if(vorh==false){
						vorhBP.setName(zname);
						vorhBP.setMenge(m*zmenge);
						vorhBP.setEinheit(zeinh);
						bedarfPosList.add(vorhBP);
						//Debug-Print
						//MainWin.StringOutln((m*zmenge)+" "+zeinh+" "+zname+" der Liste hinzugefόgt!");
					}	
				}
			} //Ende Zutatenschleife
		} //Ende Tagesgericht-Schleife
		MainWin.StringOutln("Der Kantinenplan fόr "+kantinenplan.getStandort()+" wurde der Einkaufsliste hinzugefόgt.");
	}

	/**
	 * Die Methode gibt eine Liste mit allen Bedarfpositionen zurόck.
	 * @return Eine ArrayList, welche die BedarfPos-Objekte referenziert.
	 */
	public ArrayList<BedarfPos> getBedarfPosList() {
		return bedarfPosList;
	}
	/**
	 * Die Methode weiίt der Einkaufsliste eine zu verwendende Lieferantenverwaltung zu.
	 * 
	 */
	public void setLieferantenverwaltung(Lieferantenverwaltung l){
		this.lieferantenverw=l;
	}
	/**
	 * Erzeugt aus den Bedarfspositionen (welche zuvor mit addKantinenplan aus den Kantinenplδnen erzeugt wurden) eine Einkaufsliste, genauer, 
	 * Bestellpositionen, die in einer Liste gesammelt werden (demnach die Einkaufs-Liste)
	 * 
	 * @return     true, fόr eine erfolgreich erstellte Einkaufsliste, false, falls Fehler aufgetreten sind.
	 */
	public boolean erzeugeEinkaufsliste(Lieferantenverwaltung lieferantenverw)
	{
		this.lieferantenverw=lieferantenverw;
		bestellPosList=makeVariante2();
		berechneGesamtkosten();

		return true;
	}
	/**
	 * Die Methode gleicht in zwei Schritten die Bedarfpositionen mit den Artikeln ab. Zunδchst werden Bedarfpositionen identifiziert, bei denen eine Bestellung beim 
	 * Bauernhof selbst in den Fδllen gόnstiger ist, in denen sonst kein weiterer Artikel von diesem Bauernhof bestellt wird. Diese Bauernhφfe werden zwischengespeichert.
	 * Im zweiten Schritt kann dann fόr die Bauernhφfe, bei denen ohnehin eine Bestellung erfolgt, die Lieferkostenbetrachtung entfallen, der volle Deckungsbeitrag fόr
	 * die Lieferkosten ist ja schon gedeckt. Die Methode liefert mit Sicherheit nicht das optimale Ergebnis, aber kann zumindest eindeutige Preisvorteile erkennen. Um 
	 * ein absolutes Kostenminimum identifizieren zu kφnnen, wδre eine Brute-Force-Methode nφtig, die alle mφglichen Bestellkombinationen vergleicht.
	 * @return Eine Liste, die die Bestellpositionen enthδlt
	 */
	private ArrayList<BestellPos> makeVariante2(){
		ArrayList<BedarfPos> bedarfListCopy=new ArrayList<BedarfPos>();
		ArrayList<BestellPos> bestellList=new ArrayList<BestellPos>();
		ArrayList<Bauernhof> bauernhofList=new ArrayList<Bauernhof>();
		Float kmSatz=getkmSatz();
		//Zunδchst muss eine tiefe Kopie der BedarfPos-Objekte bzw. der bedarfPosList erstellt werden, auf der das Szenario rechnen kann, ohne die Original-Daten zu zerstφren.
		for (BedarfPos bedarf:bedarfPosList){
			bedarfListCopy.add(bedarf.clone());
		}
		/*Debug-Print
    	System.out.println("Bedarfliste am Beginn");
    	System.out.println("==============================================");
    	for (BedarfPos bedarf:bedarfListCopy){
    		System.out.println(bedarf.getMenge()+" "+bedarf.getEinheit()+" "+bedarf.getName());
    	}*/
		for (BedarfPos bedarf:bedarfListCopy){
			/*
			 * Optionen:
			 * 1. Die komplette BedarfPos lδsst sich von einem Artikel abdecken.
			 * 	- Grosshandel mit spezif. Lieferkosten ist billiger als Bauernhof netto. ->Grosshandel ist immer besser
			 * 	- Bauernhof mit spezif. Lieferkosten (volle Lieferkosten auf einen Artikel gerechnet) ist billiger als Grosshandel mit spezif. Lieferkosten ->Bauernhof ist immer billiger.
			 * 	- Der Spielraum dazwischen hδngt davon ab, wieviele andere Artikel noch beim Bauernhof gekauft werden, denn desto mehr Artikel, desto niedriger die spezif.Lieferkosten.
			 * 2. Die komplette BedarfPos lδsst sich nicht komplett vom Bauernhof beschaffen, d.h. eine weitere Bestellung vom Grosshandel wird nφtig.
			 * 	- 
			 * 	- Anteilige Bestellung beim Bauernhof inklusive Lieferkosten (volle Lieferkosten) und anteilige Bestellung beim Grosshandel inklusive Lieferkosten ist billiger als gesamte Bestellung beim Grosshandel ->Bestellung beim Bauernhof und beim Grosshandel auffόllen.
			 * 	- Gesamte Bestellung beim Grosshandel ist billiger als anteillige Bestellung beim Bauernhof OHNE Lieferkosten und anteilige Bestellung beim Grosshandel mit Lieferkosten ->Gesamte Bestellung beim Grosshandel ist billiger.
			 * 	- Der Spielraum dazwischen ist der Optimierungsbereich. 
			 * 	- 
			 */

			//Vollabdeckung Variablen
			Artikel cheapestBauerVoll=new Artikel();
			cheapestBauerVoll.setPreis(Float.MAX_VALUE);
			double cheapestBauerSummeNettoVoll=Float.MAX_VALUE;
			double cheapestBauerLieferkostenVoll=Float.MAX_VALUE;

			Artikel cheapestGHVoll=new Artikel();
			cheapestGHVoll.setPreis(Float.MAX_VALUE);
			double cheapestGHSummeNettoVoll=Float.MAX_VALUE;
			double cheapestGHLieferkostenVoll=Float.MAX_VALUE;

			//Teilabdeckung Variablen
			Artikel cheapestBauerTeil=new Artikel();
			cheapestBauerTeil.setPreis(Float.MAX_VALUE);
			double cheapestBauerSummeNettoTeil=Float.MAX_VALUE;
			double cheapestBauerLieferkostenTeil=Float.MAX_VALUE;
			int maxGebindeBauer=0;

			Artikel cheapestGHTeil=new Artikel();
			cheapestGHTeil.setPreis(Float.MAX_VALUE);
			double cheapestGHSummeNettoTeil=Float.MAX_VALUE;
			double cheapestGHLieferkostenTeil=Float.MAX_VALUE;

			ArrayList<Artikel> artList=lieferantenverw.gibAlleArtikel(bedarf.getName());
			for (Artikel art:artList){				

				int anzGebinde=(int) (bedarf.getMenge()/art.getGebindegroesse());
				//Wenn Modulo grφίer 0 ist, muss ein Gebinde mehr beschafft werden.
				if (!(bedarf.getMenge()%art.getGebindegroesse()==0)){
					anzGebinde++;
				}
				if (art.getArtikelanzahl()>=anzGebinde){
					//kompletter Bedarf lieίe sich mit dem Artikel abdecken
					if (art.getLieferant().getClass()==Bauernhof.class){
						//Artikel ist Bauernhof
						Bauernhof bh=(Bauernhof) art.getLieferant();
						//Artikel + Lieferkosten des Bauernhofes ist kleiner als Artikel + Lieferkosten des aktuell billigsten Anbieters
						if ( (art.getPreis()*anzGebinde + (bh.getEntfernung()*kmSatz)) < (cheapestBauerSummeNettoVoll+cheapestBauerLieferkostenVoll) ){
							//Aktuell billigsten ersetzen
							cheapestBauerSummeNettoVoll= art.getPreis()*anzGebinde;
							cheapestBauerLieferkostenVoll=bh.getEntfernung()*kmSatz;
							cheapestBauerVoll=art;
						}
					}
					if (art.getLieferant().getClass()==Grosshandel.class){
						Grosshandel gh=(Grosshandel) art.getLieferant();
						//Artikel + Lieferkosten des Grosshandels ist kleiner als aktuell billigster Artikel + Lieferkosten
						if ( art.getPreis()*anzGebinde + gh.getLieferkostensatz() < cheapestGHSummeNettoVoll + cheapestGHLieferkostenVoll ){
							//billigsten ersetzen
							cheapestGHSummeNettoVoll= art.getPreis()*anzGebinde;
							cheapestGHLieferkostenVoll=gh.getLieferkostensatz();
							cheapestGHVoll=art;
						}
					}
				}
				else {
					//kompletter Bedarf lieίe sich mit diesem Artikel nicht abdecken
					if (art.getLieferant().getClass()==Bauernhof.class){
						//Artikel ist Bauernhof
						Bauernhof bh=(Bauernhof) art.getLieferant();
						//Artikel + Lieferkosten des Bauernhofes ist kleiner als Artikel + Lieferkosten des aktuell billigsten Anbieters
						if ( (art.getPreis()*anzGebinde + (bh.getEntfernung()*kmSatz)) < (cheapestBauerSummeNettoTeil+cheapestBauerLieferkostenTeil) ){
							//Aktuell billigsten ersetzen
							cheapestBauerSummeNettoTeil= art.getPreis()*anzGebinde;
							cheapestBauerLieferkostenTeil=bh.getEntfernung()*kmSatz;
							cheapestBauerTeil=art;
							maxGebindeBauer=art.getArtikelanzahl();
						}
					}
					if (art.getLieferant().getClass()==Grosshandel.class){
						Grosshandel gh=(Grosshandel) art.getLieferant();
						//Artikel + Lieferkosten des Grosshandels ist kleiner als aktuell billigster Artikel + Lieferkosten
						if ( art.getPreis()*anzGebinde + gh.getLieferkostensatz() < cheapestGHSummeNettoTeil + cheapestGHLieferkostenTeil ){
							//billigsten ersetzen
							cheapestGHSummeNettoTeil= art.getPreis()*anzGebinde;
							cheapestGHLieferkostenTeil=gh.getLieferkostensatz();
							cheapestGHTeil=art;
						}
					}
				}

			} //Artikel-Schleife Ende
			/*Debug-Print 
			System.out.println();
			System.out.println(bedarf.getName());
			System.out.println(bedarf.getMenge()+" "+bedarf.getEinheit());
			System.out.println("==================");
			if (!(cheapestBauerVoll.getLieferant()==null)){
				System.out.println("Billigster Bauernhof voll: "+cheapestBauerVoll.getLieferant().getLieferantenName());
				System.out.println("Summe: "+cheapestBauerSummeNettoVoll);
				System.out.println("Lieferkosten: "+cheapestBauerLieferkostenVoll);
			}
			if (!(cheapestGHVoll.getLieferant()==null)){
				System.out.println("Billigster Grosshandel voll: "+cheapestGHVoll.getLieferant().getLieferantenName());
				System.out.println("Summe: "+cheapestGHSummeNettoVoll);
				System.out.println("Lieferkosten: "+cheapestGHLieferkostenVoll);
			}
			if (!(cheapestBauerTeil.getLieferant()==null)){
				System.out.println("Billigster Bauernhof teil: "+cheapestBauerTeil.getLieferant().getLieferantenName());
				System.out.println("Summe: "+cheapestBauerSummeNettoTeil);
				System.out.println("Lieferkosten: "+cheapestBauerLieferkostenTeil);
				System.out.println("Maximal lieferbar: "+maxGebindeBauer);
			}
			if (!(cheapestGHTeil.getLieferant()==null)){
				System.out.println("Billigster Grosshandel teil: "+cheapestGHTeil.getLieferant().getLieferantenName());
				System.out.println("Summe: "+cheapestGHSummeNettoTeil);
				System.out.println("Lieferkosten: "+cheapestGHLieferkostenTeil);
				System.out.println("Maximal lieferbar: "+maxGebindeGH);
			}*/
			//Jetzt mόssen alle Kombinationen, die sicher eine Entscheidung fόr einen Bauernhof bringen durchgegangen werden
			if (cheapestGHSummeNettoVoll+cheapestGHLieferkostenVoll < cheapestBauerSummeNettoVoll){
				//BauerVoll ist raus
				//Prόfung ob Teilbestellung Bauer + Auffόllung gόnstiger ist, als alles beim GH
				//Kosten Teilbestellung Bauer
				double teilbestellung = cheapestBauerTeil.getPreis()*maxGebindeBauer+cheapestBauerLieferkostenTeil;
				double zusaetzlichzubestellen = bedarf.getMenge() - (maxGebindeBauer*cheapestBauerTeil.getGebindegroesse());
				int anzZusaetzlicheGebinde=(int) zusaetzlichzubestellen;
				//Wenn Modulo grφίer 0 ist, muss ein Gebinde mehr beschafft werden.
				if (!(zusaetzlichzubestellen%cheapestGHVoll.getGebindegroesse()==0)){
					anzZusaetzlicheGebinde++;
				}
				if  ( (teilbestellung + anzZusaetzlicheGebinde*cheapestGHVoll.getPreis()+cheapestGHLieferkostenVoll) < (cheapestGHSummeNettoVoll+cheapestGHLieferkostenVoll)){
					//Definitiv beim Bauer die Teillieferung holen
					//Bauernhof in BauernhofListe merken zum spδteren Abgleich
					System.out.println("1. Runde: Bei "+cheapestBauerVoll.getLieferant().getLieferantenName()+" Teilbestellung absetzen");
					BestellPos bp1=new BestellPos();
					cheapestBauerTeil.setArikelanzahl(0);
					bp1.setMenge(maxGebindeBauer);
					bp1.setArtikel(cheapestBauerTeil);
					bedarf.setMenge(bedarf.getMenge() - maxGebindeBauer*cheapestBauerTeil.getGebindegroesse());
					bestellList.add(bp1);
					if (!bauernhofList.contains(cheapestBauerTeil.getLieferant())){
						bauernhofList.add((Bauernhof) cheapestBauerTeil.getLieferant());
					}
				}
			}
			if (cheapestGHSummeNettoVoll+cheapestGHLieferkostenVoll > cheapestBauerSummeNettoVoll+cheapestBauerLieferkostenVoll){
				//GHVoll ist raus ->keine weiteren Prόfungen -> Alles beim BauernhofVoll
				//Bauernhof in BauernhofListe merken zum spδteren Abgleich
				//System.out.println("1. Runde: Beim Bauernhof "+cheapestBauerVoll.getLieferant().getLieferantenName()+" alles kaufen");
				BestellPos bp=new BestellPos();
				//Gebindeanzahl bestimmen
				int anzGebinde=(int) (bedarf.getMenge()/cheapestBauerVoll.getGebindegroesse());
				//Wenn Modulo grφίer 0 ist, muss ein Gebinde mehr beschafft werden.
				if (!(bedarf.getMenge()%cheapestBauerVoll.getGebindegroesse()==0)){
					anzGebinde++;
				}
				cheapestBauerVoll.setArikelanzahl(cheapestBauerVoll.getArtikelanzahl()-anzGebinde);
				bp.setMenge(anzGebinde);
				bp.setArtikel(cheapestBauerVoll);
				bedarf.setMenge(bedarf.getMenge() - anzGebinde*cheapestBauerVoll.getGebindegroesse());
				if (bedarf.getMenge()<0){
					bedarf.setMenge(0);
				}
				bestellList.add(bp);
				//Bauernhof in der Liste speichern, damit spδter abgeglichen werden kann, ob bei diesem ohnehin bestellt werden muss.
				if (!bauernhofList.contains(cheapestBauerVoll.getLieferant())){
					bauernhofList.add((Bauernhof) cheapestBauerVoll.getLieferant());
				}
			}			
		} //Ende Bedarf-Schleife
		//================================================================================================
		// Jetzt sind Bauernhφfe identifiziert, bei denen ohnehin bestellt wird, beim Abgleich der folgenden Bedarfspositionen kφnnen 
		//also die Lieferkosten dieser Bauernhφfe ignoriert werden.
		for (BedarfPos bedarf : bedarfListCopy) {
			if (bedarf.getMenge() > 0) {
				// Vollabdeckung Variablen
				Artikel cheapestBauerVoll = new Artikel();
				cheapestBauerVoll.setPreis(Float.MAX_VALUE);
				double cheapestBauerSummeNettoVoll = Float.MAX_VALUE;

				Artikel cheapestGHVoll = new Artikel();
				cheapestGHVoll.setPreis(Float.MAX_VALUE);
				double cheapestGHSummeNettoVoll = Float.MAX_VALUE;
				double cheapestGHLieferkostenVoll = Float.MAX_VALUE;

				// Teilabdeckung Variablen
				Artikel cheapestBauerTeil = new Artikel();
				cheapestBauerTeil.setPreis(Float.MAX_VALUE);
				double cheapestBauerSummeNettoTeil = Float.MAX_VALUE;
				int maxGebindeBauer = 0;

				Artikel cheapestGHTeil = new Artikel();
				cheapestGHTeil.setPreis(Float.MAX_VALUE);
				double cheapestGHSummeNettoTeil = Float.MAX_VALUE;
				double cheapestGHLieferkostenTeil = Float.MAX_VALUE;
				int maxGebindeGH = 0;

				ArrayList<Artikel> artList = lieferantenverw.gibAlleArtikel(bedarf.getName());
				for (Artikel art : artList) {
					int anzGebinde = (int) (bedarf.getMenge() / art.getGebindegroesse());
					// Wenn Modulo grφίer 0 ist, muss ein Gebinde mehr beschafft werden.
					if (!(bedarf.getMenge() % art.getGebindegroesse() == 0)) {
						anzGebinde++;
					}
					if (art.getArtikelanzahl() >= anzGebinde) {
						// kompletter Bedarf lieίe sich mit dem Artikel abdecken
						if (art.getLieferant().getClass() == Bauernhof.class
								&& bauernhofList.contains(art.getLieferant())) {
							// Artikel ist Bauernhof
							// Kosten des Bauernhofes ist kleiner als Kosten des aktuell billigsten Anbieters
							if ((art.getPreis() * anzGebinde) < (cheapestBauerSummeNettoVoll)) {
								// Aktuell billigsten ersetzen
								cheapestBauerSummeNettoVoll = art.getPreis() * anzGebinde;
								cheapestBauerVoll = art;
							}
						}
						if (art.getLieferant().getClass() == Grosshandel.class) {
							Grosshandel gh = (Grosshandel) art.getLieferant();
							// Artikel + Lieferkosten des Grosshandels ist kleiner als aktuell billigster Artikel + Lieferkosten
							if (art.getPreis() * anzGebinde	+ gh.getLieferkostensatz() < cheapestGHSummeNettoVoll + cheapestGHLieferkostenVoll) {
								// billigsten ersetzen
								cheapestGHSummeNettoVoll = art.getPreis() * anzGebinde;
								cheapestGHLieferkostenVoll = gh.getLieferkostensatz();
								cheapestGHVoll = art;
							}
						}
					} else {
						// kompletter Bedarf lieίe sich mit diesem Artikel nicht abdecken
						if (art.getLieferant().getClass() == Bauernhof.class && bauernhofList.contains(art.getLieferant())) {
							// Artikel ist Bauernhof
							// Artikel + Lieferkosten des Bauernhofes ist kleiner als Artikel + Lieferkosten des aktuell billigsten Anbieters
							if ((art.getPreis() * anzGebinde) < (cheapestBauerSummeNettoTeil)) {
								// Aktuell billigsten ersetzen
								cheapestBauerSummeNettoTeil = art.getPreis() * anzGebinde;
								cheapestBauerTeil = art;
								maxGebindeBauer = art.getArtikelanzahl();
							}
						}
						if (art.getLieferant().getClass() == Grosshandel.class) {
							Grosshandel gh = (Grosshandel) art.getLieferant();
							// Artikel + Lieferkosten des Grosshandels ist kleiner als aktuell billigster Artikel + Lieferkosten
							if (art.getPreis() * anzGebinde < cheapestGHSummeNettoTeil	+ cheapestGHLieferkostenTeil) {
								// billigsten ersetzen
								cheapestGHSummeNettoTeil = art.getPreis() * anzGebinde;
								cheapestGHLieferkostenTeil = gh.getLieferkostensatz();
								cheapestGHTeil = art;
								maxGebindeGH = art.getArtikelanzahl();
							}
						}
					}
				} // Ende Artikelschleife 2
				if (cheapestGHSummeNettoVoll + cheapestGHLieferkostenVoll < cheapestBauerSummeNettoVoll) {
					// BauerVoll ist raus
					// Prόfung ob Teilbestellungen vielleicht besser sind.
					// Kosten Teilbestellung Bauer
					double zusaetzlichzubestellen = bedarf.getMenge() - (maxGebindeBauer * cheapestBauerTeil.getGebindegroesse());
					int anzZusaetzlicheGebindeBauer = (int) zusaetzlichzubestellen;
					if (!(zusaetzlichzubestellen% cheapestGHVoll.getGebindegroesse() == 0)) {
						anzZusaetzlicheGebindeBauer++;
					}
					double teilbestellungBauer = cheapestBauerTeil.getPreis()*maxGebindeBauer + (anzZusaetzlicheGebindeBauer*cheapestGHVoll.getPreis()+cheapestGHLieferkostenVoll);

					// Kosten Teilbestellung GH
					zusaetzlichzubestellen = bedarf.getMenge() - (maxGebindeGH*cheapestGHTeil.getGebindegroesse());
					int anzZusaetzlicheGebindeGH = (int) zusaetzlichzubestellen;
					if (!(zusaetzlichzubestellen%cheapestGHVoll.getGebindegroesse() == 0)) {
						anzZusaetzlicheGebindeGH++;
					}

					double teilbestellungGH = cheapestGHTeil.getPreis()*maxGebindeGH+cheapestGHLieferkostenTeil + (anzZusaetzlicheGebindeGH*cheapestGHVoll.getPreis()+cheapestGHLieferkostenVoll);

					if ((teilbestellungBauer < cheapestGHSummeNettoVoll+cheapestGHLieferkostenVoll)	&& teilbestellungBauer < teilbestellungGH) {
						// Beim Bauer die Teillieferung holen
						System.out.println("Bei "+cheapestBauerTeil.getLieferant().getLieferantenName()+" Teilbestellung absetzen");
						BestellPos bp1 = new BestellPos();
						cheapestBauerTeil.setArikelanzahl(0);
						bp1.setMenge(maxGebindeBauer);
						bp1.setArtikel(cheapestBauerTeil);

						// Beim GH den Rest
						BestellPos bp2 = new BestellPos();
						cheapestGHVoll.setArikelanzahl(cheapestGHVoll.getArtikelanzahl() - anzZusaetzlicheGebindeBauer);
						bp2.setMenge(anzZusaetzlicheGebindeBauer);
						bp2.setArtikel(cheapestGHVoll);
						if (bedarf.getMenge() - maxGebindeBauer*cheapestBauerTeil.getGebindegroesse() - anzZusaetzlicheGebindeBauer*cheapestGHVoll.getGebindegroesse() > 0) {
							System.out.println("Problem bei Teillieferung vom Bauer");
						} else {
							bedarf.setMenge(0);
							bestellList.add(bp1);
							bestellList.add(bp2);
						}
					} else if ((teilbestellungGH < cheapestGHSummeNettoVoll+cheapestGHLieferkostenVoll)	&& teilbestellungGH < teilbestellungBauer) {
						// Beim GH Teillieferung machen
						System.out.println("Bei "+cheapestGHTeil.getLieferant().getLieferantenName()+" Teilbestellung absetzen");
						BestellPos bp1 = new BestellPos();
						cheapestGHTeil.setArikelanzahl(0);
						bp1.setMenge(maxGebindeGH);
						bp1.setArtikel(cheapestGHTeil);

						// Beim GH den Rest
						BestellPos bp2 = new BestellPos();
						cheapestGHVoll.setArikelanzahl(cheapestGHVoll.getArtikelanzahl() - anzZusaetzlicheGebindeGH);
						bp2.setMenge(anzZusaetzlicheGebindeGH);
						bp2.setArtikel(cheapestGHVoll);
						if (bedarf.getMenge() - maxGebindeBauer*cheapestBauerTeil.getGebindegroesse() - anzZusaetzlicheGebindeGH*cheapestGHVoll.getGebindegroesse() > 0) {
							System.out.println("Problem bei Teillieferung vom Grosshandel");
						} else {
							bedarf.setMenge(0);
							bestellList.add(bp1);
							bestellList.add(bp2);
						}
					} else {// Beim GH alles kaufen
						BestellPos bp = new BestellPos();
						// Gebindeanzahl bestimmen
						int anzGebinde = (int) (bedarf.getMenge() / cheapestGHVoll.getGebindegroesse());
						// Wenn Modulo grφίer 0 ist, muss ein Gebinde mehr
						// beschafft werden.
						if (!(bedarf.getMenge()%cheapestGHVoll.getGebindegroesse() == 0)) {
							anzGebinde++;
						}
						cheapestGHVoll.setArikelanzahl(cheapestGHVoll.getArtikelanzahl() - anzGebinde);
						bp.setMenge(anzGebinde);
						bp.setArtikel(cheapestGHVoll);
						bedarf.setMenge(bedarf.getMenge() - anzGebinde*cheapestGHVoll.getGebindegroesse());
						if (bedarf.getMenge() < 0) {
							bedarf.setMenge(0);
						}
						bestellList.add(bp);
					}
				}
				if (cheapestGHSummeNettoVoll + cheapestGHLieferkostenVoll > cheapestBauerSummeNettoVoll) {
					// GHVoll ist raus
					// Prόfung ob Teilbestellungen vielleicht besser sind.
					// Kosten Teilbestellung Bauer
					float zusaetzlichzubestellen = bedarf.getMenge() - (maxGebindeBauer * cheapestBauerTeil.getGebindegroesse());
					int anzZusaetzlicheGebindeBauer = (int) zusaetzlichzubestellen;
					if (!(zusaetzlichzubestellen% cheapestBauerVoll.getGebindegroesse() == 0)) {
						anzZusaetzlicheGebindeBauer++;
					}
					double teilbestellungBauer = cheapestBauerTeil.getPreis()*maxGebindeBauer + (anzZusaetzlicheGebindeBauer*cheapestBauerVoll.getPreis());

					// Kosten Teilbestellung GH
					zusaetzlichzubestellen = bedarf.getMenge() - (maxGebindeGH*cheapestGHTeil.getGebindegroesse());
					int anzZusaetzlicheGebindeGH = (int) zusaetzlichzubestellen;
					if (!(zusaetzlichzubestellen%cheapestBauerVoll.getGebindegroesse() == 0)) {
						anzZusaetzlicheGebindeGH++;
					}

					double teilbestellungGH = cheapestGHTeil.getPreis()*maxGebindeGH+cheapestGHLieferkostenTeil + (anzZusaetzlicheGebindeGH*cheapestBauerVoll.getPreis());

					if ((teilbestellungBauer < cheapestBauerSummeNettoVoll)	&& teilbestellungBauer < teilbestellungGH) {
						// Beim Bauer die Teillieferung holen
						System.out.println("Bei "+cheapestBauerTeil.getLieferant().getLieferantenName()+" Teilbestellung absetzen");
						BestellPos bp1 = new BestellPos();
						cheapestBauerTeil.setArikelanzahl(0);
						bp1.setMenge(maxGebindeBauer);
						bp1.setArtikel(cheapestBauerTeil);

						// Beim Bauer den Rest
						BestellPos bp2 = new BestellPos();
						cheapestBauerVoll.setArikelanzahl(cheapestBauerVoll.getArtikelanzahl() - anzZusaetzlicheGebindeBauer);
						bp2.setMenge(anzZusaetzlicheGebindeBauer);
						bp2.setArtikel(cheapestBauerVoll);
						if (bedarf.getMenge() - maxGebindeBauer*cheapestBauerTeil.getGebindegroesse() - anzZusaetzlicheGebindeBauer*cheapestBauerVoll.getGebindegroesse() > 0) {
							System.out.println("Problem bei Teillieferung vom Bauer");
						} else {
							bedarf.setMenge(0);
							bestellList.add(bp1);
							bestellList.add(bp2);
						}
					} else if ((teilbestellungGH < cheapestGHSummeNettoVoll+cheapestGHLieferkostenVoll)	&& teilbestellungGH < teilbestellungBauer) {
						// Beim GH Teillieferung machen
						System.out.println("Bei "+cheapestGHTeil.getLieferant().getLieferantenName()+" Teilbestellung absetzen");
						BestellPos bp1 = new BestellPos();
						cheapestGHTeil.setArikelanzahl(0);
						bp1.setMenge(maxGebindeGH);
						bp1.setArtikel(cheapestGHTeil);

						// Beim GH den Rest
						BestellPos bp2 = new BestellPos();
						cheapestBauerVoll.setArikelanzahl(cheapestBauerVoll.getArtikelanzahl() - anzZusaetzlicheGebindeGH);
						bp2.setMenge(anzZusaetzlicheGebindeGH);
						bp2.setArtikel(cheapestBauerVoll);
						if (bedarf.getMenge() - maxGebindeBauer*cheapestBauerTeil.getGebindegroesse() - anzZusaetzlicheGebindeGH*cheapestBauerVoll.getGebindegroesse() > 0) {
							System.out.println("Problem bei Teillieferung vom Grosshandel");
						} else {
							bedarf.setMenge(0);
							bestellList.add(bp1);
							bestellList.add(bp2);
						}
					} else {// Beim Bauer alles kaufen
						BestellPos bp = new BestellPos();
						// Gebindeanzahl bestimmen
						int anzGebinde = (int) (bedarf.getMenge() / cheapestBauerVoll.getGebindegroesse());
						// Wenn Modulo grφίer 0 ist, muss ein Gebinde mehr
						// beschafft werden.
						if (!(bedarf.getMenge()%cheapestBauerVoll.getGebindegroesse() == 0)) {
							anzGebinde++;
						}
						cheapestBauerVoll.setArikelanzahl(cheapestBauerVoll.getArtikelanzahl() - anzGebinde);
						bp.setMenge(anzGebinde);
						bp.setArtikel(cheapestBauerVoll);
						bedarf.setMenge(bedarf.getMenge() - anzGebinde*cheapestBauerVoll.getGebindegroesse());
						if (bedarf.getMenge() < 0) {
							bedarf.setMenge(0);
						}
						bestellList.add(bp);
					}
				}
			} // Ende if ! Bedarf=0
		} // Ende Bedarfschleife 2
		/* Debug-Print
		for (BestellPos bestell : bestellList) {
			System.out.println(bestell.getMenge() + " Gebinde a "
					+ bestell.getArtikel().getGebindegroesse() + " "
					+ bestell.getArtikel().getEinheit() + " "
					+ bestell.getArtikel().getName() + " bei "
					+ bestell.getArtikel().getLieferant().getLieferantenName()
					+ " kaufen.");
		}
		for (BedarfPos bedarf : bedarfListCopy) {
			System.out.println(bedarf.getName() + " " + bedarf.getMenge());
		}*/
		return bestellList;
	}
	/**
	 * Die Methode gibt den km-Satz in Euro-Cent zurόck, welcher in der config.properties hinterlegt wurde.
	 * 
	 * @return Den km-Satz, welche in der config.properties der Anwendung angegeben ist, in Euro-Cent.
	 */
	private float getkmSatz(){
		float kmSatz = Float.MAX_VALUE;
		try{
			Properties properties = new Properties();
			BufferedInputStream stream = new BufferedInputStream(new FileInputStream("config.properties"));
			properties.load(stream);
			stream.close();
			kmSatz = Float.parseFloat(properties.getProperty("kmSatz"));
		} 
		catch (IOException e) {
			MainWin.StringOutln(e.toString());
			MainWin.StringOutln("Die Datei config.properties konnte nicht gelesen werden. Prόfen Sie, " +
					"ob sie im Anwendungsordner vorhanden ist.");
		}
		return kmSatz;
	}

	/**
	 * Berechnet die Gesamtkosten der Bestellung inklusive Transportkosten, die sich aus allen im BestellPosArrayList enthaltenen 
	 * Bestellpositionen ergibt und schreibt sie in das Attribut gesamtkosten, welches mit getGesamtkosten() ausgelesen werden
	 * kann.
	 */
	public void berechneGesamtkosten()
	{
		double gesamtkosten=0;
		float kmSatz=getkmSatz();
		ArrayList<Bauernhof> bauernhofList=new ArrayList<Bauernhof>();
		for (BestellPos b:bestellPosList){
			if(b.getArtikel().getLieferant().getClass()==Bauernhof.class){
				if (!(bauernhofList.contains(b.getArtikel().getLieferant()))){
					//BauernhofList enthδlt den Bauernhof noch nicht
					Bauernhof bauer=(Bauernhof) (b.getArtikel().getLieferant());
					bauernhofList.add(bauer);
					//Lieferkosten des Bauernhofs aufaddieren
					gesamtkosten=gesamtkosten+bauer.getEntfernung()*kmSatz;
				}
			}
			if (b.getArtikel().getLieferant().getClass()==Grosshandel.class){
				Grosshandel gh=(Grosshandel) (b.getArtikel().getLieferant());
				//Lieferkostensatz des Grosshandels aufaddieren
				gesamtkosten=gesamtkosten+gh.getLieferkostensatz();
			}
			//Nettokosten des Artikels aufaddieren
			gesamtkosten=gesamtkosten+b.getMenge()*b.getArtikel().getPreis();

		}
		this.gesamtkosten=gesamtkosten;
	}
	/**
	 * Gibt die Gesamtkosten der Einkaufsliste zurόck, die vorher mit berechneGesamtkosten() berechnet wurden.
	 *
	 * @return Die Gesamtkosten der Einkaufsliste.
	 */
	public double getGesamtkosten()
	{
		return gesamtkosten;
	}
	/**
	 * Liefert einen ArrayList zurόck, der alle BestellPos-Objekte der Einkaufsliste enthδlt
	 * 
	 * @return Einen ArrayList der alle BestellPos-Objekte enthδlt
	 */
	public ArrayList<BestellPos> getBestellPos()
	{
		return bestellPosList;
	}
	
	public void schreibeEinkaufsliste() {
		ArrayList<BestellPos> sortList=new ArrayList<BestellPos>();
		//Um die Einkaufsliste nach Lieferanten zu sortieren, mόssen die Bestellpositionen sortiert werden
		for (Lieferant l:lieferantenverw.getLieferanten()){
			for(BestellPos bp:bestellPosList)
				if (bp.getArtikel().getLieferant()==l){
					sortList.add(bp);
				}
		}
		DecimalFormat zweiNachkomma = new DecimalFormat (",##0.00");
		DecimalFormat eineNachkomma = new DecimalFormat (",##0.0");
		Datei eklDatei = new Datei( "Einkaufsliste.txt");
		//MainWin.StringOutln("Schreibe Einkaufsliste.txt");
		if (eklDatei.openOutFile_FS()==0) {
			eklDatei.writeLine("Einkauflsliste");
			eklDatei.writeLine("Anz Name                          Lieferant                               Gebindegrφsse     Einzelpreis   Summe   Lieferkosten");
			String ausgabeZeile;	
			ArrayList<Bauernhof> bauerList=new ArrayList<Bauernhof>();
			
			for( BestellPos bp:sortList) {
				Artikel a=bp.getArtikel();
				StringBuffer lieferkosten=new StringBuffer();
				String anzOffset="";
				StringBuffer nameOffset=new StringBuffer();
				String preisOffset="";
				String summeOffset="";
				StringBuffer lieferOffset= new StringBuffer();
				String gebindeOffset="";
				//Zahlenforamte fόr die Ausgabe

				if (a.getLieferant().getClass()==Grosshandel.class){
					Grosshandel gh=(Grosshandel) a.getLieferant();
					//Da die Lieferkosten bei den Grosshandel nur einstellig sind, reicht es hier erstmal, nur einen Space den Lieferkosten vorzuhδngen
					lieferkosten.append(" ");
					lieferkosten.append(zweiNachkomma.format(new Double(gh.getLieferkostensatz())));
				}
				else if(a.getLieferant().getClass()==Bauernhof.class){
					Bauernhof bauer=(Bauernhof) a.getLieferant();
					if (!bauerList.contains(bauer)){
						//Nur bei erstmaligen Auftauchen des Bauernhofes werden die Lieferkosten ausgewiesen.
						new Double(bauer.getEntfernung()*getkmSatz()).toString();
						lieferkosten.append(zweiNachkomma.format(new Double(bauer.getEntfernung()*getkmSatz())));
						bauerList.add(bauer);
					}
				}
				//AnzahlGebinde Offset bestimmen: 3 Zeichen reserviert
				if (bp.getMenge()<100) anzOffset=" ";
				if (bp.getMenge()<10) anzOffset="  ";
				//Lieferantenname Offset: 40 Zeichen reserviert
				for (int i=0;i<(40-a.getLieferant().getLieferantenName().length());i++){
					lieferOffset.append(" ");
				}
				//Artikelname Offset: 30 Zeichen reserviert
				for (int i=0;i<(30-a.getName().length());i++){
					nameOffset.append(" ");
				}
				//Gebindegrφίe Offset: Maximal 6 Zeichen vor dem Komma (bzw. 7 mit dem Trennpunkt, deshalb auch die Verschiebung
				if (a.getGebindegroesse()<100000) gebindeOffset=" ";
				if (a.getGebindegroesse()<10000) gebindeOffset="  ";
				if (a.getGebindegroesse()<1000) gebindeOffset="    ";
				if (a.getGebindegroesse()<100) gebindeOffset="     ";
				if (a.getGebindegroesse()<10) gebindeOffset="      ";
				//Fehlt die einstellige Einheit, wird stattdessen einfach ein Space gesetzt
				String einheit =a.getEinheit();
				if(einheit.equals("")) einheit=" ";
				//Artikelpreis Offset: 	
				if (a.getPreis()<1000) preisOffset="  ";
				if (a.getPreis()<100) preisOffset="   ";
				if (a.getPreis()<10) preisOffset="    ";
				//double statt float wegen Ungenauigkeiten
		        double summe = a.getPreis()*bp.getMenge();
		        //Bestimmung der Vorkommastellen hier etwas besser. Der Offset fόr die Summe hat ebenfalls die Verschiebung wegen dem Tausender-Punkt  
		        String[] asplit =  Double.toString(summe).split("\\.");
		        //Der String wird am Punkt geteilt, nicht am Komma, da hier das Numberformat noch garnicht benutzt wurde
		        int vorkomma = asplit[0].length();
		        if (vorkomma<6) summeOffset=" ";
		        if (vorkomma<5) summeOffset="  ";
		        if (vorkomma<4) summeOffset="    ";
		        if (vorkomma<3) summeOffset="     ";
		        if (vorkomma<2) summeOffset="      ";
				//Zusammensetzen der Ausgabezeile
				ausgabeZeile = anzOffset+bp.getMenge()+" "+a.getName()+nameOffset+a.getLieferant().getLieferantenName()+lieferOffset+gebindeOffset+eineNachkomma.format(a.getGebindegroesse())+
						" "+einheit+"      "+preisOffset+zweiNachkomma.format(a.getPreis())+"    "+summeOffset+zweiNachkomma.format(summe)+"  "+lieferkosten;
				if( eklDatei.writeLine_FS(ausgabeZeile) != 0) {
					MainWin.StringOutln("Fehler beim Schreiben der Bestellposition "+a.getName()+" des Lieferanten "+a.getLieferant().getLieferantenName());
					break;
				}
			}
			if( eklDatei.writeLine_FS("                                                                                         Gesamtkosten: "+zweiNachkomma.format(gesamtkosten)) != 0) {
				MainWin.StringOutln("Fehler beim Schreiben der Gesamtkosten");
			}
			if( eklDatei.closeOutFile_FS()!=0)
				MainWin.StringOutln("Fehler beim Schlieίen der Ausgabedatei");
		} else 
			MainWin.StringOutln("Die Ausgabedatei kann nicht geφffnet werden.");
		MainWin.StringOutln("Ausgabe der Einkaufsliste in "+System.getProperty("user.dir")+" als einkaufsliste.txt");

	}
}


