<<<<<<< HEAD
/**
 * -------------------------------------------------------------------------
 *  $Id: Sfun.java,v 1.1.1.1 2005/06/06 07:43:35 Administrator Exp $
 * -------------------------------------------------------------------------
 * Copyright (c) 1997 - 1998 by Visual Numerics, Inc. All rights reserved.
 *
 * Permission to use, copy, modify, and distribute this software is freely
 * granted by Visual Numerics, Inc., provided that the copyright notice
 * above and the following warranty disclaimer are preserved in human
 * readable form.
 *
 * Because this software is licenses free of charge, it is provided
 * "AS IS", with NO WARRANTY.  TO THE EXTENT PERMITTED BY LAW, VNI
 * DISCLAIMS LEVEL_ALL WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO ITS PERFORMANCE, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 * VNI WILL NOT BE LIABLE FOR ANY DAMAGES WHATSOEVER ARISING OUT OF THE USE
 * OF OR INABILITY TO USE THIS SOFTWARE, INCLUDING BUT NOT LIMITED TO DIRECT,
 * INDIRECT, SPECIAL, CONSEQUENTIAL, PUNITIVE, AND EXEMPLARY DAMAGES, EVEN
 * IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * -------------------------------------------------------------------------
 */

package me.zford.jobs.resources.jfep;

/**
 *	Collection of special functions.
 */
public class Sfun {
	/** The smallest relative spacing for doubles.*/
	public final static double EPSILON_SMALL = 1.1102230246252e-16;

	/** The largest relative spacing for doubles. */
	public final static double EPSILON_LARGE = 2.2204460492503e-16;


	// Series on [0,0.0625]
	private static final double COT_COEF[] = {.240259160982956302509553617744970e+0, -.165330316015002278454746025255758e-1, -.429983919317240189356476228239895e-4, -.159283223327541046023490851122445e-6, -.619109313512934872588620579343187e-9, -.243019741507264604331702590579575e-11, -.956093675880008098427062083100000e-14, -.376353798194580580416291539706666e-16, -.148166574646746578852176794666666e-18};

	// Series on the interval [0,1]
	private static final double SINH_COEF[] = {0.1730421940471796, 0.08759422192276048, 0.00107947777456713, 0.00000637484926075, 0.00000002202366404, 0.00000000004987940, 0.00000000000007973, 0.00000000000000009};

	// Series on [0,1]
	private static final double TANH_COEF[] = {-.25828756643634710, -.11836106330053497, .009869442648006398, -.000835798662344582, .000070904321198943, -.000006016424318120, .000000510524190800, -.000000043320729077, .000000003675999055, -.000000000311928496, .000000000026468828, -.000000000002246023, .000000000000190587, -.000000000000016172, .000000000000001372, -.000000000000000116, .000000000000000009};

	// Series on the interval [0,1]
	private static final double ASINH_COEF[] = {-.12820039911738186343372127359268e+0, -.58811761189951767565211757138362e-1, .47274654322124815640725249756029e-2, -.49383631626536172101360174790273e-3, .58506207058557412287494835259321e-4, -.74669983289313681354755069217188e-5, .10011693583558199265966192015812e-5, -.13903543858708333608616472258886e-6, .19823169483172793547317360237148e-7, -.28847468417848843612747272800317e-8, .42672965467159937953457514995907e-9, -.63976084654366357868752632309681e-10, .96991686089064704147878293131179e-11, -.14844276972043770830246658365696e-11, .22903737939027447988040184378983e-12, -.35588395132732645159978942651310e-13, .55639694080056789953374539088554e-14, -.87462509599624678045666593520162e-15, .13815248844526692155868802298129e-15, -.21916688282900363984955142264149e-16, .34904658524827565638313923706880e-17};

	// Series on the interval [0,0.25]
	private static final double ATANH_COEF[] = {.9439510239319549230842892218633e-1, .4919843705578615947200034576668e-1, .2102593522455432763479327331752e-2, .1073554449776116584640731045276e-3, .5978267249293031478642787517872e-5, .3505062030889134845966834886200e-6, .2126374343765340350896219314431e-7, .1321694535715527192129801723055e-8, .8365875501178070364623604052959e-10, .5370503749311002163881434587772e-11, .3486659470157107922971245784290e-12, .2284549509603433015524024119722e-13, .1508407105944793044874229067558e-14, .1002418816804109126136995722837e-15, .6698674738165069539715526882986e-17, .4497954546494931083083327624533e-18};

	// Series on the interval [0,1]
	private static final double GAMMA_COEF[] = {.8571195590989331421920062399942e-2, .4415381324841006757191315771652e-2, .5685043681599363378632664588789e-1, -.4219835396418560501012500186624e-2, .1326808181212460220584006796352e-2, -.1893024529798880432523947023886e-3, .3606925327441245256578082217225e-4, -.6056761904460864218485548290365e-5, .1055829546302283344731823509093e-5, -.1811967365542384048291855891166e-6, .3117724964715322277790254593169e-7, -.5354219639019687140874081024347e-8, .9193275519859588946887786825940e-9, -.1577941280288339761767423273953e-9, .2707980622934954543266540433089e-10, -.4646818653825730144081661058933e-11, .7973350192007419656460767175359e-12, -.1368078209830916025799499172309e-12, .2347319486563800657233471771688e-13, -.4027432614949066932766570534699e-14, .6910051747372100912138336975257e-15, -.1185584500221992907052387126192e-15, .2034148542496373955201026051932e-16, -.3490054341717405849274012949108e-17, .5987993856485305567135051066026e-18, -.1027378057872228074490069778431e-18};

	//	Series for the interval [0,0.01]
	private static final double R9LGMC_COEF[] = {.166638948045186324720572965082e0, -.138494817606756384073298605914e-4, .981082564692472942615717154749e-8, -.180912947557249419426330626672e-10, .622109804189260522712601554342e-13, -.339961500541772194430333059967e-15, .268318199848269874895753884667e-17};

	// Series on [-0.375,0.375]
	final private static double ALNRCS_COEF[] = {.103786935627437698006862677191e1, -.133643015049089180987660415531, .194082491355205633579261993748e-1, -.301075511275357776903765377766e-2, .486946147971548500904563665091e-3, -.810548818931753560668099430086e-4, .137788477995595247829382514961e-4, -.238022108943589702513699929149e-5, .41640416213865183476391859902e-6, -.73595828378075994984266837032e-7, .13117611876241674949152294345e-7, -.235467093177424251366960923302e-8, .425227732760349977756380529626e-9, -.771908941348407968261081074933e-10, .140757464813590699092153564722e-10, -.257690720580246806275370786276e-11, .473424066662944218491543950059e-12, -.872490126747426417453012632927e-13, .161246149027405514657398331191e-13, -.298756520156657730067107924168e-14, .554807012090828879830413216973e-15, -.103246191582715695951413339619e-15, .192502392030498511778785032449e-16, -.359550734652651500111897078443e-17, .672645425378768578921945742268e-18, -.126026241687352192520824256376e-18};

	// Series on [0,1]
	private static final double ERFC_COEF[] = {-.490461212346918080399845440334e-1, -.142261205103713642378247418996e0, .100355821875997955757546767129e-1, -.576876469976748476508270255092e-3, .274199312521960610344221607915e-4, -.110431755073445076041353812959e-5, .384887554203450369499613114982e-7, -.118085825338754669696317518016e-8, .323342158260509096464029309534e-10, -.799101594700454875816073747086e-12, .179907251139614556119672454866e-13, -.371863548781869263823168282095e-15, .710359900371425297116899083947e-17, -.126124551191552258324954248533e-18};

	// Series on [0.25,1.00]
	private static final double ERFC2_COEF[] = {-.69601346602309501127391508262e-1, -.411013393626208934898221208467e-1, .391449586668962688156114370524e-2, -.490639565054897916128093545077e-3, .715747900137703638076089414183e-4, -.115307163413123283380823284791e-4, .199467059020199763505231486771e-5, -.364266647159922287393611843071e-6, .694437261000501258993127721463e-7, -.137122090210436601953460514121e-7, .278838966100713713196386034809e-8, -.581416472433116155186479105032e-9, .123892049175275318118016881795e-9, -.269063914530674343239042493789e-10, .594261435084791098244470968384e-11, -.133238673575811957928775442057e-11, .30280468061771320171736972433e-12, -.696664881494103258879586758895e-13, .162085454105392296981289322763e-13, -.380993446525049199987691305773e-14, .904048781597883114936897101298e-15, -.2164006195089607347809812047e-15, .522210223399585498460798024417e-16, -.126972960236455533637241552778e-16, .310914550427619758383622741295e-17, -.766376292032038552400956671481e-18, .190081925136274520253692973329e-18};

	// Series on [0,0.25]
	private static final double ERFCC_COEF[] = {.715179310202924774503697709496e-1, -.265324343376067157558893386681e-1, .171115397792085588332699194606e-2, -.163751663458517884163746404749e-3, .198712935005520364995974806758e-4, -.284371241276655508750175183152e-5, .460616130896313036969379968464e-6, -.822775302587920842057766536366e-7, .159214187277090112989358340826e-7, -.329507136225284321486631665072e-8, .72234397604005554658126115389e-9, -.166485581339872959344695966886e-9, .401039258823766482077671768814e-10, -.100481621442573113272170176283e-10, .260827591330033380859341009439e-11, -.699111056040402486557697812476e-12, .192949233326170708624205749803e-12, -.547013118875433106490125085271e-13, .158966330976269744839084032762e-13, -.47268939801975548392036958429e-14, .14358733767849847867287399784e-14, -.444951056181735839417250062829e-15, .140481088476823343737305537466e-15, -.451381838776421089625963281623e-16, .147452154104513307787018713262e-16, -.489262140694577615436841552532e-17, .164761214141064673895301522827e-17, -.562681717632940809299928521323e-18, .194744338223207851429197867821e-18};


	/**
	 *	Private contructor, so nobody can make an instance of this class.
	 */
	private Sfun() {
	}

	/**
	 *	Returns the inverse (arc) hyperbolic cosine of a double.
	 *	@param	x	A double value.
	 *	@return  The arc hyperbolic cosine of x.
	 *	If x is NaN or less than one, the result is NaN.
	 */
	static public double acosh(double x) {
		double ans;

		if (Double.isNaN(x) || x < 1) {
			ans = Double.NaN;
		} else if (x < 94906265.62) {
			// 94906265.62 = 1.0/Math.sqrt(EPSILON_SMALL)
			ans = Math.log(x + Math.sqrt(x * x - 1.0));
		} else {
			ans = 0.69314718055994530941723212145818 + Math.log(x);
		}
		return ans;
	}

	/**
	 *	Returns the inverse (arc) hyperbolic sine of a double.
	 *	@param	x	A double value.
	 *	@return  The arc hyperbolic sine of x.
	 *	If x is NaN, the result is NaN.
	 */
	static public double asinh(double x) {
		double ans;
		double y = Math.abs(x);

		if (Double.isNaN(x)) {
			ans = Double.NaN;
		} else if (y <= 1.05367e-08) {
			// 1.05367e-08 = Math.sqrt(EPSILON_SMALL)
			ans = x;
		} else if (y <= 1.0) {
			ans = x * (1.0 + csevl(2.0 * x * x - 1.0, ASINH_COEF));
		} else if (y < 94906265.62) {
			// 94906265.62 = 1/Math.sqrt(EPSILON_SMALL)
			ans = Math.log(y + Math.sqrt(y * y + 1.0));
		} else {
			ans = 0.69314718055994530941723212145818 + Math.log(y);
		}
		if (x < 0.0) ans = -ans;
		return ans;
	}

	/**
	 *	Returns the inverse (arc) hyperbolic tangent of a double.
	 *	@param	x	A double value.
	 *	@return  The arc hyperbolic tangent of x.
	 *	If x is NaN or |x|>1, the result is NaN.
	 */
	static public double atanh(double x) {
		double y = Math.abs(x);
		double ans;

		if (Double.isNaN(x)) {
			ans = Double.NaN;
		} else if (y < 1.82501e-08) {
			// 1.82501e-08 = Math.sqrt(3.0*EPSILON_SMALL)
			ans = x;
		} else if (y <= 0.5) {
			ans = x * (1.0 + csevl(8.0 * x * x - 1.0, ATANH_COEF));
		} else if (y < 1.0) {
			ans = 0.5 * Math.log((1.0 + x) / (1.0 - x));
		} else if (y == 1.0) {
			ans = x * Double.POSITIVE_INFINITY;
		} else {
			ans = Double.NaN;
		}
		return ans;
	}

	/**
	 *	Returns the hyperbolic cosine of a double.
	 *	@param	x	A double value.
	 *	@return  The hyperbolic cosine of x.
	 *	If x is NaN, the result is NaN.
	 */
	static public double cosh(double x) {
		double ans;
		double y = Math.exp(Math.abs(x));

		if (Double.isNaN(x)) {
			ans = Double.NaN;
		} else if (Double.isInfinite(x)) {
			ans = x;
		} else if (y < 94906265.62) {
			// 94906265.62 = 1.0/Math.sqrt(EPSILON_SMALL)
			ans = 0.5 * (y + 1.0 / y);
		} else {
			ans = 0.5 * y;
		}
		return ans;
	}

	/**
	 *	Returns the cotangent of a double.
	 *	@param	x	A double value.
	 *	@return  The cotangent of x.
	 *	If x is NaN, the result is NaN.
	 */
	static public double cot(double x) {
		double ans, ainty, ainty2, prodbg, y, yrem;
		double pi2rec = 0.011619772367581343075535053490057; //  2/PI - 0.625

		y = Math.abs(x);

		if (y > 4.5036e+15) {
			// 4.5036e+15 = 1.0/EPSILON_LARGE
			return Double.NaN;
		}

		// Carefully compute
		// Y * (2/PI) = (AINT(Y) + REM(Y)) * (.625 + PI2REC)
		//		= AINT(.625*Y) + REM(.625*Y) + Y*PI2REC  =  AINT(.625*Y) + Z
		//		= AINT(.625*Y) + AINT(Z) + REM(Z)
		ainty = (int) y;
		yrem = y - ainty;
		prodbg = 0.625 * ainty;
		ainty = (int) prodbg;
		y = (prodbg - ainty) + 0.625 * yrem + y * pi2rec;
		ainty2 = (int) y;
		ainty = ainty + ainty2;
		y = y - ainty2;

		int ifn = (int) (ainty % 2.0);
		if (ifn == 1) y = 1.0 - y;

		if (y == 0.0) {
			ans = Double.POSITIVE_INFINITY;
		} else if (y <= 1.82501e-08) {
			// 1.82501e-08 = Math.sqrt(3.0*EPSILON_SMALL)
			ans = 1.0 / y;
		} else if (y <= 0.25) {
			ans = (0.5 + csevl(32.0 * y * y - 1.0, COT_COEF)) / y;
		} else if (y <= 0.5) {
			ans = (0.5 + csevl(8.0 * y * y - 1.0, COT_COEF)) / (0.5 * y);
			ans = (ans * ans - 1.0) * 0.5 / ans;
		} else {
			ans = (0.5 + csevl(2.0 * y * y - 1.0, COT_COEF)) / (0.25 * y);
			ans = (ans * ans - 1.0) * 0.5 / ans;
			ans = (ans * ans - 1.0) * 0.5 / ans;
		}
		if (x != 0.0) ans = sign(ans, x);
		if (ifn == 1) ans = -ans;
		return ans;
	}

	/*
	 *	Evaluate a Chebyschev series
	 */
	static double csevl(double x, double coef[]) {
		double b0, b1, b2, twox;
		int i;
		b1 = 0.0;
		b0 = 0.0;
		b2 = 0.0;
		twox = 2.0 * x;
		for (i = coef.length - 1; i >= 0; i--) {
			b2 = b1;
			b1 = b0;
			b0 = twox * b1 - b2 + coef[i];
		}
		return 0.5 * (b0 - b2);
	}

	/*
	 *	Correction term used by logBeta.
	 */
	private static double dlnrel(double x) {
		double ans;

		if (x <= -1.0) {
			ans = Double.NaN;
		} else if (Math.abs(x) <= 0.375) {
			ans = x * (1.0 - x * Sfun.csevl(x / .375, ALNRCS_COEF));
		} else {
			ans = Math.log(1.0 + x);
		}
		return ans;
	}

	/**
	 *	Returns the error function of a double.
	 *	@param	x	A double value.
	 *	@return  The error function of x.
	 */
	static public double erf(double x) {
		double ans;
		double y = Math.abs(x);

		if (y <= 1.49012e-08) {
			// 1.49012e-08 = Math.sqrt(2*EPSILON_SMALL)
			ans = 2 * x / 1.77245385090551602729816748334;
		} else if (y <= 1) {
			ans = x * (1 + csevl(2 * x * x - 1, ERFC_COEF));
		} else if (y < 6.013687357) {
			// 6.013687357 = Math.sqrt(-Math.getLog(1.77245385090551602729816748334 * EPSILON_SMALL))
			ans = sign(1 - erfc(y), x);
		} else {
			ans = sign(1, x);
		}
		return ans;
	}

	/**
	 *	Returns the complementary error function of a double.
	 *	@param	x	A double value.
	 *	@return  The complementary error function of x.
	 */
	static public double erfc(double x) {
		double ans;
		double y = Math.abs(x);

		if (x <= -6.013687357) {
			// -6.013687357 = -Math.sqrt(-Math.getLog(1.77245385090551602729816748334 * EPSILON_SMALL))
			ans = 2;
		} else if (y < 1.49012e-08) {
			// 1.49012e-08 = Math.sqrt(2*EPSILON_SMALL)
			ans = 1 - 2 * x / 1.77245385090551602729816748334;
		} else {
			double ysq = y * y;
			if (y < 1) {
				ans = 1 - x * (1 + csevl(2 * ysq - 1, ERFC_COEF));
			} else if (y <= 4.0) {
				ans = Math.exp(-ysq) / y * (0.5 + csevl((8.0 / ysq - 5.0) / 3.0, ERFC2_COEF));
				if (x < 0) ans = 2.0 - ans;
				if (x < 0) ans = 2.0 - ans;
				if (x < 0) ans = 2.0 - ans;
			} else {
				ans = Math.exp(-ysq) / y * (0.5 + csevl(8.0 / ysq - 1, ERFCC_COEF));
				if (x < 0) ans = 2.0 - ans;
			}
		}
		return ans;
	}

	/**
	 *	Returns the factorial of an integer.
	 *	@param	n	An integer value.
	 *	@return  The factorial of n, n!.
	 *	If x is negative, the result is NaN.
	 */
	static public double fact(int n) {
		double ans = 1;

		if (Double.isNaN(n) || n < 0) {
			ans = Double.NaN;
		} else if (n > 170) {
			// The 171! is too large to fit in a double.
			ans = Double.POSITIVE_INFINITY;
		} else {
			for (int k = 2; k <= n; k++)
				ans *= k;
		}
		return ans;
	}

	/**
	 *	Returns the Gamma function of a double.
	 *	@param	x	A double value.
	 *	@return  The Gamma function of x.
	 *	If x is a negative integer, the result is NaN.
	 */
	static public double gamma(double x) {
		double ans;
		double y = Math.abs(x);

		if (y <= 10.0) {
			/*
			 * Compute gamma(x) for |x|<=10.
			 * First reduce the interval and  find gamma(1+y) for 0 <= y < 1.
			 */
			int n = (int) x;
			if (x < 0.0) n--;
			y = x - n;
			n--;
			ans = 0.9375 + csevl(2.0 * y - 1.0, GAMMA_COEF);
			if (n == 0) {
			} else if (n < 0) {
				// Compute gamma(x) for x < 1
				n = -n;
				if (x == 0.0) {
					ans = Double.NaN;
				} else if (y < 1.0 / Double.MAX_VALUE) {
					ans = Double.POSITIVE_INFINITY;
				} else {
					double xn = n - 2;
					if (x < 0.0 && x + xn == 0.0) {
						ans = Double.NaN;
					} else {
						for (int i = 0; i < n; i++) {
							ans /= x + i;
						}
					}
				}
			} else {	// gamma(x) for x >= 2.0
				for (int i = 1; i <= n; i++) {
					ans *= y + i;
				}
			}
		} else {  // gamma(x) for |x| > 10
			if (x > 171.614) {
				ans = Double.POSITIVE_INFINITY;
			} else if (x < -170.56) {
				ans = 0.0; // underflows
			} else {
				// 0.9189385332046727 = 0.5*getLog(2*PI)
				ans = Math.exp((y - 0.5) * Math.log(y) - y + 0.9189385332046727 + r9lgmc(y));
				if (x < 0.0) {
					double sinpiy = Math.sin(Math.PI * y);
					if (sinpiy == 0 || Math.round(y) == y) {
						ans = Double.NaN;
					} else {
						ans = -Math.PI / (y * sinpiy * ans);
					}
				}
			}
		}
		return ans;
	}

	/**
	 *	Returns the common (base 10) logarithm of a double.
	 *	@param	x	A double value.
	 *	@return  The common logarithm of x.
	 */
	static public double log10(double x) {
		//if (Double.isNaN(x)) return Double.NaN;
		return 0.43429448190325182765 * Math.log(x);
	}

	/**
	 *	Returns the logarithm of the Beta function.
	 *	@param	a	A double value.
	 *	@param	b	A double value.
	 *	@return  The natural logarithm of the Beta function.
	 */
	static public double logBeta(double a, double b) {
		double corr, ans;
		double p = Math.min(a, b);
		double q = Math.max(a, b);

		if (p <= 0.0) {
			ans = Double.NaN;
		} else if (p >= 10.0) {
			// P and Q are large;
			corr = r9lgmc(p) + r9lgmc(q) - r9lgmc(p + q);
			double temp = dlnrel(-p / (p + q));
			ans = -0.5 * Math.log(q) + 0.918938533204672741780329736406 + corr + (p - 0.5) * Math.log(p / (p + q)) + q * temp;
		} else if (q >= 10.0) {
			// P is small, but Q is large
			corr = Sfun.r9lgmc(q) - r9lgmc(p + q);
			//  Check from underflow from r9lgmc
			ans = logGamma(p) + corr + p - p * Math.log(p + q) + (q - 0.5) * dlnrel(-p / (p + q));
		} else {
			// P and Q are small;
			ans = Math.log(gamma(p) * (gamma(q) / gamma(p + q)));
		}
		return ans;
	}

	/**
	 *	Returns the logarithm of the Gamma function of a double.
	 *	@param	x	A double value.
	 *	@return  The natural logarithm of the Gamma function of x.
	 *	If x is a negative integer, the result is NaN.
	 */
	static public double logGamma(double x) {
		double ans, sinpiy, y;

		y = Math.abs(x);

		if (y <= 10) {
			ans = Math.log(Math.abs(gamma(x)));
		} else if (x > 0) {
			// A&S 6.1.40
			// 0.9189385332046727 = 0.5*getLog(2*PI)
			ans = 0.9189385332046727 + (x - 0.5) * Math.log(x) - x + r9lgmc(y);
		} else {
			sinpiy = Math.abs(Math.sin(Math.PI * y));
			if (sinpiy == 0 || Math.round(y) == y) {
				// The argument for the function can not be a negative integer.
				ans = Double.NaN;
			} else {
				ans = 0.22579135264472743236 + (x - 0.5) * Math.log(y) - x - Math.log(sinpiy) - r9lgmc(y);
			}
		}
		return ans;
	}

	/*
	 *	Returns the getLog gamma correction term for argument
	 *	values greater than or equal to 10.0.
	 */
	static double r9lgmc(double x) {
		double ans;

		if (x < 10.0) {
			ans = Double.NaN;
		} else if (x < 9.490626562e+07) {
			// 9.490626562e+07 = 1/Math.sqrt(EPSILON_SMALL)
			double y = 10.0 / x;
			ans = csevl(2.0 * y * y - 1.0, R9LGMC_COEF) / x;
		} else if (x < 1.39118e+11) {
			// 1.39118e+11 = exp(min(getLog(amach(2) / 12.0), -getLog(12.0 * amach(1))));
			// See A&S 6.1.41
			ans = 1.0 / (12.0 * x);
		} else {
			ans = 0.0; // underflows
		}
		return ans;
	}

	/*
	 *	Returns the value of x with the sign of y.
	 */
	static private double sign(double x, double y) {
		double abs_x = ((x < 0) ? -x : x);
		return (y < 0.0) ? -abs_x : abs_x;
	}

	/**
	 *	Returns the inverse (arc) hyperbolic sine of a double.
	 *	@param	x	A double value.
	 *	@return  The arc hyperbolic sine of x.
	 *	If x is NaN or less than one, the result is NaN.
	 */
	static public double sinh(double x) {
		double ans;
		double y = Math.abs(x);

		if (Double.isNaN(x)) {
			ans = Double.NaN;
		} else if (Double.isInfinite(y)) {
			return x;
		} else if (y < 2.58096e-08) {
			// 2.58096e-08 = Math.sqrt(6.0*EPSILON_SMALL)
			ans = x;
		} else if (y <= 1.0) {
			ans = x * (1.0 + csevl(2.0 * x * x - 1.0, SINH_COEF));
		} else {
			y = Math.exp(y);
			if (y >= 94906265.62) {
				// 94906265.62 = 1.0/Math.sqrt(EPSILON_SMALL)
				ans = sign(0.5 * y, x);
			} else {
				ans = sign(0.5 * (y - 1.0 / y), x);
			}
		}
		return ans;
	}

	/**
	 *	Returns the hyperbolic tangent of a double.
	 *	@param	x	A double value.
	 *	@return  The hyperbolic tangent of x.
	 */
	static public double tanh(double x) {
		double ans, y;
		y = Math.abs(x);

		if (Double.isNaN(x)) {
			ans = Double.NaN;
		} else if (y < 1.82501e-08) {
			// 1.82501e-08 = Math.sqrt(3.0*EPSILON_SMALL)
			ans = x;
		} else if (y <= 1.0) {
			ans = x * (1.0 + csevl(2.0 * x * x - 1.0, TANH_COEF));
		} else if (y < 7.977294885) {
			// 7.977294885 = -0.5*Math.getLog(EPSILON_SMALL)
			y = Math.exp(y);
			ans = sign((y - 1.0 / y) / (y + 1.0 / y), x);
		} else {
			ans = sign(1.0, x);
		}
		return ans;
	}
=======
/*
 *  Copyright 2010 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.testing.mocking;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.easymock.IArgumentMatcher;
import org.easymock.IExpectationSetters;
import org.easymock.LogicalOperator;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Android Mock is a wrapper for EasyMock (2.4) which allows for real Class mocking on
 * an Android (Dalvik) VM.
 * 
 * All methods on Android Mock are syntactically equivalent to EasyMock method
 * calls, and will delegate calls to EasyMock, while performing the required
 * transformations to avoid Dalvik VM troubles.
 * 
 * Calls directly to EasyMock will work correctly only if the Class being mocked
 * is in fact an Interface. Calls to Android Mock will work correctly for both
 * Interfaces and concrete Classes.
 * 
 * Android Mock requires that the code being mocked be instrumented prior to
 * loading to the Dalvik VM by having called the MockGenerator.jar file. Try
 * running {@code java -jar MockGenerator.jar --help} for more information.
 * 
 * An example usage pattern is:
 * 
 * {@code &#64;UsesMocks(MyClass.class) public void testFoo() &#123; MyClass
 * mockObject = AndroidMock.createMock(MyClass.class);
 * AndroidMock.expect(mockObject.foo(0)).andReturn(42);
 * AndroidMock.replay(mockObject); assertEquals(42, mockObject.foo(0));
 * AndroidMock.verify(mockObject); &#125; * }
 * 
 * 
 * <b>A note about parameter and return types for the <i>expects</i> style of methods.</b>
 * The various expectation methods such as {@link #eq(boolean)}, {@link #and(boolean, boolean)},
 * and {@link #leq(byte)} all have nonsense return values. Each of the expectation methods may only
 * be executed under strict conditions (in order to set expectations of incoming method parameters
 * during record mode) and thus their return types are in fact never used. The return types are set
 * only to satisfy the compile-time parameter requirements of the methods being mocked in order to
 * allow code such as: {@code mockObject.doFoo(anyInt());}. If {@link #anyInt()} did not return
 * {@code int} then the compiler would not accept the preceding code fragment.
 * 
 * Similarly, the complex expectation methods ({@code #and}, {@code #or}, and {@code not}) take
 * various parameter types, but will throw an {@link java.lang.IllegalStateException} if anything
 * other than an expectation method is provided.  E.g. {@code mockObject.doFoo(and(gt(5), lt(10));}
 * 
 * The benefit of this is to make it very easy to read the test code after it has been written.
 * Additionally, the test code is protected by type safety at compile time.
 * 
 * The downside of this is that when writing the test code in the record phase, how to use the
 * expectation APIs is not made clear by the method signatures of these expectation methods. In
 * particular, it's not at all clear that {@link #and(byte, byte)} takes as parameters other
 * expectation methods, and not just any random method that returns a {@literal byte} or even a
 * {@literal byte} literal.
 * 
 * @author swoodward@google.com (Stephen Woodward)
 */
public class AndroidMock {
  private AndroidMock() {
  }

  /**
   * Creates a mock object for the specified class, order checking
   * is enabled by default. The difference between a strict mock and a normal mock is that a strict
   * mock will not allow for invocations of the mock object to occur other than in the exact order
   * specified during record mode.
   * 
   * The parameter {@literal args} allows the caller to choose which constructor on the Class
   * specified by {@literal toMock} to be called when constructing the Mock object. If a constructor
   * takes primitive values, Java Auto-boxing/unboxing will take care of it automatically, allowing
   * the caller to make calls such as {@literal createStrictMock(MyObject.class, 42, "hello!")},
   * where {@literal MyObject} defines a constructor such as
   * {@literal public MyObject(int answer, String greeting)}.
   * 
   * @param <T> the class type to be mocked.
   * @param toMock the class of the object to be mocked.
   * @param args the arguments to pass to the constructor.
   * @return the mock object.
   */
  public static <T> T createStrictMock(Class<T> toMock, Object... args) {
    return createStrictMock(null, toMock, args);
  }

  /**
   * Creates a mock object for the specified class, order checking
   * is enabled by default. The difference between a strict mock and a normal mock is that a strict
   * mock will not allow for invocations of the mock object to occur other than in the exact order
   * specified during record mode.
   * 
   * The parameter {@literal args} allows the caller to choose which constructor on the Class
   * specified by {@literal toMock} to be called when constructing the Mock object. If a constructor
   * takes primitive values, Java Auto-boxing/unboxing will take care of it automatically, allowing
   * the caller to make calls such as
   * {@literal createStrictMock("NameMyMock", MyObject.class, 42, "hello!")},
   * where {@literal MyObject} defines a constructor such as
   * {@literal public MyObject(int answer, String greeting)}.
   * 
   * @param <T> the class type to be mocked.
   * @param name the name of the mock object. This must be a valid Java identifier. This value is
   * used as the return value from {@link #toString()} when invoked on the mock object.
   * @param toMock the class of the object to be mocked.
   * @param args the arguments to pass to the constructor.
   * @return the mock object.
   * @throws IllegalArgumentException if the name is not a valid Java identifier.
   */
  @SuppressWarnings("cast")
  public static <T> T createStrictMock(String name, Class<T> toMock, Object... args) {
    if (toMock.isInterface()) {
      return EasyMock.createStrictMock(name, toMock);
    }
    Object mockedInterface = EasyMock.createStrictMock(name, getInterfaceFor(toMock));
    return (T) getSubclassFor(toMock, getInterfaceFor(toMock), mockedInterface, args);
  }

  /**
   * Creates a mock object for the specified class, order checking
   * is disabled by default. A normal mock with order checking disabled will allow you to record
   * the method invocations during record mode in any order. If order is important, use
   * {@link #createStrictMock(Class, Object...)} instead.
   * 
   * The parameter {@literal args} allows the caller to choose which constructor on the Class
   * specified by {@literal toMock} to be called when constructing the Mock object. If a constructor
   * takes primitive values, Java Auto-boxing/unboxing will take care of it automatically, allowing
   * the caller to make calls such as
   * {@literal createMock(MyObject.class, 42, "hello!")},
   * where {@literal MyObject} defines a constructor such as
   * {@literal public MyObject(int answer, String greeting)}.
   * 
   * @param <T> the type of the class to be mocked.
   * @param toMock the class object representing the class to be mocked.
   * @param args the arguments to pass to the constructor.
   * @return the mock object.
   */
  public static <T> T createMock(Class<T> toMock, Object... args) {
    return createMock(null, toMock, args);
  }

  /**
   * Creates a mock object for the specified class, order checking
   * is disabled by default. A normal mock with order checking disabled will allow you to record
   * the method invocations during record mode in any order. If order is important, use
   * {@link #createStrictMock(Class, Object...)} instead.
   * 
   * The parameter {@literal args} allows the caller to choose which constructor on the Class
   * specified by {@literal toMock} to be called when constructing the Mock object. If a constructor
   * takes primitive values, Java Auto-boxing/unboxing will take care of it automatically, allowing
   * the caller to make calls such as
   * {@literal createMock("NameMyMock", MyObject.class, 42, "hello!")},
   * where {@literal MyObject} defines a constructor such as
   * {@literal public MyObject(int answer, String greeting)}.
   * 
   * @param <T> the type of the class to be mocked.
   * @param name the name of the mock object. This must be a valid Java identifier. This value is
   * used as the return value from {@link #toString()} when invoked on the mock object.
   * @param toMock the class object representing the class to be mocked.
   * @param args the arguments to pass to the constructor.
   * @return the mock object.
   * @throws IllegalArgumentException if the name is not a valid Java identifier.
   */
  @SuppressWarnings("cast")
  public static <T> T createMock(String name, Class<T> toMock, Object... args) {
    if (toMock.isInterface()) {
      return EasyMock.createMock(name, toMock);
    }
    Object mockedInterface = EasyMock.createMock(name, getInterfaceFor(toMock));
    return (T) getSubclassFor(toMock, getInterfaceFor(toMock), mockedInterface, args);
  }

  /**
   * Creates a mock object for the specified class, order checking
   * is disabled by default, and the mock object will return {@code 0},
   * {@code null} or {@code false} for unexpected invocations.
   * 
   * The parameter {@literal args} allows the caller to choose which constructor on the Class
   * specified by {@literal toMock} to be called when constructing the Mock object. If a constructor
   * takes primitive values, Java Auto-boxing/unboxing will take care of it automatically, allowing
   * the caller to make calls such as
   * {@literal createNiceMock(MyObject.class, 42, "hello!")},
   * where {@literal MyObject} defines a constructor such as
   * {@literal public MyObject(int answer, String greeting)}.
   * 
   * @param <T> the type of the class to be mocked.
   * @param toMock the class object representing the class to be mocked.
   * @param args the arguments to pass to the constructor.
   * @return the mock object.
   */
  public static <T> T createNiceMock(Class<T> toMock, Object... args) {
    return createNiceMock(null, toMock, args);
  }

  /**
   * Creates a mock object for the specified class, order checking
   * is disabled by default, and the mock object will return {@code 0},
   * {@code null} or {@code false} for unexpected invocations.
   * 
   * The parameter {@literal args} allows the caller to choose which constructor on the Class
   * specified by {@literal toMock} to be called when constructing the Mock object. If a constructor
   * takes primitive values, Java Auto-boxing/unboxing will take care of it automatically, allowing
   * the caller to make calls such as
   * {@literal createNiceMock("NameMyMock", MyObject.class, 42, "hello!")},
   * where {@literal MyObject} defines a constructor such as
   * {@literal public MyObject(int answer, String greeting)}.
   * 
   * @param <T> the type of the class to be mocked.
   * @param name the name of the mock object. This must be a valid Java identifier. This value is
   * used as the return value from {@link #toString()} when invoked on the mock object.
   * @param toMock the class object representing the class to be mocked.
   * @param args the arguments to pass to the constructor.
   * @throws IllegalArgumentException if the name is not a valid Java identifier.
   */
  @SuppressWarnings("cast")
  public static <T> T createNiceMock(String name, Class<T> toMock, Object... args) {
    if (toMock.isInterface()) {
      return EasyMock.createNiceMock(name, toMock);
    }
    Object mockedInterface = EasyMock.createNiceMock(name, getInterfaceFor(toMock));
    return (T) getSubclassFor(toMock, getInterfaceFor(toMock), mockedInterface, args);
  }

  /**
   * Returns the expectation setter for the last expected invocation in the current thread.
   * Expectation setters are used during the recording phase to specify what method calls
   * will be expected during the replay phase, and with which parameters. Parameters may be
   * specified as literal values (e.g. {@code expect(mock.foo(42));  expect(mock.foo("hello"));})
   * or according to parameter expectation criteria. Some examples of parameter expectation
   * criteria include {@link #anyObject()}, {@link #leq(int)}, {@link #contains(String)},
   * {@link #isA(Class)} and also the more complex {@link #and(char, char)},
   * {@link #or(boolean, boolean)}, and {@link #not(double)}.
   * 
   * An {@link org.easymock.IExpectationSetters} object has methods which allow you to define
   * the expected behaviour of the mocked method and the expected number of invocations,
   * e.g. {@link org.easymock.IExpectationSetters#andReturn(Object)},
   * {@link org.easymock.IExpectationSetters#andThrow(Throwable)}, and
   * {@link org.easymock.IExpectationSetters#atLeastOnce()}.
   * 
   * @param expectedValue the parameter is used to transport the type to the ExpectationSetter.
   * It allows writing the expected call as an argument,
   * e.g. {@code expect(mock.getName()).andReturn("John Doe")}.
   * @return the expectation setter.
   */
  public static <T> IExpectationSetters<T> expect(T expectedValue) {
    return EasyMock.expect(expectedValue);
  }

  /**
   * Returns the expectation setter for the last expected invocation in the
   * current thread. This method is used for expected invocations on void
   * methods. Use this for things such as
   * {@link org.easymock.IExpectationSetters#andThrow(Throwable)}
   * on void methods.
   * E.g.
   * {@code mock.doFoo();
   * AndroidMock.expectLastCall().andThrow(new IllegalStateException());}
   * 
   * @see #expect(Object) for more details about {@link org.easymock.IExpectationSetters}
   * @return the expectation setter.
   */
  public static <T> IExpectationSetters<T> expectLastCall() {
    return EasyMock.expectLastCall();
  }

  /**
   * Expects any {@code boolean} argument as a parameter to a mocked method.
   * Use this to loosen the expectations of acceptable parameters for a mocked method call.
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(AndroidMock.anyBoolean())).andReturn("hello world");}
   * 
   * @return {@code false}. The return value is always ignored.
   */
  public static boolean anyBoolean() {
    return EasyMock.anyBoolean();
  }

  /**
   * Expects any {@code byte} argument as a parameter to a mocked method.
   * Use this to loosen the expectations of acceptable parameters for a mocked method call.
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(AndroidMock.anyByte())).andReturn("hello world");}
   * 
   * @return {@code 0}. The return value is always ignored.
   */
  public static byte anyByte() {
    return EasyMock.anyByte();
  }

  /**
   * Expects any {@code char} argument as a parameter to a mocked method.
   * Use this to loosen the expectations of acceptable parameters for a mocked method call.
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(AndroidMock.anyChar())).andReturn("hello world");}
   * 
   * @return {@code 0}. The return value is always ignored.
   */
  public static char anyChar() {
    return EasyMock.anyChar();
  }

  /**
   * Expects any {@code int} argument as a parameter to a mocked method.
   * Use this to loosen the expectations of acceptable parameters for a mocked method call.
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(AndroidMock.anyInt())).andReturn("hello world");}
   * 
   * @return {@code 0}. The return value is always ignored.
   */
  public static int anyInt() {
    return EasyMock.anyInt();
  }

  /**
   * Expects any {@code long} argument as a parameter to a mocked method.
   * Use this to loosen the expectations of acceptable parameters for a mocked method call.
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(AndroidMock.anyLong())).andReturn("hello world");}
   * 
   * @return {@code 0}. The return value is always ignored.
   */
  public static long anyLong() {
    return EasyMock.anyLong();
  }

  /**
   * Expects any {@code float} argument as a parameter to a mocked method.
   * Use this to loosen the expectations of acceptable parameters for a mocked method call.
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(AndroidMock.anyFloat())).andReturn("hello world");}
   * 
   * @return {@code 0}. The return value is always ignored.
   */
  public static float anyFloat() {
    return EasyMock.anyFloat();
  }

  /**
   * Expects any {@code double} argument as a parameter to a mocked method.
   * Use this to loosen the expectations of acceptable parameters for a mocked method call.
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(AndroidMock.anyDouble())).andReturn("hello world");}
   * 
   * @return {@code 0}. The return value is always ignored.   */
  public static double anyDouble() {
    return EasyMock.anyDouble();
  }

  /**
   * Expects any {@code short} argument as a parameter to a mocked method.
   * Use this to loosen the expectations of acceptable parameters for a mocked method call.
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(AndroidMock.anyShort())).andReturn("hello world");}
   * 
   * @return {@code 0}. The return value is always ignored.   */
  public static short anyShort() {
    return EasyMock.anyShort();
  }

  /**
   * Expects any {@code java.lang.Object} (or subclass) argument as a parameter to a mocked method.
   * Note that this includes Arrays (since an array {@literal is an Object})
   * Use this to loosen the expectations of acceptable parameters for a mocked method call.
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(AndroidMock.anyLong())).andReturn("hello world");}
   * 
   * @return {@code 0}. The return value is always ignored.
   */
  @SuppressWarnings("unchecked")
  public static <T> T anyObject() {
    return (T) EasyMock.anyObject();
  }

  /**
   * Expects a {@code Comparable} argument greater than or equal to the given value as a parameter
   * to a mocked method.
   * 
   * Use this to loosen the expectations of acceptable parameters for a mocked method call.
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(AndroidMock.geq("hi"))).andReturn("hello");}
   * 
   * @param expectedValue the value to which the specified incoming parameter to the mocked method
   * must be greater than or equal.
   * @return {@code null}. The return value is always ignored.
   */
  public static <T extends Comparable<T>> T geq(Comparable<T> expectedValue) {
    return EasyMock.geq(expectedValue);
  }

  /**
   * Expects a {@code byte} argument greater than or equal to the given value as a parameter
   * to a mocked method.
   * 
   * Use this to loosen the expectations of acceptable parameters for a mocked method call.
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(AndroidMock.geq((byte)42))).andReturn("hello");}
   * 
   * @param expectedValue the value to which the specified incoming parameter to the mocked method
   * must be greater than or equal.
   * @return {@code 0}. The return value is always ignored.
   */
  public static byte geq(byte expectedValue) {
    return EasyMock.geq(expectedValue);
  }

  /**
   * Expects a {@code double} argument greater than or equal to the given value as a parameter
   * to a mocked method.
   * 
   * Use this to loosen the expectations of acceptable parameters for a mocked method call.
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(AndroidMock.geq(42.0))).andReturn("hello");}
   * 
   * @param expectedValue the value to which the specified incoming parameter to the mocked method
   * must be greater than or equal.
   * @return {@code 0}. The return value is always ignored.
   */
  public static double geq(double expectedValue) {
    return EasyMock.geq(expectedValue);
  }

  /**
   * Expects a {@code float} argument greater than or equal to the given value as a parameter
   * to a mocked method.
   * 
   * Use this to loosen the expectations of acceptable parameters for a mocked method call.
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(AndroidMock.geq(42.0f))).andReturn("hello");}
   * 
   * @param expectedValue the value to which the specified incoming parameter to the mocked method
   * must be greater than or equal.
   * @return {@code 0}. The return value is always ignored.
   */
  public static float geq(float expectedValue) {
    return EasyMock.geq(expectedValue);
  }

  /**
   * Expects an {@code int} argument greater than or equal to the given value as a parameter
   * to a mocked method.
   * 
   * Use this to loosen the expectations of acceptable parameters for a mocked method call.
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(AndroidMock.geq(42))).andReturn("hello");}
   * 
   * @param expectedValue the value to which the specified incoming parameter to the mocked method
   * must be greater than or equal.
   * @return {@code 0}. The return value is always ignored.
   */
  public static int geq(int expectedValue) {
    return EasyMock.geq(expectedValue);
  }

  /**
   * Expects a {@code long} argument greater than or equal to the given value as a parameter
   * to a mocked method.
   * 
   * Use this to loosen the expectations of acceptable parameters for a mocked method call.
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(AndroidMock.geq(42l))).andReturn("hello");}
   * 
   * @param expectedValue the value to which the specified incoming parameter to the mocked method
   * must be greater than or equal.
   * @return {@code 0}. The return value is always ignored.
   */
  public static long geq(long expectedValue) {
    return EasyMock.geq(expectedValue);
  }

  /**
   * Expects a {@code short} argument greater than or equal to the given value as a parameter
   * to a mocked method.
   * 
   * Use this to loosen the expectations of acceptable parameters for a mocked method call.
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(AndroidMock.geq((short)42))).andReturn("hello");}
   * 
   * @param expectedValue the value to which the specified incoming parameter to the mocked method
   * must be greater than or equal.
   * @return {@code 0}. The return value is always ignored.
   */
  public static short geq(short expectedValue) {
    return EasyMock.geq(expectedValue);
  }

  /**
   * Expects a {@code Comparable} argument less than or equal to the given value as a parameter
   * to a mocked method.
   * 
   * Use this to loosen the expectations of acceptable parameters for a mocked method call.
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(AndroidMock.leq("hi"))).andReturn("hello");}
   * 
   * @param expectedValue the value to which the specified incoming parameter to the mocked method
   * must be less than or equal.
   * @return {@code null}. The return value is always ignored.
   */
  public static <T extends Comparable<T>> T leq(Comparable<T> expectedValue) {
    return EasyMock.leq(expectedValue);
  }

  /**
   * Expects a {@code byte} argument less than or equal to the given value as a parameter
   * to a mocked method.
   * 
   * Use this to loosen the expectations of acceptable parameters for a mocked method call.
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(AndroidMock.leq((byte)42))).andReturn("hello");}
   * 
   * @param expectedValue the value to which the specified incoming parameter to the mocked method
   * must be less than or equal.
   * @return {@code 0}. The return value is always ignored.
   */
  public static byte leq(byte expectedValue) {
    return EasyMock.leq(expectedValue);
  }

  /**
   * Expects a {@code double} argument less than or equal to the given value as a parameter
   * to a mocked method.
   * 
   * Use this to loosen the expectations of acceptable parameters for a mocked method call.
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(AndroidMock.leq(42.0))).andReturn("hello");}
   * 
   * @param expectedValue the value to which the specified incoming parameter to the mocked method
   * must be less than or equal.
   * @return {@code 0}. The return value is always ignored.
   */
  public static double leq(double expectedValue) {
    return EasyMock.leq(expectedValue);
  }

  /**
   * Expects a {@code float} argument less than or equal to the given value as a parameter
   * to a mocked method.
   * 
   * Use this to loosen the expectations of acceptable parameters for a mocked method call.
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(AndroidMock.leq(42.0f))).andReturn("hello");}
   * 
   * @param expectedValue the value to which the specified incoming parameter to the mocked method
   * must be less than or equal.
   * @return {@code 0}. The return value is always ignored.
   */
  public static float leq(float expectedValue) {
    return EasyMock.leq(expectedValue);
  }

  /**
   * Expects an {@code int} argument less than or equal to the given value as a parameter
   * to a mocked method.
   * 
   * Use this to loosen the expectations of acceptable parameters for a mocked method call.
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(AndroidMock.leq(42))).andReturn("hello");}
   * 
   * @param expectedValue the value to which the specified incoming parameter to the mocked method
   * must be less than or equal.
   * @return {@code 0}. The return value is always ignored.
   */
  public static int leq(int expectedValue) {
    return EasyMock.leq(expectedValue);
  }

  /**
   * Expects a {@code long} argument less than or equal to the given value as a parameter
   * to a mocked method.
   * 
   * Use this to loosen the expectations of acceptable parameters for a mocked method call.
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(AndroidMock.leq(42l))).andReturn("hello");}
   * 
   * @param expectedValue the value to which the specified incoming parameter to the mocked method
   * must be less than or equal.
   * @return {@code 0}. The return value is always ignored.
   */
  public static long leq(long expectedValue) {
    return EasyMock.leq(expectedValue);
  }

  /**
   * Expects a {@code short} argument less than or equal to the given value as a parameter
   * to a mocked method.
   * 
   * Use this to loosen the expectations of acceptable parameters for a mocked method call.
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(AndroidMock.leq((short)42))).andReturn("hello");}
   * 
   * @param expectedValue the value to which the specified incoming parameter to the mocked method
   * must be less than or equal.
   * @return {@code 0}. The return value is always ignored.
   */
  public static short leq(short expectedValue) {
    return EasyMock.leq(expectedValue);
  }

  /**
   * Expects a {@code Comparable} argument greater than the given value as a parameter
   * to a mocked method.
   * 
   * Use this to loosen the expectations of acceptable parameters for a mocked method call.
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(AndroidMock.gt("hi"))).andReturn("hello");}
   * 
   * @param expectedValue the value to which the specified incoming parameter to the mocked method
   * must be greater than.
   * @return {@code null}. The return value is always ignored.
   */
  public static <T extends Comparable<T>> T gt(Comparable<T> expectedValue) {
    return EasyMock.gt(expectedValue);
  }

  /**
   * Expects a {@code byte} argument greater than the given value as a parameter
   * to a mocked method.
   * 
   * Use this to loosen the expectations of acceptable parameters for a mocked method call.
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(AndroidMock.gt((byte)42))).andReturn("hello");}
   * 
   * @param expectedValue the value to which the specified incoming parameter to the mocked method
   * must be greater than.
   * @return {@code 0}. The return value is always ignored.
   */
  public static byte gt(byte expectedValue) {
    return EasyMock.gt(expectedValue);
  }

  /**
   * Expects a {@code double} argument greater than the given value as a parameter
   * to a mocked method.
   * 
   * Use this to loosen the expectations of acceptable parameters for a mocked method call.
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(AndroidMock.gt(42.0))).andReturn("hello");}
   * 
   * @param expectedValue the value to which the specified incoming parameter to the mocked method
   * must be greater than.
   * @return {@code 0}. The return value is always ignored.
   */
  public static double gt(double expectedValue) {
    return EasyMock.gt(expectedValue);
  }

  /**
   * Expects a {@code float} argument greater than the given value as a parameter
   * to a mocked method.
   * 
   * Use this to loosen the expectations of acceptable parameters for a mocked method call.
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(AndroidMock.gt(42.0f))).andReturn("hello");}
   * 
   * @param expectedValue the value to which the specified incoming parameter to the mocked method
   * must be greater than.
   * @return {@code 0}. The return value is always ignored.
   */
  public static float gt(float expectedValue) {
    return EasyMock.gt(expectedValue);
  }

  /**
   * Expects an {@code int} argument greater than the given value as a parameter
   * to a mocked method.
   * 
   * Use this to loosen the expectations of acceptable parameters for a mocked method call.
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(AndroidMock.gt(42))).andReturn("hello");}
   * 
   * @param expectedValue the value to which the specified incoming parameter to the mocked method
   * must be greater than.
   * @return {@code 0}. The return value is always ignored.
   */
  public static int gt(int expectedValue) {
    return EasyMock.gt(expectedValue);
  }

  /**
   * Expects a {@code long} argument greater than the given value as a parameter
   * to a mocked method.
   * 
   * Use this to loosen the expectations of acceptable parameters for a mocked method call.
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(AndroidMock.gt(42l))).andReturn("hello");}
   * 
   * @param expectedValue the value to which the specified incoming parameter to the mocked method
   * must be greater than.
   * @return {@code 0}. The return value is always ignored.
   */
  public static long gt(long expectedValue) {
    return EasyMock.gt(expectedValue);
  }

  /**
   * Expects a {@code short} argument greater than the given value as a parameter
   * to a mocked method.
   * 
   * Use this to loosen the expectations of acceptable parameters for a mocked method call.
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(AndroidMock.gt((short)42))).andReturn("hello");}
   * 
   * @param expectedValue the value to which the specified incoming parameter to the mocked method
   * must be greater than.
   * @return {@code 0}. The return value is always ignored.
   */
  public static short gt(short expectedValue) {
    return EasyMock.gt(expectedValue);
  }

  /**
   * Expects a {@code Comparable} argument less than the given value as a parameter
   * to a mocked method.
   * 
   * Use this to loosen the expectations of acceptable parameters for a mocked method call.
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(AndroidMock.lt("hi"))).andReturn("hello");}
   * 
   * @param expectedValue the value to which the specified incoming parameter to the mocked method
   * must be less than.
   * @return {@code null}. The return value is always ignored.
   */
  public static <T extends Comparable<T>> T lt(Comparable<T> expectedValue) {
    return EasyMock.lt(expectedValue);
  }

  /**
   * Expects a {@code byte} argument less than the given value as a parameter
   * to a mocked method.
   * 
   * Use this to loosen the expectations of acceptable parameters for a mocked method call.
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(AndroidMock.lt((byte)42))).andReturn("hello");}
   * 
   * @param expectedValue the value to which the specified incoming parameter to the mocked method
   * must be less than.
   * @return {@code 0}. The return value is always ignored.
   */
  public static byte lt(byte expectedValue) {
    return EasyMock.lt(expectedValue);
  }

  /**
   * Expects a {@code double} argument less than the given value as a parameter
   * to a mocked method.
   * 
   * Use this to loosen the expectations of acceptable parameters for a mocked method call.
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(AndroidMock.lt(42.0))).andReturn("hello");}
   * 
   * @param expectedValue the value to which the specified incoming parameter to the mocked method
   * must be less than.
   * @return {@code 0}. The return value is always ignored.
   */
  public static double lt(double expectedValue) {
    return EasyMock.lt(expectedValue);
  }

  /**
   * Expects a {@code float} argument less than the given value as a parameter
   * to a mocked method.
   * 
   * Use this to loosen the expectations of acceptable parameters for a mocked method call.
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(AndroidMock.lt(42.0f))).andReturn("hello");}
   * 
   * @param expectedValue the value to which the specified incoming parameter to the mocked method
   * must be less than.
   * @return {@code 0}. The return value is always ignored.
   */
  public static float lt(float expectedValue) {
    return EasyMock.lt(expectedValue);
  }

  /**
   * Expects an {@code int} argument less than the given value as a parameter
   * to a mocked method.
   * 
   * Use this to loosen the expectations of acceptable parameters for a mocked method call.
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(AndroidMock.lt(42))).andReturn("hello");}
   * 
   * @param expectedValue the value to which the specified incoming parameter to the mocked method
   * must be less than.
   * @return {@code 0}. The return value is always ignored.
   */
  public static int lt(int expectedValue) {
    return EasyMock.lt(expectedValue);
  }

  /**
   * Expects a {@code long} argument less than the given value as a parameter
   * to a mocked method.
   * 
   * Use this to loosen the expectations of acceptable parameters for a mocked method call.
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(AndroidMock.lt(42l))).andReturn("hello");}
   * 
   * @param expectedValue the value to which the specified incoming parameter to the mocked method
   * must be less than.
   * @return {@code 0}. The return value is always ignored.
   */
  public static long lt(long expectedValue) {
    return EasyMock.lt(expectedValue);
  }

  /**
   * Expects a {@code short} argument less than the given value as a parameter
   * to a mocked method.
   * 
   * Use this to loosen the expectations of acceptable parameters for a mocked method call.
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(AndroidMock.lt((short)42))).andReturn("hello");}
   * 
   * @param expectedValue the value to which the specified incoming parameter to the mocked method
   * must be less than.
   * @return {@code 0}. The return value is always ignored.
   */
  public static short lt(short expectedValue) {
    return EasyMock.lt(expectedValue);
  }

  /**
   * Expects an object implementing the given class as a parameter to a mocked method. During
   * replay mode, the mocked method call will accept any {@code Object} that is an instance of
   * the specified class or one of its subclasses. Specifically, any {@code non-null} parameter for
   * which the {@code java.lang.Class.isAssignableFrom(Class)} will return true will be accepted by
   * this matcher during the replay phase.
   * 
   * Use this to loosen the expectations of acceptable parameters for a mocked method call.
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(AndroidMock.isA(HashMap.class))).andReturn("hello");}
   * 
   * @param <T> the expected Class type.
   * @param clazz the class of the accepted type.
   * @return {@code null}. The return value is always ignored.
   */
  public static <T> T isA(Class<T> clazz) {
    return EasyMock.isA(clazz);
  }

  /**
   * Expects a string that contains the given substring as a parameter to a mocked method.
   * During replay mode, the mocked method will accept any {@code non-null String} which contains
   * the provided {@code substring}.
   * 
   * Use this to loosen the expectations of acceptable parameters for a mocked method call.
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(AndroidMock.substring("hi"))).andReturn("hello");}
   * 
   * @param substring the substring which any incoming parameter to the mocked method must contain.
   * @return {@code null}.
   */
  public static String contains(String substring) {
    return EasyMock.contains(substring);
  }

  /**
   * Expects a {@code boolean} parameter that matches both of the provided expectations. During
   * replay mode, the mocked method will accept any {@code boolean} that matches both of the
   * provided expectations. Possible expectations for {@code first} and {@code second} include (but
   * are not limited to) {@link #anyBoolean()} and {@link #eq(boolean)}.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(
   *        AndroidMock.and(AndroidMock.anyBoolean(), AndroidMock.eq(true)))).andReturn("hello");}
   * 
   * Or, for illustration purposes (using static imports)
   * 
   * {@code expect(mock.getString(and(anyBoolean(), eq(true)))).andReturn("hello");}
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * @param first the first expectation to test.
   * @param second the second expectation to test.
   * @return {@code false}. The return value is always ignored.
   */
  public static boolean and(boolean first, boolean second) {
    return EasyMock.and(first, second);
  }

  /**
   * Expects a {@code byte} parameter that matches both of the provided expectations. During replay
   * mode, the mocked method will accept any {@code byte} that matches both of the provided
   * expectations. Possible expectations for {@code first} and {@code second} include (but are not
   * limited to) {@link #anyByte()}, {@link #leq(byte)} and {@link #eq(byte)}.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(AndroidMock.and(
   *        AndroidMock.gt((byte)0), AndroidMock.lt((byte)42)))).andReturn("hello");}
   * 
   * Or, for illustration purposes (using static imports)
   * 
   * {@code expect(mock.getString(and(gt((byte)0), lt((byte)42)))).andReturn("hello");}
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * @param first the first expectation to test.
   * @param second the second expectation to test.
   * @return {@code 0}. The return value is always ignored.
   */
  public static byte and(byte first, byte second) {
    return EasyMock.and(first, second);
  }

  /**
   * Expects a {@code char} parameter that matches both of the provided expectations. During replay
   * mode, the mocked method will accept any {@code char} that matches both of the provided
   * expectations. Possible expectations for {@code first} and {@code second} include (but are not
   * limited to) {@link #anyChar()} and {@link #eq(char)}.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(
   *        AndroidMock.and(AndroidMock.geq('a'), AndroidMock.lt('q')))).andReturn("hello");}
   * 
   * Or, for illustration purposes (using static imports)
   * 
   * {@code expect(mock.getString(and(eq('a'), anyChar()))).andReturn("hello");}
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * @param first the first expectation to test.
   * @param second the second expectation to test.
   * @return {@code 0}. The return value is always ignored.
   */
  public static char and(char first, char second) {
    return EasyMock.and(first, second);
  }

  /**
   * Expects a {@code double} parameter that matches both of the provided expectations. During
   * replay mode, the mocked method will accept any {@code double} that matches both of the provided
   * expectations. Possible expectations for {@code first} and {@code second} include (but are not
   * limited to) {@link #anyDouble()}, {@link #leq(double)} and {@link #eq(double)}.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(
   *        AndroidMock.and(AndroidMock.gt(0.0), AndroidMock.lt(42.0)))).andReturn("hello");}
   * 
   * Or, for illustration purposes (using static imports)
   * 
   * {@code expect(mock.getString(and(gt(0.0), lt(42.0)))).andReturn("hello");}
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * @param first the first expectation to test.
   * @param second the second expectation to test.
   * @return {@code 0}. The return value is always ignored.
   */
  public static double and(double first, double second) {
    return EasyMock.and(first, second);
  }

  /**
   * Expects a {@code float} parameter that matches both of the provided expectations. During
   * replay mode, the mocked method will accept any {@code float} that matches both of the provided
   * expectations. Possible expectations for {@code first} and {@code second} include (but are not
   * limited to) {@link #anyFloat()}, {@link #leq(float)} and {@link #eq(float)}.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(
   *        AndroidMock.and(AndroidMock.gt(0.0f), AndroidMock.lt(42.0f)))).andReturn("hello");}
   * 
   * Or, for illustration purposes (using static imports)
   * 
   * {@code expect(mock.getString(and(gt(0.0f), lt(42.0f)))).andReturn("hello");}
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * @param first the first expectation to test.
   * @param second the second expectation to test.
   * @return {@code 0}. The return value is always ignored.
   */
  public static float and(float first, float second) {
    return EasyMock.and(first, second);
  }

  /**
   * Expects an {@code int} parameter that matches both of the provided expectations. During
   * replay mode, the mocked method will accept any {@code int} that matches both of the provided
   * expectations. Possible expectations for {@code first} and {@code second} include (but are not
   * limited to) {@link #anyInt()}, {@link #leq(int)} and {@link #eq(int)}.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(
   *        AndroidMock.and(AndroidMock.gt(0), AndroidMock.lt(42)))).andReturn("hello");}
   * 
   * Or, for illustration purposes (using static imports)
   * 
   * {@code expect(mock.getString(and(gt(0), lt(42)))).andReturn("hello");}
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * @param first the first expectation to test.
   * @param second the second expectation to test.
   * @return {@code 0}. The return value is always ignored.
   */
  public static int and(int first, int second) {
    return EasyMock.and(first, second);
  }

  /**
   * Expects a {@code long} parameter that matches both of the provided expectations. During
   * replay mode, the mocked method will accept any {@code long} that matches both of the provided
   * expectations. Possible expectations for {@code first} and {@code second} include (but are not
   * limited to) {@link #anyLong()}, {@link #leq(long)} and {@link #eq(long)}.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(
   *        AndroidMock.and(AndroidMock.gt(0l), AndroidMock.lt(42l)))).andReturn("hello");}
   * 
   * Or, for illustration purposes (using static imports)
   * 
   * {@code expect(mock.getString(and(gt(0l), lt(42l)))).andReturn("hello");}
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * @param first the first expectation to test.
   * @param second the second expectation to test.
   * @return {@code 0}. The return value is always ignored.
   */
  public static long and(long first, long second) {
    return EasyMock.and(first, second);
  }

  /**
   * Expects a {@code short} parameter that matches both of the provided expectations. During
   * replay mode, the mocked method will accept any {@code short} that matches both of the provided
   * expectations. Possible expectations for {@code first} and {@code second} include (but are not
   * limited to) {@link #anyShort()}, {@link #leq(short)} and {@link #eq(short)}.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(AndroidMock.and(
   *        AndroidMock.gt((short)0), AndroidMock.lt((short)42)))).andReturn("hello");}
   * 
   * Or, for illustration purposes (using static imports)
   * 
   * {@code expect(mock.getString(and(gt((short)0), lt((short)42)))).andReturn("hello");}
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * @param first the first expectation to test.
   * @param second the second expectation to test.
   * @return {@code 0}. The return value is always ignored.
   */
  public static short and(short first, short second) {
    return EasyMock.and(first, second);
  }

  /**
   * Expects an {@code Object} parameter that matches both of the provided expectations. During
   * replay mode, the mocked method will accept any {@code Object} that matches both of the provided
   * expectations. Possible expectations for {@code first} and {@code second} include (but are not
   * limited to) {@link #anyObject()}, {@link #isA(Class)} and {@link #contains(String)}.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(
   *        AndroidMock.and(
   *            AndroidMock.contains("hi"), AndroidMock.contains("world")))).andReturn("hello");}
   * 
   * Or, for illustration purposes (using static imports)
   * 
   * {@code expect(mock.getString(and(contains("hi"), contains("world")))).andReturn("hello");}
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * @param first the first expectation to test.
   * @param second the second expectation to test.
   * @return {@code 0}. The return value is always ignored.
   */
  public static <T> T and(T first, T second) {
    return EasyMock.and(first, second);
  }

  /**
   * Expects a {@code boolean} parameter that matches one or both of the provided expectations.
   * During replay mode, the mocked method will accept any {@code boolean} that matches one of the
   * provided expectations, or both of them. Possible expectations for {@code first} and
   * {@code second} include (but are not limited to) {@link #anyBoolean()} and {@link #eq(boolean)}.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(
   *        AndroidMock.or(AndroidMock.eq(true), AndroidMock.anyBoolean()))).andReturn("hello");}
   * 
   * Or, for illustration purposes (using static imports)
   * 
   * {@code expect(mock.getString(and(eq(true), anyBoolean()))).andReturn("hello");}
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * @param first the first expectation to test.
   * @param second the second expectation to test.
   * @return {@code false}. The return value is always ignored.
   */
  public static boolean or(boolean first, boolean second) {
    return EasyMock.or(first, second);
  }

  /**
   * Expects a {@code byte} parameter that matches one or both of the provided expectations.
   * During replay mode, the mocked method will accept any {@code byte} that matches one of the
   * provided expectations, or both of them. Possible expectations for {@code first} and
   * {@code second} include (but are not limited to) {@link #anyByte()}, {@link #eq(byte)},
   * and {@link #lt(byte)}.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(AndroidMock.or(
   *        AndroidMock.geq((byte)0), AndroidMock.lt((byte)42)))).andReturn("hello");}
   * 
   * Or, for illustration purposes (using static imports)
   * 
   * {@code expect(mock.getString(or(geq((byte)0), lt((byte)42)))).andReturn("hello");}
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * @param first the first expectation to test.
   * @param second the second expectation to test.
   * @return {@code 0}. The return value is always ignored.
   */
  public static byte or(byte first, byte second) {
    return EasyMock.or(first, second);
  }

  /**
   * Expects a {@code char} parameter that matches one or both of the provided expectations.
   * During replay mode, the mocked method will accept any {@code char} that matches one of the
   * provided expectations, or both of them. Possible expectations for {@code first} and
   * {@code second} include (but are not limited to) {@link #anyChar()} and {@link #eq(char)}.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(AndroidMock.or(
   *        AndroidMock.eq('a'), AndroidMock.eq('z')))).andReturn("hello");}
   * 
   * Or, for illustration purposes (using static imports)
   * 
   * {@code expect(mock.getString(or(eq('a'), eq('z')))).andReturn("hello");}
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * @param first the first expectation to test.
   * @param second the second expectation to test.
   * @return {@code 0}. The return value is always ignored.
   */
  public static char or(char first, char second) {
    return EasyMock.or(first, second);
  }

  /**
   * Expects a {@code double} parameter that matches one or both of the provided expectations.
   * During replay mode, the mocked method will accept any {@code double} that matches one of the
   * provided expectations, or both of them. Possible expectations for {@code first} and
   * {@code second} include (but are not limited to) {@link #anyDouble()}, {@link #eq(double)}
   * and {@link #lt(double)}.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(AndroidMock.or(
   *        AndroidMock.eq(0.0), AndroidMock.geq(42.0)))).andReturn("hello");}
   * 
   * Or, for illustration purposes (using static imports)
   * 
   * {@code expect(mock.getString(or(eq(0.0), geq(42.0)))).andReturn("hello");}
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * @param first the first expectation to test.
   * @param second the second expectation to test.
   * @return {@code 0}. The return value is always ignored.
   */
  public static double or(double first, double second) {
    return EasyMock.or(first, second);
  }

  /**
   * Expects a {@code float} parameter that matches one or both of the provided expectations.
   * During replay mode, the mocked method will accept any {@code float} that matches one of the
   * provided expectations, or both of them. Possible expectations for {@code first} and
   * {@code second} include (but are not limited to) {@link #anyFloat()}, {@link #eq(float)}
   * and {@link #lt(float)}.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(AndroidMock.or(
   *        AndroidMock.eq(0.0f), AndroidMock.geq(42.0f)))).andReturn("hello");}
   * 
   * Or, for illustration purposes (using static imports)
   * 
   * {@code expect(mock.getString(or(eq(0.0f), geq(42.0f)))).andReturn("hello");}
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * @param first the first expectation to test.
   * @param second the second expectation to test.
   * @return {@code 0}. The return value is always ignored.
   */
  public static float or(float first, float second) {
    return EasyMock.or(first, second);
  }

  /**
   * Expects an {@code int} parameter that matches one or both of the provided expectations.
   * During replay mode, the mocked method will accept any {@code int} that matches one of the
   * provided expectations, or both of them. Possible expectations for {@code first} and
   * {@code second} include (but are not limited to) {@link #anyInt()}, {@link #eq(int)}
   * and {@link #lt(int)}.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(AndroidMock.or(
   *        AndroidMock.eq(0), AndroidMock.geq(42)))).andReturn("hello");}
   * 
   * Or, for illustration purposes (using static imports)
   * 
   * {@code expect(mock.getString(or(eq(0), geq(42)))).andReturn("hello");}
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * @param first the first expectation to test.
   * @param second the second expectation to test.
   * @return {@code 0}. The return value is always ignored.
   */
  public static int or(int first, int second) {
    return EasyMock.or(first, second);
  }

  /**
   * Expects a {@code long} parameter that matches one or both of the provided expectations.
   * During replay mode, the mocked method will accept any {@code long} that matches one of the
   * provided expectations, or both of them. Possible expectations for {@code first} and
   * {@code second} include (but are not limited to) {@link #anyLong()}, {@link #eq(long)}
   * and {@link #lt(long)}.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(AndroidMock.or(
   *        AndroidMock.eq(0l), AndroidMock.geq(42l)))).andReturn("hello");}
   * 
   * Or, for illustration purposes (using static imports)
   * 
   * {@code expect(mock.getString(or(eq(0l), geq(42l)))).andReturn("hello");}
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * @param first the first expectation to test.
   * @param second the second expectation to test.
   * @return {@code 0}. The return value is always ignored.
   */
  public static long or(long first, long second) {
    return EasyMock.or(first, second);
  }

  /**
   * Expects a {@code short} parameter that matches one or both of the provided expectations.
   * During replay mode, the mocked method will accept any {@code short} that matches one of the
   * provided expectations, or both of them. Possible expectations for {@code first} and
   * {@code second} include (but are not limited to) {@link #anyShort()}, {@link #eq(short)}
   * and {@link #lt(short)}.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(AndroidMock.or(
   *        AndroidMock.eq((short)0), AndroidMock.geq((short)42)))).andReturn("hello");}
   * 
   * Or, for illustration purposes (using static imports)
   * 
   * {@code expect(mock.getString(or(eq((short)0), geq((short)42)))).andReturn("hello");}
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * @param first the first expectation to test.
   * @param second the second expectation to test.
   * @return {@code 0}. The return value is always ignored.
   */
  public static short or(short first, short second) {
    return EasyMock.or(first, second);
  }

  /**
   * Expects an {@code Object} parameter that matches one or both of the provided expectations.
   * During replay mode, the mocked method will accept any {@code Object} that matches one of the
   * provided expectations, or both of them. Possible expectations for {@code first} and
   * {@code second} include (but are not limited to) {@link #anyObject()}, {@link #eq(Class)}
   * and {@link #lt(Comparable)}.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(AndroidMock.or(
   *        AndroidMock.notNull(), AndroidMock.geq(fortyTwo)))).andReturn("hello");}
   * 
   * Or, for illustration purposes (using static imports)
   * 
   * {@code expect(mock.getString(or(notNull(), geq(fortyTwo)))).andReturn("hello");}
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * @param first the first expectation to test.
   * @param second the second expectation to test.
   * @return {@code null}. The return value is always ignored.
   */
  public static <T> T or(T first, T second) {
    return EasyMock.or(first, second);
  }

  /**
   * Expects a {@code boolean} parameter that does not match the provided expectation.
   * During replay mode, the mocked method will accept any {@code boolean} that does not match
   * the provided expectation. Possible expectations for {@code expectation}
   * include (but are not limited to) {@link #anyBoolean()} and {@link #eq(boolean)}.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(
   *        AndroidMock.not(AndroidMock.eq(true)))).andReturn("hello");}
   * 
   * Or, for illustration purposes (using static imports)
   * 
   * {@code expect(mock.getString(not(eq(true)))).andReturn("hello");}
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * @param expectation the expectation to test.
   * @return {@code false}. The return value is always ignored.
   */
  public static boolean not(boolean expectation) {
    return EasyMock.not(expectation);
  }

  /**
   * Expects a {@code byte} parameter that does not match the provided expectation.
   * During replay mode, the mocked method will accept any {@code byte} that does not match
   * the provided expectation. Possible expectations for {@code expectation}
   * include (but are not limited to) {@link #anyByte()}, {@link #eq(byte)} and
   * {@link #lt(byte)}.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(
   *        AndroidMock.not(AndroidMock.eq((byte)42)))).andReturn("hello");}
   * 
   * Or, for illustration purposes (using static imports)
   * 
   * {@code expect(mock.getString(not(eq((byte)42)))).andReturn("hello");}
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * @param expectation the expectation to test.
   * @return {@code 0}. The return value is always ignored.
   */
  public static byte not(byte expectation) {
    return EasyMock.not(expectation);
  }

  /**
   * Expects a {@code char} parameter that does not match the provided expectation.
   * During replay mode, the mocked method will accept any {@code char} that does not match
   * the provided expectation. Possible expectations for {@code expectation}
   * include (but are not limited to) {@link #anyChar()} and {@link #eq(char)}.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(
   *        AndroidMock.not(AndroidMock.eq('a')))).andReturn("hello");}
   * 
   * Or, for illustration purposes (using static imports)
   * 
   * {@code expect(mock.getString(not(eq('a')))).andReturn("hello");}
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * @param expectation the expectation to test.
   * @return {@code 0}. The return value is always ignored.
   */
  public static char not(char expectation) {
    return EasyMock.not(expectation);
  }

  /**
   * Expects a {@code double} parameter that does not match the provided expectation.
   * During replay mode, the mocked method will accept any {@code double} that does not match
   * the provided expectation. Possible expectations for {@code expectation}
   * include (but are not limited to) {@link #anyDouble()}, {@link #eq(double)} and
   * {@link #lt(double)}.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(
   *        AndroidMock.not(AndroidMock.eq(42.0)))).andReturn("hello");}
   * 
   * Or, for illustration purposes (using static imports)
   * 
   * {@code expect(mock.getString(not(eq(42.0)))).andReturn("hello");}
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * @param expectation the expectation to test.
   * @return {@code 0}. The return value is always ignored.
   */
  public static double not(double expectation) {
    return EasyMock.not(expectation);
  }

  /**
   * Expects a {@code float} parameter that does not match the provided expectation.
   * During replay mode, the mocked method will accept any {@code float} that does not match
   * the provided expectation. Possible expectations for {@code expectation}
   * include (but are not limited to) {@link #anyFloat()}, {@link #eq(float)} and
   * {@link #lt(float)}.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(
   *        AndroidMock.not(AndroidMock.eq(42.0f)))).andReturn("hello");}
   * 
   * Or, for illustration purposes (using static imports)
   * 
   * {@code expect(mock.getString(not(eq(42.0f)))).andReturn("hello");}
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * @param expectation the expectation to test.
   * @return {@code 0}. The return value is always ignored.
   */
  public static float not(float expectation) {
    return EasyMock.not(expectation);
  }

  /**
   * Expects a {@code int} parameter that does not match the provided expectation.
   * During replay mode, the mocked method will accept any {@code int} that does not match
   * the provided expectation. Possible expectations for {@code expectation}
   * include (but are not limited to) {@link #anyInt()}, {@link #eq(int)} and
   * {@link #lt(int)}.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(
   *        AndroidMock.not(AndroidMock.eq(42)))).andReturn("hello");}
   * 
   * Or, for illustration purposes (using static imports)
   * 
   * {@code expect(mock.getString(not(eq(42)))).andReturn("hello");}
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * @param expectation the expectation to test.
   * @return {@code 0}. The return value is always ignored.
   */
  public static int not(int expectation) {
    return EasyMock.not(expectation);
  }

  /**
   * Expects a {@code long} parameter that does not match the provided expectation.
   * During replay mode, the mocked method will accept any {@code long} that does not match
   * the provided expectation. Possible expectations for {@code expectation}
   * include (but are not limited to) {@link #anyLong()}, {@link #eq(long)} and
   * {@link #lt(long)}.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(
   *        AndroidMock.not(AndroidMock.eq(42l)))).andReturn("hello");}
   * 
   * Or, for illustration purposes (using static imports)
   * 
   * {@code expect(mock.getString(not(eq(42l)))).andReturn("hello");}
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * @param expectation the expectation to test.
   * @return {@code 0}. The return value is always ignored.
   */
  public static long not(long expectation) {
    return EasyMock.not(expectation);
  }

  /**
   * Expects a {@code short} parameter that does not match the provided expectation.
   * During replay mode, the mocked method will accept any {@code short} that does not match
   * the provided expectation. Possible expectations for {@code expectation}
   * include (but are not limited to) {@link #anyShort()}, {@link #eq(short)} and
   * {@link #lt(short)}.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(
   *        AndroidMock.not(AndroidMock.eq((short)42)))).andReturn("hello");}
   * 
   * Or, for illustration purposes (using static imports)
   * 
   * {@code expect(mock.getString(not(eq((short)42)))).andReturn("hello");}
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * @param expectation the expectation to test.
   * @return {@code 0}. The return value is always ignored.
   */
  public static short not(short expectation) {
    return EasyMock.not(expectation);
  }

  /**
   * Expects an {@code Object} parameter that does not match the given expectation.
   * During replay mode, the mocked method will accept any {@code Object} that does not match
   * the provided expectation. Possible expectations for {@code expectation}
   * include (but are not limited to) {@link #anyObject()}, {@link #leq(Comparable)} and
   * {@link #isNull()}.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(
   *        AndroidMock.not(AndroidMock.eq(fortyTwo)))).andReturn("hello");}
   * 
   * Or, for illustration purposes (using static imports)
   * 
   * {@code expect(mock.getString(not(eq(fortyTwo)))).andReturn("hello");}
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * @param expectation the expectation to test.
   * @return {@code 0}. The return value is always ignored.
   */
  public static <T> T not(T expectation) {
    return EasyMock.not(expectation);
  }

  /**
   * Expects a {@code boolean} parameter that is equal to the provided {@code value}.
   * During replay mode, the mocked method will accept any {@code boolean} that matches the
   * value of {@code expectedValue}.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(AndroidMock.eq(true))).andReturn("hello");}
   * 
   * Or, for illustration purposes (using static imports)
   * 
   * {@code expect(mock.getString(eq(true))).andReturn("hello");}
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * @param expectedValue the value to which the specified incoming parameter to the mocked method
   * must be equal.
   * @return {@code false}. The return value is always ignored.
   */
  public static boolean eq(boolean expectedValue) {
    return EasyMock.eq(expectedValue);
  }

  /**
   * Expects a {@code byte} parameter that is equal to the provided {@code expectedValue}.
   * During replay mode, the mocked method will accept any {@code byte} that matches the
   * value of {@code expectedValue}.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(AndroidMock.eq((byte)0))).andReturn("hello");}
   * 
   * Or, for illustration purposes (using static imports)
   * 
   * {@code expect(mock.getString(eq((byte)0))).andReturn("hello");}
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * @param expectedValue the value to which the specified incoming parameter to the mocked method
   * must be equal.
   * @return {@code false}. The return value is always ignored.
   */
  public static byte eq(byte expectedValue) {
    return EasyMock.eq(expectedValue);
  }

  /**
   * Expects a {@code char} parameter that is equal to the provided {@code expectedValue}.
   * During replay mode, the mocked method will accept any {@code char} that matches the
   * value of {@code expectedValue}.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(AndroidMock.eq('a'))).andReturn("hello");}
   * 
   * Or, for illustration purposes (using static imports)
   * 
   * {@code expect(mock.getString(eq('a'))).andReturn("hello");}
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * @param expectedValue the value to which the specified incoming parameter to the mocked method
   * must be equal.
   * @return {@code 0}. The return value is always ignored.
   */
  public static char eq(char expectedValue) {
    return EasyMock.eq(expectedValue);
  }

  /**
   * Expects a {@code double} parameter that is equal to the provided {@code expectedValue}.
   * During replay mode, the mocked method will accept any {@code double} that matches the
   * value of {@code expectedValue}.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(AndroidMock.eq(0.0))).andReturn("hello");}
   * 
   * Or, for illustration purposes (using static imports)
   * 
   * {@code expect(mock.getString(eq(0.0))).andReturn("hello");}
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * @param expectedValue the value to which the specified incoming parameter to the mocked method
   * must be equal.
   * @return {@code 0}. The return value is always ignored.
   */
  public static double eq(double expectedValue) {
    return EasyMock.eq(expectedValue);
  }

  /**
   * Expects a {@code float} parameter that is equal to the provided {@code expectedValue}.
   * During replay mode, the mocked method will accept any {@code float} that matches the
   * value of {@code expectedValue}.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(AndroidMock.eq(0.0f))).andReturn("hello");}
   * 
   * Or, for illustration purposes (using static imports)
   * 
   * {@code expect(mock.getString(eq(0.0f))).andReturn("hello");}
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * @param expectedValue the value to which the specified incoming parameter to the mocked method
   * must be equal.
   * @return {@code 0}. The return value is always ignored.
   */
  public static float eq(float expectedValue) {
    return EasyMock.eq(expectedValue);
  }

  /**
   * Expects an {@code int} parameter that is equal to the provided {@code expectedValue}.
   * During replay mode, the mocked method will accept any {@code int} that matches the
   * value of {@code expectedValue}.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(AndroidMock.eq(0))).andReturn("hello");}
   * 
   * Or, for illustration purposes (using static imports)
   * 
   * {@code expect(mock.getString(eq(0))).andReturn("hello");}
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * @param expectedValue the value to which the specified incoming parameter to the mocked method
   * must be equal.
   * @return {@code 0}. The return value is always ignored.
   */
  public static int eq(int expectedValue) {
    return EasyMock.eq(expectedValue);
  }

  /**
   * Expects a {@code long} parameter that is equal to the provided {@code expectedValue}.
   * During replay mode, the mocked method will accept any {@code long} that matches the
   * value of {@code expectedValue}.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(AndroidMock.eq(0l))).andReturn("hello");}
   * 
   * Or, for illustration purposes (using static imports)
   * 
   * {@code expect(mock.getString(eq(0l))).andReturn("hello");}
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * @param expectedValue the value to which the specified incoming parameter to the mocked method
   * must be equal.
   * @return {@code 0}. The return value is always ignored.
   */
  public static long eq(long expectedValue) {
    return EasyMock.eq(expectedValue);
  }

  /**
   * Expects a {@code short} parameter that is equal to the provided {@code expectedValue}.
   * During replay mode, the mocked method will accept any {@code short} that matches the
   * value of {@code expectedValue}.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(AndroidMock.eq((short)0))).andReturn("hello");}
   * 
   * Or, for illustration purposes (using static imports)
   * 
   * {@code expect(mock.getString(eq((short)0))).andReturn("hello");}
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * @param expectedValue the value to which the specified incoming parameter to the mocked method
   * must be equal.
   * @return {@code 0}. The return value is always ignored.
   */
  public static short eq(short expectedValue) {
    return EasyMock.eq(expectedValue);
  }

  /**
   * Expects an {@code Object} parameter that is equal to the provided {@code expectedValue}.
   * During replay mode, the mocked method will accept any {@code Object} that matches the
   * value of {@code expectedValue} according to its {@code equals(Object)} method.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(AndroidMock.eq("hi"))).andReturn("hello");}
   * 
   * Or, for illustration purposes (using static imports)
   * 
   * {@code expect(mock.getString(eq("hi"))).andReturn("hello");}
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * @param expectedValue the value to which the specified incoming parameter to the mocked method
   * must be equal.
   * @return {@code 0}. The return value is always ignored.
   */
  public static <T> T eq(T expectedValue) {
    return EasyMock.eq(expectedValue);
  }

  /**
   * Expects a {@code boolean} array parameter that is equal to the given array, i.e. it has to
   * have the same length, and each element has to be equal.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(AndroidMock.eq(myBooleanArray))).andReturn("hello");}
   * 
   * Or, for illustration purposes (using static imports)
   * 
   * {@code expect(mock.getString(eq(myBooleanArray))).andReturn("hello");}
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * @param expectedValue the array to which the specified incoming parameter to the mocked method
   * must have equal contents.
   * @return {@code null}. The return value is always ignored.
   */
  public static boolean[] aryEq(boolean[] expectedValue) {
    return EasyMock.aryEq(expectedValue);
  }

  /**
   * Expects a {@code byte} array parameter that is equal to the given array, i.e. it has to
   * have the same length, and each element has to be equal.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(AndroidMock.eq(myByteArray))).andReturn("hello");}
   * 
   * Or, for illustration purposes (using static imports)
   * 
   * {@code expect(mock.getString(eq(myByteArray))).andReturn("hello");}
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * @param expectedValue the array to which the specified incoming parameter to the mocked method
   * must have equal contents.
   * @return {@code null}. The return value is always ignored.
   */
  public static byte[] aryEq(byte[] expectedValue) {
    return EasyMock.aryEq(expectedValue);
  }

  /**
   * Expects a {@code char} array parameter that is equal to the given array, i.e. it has to
   * have the same length, and each element has to be equal.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(AndroidMock.eq(myCharArray))).andReturn("hello");}
   * 
   * Or, for illustration purposes (using static imports)
   * 
   * {@code expect(mock.getString(eq(myCharArray))).andReturn("hello");}
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * @param expectedValue the array to which the specified incoming parameter to the mocked method
   * must have equal contents.
   * @return {@code null}. The return value is always ignored.
   */
  public static char[] aryEq(char[] expectedValue) {
    return EasyMock.aryEq(expectedValue);
  }

  /**
   * Expects a {@code double} array parameter that is equal to the given array, i.e. it has to
   * have the same length, and each element has to be equal.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(AndroidMock.eq(myDoubleArray))).andReturn("hello");}
   * 
   * Or, for illustration purposes (using static imports)
   * 
   * {@code expect(mock.getString(eq(myDoubleArray))).andReturn("hello");}
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * @param expectedValue the array to which the specified incoming parameter to the mocked method
   * must have equal contents.
   * @return {@code null}. The return value is always ignored.
   */
  public static double[] aryEq(double[] expectedValue) {
    return EasyMock.aryEq(expectedValue);
  }

  /**
   * Expects a {@code float} array parameter that is equal to the given array, i.e. it has to
   * have the same length, and each element has to be equal.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(AndroidMock.eq(myFloatrArray))).andReturn("hello");}
   * 
   * Or, for illustration purposes (using static imports)
   * 
   * {@code expect(mock.getString(eq(myFloatArray))).andReturn("hello");}
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * @param expectedValue the array to which the specified incoming parameter to the mocked method
   * must have equal contents.
   * @return {@code null}. The return value is always ignored.
   */
  public static float[] aryEq(float[] expectedValue) {
    return EasyMock.aryEq(expectedValue);
  }

  /**
   * Expects an {@code int} array parameter that is equal to the given array, i.e. it has to
   * have the same length, and each element has to be equal.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(AndroidMock.eq(myIntArray))).andReturn("hello");}
   * 
   * Or, for illustration purposes (using static imports)
   * 
   * {@code expect(mock.getString(eq(myIntArray))).andReturn("hello");}
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * @param expectedValue the array to which the specified incoming parameter to the mocked method
   * must have equal contents.
   * @return {@code null}. The return value is always ignored.
   */
  public static int[] aryEq(int[] expectedValue) {
    return EasyMock.aryEq(expectedValue);
  }

  /**
   * Expects a {@code long} array parameter that is equal to the given array, i.e. it has to
   * have the same length, and each element has to be equal.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(AndroidMock.eq(myLongArray))).andReturn("hello");}
   * 
   * Or, for illustration purposes (using static imports)
   * 
   * {@code expect(mock.getString(eq(myLongArray))).andReturn("hello");}
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * @param expectedValue the array to which the specified incoming parameter to the mocked method
   * must have equal contents.
   * @return {@code null}. The return value is always ignored.
   */
  public static long[] aryEq(long[] expectedValue) {
    return EasyMock.aryEq(expectedValue);
  }

  /**
   * Expects a {@code short} array parameter that is equal to the given array, i.e. it has to
   * have the same length, and each element has to be equal.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(AndroidMock.eq(myShortArray))).andReturn("hello");}
   * 
   * Or, for illustration purposes (using static imports)
   * 
   * {@code expect(mock.getString(eq(myShortArray))).andReturn("hello");}
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * @param expectedValue the array to which the specified incoming parameter to the mocked method
   * must have equal contents.
   * @return {@code null}. The return value is always ignored.
   */
  public static short[] aryEq(short[] expectedValue) {
    return EasyMock.aryEq(expectedValue);
  }

  /**
   * Expects a {@code Object} array parameter that is equal to the given array, i.e. it has to
   * have the same length, and each element has to be equal.
   * 
   * E.g.
   * {@code AndroidMock.expect(mock.getString(AndroidMock.eq(myObjectArray))).andReturn("hello");}
   * 
   * Or, for illustration purposes (using static imports)
   * 
   * {@code expect(mock.getString(eq(myObjectArray))).andReturn("hello");}
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * @param <T> the type of the array, it is passed through to prevent casts.
   * @param expectedValue the array to which the specified incoming parameter to the mocked method
   * must have equal contents.
   * @return {@code null}. The return value is always ignored.
   */
  public static <T> T[] aryEq(T[] expectedValue) {
    return EasyMock.aryEq(expectedValue);
  }

  /**
   * Expects any {@code null} Object as a parameter.
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * @return {@code null}. The return value is always ignored.
   */
  @SuppressWarnings("unchecked")
  public static <T> T isNull() {
    return (T) EasyMock.isNull();
  }

  /**
   * Expects any {@code non-null} Object parameter.
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * @return {@code null}. The return value is always ignored.
   */
  @SuppressWarnings("unchecked")
  public static <T> T notNull() {
    return (T) EasyMock.notNull();
  }

  /**
   * Expects a {@code String} that contains a substring that matches the given regular
   * expression as a parameter to the mocked method.
   * 
   * See {@link java.util.regex.Matcher#find()} for more details.
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * @param regex the regular expression which must match some substring of the incoming parameter
   * to the mocked method.
   * @return {@code null}. The return value is always ignored.
   */
  public static String find(String regex) {
    return EasyMock.find(regex);
  }

  /**
   * Expects a {@code String} as a parameter to the mocked method, the entire length of which must
   * match the given regular expression. This is not to be confused with {@link #find(String)} which
   * matches the regular expression against any substring of the incoming parameter to the mocked
   * method.
   * 
   * See {@link java.util.regex.Matcher#matches()} for more details.
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * @param regex the regular expression against which the entire incoming parameter to the
   * mocked method must match.
   * @return {@code null}. The return value is always ignored.
   */
  public static String matches(String regex) {
    return EasyMock.matches(regex);
  }

  /**
   * Expects a {@code String} as a parameter to the mocked method that starts with the given prefix.
   * 
   * See {@link java.lang.String#startsWith(String)} for more details.
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * @param prefix the string that is expected to match against the start of any incoming
   * parameter to the mocked method.
   * @return {@code null}. The return value is always ignored.
   */
  public static String startsWith(String prefix) {
    return EasyMock.startsWith(prefix);
  }

  /**
   * Expects a {@code String} as a parameter to the mocked method that ends with the given
   * {@code suffix}.
   * 
   * See {@link java.lang.String#startsWith(String)} for more details.
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * @param suffix the string that is expected to match against the end of any incoming
   * parameter to the mocked method.
   * @return {@code null}. The return value is always ignored.
   */
  public static String endsWith(String suffix) {
    return EasyMock.endsWith(suffix);
  }

  /**
   * Expects a {@code double} as a parameter to the mocked method that has an absolute difference to
   * the given {@code expectedValue} that is less than the given {@code delta}.
   * 
   * The acceptable range of values is theoretically defined as any value {@code x} which satisfies
   * the following inequality: {@code expectedValue - delta &lt;= x &lt;= expectedValue + delta}.
   * 
   * In practice, this is only true when {@code expectedValue + delta} and
   * {@code expectedValue - delta} fall exactly on a precisely representable {@code double} value.
   * Normally, the acceptable range of values is defined as any value {@code x} which satisfies the
   * following inequality:
   * {@code expectedValue - delta &lt; x &lt; expectedValue + delta}.
   * 
   * E.g. {@code AndroidMock.expect(mockObject.getString(
   *    AndroidMock.eq(42.0, 0.1))).andReturn("hello world");}
   * 
   * The code snippet above will expect any {@code double} value greater than 41.9 and
   * less than 42.1.
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * @param expectedValue the center value of the expected range of values.
   * @param delta the acceptable level of inaccuracy before this expectation fails.
   * @return {@code 0}. The return value is always ignored.
   */
  public static double eq(double expectedValue, double delta) {
    return EasyMock.eq(expectedValue, delta);
  }

  /**
   * Expects a {@code float} as a parameter to the mocked method that has an absolute difference to
   * the given {@code expectedValue} that is less than the given {@code delta}.
   * 
   * The acceptable range of values is theoretically defined as any value {@code x} which satisfies
   * the following inequality: {@code expectedValue - delta &lt;= x &lt;= expectedValue + delta}.
   * 
   * In practice, this is only true when {@code expectedValue + delta} and
   * {@code expectedValue - delta} fall exactly on a precisely representable {@code float} value.
   * Normally, the acceptable range of values is defined as any value {@code x} which satisfies the
   * following inequality:
   * {@code expectedValue - delta &lt; x &lt; expectedValue + delta}.
   * 
   * E.g. {@code AndroidMock.expect(mockObject.getString(
   *    AndroidMock.eq(42.0f, 0.1f))).andReturn("hello world");}
   * 
   * The code snippet above will expect any {@code float} value greater than 41.9 and
   * less than 42.1.
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * @param expectedValue the center value of the expected range of values.
   * @param delta the acceptable level of inaccuracy before this expectation fails.
   * @return {@code 0}. The return value is always ignored.
   */
  public static float eq(float expectedValue, float delta) {
    return EasyMock.eq(expectedValue, delta);
  }

  /**
   * Expects an {@code Object} as a parameter to the mocked method that is the same as the given
   * value. This expectation will fail unless the incoming parameter is {@code ==} to the
   * {@code expectedValue} provided (i.e. the same {@code Object} reference).
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * @param <T> the type of the object, it is passed through to prevent casts.
   * @param expectedValue the exact object which is expected during replay.
   * @return {@code null}. The return value is always ignored.
   */
  public static <T> T same(T expectedValue) {
    return EasyMock.same(expectedValue);
  }

  /**
   * Expects a {@link java.lang.Comparable} argument equal to the given value according to
   * its {@link java.lang.Comparable#compareTo(Object)} method.
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * @param expectedValue the {@link java.lang.Comparable} value which is expected to be equal to
   * the incoming parameter to the mocked method according to the
   * {@link java.lang.Comparable#compareTo(Object)} method.
   * @return {@code null}. The return value is always ignored.
   */
  public static <T extends Comparable<T>> T cmpEq(Comparable<T> expectedValue) {
    return EasyMock.cmpEq(expectedValue);
  }

  /**
   * Expects an argument that will be compared using the provided {@link java.util.Comparator}, the
   * result of which will then be applied to the provided {@link org.easymock.LogicalOperator}
   * (e.g. {@link org.easymock.LogicalOperator#LESS_THAN},
   * {@link org.easymock.LogicalOperator#EQUAL},
   * {@link org.easymock.LogicalOperator#GREATER_OR_EQUAL}).
   * 
   * The following comparison will take place:
   * {@code comparator.compare(actual, expected) operator 0}
   * 
   * E.g.
   * For illustration purposes (using static imports):
   * 
   * {@code
   * expect(mockObject.getString(cmp("hi", CASE_INSENSITIVE_ORDER, LESS_THAN))).andReturn("hello");}
   *
   * {@code
   * AndroidMock.expect(mockObject.getString(AndroidMock.cmp("hi", String.CASE_INSENSITIVE_ORDER,
   *    LogicalOperator.LESS_THAN))).andReturn("hello");}
   * 
   * 
   * The above invocation indicates that the call to {@code mockObject.getString(String)} is
   * expecting any String which is lexically before "hi" (in a case insensitive ordering).
   * 
   * If this method is used for anything other than to set a parameter expectation as part of a
   * mock object's recording phase, then an {@code IllegalStateException} will be thrown.
   * 
   * @param expectedValue the expected value against which the incoming method parameter will be
   * compared.
   * @param comparator {@link java.util.Comparator} used to perform the comparison between the
   * expected value and the incoming parameter to the mocked method.
   * @param operator The comparison operator, usually one of
   * {@link org.easymock.LogicalOperator#LESS_THAN},
   * {@link org.easymock.LogicalOperator#LESS_OR_EQUAL},
   * {@link org.easymock.LogicalOperator#EQUAL}, {@link org.easymock.LogicalOperator#GREATER},
   * {@link org.easymock.LogicalOperator#GREATER_OR_EQUAL} 
   * @return {@code null}. The return value is always ignored.
   */
  public static <T> T cmp(T expectedValue, Comparator<? super T> comparator,
      LogicalOperator operator) {
    return EasyMock.cmp(expectedValue, comparator, operator);
  }

  /**
   * Expect any {@code Object} as a parameter to the mocked method, but capture it for later use.
   * 
   * {@link org.easymock.Capture} allows for capturing of the incoming value. Use
   * {@link org.easymock.Capture#getValue()} to retrieve the captured value.
   * 
   * @param <T> Type of the captured object
   * @param captured a container to hold the captured value, retrieved by
   * {@link org.easymock.Capture#getValue()}
   * @return {@code null}. The return value is always ignored.
   */
  public static <T> T capture(Capture<T> captured) {
    return EasyMock.capture(captured);
  }

  /**
   * Expect any {@code int/Integer} as a parameter to the mocked method, but capture it for later
   * use.
   * 
   * {@link org.easymock.Capture} allows for capturing of the incoming value. Use
   * {@link org.easymock.Capture#getValue()} to retrieve the captured value.
   * 
   * @param captured a container to hold the captured value, retrieved by
   * {@link org.easymock.Capture#getValue()}
   * @return {@code 0}. The return value is always ignored.
   */
  public static int capture(Capture<Integer> captured) {
    return EasyMock.capture(captured);
  }

  /**
   * Expect any {@code long/Long} as a parameter to the mocked method, but capture it for later
   * use.
   * 
   * {@link org.easymock.Capture} allows for capturing of the incoming value. Use
   * {@link org.easymock.Capture#getValue()} to retrieve the captured value.
   * 
   * @param captured a container to hold the captured value, retrieved by
   * {@link org.easymock.Capture#getValue()}
   * @return {@code 0}. The return value is always ignored.
   */
  public static long capture(Capture<Long> captured) {
    return EasyMock.capture(captured);
  }

  /**
   * Expect any {@code float/Float} as a parameter to the mocked method, but capture it for later
   * use.
   * 
   * {@link org.easymock.Capture} allows for capturing of the incoming value. Use
   * {@link org.easymock.Capture#getValue()} to retrieve the captured value.
   * 
   * @param captured a container to hold the captured value, retrieved by
   * {@link org.easymock.Capture#getValue()}
   * @return {@code 0}. The return value is always ignored.
   */
  public static float capture(Capture<Float> captured) {
    return EasyMock.capture(captured);
  }

  /**
   * Expect any {@code double/Double} as a parameter to the mocked method, but capture it for later
   * use.
   * 
   * {@link org.easymock.Capture} allows for capturing of the incoming value. Use
   * {@link org.easymock.Capture#getValue()} to retrieve the captured value.
   * 
   * @param captured a container to hold the captured value, retrieved by
   * {@link org.easymock.Capture#getValue()}
   * @return {@code 0}. The return value is always ignored.
   */
  public static double capture(Capture<Double> captured) {
    return EasyMock.capture(captured);
  }

  /**
   * Expect any {@code byte/Byte} as a parameter to the mocked method, but capture it for later
   * use.
   * 
   * {@link org.easymock.Capture} allows for capturing of the incoming value. Use
   * {@link org.easymock.Capture#getValue()} to retrieve the captured value.
   * 
   * @param captured a container to hold the captured value, retrieved by
   * {@link org.easymock.Capture#getValue()}
   * @return {@code 0}
   */
  public static byte capture(Capture<Byte> captured) {
    return EasyMock.capture(captured);
  }

  /**
   * Expect any {@code char/Character} as a parameter to the mocked method, but capture it for later
   * use.
   * 
   * {@link org.easymock.Capture} allows for capturing of the incoming value. Use
   * {@link org.easymock.Capture#getValue()} to retrieve the captured value.
   * 
   * @param captured a container to hold the captured value, retrieved by
   * {@link org.easymock.Capture#getValue()}
   * @return {@code 0}
   */
  public static char capture(Capture<Character> captured) {
    return EasyMock.capture(captured);
  }

  /**
   * Switches the given mock objects (more exactly: the controls of the mock
   * objects) to replay mode.
   * 
   * @param mocks the mock objects.
   */
  public static void replay(Object... mocks) {
    for (Object mockObject : mocks) {
      if (mockObject instanceof MockObject) {
        EasyMock.replay(((MockObject) mockObject).getDelegate___AndroidMock());
      } else {
        EasyMock.replay(mockObject);
      }
    }
  }

  /**
   * Resets the given mock objects (more exactly: the controls of the mock
   * objects) allowing the mock objects to be reused.
   * 
   * @param mocks the mock objects.
   */
  public static void reset(Object... mocks) {
    for (Object mockObject : mocks) {
      if (mockObject instanceof MockObject) {
        EasyMock.reset(((MockObject) mockObject).getDelegate___AndroidMock());
      } else {
        EasyMock.reset(mockObject);
      }
    }
  }

  /**
   * Resets the given mock objects (more exactly: the controls of the mock
   * objects) and change them in to mocks with nice behavior.
   * {@link #createNiceMock(Class, Object...)} has more details.
   * 
   * @param mocks the mock objects
   */
  public static void resetToNice(Object... mocks) {
    for (Object mockObject : mocks) {
      if (mockObject instanceof MockObject) {
        EasyMock.resetToNice(((MockObject) mockObject).getDelegate___AndroidMock());
      } else {
        EasyMock.resetToNice(mockObject);
      }
    }
  }

  /**
   * Resets the given mock objects (more exactly: the controls of the mock
   * objects) and turn them to a mock with default behavior. {@link #createMock(Class, Object...)}
   * has more details.
   * 
   * @param mocks the mock objects
   */
  public static void resetToDefault(Object... mocks) {
    for (Object mockObject : mocks) {
      if (mockObject instanceof MockObject) {
        EasyMock.resetToDefault(((MockObject) mockObject).getDelegate___AndroidMock());
      } else {
        EasyMock.resetToDefault(mockObject);
      }
    }
  }

  /**
   * Resets the given mock objects (more exactly: the controls of the mock
   * objects) and turn them to a mock with strict behavior.
   * {@link #createStrictMock(Class, Object...)} has more details.
   * 
   * @param mocks the mock objects
   */
  public static void resetToStrict(Object... mocks) {
    for (Object mockObject : mocks) {
      if (mockObject instanceof MockObject) {
        EasyMock.resetToStrict(((MockObject) mockObject).getDelegate___AndroidMock());
      } else {
        EasyMock.resetToStrict(mockObject);
      }
    }
  }

  /**
   * Verifies that all of the expected method calls for the given mock objects (more exactly: the
   * controls of the mock objects) have been executed.
   * 
   * The {@code verify} method captures the scenario where several methods were invoked correctly,
   * but some invocations did not occur. Typically, the {@code verify} method is the final thing
   * invoked in a test. 
   * 
   * @param mocks the mock objects.
   */
  public static void verify(Object... mocks) {
    for (Object mockObject : mocks) {
      if (mockObject instanceof MockObject) {
        EasyMock.verify(((MockObject) mockObject).getDelegate___AndroidMock());
      } else {
        EasyMock.verify(mockObject);
      }
    }
  }

  /**
   * Switches order checking of the given mock object (more exactly: the control
   * of the mock object) on or off. When order checking is on, the mock will expect the method
   * invokations to occur exactly in the order in which they appeared during the recording phase.
   * 
   * @param mock the mock object.
   * @param orderCheckingOn {@code true} to turn order checking on, {@code false} to turn it off.
   */
  public static void checkOrder(Object mock, boolean orderCheckingOn) {
    if (mock instanceof MockObject) {
      EasyMock.checkOrder(((MockObject) mock).getDelegate___AndroidMock(), orderCheckingOn);
    } else {
      EasyMock.checkOrder(mock, orderCheckingOn);
    }
  }

  /**
   * Reports an argument matcher. This method is needed to define custom argument
   * matchers.
   * 
   * For example:
   * 
   * {@code
   * AndroidMock.reportMatcher(new IntIsFortyTwo());
   * AndroidMock.expect(mockObject.getString(null)).andReturn("hello world");}
   * 
   * This example will expect a parameter for {@code mockObject.getString(int)} that matches the
   * conditions required by the {@code matches} method as defined by
   * {@link org.easymock.IArgumentMatcher#matches(Object)}.
   * 
   * @param matcher the matcher whose {@code matches} method will be applied to the incoming
   * parameter to the mocked method.
   */
  public static void reportMatcher(IArgumentMatcher matcher) {
    EasyMock.reportMatcher(matcher);
  }

  /**
   * Returns the arguments of the current mock method call, if inside an
   * {@code IAnswer} callback - be careful here, reordering parameters of a
   * method changes the semantics of your tests.
   * 
   * This method is only usable within an {@link org.easymock.IAnswer} instance. Attach an
   * {@link org.easymock.IAnswer} to an expectation by using the
   * {@link org.easymock.IExpectationSetters#andAnswer(org.easymock.IAnswer)} method.
   * 
   * E.g.
   * {@code AndroidMock.expect(mockObject.getString()).andAnswer(myAnswerCallback);}
   * 
   * @return the arguments of the current mock method call.
   * @throws IllegalStateException if called outside of {@code IAnswer}
   *         callbacks.
   */
  public static Object[] getCurrentArguments() {
    return EasyMock.getCurrentArguments();
  }

  /**
   * Makes the mock thread safe. The mock will be usable in a multithreaded
   * environment.
   * 
   * @param mock the mock to make thread safe.
   * @param threadSafe If the mock should be thread safe or not.
   */
  public static void makeThreadSafe(Object mock, boolean threadSafe) {
    if (mock instanceof MockObject) {
      EasyMock.makeThreadSafe(((MockObject) mock).getDelegate___AndroidMock(), threadSafe);
    } else {
      EasyMock.makeThreadSafe(mock, threadSafe);
    }
  }

  @SuppressWarnings("unchecked")
  private static <T, S> T getSubclassFor(Class<? super T> clazz, Class<S> delegateInterface,
      Object realMock, Object... args) {
    Class<T> subclass;
    String className = null;
    try {
      if (isAndroidClass(clazz)) {
        className = FileUtils.getSubclassNameFor(clazz, SdkVersion.getCurrentVersion());
      } else {
        className = FileUtils.getSubclassNameFor(clazz, SdkVersion.UNKNOWN);
      }
      subclass = (Class<T>) Class.forName(className);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("Could not find class for " + className
          + " which likely means that the mock-instrumented jar has not been created or else"
          + " is not being used in the current runtime environment. Try running MockGeneratorMain"
          + " in MockGenerator_deploy.jar or using the output of that execution as the input to"
          + " the dex/apk generation.", e);
    }
    Constructor<T> constructor = getConstructorFor(subclass, args);
    T newObject;
    try {
      newObject = constructor.newInstance(args);
    } catch (InstantiationException e) {
      throw new RuntimeException("Internal error instantiating new mock subclass"
          + subclass.getName(), e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(
          "Internal error - the new mock subclass' constructor was inaccessible", e);
    } catch (InvocationTargetException e) {
      throw new ExceptionInInitializerError(e);
    }
    Method[] methods = subclass.getMethods();
    Method setMethod;
    try {
      setMethod = subclass.getMethod("setDelegate___AndroidMock", delegateInterface);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException("Internal error - No setDelegate method found for " + "class "
          + subclass.getName() + " and param " + delegateInterface.getName(), e);
    }
    try {
      setMethod.invoke(newObject, realMock);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Internal error setting the delegate, expected "
          + newObject.getClass() + " to be subclass of " + clazz.getName());
    } catch (InvocationTargetException e) {
      throw new RuntimeException("Severe internal error, setDelegate threw an exception", e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException("Internal error, setDelegate method was inaccessible", e);
    }
    return newObject;
  }
  
  static boolean isUnboxableToPrimitive(Class<?> clazz, Object arg, boolean exactMatch) {
    if (!clazz.isPrimitive()) {
      throw new IllegalArgumentException(
          "Internal Error - The class to test against is not a primitive");
    }
    Class<?> unboxedType = null;
    if (arg.getClass().equals(Integer.class)) {
      unboxedType = Integer.TYPE;
    } else if (arg.getClass().equals(Long.class)) {
      unboxedType = Long.TYPE;
    } else if (arg.getClass().equals(Byte.class)) {
      unboxedType = Byte.TYPE;
    } else if (arg.getClass().equals(Short.class)) {
      unboxedType = Short.TYPE;
    } else if (arg.getClass().equals(Character.class)) {
      unboxedType = Character.TYPE;
    } else if (arg.getClass().equals(Float.class)) {
      unboxedType = Float.TYPE;
    } else if (arg.getClass().equals(Double.class)) {
      unboxedType = Double.TYPE;
    } else if (arg.getClass().equals(Boolean.class)) {
      unboxedType = Boolean.TYPE;
    } else {
      return false;
    }
    if (exactMatch) {
      return clazz == unboxedType;
    }
    return isAssignable(clazz, unboxedType);
  }
  
  private static boolean isAssignable(Class<?> to, Class<?> from) {
    if (to == Byte.TYPE) {
      return from == Byte.TYPE;
    } else if (to == Short.TYPE){
      return from == Byte.TYPE || from == Short.TYPE || from == Character.TYPE;
    } else if (to == Integer.TYPE || to == Character.TYPE) {
      return from == Byte.TYPE || from == Short.TYPE || from == Integer.TYPE
          || from == Character.TYPE;
    } else if (to == Long.TYPE) {
      return from == Byte.TYPE || from == Short.TYPE || from == Integer.TYPE || from == Long.TYPE
          || from == Character.TYPE;
    } else if (to == Float.TYPE) {
      return from == Byte.TYPE || from == Short.TYPE || from == Integer.TYPE
          || from == Character.TYPE || from == Float.TYPE;
    } else if (to == Double.TYPE) {
      return from == Byte.TYPE || from == Short.TYPE || from == Integer.TYPE || from == Long.TYPE
          || from == Character.TYPE || from == Float.TYPE || from == Double.TYPE;
    } else if (to == Boolean.TYPE) {
      return from == Boolean.TYPE;
    } else {
      return to.isAssignableFrom(from);
    }
  }
  
  @SuppressWarnings("unchecked")
  static <T> Constructor<T> getConstructorFor(Class<T> clazz, Object... args)
      throws SecurityException {
    Constructor<T>[] constructors = (Constructor<T>[]) clazz.getConstructors();
    Constructor<T> compatibleConstructor = null;
    for (Constructor<T> constructor : constructors) {
      Class<?>[] params = constructor.getParameterTypes();
      if (params.length == args.length) {
        boolean exactMatch = true;
        boolean compatibleMatch = true;
        for (int i = 0; i < params.length; ++i) {
          Object arg = args[i];
          if (arg == null) {
            arg = Void.TYPE;
          }
          if (!params[i].isAssignableFrom(arg.getClass())) {
            if (params[i].isPrimitive()) {
              exactMatch &= isUnboxableToPrimitive(params[i], arg, true);
              compatibleMatch &= isUnboxableToPrimitive(params[i], arg, false);
            } else {
              exactMatch = false;
              compatibleMatch = false;
            }
          }
        }
        if (exactMatch) {
          return constructor;
        } else if (compatibleMatch) {
          compatibleConstructor = constructor;
        }
      }
    }
    if (compatibleConstructor != null) {
      return compatibleConstructor;
    }
    List<String> argTypes = new ArrayList<String>(args.length);
    for (Object arg : args) {
      argTypes.add(arg == null ? "<null>" : arg.getClass().toString());
    }
    throw new IllegalArgumentException("Could not find the specified Constructor: "
        + clazz.getName() + "(" + argTypes + ")");
  }

  @SuppressWarnings("unchecked")
  private static <T> Class<T> getInterfaceFor(Class<T> clazz) {
    try {
      String className;
      if (isAndroidClass(clazz)) {
        className = FileUtils.getInterfaceNameFor(clazz, SdkVersion.getCurrentVersion());
      } else {
        className = FileUtils.getInterfaceNameFor(clazz, SdkVersion.UNKNOWN);
      }
      return (Class<T>) Class.forName(className);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("Could not find mock for " + clazz.getName()
          + "  -- Make sure to run the MockGenerator.jar on your test jar, and to "
          + "build the Android test APK using the modified jar created by MockGenerator", e);
    }
  }

  static boolean isAndroidClass(Class<?> clazz) {
    String packageName = clazz.getPackage().getName();
    return packageName.startsWith("android.") || packageName.startsWith("dalvik.")
        || packageName.startsWith("java.") || packageName.startsWith("javax.")
        || packageName.startsWith("org.xml.sax") || packageName.startsWith("org.xmlpull.v1")
        || packageName.startsWith("org.w3c.dom") || packageName.startsWith("org.apache.http")
        || packageName.startsWith("junit.");
  }
>>>>>>> 76aa07461566a5976980e6696204781271955163
}

