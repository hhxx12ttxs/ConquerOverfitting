package processSimulation.statistic;

//Δnderung (16.07.10):
//Sortierung der Stichprobe mit java.util.Arrays.sort() statt sample.shellSort()

/**
 * Implementiert den Kolmogorov-Smirnov Test.
 * @author Andrea Baldas
 */
public class KSTest extends Test {
	
	/**
	 * Verteilung
	 */
	public Distribution dtr;
	
	
	/**
	 * Erstellt einen Kolmogorov-Smirnov-Test mit der Irrtumswahrscheinlichkeit alpha
	 * und der Verteilung dtr.
	 * @param alpha	Irrtumswahrscheinlichkeit
	 * @param dtr	Verteilung
	 */
	public KSTest(double alpha, Distribution dtr){
		this.alpha = alpha;
		this.dtr = dtr;
	}
	
	
	/**
	 * Fόhrt den Kolmogorov-Smirnov (one-sample) Test durch
	 * @return true, wenn die Stichprobe mit der Irrtumswahrscheinlichkeit alpha
	 * aus der Verteilung dtr stammt, sonst false
	 * @param samp	Stichprobe
	 */
	public boolean calculateTest(Sample samp){

		int j;
		double fo = 0.0, fn, ff;
		double D, dt, dt1, dt2;
		double en, h;
		double[] temp = samp.arr.clone();

		java.util.Arrays.sort(temp);
		
		D = 0;
		
		for (j = 0; j < temp.length; j++)
		{
			h = j + 1;
			fn = h / temp.length;
			ff = dtr.distFunction(temp[j]);
			dt1 = Math.abs(fo - ff);
			dt2 = Math.abs(fn - ff);
			if(dt1 > dt2){
				dt = dt1;
			}else{
				dt = dt2;
			}
			if (dt > D){
				D = dt;
			}
//			System.out.println(" j=" +j+ " h=" +h+ " fo=" +fo+ " ff=" +ff);
//			System.out.println("dt1=" +dt1+ " dt2=" +dt2+ " D=" +D);
			fo = fn;
		}
		en = Math.sqrt(temp.length);
//		double tmp1 = en+0.12+0.11/en;
		h = ProbKS((en+0.12+0.11/en) * D);
//		System.out.printf("kritischer Wert = %1.4f | tmp1 = %1.4f \n", h, tmp1);
		if (h < alpha)
			return false; // ist nicht von der Verteilung
		else 
			return true; // ist von der Verteilung
	}
	


	/*******************************************************************************
	 * Berechnet den kritischen Wert fόr D. Hierfόr wird eine Approximation,
	 * die von der Grφίe der Stichprobe abhδngt, verwendet. S. "Numerical Recipes in C".
	 * @return kritischer Wert
	 * @param alam	(sqrt(n)+0.12+0.11/sqrt(n))*D
	 *******************************************************************************/
	double ProbKS(double alam){
		
		final double EPS1 = 0.001;
		final double EPS2 = 1.0e-8;
		int j;
		double a2, fac = 2, sum = 0.0, term, termbf = 0.0;
    
		a2 = -2 * alam * alam;
		for (j = 1; j <= 100; j++)
		{
			term = fac * Math.exp(a2 * j * j);
			sum += term;
			if (Math.abs(term) <= EPS1 * termbf || Math.abs(term) <= EPS2 * sum)
				return sum;
			fac = -fac;
			termbf = Math.abs(term);
		}
		return 1.0;
	}
}

