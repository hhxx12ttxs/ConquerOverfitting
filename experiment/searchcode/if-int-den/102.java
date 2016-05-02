/*############################################################################
  Kodierung: UTF-8 ohne BOM
############################################################################*/

import java.util.LinkedList;

//############################################################################
/** Spielprinzip: 
  * # Generiert eine zufaellige Zahl der Länge 4 bis 10 ohne doppelten Ziffern
  * # Der Modus wird festgesetzt. Default: 1 (Benutzer)
  * # Es wird geraten bis die zufaellige Zahl gefunden wird.
  *
  * Zur Unterstuetzung der Benutzer und Benutzerinnen gibt das Programm vor jedem Ratevesuch
  * die vorhergehenden Rateversuche und ihr Ergebnis aus. Das Ergebnis besteht aus der Angabe,
  * der richtigen und falschen Stellen des Rateversuch im Vergleich zur zufaellig erzeugten Zahl.
  * 
  * @author Thomas Gerlach
  */
//############################################################################
public class MasterMind
{
  /** Enthält Level / Länge der zu ratenden Ziffernfolge [4 - 9] */
  private Integer level = 4;

  /** Enthält Modus / Spieltyp des Spieldurchlaufes
    * 1 - Benutzer
    * 2 - Computer
  */
  private Integer modus = 1;

  /** Anzahl der gültigen Versuche, nach Überschreitung -> Abbruch des Spieles */
  private Integer versuche = 10;

  /** Die zu erratene Ziffernfolge als Zeichenkette */
  private String geheim = new String();

  /** Liste der im Verlauf entstandenen Versuch beim Finden von Ziffer */
  private LinkedList<String> verlauf = new LinkedList<String>();
  
  //##########################################################################
  /**
    * Initialisiert die Attribute mit sinnvollen Werten.
  */
  //##########################################################################
  public MasterMind()
  {
    modus = 1;
    geheim = generate();
    versuche = 10;
  }
  
  //##########################################################################
  /** Liefert eine generierte Ziffernfolge mit dem Attribut Level
    *
    * @return Generierte Zeichenfolge
  */
  //##########################################################################
  private String generate()
  {
    return generate(level);
  }

  //##########################################################################
  /** Liefert eine generierte Ziffernfolge mit der übergebenen Länge
    *
    * @param anzahl Länge der zu generierenden Zeichenfolge
    *
    * @return Generierte Zeichenfolge
  */
  //##########################################################################
  private String generate(Integer anzahl)
  {
    String result = new String();
    Integer ziffer = 0;
    Generator zufall = new Generator(0, 9);

    while (result.length() < anzahl) {

      ziffer = zufall.ganzzahl();

      if (!result.contains(ziffer.toString())) {
        result += ziffer;
      }
    }

    return result; 
  }

  //##########################################################################
  /** Schreibt nach Prüfung das Attribut Level, generiert eine neue Ziffer und
    * liefert den Erfolg der Aktion
    *
    * @param eingabe Das zu setzende Level
    *
    * @return Den Erfolg der Aktion
  */
  //##########################################################################
  public boolean setLevel(Integer eingabe)
  {
    boolean ergebnis = false;
    
    if (0 < eingabe && eingabe < 9) {
      level = eingabe;
      geheim = generate(level);
      ergebnis = true;
    }
    
    return ergebnis ? true : false;
  }

  //##########################################################################
  /** Liefert den Wert des Attribut Level
  */
  //##########################################################################
  public Integer getLevel()
  {
    return level;
  }

  //##########################################################################
  /** Schreibt nach Prüfung das Attribut Modus und liefert Erfolg der Aktion
    *
    * @param eingabe Der zu setzende Modus
    *
    * @return Den Erfolg der Aktion
  */
  //##########################################################################
  public boolean setModus(Integer eingabe)
  {
    boolean ergebnis = false;
    
    if (eingabe.equals(1) || eingabe.equals(2)) {
      modus = eingabe;
      ergebnis = true;
    }
    
    return ergebnis ? true : false;
  }

  //##########################################################################
  /** Liefert den Wert des Attribut Modus
  */
  //##########################################################################
  public Integer getModus()
  {
    return modus;
  }

  //##########################################################################
  /** Führt das Spiel und fordert Benutzer/Computer zum Tippen auf bis Lösung
    * gefunden oder Versuche aufgebraucht sind. Ein gedruckter Verlauf informiert
    * den Spieler über vorangegangene Züge.
  */
  //##########################################################################
  public void spielen()
  {    
    verlauf.clear();
    geheim = generate();
    
    for (int i = 0; i < versuche && !verlauf.contains(geheim); i++)
    {
      verlauf.add((modus == 1) ? getBenutzerTipp() : getComputerTipp());
      schreibeVerlauf();
    }

    if (verlauf.contains(geheim)) {
      System.out.format("\nMit %d Versuchen geraten! Herzlichen Glückwunsch!\n", verlauf.size());
    } else {
      System.out.format("\nMit %d Versuchen verloren! %s wäre es gewesen ...\n", versuche, geheim);
    }
  }
  
  //##########################################################################
  /** Fordert den Nutzer zur Eingabe einer gültigen Ziffernfolge auf
    *
    * @return Liefert gültige Ziffernfolge
  */
  //##########################################################################
  private String getBenutzerTipp()
  {
    String code = Eingabe.symbole("\nIhr Tipp: ", level, "[0-9]{" + level + "}");
    while (!validiereCode(code) || verlauf.contains(code))
    {
      System.out.format("\nCode nicht valide oder bereits getippt!");
      code = Eingabe.symbole("\nIhr Tipp: ", level, "[0-9]{" + level + "}");
    }
    return code;
  }
  
  //##########################################################################
  /** Fordert den Computer zur Abgabe einer gültigen Ziffernfolge auf
    *
    * @return Liefert gültige Ziffernfolge
  */
  //##########################################################################
  private String getComputerTipp()
  {
    String kandidat = new String();
    boolean erfolg = false;
    boolean falsch = false;

    if (!verlauf.isEmpty())
    {
      for (int k = Integer.parseInt(verlauf.getLast()); !erfolg; k++)
      {
        kandidat = wandleIntegerZuString(k, level);
        if (validiereCode(kandidat))
        {
          falsch = false;
          for (int v = 0; v < verlauf.size() && !falsch; v++)
          {
            if (
                getPositionen(verlauf.get(v)) != getCodePositionen(verlauf.get(v), kandidat) ||
                getVariationen(verlauf.get(v)) != getCodeVariationen(verlauf.get(v), kandidat)
              )
            {
              falsch = true;
            }
          }
          if (!falsch)
          {
            erfolg = true;
          }
        }
      }
    } else {
      kandidat = getCodeMinimum(level);
    }
    
    System.out.format("\n\nComputer Tipp: %s", kandidat);

    return kandidat;
  }
  
  //##########################################################################
  /** Validiert übergebene Zeichenfolge anhand Einzigartigkeit und Länge
    *
    * @param code zu prüfende Zeichenfolge
    *
    * @return Erfolg der Prüfung
  */
  //##########################################################################
  private boolean validiereCode(String code)
  {
    return isCodeUnique(code) && code.length() == level ? true : false;
  }

  //##########################################################################
  /** Liefert eine übergebene Ganzzahl als Zeichenkettenentsprechung mit ggf.
    * vorangestellten Nullen
    *
    * @param zahl Zahl die es gilt umzuwandeln
    * @param stellen Anzahl der Stellen die ggf. mit Nullen aufzufüllen sind
    *
    * @return Generierte Zeichenfolge
  */
  //##########################################################################
  private String wandleIntegerZuString(Integer zahl, Integer stellen)
  {
    return String.format("%0" + stellen + "d", zahl);
  }

  //##########################################################################
  /** Liefert die kleinste mögliche Zahlenfolge der übergebenen Anzahl einer
    * Nummer als Zeichenkette
    *
    * @param stellen Anzahl Stellen der Ziffernfolge
    *
    * @return Generierte Zeichenfolge
  */
  //##########################################################################
  private String getCodeMinimum(Integer stellen)
  {
    String kandidat = new String();

    for (int i = 0; i < stellen; i++)
    {
      kandidat += i;
    }
    
    return kandidat;
  }

  //##########################################################################
  /** Liefert die größte mögliche Zahlenfolge der übergebenen Anzahl einer
    * Nummer als Zeichenkette
    *
    * @param stellen Anzahl Stellen der Ziffernfolge
    *
    * @return Generierte Zeichenfolge
  */
  //##########################################################################
  private String getCodeMaximum(Integer stellen)
  {
    String kandidat = new String();

    for (int i = 9; kandidat.length() < stellen; i--)
    {
      kandidat += i;
    }
    
    return kandidat;
  }

  //##########################################################################
  /** Prüft die übergebene Zeichenkette auf die Einzigartigkeit seiner Symbole
    * und liefert dessen Erfolg
    *
    * @param code Die zu prüfende Zeichenkette
    *
    * @return Erfolg der Prüfung
  */
  //##########################################################################
  private boolean isCodeUnique(String code)
  {
    boolean ergebnis = true;
    
    for (int i = 0; i < code.length(); i++)
    {
      if(0 <= code.indexOf(code.charAt(i), i + 1))
      {
        ergebnis = false;
      }
    }

    return ergebnis;
  }
  
  //##########################################################################
  /** Schreibt den aktuellen Verlauf des Spiel als Liste in die Standardausgabe
  */
  //##########################################################################
  private void schreibeVerlauf()
  {
    System.out.format("\n");
    for (int i = 0; i < verlauf.size(); i++)
    {
      System.out.format("\n%d: %10s an der richtigen Stelle: %d an der falschen Stelle: %d", 
        i + 1, verlauf.get(i), getPositionen(verlauf.get(i)), getVariationen(verlauf.get(i))
      );
    }
    System.out.format("\n");
  }

  //##########################################################################
  /** Liefert die Anzahl der Symbole mit gleicher Position in den übergebenen
    * Zeichenfolgen 
    *
    * @param master Zeichenfolge die als Grundlage dient
    * @param versuch Zeichenfolge die als Vergleich dient
    *
    * @return Anzahl der in beiden enthaltenen Elemente mit gleicher Position
  */
  //##########################################################################
  private Integer getCodePositionen(String master, String versuch)
  {
    Integer ergebnis = 0;

    if (master.length() <= versuch.length())
    {
      for (int i = 0; i < master.length(); i++)
      {
        if (versuch.charAt(i) == master.charAt(i))
        {
          ergebnis++;
        }
      }
    }

    return ergebnis;
  }

  //##########################################################################
  /** Liefert die Anzahl der Symbole mit gleicher Position im Attribut Geheim
    * mit der übergebenen Zeichenfolge
    *
    * @param versuch Zeichenfolge die als Vergleich dient
    *
    * @return Anzahl der in beiden enthaltenen Elemente mit gleicher Position
  */
  //##########################################################################
  public Integer getPositionen(String versuch)
  {
    return getCodePositionen(geheim, versuch);
  }

  //##########################################################################
  /** Liefert die Anzahl der Symbole die in den übergebenen Zeichenfolgen 
    * enthalten sind
    *
    * @param master Zeichenfolge die als Grundlage dient
    * @param versuch Zeichenfolge die als Vergleich dient
    *
    * @return Anzahl der in beiden enthaltenen Elemente ohne gleicher Position
  */
  //##########################################################################
  private Integer getCodeVariationen(String master, String versuch)
  {
    Integer ergebnis = 0;
    
    if (master.length() <= versuch.length())
    {
      for (int i = 0; i < master.length(); i++)
      {
        if (versuch.charAt(i) != master.charAt(i) && versuch.contains(String.valueOf(master.charAt(i))))
        {
          ergebnis++;
        }
      }
    }
    
    return ergebnis;
  }

  //##########################################################################
  /** Liefert die Anzahl der Symbole im Attribut Geheim und der übergebenen 
    * Zeichenfolgen enthalten sind
    *
    * @param versuch Zeichenfolge die als Vergleich dient
    *
    * @return Anzahl der in beiden enthaltenen Elemente ohne gleicher Position
  */
  //##########################################################################
  public Integer getVariationen(String versuch)
  {
    return getCodeVariationen(geheim, versuch);
  }

}

