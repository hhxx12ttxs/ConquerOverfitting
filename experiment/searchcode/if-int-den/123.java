package processSimulation.statistic;


/**
 * Implementiert den Chi2-Test.
 * @author Andrea Baldas
 */
public class Chi2Test extends Test {
	
	/** 
	 * Stichprobe mit den erwarteten Hδufigkeiten
	 */
	public Sample esamp;
	
	
	/**
	 * Erzeugt einen Chi2-Test mit der Irrtumswahrscheinlichkeit alpha.
	 * @param alpha Irrtumswahrscheinlichkeit
	 */
	public Chi2Test(double alpha){
		this.alpha = alpha;
		esamp = new Sample();
	}
	
	
	/******************************************************************
	 * Fόhrt den Chi2 Test durch
	 * @return true wenn die Nullhypothese bestδtigt werden kann,
	 * 				sonst false
	 * @param samp	Stichprobe mit den beobachteten Hδufigkeiten
	 *****************************************************************/
	public boolean calculateTest(Sample samp) {
		// samp enthδlt die beobachteten Hδufigkeiten
		double chisq = 0.0;
		double temp;
		int df = samp.len - 1;
		// Chi2-Verteilung erstellen
		Chi2Dtr chi = new Chi2Dtr(df);
		
		// Prόfgrφίe berechnen
		for(int i=0; i < samp.len; i++){
			if(esamp.arr[i]<0) throw new IllegalArgumentException(
					"Erwartete Haeufigkeit < 0 !!");
			temp = esamp.arr[i] - samp.arr[i];
			System.out.println("temp = " + temp);
			chisq = chisq + temp * temp / esamp.arr[i];
		}
		System.out.println("chisq = "+chisq);
		temp = chi.invDistFunction(alpha);
		System.out.println("kritischer Wert = "+temp);
		if (chisq >= temp)
					return false; // Ho ablehnen
				else 
					return true; // Ho annehmen
		
	}
//*/
	/*
//---------------------------------------------------------------------
//von Christoph Behrends verδndert:
//Der Test wurde neu implementiert (inklusive Klassierung und
//berechneter erwarteter Hδufigkeiten).
//Es entstehen aber vermutlich oft erwartete Hδufigkeiten <= 5,
//dadurch wird der Test aber abgebrochen.
//Folglich bedarf es noch einer άberarbeitung.
//Es kφnnte auch versucht werden, die Bedingung zu implementieren,
//dass fόr mindestens 80% aller Intervalle ei > 5 gilt.
//---------------------------------------------------------------------
		
		int m;	 			//Intervallanzahl
		double b;			//Intervallbreite
		double[] arrIG;		//Intervallgrenzen
		double[] arrHi;		//beobachtete Hδufigkeiten hi pro Intervall
		boolean sameDtr = false;
		
		double mean, variance;		
		
		//Formeln fόr m und b siehe:
		//http://www.faes.de/Basis/Basis-Lexikon/
		//Basis-Lexikon-Klassierung/basis-lexikon-klassierung.html
		m = (int) Math.sqrt(samp.arr.length);
		b = (samp.getMax() - samp.getMin()) / m;
		arrIG = new double[m+1];

		//Intervallgrenzen ins Array eintragen
		arrIG[0] = samp.getMin();
		arrIG[arrIG.length-1] = samp.getMax();
		for (int i = 1; i < arrIG.length-1; i++) {
			arrIG[i] = arrIG[i-1] + b;
		}

		arrHi = new double[m];
		
		//alle Daten durchlaufen
		for (int i = 0; i < samp.arr.length; i++) {
			//und Hδufigkeit im entsprechenden Intervall inkrementieren
			for (int j = 0; j < arrIG.length; j++) {
				if(j == arrIG.length-1){
					arrHi[j-1] += 1.0;
					break;
				}
				if (arrIG[j] <= samp.arr[i]
						&& samp.arr[i] < arrIG[j + 1]) {
					arrHi[j] += 1.0;
					break;
				}
			}
		}

		mean = samp.getMean();
		variance = samp.getVariance();
		NormalDtr ndtr =  new NormalDtr(mean, variance);
		double ei;	//erwartete Hδufigkeit
		double chiq = 0;
		
//		ei = (int)((ndtr.distFunction(arrIG[1])) * samp.getLen());
//		chiq += Math.pow((arrHi[0] - ei), 2) / ei;
		System.out.println("\n\tIntervalle mit beobachteten und " +
				"erwarteten Hδufigkeiten:\n");
//		System.out.printf("\t%1.3f\n", arrIG[0]);
//		System.out.println("\thi = " + arrHi[0] + "\t|\tei = " + ei);
		for (int i = 0; i < arrHi.length; i++) {
			ei = (int) Math.round((ndtr.distFunction(arrIG[i+1]) - 
					ndtr.distFunction(arrIG[i])) * samp.getLen());
			
			chiq += Math.pow((arrHi[i] - ei), 2) / ei;
			System.out.printf("\t%1.3f\n", arrIG[i]);
			System.out.println("\thi = " + arrHi[i] + "\t|\tei = " +
					ei);
			if(ei < 6){
				System.out.println("Abruch: Erwartete Hδufigkeit pro" +
						" Intervall muss grφίer als fόnf sein!");
				return sameDtr;
			}
		}
		System.out.printf("\t%1.3f\n", arrIG[arrIG.length-1]);
		
		//Freiheitsgrad (degree of freedom) = 
		//Intervallanzahl -
		//Anz. aus Stichpr. geschδtzter Parameter (Mittelwert und
		//Varianz, also 2) - 1
		int df = m - 2 - 1;
		Chi2Dtr c2d = new Chi2Dtr(df);
		//kritischer Wert (critical value)
		double cv = c2d.invDistFunction(alpha);

		if (chiq >= cv) {	// Daten entstammen nicht aus angenommener
							// Verteilung -> ho abgelehnt
			sameDtr = false;
		} else { 			// Daten entstammen aus angenommener
							//Verteilung -> ho angenommen
			sameDtr = true;
		}
		return sameDtr;
	}*/
}

