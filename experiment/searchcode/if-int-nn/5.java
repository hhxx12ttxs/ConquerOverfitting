package processSimulation.statistic;

//verδndert am 15.07.2010:
//Als Referenzwert zur Dichotomisierung wird nun der Median (vorher Mittelwert) verwendet

/**
 * Implementiert den One Sample Runs Test.
 * @author Andrea Baldas
 */
public class OSRTest extends Test {

	final static int MAXN  = 40;
	final static int UNDEF = 0;

	static double binom[][] = new double [MAXN+1][MAXN+1];
	static boolean BinomInitialized = false;

	/**
	 * Konstruktor der Klasse OSRTest
	 * @param alpha Irrtumswahrscheinlichkeit
	 */
	public OSRTest(double alpha){
		this.alpha = alpha;
	}
	
	
	/**
	 * Fuehrt den One Sample Runs Test durch um zu bestimmen, ob die Stichprobe
	 * zufaellig ist.
	 * @return true, wenn die Stichprobe zufaellig ist, sonst false
	 * @param samp		Stichprobe
	 */
	@SuppressWarnings("unused")
	public boolean calculateTest(Sample samp){
	
		int i;
		int n1 = 0;
		int n2 = 0;
		int r  = 1;
		

//		// bestimmt den Mittelwert der Stichprobe
//		double m = samp.getMean();

		//bestimmt den Median der Stichprobe
		double m = samp.getMedian();

		// bestimmt + und - der Folge	
		if (samp.arr[0] > m)
			n1 = 1;
		else
			n2 = 1;

		for (i = 1; i < samp.len; i++)
		{
			if (samp.arr[i] > m)
			{
				n1++;
				if (samp.arr[i-1] <= m)
					r++;
			}
			else
			{
				n2++;
				if (samp.arr[i-1] > m)
					r++;
			}
		}

		//System.out.println("Anzahl Runs: "+r);
//		System.out.println("m = " + m + " n1 = " + n1 + " n2 = " + n2 + " r = " + r);
		// n1 oder n2 so klein, dass die Funktionen von Prof. Totzauer verwendet werden kφnnen
		if (n1 <= MAXN/2 && n2 <= MAXN/2)
		{
			if (r > OsrMin(n1,n2,alpha/2) && r < OsrMax(n1,n2,alpha/2))
				return true;
			else
				return false;
		}
		// r ist nδherungsweise normalverteilt
		else //n1 or n2 >= MAXN
		{
			double mean;
			double variance;
			double alpha2;
			double d; // Hilfsvariable
			double ndtr1; // ndtr.invDistFunction(alpha2)
			double ndtr2; // ndtr.invDistFunction(1 - alpha2)
			int n, nn;

			n = n1 + n2;
			nn = 2 * n1 * n2;
			
			// Mittelwert und Varianz bestimmen
			mean = (double)nn / n + 1;
			variance = (double)(nn * (nn - n1 -n2))/(n * n *(n - 1));
			// Hilfsgrφίe erstellen
			d = (r-mean)/Math.sqrt(variance);
//			System.out.println("mean = " + mean + " | var = " + variance + " | d = " + d);
			NormalDtr ndtr = new NormalDtr(0.0,1.0);
			alpha2 = alpha / 2;
			double tmp = ndtr.invDistFunction(alpha2);
			double tmp2 = ndtr.invDistFunction(1 - alpha2);
//			System.out.println("tmp = " + tmp + " | tmp2 = " + tmp2);
			if (d >= tmp &&	d <= tmp2)
				return true; // Stichprobe ist zufδllig
			else
				return false;  // Stichprobe ist nicht zufδllig
		}
	}


//	==============================================================
//	 Fachhochschule Ostfriesland, FB E+I
//	 Dateiname:               OSR.C
//	==============================================================
//	 Version:                 1.0
//	 Autor:                   G. Totzauer
//	 Erstellungsdatum:        03.11.95
//
//	 Aenderungen:
//	 Datum    was? - warum?                          Name
//	 21.2.96  #define MAXN 50 hinzugefuegt           Chad Smith
//			  Es wird benoetigt um festzustellen ob
//			  n1 oder n2 zu gross fuer RunProb sind
//	 Nov 2003 Umschreibung nach Java                 Andrea Baldas
//	==============================================================
//	 Beschreibung:
//	   Funktionen zur Berechnung der Verteilung der Runs
//	   beim One-Sample-Runs-Test
//	==============================================================

/*******************************************************************************
 * Beschreibung:
 *   Initialisiert das Feld binom mit den entsprechenden
 *   Binomialkoefizienten.
 *                 ( n )        n!
 *   binom[n][k] = (   ) = -------------
 *                 ( k )    k! * (n-k)!
 *
 *******************************************************************************/
	static void InitBinom()
	{
		int nn;         // Laufvariable fuer n in obiger Formel
		int kk;         // Laufvariable fuer k in obiger Formel

		/* initialisiere binom */
		for (nn=0; nn<=MAXN; nn++)
		{
			for (kk=0; kk<=MAXN; kk++)
			{
				binom[nn][kk] = 0.0;
			}
		}

		/* initialisiere Randfaelle von binom */
		for (nn=0; nn<=MAXN; nn++)
		{
			binom[nn][0 ] = 1.0;
			binom[nn][nn] = 1.0;
		}

		/* berechne Binomialkoeffizienten */

		for (nn=1; nn<=MAXN; nn++)
		{
			for (kk=1; kk<=nn; kk++)
			{
				binom[nn][kk] = binom[nn-1][kk] + binom[nn-1][kk-1];
			}
		}
		BinomInitialized = true;
	}


/*******************************************************************************
 * Beschreibung:
 *   Anzahl der Moeglichkeiten k Kugeln auf r Faecher zu verteilen
 *   Kombinatorischer Satz: Flachsmeyer: Kombinatorik
 *   Pruefe Plausibilitaet fuer k=1 bzw r=1
 *
 * Kombination:
 * r Kugeln, alle verschieden
 * n Kugeln werden gezogen (mit zuruecklegen)
 * Reihenfolge nicht relevant
 *            (r+k-1)
 * C_w(r;k) = (     )
 *            (  k  )
 *
 * @param k		Anzahl der Kugeln
 * @param r		Anzahl der Faecher
 *******************************************************************************/
	double A(int k, int r)
	{
		double erg;          // fuer Funktionsergebnis

		if (!BinomInitialized)
		{
			InitBinom();
		}
		
		assert (r >= 1) && (k >= 0);

		if ((r >= 1) && (k >= 0))
		{
			erg = binom[k+r-1][k];
		}
		else
		{
			erg = 0;
		}
		return erg;
	}


/*******************************************************************************
 * Beschreibung:
 *   bestimmt die Wahrscheinlichkeit dafuer, dass in einer
 *   Folge mit n1 '+' und n2 '-' genau
 *   r Folgen gleicher Zeichen (runs) auftreten.
 * @param n1	Anzahl der '+' in der Folge
 * @param n2	Anzahl der '-' in der Folge
 * @param r		Anzahl der Runs in der Folge
 *******************************************************************************/
	double RunProb(int n1, int n2, int r)
	{
		double d; // Hilfsgrφίe (Rόckgabewert)
		
		int r1;   // Anzahl der Runs, die mit '+' beginnen
		int r2;   // Anzahl der Runs, die mit '-' beginnen
		
		r1 = r / 2;
		r2 = r - r1;
		
		d = A(n1 - r1, r1) * A(n2 - r2, r2) + A(n1 - r2, r2) * A(n2 - r1, r1);
		d = d / binom[n1 + n2][n1];
		
		return d;
	}



/*******************************************************************************
 * Beschreibung:
 *   bestimmt die groesste Zahl r, fuer die die
 *   Wahrscheinlichkeit, dass es <=r runs gibt
 *   kleiner als a ist.
 *   liefert undef, wenn die Wahrscheinlichkeit fuer die
 *   kleinst moegliche Anzahl runs bereits groesser als a ist.
 * @param n1	Anzahl der '+' in der Folge
 * @param n2	Anzahl der '-' in der Folge
 * @param a		gesuchtes Quantil
 *******************************************************************************/
	public int OsrMin(int n1, int n2, double a )
	{
		int r;           // Laufvariable fuer Anzahl runs
		int rmin;        // Minimale Anzahl runs
		int rmax;        // Maximale Anzahl runs

		double f = 0.0;  // Wert der diskreten Dichte
		double F = 0.0;  // Wert der Verteilungsfunktion

		// bestimme minimale und maximale Anzahl runs
		if ((n1==0) && (n2==0))
		{
			rmin = rmax = 0;
		}
		else if ((n1==0) || (n2==0))
		{
			rmin = rmax = 1;
		}
		else   // Dies stellt den Normalfall dar.
		{
			rmin = 2;
			rmax = 2 * ( n1 < n2 ? n1 : n2 );
			if(n1 != n2)
			{
				rmax = rmax +1;
			}
		}

		// bestimme gesuchtes r
		if ((f = RunProb(n1, n2, rmin)) > a)
		{
			return ( UNDEF );
		}
		else
		{
			for ( r = rmin; r <= rmax; r++ )
			{
				f = RunProb(n1, n2, r);
				F += f;
				if ( F >= a )
				{
					break;
				}
			}
		}
		return (r - 1);

	}



/*******************************************************************************
 * Beschreibung:
 *   bestimmt die kleinste Zahl r, fuer die die
 *   Wahrscheinlichkeit, dass es >=r runs gibt
 *   kleiner als a ist.
 *   liefert undef, wenn die Wahrscheinlichkeit fuer die
 *   groesst moegliche Anzahl runs bereits groesser als a ist.
 * @param n1	Anzahl der '+' in der Folge
 * @param n2	Anzahl der '-' in der Folge
 * @param a		gesuchtes Quantil
 *******************************************************************************/
	public int OsrMax(int n1, int n2, double a )
	{
		int r;           // Laufvariable fuer Anzahl runs
		int rmin;        // Minimale Anzahl runs
		int rmax;        // Maximale Anzahl runs

		double f=0.0;    // Wert der diskreten Dichte
		double F=0.0;    // Summe der Wahrscheinlichkeiten

		// bestimme minimale und maximale Anzahl runs

		if ((n1==0) && (n2==0))
		{
			rmin = rmax = 0;
		}
		else if ((n1==0) || (n2==0))
		{
			rmin = rmax = 1;
		}
		else   // Dies stellt den Normalfall dar.
		{
			rmin = 2;
			rmax = 2 * ( n1 < n2 ? n1 : n2 );
			if (n1 != n2)
			{
				rmax = rmax +1;
			}
		}

		// bestimme gesuchtes r
		if ((f = RunProb(n1, n2, rmax)) > a)
		{
			return ( UNDEF );
		}
		else
		{
			for ( r=rmax; r>=rmin; r-- )
			{
				f = RunProb(n1, n2, r);
				F += f;
				if ( F >= a )
				{
					break;
				}
			}
		}
		return( r + 1 );
	}
}

