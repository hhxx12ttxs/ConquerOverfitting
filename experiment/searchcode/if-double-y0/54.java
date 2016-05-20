
package processSimulation.statistic;

/**
 * Implementiert die Normalverteilung.
 * @author Andrea Baldas
 */
public class NormalDtr extends Distribution {

	/**
	 * Mittelwert der Normalverteilung
	 */
	public double mean;
	/**
	 * Varianz der Normalverteilung
	 */
	public double variance;
	
	/*
	 *    COEFFICIENTS FOR METHOD  normalInverse   
	 */
	/* Copyright Š 1999 CERN - European Organization for Nuclear Research */
	/*
	 * approximation for 0 <= |y - 0.5| <= 3/8 
	 */
	protected static final double P0[] = {
		-5.99633501014107895267E1,
		 9.80010754185999661536E1,
		-5.66762857469070293439E1,
		 1.39312609387279679503E1,
		-1.23916583867381258016E0,
		};
	protected static final double Q0[] = {
		/* 1.00000000000000000000E0,*/
		 1.95448858338141759834E0,
		 4.67627912898881538453E0,
		 8.63602421390890590575E1,
		-2.25462687854119370527E2,
		 2.00260212380060660359E2,
		-8.20372256168333339912E1,
		 1.59056225126211695515E1,
		-1.18331621121330003142E0,
		};


	/* Approximation for interval z = sqrt(-2 log y ) between 2 and 8
	 * i.e., y between exp(-2) = .135 and exp(-32) = 1.27e-14.
	 */
	protected static final double P1[] = {
		 4.05544892305962419923E0,
		 3.15251094599893866154E1,
		 5.71628192246421288162E1,
		 4.40805073893200834700E1,
		 1.46849561928858024014E1,
		 2.18663306850790267539E0,
		-1.40256079171354495875E-1,
		-3.50424626827848203418E-2,
		-8.57456785154685413611E-4,
		};
	protected static final double Q1[] = {
		/*  1.00000000000000000000E0,*/
		 1.57799883256466749731E1,
		 4.53907635128879210584E1,
		 4.13172038254672030440E1,
		 1.50425385692907503408E1,
		 2.50464946208309415979E0,
		-1.42182922854787788574E-1,
		-3.80806407691578277194E-2,
		-9.33259480895457427372E-4,
		};

	/* Approximation for interval z = sqrt(-2 log y ) between 8 and 64
	 * i.e., y between exp(-32) = 1.27e-14 and exp(-2048) = 3.67e-890.
	 */
	protected static final double  P2[] = {
		  3.23774891776946035970E0,
		  6.91522889068984211695E0,
		  3.93881025292474443415E0,
		  1.33303460815807542389E0,
		  2.01485389549179081538E-1,
		  1.23716634817820021358E-2,
		  3.01581553508235416007E-4,
		  2.65806974686737550832E-6,
		  6.23974539184983293730E-9,
		};
	protected static final double  Q2[] = {
		/*  1.00000000000000000000E0,*/
		  6.02427039364742014255E0,
		  3.67983563856160859403E0,
		  1.37702099489081330271E0,
		  2.16236993594496635890E-1,
		  1.34204006088543189037E-2,
		  3.28014464682127739104E-4,
		  2.89247864745380683936E-6,
		  6.79019408009981274425E-9,
		};

	
	/**
	 * Erstellt eine Normalverteilung mit dem Mittelwert mean und der 
	 * Varianz variance.
	 * @param mean	arithmetischer Mittelwert der Verteilung
	 * @param variance 	Varianz der Verteilung
	 */
	public NormalDtr(double mean, double variance) throws ArithmeticException {
		
		if(variance == 0){
			throw new ArithmeticException("Variance must not be 0!");
		}else{
			this.mean = mean;
			this.variance = variance;		
		}
	}

	
	/**
	 * Berechnet die Dichte nach der Formel der Gaussschen Normalverteilung, welche
	 * von den Parametern Mittelwert und Standardabweichung abhängt.
	 * <pre>
	 *                                                           2
	 *                                   1 ( x - arith.Mittel  )
	 *                                 - - ( ----------------- )
	 *                  1                2 (      Std.abw.     )
	 * f(x)= --------------------- *  e 
	 *       sqrt(2 pi) * Std.abw.
	 * </pre>
	 * 
	 * @param x	ein Wert der Zufallsvariablen X
	 */
	public double denseFunction(double x) {
		double a,b,z;
		z = (x - mean) / Math.sqrt(variance);
		a = Math.sqrt(2 * Math.PI) * variance;
		b = - Math.pow(z,2) / 2;
		return Math.exp(b) / a;
	}
	
	/**
	 * Returns the area under the Normal (Gaussian) probability density
	 * function, integrated from minus infinity to <tt>x</tt>.
	 * <pre>
	 *                            x
	 *                             -
	 *                   1        | |          2
	 *  normal(x)  = ---------    |    exp( - t /2 ) dt
	 *               sqrt(2pi)  | |
	 *                           -
	 *                          -inf.
	 *
	 *             =  ( 1 + erf(z) ) / 2
	 *             =  erfc(z) / 2
	 * </pre>
	 * where <tt>z = x/sqrt(2)</tt>.
	 * 
	 * Computation is via the functions <tt>errorFunction</tt> and <tt>errorFunctionComplement</tt>.
	 * @return Area under the Normal (Gaussian) probability density
	 * function, integrated from minus infinity to <tt>x</tt>
	 * @param a Endpunkt der Integration
	 */
	/* Copyright Š 1999 CERN - European Organization for Nuclear Research 
	 * veraendert durch Andrea Baldas*/
	public double distFunction(double a) throws ArithmeticException { 
		
		double x, y, z;
//		System.out.println("mean = " + mean + " | var = " + variance);
//		double sigma = Math.sqrt(variance);
//		System.out.println("sigma = " + sigma);
		a = (a-mean) / Math.sqrt(variance);
//System.out.println("a = " + a);
		x = a * SQRTH;
		z = Math.abs(x);
		
		if( z < SQRTH ) y = 0.5 + 0.5 * errorFunction(x);
		else {
			y = 0.5 * errorFunctionComplemented(z);
			if( x > 0 )  y = 1.0 - y;
		}
		
		return y;
	}
	


	/**
	 * Returns the value, <tt>x</tt>, for which the area under the
	 * Normal (Gaussian) probability density function (integrated from
	 * minus infinity to <tt>x</tt>) is equal to the argument <tt>y</tt>; formerly named <tt>ndtri</tt>.
	 * <p>
	 * For small arguments <tt>0 < y < exp(-2)</tt>, the program computes
	 * <tt>z = sqrt( -2.0 * log(y) )</tt>;  then the approximation is
	 * <tt>x = z - log(z)/z  - (1/z) P(1/z) / Q(1/z)</tt>.
	 * There are two rational functions P/Q, one for <tt>0 < y < exp(-32)</tt>
	 * and the other for <tt>y</tt> up to <tt>exp(-2)</tt>. 
	 * For larger arguments,
	 * <tt>w = y - 0.5</tt>, and  <tt>x/sqrt(2pi) = w + w**3 R(w**2)/S(w**2))</tt>.
	 * @return value, <tt>x</tt>, for which the area under the
	 * Normal (Gaussian) probability density function (integrated from
	 * minus infinity to <tt>x</tt>) is equal to the argument <tt>y</tt>
	 * @param y0 area under the Normal (Gaussian) probability density function
	 */
	/* Copyright Š 1999 CERN - European Organization for Nuclear Research 
	 * veraendert durch Andrea Baldas*/
	public double invDistFunction(double y0) throws ArithmeticException { 
		double x, y, z, y2, x0, x1;
		int code;

		final double s2pi = Math.sqrt(2.0*Math.PI);

		y0 = (y0-mean) / Math.sqrt(variance);

		if( y0 <= 0.0 ) throw new IllegalArgumentException("NormalDtr.invDistFunction: Wert kleiner als 0");
		if( y0 >= 1.0 ) throw new IllegalArgumentException("NormalDtr.invDistFunction: Wert groesser als 1");
		code = 1;
		y = y0;
		if( y > (1.0 - 0.13533528323661269189) ) { /* 0.135... = exp(-2) */
			y = 1.0 - y;
			code = 0;
		}

		if( y > 0.13533528323661269189 ) {
			y = y - 0.5;
			y2 = y * y;
			x = y + y * (y2 * Polynomial.polevl( y2, P0, 4)/Polynomial.p1evl( y2, Q0, 8 ));
			x = x * s2pi; 
			return(x);
		}

		x = Math.sqrt( -2.0 * Math.log(y) );
		x0 = x - Math.log(x)/x;

		z = 1.0/x;
		if( x < 8.0 ) /* y > exp(-32) = 1.2664165549e-14 */
			x1 = z * Polynomial.polevl( z, P1, 8 )/Polynomial.p1evl( z, Q1, 8 );
		else
			x1 = z * Polynomial.polevl( z, P2, 8 )/Polynomial.p1evl( z, Q2, 8 );
		x = x0 - x1;
		if( code != 0 )
			x = -x;
		return( x );
	}
	

	/**
	 * Returns the error function of the normal distribution; formerly named <tt>erf</tt>.
	 * The integral is
	 * <pre>
	 *                           x 
	 *                            -
	 *                 2         | |          2
	 *   erf(x)  =  --------     |    exp( - t  ) dt.
	 *              sqrt(pi)   | |
	 *                          -
	 *                           0
	 * </pre>
	 * <b>Implementation:</b>
	 * For <tt>0 <= |x| < 1, erf(x) = x * P4(x**2)/Q5(x**2)</tt>; otherwise
	 * <tt>erf(x) = 1 - erfc(x)</tt>.
	 * <p>
	 * Code adapted from the <A HREF="http://www.sci.usq.edu.au/staff/leighb/graph/Top.html">Java 2D Graph Package 2.4</A>,
	 * which in turn is a port from the <A HREF="http://people.ne.mediaone.net/moshier/index.html#Cephes">Cephes 2.2</A> Math Library (C).
	 * @return the error function of the normal distribution
	 * @param x the argument to the function.
	 */
	/* Copyright Š 1999 CERN - European Organization for Nuclear Research */
	static public double errorFunction(double x) throws ArithmeticException { 
		double y, z;
		final double T[] = {
						 9.60497373987051638749E0,
						 9.00260197203842689217E1,
						 2.23200534594684319226E3,
						 7.00332514112805075473E3,
						 5.55923013010394962768E4
						};
		final double U[] = {
					   //1.00000000000000000000E0,
						 3.35617141647503099647E1,
						 5.21357949780152679795E2,
						 4.59432382970980127987E3,
						 2.26290000613890934246E4,
						 4.92673942608635921086E4
						};
	
		if( Math.abs(x) > 1.0 ) return( 1.0 - errorFunctionComplemented(x) );
		z = x * x;
		y = x * Polynomial.polevl( z, T, 4 ) / Polynomial.p1evl( z, U, 5 );
		return y;
	}
	
	
	/**
	 * Returns the complementary Error function of the normal distribution; formerly named <tt>erfc</tt>.
	 * <pre>
	 *  1 - erf(x) =
	 *
	 *                           inf. 
	 *                             -
	 *                  2         | |          2
	 *   erfc(x)  =  --------     |    exp( - t  ) dt
	 *               sqrt(pi)   | |
	 *                           -
	 *                            x
	 * </pre>
	 * <b>Implementation:</b>
	 * For small x, <tt>erfc(x) = 1 - erf(x)</tt>; otherwise rational
	 * approximations are computed.
	 * <p>
	 * Code adapted from the <A HREF="http://www.sci.usq.edu.au/staff/leighb/graph/Top.html">Java 2D Graph Package 2.4</A>,
	 * which in turn is a port from the <A HREF="http://people.ne.mediaone.net/moshier/index.html#Cephes">Cephes 2.2</A> Math Library (C).
	 * @return complementary Error function of the normal distribution
	 * @param a the argument to the function.
	 */
	/* Copyright Š 1999 CERN - European Organization for Nuclear Research */
	static public double errorFunctionComplemented(double a) throws ArithmeticException { 
		double x,y,z,p,q;
	
		double P[] = {
						 2.46196981473530512524E-10,
						 5.64189564831068821977E-1,
						 7.46321056442269912687E0,
						 4.86371970985681366614E1,
						 1.96520832956077098242E2,
						 5.26445194995477358631E2,
						 9.34528527171957607540E2,
						 1.02755188689515710272E3,
						 5.57535335369399327526E2
						};
		double Q[] = {
						//1.0
						  1.32281951154744992508E1,
						  8.67072140885989742329E1,
						  3.54937778887819891062E2,
						  9.75708501743205489753E2,
						  1.82390916687909736289E3,
						  2.24633760818710981792E3,
						  1.65666309194161350182E3,
						  5.57535340817727675546E2
						 };
	
		double R[] = {
						  5.64189583547755073984E-1,
						  1.27536670759978104416E0,
						  5.01905042251180477414E0,
						  6.16021097993053585195E0,
						  7.40974269950448939160E0,
						  2.97886665372100240670E0
						 };
		double S[] = {
						//1.00000000000000000000E0, 
						  2.26052863220117276590E0,
						  9.39603524938001434673E0,
						  1.20489539808096656605E1,
						  1.70814450747565897222E1,
						  9.60896809063285878198E0,
						  3.36907645100081516050E0
						 };
	
		if( a < 0.0 )   x = -a;
		else            x = a;
	
		if( x < 1.0 )   return 1.0 - errorFunction(a);
	
		z = -a * a;
	
		if( z < -MAXLOG ) {
			 if( a < 0 )  return( 2.0 );
			 else         return( 0.0 );
		}
	
		z = Math.exp(z);
	
		if( x < 8.0 ) {
		  p = Polynomial.polevl( x, P, 8 );
		  q = Polynomial.p1evl( x, Q, 8 );
		} else {
		  p = Polynomial.polevl( x, R, 5 );
		  q = Polynomial.p1evl( x, S, 6 );
		}
	
		y = (z * p)/q;
	
		if( a < 0 ) y = 2.0 - y;
	
		if( y == 0.0 ) {
				if( a < 0 ) return 2.0;
				else        return( 0.0 );
		 }
	
		return y;
	}

}

