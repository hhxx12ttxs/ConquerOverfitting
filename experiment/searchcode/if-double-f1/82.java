package processSimulation.statistic;

/**
 * Implementiert die Studentsche t-Verteilung.
 * @author Andrea Baldas
 */
public class TDtr extends Distribution {

	/**
	 * Degrees of freedom (Freiheitsgrad)
	 */
	public double df;

	
	/**
	 * Erstellt eine t-Verteilung mit dem Freiheitsgrad df.
	 * @param df Degrees of freedom
	 */
	public TDtr(double df){
	
		this.df = df;
	}
	
	/**
	 * Berechnet die Dichte. 
	 * <pre>
	 *             -    
	 *            | ((n+1)/2)
	 * B    = ----------------------
	 *  n       -
	 *         | (n/2) sqrt(n * pi)
	 * 
	 *                      1
	 * f(t) = B  *  -------------------
	 *         n                  n+1
	 *                            ---
	 *                      2      2
	 *               ( 1 + t / n )
	 *           
	 * </pre>
	 * 
	 * @return Dichte
	 * @param t	ein Wert der Zufallsvariablen T
	 */
	public double denseFunction(double t) {
		
		double b,c,d;
		
		c = (df+1)/2;
		b = Gamma.gamma(c) / (Gamma.gamma(df/2) * Math.sqrt(df*Math.PI));
		d = 1 + Math.pow(t,2)/df;
		return b / (Math.pow(d,c));		
	}


	/**
	 * Returns the integral from minus infinity to <tt>t</tt> of the Student-t 
	 * distribution with <tt>k &gt; 0</tt> degrees of freedom.
	 * <pre>
	 *                                      t
	 *                                      -
	 *                                     | |
	 *              -                      |         2   -(k+1)/2
	 *             | ( (k+1)/2 )           |  (     x   )
	 *       ----------------------        |  ( 1 + --- )        dx
	 *                     -               |  (      k  )
	 *       sqrt( k pi ) | ( k/2 )        |
	 *                                   | |
	 *                                    -
	 *                                   -inf.
	 * </pre>
	 * Relation to incomplete beta integral:
	 * <p>
	 * <tt>1 - studentT(k,t) = 0.5 * Gamma.incompleteBeta( k/2, 1/2, z )</tt>
	 * where <tt>z = k/(k + t**2)</tt>.
	 * <p>
	 * Since the function is symmetric about <tt>t=0</tt>, the area under the
	 * right tail of the density is found by calling the function
	 * with <tt>-t</tt> instead of <tt>t</tt>.
	 * @return Integral from minus infinity to <tt>t</tt> of the Student-t 
	 * distribution with <tt>k &gt; 0</tt> degrees of freedom.
	 * @param t integration end point.
	 */
	/* Copyright Š 1999 CERN - European Organization for Nuclear Research 
	 * veraendert durch Andrea Baldas*/
	public double distFunction(double t) throws ArithmeticException { 
		if( df <= 0 ) throw new IllegalArgumentException();
		if( t == 0 ) return( 0.5 );
	
		double cdf = 0.5 * Gamma.incompleteBeta( 0.5*df, 0.5, df / (df + t * t) );
	
		if (t >= 0) cdf = 1.0 - cdf; // fixes bug reported by stefan.bentink@molgen.mpg.de
	 
		return cdf;
	}
	
	/**
	 * Returns the value, <tt>t</tt>, for which the area under the
	 * Student-t probability density function (integrated from
	 * minus infinity to <tt>t</tt>) is equal to <tt>1-alpha/2</tt>.
	 * The value returned corresponds to usual Student t-distribution lookup
	 * table for <tt>t<sub>alpha[size]</sub></tt>.
	 * <p>
	 * The function uses the studentT function to determine the return
	 * value iteratively.
	 * @return Value, <tt>t</tt>, for which the area under the
	 * Student-t probability density function (integrated from
	 * minus infinity to <tt>t</tt>) is equal to <tt>1-alpha/2</tt>.
	 * @param alpha probability
	 */
	/* Copyright Š 1999 CERN - European Organization for Nuclear Research 
	 * veraendert durch Andrea Baldas*/
	public double invDistFunction(double alpha) {

		 double cumProb = 1-alpha/2; // Cumulative probability
		 double f1,f2,f3;
		 double x1,x2,x3;
		 double g,s12;

		 NormalDtr ni = new NormalDtr(0,1);
		 x1 = ni.invDistFunction(cumProb);

		 // Return inverse of normal for large size
		 if (df > 200) {
			return x1;
		 }
		 // Find a pair of x1,x2 that braket zero
		 f1 = distFunction(x1)-cumProb;
		 x2 = x1; f2 = f1;
		 do {
			if (f1>0) {
			   x2 = x2/2;
			} else {
			   x2 = x2+x1;
			}
			f2 = distFunction(x2)-cumProb;
		 } while (f1*f2>0);


		 // Find better approximation
		 // Pegasus-method
		 do {
			// Calculate slope of secant and t value for which it is 0.
			s12 = (f2-f1)/(x2-x1);
			x3 = x2 - f2/s12;

			// Calculate function value at x3
			f3 = distFunction(x3)-cumProb;
			if (Math.abs(f3)<1e-8) { // This criteria needs to be very tight!
			   // We found a perfect value -> return
			   return x3;
			}

			if (f3*f2<0) {
			   x1=x2; f1=f2;
			   x2=x3; f2=f3;
			} else {
			   g = f2/(f2+f3);
			   f1=g*f1;
			   x2=x3; f2=f3;
			}
		 } while(Math.abs(x2-x1)>0.001);

		 if (Math.abs(f2)<=Math.abs(f1)) {
			return x2;
		 } else {
			return x1;
		 }
	}

}

