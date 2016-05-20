package processSimulation.statistic;

/**
 * Implementiert die Chi2-Verteilung.
 * @author Andrea Baldas
 */
public class Chi2Dtr extends Distribution {
	
	/**
	 * Degrees of freedom (Freiheitsgrad)
	 */
	public double df;


	/**
	 * Erzeugt eine Chi2-Verteilung mit dem Freiheitsgrad df.
	 * @param df Degrees of freedom
	 */
	public Chi2Dtr(double df) {

		this.df = df;
	}


	/**
	 * Berechnet die Dichte der Chi2-Verteilung. Die Chi2-Verteilung besitzt
	 * eine Dichte für z &gt; 0. Andernfalls ist die Dichte gleich 0.
	 * <pre>
	 *                 1
	 * K    = ------------------------
	 *  n       (n/2)     -    
	 *         2      *  | (n/2) 
	 *   
	 * 
	 *                n-2           z
	 *                ---        - ---
	 *                 2            2
	 * f(z) = K  *  z       *   e        fuer z > 0
	 *         n    
	 *      
	 *      = 0                          fuer z <= 0 
	 *   
	 *                          -
	 * mit  der Gammafunktion  |       
	 * </pre>
	 * 
	 * @return Dichte
	 * @param z	ein Wert der Zufallsvariablen Z
	 */
	public double denseFunction(double z) {
		
		double k,a,b;
		
		if( z > 0 ) {
			a = df/2;
			k = 1 / (Math.pow(2,a) * Gamma.gamma(a));
			b = (df-2)/2;
			return k * Math.pow(z,b) * Math.exp(-z/2);
		}else {
			return 0;
		}
		
	}


	/**
	 * Returns the area under the left hand tail (from 0 to <tt>x</tt>)
	 * of the Chi square probability density function with
	 * <tt>v</tt> degrees of freedom.
	 * <pre>
	 *                                    x
	 *                                    -
	 *                        1          | |  v/2-1  -t/2
	 *  P( x | v )   =   -----------     |   t      e     dt
	 *                    v/2  -       | |
	 *                   2    | (v/2)   -
	 *                                   0
	 * </pre>
	 * where <tt>x</tt> is the Chi-square variable.
	 * <p>
	 * The incomplete gamma integral is used, according to the
	 * formula
	 * <p>
	 * <tt>y = chiSquare( v, x ) = incompleteGamma( v/2.0, x/2.0 )</tt>.
	 * <p>
	 * The arguments must both be positive.
	 *
	 * @return Area under the left hand tail (from 0 to <tt>x</tt>)
	 * of the Chi square probability density function with
	 * <tt>v</tt> degrees of freedom.
	 * @param x integration end point.
	 */
	/* Copyright Š 1999 CERN - European Organization for Nuclear Research 
	 * veraendert durch Andrea Baldas*/
	public double distFunction(double x) throws ArithmeticException { 
		if( x < 0.0 || df < 1.0 ) return 0.0;
		return Gamma.incompleteGamma( df/2.0, x/2.0 );
	}
	
	

	/**
	 * Inverse Verteilungsfunktion.<br>
	 * @return Wert, <tt>x</tt>, für welchen die Fläche
	 * unter der Chi2-Dichtefunktion (integriert von 0 bis <tt>x</tt>) dem
	 * Wert <tt>alpha</tt> entspricht.
	 * @param alpha 	Irrtumswahrsccheinlichkeit
	 */
	public double invDistFunction(double alpha) throws ArithmeticException { 
		double x;
		if( alpha < 0.0 || df < 1.0 ) return 0.0;
		x = Gamma.incompleteGammaInverse( df/2.0, alpha );
		x = 2*x;
		return x;
	}


	/**
	 * Returns the area under the right hand tail (from <tt>x</tt> to
	 * infinity) of the Chi square probability density function
	 * with <tt>v</tt> degrees of freedom.
	 * <pre>
	 *                                  inf.
	 *                                    -
	 *                        1          | |  v/2-1  -t/2
	 *  P( x | v )   =   -----------     |   t      e     dt
	 *                    v/2  -       | |
	 *                   2    | (v/2)   -
	 *                                   x
	 * </pre>
	 * where <tt>x</tt> is the Chi-square variable.
	 *
	 * The incomplete gamma integral is used, according to the
	 * formula
	 *
	 * <tt>y = chiSquareComplemented( v, x ) = incompleteGammaComplement( v/2.0, x/2.0 )</tt>.
	 *
	 *
	 * The arguments must both be positive.
	 * 
	 * @return Area under the right hand tail (from <tt>x</tt> to
	 * infinity) of the Chi square probability density function
	 * with <tt>v</tt> degrees of freedom.
	 * @param x Anfangspunkt der Integration
	 */
	/* Copyright Š 1999 CERN - European Organization for Nuclear Research 
	 * veraendert durch Andrea Baldas*/
	public double distFunctionComplement(double x) throws ArithmeticException { 
		if( x < 0.0 || df < 1.0 ) return 0.0;
		return Gamma.incompleteGammaComplement( df/2.0, x/2.0 );
	}
	


}

