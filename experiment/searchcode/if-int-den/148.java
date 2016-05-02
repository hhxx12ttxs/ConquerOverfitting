package tools;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 
 * @author menzel
 *
 */
public class getSave {

	private Vergleich random;

	/**
	 * Konstruktor fuer das Laden zwei gespeicherter Profile. erzeugt einen Vergleich aus diesen
	 * @param link1 - link zum ersten Profil
	 * @param link2 - link zum zweiten Profil
	 * @throws IOException 
	 */
	public getSave(String link1, String link2) throws IOException{

		String text1 = getText(link1);
		String text2 = getText(link2);			
		
		//erzeuge 2 leere Profile
		Profil random1 = new Profil(null, 0, null);
		Profil random2 = new Profil(null, 0, null);

		// setze ihre Namen:
		random1.setName(getName(text1));
		random2.setName(getName(text2));

		System.out.println("Titel: " +getName(text1));

		//Setzte ListeH:
		random1.setListeH(getListe(link1));
		random2.setListeH(getListe(link2));

		//belaed den leeren Vergleich
		this.random = new Vergleich(random1,random2);
		
		System.out.println("\nDie Aehnlichkeit der beiden Dateien betraegt: "  + Math.round(random.getAehnlichkeit()));

		
	}

	/**
	 * Parst aus einer Datei ein vorhandenes Textprofil
	 * @param link zur datei
	 * @return ArrayList mit Ngrammen
	 * @throws IOException 
	 */
	private CopyOnWriteArrayList<Ngramm> getListe(String link) throws IOException {

		CopyOnWriteArrayList<Ngramm> ListeHrandom  = new CopyOnWriteArrayList<Ngramm>();
		BufferedReader dateiStream;

			dateiStream = new BufferedReader(new FileReader(link));

			String zeile = dateiStream.readLine();

			while (  ((zeile = dateiStream.readLine()) != null)) {
				
				//parse alle ausser die erste Zeile die den Namen enthaellt
				if(!zeile.startsWith("#") && !zeile.contains("]")){

					//parse name und anzahl aus jeder zeile eines Vergleichs Files
					String name = zeile.substring((zeile.indexOf('>')+1), (zeile.indexOf('=')));
					int anzahl =Integer.parseInt(zeile.substring((zeile.indexOf('=')+1), (zeile.indexOf('>',(zeile.indexOf('=')+1)))));

					Ngramm random = new Ngramm(name, anzahl);
					ListeHrandom.add(random);
				}
			}

		return ListeHrandom;
	}

	/**
	 * Parst aus einem Text den Namen
	 * @param text - text aus welchem der Name geparst wird
	 * @return name - Name des Textes
	 */
	private String getName(String text) {

		return text.substring(5, text.indexOf('#', 6));
	}

	/**
	 * List den Text aus einer Datei
	 * @param link1 - Ort der Datei
	 * @return String des Textes in der Datei
	 * @throws IOException 
	 */
	private String getText(String link) throws IOException {

		String text = "";
		String line;

		BufferedReader dateiStream;

			dateiStream = new BufferedReader(new FileReader(link));

			while ((line = dateiStream.readLine()) != null)  {			
				text += line;
			}

			display.Con.testAussage(text);
			dateiStream.close();
		
		return text;

	}

	/**
	 * Getter fuer den erzeugten Vergleich
	 * @return den erzeugten Vergleich
	 */
	public Vergleich returnSave(){
		return this.random;

	}

}

