//======================================================================
// Fachhochschule Ostfriesland, FB E+I
// Dateiname:                RamdomGenerator.java
//======================================================================
// Version:                s.u    aus Distribution.java
// Autor:                  Malte Mueller =>  java "Portierung"
//                         P. Stumfol / G. Totzauer => Original
// Erstellungsdatum:       14.12.93
// 
// Aenderungen:
// Datum   was? - warum?                             Name
//   07.09.94  stark abgewandelt                            G. Totzauer
//   24.10.94  Funktionen Draw() und Discrete()             G. Totzauer
//             hinzugefuegt
//   03.09.95  Funktion Randint() hinzugefuegt              G. Totzauer
//   13.11.95  Beschreibungen der Methoden hinzu-           H. Fasse
//             gefuegt
//   14.05.98  uni() geaendert. Rueckgabewert auf           M. Mueller
//             >= 1.0 getestet.
//   13.03.99  Umformatierungen
//             Wiedererstellung verschwundener 
//             Kommentare                                   G. Totzauer
//   31.03.99  Kommentare fuer javadoc aufbereitet          G. Totzauer
//   10.07.99                                               G. Totzauer
//     Namensanpassung an Java Code Conventions
//   03.05.01  RandomGenerator erweitert Random             G. Totzauer
//   03.05.01  getUni() verwendet Random.nextDouble()       G. Totzauer
//   30.01.08  getRandint() verw. Random.nextInt()          G. Totzauer
//			   getNormal()  verw. 
//							Random.nextGaussian()           G. Totzauer
//======================================================================
// Beschreibung:
//
//   Klasse RandomGenerator zur Erzeugung von Pseudozufallszahlen
//   verschiedener Verteilungen.
//======================================================================
//
// Inhalt: 
//   - RandomGenerator() Konstruktor
//   - getUni()          gleichverteilte Zufallszahl X ~ G[0..1)
//   - getUniform()      stetig gleichverteilte Zufallszahl
//   - getExponential()  Exponentialverteilte Zufallszahl
//   - getErlang()       Erlangverteilte Zufallszahl
//   - getNormal()       Normalverteilte Zufallszahl
//   - get1WithProb()    1 mit Wahrsch. lt. Param, 0 sonst
//   - getDiscrete()     diskret, gemaess Vtlgsfkt im Param
//   - getRandint()      diskret gleichverteilte Zufallszahl
//======================================================================

package processSimulation.random;

import java.util.Random;

/**
 * Die Klasse beinhaltet einen Pseudozufallszahlengenerator und dient 
 * der Erzeugung von Pseudozufallszahlen verschiedener Verteilungen.
 * 
 * @version 1.4 30.01.2008
 * @author Guenter Totzauer
 */
public class RandomGenerator extends Random {
	/**
	 * wird hier angelegt um Warnungen in Eclipse zu vermeiden, haengt damit
	 * zusammen, dass Random Serializable implementiert.
	 */
	private static final long serialVersionUID = 1L;

	public RandomGenerator(long seed) {
		super(seed);
	}

	/**
	 * Ermittelt die naechste gleichverteilte double-Pseudo-Zufallszahl im
	 * Intervall [0..1), die als Basis der Pseudozufallszahlen anderer
	 * Verteilungen verwendet wird.
	 */
	private double getUni() {
		return nextDouble();
	} // getUni()

	/**
	 * zieht eine G[low..high) verteilte Zufallszahl.
	 * 
	 * @param low
	 *            Untergrenze der moeglichen Zahlen - inklusiv
	 * @param high
	 *            Obergrenze der moeglichen Zahlen - exklusiv
	 */
	public double getUniform(double low, double high) {
		return ((high - low) * getUni() + low);
	}

	/**
	 * zieht eine exponential verteilte Zufallszahl.
	 * 
	 * @param mean
	 *            Erwartungswert der Exponentialverteilung
	 */
	public double getExponential(double mean) {
		return (-mean * java.lang.Math.log(getUni()));
	}

	/**
	 * zieht eine Erlang-verteilte Zufallszahl als k-fache Summe
	 * Exponential(mean)-verteilter Zufallszahlen.
	 * 
	 * @param mean
	 *            Erwartungswert der zu Grunde liegenden Exponentialverteilung
	 * @param k
	 *            Anzahl Phasen
	 */
	public double getErlang(double mean, int k) {
		double sum = 0.0;
		for (int i = 0; i < k; i++) {
			sum += getExponential(mean);
		}
		return sum;
	}

	/**
	 * zieht eine normalverteilte Zufallszahl mit gegebenem Wert fuer Mittelwert
	 * und Standardabweichung.
	 * 
	 * @param mean
	 *            Erwartungswert der Normalverteilung
	 * @param stddev
	 *            Standardabweichung der Normalverteilung
	 */
	public double getNormal(double mean, double stddev) {
		return (nextGaussian() * stddev + mean);
	}

	/**
	 * zieht eine 1 gemaess der vorgegebenen Wahrscheinlichkeit und sonst eine
	 * 0.
	 * 
	 * @param prob
	 *            Wahrscheinlichkeit fuer eine 1
	 */
	public int get1WithProb(double prob) {
		if (getUni() <= prob) {
			return 1;
		} else {
			return 0;
		}
	}

	/**
	 * zieht eine Zufallszahl gemaess der uebergebenen diskreten 
	 * Verteilung. Die Schritte sind: 
	 * - ziehe eine Zufallszahl z mit Uni() 
	 * - bestimme den kleinsten Index i mit der Eigenschaft 
	 *    distrib[i] > z
	 * 
	 * @param distrib
	 *            diskrete Verteilungsfunktion
	 */
	public int getDiscrete(double[] distrib) {
		double z;
		int i = 0;

		z = getUni();
		try {
			while (distrib[i] <= z) {
				i++;
			}
		} catch (ArrayIndexOutOfBoundsException oobe) {
			System.out.println(z);
		}
		return (i);
	}

	/**
	 * zieht eine diskrete gleichverteilte Zufallszahl.
	 * 
	 * @param low 		Unterste der moeglichen Zahlen
	 * @param high 		Oberste der moeglichen Zahlen
	 */
	public int getRandint(int low, int high) {
		return nextInt(high - low + 1) + low;
	}
} // end class RandomGenerator


