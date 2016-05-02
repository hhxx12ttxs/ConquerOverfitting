/*############################################################################
  Kodierung: UTF-8 ohne BOM - öäüß
############################################################################*/

//############################################################################
/** Die Klasse Generator dient der Generierung einzelner Zahlen, Symbole und
  * Listen.
  *
  * @author Thomas Gerlach
*/
//############################################################################
public class Generator
{
  private int intMin;
  
  private int intMax;
  
  private double dblMin;
  
  private double dblMax;
  
  //##########################################################################
  /** 
  */
  //##########################################################################
	public Generator(int minimum, int maximum)
  {
    intMin = minimum;
    intMax = maximum;
    if (intMin > intMax)
    {
      int temp = intMin;
      intMin = intMax;
      intMax = temp;
    }
  }
  
  //##########################################################################
  /**
  */
  //##########################################################################
	public Generator(double minimum, double maximum)
  {
    dblMin = minimum;
    dblMax = maximum;
    if (dblMin > dblMax)
    {
      double temp = dblMin;
      dblMin = dblMax;
      dblMax = temp;
    }
  }
  
  //##########################################################################
  /** Erzeugt und liefert eine Ganzzahl im Wertebereich der Attribute min und max
    *
    * @return Generierte Ganzzahl
  */
  //##########################################################################
	public int ganzzahl()
  {
    return ganzzahl(intMin, intMax);
  }

  //##########################################################################
  /** Erzeugt und liefert eine Ganzzahl im Wertebereich von min und max
    *
    * @param min Untere Grenze des Wertebereich
    * @param max Obere Grenze des Wertebereich
    *
    * @return Generierte Ganzzahl
  */
  //##########################################################################
	public int ganzzahl(int min, int max)
  {
    return min + (int)Math.floor(Math.random() * (max - min + 1));
  }

  //##########################################################################
  /** Erzeugt und liefert eine Liste von Ganzzahlen im Wertebereich von den 
    * Attributen min und max
    *
    * @param anzahl Anzahl der Elemente der zu generierenden Liste
    *
    * @return Generierte Liste mit Ganzzahlen
  */
  //##########################################################################
  public int[] listeGanzzahl(int anzahl)
  {
    return listeGanzzahl(anzahl, intMin, intMax);
  }
  
  //##########################################################################
  /** Erzeugt und liefert eine Liste von Ganzzahlen im Wertebereich von min und max
    *
    * @param anzahl Anzahl der Elemente der zu generierenden Liste
    * @param min Untere Grenze des Wertebereich
    * @param max Obere Grenze des Wertebereich
    *
    * @return Generierte Liste mit Ganzzahlen
  */
  //##########################################################################
  public int[] listeGanzzahl(int anzahl, int min, int max)
  {
    int[] ergebnis = new int[anzahl];
    for (int i = 0; i < anzahl; i++)
    {
      ergebnis[i] = ganzzahl(min, max);
    }
    return ergebnis;
  }

  //##########################################################################
  /** Erzeugt und liefert eine Gleitzahl im Wertebereich der Attribute min und max
    *
    * @return Generierte Gleitzahl
  */
  //##########################################################################
	public double gleitzahl()
  {
    return gleitzahl(dblMin, dblMax);
  }

  //##########################################################################
  /** Erzeugt und liefert eine Gleitzahl im Wertebereich von min und max
    *
    * @param min Untere Grenze des Wertebereich
    * @param max Obere Grenze des Wertebereich
    *
    * @return Generierte Gleitzahl
  */
  //##########################################################################
	public double gleitzahl(double min, double max)
  {
    return Math.random() * (dblMax - dblMin) + dblMin;
  }

  //##########################################################################
  /** Erzeugt und liefert eine Liste von Gleitzahlen im Wertebereich der 
    * Attribute min und max
    *
    * @param anzahl Anzahl der Elemente der zu generierenden Liste
    *
    * @return Generierte Liste mit Gleitzahlen
  */
  //##########################################################################
  public double[] listeGleitzahl(int anzahl)
  {
    return listeGleitzahl(anzahl, dblMin, dblMax);
  }
  
  //##########################################################################
  /** Erzeugt und liefert eine Liste von Gleitzahlen im Wertebereich von min und max
    *
    * @param anzahl Anzahl der Elemente der zu generierenden Liste
    * @param min Untere Grenze des Wertebereich
    * @param max Obere Grenze des Wertebereich
    *
    * @return Generierte Liste mit Gleitzahlen
  */
  //##########################################################################
  public double[] listeGleitzahl(int anzahl, double min, double max)
  {
    double[] ergebnis = new double[anzahl];
    for (int i = 0; i < anzahl; i++)
    {
      ergebnis[i] = gleitzahl(min, max);
    }
    return ergebnis;
  }

  //##########################################################################
  /** Erzeugt und liefert eine Liste von Ganzzahlen im Wertebereich von min und max
    * als Zeichenkette
    *
    * @param anzahl Anzahl der Elemente der zu generierenden Liste
    * @param min Untere Grenze des Wertebereich
    * @param max Obere Grenze des Wertebereich
    *
    * @return Generierte Liste mit Symbolen
  */
  //##########################################################################
	public String symboleGanzzahl(int anzahl, int min, int max)
  {
    String ergebnis = new String();
    while (ergebnis.length() < anzahl)
    {
      ergebnis += ganzzahl(min, max);
    }
    return ergebnis;
  }

  //##########################################################################
  /** Erzeugt und liefert eine Liste von Ganzzahlen im Wertebereich von min und max
    * als Zeichenkette deren Elemente einmalig sind
    *
    * @param anzahl Anzahl der Elemente der zu generierenden Liste
    *
    * @return Generierte Liste mit Symbolen
  */
  //##########################################################################
	public String symboleGanzzahlUnikat(int anzahl)
  {
    String ergebnis = new String();
    Integer ziffer = 0;

    while (ergebnis.length() < anzahl)
    {
      ziffer = ganzzahl(0, 9);
      if (!ergebnis.contains(ziffer.toString()))
      {
        ergebnis += ziffer;
      }
    }

    return ergebnis; 
  }  

}
