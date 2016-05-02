/*
 * unif_rand() produces different random numbers when compared to R
 * whatever the seed is. I think we can pass it until this gets a higher priorty.
 */
package org.renjin.primitives.random;


import org.apache.commons.math.random.MersenneTwister;
import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.primitives.annotations.Current;
import org.renjin.primitives.annotations.Primitive;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.IntVector;
import org.renjin.sexp.Symbols;


public class RNG {

  public MersenneTwister mersenneTwisterAlg = null;
  public RNGtype RNG_kind = RNGtype.MERSENNE_TWISTER; //default
  public N01type N01_kind = N01type.INVERSION; //default
  int[] dummy = new int[625];
  DoubleVector seeds;
  int randomseed = 0;
  static double i2_32m1 = 2.328306437080797e-10;/* = 1/(2^32 - 1) */
  public Context.Globals context;
  
  
  RNGTAB[] RNG_Table = new RNGTAB[]{
    new RNGTAB(RNGtype.WICHMANN_HILL, N01type.BUGGY_KINDERMAN_RAMAGE, "Wichmann-Hill"),
    new RNGTAB(RNGtype.MARSAGLIA_MULTICARRY, N01type.BUGGY_KINDERMAN_RAMAGE, "Marsaglia-MultiCarry"),
    new RNGTAB(RNGtype.SUPER_DUPER, N01type.BUGGY_KINDERMAN_RAMAGE, "Super-Duper"),
    new RNGTAB(RNGtype.MERSENNE_TWISTER, N01type.BUGGY_KINDERMAN_RAMAGE, "Mersenne-Twister"),
    new RNGTAB(RNGtype.KNUTH_TAOCP, N01type.BUGGY_KINDERMAN_RAMAGE, "Knuth-TAOCP"),
    new RNGTAB(RNGtype.USER_UNIF, N01type.BUGGY_KINDERMAN_RAMAGE, "User-supplied"),
    new RNGTAB(RNGtype.KNUTH_TAOCP2, N01type.BUGGY_KINDERMAN_RAMAGE, "Knuth-TAOCP-2002")};

  public RNG(Context.Globals globals){
	  this.context = globals;
  }
  
  @Primitive("RNGkind")
  public static IntVector RNGkind(@Current Context context, int kind, int normalkind) {
	RNG rng = context.getGlobals().rng;  
    try {
      rng.RNG_kind = RNGtype.values()[kind];
    } catch (Exception e) {
      throw new EvalException("RNGkind: unimplemented RNG kind " + kind);
    }

    try {
      rng.N01_kind = N01type.values()[normalkind];
    } catch (Exception e) {
      throw new EvalException("invalid Normal type in RNGkind");
    }

    rng.RNG_kind = RNGtype.values()[kind];
    rng.N01_kind = N01type.values()[normalkind];
    //System.out.println("Random generator is set to " + RNG.RNG_kind + " and " + RNG.N01_kind);
    return (new IntVector(rng.RNG_kind.ordinal(), rng.N01_kind.ordinal()));
  }

  /*
   * Primitives.
   */
  @Primitive("set.seed")
  public static void set_seed(@Current Context context, int seed, int kind, int normalkind) {
    RNG rng = context.getGlobals().rng;
	rng.randomseed = seed;
    RNGkind(context, kind, normalkind);
    switch (rng.RNG_kind) {
      case WICHMANN_HILL:
        throw new EvalException(rng.RNG_kind + " not implemented yet");

      case MARSAGLIA_MULTICARRY:
        throw new EvalException(rng.RNG_kind + " not implemented yet");

      case SUPER_DUPER:
        throw new EvalException(rng.RNG_kind + " not implemented yet");

      case MERSENNE_TWISTER:
        if (rng.mersenneTwisterAlg == null) {
          rng.mersenneTwisterAlg = new MersenneTwister(seed);
        } else {
          rng.mersenneTwisterAlg.setSeed(seed);
        }
        return;

      case KNUTH_TAOCP:
      case KNUTH_TAOCP2:
        throw new EvalException(rng.RNG_kind + " not implemented yet");
      case USER_UNIF:
        throw new EvalException(rng.RNG_kind + " not implemented yet");
      default:
        throw new EvalException(rng.RNG_kind + " not implemented yet");
    }
  }

  @Primitive("runif")
  public static DoubleVector runif(@Current Context context, int n, double a, double b) {
	RNG rng = context.getGlobals().rng;
    DoubleVector.Builder vb = new DoubleVector.Builder();
    for (int i = 0; i < n; i++) {
      vb.add(a + rng.unif_rand() * (b - a));
    }
    return (vb.build());
  }

  @Primitive("rnorm")
  public static DoubleVector rnorm(@Current Context context, int n, double mean, double sd) {
    DoubleVector.Builder vb = new DoubleVector.Builder();
    for (int i = 0; i < n; i++) {
      vb.add(Normal.rnorm(context.getGlobals(), mean, sd));
    }
    return (vb.build());
  }

  @Primitive("rgamma")
  public static DoubleVector rgamma(@Current Context context, int n, double shape, double scale) {
    DoubleVector.Builder vb = new DoubleVector.Builder();
    for (int i = 0; i < n; i++) {
      vb.add(Gamma.rgamma(context.getGlobals(), shape, scale));
    }
    return (vb.build());
  }

  @Primitive("rchisq")
  public static DoubleVector rchisq(@Current Context context, int n, double df) {
    DoubleVector.Builder vb = new DoubleVector.Builder();
    for (int i = 0; i < n; i++) {
      vb.add(ChiSquare.rchisq(context.getGlobals(), df));
    }
    return (vb.build());
  }

  @Primitive("rnchisq")
  public static DoubleVector rnchisq(@Current Context context, int n, double df, double ncp) {
    DoubleVector.Builder vb = new DoubleVector.Builder();
    for (int i = 0; i < n; i++) {
      vb.add(ChiSquare.rnchisq(context.getGlobals(), df, ncp));
    }
    return (vb.build());
  }

  @Primitive("rexp")
  public static DoubleVector rexp(@Current Context context, int n, double invrate) {
    DoubleVector.Builder vb = new DoubleVector.Builder();
    for (int i = 0; i < n; i++) {
      vb.add(Exponantial.rexp(context.getGlobals(), invrate));
    }
    return (vb.build());
  }

  @Primitive("rpois")
  public static DoubleVector rpois(@Current Context context, int n, double mu) {
    DoubleVector.Builder vb = new DoubleVector.Builder();
    for (int i = 0; i < n; i++) {
      vb.add(Poisson.rpois(context.getGlobals(), mu));
    }
    return (vb.build());
  }

  @Primitive("rsignrank")
  public static DoubleVector rsignrank(@Current Context context, int nn, double n) {
    DoubleVector.Builder vb = new DoubleVector.Builder();
    for (int i = 0; i < nn; i++) {
      vb.add(SignRank.rsignrank(context.getGlobals(), n));
    }
    return (vb.build());
  }

  @Primitive("rwilcox")
  public static DoubleVector rwilcox(@Current Context context, int nn, double m, double n) {
    DoubleVector.Builder vb = new DoubleVector.Builder();
    for (int i = 0; i < nn; i++) {
      vb.add(Wilcox.rwilcox(context.getGlobals(), m, n));
    }
    return (vb.build());
  }

  @Primitive("rgeom")
  public static DoubleVector rgeom(@Current Context context, int n, double p) {
    DoubleVector.Builder vb = new DoubleVector.Builder();
    for (int i = 0; i < n; i++) {
      vb.add(Geometric.rgeom(context.getGlobals(), p));
    }
    return (vb.build());
  }

  @Primitive("rt")
  public static DoubleVector rt(@Current Context context, int n, double df) {
    DoubleVector.Builder vb = new DoubleVector.Builder();
    for (int i = 0; i < n; i++) {
      vb.add(StudentsT.rt(context.getGlobals(), df));
    }
    return (vb.build());
  }

  @Primitive("rcauchy")
  public static DoubleVector rcauchy(@Current Context context, int n, double location, double scale) {
    DoubleVector.Builder vb = new DoubleVector.Builder();
    for (int i = 0; i < n; i++) {
      vb.add(Cauchy.rcauchy(context.getGlobals(), location, scale));
    }
    return (vb.build());
  }

  @Primitive("rlnorm")
  public static DoubleVector rlnorm(@Current Context context, int n, double meanlog, double sdlog) {
    DoubleVector.Builder vb = new DoubleVector.Builder();
    for (int i = 0; i < n; i++) {
      vb.add(LNorm.rlnorm(context.getGlobals(), meanlog, sdlog));
    }
    return (vb.build());
  }

  @Primitive("rlogis")
  public static DoubleVector rlogis(@Current Context context, int n, double location, double scale) {
    DoubleVector.Builder vb = new DoubleVector.Builder();
    for (int i = 0; i < n; i++) {
      vb.add(RLogis.rlogis(context.getGlobals(), location, scale));
    }
    return (vb.build());
  }

  @Primitive("rweibull")
  public static DoubleVector rweibull(@Current Context context, int n, double shape, double scale) {
    DoubleVector.Builder vb = new DoubleVector.Builder();
    for (int i = 0; i < n; i++) {
      vb.add(Weibull.rweibull(context.getGlobals(), shape, scale));
    }
    return (vb.build());
  }

  @Primitive("rnbinom")
  public static DoubleVector rnbinom(@Current Context context, int n, double size, double prob) {
    DoubleVector.Builder vb = new DoubleVector.Builder();
    for (int i = 0; i < n; i++) {
      vb.add(NegativeBinom.rnbinom(context.getGlobals(), size, prob));
    }
    return (vb.build());
  }

  @Primitive("rnbinom_mu")
  public static DoubleVector rnbinom_mu(@Current Context context, int n, double size, double mu) {
    DoubleVector.Builder vb = new DoubleVector.Builder();
    for (int i = 0; i < n; i++) {
      vb.add(NegativeBinom.rnbinom_mu(context.getGlobals(), size, mu));
    }
    return (vb.build());
  }
  
  @Primitive("rbinom")
  public static DoubleVector rbinom(@Current Context context, int n, double size, double prob) {
    DoubleVector.Builder vb = new DoubleVector.Builder();
    for (int i = 0; i < n; i++) {
      vb.add(Binom.rbinom(context.getGlobals(), size, prob));
    }
    return (vb.build());
  }
  
  
  @Primitive("rf")
  public static DoubleVector rf(@Current Context context, int n, double df1, double df2) {
    DoubleVector.Builder vb = new DoubleVector.Builder();
    for (int i = 0; i < n; i++) {
      vb.add(F.rf(context.getGlobals(), df1, df2));
    }
    return (vb.build());
  }

  @Primitive("rbeta")
  public static DoubleVector rbeta(@Current Context context, int n, double shape1, double shape2) {
    DoubleVector.Builder vb = new DoubleVector.Builder();
    for (int i = 0; i < n; i++) {
      vb.add(Beta.rbeta(context.getGlobals(), shape1, shape2));
    }
    return (vb.build());
  }
  
  @Primitive("rhyper")
  public static DoubleVector rhyper(@Current Context context, int nn, double m, double n, double k){
    DoubleVector.Builder vb = new DoubleVector.Builder();
    for (int i = 0; i < nn; i++) {
      vb.add(HyperGeometric.Random_hyper_geometric.rhyper(context.getGlobals(), m, n, k));
    }
    return (vb.build());
  }

  @Primitive("rmultinom")
  public static DoubleVector rmultinom(@Current Context context, int n, int size, DoubleVector prob){
    DoubleVector.Builder vb = new DoubleVector.Builder();
    int[] RN = new int[prob.length()];
    for (int i=0;i<n;i++){
    	Multinomial.rmultinom(context.getGlobals(), size, prob.toDoubleArray(), prob.length(), RN);
    	for (int j = 0; j < prob.length(); j++) {
    		vb.add(RN[j]);
    	}
    }
    vb.setAttribute(Symbols.DIM, new IntVector(prob.length(), n));
    return (vb.build());
  }
  /*
   * One of the Most important method in RNG
   * Before creating a random number from the distribution D,
   * we generate a uniform distributed random variable. 
   * 
   * Generated random numbers depend on the algorithm used.
   * As in original interpreter, the default algorithm is MERSENNE_TWISTER.
   * 
   * MERSENNE_TWISTER algorithm is imported from the apache commons math api.
   * But there is a small problem with this. The original interpreter and the renjin
   * produces different pseudo random numbers even the seed is same.
   * 
   * I am leaving this as is, I think it is not a real problem for now, somebody can 
   * correct the mechanism underlying the uniform random number generation for consistency 
   * with the original interpreter. 
   * 
   * Because I have not got the desired outputs, I can not test my generated random numbers. But one can 
   * see that, for example a sample of 1000 random numbers from a Chisquare(15) distribution has an
   * average of nearly 15. Similarly, a sample drawn from a Normal (0,1) distribution has a mean and variance
   * nearly zero and one, respectively.
   * 
   * mhsatman
   */
  public double unif_rand() {
    double value;

    switch (this.RNG_kind) {

      case WICHMANN_HILL:
        throw new EvalException(RNG_kind + " not implemented yet");

      case MARSAGLIA_MULTICARRY:
        throw new EvalException(RNG_kind + " not implemented yet");

      case SUPER_DUPER:
        throw new EvalException(RNG_kind + " not implemented yet");

      case MERSENNE_TWISTER:
        if (mersenneTwisterAlg == null) {
          if (this.randomseed == 0) {
            Randomize(RNG_kind);
          }
          mersenneTwisterAlg = new MersenneTwister((long) this.randomseed);
        }
        return (mersenneTwisterAlg.nextDouble());

      case KNUTH_TAOCP:
      case KNUTH_TAOCP2:
        throw new EvalException(RNG_kind + " not implemented yet");
      case USER_UNIF:
        throw new EvalException(RNG_kind + " not implemented yet");
      default:
        throw new EvalException(RNG_kind + " not implemented yet");
    }
  }

  /*
   * This part of R is platform dependent. this formula is random itself :)
   */
  public void Randomize(RNGtype kind) {
    int sseed;
    sseed = (int) (new java.util.Date()).getTime();
    this.randomseed = sseed;
    switch (RNG_kind) {

      case WICHMANN_HILL:
        throw new EvalException(RNG_kind + " not implemented yet");

      case MARSAGLIA_MULTICARRY:
        throw new EvalException(RNG_kind + " not implemented yet");

      case SUPER_DUPER:
        throw new EvalException(RNG_kind + " not implemented yet");

      case MERSENNE_TWISTER:
        if (mersenneTwisterAlg == null) {
          mersenneTwisterAlg = new MersenneTwister(sseed);
        } else {
          mersenneTwisterAlg.setSeed(sseed);
        }
        return;

      case KNUTH_TAOCP:
      case KNUTH_TAOCP2:
        throw new EvalException(RNG_kind + " not implemented yet");
      case USER_UNIF:
        throw new EvalException(RNG_kind + " not implemented yet");
      default:
        throw new EvalException(RNG_kind + " not implemented yet");
    }
  }
}

