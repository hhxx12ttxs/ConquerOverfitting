class StringBefehle2 {
	private String vokale  = "aeiouäöü";
	
	public boolean enthaelt(String s, char c) {
		for (int i = 0; i < s.length(); i = i + 1) {
			if (c == s.charAt(i)) {
				return true;
			}
		}
		
		return false;
	}

	// "Ersatz", 's', 'b'
	
	// Erstelle einen leeren String "ausgabe"
	// Gehe Zeichen für Zeichen durch den String
	// Wenn das aktuelle Zeichen NICHT das gesuchte ist,
	// füge es so an den Ausgabe-String an.
	// Wenn es das gesuchte ist, hänge den
	// Ersatzbuchstaben an den Ausgabe-String an
	// Gib ausgabe zurück
	public String ersetze1(String s, char gesucht, char ersatz) {
		String ausgabe = "";
		
		for (int i = 0; i < s.length(); i = i + 1) {
			char aktuell = s.charAt(i);
			if (aktuell != gesucht) {
				ausgabe = ausgabe + aktuell;
			} else {
				ausgabe = ausgabe + ersatz;
			}
		}
		
		return ausgabe;
	}
	
	public String ersetze2(String s, char gesucht, char ersatz) {
		char gesucht_klein = ("" + gesucht).toLowerCase().charAt(0);
		char gesucht_gross = ("" + gesucht).toUpperCase().charAt(0);
		
		String ausgabe = "";
		
		for (int i = 0; i < s.length(); i = i + 1) {
			char aktuell = s.charAt(i);
			if (aktuell != gesucht_gross && aktuell != gesucht_klein) {
				ausgabe = ausgabe + aktuell;
			} else {
				ausgabe = ausgabe + ersatz;
			}
		}
		
		return ausgabe;
	}
	
	public String ersetze3(String s, char gesucht, char ersatz) {
		gesucht = Character.toLowerCase(gesucht);
		char gesucht_gross = Character.toUpperCase(gesucht);
		ersatz =  Character.toLowerCase(ersatz);
		char ersatz_gross =	 Character.toUpperCase(ersatz);
		
		String ausgabe = "";
		
		for (int i = 0; i < s.length(); i = i + 1) {
			char aktuell = s.charAt(i);
			
			if (aktuell == gesucht_gross) {
				ausgabe = ausgabe + ersatz_gross;
			} else if (aktuell == gesucht) {
				ausgabe = ausgabe + ersatz;
			} else {
				ausgabe = ausgabe + aktuell;
			}
		}
		
		return ausgabe;
	}
	
	public String chinesen(String s) {
		s = s + "\n\n";
		String ausgabe = s;
		
		// Bastel ein String nur mit "a"'s
		for (int i = 0; i < vokale.length(); i++) {
			char gesucht = vokale.charAt(i);
			s = ersetze3(s, gesucht, 'a');
		}

		for (int i = 1; i < vokale.length(); i++) {
			String t = ersetze3(s, 'a', vokale.charAt(i));
			ausgabe = ausgabe + t;
		}
		
		return ausgabe;
	}
	
	public boolean ist_vokal(char c) {
		return vokale.contains(Character.toString(c));
	}
	
	
	
	public String chinesenMitDoppellauten(String s) {
		//	s = "Drei Chin...."
		String ausgabe = "";
		
		// Doppellaute
		// ai, ei, ey, ay
		// au,
		// eu, äu, 
		// ie
		for (int i = 0; i < s.length() - 1; i = i + 1) {
			char aktuell = s.charAt(i);
			if ( ist_vokal(aktuell) ) {
				ausgabe = ausgabe + aktuell;
				
				if ( ist_vokal( s.charAt(i+1 )) ) {
					i = i + 1;
				}
			} else {
				ausgabe = ausgabe + aktuell;
			}
		}
		
		// Letztes Zeichen getrennt behandeln
		// s = "...d"
		// s = "...a"
		// s = "..da"
		// s = "..aa"
		char letzter = s.charAt(s.length() - 1);
		char vorletzter = s.charAt(s.length() - 2);
		if (! (ist_vokal(letzter) && ist_vokal(vorletzter)) ) {
			ausgabe += letzter;
		}
		
		return ausgabe;
		
		// 2 Vokale
		// Meier, Eier, Reiher, Geier
		// Treueeid => Trä'ä'äd
		// zweieiig => zwä'ä'äg
		// Vertrauen => Värträ'än
	}
	
	public void testChinesen() {
		String s = "Drei Chinesen mit dem Kontrabass; saßen " +
			"auf der Straße; und erzählten sich was, " + 
			"da kam die Polizei, Ja was ist den das? " +
			"Drei Chinesen mit den Kontrabass! Meier, Eier, " +
			"Reiher, Geier, a";
		// => "Draa Chanasan mat ..."
		// => "Dree Chenesen met ..."
		// => "Drii Chinisin mit ..."
		// System.out.println( chinesen(s) );
		s = "...d";
		System.out.println( chinesenMitDoppellauten(s) );
		s = "...a";
		System.out.println( chinesenMitDoppellauten(s) );
		s = "..da";
		System.out.println( chinesenMitDoppellauten(s) );
		s = "..aa";
		System.out.println( chinesenMitDoppellauten(s) );
	}
	
	public static void main(String[] args) {
		StringBefehle2 sb = new StringBefehle2();
		// String a = "Schwendi";
		// boolean gefunden = sb.enthaelt(a, 'z');
		// 
		// if (gefunden) {
		//	   System.out.println("'z' ist in '" + a + "' enthalten.");
		// } else {
		//	   System.out.println("'z' ist in '" + a + "' NICHT enthalten.");
		// }
		// 
		// a = "Ersatzbuchstaben";
		// String neu = sb.ersetze(a, 's', 'b');
		// System.out.println(neu);
		
		// a = "Drei Chinesen mit dem Kontrabass...";
		// sb.ersetze(a, 'e', 'ü');
		// System.out.println(ausgabe);
		// String s = "Äusgäbe";
		// System.out.println(s);
		// s = sb.ersetze3(s, 'ä', 'Ü');
		// System.out.println(s);
		
		sb.testChinesen();
	}
}

