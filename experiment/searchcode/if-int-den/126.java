package display;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import tools.Matrix;
import tools.Profil;
import tools.Vergleich;
import tools.getSave;
/**
 * 
 * @author menzel
 *
 */
public class Con {

	private static final String version = "0.4.2";
	private static final String autor = "M. Menzel";
	private static final boolean TestAussagen = false;


	/**
	 * @param args
	 */
	public static void main(String[] args)  {

		willkommen();

		run();
	}

	/**
	 * Lauf Programm, steuert den Ablauf des Programms, beendet das Programm wenn der User dies ausloest
	 */
	private static void run(){

		Matrix standart = new Matrix(); //erzeuge standart Matrix

		String eingabe;
		String file = null;
		boolean run = true;
		String re = null; // re fuer restriktionsenzym


		while(run){
			eingabe = Frage("Was moechten sie tun ?");


			switch(eingabe){
			case "g":

				re = Frage("Geben sie die Schnittstelle des Restriktionsenzyms ein, Beispiel: 'AT|CG'. Ohne Anfuerhrungszeichen'\n" +
						"der vertikale Strich zeigt die tatsaechliche Schnittstelle an:");

				eingabe = "v";

				//break fehlt, damit das Program weiter zu case "v" laeuft !
			case "v":

				file = Frage("Geben sie den Namen oder Text der ersten Datei ein:");
				Profil dflt = new Profil(file, 4, re);

				file = Frage("Geben sie den Namen oder Text der zweiten Datei ein:");
				Profil dflt2 = new Profil(file, 4, re);

				Vergleich one = new Vergleich(dflt, dflt2);

				System.out.println("\nDie Aehnlichkeit der beiden Dateien betraegt: "  + Math.round(one.getAehnlichkeit()));

				standart.addVergleich(one);

				break;
			case "m":

				//zeige die Matrix:
				line(10);
				standart.zeigeMatrix();
				line(10);

				break;
			case "s":

				String link1 = Frage("Wie heisst die erste .tcs Datei?");
				String link2 = Frage("Wie heisst die zweite .tcs Datei?");

				link1 = pruefeEndung(link1);
				link2 = pruefeEndung(link2);

				//hole erzeugen vergleich und fuege ihn zur Matrix hinzu
				getSave dflt3;
				try {
					dflt3 = new getSave(link1, link2);
					
					standart.addVergleich(dflt3.returnSave());
				
				} catch (IOException e) {
					
					System.out.println("Mit der Datei ist ein Fehler aufgetreten, sie existiert nicht oder ist Fehlerhaft");
				}


				break;
			case "h":
				willkommen(); //hilftsnachricht

				break;				
			case"exit":
				run = false;
				System.out.println("\nDas Programm wird nun beendet.");
				System.exit(0);

				break;
			default:				

				System.err.println("Unbekannte Eingabe");
				break;
			}
		}
	}
	/**
	 * Prueft ob die Korrekte Endung gesetzt wurde
	 * @param link - link der ueberprueft wird
	 * @return orginaler link wenn endung vorhanden, oder link mit endung wenn endung nicht vorhanden
	 */
	private static String pruefeEndung(String link) {

		String endung = ".tcs";

		if(link.endsWith(endung)){
			return link;
		} else{
			return (link + ".tcs");
		}
	}

	/**
	 * Begruesst den Nutzer und gibt Infos zum Programm und zur Bedienung aus
	 * Gibt Hinweis ob TestAussagen an sind (-> SuppressWarning)
	 */
	@SuppressWarnings("unused")
	private static void willkommen() {

		line(50);
		System.out.println("Willkommen zu TextCompare. Version: " + version + ". Autor: " + autor);
		System.out.println("Grundlegende Steuerung des Programms: \n" +
				"Um einen Vergleich zu starten geben sie 'v' ein. Das Format sollte dem .txt Format entsprechen\n" +
				"Um die Matrix zu zeigen geben sie 'm' ein. \n" +
				"Wenn sie gespeicherte .tcs Profile laden moechten geben sie 's' ein. \n" +
				"Damit diese Nachricht angezeigt wird druecken sie 'h'. \n" +
				"Einen genetischen Vergleich starten sie mit 'g' \n" +
				"Um das Programm zu beenden geben sie 'exit' ein.");
		
		if(TestAussagen == true ){
			System.err.println("\nTestaussage sind eingeschaltet\n");
		}

		line(50);
	}

	/**
	 * Gibt eine Linie der Laenge i aus, bestehd aus '_ '
	 * @param i - laenge der Linie
	 */
	private static void line(int i) {

		for(int j = 0; j<i; j++){
			System.out.print("_ ");
		}
		System.out.println();		
	}

	/**
	 * Stellt dem User eine Frage
	 * @param frage- text der Frage
	 * @return Antwort des Users auf die Frage
	 */
	public static String Frage(String frage){

		InputStreamReader conv = new InputStreamReader(System.in);
		BufferedReader in = new BufferedReader(conv);

		System.out.println(frage);
		try {
			return in.readLine();

		} catch (IOException e) {

			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Gibt Aussagen ueber den Ablauf des Programms zur Fehlersuche
	 * @param text - der ausgegeben wird
	 * @standart - Deaktiviert
	 */
	public static void testAussage(String text) {
		//System.out.println(text);
	}

	/**
	 * Gibt Aussagen ueber den Ablauf des Programms zur Fehlersuche, hier im spaeteren Programm Verlauf, ab Matrix und fuer Gene
	 * @param text - der ausgegeben wird
	 * @standart - Deaktiviert
	 */
	public static void testAussageLate(String text) {

		if(TestAussagen){
			System.out.println("#testAussage#  " + text);
		}
	}

}
