/*############################################################################
  Kodierung: UTF-8 ohne BOM - üöä
############################################################################*/

//############################################################################
/** Repräsentiert einen Studenten als Hilfskraft für den Verkauf von Zeitungen
  *
  * @author Thomas Gerlach
*/
//############################################################################
public class Student
{
  private String name = new String();
  private int anzahlTage;
  private int anzahlProdukte;
  private double gewinn;
  private int[][] verkauf = null;
  
  //##########################################################################
  /** Initialisiert den Studenten
  */
  //##########################################################################
  public Student(String derName)
  {
    name = derName;
    anzahlTage = 7;
    gewinn = 0;
    anzahlProdukte = Zeitungen.values().length;
    verkauf = verkaufen();
    gewinn = kalkulieren();
  }
  
  //##########################################################################
  /** Liefert den Wert des Attribut name
    *
    * @return Name des Studenten
  */
  //##########################################################################
  public String getName()
  {
    return name;
  }

  //##########################################################################
  /** Führt den Verkauf der Produkte für die Anzahl der Tage durch und liefert
    * das Ergebnis als zweidimensionle Liste. Die Liste ist strukturiert nach 
    * Tagen mit Anzahl Verkauf der Produkte.
    *
    * @return Ergebnis des Verkaufsvorganges
  */
  //##########################################################################
  private int[][] verkaufen()
  {
    int[][] statistik = new int[anzahlTage][anzahlProdukte];
    Generator zufall = new Generator(15, 75);
    for (int i = 0; i < anzahlTage; i++)
    {
      statistik[i] = zufall.listeGanzzahl(anzahlProdukte);
    }
    return statistik;
  }

  //##########################################################################
  /** Berechnet und liefert den Gewinn des Studenten anhand der Anzahl der 
    * verkauften Produkte, deren Preise und deren Provision.
    *
    * @return Gewinn als Summe der Provisionen aller verkauften Exemplare
  */
  //##########################################################################
  private double kalkulieren()
  {
    double summe = 0;
    double[] preise = Zeitungen.getListePreis();
    double[] anteil = Zeitungen.getListeAnteil();
    for (int i = 0; i < verkauf.length; i++)
    {
      for (int j = 0; j < verkauf[i].length; j++)
      {
        summe += verkauf[i][j] * preise[j] * anteil[j];
      }
    }
    return summe;
  }
  
  //##########################################################################
  /** Liefert die Verkaufszahlen der Produkte über die gesamte Laufzeit (Tage)
    *
    * @return Struktur der Verkäufe über den gesamten Zeitraum
  */
  //##########################################################################
  public int[][] getVerkauf()
  {
    return verkauf;
  }

  //##########################################################################
  /** Liefert die Verkaufszahlen der Produkte für einen bestimmten Tag
  */
  //##########################################################################
  public int[] getVerkauf(int tag)
  {
    int[] ergebnis = null;
    if (0 <= tag && tag < verkauf.length)
    {
      ergebnis = verkauf[tag];
    }
    return ergebnis;
  }

  //##########################################################################
  /** Liefert den Wert des Attribut gewinn
    *
    * @return Gewinn aus dem Verkauf der Produkte
  */
  //##########################################################################
  public double getGewinn()
  {
    return gewinn;
  }
    
}
