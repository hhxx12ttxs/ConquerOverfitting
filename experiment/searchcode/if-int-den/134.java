/*############################################################################
  Kodierung: UTF-8 ohne BOM - üöä
############################################################################*/

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

//############################################################################
/** Repräsentiert den Verkauf von Zeitungen über einen festgelegten Zeitraum
  *
  * @author Thomas Gerlach
*/
//############################################################################
public class Umsatz
{
  private int anzahlStudenten = 0;
  private String sortierung = new String();
  private ArrayList<Student> studenten = new ArrayList<Student>();
  
  //##########################################################################
  /** Initialisiert den Verkauf durch das Einstellen und Entsenden von Studenten
  */
  //##########################################################################
  public Umsatz(int dieAnzahlStudenten)
  {
    anzahlStudenten = dieAnzahlStudenten;
    generiereVerkauf();
  }
    
  //##########################################################################
  /** Erstellt eine Liste von Studenten deren Tätigkeiten es ist Zeitungen zu
    * verkaufen.
  */
  //##########################################################################
  private void generiereVerkauf()
  {
    Namen namen = new Namen();
    for (int i = 0; i < anzahlStudenten; i++) {
      studenten.add(new Student(namen.erzeugeNamen()));
    }
  }
  
  //##########################################################################
  /** Visualisiert den Verkauf der Produkte durch Auflistung der Verkäufer und
    * der Anzahl sowie des Umsatzes alphabetisch sortiert.
    *
    * @param sortierFolge Symbol (+/-) zur Steuerung der Sortierreihenfolge
  */
  //##########################################################################
  public void zeigeTagesschau(String sortierFolge)
  {
    Comparator<Student> comparator = new NamenComparator();
    Collections.sort(studenten, comparator);
    if (sortierFolge.contains("-")) {
      Collections.reverse(studenten);
    }

    Eingabe dialog = new Eingabe();
    double[] summe = new double[6];
    String[] tage = Wochentage.getListeTitel();
    double[] preise = Zeitungen.getListePreis();
    int auswahl = dialog.auswahl("Auswahl Wochentag: ", tage);

    System.out.format("\n-----------------------------------------------------------------");
    System.out.format("\nTAGESUMSATZ");
    System.out.format("\n-----------------------------------------------------------------");
    System.out.format("\n%-20s%40s", tage[auswahl], Listen.format(Zeitungen.getListeTitel(), "          "));
    System.out.format("\n-----------------------------------------------------------------");

    for (Student student : studenten)
    {
      int[] daten = student.getVerkauf(auswahl);
      System.out.format("\n%-20s %5d %8.2f %5d %8.2f %5d %8.2f", 
        student.getName(), 
        daten[0], 
        daten[0] * preise[0], 
        daten[1], 
        daten[1] * preise[1], 
        daten[2], 
        daten[2] * preise[2]
      );
      summe[0] += daten[0];
      summe[1] += daten[0] * preise[0];
      summe[2] += daten[1];
      summe[3] += daten[1] * preise[1];
      summe[4] += daten[2];
      summe[5] += daten[2] * preise[2];
    }

    System.out.format("\n-----------------------------------------------------------------");
    System.out.format("\n%-20s %5.0f %8.2f %5.0f %8.2f %5.0f %8.2f", "Summe", summe[0], summe[1], summe[2], summe[3], summe[4], summe[5]);
    System.out.format("\n");
  }
  
  //##########################################################################
  /** Visualisiert die Gewinne durch den Verkauf der Produkte durch Auflistung 
    * der Verkäufer und Gewinne in sortierter Folge
    *
    * @param sortierFolge Symbol (+/-) zur Steuerung der Sortierreihenfolge
  */
  //##########################################################################
  public void zeigeWochenschau(String sortierFolge)
  {
    double summe = 0;
    Comparator<Student> comparator = new GewinnComparator();
    Collections.sort(studenten, comparator);
    if (sortierFolge.contains("-")) {
      Collections.reverse(studenten);
    }

    System.out.format("\n-----------------------------------------------------------------");
    System.out.format("\nWOCHENUMSATZ");
    System.out.format("\n-----------------------------------------------------------------");
    for (Student student : studenten)
    {
      System.out.format("\n%-20s %8.2f", student.getName(), student.getGewinn());
      summe += student.getGewinn();
    }
    System.out.format("\n-----------------------------------------------------------------");
    System.out.format("\n%-20s %8.2f", "Summe", summe);
    System.out.format("\n");
  }
}
