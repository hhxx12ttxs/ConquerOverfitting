/*############################################################################
  Kodierung: UTF-8 ohne BOM - üöä
############################################################################*/

import java.util.Arrays;

//############################################################################
/** Enthält diverse Methoden zur Behandlung von Arrays
  *
  * @author Thomas Gerlach
*/
//############################################################################
public class Listen
{

  //############################################################################
  /**
  */
  //############################################################################
  private Listen()
  {
  }

  //############################################################################
  /** Prüft ob ein Wert in der Liste enthalten ist
    *
    * @param array Liste die zu durchsuchen ist
    * @param value Wert der zu finden ist
    *
    * @return TRUE / FALSE als Entsprechung über den Fund
  */
  //############################################################################
  public static boolean isMember(int[] array, int value)
  {
    boolean ergebnis = false;
    for (int element : array)
    {
      if (element == value)
      {
        ergebnis = true;
      }
    }
    return ergebnis;
  }

  //############################################################################
  /** Prüft ob ein Wert in der Liste enthalten ist
    *
    * @param array Liste die zu durchsuchen ist
    * @param value Wert der zu finden ist
    *
    * @return TRUE / FALSE als Entsprechung über den Fund
  */
  //############################################################################
  public static boolean isMember(double[] array, double value)
  {
    boolean ergebnis = false;
    for (double element : array)
    {
      if (element == value)
      {
        ergebnis = true;
      }
    }
    return ergebnis;
  }

  //############################################################################
  /** Prüft ob ein Wert in der Liste enthalten ist
    *
    * @param array Liste die zu durchsuchen ist
    * @param value Wert der zu finden ist
    *
    * @return TRUE / FALSE als Entsprechung über den Fund
  */
  //############################################################################
  public static boolean isMember(String[] array, String value)
  {
    boolean ergebnis = false;
    for (String element : array)
    {
      if (element.equals(value))
      {
        ergebnis = true;
      }
    }
    return ergebnis;
  }
  
  //############################################################################
  /** Liefert den kleinsten Wert der übergeben Liste
    *
    * @param array Die zu durchsuchende Liste
    *
    * @return Kleinster Wert der Liste
  */
  //############################################################################
  public static int minimum(int[] array)
  {
    int ergebnis = 0;
    
    if (0 < array.length)
    {
      ergebnis = array[0];
      for (int i = 0; i < array.length; i++)
      {
        if (array[i] < ergebnis)
          ergebnis = array[i];
      }
    } else {
    }
    
    return ergebnis;
  }

  //############################################################################
  /** Liefert den kleinsten Wert der übergeben Liste
    *
    * @param array Die zu durchsuchende Liste
    *
    * @return Kleinster Wert der Liste
  */
  //############################################################################
  public static double minimum(double[] array)
  {
    double ergebnis = 0;
    
    if (0 < array.length)
    {
      ergebnis = array[0];
      for (int i = 0; i < array.length; i++)
      {
        if (array[i] < ergebnis)
          ergebnis = array[i];
      }
    } else {
    }
    
    return ergebnis;
  }

  //############################################################################
  /** Liefert den größten Wert der übergeben Liste
    *
    * @param array Die zu durchsuchende Liste
    *
    * @return Größter Wert der Liste
  */
  //############################################################################
  public static int maximum(int[] array)
  {
    int ergebnis = 0;
    
    if (0 < array.length)
    {
      ergebnis = array[0];
      for (int i = 0; i < array.length; i++)
      {
        if (array[i] > ergebnis)
          ergebnis = array[i];
      }
    } else {
    }
    
    return ergebnis;
  }

  //############################################################################
  /** Liefert den größten Wert der übergeben Liste
    *
    * @param array Die zu durchsuchende Liste
    *
    * @return Größter Wert der Liste
  */
  //############################################################################
  public static double maximum(double[] array)
  {
    double ergebnis = 0;
    
    if (0 < array.length)
    {
      ergebnis = array[0];
      for (int i = 0; i < array.length; i++)
      {
        if (array[i] > ergebnis)
          ergebnis = array[i];
      }
    } else {
    }
    
    return ergebnis;
  }

  //############################################################################
  /** Verbindet alle Elemente der übergebenen Liste mit einer Zeichenkette und
    * liefert das Ergebnis als Zeichenkette zurück
    *
    * @param array Liste von Elementen
    * @param kleber Zeichenkette als Verbinder
    *
    * @return Zusammengesetzte Zeichenkette
  */
  //############################################################################
  public static String format(int[] array, String kleber)
  {
    String ergebnis = new String();

    for (int i = 0; i < array.length; i++)
    {
      ergebnis += String.format("%d%s", array[i], (i + 1 != array.length) ? ", " : "");
    }

    return ergebnis;
  }

  //############################################################################
  /** Verbindet alle Elemente der übergebenen Liste mit einer Zeichenkette und
    * liefert das Ergebnis als Zeichenkette zurück
    *
    * @param array Liste von Elementen
    * @param kleber Zeichenkette als Verbinder
    *
    * @return Zusammengesetzte Zeichenkette
  */
  //############################################################################
  public static String format(double[] array, String kleber)
  {
    String ergebnis = new String();

    for (int i = 0; i < array.length; i++)
    {
      ergebnis += String.format("%.2f%s", array[i], (i + 1 != array.length) ? ", " : "");
    }

    return ergebnis;
  }

  //############################################################################
  /** Verbindet alle Elemente der übergebenen Liste mit einer Zeichenkette und
    * liefert das Ergebnis als Zeichenkette zurück
    *
    * @param array Liste von Elementen
    * @param kleber Zeichenkette als Verbinder
    *
    * @return Zusammengesetzte Zeichenkette
  */
  //############################################################################
  public static String format(String[] array, String kleber)
  {
    String ergebnis = new String();

    for (int i = 0; i < array.length; i++)
    {
      ergebnis = ergebnis.concat(array[i]).concat((i < array.length - 1) ? kleber : "");
    }

    return ergebnis;
  }
}
